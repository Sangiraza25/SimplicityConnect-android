package com.siliconlabs.bledemo.home_screen.menu_items

import androidx.annotation.DrawableRes
import com.siliconlabs.bledemo.bluetooth.services.BluetoothService

class OTADemo( @DrawableRes imageResId: Int,
title: String,
description: String
) : DemoMenuItem(imageResId, title, description
) {

    override val connectType: BluetoothService.GattConnectType = BluetoothService.GattConnectType.WIFI_OTA_UPDATE
}