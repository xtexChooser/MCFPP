package top.mcfpp.model.generic

import top.mcfpp.Project
import top.mcfpp.antlr.MCFPPImVisitor
import top.mcfpp.antlr.mcfppParser
import top.mcfpp.core.lang.MCFPPValue
import top.mcfpp.core.lang.Var
import top.mcfpp.model.CanSelectMember
import top.mcfpp.model.CompoundData
import top.mcfpp.model.function.ExtensionFunction
import top.mcfpp.model.function.Function
import top.mcfpp.model.function.FunctionParam
import top.mcfpp.util.LogProcessor

class GenericExtensionFunction: ExtensionFunction, Generic<ExtensionFunction> {

    override val readOnlyParams: ArrayList<FunctionParam> = ArrayList()

    override val compiledFunctions: HashMap<List<Any?>, Function> = HashMap()

    /**
     * 创建一个函数
     * @param name 函数的标识符
     */
    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(name: String, owner: CompoundData, namespace: String = Project.currNamespace, ctx: mcfppParser.FunctionBodyContext):super(name, owner, namespace, ctx)

    override fun invoke(readOnlyArgs: ArrayList<Var<*>>, normalArgs: ArrayList<Var<*>>, caller: CanSelectMember?): Var<*> {
        return compile(readOnlyArgs).invoke(normalArgs, caller)
    }

    override fun addParamsFromContext(ctx: mcfppParser.FunctionParamsContext) {
        val r = ctx.readOnlyParams().parameterList()
        val n = ctx.normalParams().parameterList()
        if(r == null && n == null) return
        for (param in r.parameter()){
            val (p,v) = parseParam(param)
            readOnlyParams.add(p)
            if(v !is MCFPPValue<*>){
                LogProcessor.error("ReadOnly params must have a concrete value")
                throw Exception()
            }
            field.putVar(p.identifier, v)
        }
        hasDefaultValue = false
        for (param in n.parameter()) {
            var (p,v) = parseParam(param)
            normalParams.add(p)
            if(v is MCFPPValue<*>) v = v.toDynamic(false)
            field.putVar(p.identifier, v)
        }
    }

    override fun compile(args: List<Var<*>>): Function{
        //函数参数已知条件下的编译
        val readOnlyArgs = args.subList(0, readOnlyParams.size)
        val readOnlyValues = readOnlyArgs.map { (it as MCFPPValue<*>).value }
        val normalArgs = args.subList(readOnlyArgs.size, args.size)
        val normalValues = normalArgs.map { if (it is MCFPPValue<*>) it.value else null }
        val values = readOnlyValues + normalValues
        compiledFunctions[values]?.let { return it }
        val cf = Function(this)
        //去除原来的function在编译的时候添加的变量
        for (v in ArrayList(cf.field.allVars).subList(cf.normalParams.size, cf.field.allVars.size)) {
            cf.field.removeVar(v.identifier)
        }
        //替换变量
        for (i in readOnlyValues.indices){
            cf.field.putVar(
                readOnlyParams[i].identifier,
                cf.field.getVar(readOnlyParams[i].identifier)!!.assignedBy(readOnlyArgs[i]),
                true
            )
        }
        for (i in normalValues.indices) {
            if (normalValues[i] != null) {
                cf.field.putVar(
                    normalParams[i].identifier,
                    cf.field.getVar(normalParams[i].identifier)!!.assignedBy(normalArgs[i]),
                    true
                )
            }
        }
        //去除确定的参数
        val params = ArrayList<FunctionParam>()
        for (i in normalArgs.indices) {
            if (normalArgs[i] !is MCFPPValue<*>) {
                params.add(normalParams[i])
            }else{
                cf.excludedArgIndex.add(i.toByte())
            }
        }
        cf.normalParams = params
        cf.commands.clear()
        cf.identifier = this.identifier + "_" + compiledFunctions.size
        compiledFunctions[values] = cf
        cf.ast = null
        cf.runInFunction {
            val qwq = buildString {
                for ((index, np) in normalParams.withIndex()) {
                    append("${np.typeIdentifier} ${np.identifier} = ${values[index]}, ")
                }
            }
            addComment(qwq)
            MCFPPImVisitor().visitFunctionBody(ast!!)
        }
        return cf
    }

    override fun isSelf(key: String, readOnlyArgs: List<Var<*>>, normalArgs: List<Var<*>>): Boolean {
        if (this.identifier == key && this.normalParams.size == normalArgs.size && this.readOnlyParams.size == readOnlyArgs.size) {
            if (this.normalParams.size == 0 && this.readOnlyParams.size == 0) {
                return true
            }
            var hasFoundFunc = true
            //参数比对
            for (i in normalArgs.indices) {
                if (!field.getVar(normalParams[i].identifier)!!.canAssignedBy(normalArgs[i])) {
                    hasFoundFunc = false
                    break
                }
            }
            if(hasFoundFunc){
                for (i in readOnlyArgs.indices) {
                    if (!field.getVar(readOnlyParams[i].identifier)!!.canAssignedBy(readOnlyArgs[i])) {
                        hasFoundFunc = false
                        break
                    }
                }
            }
            return hasFoundFunc
        }else{
            return false
        }
    }


    override fun isSelfWithDefaultValue(key: String, readOnlyArgs: List<Var<*>>, normalArgs: List<Var<*>>): Boolean {
        if(key != this.identifier || normalArgs.size > this.normalParams.size || readOnlyArgs.size > this.normalParams.size) return false
        if (this.normalParams.size == 0 && this.readOnlyParams.size == 0) {
            return true
        }
        var hasFoundFunc = true
        //参数比对
        var index = 0
        while (index < normalArgs.size) {
            if (!field.getVar(normalParams[index].identifier)!!.canAssignedBy(normalArgs[index])) {
                hasFoundFunc = false
                break
            }
            index++
        }
        hasFoundFunc = hasFoundFunc && this.normalParams[index].hasDefault
        if(!hasFoundFunc) return false
        index = 0
        while (index < readOnlyArgs.size) {
            if (!field.getVar(readOnlyParams[index].identifier)!!.canAssignedBy(readOnlyArgs[index])) {
                hasFoundFunc = false
                break
            }
            index++
        }
        return hasFoundFunc && this.readOnlyParams[index].hasDefault
    }

}