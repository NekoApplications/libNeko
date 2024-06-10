package icu.takeneko.libNeko.nyatwork.discovery

import icu.takeneko.libNeko.nyatwork.FriendlyByteBuf
import java.net.*
import kotlin.concurrent.thread

class DiscoveryPacketSender(private val address: String, private val port: Int, private val interval: Long = 1000) {

    private lateinit var dataProvider: () -> DiscoveryData
    private var thread: Thread = newThread()

    fun dataProvider(pv: () -> DiscoveryData) {
        this.dataProvider = pv
    }

    private fun newThread(): Thread = thread(start = false, name = "DiscoveryPacketSender", block = ::send)

    fun start() {
        if (thread.isAlive) {
            thread.interrupt()
            thread = newThread()
        }
        thread.start()
    }

    fun stop() {
        if (thread.isAlive) thread.interrupt()
        thread = newThread()
    }

    fun send() {
        val socket = MulticastSocket(port)
        val inetAddress = InetAddress.getByName(address)
        socket.joinGroup(InetSocketAddress(address, port), NetworkInterface.getByInetAddress(inetAddress))
        while (true) {
            try {
                val buf = FriendlyByteBuf.createEmpty()
                dataProvider().encode(buf)
                val byteArray = ByteArray(buf.readerIndex())
                buf.readerIndex(0)
                buf.readBytes(byteArray)
                val packet = DatagramPacket(byteArray, byteArray.size, inetAddress, port)
                socket.send(packet)
                Thread.sleep(interval)
            } catch (_: InterruptedException) {
            }
        }
    }
}