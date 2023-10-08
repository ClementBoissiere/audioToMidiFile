import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer

class CircularBuffer(private val capacity: Int) {
    private val buffer: ByteBuffer = ByteBuffer.allocate(capacity * elementSize)
    private var size = 0

    fun add(value: Pair<Int, Long>) {
        if (size >= capacity) {
            buffer.compact()
            buffer.position(size * elementSize)
            size--
        }
        writeValue(value)
        size++
    }

    fun get(index: Int): Pair<Int, Long> {
        buffer.position(index * elementSize)
        return readValue()
    }

    private fun writeValue(value: Pair<Int, Long>) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(byteArrayOutputStream)

        dataOutputStream.writeInt(value.first)
        dataOutputStream.writeLong(value.second)


        val serializedValue = byteArrayOutputStream.toByteArray()
        buffer.put(serializedValue)
    }

    private fun readValue(): Pair<Int, Long> {
        val byteArray = ByteArray(elementSize)
        buffer.get(byteArray)

        val byteArrayInputStream = ByteArrayInputStream(byteArray)
        val dataInputStream = DataInputStream(byteArrayInputStream)

        val firstValue = dataInputStream.readInt()
        val secondValue = dataInputStream.readLong()

        return Pair(firstValue, secondValue)
    }

    fun checkForKey(keyToCheck: Int): Boolean {
        if (size == 0) {
            return false
        }

        val firstPair = get(0)
        if (firstPair.first != keyToCheck) {
            return false
        }

        for (i in 1 until size) {
            val currentPair = get(i)
            if (currentPair.first != keyToCheck || currentPair != firstPair) {
                return false
            }
        }

        return true
    }

    companion object {
        private const val elementSize = 12 // Taille en octets de Pair<Int, Long>
    }
}
