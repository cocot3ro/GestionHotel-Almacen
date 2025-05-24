package com.cocot3ro.gh.almacen.data.database.adapter

import app.cash.sqldelight.ColumnAdapter

object LongArrayColumnAdapter : ColumnAdapter<LongArray, String> {
    override fun decode(databaseValue: String): LongArray {
        if (databaseValue.isEmpty()) {
            return longArrayOf()
        }

        return databaseValue.split(", ").map(String::toLong).toLongArray()
    }

    override fun encode(value: LongArray): String {
        return value.joinToString(", ")
    }

}