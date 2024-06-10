package icu.takeneko.libNeko.nyatwork

import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryData
import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryPacketReceiver
import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryResult
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.Socket

abstract class NyatworkClient<T: NyatworkClient<T>>(
        inPipelineConfigurator: PipelineBuilder<PacketHandlingContext, PacketHandlingContext, Unit>.(T) -> Unit,
    val outPipelineConfigurator: PipelineBuilder<PacketSendingContext, PacketSendingContext, Unit>.(T) -> PipelineBuilder<PacketSendingContext, PacketSendingContext, FriendlyByteBuf>
) : NyatworkService<DiscoveryPacketReceiver, T>(inPipelineConfigurator, {
    outPipelineConfigurator(this, it)
        .then(object :PipelineModule<FriendlyByteBuf, Unit>{
            override fun accept(i: FriendlyByteBuf) {
                runBlocking {
                    it.outChannel.writeFully(i.dump())
                }
            }
        })
}, "NyatworkClient") {

    private lateinit var discovery: DiscoveryPacketReceiver
    private lateinit var socket: Socket
    private lateinit var outChannel: ByteWriteChannel

    override fun configureDiscovery(configurator: () -> DiscoveryPacketReceiver) {
        discovery = configurator()
        discovery.onServerFound(::onServerFound)
    }

    fun sendPacket(packet: Packet) {
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
                    while (true) {
                        try {
                            val line = (receiveChannel.readUTF8Line() ?: continue).encodeToByteArray()
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