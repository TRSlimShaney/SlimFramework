package SlimFramework

//  status codes
//  success codes
const val STA_NORMAL = 0

//  failure codes
const val STA_FAIL = -1
const val STA_NOKEY = -2
const val STA_KEYINUSE = -3
const val STA_NOTABLE = -4
const val STA_NORECORD = -5
const val STA_DBFAIL = -6
const val STA_BADREQ = -7




//  db service codes
const val DB_SWAPRECORD = 0
const val DB_PUTIFABSENT = 1
const val DB_REMOVERECORD = 2
const val DB_GETTABLE = 3
const val DB_GETRECORD = 4


//  map the status codes to their names
private val statusmap = mutableMapOf<Int, String>(
        STA_NORMAL to "STA_NORMAL",
        STA_FAIL to "STA_FAIL",
        STA_NOKEY to "STA_NO_KEY",
        STA_NOTABLE to "STA_NO_TABLE",
        STA_NORECORD to "STA_NO_RECORD",
        STA_DBFAIL to "STA_DBFAIL",
        STA_BADREQ to "STA_BADREQ"
)

//  useful when logging so status is printed with name rather than value
fun frwGetStatusName(status: Int): String {
    return statusmap.getOrDefault(status, "STATUS_NOT_FOUND:${status}")
}
