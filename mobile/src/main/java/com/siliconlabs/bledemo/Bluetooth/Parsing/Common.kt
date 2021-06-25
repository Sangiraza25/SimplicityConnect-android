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

import android.content.Context
import com.siliconlabs.bledemo.Bluetooth.Parsing.Engine.Companion.instance
import com.siliconlabs.bledemo.Browser.Activities.DeviceServicesActivity
import com.siliconlabs.bledemo.R
import com.siliconlabs.bledemo.Utils.Constants
import com.siliconlabs.bledemo.Utils.Converters.getDecimalValue
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.pow

// Common - contains common members and methods for whole application
object Common {
    const val BLUEGIGA_URL_ORIGINAL = "http://www.bluegiga.com/en-US/products/bluetooth-4.0-modules/"

    const val PROPERTY_VALUE_WRITE = "WRITE"
    const val PROPERTY_VALUE_WRITE_NO_RESPONSE = "WRITE NO RESPONSE"
    const val PROPERTY_VALUE_READ = "READ"
    const val PROPERTY_VALUE_NOTIFY = "NOTIFY"
    const val PROPERTY_VALUE_INDICATE = "INDICATE"
    const val PROPERTY_VALUE_SIGNED_WRITE = "SIGNED WRITE"
    const val PROPERTY_VALUE_EXTENDED_PROPS = "EXTENDED PROPS"
    const val PROPERTY_VALUE_BROADCAST = "BROADCAST"

    const val MENU_CONNECT = 0
    const val MENU_DISCONNECT = 1
    const val MENU_SCAN_RECORD_DETAILS = 2

    private const val START_INDEX_UUID = 4
    private const val END_INDEX_UUID = 8

    var FLOAT_POSITIVE_INFINITY = 0x007FFFFE
    var FLOAT_NaN = 0x007FFFFF
    var FLOAT_NRes = 0x00800000
    var FLOAT_RESERVED = 0x00800001
    var FLOAT_NEGATIVE_INFINITY = 0x00800002

    var FIRST_FLOAT_RESERVED_VALUE = FLOAT_POSITIVE_INFINITY

    var SFLOAT_POSITIVE_INFINITY = 0x07FE
    var SFLOAT_NaN = 0x07FF
    var SFLOAT_NRes = 0x0800
    var SFLOAT_RESERVED = 0x0801
    var SFLOAT_NEGATIVE_INFINITY = 0x0802

    var FONT_SCALE_SMALL = 0.85f
    var FONT_SCALE_NORMAL = 1.0f
    var FONT_SCALE_LARGE = 1.15f
    var FONT_SCALE_XLARGE = 1.3f

    private var FIRST_SFLOAT_RESERVED_VALUE = SFLOAT_POSITIVE_INFINITY

    private val reservedSFloatValues = floatArrayOf(SFLOAT_POSITIVE_INFINITY.toFloat(), SFLOAT_NaN.toFloat(), SFLOAT_NaN.toFloat(), SFLOAT_NaN.toFloat(),
            SFLOAT_NEGATIVE_INFINITY.toFloat())

    private val reservedFloatValues = floatArrayOf(FLOAT_POSITIVE_INFINITY.toFloat(), FLOAT_NaN.toFloat(), FLOAT_NaN.toFloat(), FLOAT_NaN.toFloat(),
            FLOAT_NEGATIVE_INFINITY.toFloat())

    // Converts UUID from 16-bit to 128-bit form
    fun convert16to128UUID(uuid: String): String {
        return Consts.BLUETOOTH_BASE_UUID_PREFIX + uuid + Consts.BLUETOOTH_BASE_UUID_POSTFIX
    }

    // Converts UUID from 128-bit to 16-bit form
    fun convert128to16UUID(uuid: String): String {
        return uuid.substring(START_INDEX_UUID, END_INDEX_UUID)
    }

    // Compares two uuid objects
    fun equalsUUID(uuida: UUID, uuidb: UUID?): Boolean {
        return uuida.compareTo(uuidb) == 0
    }

    // Gets properties as human readable text
    fun getProperties(context: Context, properties: Int): String {
        var props = StringBuilder()
        val propertyNames = context.resources.getStringArray(R.array.properties_array)
        for (i in 0..7) {
            if (properties shr i and 1 != 0) {
                props.append(propertyNames[i]).append(", ")
            }
        }
        // remove last comma
        if (props.isNotEmpty()) {
            props = StringBuilder(props.substring(0, props.length - 2))
        }
        return props.toString()
    }

    // Checks if given property is set
    fun isSetProperty(property: PropertyType, properties: Int): Boolean {
        return properties shr property.ordinal and 1 != 0
    }

    // Checks if given bit is set
    fun isBitSet(bit: Int, value: Int): Boolean {
        return value shr bit and 0x1 != 0
    }

    // Changes bit to opposite value
    fun toggleBit(bit: Int, value: Int): Int {
        var tmpVal = value
        return 1 shl bit.let { tmpVal = tmpVal xor it; tmpVal }
    }

    // Reads SFLOAT type
    fun readSfloat(value: ByteArray, start: Int, end: Int): Float {
        var mantissa = 0
        mantissa = mantissa shl 8 or getDecimalValue(value[start + end - 1]).toInt()
        mantissa = mantissa shl 8 or (getDecimalValue(value[start + end]).toInt() and 0x0F)
        var exponent: Int = value[start + end].toInt() and 0xF0
        if (exponent >= 0x0008) {
            exponent = -(0x000F + 1 - exponent)
        }
        var output = 0f
        if (mantissa >= FIRST_SFLOAT_RESERVED_VALUE && mantissa <= SFLOAT_NEGATIVE_INFINITY) {
            output = reservedSFloatValues[mantissa - FIRST_SFLOAT_RESERVED_VALUE]
        } else {
            if (mantissa >= 0x0800) {
                mantissa = -(0x0FFF + 1 - mantissa)
            }
            val magnitude = 10.0.pow(exponent.toDouble())
            output = (mantissa * magnitude).toFloat()
        }
        return output
    }

    // Reads FLOAT type
    fun readFloat(value: ByteArray, start: Int, end: Int): Float {
        var mantissa = 0
        mantissa = mantissa shl 8 or getDecimalValue(value[start + end - 1]).toInt()
        mantissa = mantissa shl 8 or getDecimalValue(value[start + end - 2]).toInt()
        mantissa = mantissa shl 8 or getDecimalValue(value[start + end - 3]).toInt()
        var exponent: Int = value[start + end].toInt() and 0xFF
        if (exponent >= 0x00080) {
            exponent = -(0x00FF + 1 - exponent)
        }
        var output = 0f
        if (mantissa in FIRST_FLOAT_RESERVED_VALUE..FLOAT_NEGATIVE_INFINITY) {
            output = reservedFloatValues[mantissa - FIRST_FLOAT_RESERVED_VALUE]
        } else {
            if (mantissa >= 0x7FFFFF) {
                mantissa = -(0xFFFFFF + 1 - mantissa)
            }
            val magnitude = Math.pow(10.0, exponent.toDouble())
            output = (mantissa * magnitude).toFloat()
        }
        return output
    }

    // Reads float32 type
    fun readFloat32(value: ByteArray?, start: Int, end: Int): Float {
        val bytes = Arrays.copyOfRange(value, start, end)
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).float
    }

    // Reads float64 type
    fun readFloat64(value: ByteArray?, start: Int, end: Int): Double {
        val bytes = Arrays.copyOfRange(value, start, end)
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).double
    }

    // Returns UUID text in 16 bits version if it is standard Bluetooth UUID or
    // in 128 bits form if not
    fun getUuidText(uuid: UUID): String {
        val strUuid = uuid.toString().toUpperCase(Locale.getDefault())
        return if (strUuid.startsWith(Consts.BLUETOOTH_BASE_UUID_PREFIX)
                && strUuid.endsWith(Consts.BLUETOOTH_BASE_UUID_POSTFIX)) {
            "0x" + convert128to16UUID(strUuid)
        } else {
            strUuid
        }
    }

    fun getServiceName(uuid: UUID?, context: Context): String {
        val service = instance?.getService(uuid)
        return if (service != null) service.name?.trim { it <= ' ' }!! else context.getString(R.string.unknown_service)
    }

    fun checkOTAService(serviceUuid: String, serviceName: String): String {
        return if (serviceUuid.toLowerCase(Locale.getDefault()) == DeviceServicesActivity.ota_service.toString().toLowerCase(Locale.getDefault())) {
            Constants.OTA_SERVICE
        } else serviceName
    }

    enum class PropertyType(val value: Int) {
        BROADCAST(1),
        READ(2),
        WRITE_NO_RESPONSE(4),
        WRITE(8), NOTIFY(16),
        INDICATE(32),
        SIGNED_WRITE(64),
        EXTENDED_PROPS(128);

    }
}