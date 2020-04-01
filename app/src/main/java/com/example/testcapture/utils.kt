@file:JvmName("Utils")
package com.example.testcapture

fun getString(str: String): String {
    return str.replace("\r\n", "\\n")
}