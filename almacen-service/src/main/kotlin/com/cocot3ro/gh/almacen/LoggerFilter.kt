package com.cocot3ro.gh.almacen

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

class LoggerFilter : Filter<ILoggingEvent>() {

    override fun decide(event: ILoggingEvent?): FilterReply {
        if (event?.message?.contains("argon2") == true) {
            return FilterReply.DENY
        }

        return FilterReply.NEUTRAL
    }

}