package icu.takeneko.libNeko.nyatwork.discovery

import icu.takeneko.libNeko.nyatwork.util.FriendlyByteBuf
import icu.takeneko.libNeko.registry.Identifier

data class DiscoveryData(
    val serviceIP: String,
    val servicePort: Int,
    val serviceId: Identifier
) {
    fun encode(buf: FriendlyByteBuf): Int {
        buf.writeIdentifier(serviceId)
        buf.writeUtf(serviceIP)
        buf.writeVarInt(servicePort)
        return buf.writerIndex()
    }

    companion object {
        fun decode(buf: FriendlyByteBuf): DiscoveryData {
            val id = buf.readIdentifier()
            val ip = buf.readUtf()
            val port = buf.readVarInt()
            return DiscoveryData(ip, port, id)
        }
    }
}