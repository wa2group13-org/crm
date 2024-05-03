package it.polito.wa2.g13.crm.utils

import jakarta.servlet.http.HttpServletRequest

fun HttpServletRequest.requestURIDropLast(n: Int): String =
    this.requestURI.split("/").dropLast(n).joinToString("/")