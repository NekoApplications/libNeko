package icu.takeneko.libNeko.nyatwork.packet

import icu.takeneko.libNeko.nyatwork.Pipeline
import icu.takeneko.libNeko.nyatwork.util.FriendlyByteBuf
import io.ktor.utils.io.*

data class PacketHandlingContext(
    val buf: FriendlyByteBuf,
    val sendChannel: ByteWriteChannel,
    val inPipeline: Pipeline<PacketHandlingContext, Unit>,
    val outPipeline: Pipeline<PacketSendingContext, Unit>
) {
}