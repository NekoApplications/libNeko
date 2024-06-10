package icu.takeneko.libNeko.nyatwork

import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryData
import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryPacketReceiver
import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryPacketSender
import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryResult
import icu.takeneko.libNeko.registry.Identifier
import org.junit.jupiter.api.Test
import kotlin.test.expect

class DiscoveryTest {
    @Test
    fun discoveryTest() {
        val sender = DiscoveryPacketSender("224.114.51.2", 24444, 1000)
        val receiver = DiscoveryPacketReceiver("224.114.51.2", 24444)
        sender.dataProvider(::data)
        receiver.onServerFound {
            println(it)
            expect(data()){it}
            sender.stop()
            DiscoveryResult.ACCEPT
        }
        sender.start()
        receiver.start()
        receiver.join()
    }

    private fun data(): DiscoveryData {
        return DiscoveryData("localhost", 10000, Identifier("nyatwork", "test"))
    }
}