package net.staric.kronometer.backend;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import net.staric.kronometer.models.Event;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

class BluetoothSensorThread extends Thread {
    private final UUID BLUETOOTH_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final Intent notifyDataChangedIntent;
    private final Intent notifyStatusChangedIntent;

    private String deviceAddress;
    private final KronometerService kronometerService;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice btDevice;
    private BluetoothSocket btSocket;
    private InputStream inStream;

    public BluetoothSensorThread(String deviceAddress, KronometerService kronometerService) {
        this.deviceAddress = deviceAddress;
        this.kronometerService = kronometerService;

        notifyStatusChangedIntent = new Intent(KronometerService.STATUS_CHANGED_ACTION);
        notifyDataChangedIntent = new Intent(KronometerService.DATA_CHANGED_ACTION);
    }

    @Override
    public void run() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        while (!isInterrupted()) {
            if (bluetoothAdapter == null) {
                changeBluetoothStatus("This device does not support bluetooth");
                return;
            }
            else if (!bluetoothAdapter.isEnabled()) {
                changeBluetoothStatus("Bluetooth is not enabled");
                //TODO: Ask for bluetooth

            } else if (!openSocket()) {
                changeBluetoothStatus("Sensor is not available");
            } else if (!openStream()) {
                changeBluetoothStatus("Error connecting to sensor");
            } else {
                changeBluetoothStatus("Sensor connected");
                listenForData();
                changeBluetoothStatus("Sensor disconnected");
            }

            try {
                sleep(10000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private boolean openSocket() {
        btDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
        btSocket = null;
        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(BLUETOOTH_SERIAL);
            btSocket.connect();
            return true;
        } catch (IOException e) {
            try {
                if (btSocket != null)
                    btSocket.close();
            } catch (IOException e1) {}
            return false;
        }
    }

    private boolean openStream() {
        try {
            inStream = btSocket.getInputStream();
            return true;
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e1) {}
            return false;
        }
    }

    private void listenForData() {
        try {
            while (!this.isInterrupted()) {
                readData();
            }
        }
        catch (IOException ex) {}
        finally {
            try {
                inStream.close();
            } catch (IOException e) {}

            try {
                btSocket.close();
            } catch (IOException e) {}
        }
    }

    private void readData() throws IOException {
        byte[] data = new byte[1];
        inStream.read(data);
        if (data[0] == 'E')
            kronometerService.addEvent(new Event(new Date()));
            notifyDataChanged();
    }

    private void changeBluetoothStatus(String status) {
        notifyBluetoothStatusChanged(status);
    }

    protected void notifyBluetoothStatusChanged(String status) {
        notifyStatusChangedIntent.putExtra("bluetoothStatus", status);
        kronometerService.sendBroadcast(notifyStatusChangedIntent);
    }

    protected void notifyDataChanged() {
        kronometerService.sendBroadcast(notifyDataChangedIntent);
    }
}
