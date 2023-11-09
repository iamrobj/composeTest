package com.robj.deblocktest

fun Double.formatToDecimalPlaces(decimalPlaces: Int = 2): String {
    val num = this.toString().toDouble()
    return if (this % 1 == 0.0) {
        String.format("%.0f", this)
    } else {
        String.format("%.${decimalPlaces}f", this)
    }
}