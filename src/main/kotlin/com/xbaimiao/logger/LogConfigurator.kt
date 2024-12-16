package com.xbaimiao.logger

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import de.siegmar.logbackgelf.GelfTcpAppender
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LogConfigurator {

    @JvmStatic
    fun main(args: Array<String>) {
        val a = configureLogger(this::class.java.name)
        a.info("测试LOG信息")

        println(1)
        info("test")
        Thread.currentThread().join()
    }

    fun configureLogger(name: String): Logger {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

        val consoleAppender = ConsoleAppender<ILoggingEvent>()
        consoleAppender.name = "CONSOLE"
        consoleAppender.context = loggerContext
        consoleAppender.encoder = createEncoder("[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread/%-5level]: [%logger{36}] %msg%n")
        consoleAppender.start()

        val fileAppender = RollingFileAppender<ILoggingEvent>()
        fileAppender.name = "FILE"
        fileAppender.context = loggerContext
        fileAppender.file = "logs/app.log"

        val rollingPolicy = TimeBasedRollingPolicy<ILoggingEvent>()
        rollingPolicy.context = loggerContext
        rollingPolicy.setParent(fileAppender)
        rollingPolicy.fileNamePattern = "logs/app-%d{yyyy-MM-dd}.log"
        rollingPolicy.maxHistory = 30
        rollingPolicy.start()

        fileAppender.encoder =
            createEncoder("[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread/%-5level]: [%logger{36}] %msg%n")
        fileAppender.rollingPolicy = rollingPolicy
        fileAppender.start()

        val gelfAppender = GelfTcpAppender()
        gelfAppender.name = "GELF"
        gelfAppender.context = loggerContext
        gelfAppender.graylogHost = "localhost"
        gelfAppender.graylogPort = 12201
        gelfAppender.start()

        val asyncGelfAppender = AsyncAppender()
        asyncGelfAppender.name = "ASYNC GELF"
        asyncGelfAppender.context = loggerContext
        asyncGelfAppender.addAppender(gelfAppender)
        asyncGelfAppender.start()

        val logger = LoggerFactory.getLogger(name) as ch.qos.logback.classic.Logger
        logger.addAppender(consoleAppender)
        logger.addAppender(fileAppender)
        logger.addAppender(asyncGelfAppender)
        logger.level = Level.DEBUG
        return logger
    }

    private fun createEncoder(pattern: String): LayoutWrappingEncoder<ILoggingEvent> {
        val encoder = PatternLayoutEncoder()
        encoder.pattern = pattern
        encoder.context = LoggerFactory.getILoggerFactory() as LoggerContext
        encoder.start()
        return encoder
    }

}