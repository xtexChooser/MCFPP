package top.mcfpp.type

import net.querz.nbt.tag.*
import top.mcfpp.core.lang.ImmutableList
import top.mcfpp.core.lang.ImmutableListConcrete
import top.mcfpp.core.lang.Var
import top.mcfpp.core.lang.nbt.*
import top.mcfpp.core.lang.nbt.ByteArray
import top.mcfpp.core.lang.nbt.IntArray
import top.mcfpp.core.lang.nbt.LongArray
import top.mcfpp.model.Class
import top.mcfpp.model.CompoundData
import top.mcfpp.model.FieldContainer

/**
 * 以NBT为底层的类型，包括普通的NBT类型，以及由nbt实现的map，list和dict
 */
class MCFPPNBTType {
    object NBT : MCFPPType(arrayListOf(MCFPPBaseType.Any)) {

        override val objectData: CompoundData
            get() = NBTBasedData.data

        override val typeName: String
            get() = "nbt"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            NBTBasedDataConcrete(container, IntTag(0), identifier)

        override fun build(identifier: String): Var<*> = NBTBasedDataConcrete(IntTag(0), identifier)
        override fun build(identifier: String, clazz: Class): Var<*> =
            NBTBasedDataConcrete(clazz, IntTag(0), identifier)

        override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> =
            NBTBasedData(identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = NBTBasedData(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = NBTBasedData(identifier)

    }

    object Byte: MCFPPType(arrayListOf(MCFPPBaseType.Int)){

        override val objectData: CompoundData
            get() = NBTBasedData.data

        override val typeName: String
            get() = "byte"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            MCByteConcrete(container, 0, identifier)

        override fun build(identifier: String): Var<*> = MCByteConcrete(0, identifier)
        override fun build(identifier: String, clazz: Class): Var<*> =
            MCByteConcrete(clazz, 0, identifier)

        override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> =
            MCByte(container, identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = MCByte(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = MCByte(clazz, identifier)
    }

    object Short: MCFPPType(arrayListOf(MCFPPBaseType.Int)){

        override val objectData: CompoundData
            get() = NBTBasedData.data

        override val typeName: String
            get() = "short"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            MCShortConcrete(container, 0, identifier)

        override fun build(identifier: String): Var<*> = MCShortConcrete(0, identifier)
        override fun build(identifier: String, clazz: Class): Var<*> =
            MCShortConcrete(clazz, 0, identifier)

        override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> =
            MCShort(container, identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = MCShort(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = MCShort(clazz, identifier)
    }

    object Long: MCFPPType(arrayListOf(NBT)){

        override val objectData: CompoundData
            get() = NBTBasedData.data

        override val typeName: String
            get() = "long"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            MCLongConcrete(LongTag(0), identifier)

        override fun build(identifier: String): Var<*> = MCLongConcrete(LongTag(0), identifier)
        override fun build(identifier: String, clazz: Class): Var<*> =
            MCLongConcrete(LongTag(0), identifier)

        override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> =
            MCLong(identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = MCLong(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = MCLong(identifier)
    }

    object Double: MCFPPType(arrayListOf(NBT)){

        override val objectData: CompoundData
            get() = NBTBasedData.data

        override val typeName: String
            get() = "double"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            MCDoubleConcrete(container, DoubleTag(0.0), identifier)

        override fun build(identifier: String): Var<*> = MCDoubleConcrete(DoubleTag(0.0), identifier)
        override fun build(identifier: String, clazz: Class): Var<*> =
            MCDoubleConcrete(clazz, DoubleTag(0.0), identifier)

        override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> =
            MCDouble(identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = MCLong(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = MCLong(identifier)
    }

    object ByteArray: MCFPPType(arrayListOf(NBT)){

        override val objectData: CompoundData
            get() = NBTBasedData.data

        override val typeName: String
            get() = "ByteArray"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            ByteArrayConcrete(ByteArrayTag(), identifier)

        override fun build(identifier: String): Var<*> = ByteArrayConcrete(ByteArrayTag(), identifier)
        override fun build(identifier: String, clazz: Class): Var<*> =
            ByteArrayConcrete(ByteArrayTag(), identifier)

        override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> =
            ByteArray(identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = ByteArray(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = ByteArray(identifier)
    }

    object IntArray: MCFPPType(arrayListOf(NBT)){

        override val objectData: CompoundData
            get() = NBTBasedData.data

        override val typeName: String
            get() = "IntArray"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            IntArrayConcrete(IntArrayTag(), identifier)

        override fun build(identifier: String): Var<*> = IntArrayConcrete(IntArrayTag(), identifier)
        override fun build(identifier: String, clazz: Class): Var<*> =
            IntArrayConcrete(IntArrayTag(), identifier)

        override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> =
            IntArray(identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = IntArray(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = IntArray(identifier)
    }

    object LongArray: MCFPPType(arrayListOf(NBT)){

        override val objectData: CompoundData
            get() = NBTBasedData.data

        override val typeName: String
            get() = "LongArray"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            LongArrayConcrete(LongArrayTag(), identifier)

        override fun build(identifier: String): Var<*> = LongArrayConcrete(LongArrayTag(), identifier)
        override fun build(identifier: String, clazz: Class): Var<*> =
            LongArrayConcrete(LongArrayTag(), identifier)

        override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> =
            LongArray(identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = LongArray(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = LongArray(identifier)
    }

}

class MCFPPListType(
    val generic: MCFPPType = MCFPPBaseType.Any
): MCFPPType(arrayListOf(MCFPPNBTType.NBT)){

    override val objectData: CompoundData
        get() = NBTList.data

    override val typeName: String
        get() = "list"

    override val nbtType: java.lang.Class<out Tag<*>>
        get() = ListTag::class.java

    override fun build(identifier: String, container: FieldContainer): Var<*> = NBTListConcrete(ArrayList(), identifier, generic)
    override fun build(identifier: String): Var<*> = NBTListConcrete(ArrayList(), identifier, generic)
    override fun build(identifier: String, clazz: Class): Var<*> = NBTListConcrete(ArrayList(), identifier, generic)
    override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> = NBTList(identifier, generic)
    override fun buildUnConcrete(identifier: String): Var<*> = NBTList(identifier, generic)
    override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = NBTList(identifier, generic)

    override fun toString(): String {
        return "list[${generic.typeName}]"
    }
}

class MCFPPImmutableListType(
    val generic: MCFPPType = MCFPPBaseType.Any
): MCFPPType(arrayListOf(MCFPPNBTType.NBT)){

    override val objectData: CompoundData
        get() = NBTList.data

    override val typeName: String
        get() = "ImmutableList"

    override val nbtType: java.lang.Class<out Tag<*>>
        get() = ListTag::class.java

    override fun build(identifier: String, container: FieldContainer): Var<*> = ImmutableListConcrete(ListTag(IntTag::class.java), identifier, generic)
    override fun build(identifier: String): Var<*> = ImmutableListConcrete(ListTag(IntTag::class.java), identifier, generic)
    override fun build(identifier: String, clazz: Class): Var<*> = ImmutableListConcrete(ListTag(IntTag::class.java), identifier, generic)
    override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> = ImmutableList(identifier, generic)
    override fun buildUnConcrete(identifier: String): Var<*> = ImmutableList(identifier, generic)
    override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = ImmutableList(identifier, generic)

    override fun toString(): String {
        return "ImmutableList[${generic.typeName}]"
    }

}

open class MCFPPCompoundType(
    val generic: MCFPPType
): MCFPPType(arrayListOf(MCFPPNBTType.NBT)){

    override val typeName: String
        get() = "compound"

    override fun toString(): String {
        return "compound[${generic.typeName}]"
    }

}

class MCFPPDictType(generic: MCFPPType): MCFPPCompoundType(generic){
    override val typeName: String
        get() = "dict"

    override fun toString(): String {
        return "dict[${generic.typeName}]"
    }

    override fun build(identifier: String, container: FieldContainer): Var<*> = NBTDictionaryConcrete(HashMap(), identifier)
    override fun build(identifier: String): Var<*> = NBTDictionaryConcrete(HashMap(), identifier)
    override fun build(identifier: String, clazz: Class): Var<*> = NBTDictionaryConcrete(HashMap(), identifier)
    override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> = NBTDictionary(identifier)
    override fun buildUnConcrete(identifier: String): Var<*> = NBTDictionary(identifier)
    override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = NBTDictionary(identifier)
}

class MCFPPMapType(generic: MCFPPType): MCFPPCompoundType(generic){
    override val typeName: String
        get() = "map"

    override fun toString(): String {
        return "map[${generic.typeName}]"
    }

    override fun build(identifier: String, container: FieldContainer): Var<*> = NBTMapConcrete(HashMap(), identifier, generic)
    override fun build(identifier: String): Var<*> = NBTMapConcrete(HashMap(), identifier, generic)
    override fun build(identifier: String, clazz: Class): Var<*> = NBTMapConcrete(HashMap(), identifier, generic)
    override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> = NBTMap(identifier, generic)
    override fun buildUnConcrete(identifier: String): Var<*> = NBTMap(identifier, generic)
    override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = NBTMap(identifier, generic)
}
