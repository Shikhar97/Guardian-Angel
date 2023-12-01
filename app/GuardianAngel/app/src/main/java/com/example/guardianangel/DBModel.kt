package com.example.guardianangel

import android.os.Parcel
import android.os.Parcelable

data class DBModel(
    var SLEEP_WELLNESS: Boolean = false, // Changed type to Boolean
    var WAKEUP_PREFERENCE: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(), // Read a byte as Boolean
        parcel.readValue(String::class.java.classLoader) as? String
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (SLEEP_WELLNESS) 1 else 0) // Write Boolean as a byte
        parcel.writeValue(WAKEUP_PREFERENCE)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DBModel> {
        override fun createFromParcel(parcel: Parcel): DBModel {
            return DBModel(parcel)
        }

        override fun newArray(size: Int): Array<DBModel?> {
            return arrayOfNulls(size)
        }
    }
}
