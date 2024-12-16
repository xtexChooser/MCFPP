package top.mcfpp.core.lang.nbt

import net.querz.nbt.tag.ByteArrayTag
import top.mcfpp.core.lang.*
import top.mcfpp.model.FieldContainer
import top.mcfpp.model.accessor.Property
import top.mcfpp.type.MCFPPNBTType
import top.mcfpp.type.MCFPPType
import top.mcfpp.util.LogProcessor
import java.util.*

open class ByteArray: NBTArray {

    override var type: MCFPPType = MCFPPNBTType.ByteArray

    override val arrayType: MCFPPType = MCFPPNBTType.Byte

    constructor(curr: FieldContainer, identifier: String = UUID.randomUUID().toString()) : super(curr, identifier)

    constructor(identifier: String = UUID.randomUUID().toString()) : super(identifier)

    constructor(b: NBTArray) : super(b)
}

class ByteArrayConcrete: ByteArray, MCFPPValue<ByteArrayTag>{

    override var value: ByteArrayTag

    constructor(curr: FieldContainer, value: ByteArrayTag, identifier: String = UUID.randomUUID().toString()) : super(curr, identifier){
        this.value = value
    }

    constructor(value: ByteArrayTag, identifier: String = UUID.randomUUID().toString()) : super(identifier){
        this.value = value
    }

    constructor(b: NBTArray, value: ByteArrayTag) : super(b){
        this.value = value
    }

    override fun toDynamic(replace: Boolean): Var<*> {
        NBTBasedDataConcrete(this, value).toDynamic(replace)
        return ByteArray(this)
    }

    override fun getByIndex(index: Var<*>): PropertyVar {
        if(index is MCInt){
            val v = arrayType.build(UUID.randomUUID().toString())
            v.nbtPath = nbtPath.intIndex(index)
            v.parent = this
            return PropertyVar(Property.buildSimpleProperty(v), v,this)
        }else{
            LogProcessor.error("Index must be a int")
            val re = UnknownVar("error_${identifier}_index_${index.identifier}")
            return PropertyVar(Property.buildSimpleProperty(re), re, this)
        }
    }

}