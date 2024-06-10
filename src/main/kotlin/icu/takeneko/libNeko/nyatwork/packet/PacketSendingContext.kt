package icu.takeneko.libNeko.nyatwork.packet

import icu.takeneko.libNeko.nyatwork.Pipeline
import io.ktor.utils.io.*

data class PacketSendingContext(
    val packetContext: Packet,
    val sendChannel: ByteWriteChannel,
    val inPipeline: Pipeline<PacketHandlingContext, Unit>,
    val outPipeline: Pipeline<PacketSendingContext, Unit>
) {
}