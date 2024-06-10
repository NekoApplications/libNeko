package icu.takeneko.libNeko.nyatwork

import io.ktor.utils.io.*

data class PacketSendingContext(
    val packetContext: Packet,
    val sendChannel: ByteWriteChannel,
    val inPipeline: Pipeline<PacketHandlingContext, Unit>,
    val outPipeline: Pipeline<PacketSendingContext, Unit>
) {
}