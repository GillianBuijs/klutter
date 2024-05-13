package dev.buijs.klutter.kore.project

import dev.buijs.klutter.kore.KlutterException
import dev.buijs.klutter.kore.tasks.execute
import java.io.File
import java.nio.file.Path

enum class OperatingSystem(val value: String) {
    WINDOWS("windows"),
    MACOS("macos"),
    LINUX("linux")
}

enum class Architecture {
    X64,
    ARM64,
}

data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
)

val Version.prettyPrint: String
    get() = "${major}.${minor}.${patch}"

val isWindows: Boolean
    get() = currentOperatingSystem == OperatingSystem.WINDOWS

val isMacos: Boolean
    get() = currentOperatingSystem == OperatingSystem.MACOS

/**
 * Get current [OperatingSystem] from System Properties.
 *
 * @returns [OperatingSystem].
 * @throws [KlutterException] Unsupported OperatingSystem if not one of [OperatingSystem].
 */
val currentOperatingSystem: OperatingSystem
    get() = System.getProperty("os.name").uppercase().let { screaming ->
        when {
            screaming.contains("WIN") -> OperatingSystem.WINDOWS
            screaming.contains("MAC") -> OperatingSystem.MACOS
            screaming.contains("LINUX") -> OperatingSystem.LINUX
            else -> throw KlutterException("Unsupported OperatingSystem: $screaming")
        }
    }

val currentArchitecture: Architecture
    get() {
        val sysctl = "sysctl -n sysctl.proc_translated" execute currentWorkingDirectory
        val usesRosetta = sysctl == "1"
        val architecture = """uname -m""" execute currentWorkingDirectory
        val archContainsARM = architecture.uppercase().contains("ARM")
        return when {
            usesRosetta -> Architecture.ARM64
            archContainsARM -> Architecture.ARM64
            else -> Architecture.X64
        }
    }

val currentWorkingDirectory: File
    get() = Path.of("").toFile().absoluteFile