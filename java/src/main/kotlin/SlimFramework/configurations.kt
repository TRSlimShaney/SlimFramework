package SlimFramework

import java.io.File

class FrwConfigurations(private val file: String, level: LoggingLevel) {

    private val logger = FrwLogger(level,"FrwConfigurations.LOG")
    private val configs = mutableMapOf<String, String>()
    private val classname = "FrwConfigurations"
    private val directory = "configs"

    init {
        loadConfig()
    }

    private fun loadConfig() {
        val routine = "loadConfig"
        val folder = File(directory)

        if (!folder.exists()) {
            folder.mkdir()
        }

        if (!File(directory, file).exists()) {
            logger.debug(classname, routine, "config created")
            File(directory, file).createNewFile()
        }

        if (File(directory, file).exists()) {

            val lines = File(directory, file).readLines()

            if (lines.isEmpty()) {
                logger.error(classname, routine, "config is empty")
            }
            else {
                lines.forEach {
                    val split = it.split("=")
                    if (split.size == 2) {
                        configs[split[0]] = split[1]
                    }
                    else {
                        logger.error(classname, routine, "config line not formatted as expected: $it")
                    }
                }
            }
        }
    }

    fun putConfig(key: String, value: String): Int {
        val routine = "putConfig"

        if (!configs.containsKey(key)) {
            configs[key] = value
            File(directory, file).appendText("${key}=${value}\n")
        }
        else {
            logger.error(classname, routine, "config already contains key $key")
            return STA_KEYINUSE
        }
        return STA_NORMAL
    }

    fun getConfig(key: String): String {
        return configs.getOrDefault(key, "")
    }
}