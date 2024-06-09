package icu.takeneko.libNeko.nyatwork

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister.Pack

abstract class NyatworkService<T>(
    inPipelineConfigurator: PipelineBuilder<FriendlyByteBuf, FriendlyByteBuf, Unit>.() -> Unit,
    outPipelineConfigurator: PipelineBuilder<Packet, Packet, FriendlyByteBuf>.() -> Unit
) {

    private val inPipeline: Pipeline<FriendlyByteBuf, Unit>
    private val outPipeline: Pipeline<Packet, FriendlyByteBuf>

    init {
        inPipeline = pipeline<FriendlyByteBuf, Unit>().also(inPipelineConfigurator).finish()
        outPipeline = pipeline<Packet, FriendlyByteBuf>().also(outPipelineConfigurator).finish()
    }

    abstract fun configureDiscovery(configurator: () -> T)
}