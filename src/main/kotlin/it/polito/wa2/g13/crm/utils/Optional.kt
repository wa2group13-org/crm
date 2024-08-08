package it.polito.wa2.g13.crm.utils

import java.util.*

fun <T> Optional<T>.nullable(): T? = orElse(null)
