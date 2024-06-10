package icu.takeneko.libNeko.nyatwork

import io.ktor.utils.io.*

data class PacketHandlingContext(
    val buf: FriendlyByteBuf,
    val sendChannel: ByteWriteChannel,
    val inPipeline: Pipeline<PacketHandlingContext, Unit>,
    val outPipeline: Pipeline<PacketSendingContext, Unit>
) {
}