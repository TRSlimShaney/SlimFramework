package SlimFramework

import java.io.File
import java.time.LocalDateTime


class FrwLogger(var level: LoggingLevel, private val filename: String) {

    //  use semaphore so threads don't fight over logging
    private val s = FrwSemaphore(1, "Logger")
    private val directory = "logs"

    init {
        val folder = File(directory)
        if (!folder.exists()) {
            folder.mkdir()
        }
    }

    //  for logging errors
    fun error(classname: String, routine: String, msg: String) {
        if (level >= LoggingLevel.error) {
            writeLine("ERR", classname, routine, msg)
        }
    }

    //  for logging debug
    fun debug(classname: String, routine: String, msg: String) {
        if (level >= LoggingLevel.debug) {
            writeLine("DBG", classname, routine, msg)
        }
    }

    //  for logging information
    fun info(classname: String, routine: String, msg: String) {
        if (level >= LoggingLevel.info) {
            writeLine("INF", classname, routine, msg)
        }
    }

    //  write the logging information to the console and/or logfile
    private fun writeLine(logtype: String, classname: String, routine: String, msg: String) {
        s.lock()
        val line = "TIME:${LocalDateTime.now()}::${logtype}::CLASS:${classname}::FUN:${routine}: ${msg}.\n"
        File(directory, filename).appendText(line, Charsets.UTF_8)
        print(line)
        s.unlock()
    }
}

//  enum for logging levels
enum class LoggingLevel {
    none,
    error,
    info,
    debug
}