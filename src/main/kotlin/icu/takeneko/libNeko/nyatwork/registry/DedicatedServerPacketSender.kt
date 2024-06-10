package icu.takeneko.libNeko.nyatwork.registry

import icu.takeneko.libNeko.nyatwork.PipelineModule
import icu.takeneko.libNeko.nyatwork.packet.PacketSendingContext
import icu.takeneko.libNeko.nyatwork.util.FriendlyByteBuf
import kotlinx.coroutines.runBlocking

internal object DedicatedServerPacketSender : PipelineModule<Pair<PacketSendingContext, FriendlyByteBuf>, Unit> {
    override fun accept(i: Pair<PacketSendingContext, FriendlyByteBuf>) {
        runBlocking {
            val (ctx, buf) = i
            val arr = buf.dump()
            ctx.sendChannel.writeFully(arr, 0, arr.size)
            ctx.sendChannel.writeByte('\n'.code.toByte())
        }
    }
}