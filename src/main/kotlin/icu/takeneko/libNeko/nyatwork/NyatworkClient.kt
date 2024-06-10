package icu.takeneko.libNeko.nyatwork

import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryData
import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryPacketReceiver
import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryResult
import icu.takeneko.libNeko.nyatwork.packet.Packet
import icu.takeneko.libNeko.nyatwork.packet.PacketHandlingContext
import icu.takeneko.libNeko.nyatwork.packet.PacketSendingContext
import icu.takeneko.libNeko.nyatwork.util.FriendlyByteBuf
import icu.takeneko.libNeko.util.readByteArrayLine
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.Socket
import java.util.concurrent.locks.LockSupport

abstract class NyatworkClient<T: NyatworkClient<T>>(
    inPipelineConfigurator: PipelineBuilder<PacketHandlingContext, PacketHandlingContext, Unit>.(T) -> Unit,
    val outPipelineConfigurator: PipelineBuilder<PacketSendingContext, PacketSendingContext, Unit>.(T) -> PipelineBuilder<PacketSendingContext, PacketSendingContext, FriendlyByteBuf>
) : NyatworkService<DiscoveryPacketReceiver, T>(inPipelineConfigurator, {
    outPipelineConfigurator(this, it)
        .then(object :PipelineModule<FriendlyByteBuf, Unit>{
            override fun accept(i: FriendlyByteBuf) {
                runBlocking {
                    it.outChannel.writeFully(i.dump())
                    it.outChannel.writeByte('\n'.code.toByte())
                }
            }
        })
}, "NyatworkClient") {

    private lateinit var discovery: DiscoveryPacketReceiver
    private lateinit var socket: Socket
    private lateinit var outChannel: ByteWriteChannel
    private var ready = false

    override fun configureDiscovery(configurator: () -> DiscoveryPacketReceiver) {
        discovery = configurator()
        discovery.onServerFound(::onServerFound)
    }

    fun sendPacket(packet: Packet) {
        while (!ready){
            LockSupport.parkNanos(10)
        }
        outPipeline.accept(PacketSendingContext(packet, outChannel, this.inPipeline, this.outPipeline))
    }

    override fun serviceThread() {
        runBlocking {
            try {
                val selectorManager = SelectorManager(Dispatchers.IO)
                val socket = aSocket(selectorManager).tcp().connect(targetAddress, targetPort)
                launch {
                    val receiveChannel = socket.openReadChannel()
                    val sendChannel = socket.openWriteChannel(autoFlush = true)
                    this@NyatworkClient.outChannel = sendChannel
                    while (true) {
                        try {
                            ready = true
                            val line = receiveChannel.readByteArrayLine()
                            val buf = FriendlyByteBuf.wrap(line)
                            inPipeline.accept(PacketHandlingContext(buf, sendChannel, inPipeline, outPipeline))
                        } catch (e: Throwable) {
                            onServiceException(e)
                        }
                    }
                }
            } catch (e: Throwable) {
                onServiceException(e)
            }
        }
    }

    abstract fun onServiceException(e: Throwable)

    abstract fun onServerFound(dd: DiscoveryData): DiscoveryResult

}