package top.mcfpp.core.lang.resource
            
import top.mcfpp.command.Command
import top.mcfpp.command.Commands
import top.mcfpp.core.lang.Var
import top.mcfpp.type.MCFPPResourceType
import top.mcfpp.type.MCFPPType
import top.mcfpp.core.lang.MCFPPValue
import top.mcfpp.model.CompoundData
import top.mcfpp.model.FieldContainer
import java.util.*
import top.mcfpp.model.function.Function
import top.mcfpp.mni.resource.FunctionIDData
import top.mcfpp.mni.resource.FunctionIDConcreteData
import top.mcfpp.util.TempPool

open class FunctionID: ResourceID {

    override var type: MCFPPType = MCFPPResourceType.FunctionID

    /**
     * 创建一个FunctionID类型的变量。它的mc名和变量所在的域容器有关。
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
     * 创建一个FunctionID值。它的标识符和mc名相同。
     * @param identifier identifier
     */
    constructor(identifier: String = TempPool.getVarIdentify()) : super(identifier){
        isTemp = true
    }

    /**
     * 复制一个FunctionID
     * @param b 被复制的FunctionID值
     */
    constructor(b: FunctionID) : super(b)

    companion object {
        val data = CompoundData("FunctionID","mcfpp.lang.resource")

        init {
            data.initialize()
            data.extends(ResourceID.data)
            data.getNativeFromClass(FunctionIDData::class.java)
        }
    }
}

class FunctionIDConcrete: MCFPPValue<String>, FunctionID{

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

    constructor(id: FunctionID, value: String) : super(id){
        this.value = value
    }

    constructor(id: FunctionIDConcrete) : super(id){
        this.value = id.value
    }

    override fun clone(): FunctionIDConcrete {
        return FunctionIDConcrete(this)
    }

    override fun getTempVar(): FunctionIDConcrete {
        return FunctionIDConcrete(this.value)
    }

    override fun toDynamic(replace: Boolean): Var<*> {
        val parent = parent
        if (parentClass() != null) {
            val cmd = Commands.selectRun(parent!!, "data modify entity @s data.${identifier} set value $value")
            Function.addCommands(cmd)
        } else {
            val cmd = Command.build("data modify")
                .build(nbtPath.toCommandPart())
                .build("set value $value")
            Function.addCommand(cmd)
        }
        val re = FunctionID(this)
        if(replace){
            Function.currFunction.field.putVar(identifier, re, true)
        }
        return re
    }

    override fun toString(): String {
        return "[$type,value=$value]"
    }
    
    companion object {
        val data = CompoundData("FunctionID","mcfpp.lang.resource")

        init {
            data.initialize()
            data.extends(ResourceID.data)
            data.getNativeFromClass(FunctionIDConcreteData::class.java)
        }
    }
    
}        
