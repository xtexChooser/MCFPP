package top.mcfpp.core.lang.resource
            
import net.querz.nbt.tag.StringTag
import top.mcfpp.core.lang.MCFPPValue
import top.mcfpp.core.lang.Var
import top.mcfpp.core.lang.nbt.NBTBasedDataConcrete
import top.mcfpp.mni.resource.AdvancementConcreteData
import top.mcfpp.mni.resource.AdvancementData
import top.mcfpp.model.CompoundData
import top.mcfpp.model.FieldContainer
import top.mcfpp.type.MCFPPResourceType
import top.mcfpp.type.MCFPPType
import top.mcfpp.util.TempPool

open class Advancement: ResourceID {

    override var type: MCFPPType = MCFPPResourceType.Advancement

    /**
     * 创建一个Advancement类型的变量。它的mc名和变量所在的域容器有关。
     *
     * @param identifier 标识符。默认为
     */
    constructor(
        curr: FieldContainer,
        identifier: String = TempPool.getVarIdentify()
    ) : super(curr, identifier) {
        this.identifier = identifier
    }

    /**
     * 创建一个Advancement值。它的标识符和mc名相同。
     * @param identifier identifier
     */
    constructor(identifier: String = TempPool.getVarIdentify()) : super(identifier){
        isTemp = true
    }

    /**
     * 复制一个Advancement
     * @param b 被复制的Advancement值
     */
    constructor(b: Advancement) : super(b)

    companion object {
        val data = CompoundData("Advancement","mcfpp.lang.resource")

        init {
            data.initialize()
            data.extends(ResourceID.data)
            data.getNativeFromClass(AdvancementData::class.java)
        }
    }
}

class AdvancementConcrete: MCFPPValue<String>, Advancement{

    override var value: String

    constructor(
        curr: FieldContainer,
        value: String,
        identifier: String = TempPool.getVarIdentify()
    ) : super(curr, identifier) {
        this.value = value
    }

    constructor(value: String, identifier: String = TempPool.getVarIdentify()) : super(identifier) {
        this.value = value
    }

    constructor(id: Advancement, value: String) : super(id){
        this.value = value
    }

    constructor(id: AdvancementConcrete) : super(id){
        this.value = id.value
    }

    override fun clone(): AdvancementConcrete {
        return AdvancementConcrete(this)
    }

    override fun getTempVar(): AdvancementConcrete {
        return AdvancementConcrete(this.value)
    }

    override fun toDynamic(replace: Boolean): Var<*> {
        NBTBasedDataConcrete(this, StringTag(value)).toDynamic(replace)
        return Advancement(this)
    }

    override fun toString(): String {
        return "[$type,value=$value]"
    }
    
    companion object {
        val data = CompoundData("Advancement","mcfpp.lang.resource")

        init {
            data.initialize()
            data.extends(ResourceID.data)
            data.getNativeFromClass(AdvancementConcreteData::class.java)
        }
    }
    
}        
