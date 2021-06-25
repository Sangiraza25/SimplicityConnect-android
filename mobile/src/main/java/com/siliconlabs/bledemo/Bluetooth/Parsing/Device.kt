/*
 * Bluegiga’s Bluetooth Smart Android SW for Bluegiga BLE modules
 * Contact: support@bluegiga.com.
 *
 * This is free software distributed under the terms of the MIT license reproduced below.
 *
 * Copyright (c) 2013, Bluegiga Technologies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files ("Software")
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF 
 * ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A  PARTICULAR PURPOSE.
 */
package com.siliconlabs.bledemo.Bluetooth.Parsing

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import com.siliconlabs.bledemo.Bluetooth.BLE.BluetoothDeviceInfo
import com.siliconlabs.bledemo.Bluetooth.BLE.ScanRecordCompat.Companion.parseFromBytes
import com.siliconlabs.bledemo.Bluetooth.BLE.ScanResultCompat

// Device - it's wrapper for BLE device object
class Device : BluetoothDeviceInfo {
    var bluetoothGatt: BluetoothGatt? = null

    constructor() {}
    constructor(bluetoothDevice: BluetoothDevice, rssi: Int, scanRecord: ByteArray?) {
        device = bluetoothDevice
        hasAdvertDetails = false
        bluetoothGatt = null
        isOfInterest = true
        val result = ScanResultCompat()
        result.device = device
        result.rssi = rssi
        result.timestampNanos = System.currentTimeMillis() * 1000
        result.scanRecord = parseFromBytes(scanRecord)
        result.advertData = ScanRecordParser.getAdvertisements(scanRecord)
        scanInfo = result
    }

    override fun clone(): BluetoothDeviceInfo {
        val retVal: Device = super.clone() as Device
        retVal.bluetoothGatt = bluetoothGatt
        retVal.hasAdvertDetails = hasAdvertDetails
        return retVal
    }

    fun getBluetoothDevice(): BluetoothDevice {
        return device
    }
}