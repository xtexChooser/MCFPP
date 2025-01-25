package top.mcfpp.model.generic

import top.mcfpp.Project
import top.mcfpp.antlr.MCFPPGenericClassFieldVisitor
import top.mcfpp.antlr.MCFPPGenericClassImVisitor
import top.mcfpp.antlr.mcfppParser
import top.mcfpp.core.lang.MCFPPTypeVar
import top.mcfpp.core.lang.MCFPPValue
import top.mcfpp.core.lang.Var
import top.mcfpp.model.Class
import top.mcfpp.model.CompiledGenericClass
import top.mcfpp.model.accessor.Property
import top.mcfpp.type.MCFPPType

/**
 * 泛型类，即携带只读参数的类。mcfpp中的泛型借助只读参数实现，只读参数和函数的只读参数完全一致。通常来说，可以使用Class<类型 标识符>这样的形式
 *声明一个泛型类。
 *
 * 和泛型函数一样，泛型类将会在使用的时候根据传入的泛型参数的值即时编译。而泛型类本身不会被编译。编译后的泛型类将会被从0开始编号，以此进行区分
 *
 * 泛型类同样拥有“重载”的特性。例如，Test<int i>和Test<int i, int j>会被视作两个不同的泛型类。当然，两个重载的泛型类之间也是可以相互继承的。
 *诸如Test<int i, int j> : Test<i>是完全被允许的行为。
 *
 * 泛型类的只读参数是不能被继承的，它在编译的过程中即被确定，也不会作为成员储存在数据包中。
 */
open class GenericClass : Class {

    val ctx : mcfppParser.ClassBodyContext

    val compiledClasses: HashMap<List<Any?>, CompiledGenericClass> = HashMap()

    var index = 0

    val readOnlyParams: ArrayList<ClassParam> = ArrayList()

    override val namespaceID : String
        get() = "$namespace:${identifier}_${readOnlyParams.joinToString("_") { it.typeIdentifier }}"

    @get:Override
    override val prefix: String
        get() = "${namespace}_class_${identifier}_${readOnlyParams.joinToString("_") { it.typeIdentifier }}_"

    /**
     * 获取这个类指针对于的marker的tag
     */
    override val tag: String
        get() = "${namespace}_class_${identifier}_${readOnlyParams.joinToString("_") { it.typeIdentifier }}_pointer"

    /**
     * 生成一个类，它拥有指定的标识符和命名空间
     * @param identifier 类的标识符
     * @param namespace 类的命名空间
     */
    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(identifier: String, namespace: String = Project.currNamespace, ctx : mcfppParser.ClassBodyContext):super(identifier, namespace) {
        this.ctx = ctx
    }

    open fun compile(readonlyArgs: List<Var<*>>) : CompiledGenericClass {
        //只读属性
        val args = ArrayList<Var<*>>()
        for (i in readOnlyParams.indices) {
            val r = readonlyArgs[i].implicitCast(readOnlyParams[i].type!!)
            r.isConst = true
            args.add(r)
        }
        val values = args.map { (it as MCFPPValue<*>).value }
        compiledClasses[values]?.let { return it }

        val cls = CompiledGenericClass(
            "${identifier}_${readOnlyParams.joinToString("_") { it.typeIdentifier }}_$index",
            namespace,
            this,
            args.map { it as MCFPPValue<*> }
        )
        cls.initialize()
        for (parent in this.parent){
            cls.extends(parent)
        }
        cls.isStaticClass = this.isStaticClass
        cls.isAbstract = this.isStaticClass

        //只读属性
        for (i in readOnlyParams.indices) {
            if(args[i] is MCFPPTypeVar){
                cls.field.putType(readOnlyParams[i].identifier, (args[i] as MCFPPTypeVar).value)
            }
            cls.field.putVar(readOnlyParams[i].identifier, args[i], false)
            cls.field.putProperty(readOnlyParams[i].identifier, Property.buildSimpleProperty(args[i]))
        }

        //注册
        Class.currClass = cls
        MCFPPGenericClassFieldVisitor(cls).visitClassDeclaration(ctx.parent as mcfppParser.ClassDeclarationContext)
        Class.currClass = cls
        MCFPPGenericClassImVisitor().visitClassBody(ctx)
        index ++

        compiledClasses[args.map { (it as MCFPPValue<*>).value }] = cls

        return cls
    }

    fun isSelfByType(identifier: String, readOnlyParam: List<MCFPPType>): Boolean {
        if (this.identifier == identifier) {
            if (readOnlyParam.size != readOnlyParams.size) {
                return false
            }
            for (i in readOnlyParam.indices) {
                if(readOnlyParams[i].type != null){
                    if (!readOnlyParams[i].type!!.build("").canAssignedBy(readOnlyParam[i].build(""))) {
                        return false
                    }
                }else{
                    if(readOnlyParam[i].typeName != readOnlyParam[i].typeName){
                        return false
                    }
                }
            }
            return true
        }
        return false
    }

    fun isSelfByString(identifier: String, readOnlyParam: List<String>): Boolean{
        if (this.identifier == identifier) {
            if (readOnlyParam.size != readOnlyParams.size) {
                return false
            }
            for (i in readOnlyParam.indices) {
                if (readOnlyParam[i] != readOnlyParams[i].typeIdentifier) {
                    return false
                }
            }
            return true
        }
        return false
    }

    companion object {
        class Unknown
    }
}

class ClassParam(

    /**
     * 参数类型标识符
     */
    var typeIdentifier: String,

    /**
     * 参数的名字
     */
    var identifier: String,

    /**
     * 参数的类型。只有在[Project.INDEX_TYPE]阶段结束后才有值
     */
    var type: MCFPPType? = null
)