package SlimFramework

import com.google.gson.Gson
import java.util.concurrent.Semaphore

private const val classname = "FrwUtilities"

//  java semaphore wrapped with more convenient logic
class FrwSemaphore(val capacity: Int, val name: String) {
    //  create the java semaphore
    private val s = Semaphore(capacity)
    private val classname = "FrwSemaphore"

    //  acquire a lock on the semaphore
    fun lock() {
        val routine = "lock"
        debug(classname, routine, "Locking $name semaphore")
        s.acquireUninterruptibly()
    }

    //  release the lock on the semaphore
    fun unlock() {
        val routine = "unlock"
        debug(classname, routine, "Unlocking $name semaphore")
        s.release()
    }
}

fun frwJSONStringify(obj: Any): String {
    val gson = Gson()
    return gson.toJson(obj)
}

fun <T> frwJSONParse(json: String, type: Class<T>): T {
    val gson = Gson()
    return gson.fromJson(json, type);
}