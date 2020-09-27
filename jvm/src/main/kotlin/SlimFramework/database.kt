package SlimFramework

import java.io.File
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread



class FrwDatabase(port: Int, name: String, saveToFile: Boolean, level: LoggingLevel) {

    private val queue = mutableListOf<IncomingRequest>()
    private val backend = FrwBackend(port, queue, name, level)
    private val dbi = DatabaseInterface(name, saveToFile, level)
    private val switch = DBSwitch(queue, dbi)

    fun start() {
        thread(isDaemon = true, block = { run() })
    }

    private fun run() {
        backend.start()
        switch.processMessages()
    }
}


private class DatabaseInterface(name: String, saveToFile: Boolean, level: LoggingLevel) {

    private val classname = "DatabaseInterface"
    private val heap = Heap(name, saveToFile, level)
    private val s = FrwSemaphore(1, classname)

    fun swapRecord(table: String, key: String, newrecord: String): Int {
        s.lock()
        if (heap.hasRecord(table, key)) {
            heap.swapRecord(table, key, newrecord)
        }
        else {
            heap.putRecord(table, key, newrecord)
        }
        s.unlock()
        return STA_NORMAL
    }

    fun putRecordIfAbsent(table: String, key: String, record: String): Int {
        s.lock()
        if (heap.hasRecord(table, key)) {
            s.unlock()
            return STA_KEYINUSE
        }
        heap.putRecord(table, key, record)
        s.unlock()
        return STA_NORMAL
    }

    fun removeRecord(table: String, key: String): Int {
        s.lock()
        if (heap.hasRecord(table, key)) {
            heap.removeRecord(table, key)
            s.unlock()
            return STA_NORMAL
        }
        s.unlock()
        return STA_NORECORD
    }

    fun getRecordsOfTable(table: String): MutableMap<String, String> {
        s.lock()
        val table = heap.getRecordsOfTable(table)
        s.unlock()
        return table
    }

    fun getRecord(table: String, key: String): String {
        s.lock()
        val record = heap.getRecord(table, key)
        s.unlock()
        return record
    }
}

private class DBSwitch(private val queue: MutableList<IncomingRequest>, private val db: DatabaseInterface) {

    fun processMessages() {
        while (true) {
            if (queue.size > 0) {
                val req = queue.first()
                thread(isDaemon = true, block = { serviceSwitch(req.client, req.json) })
                queue.removeAt(0)
            }
            else {
                Thread.sleep(10)
            }
        }
    }

    private fun serviceSwitch(client: Socket, json: String) {
        val req = frwFromJSON(json, DBRequest::class.java)
        var status = STA_NORMAL
        var record = ""

        if (validateRequest(req)) {
            when (req.DB_CODE) {
                DB_SWAPRECORD -> {
                    status = db.swapRecord(req.table, req.key, req.record)
                }
                DB_PUTIFABSENT -> {
                    status = db.putRecordIfAbsent(req.table, req.key, req.record)
                }
                DB_REMOVERECORD -> {
                    status = db.removeRecord(req.table, req.key)
                }
                DB_GETTABLE -> {
                    val table = db.getRecordsOfTable(req.table)
                    val rsp = DBTableResponse(status, table)
                    frwSendResponse(rsp, client)
                    return
                }
                DB_GETRECORD -> {
                    record = db.getRecord(req.table, req.key)
                    if (record.isBlank()) {
                        status = STA_NORECORD
                    }
                }
                else -> {
                    status = STA_DBFAIL
                }
            }
        }
        else {
            status = STA_BADREQ
        }
        val rsp = DBResponse(status, record)
        frwSendResponse(rsp, client)
    }

    private fun validateRequest(req: DBRequest): Boolean {
        if (req.table.isBlank()) {
            return false
        }
        when (req.DB_CODE) {
            DB_SWAPRECORD, DB_PUTIFABSENT, DB_REMOVERECORD -> {
                if (req.key.isBlank() || req.record.isBlank()) {
                    return false
                }
            }
            DB_GETRECORD -> {
                if (req.key.isBlank()) {
                    return false
                }
            }
        }
        req.table = req.table.toLowerCase()
        req.key = req.key.toLowerCase()
        return true
    }
}

private class Heap(private val name: String, private val saveToFile: Boolean, level: LoggingLevel) {

    private val classname = "($name)FrwDatabase"
    private val db = mutableMapOf<String, MutableMap<String, String>>()
    private val directory = "dbfiles"
    private val delimiter = "[][][]"

    init {
        if (saveToFile) {
            loadDb()
        }
    }

    private fun loadDb() {
        val routine = "loadDb"
        val folder = File(directory)
        if (!folder.exists()) {
            folder.mkdir()
        }
        val file = File(directory, "${name}.db")
        if (file.exists()) {
            val lines = file.readLines()
            lines.forEach {
                val split = it.split(delimiter)
                if (split.size == 3) {
                    putRecord(split[0], split[1], split[2])
                }
                else {
                    error(classname, routine, "dbfile line not formatted as expected: $it")
                }
            }
        }
    }

    private fun writeFile(table: String, key: String, record: String) {
        val file = File(directory, "${name}.db")
        if (file.exists()) {
            val lines = file.appendText("${table}${delimiter}${key}${delimiter}${record}\n")
        }
    }

    private fun removeFile(table: String, key: String) {
        val file = File(directory, "${name}.db")
        if (file.exists()) {
            val lines = file.readLines()
            val newlines = mutableListOf<String>()
            lines.forEach {
                val split = it.split(delimiter)
                if (split.size == 3 && !(split[0] == table && split[1] == key)) {
                    newlines.add("${split[0]}${delimiter}${split[1]}${delimiter}${split[2]}\n")
                }
            }
            val file2 = File(directory, "${UUID.randomUUID()}")
            newlines.forEach {
                file2.appendText(it)
            }
            file.delete()
            file2.renameTo(file)
        }
    }

    fun putRecord(table: String, key: String, record: String): Int {
        val routine = "storeRecord"
        if (!db.containsKey(table)) {
            db[table] = mutableMapOf<String, String>()
            db[table]!![key] = record
        }
        else {
            if (db[table]!!.containsKey(key)) {
                error(classname, routine, "Table $table with key $key Already has record")
                return STA_KEYINUSE
            }
            db[table]!![key] = record
            if (saveToFile) {
                writeFile(table, key, record)
            }
        }
        return STA_NORMAL
    }

    fun getRecord(table: String, key: String): String {
        val routine = "getRecord"
        if (containsRecord(table, key)) {
            val result = db[table]!![key]!!
            return result
        }
        val result = ""
        error(classname, routine, "Record in Table $table with key $key not found")
        return result
    }

    fun hasRecord(table: String, key: String): Boolean {
        val result = containsRecord(table, key)
        return result
    }

    private fun containsRecord(table: String, key: String): Boolean {
        if (!db.containsKey(table)) {
            return false
        }
        if (!db[table]!!.containsKey(key)) {
            return false
        }
        return true
    }

    fun swapRecord(table: String, key: String, newrecord: String): Int {
        val routine = "swapRecord"
        if (containsRecord(table, key)) {
            db[table]!![key] = newrecord
            if (saveToFile) {
                removeFile(table, key)
                writeFile(table, key, newrecord)
            }
            return STA_NORMAL
        }
        error(classname, routine, "No record found to swap")
        return STA_NORECORD
    }

    fun removeRecord(table: String, key: String): Int {
        val routine = "removeRecord"
        if (containsRecord(table, key)) {
            db[table]!!.remove(key)
            if (saveToFile) {
                removeRecord(table, key)
            }
            return STA_NORMAL
        }
        error(classname, routine, "No record found to remove")
        return STA_NORECORD
    }

    fun getRecordsOfTable(table: String): MutableMap<String, String> {
        if (db.containsKey(table)) {
            return db[table]!!
        }
        return mutableMapOf<String, String>()
    }
}

fun <T> frwParseDBRecord(rsp: DBResponse, type: Class<T>): T {
    return frwFromJSON(rsp.record, type)
}

fun <T> frwParseDBTable(rsp: DBTableResponse, type: Class<T>): MutableMap<String, T> {
    val result = mutableMapOf<String, T>()
    rsp.table.forEach {
        result[it.key] = frwFromJSON(it.value, type)
    }
    return result
}