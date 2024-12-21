package top.mcfpp.core.lang.nbt

import top.mcfpp.core.lang.MCInt
import top.mcfpp.core.lang.PropertyVar
import top.mcfpp.core.lang.UnknownVar
import top.mcfpp.core.lang.Var
import top.mcfpp.model.accessor.Property
import top.mcfpp.type.MCFPPType
import top.mcfpp.util.LogProcessor
import java.util.*

abstract class NBTArray: NBTBasedData {

    abstract val arrayType: MCFPPType

    constructor(identifier: String = UUID.randomUUID().toString()) : super(identifier)

    constructor(b: NBTArray) : super(b)

    override fun getByIndex(index: Var<*>): PropertyVar{
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