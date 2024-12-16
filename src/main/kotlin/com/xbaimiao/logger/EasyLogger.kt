package com.xbaimiao.logger

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.util.plugin
import org.slf4j.Logger

@Suppress("unused")
class EasyLogger : EasyPlugin() {

    lateinit var rootLogger: Logger

    override fun enable() {
        saveDefaultConfig()
        val host = config.getString("gelf-host") ?: "localhost"
        val port = config.getInt("gelf-port", 12201)
        val loggerName = config.getString("logger-name") ?: plugin.name

        LogConfigurator.configureGelf(host, port)
        rootLogger = LogConfigurator.getLogger(loggerName)
    }

}
