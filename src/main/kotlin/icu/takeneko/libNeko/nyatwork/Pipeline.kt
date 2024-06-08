package icu.takeneko.libNeko.nyatwork

interface Pipeline<I, O> {
    fun accept(i: I): O
}

internal class SimplePipeline<I : Any, O : Any>(private val pipeline: MutableList<PipelineModule<Any, Any>>) :
    Pipeline<I, O> {
    override fun accept(i: I): O {
        var input: Any = i
        for (module in pipeline) {
            val out = module.accept(input)
                ?: throw IllegalArgumentException("Pipeline failed at module $module\ninput: $input\noutput: null")
            input = out
        }
        return input as O
    }
}

interface PipelineModule<I : Any, O : Any> {
    fun accept(i: I): O?
}

class PipelineBuilder<T : Any, I : Any, O : Any>(private val pipeline: MutableList<PipelineModule<Any, Any>>) {

    fun <O1 : Any> then(mod: PipelineModule<I, O1>): PipelineBuilder<T, I, O1> {
        pipeline.add(mod as PipelineModule<Any, Any>)
        return PipelineBuilder<T, I, O1>(pipeline)
    }

    fun finish(): Pipeline<T, O> {
        return SimplePipeline(pipeline)
    }
}

fun <I : Any, O : Any> pipeline(): PipelineBuilder<I, I, O> {
    return PipelineBuilder(mutableListOf())
}