package com.airqualitysensors.Utilities.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.Observable;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedTransferQueue;

public class BluetoothManager extends Observable {
    private UUID MY_UUID;
    private String address;
    private BluetoothSocket mmSocket;
    private String TAG = "BluetoothManager";
    private Thread connectionThread;
    public Queue<Data> dataQueue;

    private boolean noDevice_Error = true;
    private boolean closeRequested = false;

    public final String TYPE = "type";
    public final String DATA = "data";

    public BluetoothManager(){
        dataQueue = new LinkedTransferQueue<>();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) return;
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice bt = null;
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if(device.getName().equals("HC-06"))
                    bt = device;
            }
            if(bt != null)
                address = bt.getAddress();
            else
                noDevice_Error = true;
        }

        if(bt != null){
            BluetoothSocket tmp = null;
            try {
                MY_UUID = bt.getUuids()[0].getUuid();
                        // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                mmSocket = bluetoothAdapter.getRemoteDevice(address).createInsecureRfcommSocketToServiceRecord(MY_UUID);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                noDevice_Error = false;
            } catch (IOException e) {
                noDevice_Error = true;
                Log.e(TAG, "Socket's create() method failed", e);
            }
        }
    }

    public void connect(){
        if(!noDevice_Error && connectionThread != null)
            return;
        connectionThread = new Thread(() -> {
            try {
                tryConnect();
                collectPacket();
            } catch (IOException e) {

                if(mmSocket != null) {
                    try {
                        mmSocket.close();
                    } catch (IOException closeException) {
                        Log.e(TAG, "Could not close the client socket", closeException);
                        closeException.printStackTrace();
                    }
                }
                e.printStackTrace();
            }
        });

        connectionThread.start();
    }

    public boolean isConnected(){
        return connectionThread.isAlive();
    }

    /**
     * Blocking method
     */
    private void tryConnect() throws IOException {
            Log.i(TAG, "Connection...");
            if(mmSocket == null) {
                Log.e(TAG, "tryConnect: no bluetooth device detected");
                return;
            }
            mmSocket.connect();
            Log.i(TAG, "Connected");
    }

    /**
     * Blocking method
     */
    private void collectPacket() throws IOException {
        if(mmSocket == null){
            return;
        }
            Log.i(TAG, "Hello said");
            mmSocket.getOutputStream().write(new String("hello").getBytes());
            Log.i(TAG, "Listening...");
            while(!closeRequested) {
                String s;
                s = readPacket();
                if(s.equals(TYPE)) {
                    String type = readPacket();
                    if(readPacket().equals(DATA)) {
                        dataQueue.add(new Data(type, readPacket()));
                        setChanged();
                        notifyObservers();
                    }
                }
            }

        cancel();
    }

    private String readPacket() {
        char c;
        int i = 0;
        StringBuilder sb = new StringBuilder();
        try {
            while ((c = (char) mmSocket.getInputStream().read()) != '\n' && i<20) {
                sb.append(c);
                i++;
            }
        }catch (IOException e){
            Log.e(TAG, e.getMessage());
            return "error";
        }
        Log.i(TAG, "Received" + sb);
        return sb.toString();
    }


    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            closeRequested = true;
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
