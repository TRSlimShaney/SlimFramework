package SlimFramework

import java.io.DataOutputStream
import java.net.Socket
import java.util.*

private val classname = "FrwIPC"

private val socketmap = mutableMapOf<String, Pair<String, Int>>()

fun frwAddSocketMapping(name: String, address: String, port: Int): Int {
    if (!socketmap.containsKey(name)) {
        socketmap[name] = Pair(address, port)
        return STA_NORMAL
    }
    return STA_KEYINUSE;
}

fun frwSendResponse(obj: Any, client: Socket): Int {
    val routine = "FrwSendResponse"
    debug(classname, routine,"Entering function")
    val json = frwJSONStringify(obj)
    debug(classname, routine,"Sending response: $json")
    val outstream = DataOutputStream(client.getOutputStream())
    try {
        outstream.writeUTF(json)
        outstream.flush()
        client.close()
    }
    catch(e: Exception) {
        error(classname, routine,"Sending response failed: ${e.message}")
        return STA_FAIL
    }
    return STA_NORMAL
}

fun frwSendWithNoResponse(obj: Any, address: String, port: Int): Int {
    val routine = "FrwSendRequest"
    debug(classname, routine,"Entering function")
    val rsp = SendMessage(frwJSONStringify(obj), address, port, 5000)
    if (rsp.isBlank()) {
        return STA_FAIL
    }
    return STA_NORMAL
}

fun <T> frwSendWithResponse(obj: Any, address: String, port: Int, type: Class<T>): T {
    val routine = "FrwSendWithResponse"
    debug(classname, routine,"Entering function")
    val rspjson = SendMessage(frwJSONStringify(obj), address, port, 0)
    return frwJSONParse(rspjson, type)
}

fun frwSendWithNoResponse(name: String, obj: Any): Int {
    val routine = "FrwSendWithNoResponse"
    debug(classname, routine, "Entering function")
    if (socketmap.containsKey(name)) {
        val pair = socketmap[name]
        val rsp = SendMessage(frwJSONStringify(obj), pair!!.first, pair.second, 5000)
        if (rsp.isBlank()) {
            return STA_FAIL
        }
        return STA_NORMAL
    }
    else {
        error(classname, routine, "Name \"$name\" not found in socket map")
    }
    return STA_NOKEY
}

fun <T> frwSendWithResponse(name: String, obj: Any, type: Class<T>): T {
    val routine = "FrwSendWithResponse"
    debug(classname, routine, "Entering function")
    var rsp = ""
    if (socketmap.containsKey(name)) {
        val pair = socketmap[name]
        rsp = SendMessage(frwJSONStringify(obj), pair!!.first, pair.second, 0)
    }
    else {
        error(classname, routine, "Name \"$name\" not found in socket map")
    }
    return frwJSONParse(rsp, type)
}

fun frwSendWithResponse(name: String, json: String): String {
    val routine = "FrwSendWithResponse"
    debug(classname, routine, "Entering function")
    var rsp = ""
    if (socketmap.containsKey(name)) {
        val pair = socketmap[name]
        rsp = SendMessage(json, pair!!.first, pair.second, 0)
    }
    else {
        error(classname, routine, "Name \"$name\" not found in socket map")
    }
    return rsp
}

private fun SendMessage(sendjson: String, address: String, port: Int, timeout: Int): String {
    val routine = "FrwSendMessage"
    debug(classname, routine,"Entering function")
    debug(classname, routine,"Sending message: $sendjson")
    var rspjson = StringBuilder()
    try {
        val client = Socket(address, port)
        client.soTimeout = timeout
        val outstream = DataOutputStream(client.getOutputStream())
        outstream.writeUTF(sendjson)
        outstream.flush()
        client.shutdownOutput()

        Thread.sleep(25)

        val scanner = Scanner(client.getInputStream())
        while (scanner.hasNextLine()) {
            rspjson.append(scanner.nextLine())
        }
        rspjson = rspjson.delete(0, 2)
        client.close()
    }
    catch (e: Exception) {
        error(classname, routine,"Sending request failed: ${e.message}")
    }
    return rspjson.toString()
}

class FrwIPC(name: String, level: LoggingLevel) {

}

class DBRequest {
    var DB_CODE: Int
    var table: String
    var key: String
    var record: String

    constructor(DB_CODE: Int, table: String, key: String, record: String) {
        this.DB_CODE = DB_CODE
        this.table = table
        this.key = key
        this.record = record
    }

    constructor(DB_CODE: Int, table: String, key: String) {
        this.DB_CODE = DB_CODE
        this.table = table
        this.key = key
        this.record = ""
    }

    constructor(DB_CODE: Int, table: String) {
        this.DB_CODE = DB_CODE
        this.table = table
        this.key = ""
        this.record = ""
    }
}
class DBResponse(val status: Int, val record: String)
class DBTableResponse(val status: Int, val table: MutableMap<String, String>)

