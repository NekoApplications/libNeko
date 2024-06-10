package icu.takeneko.libNeko.nyatwork

import icu.takeneko.libNeko.nyatwork.packet.PacketHandlingContext
import icu.takeneko.libNeko.nyatwork.packet.PacketSendingContext
import kotlin.properties.Delegates

abstract class NyatworkService<T, S : NyatworkService<T, S>>(
    inPipelineConfigurator: PipelineBuilder<PacketHandlingContext, PacketHandlingContext, Unit>.(S) -> Unit,
    outPipelineConfigurator: PipelineBuilder<PacketSendingContext, PacketSendingContext, Unit>.(S) -> Unit,
    private val threadName: String
) {

    protected val inPipeline: Pipeline<PacketHandlingContext, Unit>
    protected val outPipeline: Pipeline<PacketSendingContext, Unit>
    lateinit var targetAddress: String
    var targetPort by Delegates.notNull<Int>()
    var thread: Thread = createThread()

    init {
        inPipeline = pipeline<PacketHandlingContext, Unit>().also { inPipelineConfigurator(it, this as S) }.finish()
        outPipeline = pipeline<PacketSendingContext, Unit>().also { outPipelineConfigurator(it, this as S) }.finish()
    }

    abstract fun configureDiscovery(configurator: () -> T)

    private fun createThread() = kotlin.concurrent.thread(start = false, block = this::serviceThread, name = threadName)

    fun configureAddress(address: String, port: Int) {
        this.targetAddress = address
        this.targetPort = port
    }

    abstract fun serviceThread()

    fun start() {
        if (thread.isAlive) {
            thread.interrupt()
            thread = createThread()
        }
        thread.start()
    }

    fun stop() {
        if (thread.isAlive) {
            thread.interrupt()
        }
        thread = createThread()
    }
}