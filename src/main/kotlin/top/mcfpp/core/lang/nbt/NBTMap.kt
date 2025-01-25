@file:Suppress("LeakingThis")

package top.mcfpp.core.lang.nbt

import net.querz.nbt.tag.StringTag
import top.mcfpp.core.lang.MCAny
import top.mcfpp.core.lang.MCFPPValue
import top.mcfpp.core.lang.Var
import top.mcfpp.mni.NBTMapData
import top.mcfpp.model.CompoundData
import top.mcfpp.type.MCFPPBaseType
import top.mcfpp.type.MCFPPMapType
import top.mcfpp.type.MCFPPType
import top.mcfpp.util.TempPool

open class NBTMap : NBTDictionary {

    var keyList : NBTList
    var valueList : NBTList
    var keyValueSet: NBTDictionary

    val genericType: MCFPPType

    /**
     * 创建一个map值。它的标识符和mc名相同。
     * @param identifier identifier
     */
    constructor(identifier: String = TempPool.getVarIdentify(), genericType : MCFPPType) : super(identifier){
        this.genericType = genericType
        keyList = NBTList(identifier + "_key", MCFPPBaseType.String)
        valueList = NBTList(identifier + "_value", genericType)
        keyValueSet = NBTDictionary(identifier + "_keyValueSet")
        keyList.nbtPath = nbtPath.memberIndex("key")
        valueList.nbtPath = nbtPath.memberIndex("value")
        keyValueSet.nbtPath = nbtPath.memberIndex("keyValueSet")
        type = MCFPPMapType(genericType)
    }

    constructor(b: NBTMap): super(b){
        this.keyList = b.keyList.clone() as NBTList
        this.valueList = b.valueList.clone() as NBTList
        this.keyValueSet = b.keyValueSet.clone() as NBTDictionary
        this.genericType = b.genericType
        type = MCFPPMapType(genericType)
    }

    override fun doAssignedBy(b: Var<*>): NBTMap {
        return super.assignedBy(b) as NBTMap
    }

    companion object{
        val data = CompoundData("map","mcfpp.lang")

        init {
            data.initialize()
            data.extends(MCAny.data)
            data.getNativeFromClass(NBTMapData::class.java)
        }
    }
}

class NBTMapConcrete : NBTMap, MCFPPValue<HashMap<String, Var<*>>> {

    override var value: HashMap<String, Var<*>>

    /**
     * 创建一个固定的map。它的标识符和mc名一致
     * @param identifier 标识符。如不指定，则为随机uuid
     * @param value 值
     */
    constructor(value: HashMap<String, Var<*>>, identifier: String = TempPool.getVarIdentify(), genericType: MCFPPType) : super(identifier, genericType){
        this.value = value
        keyList = NBTListConcrete(ArrayList(value.keys.map { MCStringConcrete(StringTag((it))) }), identifier + "_key", MCFPPBaseType.String)
        valueList = NBTListConcrete(ArrayList(value.values), identifier + "_value", genericType)
        keyValueSet = NBTDictionaryConcrete(value, identifier + "_keyValueSet")
    }

    /**
     * 复制一个map
     * @param b 被复制的map值
     */
    constructor(b: NBTMap, value: HashMap<String, Var<*>>) : super(b){
        this.value = value
        keyList = NBTListConcrete(ArrayList(value.keys.map { MCStringConcrete(StringTag((it))) }), identifier + "_key", MCFPPBaseType.String)
        valueList = NBTListConcrete(ArrayList(value.values), identifier + "_value", genericType)
        keyValueSet = NBTDictionaryConcrete(value, identifier + "_keyValueSet")
    }

    constructor(v: NBTMapConcrete) : super(v){
        this.value = v.value
        keyList = v.keyList.clone() as NBTListConcrete
        valueList = v.valueList.clone() as NBTListConcrete
        keyValueSet = v.keyValueSet.clone() as NBTDictionaryConcrete
    }

    override fun clone(): NBTMapConcrete {
        return NBTMapConcrete(this)
    }

    override fun toDynamic(replace: Boolean): Var<*> {
        TODO("Not yet implemented")
    }

    fun indexOf(key: String): Int{
        return (keyList as NBTListConcrete).value.indexOfFirst { (it as MCStringConcrete).value.value == key }
    }
}