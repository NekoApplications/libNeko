package icu.takeneko.libNeko.nyatwork.discovery

import icu.takeneko.libNeko.nyatwork.util.FriendlyByteBuf
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import kotlin.concurrent.thread

class DiscoveryPacketReceiver(private val address: String, private val port: Int) {

    private lateinit var onServerFound: (DiscoveryData) -> DiscoveryResult
    private var thread = newThread()

    private fun newThread(): Thread = thread(start = false, name = "DiscoveryPacketSender", block = ::recv)

    private fun recv() {
        val socket = MulticastSocket(port)
        socket.reuseAddress = true
        val inetAddress = InetAddress.getByName(address)
        socket.joinGroup(InetSocketAddress(inetAddress, port), null)
        val packet = DatagramPacket(ByteArray(32768), 32768)
        while (true) {
            try {
                socket.receive(packet)
                val ba = ByteArray(packet.length)
                var cursor = 0
                for (i in packet.offset until packet.length + packet.offset) {
                    ba[cursor++] = packet.data[i]
                }
                val buf = FriendlyByteBuf.wrap(ba)
                val data = DiscoveryData.decode(buf)
                when (onServerFound(data)) {
                    DiscoveryResult.IGNORE -> continue
                    DiscoveryResult.ACCEPT -> break
                }
            } catch (_: Exception) {
            } catch (_: IOException) {
                break
            }
        }
    }

    fun onServerFound(fn: (DiscoveryData) -> DiscoveryResult) {
        onServerFound = fn
    }

    fun stop() {
        if (thread.isAlive) thread.interrupt()
        thread = newThread()
    }

    fun start() {
        if (thread.isAlive) {
            thread.interrupt()
            thread = newThread()
        }
        thread.start()
    }

    fun join() {
        if (thread.isAlive){
            thread.join()
        }
    }
}