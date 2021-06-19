package com.airqualitysensors.Utilities;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.airqualitysensors.Utilities.Bluetooth.BluetoothManager;
import com.airqualitysensors.Utilities.Bluetooth.ConnectionListener;
import com.airqualitysensors.Utilities.Bluetooth.Data;
import com.airqualitysensors.Utilities.Database.DataBase;
import com.airqualitysensors.Utilities.Database.DataDao;
import com.airqualitysensors.Utilities.Database.TimedData;
import com.airqualitysensors.Utilities.Database.Type;
import com.airqualitysensors.Utilities.Database.TypeFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

public class DataManager extends AndroidViewModel implements Observer {
    private DataBase db;
    private DataDao dao;
    private MutableLiveData<List<TimedData>> data;
    private MutableLiveData<TimedData[]> currentData;
    private List<Type> types;

    private Type currentType;
    private final Queue<Runnable> tasks;
    private Thread taskExecutor;

    private BluetoothManager bluetoothManager;

    public DataManager(@NonNull Application application) {
        super(application);
        db = Room.databaseBuilder(application, DataBase.class, "timeddata").build();
        dao = db.dataDao();
        data = new MutableLiveData<>();
        currentData = new MutableLiveData<>();

         Thread t = new Thread(){
            @Override
            public void run() {
                dao.deleteData();
                dao.deleteTypes();
                initTypes();
                initData();
            }
        };
        //t.start();
        tasks = new LinkedTransferQueue<>();
        tasks.add(typeLoader());
        tasks.add(dataLoader());
        executeTasks();
    }

    public void connect(String deviceName, ConnectionListener listener) {
        bluetoothManager = new BluetoothManager(deviceName);
        bluetoothManager.registerConnectionListener(listener);
        bluetoothManager.addObserver(this);
        Thread temp = new Thread(() -> bluetoothManager.connect());
        temp.start();
    }

    private void executeTasks(){
        if(taskExecutor == null || !taskExecutor.isAlive()) {
            taskExecutor = new Thread() {
                @Override
                public void run() {
                    Runnable r;
                    while ((r = tasks.poll()) != null) {
                        try {
                            Thread temp = new Thread(r);
                            temp.start();
                            temp.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            taskExecutor.start();
        }
    }

    private void initData(){
        TimedData[] data = new TimedData[5];
        Calendar c = Calendar.getInstance();
        Type co2Type = TypeFactory.getInstanceOf(TypeFactory.CO2);
        data[0] = new TimedData(){{
            typeId = co2Type.typeId;
            time = c.getTime();
            value = 0;
        }};
        c.add(Calendar.DAY_OF_MONTH, 1);
        data[1] = new TimedData(){{
            typeId = co2Type.typeId;
            time = c.getTime();
            value = 2;
        }};
        c.add(Calendar.DAY_OF_MONTH, 1);
        data[2] = new TimedData(){{
            typeId = co2Type.typeId;
            time = c.getTime();
            value = 4;
        }};
        c.add(Calendar.DAY_OF_MONTH, 1);
        data[3] = new TimedData(){{
            typeId = co2Type.typeId;
            time = c.getTime();
            value = 2;
        }};
        c.add(Calendar.DAY_OF_MONTH, 1);
        data[4] = new TimedData(){{
            typeId = co2Type.typeId;
            time = c.getTime();
            value = 3;
        }};

        dao.insertAll(data);
    }

    private void initTypes(){
        DataDao dao = db.dataDao();
        dao.insertAll(TypeFactory.types);
    }

    private Runnable dataLoader() {
        return () -> {
            if(currentType != null) {
                List<TimedData> d = dao.getDataByType(currentType.typeId);
                data.postValue(d);
            }
        };
    }

    public void changeType(Type type){
        currentType = type;
        tasks.add(dataLoader());
        executeTasks();
    }

    public void addData(TimedData data){
        Thread t = new Thread() {
            @Override
            public void run() {
                dao.insertAll(data);
                if(tasks.isEmpty()) {
                    tasks.add(dataLoader());
                    executeTasks();
                }
            }
        };
        t.start();

        TimedData[] td = currentData.getValue() == null ? new TimedData[TypeFactory.types.length] : currentData.getValue();
        td[data.typeId] = data;
        currentData.postValue(td);
    }

    private Runnable typeLoader() {
            return () -> {
                    if (types==null) {
                        types = dao.getAllTypes();
                    }
                    if(!types.isEmpty())
                        currentType = types.get(3);
            };
    }

    public List<TimedData> getDataByType(Type t) {
        return dao.getDataByType(t.typeId);
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        db.close();
        bluetoothManager.cancel();
    }

    public List<Type> getTypes() {
        return types;
    }

    public void setTypes(List<Type> types) {
        this.types = types;
    }

    public LiveData<TimedData[]> getCurrentData(){ return currentData; }

    public LiveData<List<TimedData>> getData(){
        return data;
    }

    public Type getCurrentType() {
        return currentType;
    }

    @Override
    public void update(Observable observable, Object o) {
        while(observable instanceof BluetoothManager && !bluetoothManager.dataQueue.isEmpty()){
            Data data = bluetoothManager.dataQueue.poll();
            try {
                addData(new TimedData() {{
                            typeId = TypeFactory.getInstanceOf(TypeFactory.getType(data != null ? data.getType() : null)).typeId;
                            time = Calendar.getInstance().getTime();
                            value = Float.parseFloat(data.getData());
                        }});
            }catch (Exception e){
            Log.e("Data Manager", e.toString());
            }
        }
    }

}
