package icu.takeneko.libNeko.nyatwork

import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryData
import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryResult
import icu.takeneko.libNeko.nyatwork.packet.Packet
import icu.takeneko.libNeko.nyatwork.packet.PacketDecoder
import icu.takeneko.libNeko.nyatwork.packet.PacketEncoder
import icu.takeneko.libNeko.nyatwork.packet.PacketHandlingContext
import icu.takeneko.libNeko.nyatwork.registry.RegistryNyatworkClient
import icu.takeneko.libNeko.nyatwork.registry.RegistryNyatworkDedicatedServer
import icu.takeneko.libNeko.nyatwork.util.FriendlyByteBuf
import icu.takeneko.libNeko.registry.BuiltinRegistries
import icu.takeneko.libNeko.registry.Identifier
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.locks.LockSupport

class ServiceTest {

    class TestServer : RegistryNyatworkDedicatedServer(Identifier("nyatwork", "test/server")) {
        override fun onServiceException(e: Throwable) {
            e.printStackTrace()
        }

        override fun getDiscoveryData(): DiscoveryData {
            return DiscoveryData("localhost", 10000, Identifier("nyatwork", "test/server"))
        }
    }

    class TestClient : RegistryNyatworkClient() {
        override fun onServiceException(e: Throwable) {
            e.printStackTrace()
        }

        override fun onServerFound(dd: DiscoveryData): DiscoveryResult {
            println(dd)
            return DiscoveryResult.IGNORE
        }
    }

    class TestPacket(val uuid:UUID, val s:String) : Packet() {
        override fun handle(ctx: PacketHandlingContext) {
            println("TestPacket handle $uuid, $s")
            Exception().printStackTrace()
            ctx.responsePacket(TestResponsePacket("Hello!"))
        }

        object Encoder:PacketEncoder<TestPacket>{
            override fun encode(i: TestPacket, buf: FriendlyByteBuf) {
                buf.writeUUID(i.uuid)
                buf.writeUtf(i.s)
            }
        }

        object Decoder: PacketDecoder<TestPacket>{
            override fun decode(i: FriendlyByteBuf): TestPacket {
                val uuid = i.readUUID()
                val s = i.readUtf()
                return TestPacket(uuid, s)
            }
        }

        companion object{
            val ID = Identifier("nyatwork", "packet/test")
        }
    }

    class TestResponsePacket(val s:String) : Packet() {
        override fun handle(ctx: PacketHandlingContext) {
            println("TestResponsePacket handle $s")
            Exception().printStackTrace()
        }

        object Encoder:PacketEncoder<TestResponsePacket>{
            override fun encode(i: TestResponsePacket, buf: FriendlyByteBuf) {
                buf.writeUtf(i.s)
            }
        }

        object Decoder: PacketDecoder<TestResponsePacket>{
            override fun decode(i: FriendlyByteBuf): TestResponsePacket {
                val s = i.readUtf()
                return TestResponsePacket(s)
            }
        }

        companion object{
            val ID = Identifier("nyatwork", "packet/response")
        }
    }

    @Test
    fun service() {
        val server = TestServer()
        val client = TestClient()
        BuiltinRegistries.packetTypes.register(TestPacket.ID, TestPacket::class.java)
        BuiltinRegistries.packetTypes.register(TestResponsePacket.ID, TestResponsePacket::class.java)

        BuiltinRegistries.packetEncoder.register(TestPacket.ID, TestPacket.Encoder as PacketEncoder<Packet>)
        BuiltinRegistries.packetDecoder.register(TestPacket.ID, TestPacket.Decoder as PacketDecoder<Packet>)

        BuiltinRegistries.packetEncoder.register(TestResponsePacket.ID, TestResponsePacket.Encoder as PacketEncoder<Packet>)
        BuiltinRegistries.packetDecoder.register(TestResponsePacket.ID, TestResponsePacket.Decoder as PacketDecoder<Packet>)

        server.configureAddress("0.0.0.0", 10000)
        client.configureAddress("localhost", 10000)

        server.start()
        while (!server.ready){
            LockSupport.parkNanos(1000)
        }
        client.start()
        client.sendPacket(TestPacket(UUID.randomUUID(), "Hello!!"))

    }
}