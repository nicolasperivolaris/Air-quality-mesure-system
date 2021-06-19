package com.airqualitysensors.Utilities.Database;

public class TypeFactory {

    public static final int ERROR = -1;
    public static final int CO2 = 0;
    public static final int HUMIDITY = 1;
    public static final int TEMPERATURE = 2;
    public static final int LIGHT = 3;
    public static final int PRESSURE = 4;

    public final static  Type[] types = {new Type(){{
        typeId = CO2;
        name = "CO2";
        unit = "ppm";
    }}, new Type(){{
        typeId = HUMIDITY;
        name = "Humidity";
        unit = "%";
    }},  new Type(){{
        typeId = TEMPERATURE;
        name = "Temperature";
        unit = "C°";
    }}, new Type(){{
        typeId = LIGHT;
        name = "Lighting";
        unit = "lux";
    }}, new Type(){{
        typeId = PRESSURE;
        name = "Pressure";
        unit = "hP";
    }}};

    public static Type getInstanceOf(int type){
        return types[type];
    }

    public static int getType(String s){
        switch (s.trim()){
            case "co2" : return CO2;
            case "humi" : return HUMIDITY;
            case "temp": return TEMPERATURE;
            case "lumi" : return LIGHT;
            case "pres" : return PRESSURE;
            default: return ERROR;
        }
    }
}
