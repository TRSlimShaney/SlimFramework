package SlimFramework

import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class CoverPage(val PrintCoverPage: Boolean, val Text: String)
const val A4: String = "a4"
const val Letter: String = "letter"
const val legal: String = "legal"

const val LandscapeCC = 4
const val LandscapeC = 5
const val ReversePortrait = 6

const val Draft = 3
const val Normal = 4
const val Best = 5

const val OneSided = "one-sided"
const val TwoSidedLongEdge = "two-sided-long-edge"
const val TwoSidedShortEdge = "two-sided-short-edge"

class FrwCUPSPrintJob {
    private val PrinterName: String
    private val ServerName: String
    private val PrintJob: ByteArray
    private val Filename: String
    private val FileOrName: Boolean

    var Stapled: Boolean = false
    var NumberOfCopies: Int = 1
    var ForceEncryptedDelivery: Boolean = false
    var Username: String = ""
    var JobName: String = ""
    var CoverPage: CoverPage = CoverPage(false, "")
    var PageSize: String = Letter
    var NumberOfPagesPerPage: Int = 1
    var Orientation: Int = 0
    var PrintQuality: Int = 4
    var Sidedness: String = TwoSidedLongEdge

    constructor(PrinterName: String, ServerName: String, PrintJob: StringBuilder) {
        this.PrinterName = PrinterName
        this.ServerName = ServerName
        this.PrintJob = PrintJob.toString().toByteArray()
        this.Filename = ""
        this.FileOrName = true
    }

    constructor(PrinterName: String, ServerName: String, PrintJob: ByteArray) {
        this.PrinterName = PrinterName
        this.ServerName = ServerName
        this.PrintJob = PrintJob
        this.Filename = ""
        this.FileOrName = true
    }

    constructor(PrinterName: String, ServerName: String, Filename: String) {
        this.PrinterName = PrinterName
        this.ServerName = ServerName
        this.PrintJob = byteArrayOf()
        this.Filename = Filename
        this.FileOrName = false
    }

    fun printFile() {
        if (!FileOrName) {
            StartProcess(Filename)
        }
    }

    fun printBytes() {
        if (FileOrName) {
            var name = "${UUID.randomUUID()}.bin"
            var path = Paths.get(".\\${name}")

            while (Files.exists(path)) {
                name = "${UUID.randomUUID()}.bin"
                path = Paths.get(".\\${name}")
            }

            Files.write(path, PrintJob)

            StartProcess(name);

            Files.delete(path);
        }
    }

    private fun StartProcess(filename: String) {
        val options = GetOptions()
        val copies = GetCopies()
        Runtime.getRuntime().exec("lp ${options}${copies}${filename}")
    }

    private fun GetOptions(): String {
        val options = StringBuilder("-o ")
        var anyoptions = false

        if (Stapled) {
            options.append("staple=1plu ")
        }

        if (CoverPage.PrintCoverPage) {
            options.append("job-sheets=${CoverPage.Text} ")
            anyoptions = true
        }

        if (PageSize != Letter) {
            options.append("media=$PageSize ")
            anyoptions = true
        }

        if (NumberOfPagesPerPage > 1) {
            options.append("number-up=${NumberOfPagesPerPage} ")
            anyoptions = true
        }

        if (Orientation != 0) {
            options.append("orientation-requested=${Orientation} ")
            anyoptions = true
        }

        if (PrintQuality != 4) {
            options.append("print-quality=${PrintQuality} ")
            anyoptions = true
        }

        if (Sidedness != TwoSidedLongEdge) {
            options.append("sides=${Sidedness} ")
            anyoptions = true
        }

        if (anyoptions) {
            return options.toString()
        }
        return ""
    }

    private fun GetCopies(): String {
        val copies = StringBuilder("-n ${NumberOfCopies} ")

        if (NumberOfCopies > 1) {
            return copies.toString()
        }
        return ""
    }
}