package com.xbaimiao.logger

import org.slf4j.Logger

interface EasyLoggerAPI {

    fun getLogger(name: String): Logger

}