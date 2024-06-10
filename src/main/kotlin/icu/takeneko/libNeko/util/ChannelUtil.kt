package icu.takeneko.libNeko.util

import io.ktor.utils.io.*

suspend fun ByteReadChannel.readByteArrayLine(
): ByteArray {
    var blockCount = 8
    var output = ByteArray(blockCount * 1024)
    var ptr = 0
    while (!isClosedForRead) {
        val byte = this.readByte()
        if (byte.toInt().toChar() == '\n') {
            break
        } else {
            if ((ptr + 1) > (output.size - 1)) {
                blockCount++
                val old = output
                output = ByteArray(blockCount * 1024)
                System.arraycopy(old, 0, output, 0, old.size)
            }
            output[ptr++] = byte
        }
    }
    val result = ByteArray(ptr)
    System.arraycopy(output, 0, result, 0, ptr)
    return result
}