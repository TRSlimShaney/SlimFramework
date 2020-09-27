package SlimFramework



//  based on int return, did the function succeed?
fun frwSUCCESS(status: Int): Boolean {
    if (status >= 0) {
        return true
    }
    return false
}

//  based on int return, did the function fail?
fun frwFAILURE(status: Int): Boolean {
    if (frwSUCCESS(status)) {
        return false
    }
    return true
}

//  streamlines logging errors if functions fail
fun frwFCALL(routine: String, classname: String, logger: FrwLogger, status: Int): Boolean {
    if (frwSUCCESS(status)) {
        logger.debug(classname, routine, "FCALL succeeded with status ${frwGetStatusName(status)}")
        return true
    }
    else {
        logger.error(classname, routine, "FCALL FAILED with status ${frwGetStatusName(status)}")
    }
    return false
}