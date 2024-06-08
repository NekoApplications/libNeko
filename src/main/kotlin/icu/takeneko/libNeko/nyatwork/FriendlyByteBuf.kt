package icu.takeneko.libNeko.nyatwork

import icu.takeneko.libNeko.registry.Identifier
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*
import java.util.function.IntFunction

class FriendlyByteBuf(private val parent: ByteBuffer) {
    fun isReadOnly(): Boolean = parent.isReadOnly

    fun isDirect(): Boolean = parent.isDirect

    fun duplicate(): FriendlyByteBuf = FriendlyByteBuf(parent.duplicate())

    fun get(): Byte = parent.get()

    fun get(index: Int): Byte = parent.get(index)

    fun put(b: Byte) {
        parent.put(b)
    }

    fun put(index: Int, b: Byte) {
        parent.put(index, b)
    }

    fun getChar(): Char {
        return parent.getChar()
    }

    fun getChar(index: Int): Char {
        return parent.getChar(index)
    }

    fun putChar(value: Char): ByteBuffer {
        return parent.putChar(value)
    }

    fun putChar(index: Int, value: Char): ByteBuffer {
        return parent.putChar(index, value)
    }

    fun getShort(): Short {
        return parent.getShort()
    }

    fun getShort(index: Int): Short {
        return parent.getShort(index)
    }

    fun putShort(value: Short): ByteBuffer {
        return parent.putShort(value)
    }

    fun putShort(index: Int, value: Short): ByteBuffer {
        return parent.putShort(index, value)
    }

    fun getInt(): Int {
        return parent.getInt()
    }

    fun getInt(index: Int): Int {
        return parent.getInt(index)
    }

    fun putInt(value: Int): ByteBuffer {
        return parent.putInt(value)
    }

    fun putInt(index: Int, value: Int): ByteBuffer {
        return parent.putInt(index, value)
    }


    fun getLong(): Long {
        return parent.getLong()
    }

    fun getLong(index: Int): Long {
        return parent.getLong(index)
    }

    fun putLong(value: Long): ByteBuffer {
        return parent.putLong(value)
    }

    fun putLong(index: Int, value: Long): ByteBuffer {
        return parent.putLong(index, value)
    }

    fun getFloat(): Float {
        return parent.getFloat()
    }

    fun getFloat(index: Int): Float {
        return parent.getFloat(index)
    }

    fun putFloat(value: Float): ByteBuffer {
        return parent.putFloat(value)
    }

    fun putFloat(index: Int, value: Float): ByteBuffer {
        return parent.putFloat(index, value)
    }

    fun getDouble(): Double {
        return parent.getDouble()
    }

    fun getDouble(index: Int): Double {
        return parent.getDouble(index)
    }

    fun putDouble(value: Double): ByteBuffer {
        return parent.putDouble(value)
    }

    fun putDouble(index: Int, value: Double): ByteBuffer {
        return parent.putDouble(index, value)
    }

    private fun getMaxEncodedUtfLength(maxLength: Int): Int {
        return maxLength * 3
    }

    fun readUtf(maxLength: Int = 32768): String {
        val maxBytes: Int = getMaxEncodedUtfLength(maxLength)
        val size: Int = this.readVarInt()
        if (size > maxBytes) {
            throw RuntimeException("The received encoded string buffer length is longer than maximum allowed ($size > $maxBytes)")
        } else if (size < 0) {
            throw RuntimeException("The received encoded string buffer length is less than zero! Weird string!")
        } else {
            val byteArray = ByteArray(size)
            for (i in 0 until size) {
                byteArray[i] = get()
            }
            val string = String(byteArray, StandardCharsets.UTF_8)
            if (string.length > maxBytes) {
                throw RuntimeException("The received string length is longer than maximum allowed (${string.length} > $maxBytes)")
            } else {
                return string
            }
        }
    }

    private fun readerIndex(int: Int) {
        parent.position(int)
    }

    private fun readerIndex(): Int {
        return parent.position()
    }


    fun writeUtf(string: String): FriendlyByteBuf {
        return this.writeUtf(string, 32767)
    }

    fun writeUtf(string: String, maxLength: Int): FriendlyByteBuf {
        if (string.length > maxLength) {
            val var10002 = string.length
            throw RuntimeException("String too big (was $var10002 characters, max $maxLength)")
        } else {
            val bs = string.toByteArray(StandardCharsets.UTF_8)
            val i: Int = getMaxEncodedUtfLength(maxLength)
            if (bs.size > i) {
                throw RuntimeException("String too big (was " + bs.size + " bytes encoded, max " + i + ")")
            } else {
                this.writeVarInt(bs.size)
                for (b in bs) {
                    this.put(b)
                }
                return this
            }
        }
    }

    fun writeVarInt(i: Int) {
        var input = i
        while ((input and -128) != 0) {
            this.put((input and 127 or 128).toByte())
            input = input ushr 7
        }

        this.put(input.toByte())
    }

    fun writeVarLong(v: Long): FriendlyByteBuf {
        var value = v
        while ((value and -128L) != 0L) {
            this.put(((value and 127L).toInt() or 128).toByte())
            value = value ushr 7
        }

        this.put(value.toByte())
        return this
    }

    fun <T : Enum<T>> readEnum(enumClass: Class<T>): T {
        return (enumClass.enumConstants as Array<Enum<*>>)[readVarInt()] as T
    }

    fun writeEnum(value: Enum<*>) {
        this.writeVarInt(value.ordinal)
    }

    fun readVarInt(): Int {
        var i = 0
        var j = 0

        var b: Byte
        do {
            b = this.get()
            i = i or ((b.toInt() and 127) shl (j++ * 7))
            if (j > 5) {
                throw java.lang.RuntimeException("VarInt too big")
            }
        } while ((b.toInt() and 128) == 128)

        return i
    }

    fun readVarLong(): Long {
        var l = 0L
        var i = 0

        var b: Byte
        do {
            b = this.get()
            l = l or ((b.toInt() and 127).toLong() shl (i++ * 7))
            if (i > 10) {
                throw java.lang.RuntimeException("VarLong too big")
            }
        } while ((b.toInt() and 128) == 128)

        return l
    }

    fun writeIdentifier(id: Identifier) {
        this.writeUtf(id.namespace)
        this.writeUtf(id.path)
    }

    fun readIdentifier(): Identifier {
        val namespace = this.readUtf()
        val path = this.readUtf()
        return Identifier(namespace, path)
    }

    fun writeUUID(uuid: UUID): FriendlyByteBuf {
        this.putLong(uuid.mostSignificantBits)
        this.putLong(uuid.leastSignificantBits)
        return this
    }

    fun readUUID(): UUID {
        return UUID(this.getLong(), this.getLong())
    }

    fun readInstant(): Instant {
        return Instant.ofEpochMilli(this.getLong())
    }

    fun writeInstant(instant: Instant) {
        this.putLong(instant.toEpochMilli())
    }

    fun writeBoolean(value: Boolean) {
        this.put(if (value) 0b1 else 0b0)
    }

    fun readBoolean(): Boolean {
        return this.get().toInt() == 1
    }

    fun readBytes(bs: ByteArray) {
        for (i in bs.indices) {
            bs[i] = get()
        }
    }

    fun <T, C : MutableCollection<T>> readCollection(
        collectionFactory: IntFunction<C>,
        elementReader: Reader<T>
    ): C {
        val i = this.readVarInt()
        val collection: C = collectionFactory.apply(i) as C

        for (j in 0 until i) {
            collection.add(elementReader.apply(this))
        }

        return collection
    }

    fun <T> writeCollection(collection: Collection<T>, elementWriter: Writer<T>) {
        this.writeVarInt(collection.size)
        for (t in collection) {
            elementWriter.accept(this, t)
        }
    }

    fun <T : Any> readList(elementReader: Reader<T>): List<T> {
        return readCollection<T, MutableCollection<T>>({u -> ArrayList<T>(u)}, elementReader) as List<T>
    }

    fun writeByteArray(array: ByteArray): FriendlyByteBuf {
        this.writeVarInt(array.size)
        this.writeBytes(array)
        return this
    }

    private fun writeBytes(array: ByteArray) {
        this.parent.put(array)
    }

    fun readByteArray(maxLength: Int): ByteArray {
        val i = this.readVarInt()
        if (i > maxLength) {
            throw RuntimeException("ByteArray with size $i is bigger than allowed $maxLength")
        } else {
            val bs = ByteArray(i)
            this.readBytes(bs)
            return bs
        }
    }


    fun <T> writeOptional(optional: Optional<T>, writer: Writer<T>) {
        if (optional.isPresent) {
            this.writeBoolean(true)
            writer.accept(this, optional.get())
        } else {
            this.writeBoolean(false)
        }
    }

    fun <T : Any> readOptional(reader: Reader<T>): Optional<T> {
        return if (this.readBoolean())
            Optional.of<T>(reader.apply(this))
        else
            Optional.empty<T>()
    }

    companion object {
        fun wrap(array: ByteArray): FriendlyByteBuf {
            val par = ByteBuffer.wrap(array)
            return FriendlyByteBuf(par)
        }

        fun createEmpty(size:Int = 32768): FriendlyByteBuf {
            val par = ByteBuffer.allocate(size)
            return FriendlyByteBuf(par)
        }
    }

}