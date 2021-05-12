package com.airqualitysensors;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.airqualitysensors.Utilities.DataManager;
import com.airqualitysensors.Utilities.Database.TimedData;
import com.airqualitysensors.Utilities.Database.Type;
import com.airqualitysensors.Utilities.Database.TypeFactory;
import com.airqualitysensors.Utilities.MyViewModelFactory;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int LEGEND_COLOR[] = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};
    private View currentView;
    private DataManager viewModel;
    private GraphView graph;
    private boolean showAllMode = false;

    public static DataPoint[] convertDataToPoints(List<TimedData> data) {
        DataPoint[] result = new DataPoint[data.size()];
        Collections.sort(data, new Comparator<TimedData>() {
            @Override
            public int compare(TimedData data, TimedData t1) {
                return data.time.compareTo(t1.time);
            }
        });
        int i = 0;
        for (TimedData d : data) {
            result[i] = new DataPoint(d.time, d.value);
            i++;
        }
        return result;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            currentView = findViewById(R.id.co2_row);
            rowClick(currentView);
        }

        graph = findViewById(R.id.graph);
        graph.setVisibility(View.VISIBLE);
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(30);
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    Date date = new Date((long) value);
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                    return format.format(date);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX) + viewModel.getCurrentType().unit;
                }
            }
        });

        viewModel = new ViewModelProvider(this, new MyViewModelFactory(getApplication())).get(DataManager.class);

        viewModel.getData().observe(this, this::onChanged);

        viewModel.getCurrentData().observe(this, currentData -> {
            ((TextView) findViewById(R.id.co2_value)).setText(formatDataValue(currentData, TypeFactory.CO2));
            ((TextView) findViewById(R.id.humidity_value)).setText(formatDataValue(currentData, TypeFactory.HUMIDITY));
            ((TextView) findViewById(R.id.temp_value)).setText(formatDataValue(currentData, TypeFactory.TEMPERATURE));
            ((TextView) findViewById(R.id.light_value)).setText(formatDataValue(currentData, TypeFactory.LIGHT));
        });
    }

    private String formatDataValue(TimedData[] currentData, int type) {
        return "" + (currentData[type] != null ? currentData[type].value : "--") + TypeFactory.getInstanceOf(type).unit;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            getMenuInflater().inflate(R.menu.menu, menu);
        else
            menu.clear();
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    public void rowClick(View view) {
        currentView.setBackground(null);
        currentView = view;
        view.setBackground(ContextCompat.getDrawable(this, R.drawable.round_rectangle));

        if (viewModel != null)
            switch (view.getId()) {
                case R.id.co2_row:
                    viewModel.changeType(TypeFactory.getInstanceOf(TypeFactory.CO2));
                    break;
                case R.id.humidity_row:
                    viewModel.changeType(TypeFactory.getInstanceOf(TypeFactory.HUMIDITY));
                    break;
                case R.id.temp_row:
                    viewModel.changeType(TypeFactory.getInstanceOf(TypeFactory.TEMPERATURE));
                    break;
                case R.id.light_row:
                    viewModel.changeType(TypeFactory.getInstanceOf(TypeFactory.LIGHT));
                    break;
            }
    }

    @SuppressLint("NonConstantResourceId")
    public void menuClick(MenuItem item) {
        showAllMode = false;
        if (viewModel.getTypes() != null)
            switch (item.getItemId()) {
                case R.id.menu_co2:
                    viewModel.changeType(TypeFactory.getInstanceOf(TypeFactory.CO2));
                    break;
                case R.id.menu_humidity:
                    viewModel.changeType(TypeFactory.getInstanceOf(TypeFactory.HUMIDITY));
                    break;
                case R.id.menu_temp:
                    viewModel.changeType(TypeFactory.getInstanceOf(TypeFactory.TEMPERATURE));
                    break;
                case R.id.menu_light:
                    viewModel.changeType(TypeFactory.getInstanceOf(TypeFactory.LIGHT));
                    break;
                case R.id.menu_all: {
                    showAllMode = true;
                    onChanged(null);
                }
                break;
            }
    }

    private void onChanged(List<TimedData> data) {
        graph.removeAllSeries();
        final DataPoint[][] points = {new DataPoint[0]};
        if (showAllMode) {
            int i = 0;
            List<Type> types = viewModel.getTypes();
            for (; i < types.size(); i++) {
                int finalI = i;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        points[0] = convertDataToPoints(viewModel.getDataByType(types.get(finalI)));
                    }
                });
                thread.start();
                LineGraphSeries<DataPoint> serie = new LineGraphSeries<DataPoint>(points[0]);
                serie.setTitle(types.get(i).name);
                serie.setColor(LEGEND_COLOR[i]);
                graph.addSeries(serie);
            }
            graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        } else {
            points[0] = convertDataToPoints(data);
            graph.addSeries(new LineGraphSeries<DataPoint>(points[0]));
            graph.getLegendRenderer().setVisible(false);
            graph.getGridLabelRenderer().setVerticalLabelsVisible(true);
        }

        if (points[0].length != 0) {
            graph.getViewport().setMinX(points[0][0].getX());
            graph.getViewport().setMaxX(points[0][points[0].length - 1].getX());
            graph.getViewport().setXAxisBoundsManual(true);
        }
    }
}
