package top.mcfpp.util

import net.querz.nbt.io.SNBTUtil
import net.querz.nbt.tag.IntArrayTag
import java.nio.ByteBuffer
import java.util.*

class MCUUID {

    val uuid: UUID

    val uuidNBT: IntArrayTag

    val uuidSNBT : String

    constructor() : this(UUID.randomUUID())

    constructor(uuid: UUID){
        val bytes = ByteArray(16)
        val byteBuffer = ByteBuffer.wrap(bytes)
        byteBuffer.putLong(uuid.mostSignificantBits)
        byteBuffer.putLong(uuid.leastSignificantBits)
        val array = IntArray(4)
        array[0] = ByteBuffer.wrap(bytes.copyOfRange(0, 4)).int
//        val a = bytes[4]
//        val b = bytes[5]
//        bytes[4] = bytes[6]
//        bytes[5] = bytes[7]
//        bytes[6] = a
//        bytes[7] = b
        array[1] = ByteBuffer.wrap(bytes.copyOfRange(4, 8)).int
        array[2] = ByteBuffer.wrap(bytes.copyOfRange(8, 12)).int
        array[3] = ByteBuffer.wrap(bytes.copyOfRange(12, 16)).int
        uuidNBT = IntArrayTag(array)
        uuidSNBT = SNBTUtil.toSNBT(uuidNBT)
        this.uuid = uuid
    }

    constructor(uuid: IntArrayTag){
        // Extension function to convert Int to reversed ByteArray
        fun Int.toByteArray(): ByteArray {
            return ByteBuffer.allocate(4).putInt(this).array()
        }

        val array = uuid.value
        val bytes = ByteArray(16)

        // Convert each integer string to an integer, reverse the byte order, and copy to the byte array
        array[0].toByteArray().copyInto(bytes, 0)
        array[1].toByteArray().copyInto(bytes, 4)

//        val a = bytes[4]
//        val b = bytes[5]
//        bytes[4] = bytes[6]
//        bytes[5] = bytes[7]
//        bytes[6] = a
//        bytes[7] = b

        array[2].toByteArray().copyInto(bytes, 8)
        array[3].toByteArray().copyInto(bytes, 12)

        uuidNBT = uuid
        uuidSNBT = SNBTUtil.toSNBT(uuidNBT)
        this.uuid = UUID(
            ByteBuffer.wrap(bytes.copyOfRange(0, 8)).long,
            ByteBuffer.wrap(bytes.copyOfRange(8, 16)).long
        )
    }

    companion object{
        fun genFromString(string: String): MCUUID {
            return MCUUID(UUID.nameUUIDFromBytes(string.toByteArray()))
        }
    }
}