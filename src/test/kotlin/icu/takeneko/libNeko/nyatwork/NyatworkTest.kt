package icu.takeneko.libNeko.nyatwork

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.expect

class NyatworkTest {
    @Test
    fun pipelineTest() {
        val mod1 = object : PipelineModule<Int, Int> {
            override fun accept(i: Int): Int {
                println("input $i")
                return i.inc().also { println("output $it") }
            }
        }

        val mod2 = object : PipelineModule<Int, Float> {
            override fun accept(i: Int): Float {
                println("input $i")
                return (i.dec() + 0.15f).also { println("output $it") }
            }
        }

        val mod3 = object : PipelineModule<Float, String> {
            override fun accept(i: Float): String {
                println("input $i")
                return (i + 114514).toString().also { println("output $it") }
            }
        }

        val pipeline = pipeline<Int, String>()
            .start(mod1)
            .then(mod2)
            .then(mod3)
            .finish()

        expect("114524.15") {
            pipeline.accept(10).also { println("finish $it") }
        }
    }

    @Test
    fun bufTest() {
        val buf = FriendlyByteBuf.createEmpty()
        val uuid = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()
        buf.writeUUID(uuid)
        buf.writeVarInt(1000)
        buf.writeVarLong(1000L)
        buf.writeUtf("Hello World!")
        buf.put(1)
        buf.writeBoolean(false)
        buf.writeCollection(listOf(1, 2, 3), FriendlyByteBuf::putInt)
        buf.writeCollection(listOf(uuid, uuid2, uuid), FriendlyByteBuf::writeUUID)

        buf.readerIndex(0)

        expect(uuid) { buf.readUUID() }
        expect(1000) { buf.readVarInt() }
        expect(1000L) { buf.readVarLong() }
        expect("Hello World!") { buf.readUtf() }
        expect(1) { buf.get() }
        expect(false) { buf.readBoolean() }
        expect(listOf(1, 2, 3)) { buf.readList(FriendlyByteBuf::getInt) }
        expect(listOf(uuid, uuid2, uuid)) { buf.readList(FriendlyByteBuf::readUUID) }

    }
}