package com.xbaimiao.logger

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.LogbackServiceProvider
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import com.xbaimiao.easylib.util.plugin
import de.siegmar.logbackgelf.GelfTcpAppender
import org.slf4j.Logger

object LogConfigurator {

    private val provider = LogbackServiceProvider().also { it.initialize() }
    private val loggerMap = HashMap<String, Logger>()

    private val fileAppender by lazy {
        val loggerContext = provider.loggerFactory as LoggerContext

        val fileAppender = RollingFileAppender<ILoggingEvent>()
        fileAppender.name = "FILE"
        fileAppender.context = loggerContext
        fileAppender.file = "${plugin.dataFolder.path}/logs/latest.log"

        val rollingPolicy = TimeBasedRollingPolicy<ILoggingEvent>()
        rollingPolicy.context = loggerContext
        rollingPolicy.setParent(fileAppender)
        rollingPolicy.fileNamePattern = "${plugin.dataFolder.path}/logs/%d{yyyy-MM-dd}.log"
        rollingPolicy.maxHistory = 30
        rollingPolicy.start()

        fileAppender.encoder =
            createEncoder(loggerContext, "[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread/%-5level]: [%logger{36}] %msg%n")
        fileAppender.rollingPolicy = rollingPolicy
        fileAppender.start()
        fileAppender
    }

    private lateinit var asyncGelfAppender: AsyncAppender

    internal fun getLogger(name: String): Logger {
        return loggerMap.computeIfAbsent(name) { configureLogger(name) }
    }

    internal fun configureGelf(host: String, port: Int) {
        val loggerContext = provider.loggerFactory as LoggerContext
        val gelfAppender = GelfTcpAppender()
        gelfAppender.name = "GELF"
        gelfAppender.context = loggerContext
        gelfAppender.graylogHost = host
        gelfAppender.graylogPort = port
        gelfAppender.start()

        asyncGelfAppender = AsyncAppender()
        asyncGelfAppender.name = "ASYNC GELF"
        asyncGelfAppender.context = loggerContext
        asyncGelfAppender.addAppender(gelfAppender)
        asyncGelfAppender.start()
    }

    private fun configureLogger(name: String): Logger {
        val logger = provider.loggerFactory.getLogger(name) as ch.qos.logback.classic.Logger
        logger.addAppender(fileAppender)
        logger.addAppender(asyncGelfAppender)
        logger.level = Level.DEBUG
        return logger
    }

    private fun createEncoder(loggerContext: LoggerContext, pattern: String): LayoutWrappingEncoder<ILoggingEvent> {
        val encoder = PatternLayoutEncoder()
        encoder.pattern = pattern
        encoder.context = loggerContext
        encoder.start()
        return encoder
    }

}