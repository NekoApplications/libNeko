package icu.takeneko.libNeko.nyatwork.registry

import icu.takeneko.libNeko.nyatwork.PipelineModule
import icu.takeneko.libNeko.nyatwork.packet.Packet
import icu.takeneko.libNeko.nyatwork.packet.PacketSendingContext
import icu.takeneko.libNeko.nyatwork.util.FriendlyByteBuf
import icu.takeneko.libNeko.registry.BuiltinRegistries

internal object DedicatedServerPacketEncoder :
    PipelineModule<PacketSendingContext, Pair<PacketSendingContext, FriendlyByteBuf>> {

    fun encode(i: Packet): FriendlyByteBuf? {
        val buf = FriendlyByteBuf.createEmpty()
        val id = BuiltinRegistries.packetTypes.getKey(i::class.java) ?: return null
        val encoder = BuiltinRegistries.packetEncoder.get(id) ?: return null
        buf.writeIdentifier(id)
        encoder.encode(i, buf)
        return buf
    }

    override fun accept(i: PacketSendingContext): Pair<PacketSendingContext, FriendlyByteBuf>? {
        return i to (encode(i.packetContext) ?: return null)
    }
}

