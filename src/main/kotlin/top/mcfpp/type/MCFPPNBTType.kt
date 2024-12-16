package top.mcfpp.type

import net.querz.nbt.tag.*
import top.mcfpp.model.Class
import top.mcfpp.model.CompoundData
import top.mcfpp.model.FieldContainer
import top.mcfpp.core.lang.*
import top.mcfpp.core.lang.nbt.*
import top.mcfpp.core.lang.nbt.ByteArray

/**
 * 以NBT为底层的类型，包括普通的NBT类型，以及由nbt实现的map，list和dict
 */
class MCFPPNBTType {
    object NBT : MCFPPType(parentType = listOf(MCFPPBaseType.Any)) {

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
            NBTBasedData(container, identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = NBTBasedData(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = NBTBasedData(clazz, identifier)

    }

    object Byte: MCFPPType(parentType = listOf(MCFPPBaseType.Int)){

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

    object Short: MCFPPType(parentType = listOf(MCFPPBaseType.Int)){

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

    object Long: MCFPPType(parentType = listOf(NBT)){

        override val objectData: CompoundData
            get() = NBTBasedData.data

        override val typeName: String
            get() = "long"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            MCLongConcrete(container, LongTag(0), identifier)

        override fun build(identifier: String): Var<*> = MCLongConcrete(LongTag(0), identifier)
        override fun build(identifier: String, clazz: Class): Var<*> =
            MCLongConcrete(clazz, LongTag(0), identifier)

        override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> =
            MCLong(container, identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = MCLong(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = MCLong(clazz, identifier)
    }

    object Double: MCFPPType(parentType = listOf(NBT)){

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
            MCDouble(container, identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = MCLong(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = MCLong(clazz, identifier)
    }

    object ByteArray: MCFPPType(parentType = listOf(NBT)){

        override val objectData: CompoundData
            get() = NBTBasedData.data

        override val typeName: String
            get() = "ByteArray"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            ByteArrayConcrete(container, ByteArrayTag(), identifier)

        override fun build(identifier: String): Var<*> = ByteArrayConcrete(ByteArrayTag(), identifier)
        override fun build(identifier: String, clazz: Class): Var<*> =
            ByteArrayConcrete(clazz, ByteArrayTag(), identifier)

        override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> =
            ByteArray(container, identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = ByteArray(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = ByteArray(clazz, identifier)
    }

    object IntArray: MCFPPType(parentType = listOf(NBT)){

        override val objectData: CompoundData
            get() = NBTBasedData.data

        override val typeName: String
            get() = "IntArray"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            IntArrayConcrete(container, IntArrayTag(), identifier)

        override fun build(identifier: String): Var<*> = IntArrayConcrete(IntArrayTag(), identifier)
        override fun build(identifier: String, clazz: Class): Var<*> =
            IntArrayConcrete(clazz, IntArrayTag(), identifier)

        override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> =
            IntArray(container, identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = IntArray(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = IntArray(clazz, identifier)
    }

    object LongArray: MCFPPType(parentType = listOf(NBT)){

        override val objectData: CompoundData
            get() = NBTBasedData.data

        override val typeName: String
            get() = "LongArray"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            LongArrayConcrete(container, LongArrayTag(), identifier)

        override fun build(identifier: String): Var<*> = LongArrayConcrete(LongArrayTag(), identifier)
        override fun build(identifier: String, clazz: Class): Var<*> =
            LongArrayConcrete(clazz, LongArrayTag(), identifier)

        override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> =
            LongArray(container, identifier)

        override fun buildUnConcrete(identifier: String): Var<*> = LongArray(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = LongArray(clazz, identifier)
    }

}

class MCFPPListType(
    val generic: MCFPPType = MCFPPBaseType.Any
): MCFPPType(NBTList.data, listOf(MCFPPNBTType.NBT)){

    override val objectData: CompoundData
        get() = NBTList.data

    override val typeName: String
        get() = "list<${generic.typeName}>"

    override val nbtType: java.lang.Class<out Tag<*>>
        get() = ListTag::class.java

    override fun build(identifier: String, container: FieldContainer): Var<*> = NBTListConcrete(container, ArrayList(), identifier, generic)
    override fun build(identifier: String): Var<*> = NBTListConcrete(ArrayList(), identifier, generic)
    override fun build(identifier: String, clazz: Class): Var<*> = NBTListConcrete(clazz, ArrayList(), identifier, generic)
    override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> = NBTList(container, identifier, generic)
    override fun buildUnConcrete(identifier: String): Var<*> = NBTList(identifier, generic)
    override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = NBTList(clazz, identifier, generic)
}

class MCFPPImmutableListType(
    val generic: MCFPPType = MCFPPBaseType.Any
): MCFPPType(NBTList.data, listOf(MCFPPNBTType.NBT)){

    override val objectData: CompoundData
        get() = NBTList.data

    override val typeName: String
        get() = "list[${generic.typeName}]"

    override val nbtType: java.lang.Class<out Tag<*>>
        get() = ListTag::class.java

    override fun build(identifier: String, container: FieldContainer): Var<*> = ImmutableListConcrete(container, ListTag(IntTag::class.java), identifier, generic)
    override fun build(identifier: String): Var<*> = ImmutableListConcrete(ListTag(IntTag::class.java), identifier, generic)
    override fun build(identifier: String, clazz: Class): Var<*> = ImmutableListConcrete(clazz, ListTag(IntTag::class.java), identifier, generic)
    override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> = ImmutableList(container, identifier, generic)
    override fun buildUnConcrete(identifier: String): Var<*> = ImmutableList(identifier, generic)
    override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = ImmutableList(clazz, identifier, generic)

}

open class MCFPPCompoundType(
    val generic: MCFPPType
): MCFPPType(NBTDictionary.data, listOf(MCFPPNBTType.NBT)){

    override val typeName: String
        get() = "compound[${generic.typeName}]"

}

class MCFPPDictType(generic: MCFPPType): MCFPPCompoundType(generic){
    override val typeName: String
        get() = "dict[${generic.typeName}]"

    override fun build(identifier: String, container: FieldContainer): Var<*> = NBTDictionaryConcrete(container, HashMap(), identifier)
    override fun build(identifier: String): Var<*> = NBTDictionaryConcrete(HashMap(), identifier)
    override fun build(identifier: String, clazz: Class): Var<*> = NBTDictionaryConcrete(clazz, HashMap(), identifier)
    override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> = NBTDictionary(container, identifier)
    override fun buildUnConcrete(identifier: String): Var<*> = NBTDictionary(identifier)
    override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = NBTDictionary(clazz, identifier)
}

class MCFPPMapType(generic: MCFPPType): MCFPPCompoundType(generic){
    override val typeName: String
        get() = "map[${generic.typeName}]"


    override fun build(identifier: String, container: FieldContainer): Var<*> = NBTMapConcrete(container, HashMap(), identifier, generic)
    override fun build(identifier: String): Var<*> = NBTMapConcrete(HashMap(), identifier, generic)
    override fun build(identifier: String, clazz: Class): Var<*> = NBTMapConcrete(clazz, HashMap(), identifier, generic)
    override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> = NBTMap(container, identifier, generic)
    override fun buildUnConcrete(identifier: String): Var<*> = NBTMap(identifier, generic)
    override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = NBTMap(clazz, identifier, generic)
}
