package SlimFramework

import java.io.File

private val configs = mutableMapOf<String, String>()
private val classname = "FrwConfigurations"
private val directory = "configs"
var file = "config.cfg"

fun frwLoadConfig() {
    val routine = "loadConfig"
    val folder = File(directory)

    if (!folder.exists()) {
        folder.mkdir()
    }

    if (!File(directory, file).exists()) {
        debug(classname, routine, "config created")
        File(directory, file).createNewFile()
    }

    if (File(directory, file).exists()) {

        val lines = File(directory, file).readLines()

        if (lines.isEmpty()) {
            error(classname, routine, "config is empty")
        }
        else {
            lines.forEach {
                val split = it.split("=")
                if (split.size == 2) {
                    configs[split[0]] = split[1]
                }
                else {
                    error(classname, routine, "config line not formatted as expected: $it")
                }
            }
        }
    }
}

fun frwPutConfig(key: String, value: String): Int {
    val routine = "putConfig"

    if (!configs.containsKey(key)) {
        configs[key] = value
        File(directory, file).appendText("${key}=${value}\n")
    }
    else {
        error(classname, routine, "config already contains key $key")
        return STA_KEYINUSE
    }
    return STA_NORMAL
}

fun frwGetConfig(key: String): String {
    return configs.getOrDefault(key, "")
}