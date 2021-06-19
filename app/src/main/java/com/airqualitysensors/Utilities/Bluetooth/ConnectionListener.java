package com.airqualitysensors.Utilities.Bluetooth;

public interface ConnectionListener {
    void connectionFailed();
    void connectionBroken();
    void connected();
}
