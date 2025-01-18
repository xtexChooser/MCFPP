package top.mcfpp.model.function

import top.mcfpp.annotations.InsertCommand
import top.mcfpp.antlr.mcfppParser
import top.mcfpp.command.Command
import top.mcfpp.command.Commands
import top.mcfpp.core.lang.*
import top.mcfpp.model.Class
import top.mcfpp.model.CompoundData
import top.mcfpp.type.MCFPPType
import java.util.*

/**
 * 一个构造函数。它是一个特殊的成员方法，将会在类的初始化阶段之后调用。
 */
open class ClassConstructor
    (
    /**
     * 此构造函数对应的类。
     */
    var target: Class
) : Function("_init_" + target.identifier.lowercase(Locale.getDefault()) + "_" + target.constructors.size, target, false, context = null) {

    private val leadFunction: Function
    init {
        //添加this指针
        val thisObj = ClassPointer(target,"this")
        thisObj.identifier = "this"
        field.putVar("this",thisObj)
        leadFunction = Function(this.identifier + "_lead",this.namespace, context = null)
        target.field.addFunction(leadFunction,false)
        leadFunction.runInFunction {
            //获取所有函数
            val funcs = StringBuilder("functions:{")
            target.field.forEachFunction { f ->
                run {
                    funcs.append("${f.identifier}:\"${f.namespaceID}\",")
                }
            }
            funcs.append("}")
            //对象实体创建
            when (target.baseEntity) {
                Class.ENTITY_MARKER -> {
                    addCommand("data merge entity @s {Tags:[${target.tag},${target.tag}_data,mcfpp_ptr,just],data:{$funcs}}")
                }
                Class.ENTITY_ITEM_DISPLAY -> {
                    addCommand("data modify entity @s item.components.\"minecraft:custom_data\".mcfppData set value {Tags:[${target.tag},${target.tag}_data,mcfpp_ptr,just],data:{$funcs}}")
                }
                else -> {
                    addCommand("tag @s add ${target.tag}")
                    addCommand("summon marker ~ ~ ~ {Tags:[${target.tag}_data,mcfpp_ptr,just],data:{$funcs}}")
                    addCommand("ride @n[tag=just, type=marker] mount @s")
                    addCommand("tag @n[tag=just, type=marker] remove just")
                }
            }
            //初始指针
            addCommand(Command("data modify").build(Class.tempPtr.toCommandPart()).build("set from entity @s UUID"))
            //初始化
            if(target.classPreInit.commands.size > 0){
                //给函数开栈
                addCommand(Commands.stackIn())
                //不应当立即调用它自己的函数，应当先调用init，再调用constructor
                addCommand(Commands.function(target.classPreInit))
                //调用完毕，将子函数的栈销毁
                addCommand(Commands.stackOut())
            }
            //给函数开栈，调用构造函数
            addCommand(Commands.stackIn())
            //调用构造函数
            addCommand(Commands.function(this))
            //销毁指针，释放堆内存
            for (p in field.allVars){
                if (p is ClassPointer){
                    p.dispose()
                }
            }
            //调用完毕，将子函数的栈销毁
            addCommand(Commands.stackOut())
        }
    }

    /**
     * 调用构造函数。类的实例的实体的生成，类的初始化（preinit和init函数），自身的调用和地址分配都在此方法进行。
     * @param args 函数的参数
     * @param callerClassP 构造方法将要构建的对象的临时指针
     */
    @Override
    @InsertCommand
    override fun invoke(normalArgs: ArrayList<Var<*>>, callerClassP: ClassPointer) {
        //参数传递
        argPass(normalArgs)
        addCommand("execute in minecraft:overworld summon ${target.baseEntity} run function " + leadFunction.namespaceID)
        //取出栈内的值
        fieldRestore()
    }

    fun addParamsFromContext(ctx: mcfppParser.NormalParamsContext) {
        val n = ctx.parameterList()?:return
        for (param in n.parameter()) {
            val (p,v) = parseParam(param)
            normalParams.add(p)
            field.putVar(p.identifier, v)
        }
    }

    @get:Override
    override val prefix: String
        get() = namespace + "_class_" + target.identifier + "_init_"

    @Override
    override fun equals(other: Any?): Boolean {
        if (other is ClassConstructor) {
            if (other.target == target) {
                if (other.normalParams.size == normalParams.size) {
                    for (i in 0 until other.normalParams.size) {
                        if (other.normalParams[i] != normalParams[i]) {
                            return false
                        }
                    }
                    return true
                }
            }
        }
        return false
    }

    override fun hashCode(): Int {
        return target.hashCode()
    }

    fun isSelf(d: CompoundData, normalParams: List<MCFPPType>) : Boolean{
        if (this.target == d && this.normalParams.size == normalParams.size) {
            if (this.normalParams.size == 0) {
                return true
            }
            var hasFoundFunc = true
            //参数比对
            for (i in normalParams.indices) {
                if (!FunctionParam.isSubOf(normalParams[i],this.normalParams[i].type)) {
                    hasFoundFunc = false
                    break
                }
            }
            return hasFoundFunc
        }else{
            return false
        }
    }
}