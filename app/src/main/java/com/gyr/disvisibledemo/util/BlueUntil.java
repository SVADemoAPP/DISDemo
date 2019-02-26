package com.gyr.disvisibledemo.util;

import android.bluetooth.BluetoothAdapter;

public class BlueUntil {
    private static BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    public static BluetoothAdapter getBluetoothAdapter(){
        return mAdapter;
    }

    public static boolean isBluetoothAvaliable(){
        return mAdapter != null;
    }


}
