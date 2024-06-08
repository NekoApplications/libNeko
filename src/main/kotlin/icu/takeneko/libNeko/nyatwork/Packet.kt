package icu.takeneko.libNeko.nyatwork

import icu.takeneko.libNeko.registry.BuiltinRegistries

interface Packet {

    fun handle()
}

interface PacketDecoder<O : Packet> {
    fun decode(i: FriendlyByteBuf): O?
}

interface PacketEncoder<I : Packet> {
    fun encode(i: I, buf: FriendlyByteBuf)
}

class PacketHandlerModule() : PipelineModule<Packet, Unit> {
    override fun accept(i: Packet) {
        i.handle()
    }
}


object RegistryPacketDecoder : PipelineModule<FriendlyByteBuf, Packet> {
    fun decode(i: FriendlyByteBuf): Packet? {
        val id = i.readIdentifier()
        val decoder = BuiltinRegistries.packetDecoder.get(id) ?: return null
        return decoder.decode(i)
    }

    override fun accept(i: FriendlyByteBuf): Packet? {
        return decode(i)
    }
}

object RegistryPacketEncoder : PipelineModule<Packet, FriendlyByteBuf> {

    fun encode(i: Packet): FriendlyByteBuf? {
        val buf = FriendlyByteBuf.createEmpty()
        val id = BuiltinRegistries.packetTypes.getKey(i::class.java) ?: return null
        val encoder = BuiltinRegistries.packetEncoder.get(id) ?: return null
        buf.writeIdentifier(id)
        encoder.encode(i, buf)
        return buf
    }

    override fun accept(i: Packet): FriendlyByteBuf? {
        return encode(i)
    }
}
