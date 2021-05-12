package com.airqualitysensors.Utilities.Bluetooth;

public class Data{

    protected String type;
    protected String data;

    Data(String type, String data){
        this.type = type;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Data{" +
                "type='" + type + '\'' +
                ", data='" + data + '\'' +
                '}';
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}