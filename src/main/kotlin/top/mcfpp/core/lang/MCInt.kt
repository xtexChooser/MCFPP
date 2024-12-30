package top.mcfpp.core.lang

import top.mcfpp.annotations.InsertCommand
import top.mcfpp.command.Command
import top.mcfpp.command.Commands
import top.mcfpp.core.lang.bool.CommandBoolPart
import top.mcfpp.core.lang.bool.ExecuteBool
import top.mcfpp.core.lang.bool.ScoreBoolConcrete
import top.mcfpp.core.lang.nbt.MCLong
import top.mcfpp.exception.VariableConverseException
import top.mcfpp.mni.MCIntData
import top.mcfpp.model.CompoundData
import top.mcfpp.model.FieldContainer
import top.mcfpp.model.Member
import top.mcfpp.model.function.Function
import top.mcfpp.type.MCFPPBaseType
import top.mcfpp.type.MCFPPNBTType
import top.mcfpp.type.MCFPPType
import top.mcfpp.util.LogProcessor
import top.mcfpp.util.TextTranslator
import top.mcfpp.util.TextTranslator.translate
import java.util.*

/**
 * 代表了mc中的一个整数。实质上是记分板中的一个记分项。你可以对它进行加减乘除等基本运算操作，以及大小比较等逻辑运算。
 */
open class MCInt : MCNumber<Int> {

    constructor(curr: FieldContainer, identifier: String = UUID.randomUUID().toString()) : super(curr, identifier)

    constructor(identifier: String = UUID.randomUUID().toString()) : super(identifier)

    constructor(b: MCInt) : super(b)

    constructor(b: EnumVar): super(b)

    override var type: MCFPPType = MCFPPBaseType.Int

    override fun doAssignedBy(b: Var<*>) : MCInt {
        return when (b) {
            is MCInt -> {
                assignCommand(b)
            }

            is CommandReturn -> {
                if(parentClass() != null){
                    Function.addCommands(
                        Commands.selectRun(parent!!, b.command, false)
                    )
                }else{
                    Function.addCommand(b.command)
                }
                MCInt(this)
            }

            else -> {
                LogProcessor.error(TextTranslator.ASSIGN_ERROR.translate(b.type.typeName, type.typeName))
                this
            }
        }
    }

    override fun canAssignedBy(b: Var<*>): Boolean {
        if(!b.implicitCast(type).isError) return true
        return when(b){
            is CommandReturn -> true
            else -> false
        }
    }

    override fun explicitCast(type: MCFPPType): Var<*> {
        val re = super.explicitCast(type)
        if(!re.isError) return re
        //TODO 类支持
        return when (type) {
            MCFPPBaseType.Float -> {
                MCInt("inp").assignedBy(this)
                Function.addCommand("function math:hpo/float/_scoreto")
                return MCFloat().assignedBy(MCFloat.ssObj)
            }
            MCFPPNBTType.Long -> {
                storeToStack()
                val ret = MCLong()
                Function.addCommand(Commands.dataSetFrom(ret.nbtPath, nbtPath))
                ret
            }
            else -> re
        }
    }

    override fun implicitCast(type: MCFPPType): Var<*> {
        val re = super.implicitCast(type)
        if(!re.isError) return re
        //TODO 类支持
        return when (type) {
            MCFPPBaseType.Float -> {
                MCInt("inp").assignedBy(this)
                Function.addCommand("function math:hpo/float/_scoreto")
                return MCFloat().assignedBy(MCFloat.ssObj)
            }
            else -> re
        }
    }

    //this = a
    @InsertCommand
    override fun assignCommand(a: MCNumber<*>) : MCInt {
        return assignCommandLambda(a,
            ifThisIsClassMemberAndAIsConcrete =  { b, final ->
                //对类中的成员的值进行修改
                if(final.size == 2){
                    Function.addCommand(final[0])
                }
                Function.addCommand(final.last().build(Commands.sbPlayerSet(this, (b as MCIntConcrete).value)))
                this
            },
            ifThisIsClassMemberAndAIsNotConcrete = { b, final ->
                //对类中的成员的值进行修改
                if(final.size == 2){
                    Function.addCommand(final[0])
                }
                Function.addCommand(final.last().build(Commands.sbPlayerOperation(this,"=",b as MCInt)))
                this
            },
            ifThisIsNormalVarAndAIsConcrete = { b, _ ->
                MCIntConcrete(this, (b as MCIntConcrete).value)
            },
            ifThisIsNormalVarAndAIsClassMember = { c, cmd ->
                if(cmd.size == 2){
                    Function.addCommand(cmd[0])
                }
                Function.addCommand(cmd.last().build(Commands.sbPlayerOperation(this, "=", c as MCInt)))
                MCInt(this)
            },
            ifThisIsNormalVarAndAIsNotConcrete = { c, _ ->
                //变量进栈
                Function.addCommand(Commands.sbPlayerOperation(this, "=", c as MCInt))
                MCInt(this)
            }
        ) as MCInt
    }

    @InsertCommand
    override fun plus(a: Var<*>): Var<*>? {
        if(!isTemp && a.isTemp){
            return a.plus(this)
        }else if(!isTemp){
            return getTempVar().plus(a)
        }
        when(a){
            is MCIntConcrete -> {
                Function.addCommand(Commands.sbPlayerAdd(this, a.value))
                return this
            }
            is MCInt -> {
                Function.addCommand(Commands.sbPlayerOperation(this, "+=", a))
                return this
            }
            else -> return null
        }
    }

    @InsertCommand
    override fun minus(a: Var<*>): Var<*>? {
        if(!isTemp && a.isTemp){
            return a.minus(this)
        }else if(!isTemp){
            return getTempVar().minus(a)
        }
        when(a){
            is MCIntConcrete -> {
                Function.addCommand(Commands.sbPlayerRemove(this, a.value))
                return this
            }
            is MCInt -> {
                Function.addCommand(Commands.sbPlayerOperation(this, "-=", a))
                return this
            }
            else -> return null
        }
    }

    @InsertCommand
    override fun multiple(a: Var<*>): Var<*>? {
        //t *= a
        if(!isTemp && a.isTemp){
            return a.multiple(this)
        }else if(!isTemp){
            return getTempVar().multiple(a)
        }
        when(a){
            is MCIntConcrete -> {
                Function.addCommand(Commands.sbPlayerSet(a, a.value))
                Function.addCommand(Commands.sbPlayerOperation(this, "*=", a))
                return this
            }
            is MCInt -> {
                Function.addCommand(Commands.sbPlayerOperation(this, "*=", a))
                return this
            }
            else -> return null
        }
    }

    @InsertCommand
    override fun divide(a: Var<*>): Var<*>? {
        //t /= a
        if(!isTemp && a.isTemp){
            return a.divide(this)
        }else if(!isTemp){
            return getTempVar().divide(a)
        }
        when(a){
            is MCIntConcrete -> {
                Function.addCommand(Commands.sbPlayerSet(a, a.value))
                Function.addCommand(Commands.sbPlayerOperation(this, "/=", a))
                return this
            }
            is MCInt -> {
                Function.addCommand(Commands.sbPlayerOperation(this, "/=", a))
                return this
            }
            else -> return null
        }
    }

    @InsertCommand
    override fun modular(a: Var<*>): Var<*>? {
        //t %= a
        if(!isTemp && a.isTemp){
            return a.modular(this)
        }else if(!isTemp){
            return getTempVar().modular(a)
        }
        when(a){
            is MCIntConcrete -> {
                Function.addCommand(Commands.sbPlayerSet(a, a.value))
                Function.addCommand(Commands.sbPlayerOperation(this, "%=", a))
                return this
            }
            is MCInt -> {
                Function.addCommand(Commands.sbPlayerOperation(this, "%=", a))
                return this
            }
            else -> return null
        }
    }

    @InsertCommand
    override fun isBigger(a: Var<*>): Var<*>? {
        //re = t > a
        if (a !is MCInt) return null
        val re = ExecuteBool()
        if (a is MCIntConcrete) {
            //execute store success score qwq qwq if score qwq qwq matches a+1..
            re.value.add(CommandBoolPart(false, Command("if score $name $sbObject matches ${a.value + 1}..")))
        } else {
            re.value.add(CommandBoolPart(false, Command("if score $name $sbObject > ${a.name} ${a.sbObject}")))
        }
        re.isTemp = true
        return re
    }

    @InsertCommand
    override fun isSmaller(a: Var<*>): Var<*>? {
        //re = t < a
        if (a !is MCInt) return null
        val re = ExecuteBool()
        if (a is MCIntConcrete) {
            //execute store success score qwq qwq if score qwq qwq matches a+1..
            re.value.add(CommandBoolPart(false, Command("if score $name $sbObject matches ..${a.value - 1}")))
        } else {
            re.value.add(CommandBoolPart(false, Command("if score $name $sbObject < ${a.name} ${a.sbObject}")))
        }
        re.isTemp = true
        return re
    }

    @InsertCommand
    override fun isSmallerOrEqual(a: Var<*>): Var<*>? {
        //re = t <= a
        if (a !is MCInt) return null
        val re = ExecuteBool()
        if (a is MCIntConcrete) {
            //execute store success score qwq qwq if score qwq qwq matches a+1..
            re.value.add(CommandBoolPart(false, Command("if score $name $sbObject matches ..${a.value}")))
        } else {
            re.value.add(CommandBoolPart(false, Command("if score $name $sbObject <= ${a.name} ${a.sbObject}")))
        }
        re.isTemp = true
        return re
    }

    @InsertCommand
    override fun isBiggerOrEqual(a: Var<*>): Var<*>? {
        //re = t <= a
        if (a !is MCInt) return null
        val re = ExecuteBool()
        if (a is MCIntConcrete) {
            //execute store success score qwq qwq if score qwq qwq matches a+1..
            re.value.add(CommandBoolPart(false, Command("if score $name $sbObject matches ${a.value}..")))
        } else {
            re.value.add(CommandBoolPart(false, Command("if score $name $sbObject >= ${a.name} ${a.sbObject}")))
        }
        re.isTemp = true
        return re
    }

    @InsertCommand
    override fun isEqual(a: Var<*>): Var<*>? {
        //re = t == a
        if (a !is MCInt) return null
        val re = ExecuteBool()
        if (a is MCIntConcrete) {
            //execute store success score qwq qwq if score qwq qwq = owo owo
            re.value.add(CommandBoolPart(false, Command("if score $name $sbObject matches ${a.value}")))
        } else {
            re.value.add(CommandBoolPart(false, Command("if score $name $sbObject = ${a.name} ${a.sbObject}")))
        }
        re.isTemp = true
        return re
    }

    @InsertCommand
    override fun isNotEqual(a: Var<*>): Var<*>? {
        //re = t != a
        if (a !is MCInt) return null
        val re = ExecuteBool()
        if(a is MCIntConcrete){
            //execute store success score qwq qwq if score qwq qwq matches owo owo
            re.value.add(CommandBoolPart(false, Command("unless score $name $sbObject matches ${a.value}")))
        }else{
            re.value.add(CommandBoolPart(false, Command("unless score $name $sbObject = ${a.name} ${a.sbObject}")))
        }
        re.isTemp = true
        return re
    }

    override fun clone(): MCInt {
        return MCInt(this)
    }

    /**
     * 获取临时变量
     *
     * @return 返回临时变量
     */
    @InsertCommand
    override fun getTempVar(): MCInt {
        if (isTemp) return this
        val re = MCInt()
        re.isTemp = true
        return re.assignedBy(this) as MCInt
    }

    override fun storeToStack() {
        if(parentClass() != null || hasStoredInStack) return
        Function.addCommand(Command("execute store result")
            .build(nbtPath.toCommandPart())
            .build("int 1 run scoreboard players get $name $sbObject"))
        hasStoredInStack = true
    }

    override fun getFromStack() {
        if(parent != null) return
        Function.addCommand(
            Command("execute store result score $name $sbObject run data get")
                .build(nbtPath.toCommandPart())
        )
    }

    /**
     * 根据标识符获取一个成员。
     *
     * @param key 成员的mcfpp标识符
     * @param accessModifier 访问者的访问权限
     * @return 返回一个值对。第一个值是成员变量或null（如果成员变量不存在），第二个值是访问者是否能够访问此变量。
     */
    override fun getMemberVar(key: String, accessModifier: Member.AccessModifier): Pair<Var<*>?, Boolean> {
        return data.getVar(key) to true
    }

    /**
     * 根据方法标识符和方法的参数列表获取一个方法。如果没有这个方法，则返回null
     *
     * @param key 成员方法的标识符
     * @param normalArgs 成员方法的参数
     * @return 返回一个值对。第一个值是成员变量或null（如果成员方法不存在），第二个值是访问者是否能够访问此变量。
     */
    override fun getMemberFunction(
        key: String,
        readOnlyArgs: List<Var<*>>,
        normalArgs: List<Var<*>>,
        accessModifier: Member.AccessModifier
    ): Pair<Function, Boolean> {
        return data.getFunction(key, readOnlyArgs, normalArgs) to true
    }

    companion object {
        val data by lazy {
            CompoundData("int","mcfpp").apply {
                extends(MCAny.data)
                getNativeFromClass(MCIntData::class.java)
            }
        }
    }
}

class MCIntConcrete : MCInt, MCFPPValue<Int> {

    override var value: Int = 0

    /**
     * 创建一个固定的int
     *
     * @param identifier 标识符
     * @param curr 域容器
     * @param value 值
     */
    constructor(
        curr: FieldContainer,
        value: Int,
        identifier: String = UUID.randomUUID().toString()
    ) : super(curr, identifier) {
        this.value = value
    }

    /**
     * 创建一个固定的int。它的标识符和mc名一致/
     * @param identifier 标识符。如不指定，则为随机uuid
     * @param value 值
     */
    constructor(value: Int, identifier: String = UUID.randomUUID().toString()) : super(identifier) {
        this.value = value
    }

    constructor(int: MCInt, value: Int) : super(int){
        this.value = value
    }

    constructor(int: MCIntConcrete) : super(int){
        this.value = int.value
    }

    constructor(enum: EnumVarConcrete) : super(enum){
        this.value = enum.value.value
    }

    override fun clone(): MCIntConcrete {
        return MCIntConcrete(this)
    }

    override fun storeToStack() {}

    override fun getFromStack() {}

    /**
     * 动态化
     *
     */
    override fun toDynamic(replace: Boolean): Var<*> {
        //避免错误 Smart cast to 'ClassPointer' is impossible, because 'parent' is a mutable property that could have been changed by this time
        val parent = parent

        if (parentClass() != null) {
            val cmd = Commands.selectRun(parent!!, "scoreboard players set @s $sbObject $value")
            Function.addCommands(cmd)
        } else {
            Function.addCommand("scoreboard players set $name $sbObject $value")
        }
        val re = MCInt(this)
        if(replace){
            if(parentTemplate() != null){
                (parent as DataTemplateObject).instanceField.putVar(identifier, re, true)
            }else{
                Function.currFunction.field.putVar(identifier, re, true)
            }
        }
        return re
    }

    @Override
    override fun explicitCast(type: MCFPPType): Var<*> {
        //TODO 类支持
        return when (type) {
            this.type -> this
            MCFPPBaseType.Float -> MCFloatConcrete(value = value.toFloat())
            MCFPPBaseType.Any -> this
            else -> {
                LogProcessor.error("Cannot cast [${this.type}] to [$type]")
                throw VariableConverseException()
            }
        }
    }

    @InsertCommand
    override fun plus(a: Var<*>): Var<*>? {
        //t = t + a
        when(a){
            is MCIntConcrete -> {
                value += a.value
                return this
            }
            is MCInt -> {
                return a.plus(this)
            }
            else -> return null
        }
    }

    @InsertCommand
    override fun minus(a: Var<*>): Var<*>? {
        //t = t + a
        when(a){
            is MCIntConcrete -> {
                value -= a.value
                return this
            }
            is MCInt -> {
                return a.minus(this)
            }
            else -> return this
        }
    }


    @Override
    @InsertCommand
    override fun multiple(a: Var<*>): Var<*>? {
        //t = t * a
        when(a){
            is MCIntConcrete -> {
                value *= a.value
                return this
            }
            is MCInt -> {
                return a.multiple(this)
            }
            else -> return this
        }
    }

    @Override
    @InsertCommand
    override fun divide(a: Var<*>): Var<*>? {
        //t = t / a
        when(a){
            is MCIntConcrete -> {
                value /= a.value
                return this
            }
            is MCInt -> {
                return a.divide(this)
            }
            else -> return this
        }
    }

    @Override
    @InsertCommand
    override fun modular(a: Var<*>): Var<*>? {
        //t = t % a
        when(a){
            is MCIntConcrete -> {
                value %= a.value
                return this
            }
            is MCInt -> {
                return a.modular(this)
            }
            else -> return this
        }
    }

    @Override
    @InsertCommand
    override fun isBigger(a: Var<*>): Var<*>? {
        //re = t > a
        if (a !is MCInt) return null
        return if (a is MCIntConcrete) {
            ScoreBoolConcrete(value > a.value)
        } else {
            //注意大小于换符号！
            a.isSmaller(this)
        }
    }

    @Override
    @InsertCommand
    override fun isSmaller(a: Var<*>): Var<*>? {
        //re = t < a
        if (a !is MCInt) return null
        return if (a is MCIntConcrete) {
            ScoreBoolConcrete(value < a.value)
        } else {
            a.isBigger(this)
        }
    }

    @Override
    @InsertCommand
    override fun isSmallerOrEqual(a: Var<*>): Var<*>? {
        //re = t <= a
        if (a !is MCInt) return null
        return if (a is MCIntConcrete) {
            ScoreBoolConcrete(value <= a.value)
        } else {
            a.isBiggerOrEqual(this)
        }
    }

    @Override
    @InsertCommand
    override fun isBiggerOrEqual(a: Var<*>): Var<*>? {
        //re = t <= a
        if (a !is MCInt) return null
        return if (a is MCIntConcrete) {
            ScoreBoolConcrete(value >= a.value)
        } else {
            a.isSmallerOrEqual(this)
        }
    }

    @Override
    @InsertCommand
    override fun isEqual(a: Var<*>): Var<*>? {
        //re = t == a
        if (a !is MCInt) return null
        return if (a is MCIntConcrete) {
            ScoreBoolConcrete(value == a.value)
        } else {
            a.isEqual(this)
        }
    }

    @Override
    @InsertCommand
    override fun isNotEqual(a: Var<*>): Var<*>? {
        //re = t != a
        if (a !is MCInt) return null
        return if (a is MCIntConcrete) {
            ScoreBoolConcrete(value != a.value)
        } else {
            a.isNotEqual(this)
        }
    }

    /**
     * 获取临时变量
     *
     * @return 返回临时变量
     */
    @Override
    @InsertCommand
    override fun getTempVar(): MCIntConcrete {
        if (isTemp) return this
        return MCIntConcrete(value)
    }
}