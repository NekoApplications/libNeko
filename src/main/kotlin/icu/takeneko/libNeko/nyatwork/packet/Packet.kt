package icu.takeneko.libNeko.nyatwork.packet

import icu.takeneko.libNeko.nyatwork.PipelineModule
import icu.takeneko.libNeko.nyatwork.util.FriendlyByteBuf
import icu.takeneko.libNeko.registry.BuiltinRegistries

abstract class Packet {

    abstract fun handle(ctx: PacketHandlingContext)
}

interface PacketDecoder<O : Packet> {
    fun decode(i: FriendlyByteBuf): O?
}

interface PacketEncoder<I : Packet> {
    fun encode(i: I, buf: FriendlyByteBuf)
}

object PacketHandler : PipelineModule<Pair<PacketHandlingContext, Packet>, Unit> {
    override fun accept(p: Pair<PacketHandlingContext, Packet>) {
        p.second.handle(p.first)
    }
}


object RegistryPacketDecoder : PipelineModule<PacketHandlingContext, Pair<PacketHandlingContext, Packet>> {
    fun decode(i: FriendlyByteBuf): Packet? {
        val id = i.readIdentifier()
        val clazz = BuiltinRegistries.packetTypes.get(id) ?: return null
        val decoder = BuiltinRegistries.packetDecoder.get(id) ?: return null
        return clazz.cast(decoder.decode(i))
    }

    override fun accept(i: PacketHandlingContext): Pair<PacketHandlingContext, Packet>? {
        return i to (decode(i.buf) ?: return null)
    }
}

object RegistryPacketEncoder : PipelineModule<PacketSendingContext, FriendlyByteBuf> {

    fun encode(i: Packet): FriendlyByteBuf? {
        val buf = FriendlyByteBuf.createEmpty()
        val id = BuiltinRegistries.packetTypes.getKey(i::class.java) ?: return null
        val encoder = BuiltinRegistries.packetEncoder.get(id) ?: return null
        buf.writeIdentifier(id)
        encoder.encode(i, buf)
        return buf
    }

    override fun accept(i: PacketSendingContext): FriendlyByteBuf? {
        return encode(i.packetContext)
    }
}


