package SlimFramework

import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread

class FrwBackend(private val port: Int, private val jsonqueue: MutableList<IncomingRequest>, name: String, level: LoggingLevel) {

    private val classname = "($name)FrwBackend"
    private val server = ServerSocket(port)

    init {
        //server.soTimeout = 5000
    }

    fun start() {
        thread(isDaemon = true, block = { serve() })
    }

    //  continuously serve requests
    private fun serve() {
        val routine = "serve"
        while (!server.isClosed) {
            val client = server.accept()
            debug(classname, routine, "Request accepted from ${client.remoteSocketAddress}")
            //  create a thread to handle this request
            thread(isDaemon = true, block = { receiveRequest(client) })
        }
    }

    //  listen for the json
    private fun listenForJSON(client: Socket): String {
        val routine = "listenForJSON"
        var json = StringBuilder()
        try {
            debug(classname, routine, "Parsing data from ${client.remoteSocketAddress}")
            val scanner = Scanner(client.getInputStream())
            while (scanner.hasNextLine()) {
                json.append(scanner.nextLine())
            }
            client.shutdownInput()
            if (json.isNotEmpty()) {
                json = json.delete(0, 2)
            }
            debug(classname, routine, "Data received: ${json}")
        } catch (e: Exception) {
            error(classname, routine, "${e.message}")
            client.close()
            throw e
        }
        return json.toString()
    }

    private fun receiveRequest(client: Socket) {
        val routine = "receiveRequest"
        try {
            //  listen for the json
            val json = listenForJSON(client)
            //  check if json is null or blank
            if (!json.isNullOrBlank()) {
                //  json is not null or blank
                //  add this request and the socket to the queue
                debug(classname, routine, "Adding json to queue..")
                jsonqueue.add(IncomingRequest(json, client))
            } else {
                //  json was null or blank
                debug(classname, routine, "Blank request received")
            }
        } catch (e: Exception) {
            error(classname, routine, "${e.message}")
            throw e
        }
    }

    fun goodbye() {
        server.close()
    }
}

class IncomingRequest(val json: String, val client: Socket)