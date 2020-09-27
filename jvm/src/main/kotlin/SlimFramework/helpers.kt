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