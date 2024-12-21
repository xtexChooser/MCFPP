package top.mcfpp.core.lang.nbt

import net.querz.nbt.tag.LongArrayTag
import top.mcfpp.core.lang.*
import top.mcfpp.model.accessor.Property
import top.mcfpp.type.MCFPPNBTType
import top.mcfpp.type.MCFPPType
import top.mcfpp.util.LogProcessor
import java.util.*

open class LongArray: NBTArray {

    override var type: MCFPPType = MCFPPNBTType.LongArray

    override val arrayType: MCFPPType = MCFPPNBTType.Long

    constructor(identifier: String = UUID.randomUUID().toString()) : super(identifier)

    constructor(b: NBTArray) : super(b)
}

class LongArrayConcrete: LongArray, MCFPPValue<LongArrayTag>{

    override var value: LongArrayTag

    constructor(value: LongArrayTag, identifier: String = UUID.randomUUID().toString()) : super(identifier){
        this.value = value
    }

    constructor(b: NBTArray, value: LongArrayTag) : super(b){
        this.value = value
    }

    override fun toDynamic(replace: Boolean): Var<*> {
        NBTBasedDataConcrete(this, value).toDynamic(replace)
        return LongArray(this)
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