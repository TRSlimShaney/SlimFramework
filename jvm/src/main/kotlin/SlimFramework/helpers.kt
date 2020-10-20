package SlimFramework



//  based on int return, did the function succeed?
fun frwSUCCESS(status: Int): Boolean {
    return status >= 0
}

//  based on int return, did the function fail?
fun frwFAILURE(status: Int): Boolean {
    return !frwSUCCESS(status)
}

//  streamlines logging errors if functions fail
fun frwFCALL(routine: String, classname: String, status: Int): Boolean {
    if (frwSUCCESS(status)) {
        debug(classname, routine, "FCALL succeeded with status ${frwGetStatusName(status)}")
        return true
    }
    else {
        error(classname, routine, "FCALL FAILED with status ${frwGetStatusName(status)}")
    }
    return false
}

fun frwIsBlankString(empty: String): Boolean {
    return empty.isNullOrBlank()
}

fun frwIsNotBlankString(empty: String): Boolean {
    return !frwIsBlankString(empty)
}

fun frwToBytes(bytes: String): ByteArray {
    return bytes.toByteArray()
}

fun frwExceptionToString(e: Exception): String {
    return "Exception: ${e.message}"
}

fun frwGreaterThan(a: Int, b: Int): Boolean {
    return a > b
}

fun frwLessThan(a: Int, b: Int): Boolean {
    return a < b
}

fun <T>frwAreEqual(a: T, b: T): Boolean {
    return a == b
}

fun <T>frwNotEqual(a: T, b: T): Boolean {
    return !frwAreEqual(a, b)
}

fun <T>frwToSet(list: MutableList<T>): MutableSet<T> {
    val set = mutableSetOf<T>()
    for (item in list) {
        set.add(item)
    }
    return set
}

fun frwComesBefore(a: Char, b: Char): Boolean {
    return a < b
}

fun frwComesAfter(a: Char, b: Char): Boolean {
    return a > b
}

fun frwGreaterThan(a: String, b: String): Boolean {
    val result = a.compareTo(b)
    return frwGreaterThan(result, 0)
}

fun frwLessThan(a: String, b: String): Boolean {
    val result = a.compareTo(b)
    return frwLessThan(result, 0);
}