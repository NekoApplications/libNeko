package icu.takeneko.libNeko.registry

import icu.takeneko.libNeko.nyatwork.packet.Packet
import icu.takeneko.libNeko.nyatwork.packet.PacketDecoder
import icu.takeneko.libNeko.nyatwork.packet.PacketEncoder

object BuiltinRegistries {

    val packetDecoder = Registry<PacketDecoder<Packet>>(Identifier("nyatwork","packet/decoder"))
    val packetEncoder = Registry<PacketEncoder<Packet>>(Identifier("nyatwork","packet/encoder"))
    val packetTypes = Registry<Class<out Packet>>(Identifier("nyatwork", "packet/class"))

    fun finish() {
        packetTypes.freeze()
        packetDecoder.freeze()
        packetEncoder.freeze()
    }
}