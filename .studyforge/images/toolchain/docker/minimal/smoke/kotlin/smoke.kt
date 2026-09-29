import kotlin.system.exitProcess

fun main() {
    if (1 + 1 != 2) {
        System.err.println("smoke: arithmetic disagrees")
        exitProcess(1)
    }
    println("smoke: kotlin ok")
}
