package top.mcfpp.antlr

import top.mcfpp.Project
import top.mcfpp.annotations.InsertCommand
import top.mcfpp.annotations.MNIFunction
import top.mcfpp.antlr.mcfppParser.TemplateDeclarationContext
import top.mcfpp.compiletime.CompileTimeFunction
import top.mcfpp.core.lang.*
import top.mcfpp.exception.UndefinedException
import top.mcfpp.exception.VariableConverseException
import top.mcfpp.io.MCFPPFile
import top.mcfpp.model.*
import top.mcfpp.model.Member.AccessModifier
import top.mcfpp.model.accessor.*
import top.mcfpp.model.field.GlobalField
import top.mcfpp.model.field.IFieldWithType
import top.mcfpp.model.function.*
import top.mcfpp.model.function.Function
import top.mcfpp.model.generic.GenericExtensionFunction
import top.mcfpp.model.generic.GenericFunction
import top.mcfpp.type.MCFPPBaseType
import top.mcfpp.type.MCFPPType
import top.mcfpp.util.LogProcessor
import top.mcfpp.util.StringHelper
import top.mcfpp.util.TextTranslator
import top.mcfpp.util.TextTranslator.translate
import java.util.*

/**
 * 在编译工程之前，应当首先将所有文件中的资源全部遍历一次并写入缓存。
 * TODO 存在优化空间，因为部分代码和McfppTypeVisitor有高度重合和相关性
 */
open class MCFPPFieldVisitor : mcfppParserBaseVisitor<Any?>() {

    protected var isStatic = false

    protected lateinit var typeScope : IFieldWithType

    private var currClassOrTemplate: CompoundData? = null

    /**
     * 遍历整个文件。一个文件包含了命名空间的声明，函数的声明，类的声明以及全局变量的声明。全局变量是可以跨文件调用的。
     * @param ctx the parse tree
     * @return null
     */
    override fun visitCompilationUnit(ctx: mcfppParser.CompilationUnitContext): Any? {
        Project.ctx = ctx
        typeScope = GlobalField.localNamespaces[Project.currNamespace]!!.field
        //文件结构，类和函数
        for (t in ctx.typeDeclaration()) {
            visit(t)
        }
        return null
    }

    /**
     * 类或函数声明
     * @param ctx the parse tree
     * @return null
     */

    override fun visitDeclarations(ctx: mcfppParser.DeclarationsContext): Any? {
        Project.ctx = ctx
        if (ctx.globalDeclaration() != null) {
            return null
        }
        super.visitDeclarations(ctx)
        return null
    }

//region interface

    override fun visitInterfaceDeclaration(ctx: mcfppParser.InterfaceDeclarationContext): Any? {
        Project.ctx = ctx
        //注册类
        val id = ctx.classWithoutNamespace().text
        val namespace = GlobalField.localNamespaces[Project.currNamespace]!!

        if (namespace.field.hasInterface(id)) {
            //重复声明
            Interface.currInterface = namespace.field.getInterface(id)
        } else {
            throw UndefinedException("Interface Should have been defined: $id")
        }
        typeScope = Interface.currInterface!!.field
        currClassOrTemplate = Interface.currInterface
        //接口成员
        for (m in ctx.interfaceBody().interfaceFunctionDeclaration()){
            visit(m)
        }
        typeScope = MCFPPFile.currFile!!.field.namespaceField
        currClassOrTemplate = null
        return null
    }


    override fun visitInterfaceFunctionDeclaration(ctx: mcfppParser.InterfaceFunctionDeclarationContext): Any? {
        Project.ctx = ctx
        //创建函数对象
        val f = Function(
            ctx.Identifier().text,
            Interface.currInterface!!,
            null
        )
        f.returnType = ctx.functionReturnType()?.type()?.let {
            MCFPPType.parseFromContext(it.typeWithoutExcl().type(), typeScope)?:  run {
                LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(it.text))
                MCFPPBaseType.Any
            }
        }?: MCFPPBaseType.Void
        //解析参数
        f.addParamsFromContext(ctx.functionParams())
        //注册函数
        if (Interface.currInterface!!.field.hasFunction(f, true)) {
            LogProcessor.error("Already defined function:" + ctx.Identifier().text + "in class " + Class.currClass!!.identifier)
            Function.currFunction = Function.nullFunction
        }
        return null
    }
//endregion

//region native class
    /**
     * TODO native类的声明
     * @param ctx the parse tree
     * @return null
     */
    override fun visitNativeClassDeclaration(ctx: mcfppParser.NativeClassDeclarationContext): Any? {
        //NativeClassVisitor().visit(ctx)
        return null
    }
//endregion

//region class
    /**
     * 类的声明
     * @param ctx the parse tree
     * @return null
     */

    override fun visitClassDeclaration(ctx: mcfppParser.ClassDeclarationContext): Any? {
        Project.ctx = ctx
        //注册类
        val id = ctx.classWithoutNamespace().text
        val namespace = GlobalField.localNamespaces[Project.currNamespace]!!
        if(ctx.readOnlyParams() != null){
            //如果是泛型类，将类型实例化，但暂时不编译
            val types = ctx.readOnlyParams().parameterList().parameter().map { MCFPPType.parseFromContext(it.type(), namespace.field)?: run {
                LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(it.text))
                MCFPPBaseType.Any
            } }
            val clazz = namespace.field.getClass(id, types)!!
            for (p in clazz.readOnlyParams) {
                p.type = MCFPPType.parseFromIdentifier(p.typeIdentifier, namespace.field)
            }
            return null
        }
        val clazz = if (namespace.field.hasClass(id)) {
            namespace.field.getClass(id)
        } else {
            throw UndefinedException("Class Should have been defined: $id")
        }
        Class.currClass = clazz
        currClassOrTemplate = clazz
        typeScope = Class.currClass!!.field
        isStatic = false
        //解析类中的成员
        //先解析函数和构造函数
        for (c in ctx.classBody().classMemberDeclaration()) {
            c!!
            if (c.classMember() != null && (c.classMember().classFunctionDeclaration() != null || c.classMember().abstractClassFunctionDeclaration() != null)) {
                visit(c)
            }
        }
        //再解析变量
        for (c in ctx.classBody().classMemberDeclaration()) {
            if (c!!.classMember() != null && c.classMember().classFieldDeclaration() != null) {
                visit(c)
            }
        }
        //如果没有构造函数，自动添加默认的空构造函数
        if (Class.currClass!!.constructors.size == 0) {
            Class.currClass!!.addConstructor(ClassConstructor(Class.currClass!!))
        }
        //是否为抽象类
        if(!Class.currClass!!.isAbstract){
            var il : Function? = null
            Class.currClass!!.field.forEachFunction { f ->
                run {
                    if(f.isAbstract){
                        il = f
                        return@run
                    }
                }
            }
            if(il != null){
                LogProcessor.error("Class ${Class.currClass} must either be declared abstract or implement abstract method ${il!!.nameWithNamespace}")
            }
        }
        Class.currClass = null
        currClassOrTemplate = null
        typeScope = MCFPPFile.currFile!!.field.namespaceField
        return null
    }

    override fun visitObjectClassDeclaration(ctx: mcfppParser.ObjectClassDeclarationContext): Any? {
        Project.ctx = ctx
        //注册类
        val id = ctx.classWithoutNamespace().text
        val namespace = GlobalField.localNamespaces[Project.currNamespace]!!
        if(ctx.readOnlyParams() != null){
            return null
        }
        val clazz = namespace.field.getObject(id)
        if(clazz !is ObjectClass){
            throw UndefinedException("Class should have been defined: $id")
        }
        //链接伴随对象
        val cls = namespace.field.getClass(id)
        if(cls != null){
            if(cls.objectClass != null){
                LogProcessor.error("Duplicate object class definition: $id")
            }else{
                cls.objectClass = clazz
            }
        }
        Class.currClass = clazz
        currClassOrTemplate = clazz
        typeScope = Class.currClass!!.field
        isStatic = true
        //解析类中的成员
        //先解析函数和构造函数
        for (c in ctx.classBody().classMemberDeclaration()) {
            c!!
            if (c.classMember() != null && (c.classMember().classFunctionDeclaration() != null || c.classMember().abstractClassFunctionDeclaration() != null)) {
                visit(c)
            }
        }
        //再解析变量
        for (c in ctx.classBody().classMemberDeclaration()) {
            if (c!!.classMember() != null && c.classMember().classFieldDeclaration() != null) {
                visit(c)
            }
        }
        Class.currClass = null
        currClassOrTemplate = null
        typeScope = MCFPPFile.currFile!!.field.namespaceField
        return null
    }

    /**
     * 类成员的声明。由于函数声明可以后置，因此需要先查明函数声明情况再进行变量的注册以及初始化。
     * <pre>
     * `classMemberDeclaration
     * :   accessModifier? (STATIC)? classMember
     * ;
    `</pre> *
     * @param ctx the parse tree
     * @return null
     */
    
    override fun visitClassMemberDeclaration(ctx: mcfppParser.ClassMemberDeclarationContext): Any? {
        Project.ctx = ctx
        val m = visit(ctx.classMember())
        if(m is Member){
            //访问修饰符
            m.accessModifier = AccessModifier.valueOf((ctx.accessModifier()?.text?:"public").uppercase(Locale.getDefault()))
            if (m !is ClassConstructor) {
                Class.currClass!!.addMember(m)
            }
        }else if(m is Pair<*,*>){//Pair<Var, Property>
            val v = m.first as Var<*>?
            val p = m.second as Property?
            if(v == null || p == null) return null
            //访问修饰符
            v.accessModifier = AccessModifier.valueOf((ctx.accessModifier()?.text?:"public").uppercase(Locale.getDefault()))
            p.accessModifier = v.accessModifier
            Class.currClass!!.addMember(v)
            Class.currClass!!.addMember(p)
        }
        return null
    }

    override fun visitClassMember(ctx: mcfppParser.ClassMemberContext): Any? {
        Project.ctx = ctx
        return if (ctx.nativeClassFunctionDeclaration() != null) {
            visit(ctx.nativeClassFunctionDeclaration())
        } else if (ctx.classFunctionDeclaration() != null) {
            visit(ctx.classFunctionDeclaration())
        } else if (ctx.classFieldDeclaration() != null) {
            visit(ctx.classFieldDeclaration())
        } else if (ctx.classConstructorDeclaration() != null) {
            visit(ctx.classConstructorDeclaration())
        }else{
            return null
        }
    }

    /**
     * 类方法的声明
     * @param ctx the parse tree
     * @return 这个类方法的对象
     */
    
    override fun visitClassFunctionDeclaration(ctx: mcfppParser.ClassFunctionDeclarationContext): Any {
        Project.ctx = ctx
        //创建函数对象
        val f = if(ctx.functionParams().readOnlyParams() != null && ctx.functionParams().readOnlyParams().parameterList().parameter().size != 0){
            GenericFunction(
                ctx.Identifier().text,
                Class.currClass!!,
                Class.currClass!! is ObjectClass,
                ctx.functionBody()
            )
        }else{
            Function(
                ctx.Identifier().text,
                Class.currClass!!,
                Class.currClass!! is ObjectClass,
                ctx.functionBody()
            )
        }
        f.returnType = if(ctx.functionReturnType()?.type() != null){
            MCFPPType.parseFromContext(ctx.functionReturnType().type(), typeScope)?: run {
                LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(ctx.functionReturnType().text))
                MCFPPBaseType.Any
            }
        }else{
            MCFPPBaseType.Void
        }
        if(!isStatic){
            val thisObj = Class.currClass!!.getType().buildUnConcrete("this")
            f.field.putVar("this",thisObj)
        }
        //解析参数
        f.addParamsFromContext(ctx.functionParams())
        //注册函数
        if (Class.currClass!!.field.hasFunction(f, true)) {
            if(ctx.OVERRIDE() != null){
                if(isStatic){
                    LogProcessor.error("Cannot override static method ${ctx.Identifier()}")
                    throw Exception()
                }
            }else{
                LogProcessor.error("Already defined function:" + ctx.Identifier().text + "in class " + Class.currClass!!.identifier)
                Function.currFunction = Function.nullFunction
            }
        }
        f.ast = null
        return f
    }

    
    override fun visitAbstractClassFunctionDeclaration(ctx: mcfppParser.AbstractClassFunctionDeclarationContext): Any {
        Project.ctx = ctx
        //抽象函数没有函数体，不能作为GenericFunction
        //创建函数对象
        val f = Function(
            ctx.Identifier().text,
            Class.currClass!!,
            false,
            null
        )
        f.returnType = if(ctx.functionReturnType()?.type() != null){
            MCFPPType.parseFromContext(ctx.functionReturnType().type(), typeScope)?: run {
                LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(ctx.functionReturnType().text))
                MCFPPBaseType.Any
            }
        }else{
            MCFPPBaseType.Void
        }
        f.isAbstract = true
        if(f.isStatic){
            LogProcessor.error("Static Function cannot be abstract: ${ctx.Identifier().text} in class ${Class.currClass!!.identifier}" )
            throw Exception()
        }
        //解析参数
        f.addParamsFromContext(ctx.functionParams())
        //注册函数
        if (Class.currClass!!.field.hasFunction(f, true)) {
            LogProcessor.error("Already defined function:" + ctx.Identifier().text + "in class " + Class.currClass!!.identifier)
            Function.currFunction = Function.nullFunction
        }
        return f
    }

    override fun visitNativeClassFunctionDeclaration(ctx: mcfppParser.NativeClassFunctionDeclarationContext): Any? {
        Project.ctx = ctx
        val nf = NativeFunction(ctx.Identifier().text, Project.currNamespace)
        nf.returnType = if(ctx.functionReturnType()?.type() != null){
            MCFPPType.parseFromContext(ctx.functionReturnType().type(), typeScope)?: run {
                LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(ctx.functionReturnType().text))
                MCFPPBaseType.Any
            }
        }else{
            MCFPPBaseType.Void
        }
        nf.addParamsFromContext(ctx.functionParams())
        //是类成员
        nf.ownerType = Function.Companion.OwnerType.CLASS
        try {
            //根据JavaRefer找到类
            val refer = ctx.javaRefer().text
            val clsName = refer.substring(0,refer.lastIndexOf('.'))
            val clazz = Project.classLoader.loadClass(clsName)
            val methods = clazz.methods
            var hasFind = false
            for(method in methods){
                val mniRegister = method.getAnnotation(MNIFunction::class.java) ?: continue
                //解析MNIMethod注解成员
                val readOnlyType = mniRegister.readOnlyParams.map {
                    MCFPPType.parseFromIdentifier(it.split(" ", limit = 2)[0], Namespace.currNamespaceField)
                }
                val normalType = mniRegister.normalParams.map {
                    MCFPPType.parseFromIdentifier(it.split(" ", limit = 2)[0], Namespace.currNamespaceField)
                }
                //比对
                if(nf.readOnlyParams.map { it.type } == readOnlyType && nf.normalParams.map { it.type } == normalType){
                    hasFind = true
                    nf.javaMethod = method
                    break
                }
            }
            if(!hasFind){
                throw NoSuchMethodException("Cannot find method ${ctx.Identifier().text} with correct parameters in class $clsName")
            }
        } catch (e: ClassNotFoundException) {
            LogProcessor.error("Cannot find java class: " + e.message)
            return null
        }
        return nf
    }

    /**
     * 构造函数的声明
     * @param ctx the parse tree
     * @return 这个构造函数的对象
     */
    override fun visitClassConstructorDeclaration(ctx: mcfppParser.ClassConstructorDeclarationContext): Any {
        Project.ctx = ctx
        //类构造函数
        //创建构造函数对象，注册函数
        val f = ClassConstructor(Class.currClass!!)
        f.addParamsFromContext(ctx.normalParams())
        if(!Class.currClass!!.addConstructor(f)){
            LogProcessor.error("Already defined constructor:  constructor(" + ctx.normalParams().text + ") in class " + Class.currClass)
        }
        return f
    }

    private lateinit var currVar: Var<*>
    /**
     * 类字段的声明
     * @param ctx the parse tree
     * @return null
     */
    @InsertCommand
    override fun visitClassFieldDeclaration(ctx: mcfppParser.ClassFieldDeclarationContext): Pair<Var<*>?, Property?> {
        Project.ctx = ctx
        //只有类字段构建
        val c = ctx.fieldDeclarationExpression()
        val type = MCFPPType.parseFromContext(ctx.type(), typeScope)?: run {
            LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(ctx.type().text))
            MCFPPBaseType.Any
        }
        val `var` = type.buildUnConcrete(c.Identifier().text, Class.currClass!!)
        if(Class.currClass is ObjectClass && `var` is OnScoreboard){
            `var`.name = (Class.currClass as ObjectClass).mcuuid.uuid.toString()
        }else if(`var` is OnScoreboard){
            `var`.name = "@s"
        }
        `var`.isDynamic = true
        `var`.parent = ClassPointer(Class.currClass!!, "this")
        if (Class.currClass!!.field.containVar(c.Identifier().text)) {
            LogProcessor.error("Duplicate defined variable name:" + c.Identifier().text)
            return null to null
        }
        //变量的初始化
        if (c.expression() != null) {
            Function.currFunction = Class.currClass!!.classPreInit
            //是类的成员
            Function.addComment(ctx.text)
            val init: Var<*> = MCFPPExprVisitor().visit(c.expression())!!
            try {
                `var`.assignedBy(init)
            } catch (e: VariableConverseException) {
                LogProcessor.error("Cannot convert " + init.javaClass + " to " + `var`.javaClass)
                Function.currFunction = Function.nullFunction
                throw VariableConverseException()
            }
            Function.currFunction = Function.nullFunction
            `var`.hasAssigned = true
        }
        //属性访问器
        `var`.parent = null
        currVar = `var`
        val properties = (ctx.accessor()?.let{visit(ctx.accessor())}?: Property.buildSimpleProperty(`var`)) as Property
        return `var` to properties
    }

    override fun visitAccessor(ctx: mcfppParser.AccessorContext): Any? {
        Project.ctx = ctx
        val getter = if(ctx.getter() != null){
            visit(ctx.getter()) as AbstractAccessor
        }else{
            null
        }
        val setter = if(ctx.setter() != null){
            visit(ctx.setter()) as AbstractMutator
        }else{
            null
        }
        return Property(currVar.identifier, getter, setter)
    }

    override fun visitGetter(ctx: mcfppParser.GetterContext): Any? {
        Project.ctx = ctx
        return if(ctx.functionBody() != null){
            FunctionAccessor(currVar, currClassOrTemplate!!)
        }else if(ctx.javaRefer() != null){
            NativeAccessor(ctx.javaRefer().text, currClassOrTemplate!!, currVar)
        }else if(ctx.expression() != null){
            ExpressionAccessor(ctx.expression(), currVar)
        }else{
            SimpleAccessor()
        }
    }

    override fun visitSetter(ctx: mcfppParser.SetterContext): Any? {
        Project.ctx = ctx
        return if(ctx.functionBody() != null){
            FunctionMutator(currVar, currClassOrTemplate!!)
        }else if(ctx.javaRefer() != null){
            NativeMutator(ctx.javaRefer().text, currClassOrTemplate!!, currVar)
        }else if(ctx.expression() != null){
            ExpressionMutator(ctx.expression(), currVar)
        }else{
            SimpleMutator(currVar)
        }
    }

//endregion

//region function
    /**
     * 函数的声明
     * @param ctx the parse tree
     * @return null
     */
    
    override fun visitFunctionDeclaration(ctx: mcfppParser.FunctionDeclarationContext): Any? {
        Project.ctx = ctx
        //创建函数对象
        val identifier : String = ctx.Identifier().text
        val f = if(ctx.functionParams()?.readOnlyParams() != null && ctx.functionParams().readOnlyParams().parameterList().parameter().size != 0){
            GenericFunction(identifier, Project.currNamespace, ctx.functionBody())
        }else {
            Function(identifier, Project.currNamespace, ctx.functionBody())
        }
        f.returnType = if(ctx.functionReturnType()?.type() != null){
            MCFPPType.parseFromContext(ctx.functionReturnType().type(), typeScope)?: run {
                LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(ctx.functionReturnType().text))
                MCFPPBaseType.Any
            }
        }else{
            MCFPPBaseType.Void
        }
        //解析参数
        ctx.functionParams()?.let { f.addParamsFromContext(it) }
        //TODO 解析函数的注解
        //不是类的成员
        f.ownerType = Function.Companion.OwnerType.NONE
        //写入域
        val namespace = GlobalField.localNamespaces[f.namespace]!!
        if (namespace.field.hasFunction(f, true)) {
            LogProcessor.error("Already defined function: " + f.namespaceID)
            Function.currFunction = Function.nullFunction
        } else if(namespace.field.hasDeclaredType(f.identifier)) {
            LogProcessor.error("Function name conflicted with type name: " + f.identifier)
            Function.currFunction = Function.nullFunction
        } else{
            namespace.field.addFunction(f,false)
        }
        if (f.isEntrance
            && ctx.functionParams().normalParams().parameterList().parameter().size != 0
            && (ctx.functionParams().readOnlyParams() == null || ctx.functionParams().readOnlyParams().parameterList().parameter().size != 0)
            ) {
            LogProcessor.error("Entrance function shouldn't have parameter:" + f.namespaceID)
        }
        return null
    }

    override fun visitInlineFunctionDeclaration(ctx: mcfppParser.InlineFunctionDeclarationContext): Any? {
        Project.ctx = ctx
        //创建函数对象
        val f: Function
        //是否是内联函数
        val identifier : String = ctx.Identifier().text
        f = InlineFunction(identifier, Project.currNamespace, ctx.functionBody())
        //解析参数
        f.addParamsFromContext(ctx.functionParams())
        //TODO 解析函数的注解
        //不是类的成员
        f.ownerType = Function.Companion.OwnerType.NONE
        //写入域
        val namespace = GlobalField.localNamespaces[f.namespace]!!
        if (!namespace.field.hasFunction(f, true)) {
            namespace.field.addFunction(f,false)
        } else {
            LogProcessor.error("Already defined function:" + f.namespaceID)
            Function.currFunction = Function.nullFunction
        }
        if (f.isEntrance
            && ctx.functionParams().normalParams().parameterList().parameter().size != 0
            && (ctx.functionParams().readOnlyParams() == null || ctx.functionParams().readOnlyParams().parameterList().parameter().size != 0)
        ) {
            LogProcessor.error("Entrance function shouldn't have parameter:" + f.namespaceID)
        }
        return null
    }

    override fun visitCompileTimeFuncDeclaration(ctx: mcfppParser.CompileTimeFuncDeclarationContext): Any? {
        Project.ctx = ctx
        //创建函数对象
        val f: Function
        //是否是编译时函数
        val identifier : String = ctx.Identifier().text
        f = CompileTimeFunction(
            identifier,Project.currNamespace,
            ctx.functionBody()
        )
        f.returnType = if(ctx.functionReturnType()?.type() != null){
            MCFPPType.parseFromContext(ctx.functionReturnType().type(), typeScope)?: run {
                LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(ctx.functionReturnType().text))
                MCFPPBaseType.Any
            }
        }else{
            MCFPPBaseType.Void
        }
        //解析参数
        f.addParamsFromContext(ctx.functionParams())
        //TODO 解析函数的注解
        //不是类的成员
        f.ownerType = Function.Companion.OwnerType.NONE
        //写入域
        val namespace = GlobalField.localNamespaces[f.namespace]!!
        if (!namespace.field.hasFunction(f, true)) {
            f.setField(namespace.field)
            namespace.field.addFunction(f,false)
        } else {
            LogProcessor.error("Already defined function:" + f.namespaceID)
            Function.currFunction = Function.nullFunction
        }
        if (f.isEntrance
            && ctx.functionParams().normalParams().parameterList().parameter().size != 0
            && (ctx.functionParams().readOnlyParams() == null || ctx.functionParams().readOnlyParams().parameterList().parameter().size != 0)
        ) {
            LogProcessor.error("Entrance function shouldn't have parameter:" + f.namespaceID)
        }
        return null
    }


    //TODO 单例的拓展函数
    override fun visitExtensionFunctionDeclaration(ctx: mcfppParser.ExtensionFunctionDeclarationContext?): Any? {
        Project.ctx = ctx!!
        val ownerType : Function.Companion.OwnerType
        //获取被拓展的类
        val data : CompoundData = if(ctx.type().typeWithoutExcl().className() == null){
            ownerType = Function.Companion.OwnerType.BASIC
            when(ctx.type().text){
                "int" -> MCInt.data
                else -> {
                    LogProcessor.error("Cannot add extension function to ${ctx.type().text}")
                    return null
                }
            }
        }else{
            val (nsp, id) = StringHelper.splitNamespaceID(ctx.type().typeWithoutExcl().className().text)
            val qwq: Class? = GlobalField.getClass(nsp, id)
            if (qwq == null) {
                val pwp = GlobalField.getTemplate(nsp, id)
                if(pwp == null){
                    LogProcessor.error("Undefined class or struct:" + ctx.type().typeWithoutExcl().className().text)
                    return null
                }else{
                    ownerType = Function.Companion.OwnerType.TEMPLATE
                    pwp
                }
            }else{
                ownerType = Function.Companion.OwnerType.CLASS
                qwq
            }
        }
        //创建函数对象
        val f = if(ctx.functionParams().readOnlyParams() != null && ctx.functionParams().readOnlyParams().parameterList().parameter().size != 0){
            GenericExtensionFunction(ctx.Identifier().text, data, Project.currNamespace, ctx.functionBody())
        }else{
            ExtensionFunction(ctx.Identifier().text, data, Project.currNamespace, ctx.functionBody())
        }
        //解析参数
        f.accessModifier = AccessModifier.PUBLIC
        f.returnType = if(ctx.functionReturnType()?.type() != null){
            MCFPPType.parseFromContext(ctx.functionReturnType().type(), typeScope)?: run {
                LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(ctx.functionReturnType().text))
                MCFPPBaseType.Any
            }
        }else{
            MCFPPBaseType.Void
        }
        f.ownerType = ownerType
        f.isStatic = ctx.STATIC() != null
        f.addParamsFromContext(ctx.functionParams())
        val field = data.field
        //注册函数
        if (!field.addFunction(f,false)) {
            LogProcessor.error("Already defined function:" + ctx.Identifier().text + "in class " + Class.currClass!!.identifier)
            Function.currFunction = Function.nullFunction
        }
        return null
    }

    /**
     * native函数的声明
     * @param ctx the parse tree
     * @return 如果是全局，返回null，否则返回这个函数对象
     */
    
    override fun visitNativeFuncDeclaration(ctx: mcfppParser.NativeFuncDeclarationContext): Any? {
        Project.ctx = ctx
        val nf = NativeFunction(ctx.Identifier().text, Project.currNamespace)
        nf.returnType = if(ctx.functionReturnType()?.type() != null){
            MCFPPType.parseFromContext(ctx.functionReturnType().type(), typeScope)?: run {
                LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(ctx.functionReturnType().text))
                MCFPPBaseType.Any
            }
        }else{
            MCFPPBaseType.Void
        }
        nf.addParamsFromContext(ctx.functionParams())
        try {
            //根据JavaRefer找到类
            val refer = ctx.javaRefer().text
            val clsName = refer.substring(0,refer.lastIndexOf('.'))
            val clazz = Project.classLoader.loadClass(clsName)
            val methods = clazz.methods
            var hasFind = false
            for(method in methods){
                val mniRegister = method.getAnnotation(MNIFunction::class.java) ?: continue
                //解析MNIMethod注解成员
                val readOnlyType = mniRegister.readOnlyParams.map {
                    MCFPPType.parseFromIdentifier(it.split(" ", limit = 2)[0], Namespace.currNamespaceField)
                }
                val normalType = mniRegister.normalParams.map {
                    MCFPPType.parseFromIdentifier(it.split(" ", limit = 2)[0], Namespace.currNamespaceField)
                }
                //比对
                if(nf.readOnlyParams.map { it.type } == readOnlyType && nf.normalParams.map { it.type } == normalType){
                    hasFind = true
                    nf.javaMethod = method
                    break
                }
            }
            if(!hasFind){
                throw NoSuchMethodException("Cannot find method ${ctx.Identifier().text} with correct parameters in class $clsName")
            }
        } catch (e: ClassNotFoundException) {
            LogProcessor.error("Cannot find java class: " + e.message)
            e.printStackTrace()
            return null
        }
        //写入域
        val namespace = GlobalField.localNamespaces[nf.namespace]!!
        //是普通的函数
        nf.ownerType = Function.Companion.OwnerType.NONE
        if (!namespace.field.hasFunction(nf, true)) {
            namespace.field.addFunction(nf,false)
        } else {
            LogProcessor.error("Already defined function:" + ctx.Identifier().text)
            Function.currFunction = Function.nullFunction
        }
        return nf
    }
//endregion

//region template
    override fun visitTemplateDeclaration(ctx: TemplateDeclarationContext?): Any? {
        Project.ctx = ctx!!
        //注册模板
        val id = ctx.classWithoutNamespace().text
        val namespace1 = GlobalField.localNamespaces[Project.currNamespace]!!
        val template = if(namespace1.field.hasTemplate(id)){
            namespace1.field.getTemplate(id)!!
        }else{
            throw UndefinedException("Template should have been defined: $id")
        }
        DataTemplate.currTemplate = template
        currClassOrTemplate = template
        typeScope = template.field
        for (c in ctx.className()){
            //是否存在继承
            val (namespace, identifier) = StringHelper.splitNamespaceID(c.text)
            val s = GlobalField.getTemplate(namespace, identifier)
            if(s == null){
                val o = GlobalField.getObject(namespace, identifier)
                if(o is ObjectDataTemplate) {
                    template.extends(o)
                }else{
                    LogProcessor.error("Undefined template: " + c.text)
                }
            }else{
                if(s == template){
                    LogProcessor.error("Infinitive reference: $id -> $identifier")
                }else{
                    template.extends(s)
                }
            }
        }
        isStatic = false
        //解析成员
        //先解析函数和构造函数
        for (c in ctx.templateBody().templateMemberDeclaration()) {
            if (c!!.templateMember().templateFunctionDeclaration() != null || c.templateMember().templateConstructorDeclaration() != null) {
                visit(c)
            }
        }
        //再解析变量
        for (c in ctx.templateBody().templateMemberDeclaration()) {
            if (c!!.templateMember().templateFieldDeclaration() != null) {
                visit(c)
            }
        }
        //如果没有构造函数，生成默认的构造函数
        if(template.constructors.isEmpty()){
            template.addMember(DataTemplateConstructor(DataTemplate.currTemplate!!, null))
        }
        DataTemplate.currTemplate = null
        currClassOrTemplate = null
        typeScope = MCFPPFile.currFile!!.field.namespaceField
        return null
    }

    override fun visitObjectTemplateDeclaration(ctx: mcfppParser.ObjectTemplateDeclarationContext): Any? {
        Project.ctx = ctx
        //注册模板
        val id = ctx.classWithoutNamespace().text
        val namespace1 = GlobalField.localNamespaces[Project.currNamespace]!!
        val objectTemplate = namespace1.field.getObject(id)
        if(objectTemplate !is ObjectDataTemplate){
            throw UndefinedException("Template should have been defined: $id")
        }
        DataTemplate.currTemplate = objectTemplate
        currClassOrTemplate = objectTemplate
        typeScope = objectTemplate.field
        for (c in ctx.className()){
            //是否存在继承
            val (namespace, identifier) = StringHelper.splitNamespaceID(c.text)
            val s = GlobalField.getTemplate(namespace, identifier)
            if(s == null){
                val o = GlobalField.getObject(namespace, identifier)
                if(o is ObjectDataTemplate) {
                    if(o == objectTemplate){
                        LogProcessor.error("Infinitive reference: $id -> $identifier")
                    }else{
                        objectTemplate.extends(o)
                    }
                }else{
                    LogProcessor.error("Undefined template: " + c.text)
                }
            }else{
                objectTemplate.extends(s)
            }
        }
        isStatic = true
        //解析成员
        //先解析函数
        for (c in ctx.templateBody().templateMemberDeclaration()) {
            if (c!!.templateMember().templateFunctionDeclaration() != null) {
                visit(c)
            }
        }
        //再解析变量
        for (c in ctx.templateBody().templateMemberDeclaration()) {
            if (c!!.templateMember().templateFieldDeclaration() != null) {
                visit(c)
            }
        }
        DataTemplate.currTemplate = null
        currClassOrTemplate = null
        typeScope = MCFPPFile.currFile!!.field.namespaceField
        return null
    }

    override fun visitTemplateMemberDeclaration(ctx: mcfppParser.TemplateMemberDeclarationContext): Any? {
        Project.ctx = ctx
        val m = visit(ctx.templateMember())
        val accessModifier = AccessModifier.valueOf(ctx.accessModifier()?.text?:"public".uppercase(Locale.getDefault()))
        //访问修饰符
        if(m is Member){
            m.accessModifier = accessModifier
            DataTemplate.currTemplate!!.addMember(m)
        }else if(m is Pair<*,*>){//Pair<Var, Property>
            val v = m.first as Var<*>?
            val p = m.second as Property?
            if(v == null || p == null) return null
            //访问修饰符
            v.accessModifier = AccessModifier.valueOf(ctx.accessModifier()?.text?:"public".uppercase(Locale.getDefault()))
            p.accessModifier = v.accessModifier
            DataTemplate.currTemplate!!.addMember(v)
            DataTemplate.currTemplate!!.addMember(p)
        }
        return null
    }

    
    override fun visitTemplateMember(ctx: mcfppParser.TemplateMemberContext): Any? {
        Project.ctx = ctx
        return if (ctx.templateFunctionDeclaration() != null) {
            visit(ctx.templateFunctionDeclaration())
        } else if (ctx.templateFieldDeclaration() != null) {
            visit(ctx.templateFieldDeclaration())
        } else if(ctx.templateConstructorDeclaration() != null) {
            visit(ctx.templateConstructorDeclaration())
        } else{
            return null
        }
    }

    override fun visitTemplateFunctionDeclaration(ctx: mcfppParser.TemplateFunctionDeclarationContext): Any {
        Project.ctx = ctx
        //创建函数对象
        val f = if(ctx.functionParams().readOnlyParams() != null && ctx.functionParams().readOnlyParams().parameterList().parameter().size != 0){
            GenericFunction(
                ctx.Identifier().text,
                DataTemplate.currTemplate!!,
                DataTemplate.currTemplate is ObjectDataTemplate,
                ctx.functionBody()
            )
        }else {
            Function(
                ctx.Identifier().text,
                DataTemplate.currTemplate!!,
                DataTemplate.currTemplate is ObjectDataTemplate,
                ctx.functionBody()
            )
        }
        f.returnType = if(ctx.functionReturnType()?.type() != null){
            MCFPPType.parseFromContext(ctx.functionReturnType().type(), typeScope)?: run {
                LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(ctx.functionReturnType().text))
                MCFPPBaseType.Any
            }
        }else{
            MCFPPBaseType.Void
        }
        //解析参数
        f.addParamsFromContext(ctx.functionParams())
        //注册函数
        if (DataTemplate.currTemplate!!.field.hasFunction(f, true)) {
            LogProcessor.error("Already defined function:" + ctx.Identifier().text + "in struct " + DataTemplate.currTemplate!!.identifier)
            Function.currFunction = Function.nullFunction
        }
        return f
    }

    override fun visitTemplateFieldDeclaration(ctx: mcfppParser.TemplateFieldDeclarationContext): Pair<Var<*>?, Property?> {
        Project.ctx = ctx
        val `var` = if(ctx.singleTemplateFieldType() != null){
            val type = MCFPPType.parseFromContext(ctx.singleTemplateFieldType().type(), typeScope)?: run {
                LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(ctx.singleTemplateFieldType().type().text))
                MCFPPBaseType.Any
            }
            type.build(ctx.Identifier().text).apply {
                nullable = ctx.singleTemplateFieldType().QUEST() != null
            }
        }else{
            val vars = ArrayList<Var<*>>()
            for (type in ctx.unionTemplateFieldType().type()){
                val t = MCFPPType.parseFromContext(type, typeScope)?: run {
                    LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(ctx.singleTemplateFieldType().type().text))
                    MCFPPBaseType.Any
                }
                vars.add(
                    t.build(ctx.Identifier().text)
                )
            }
            UnionTypeVarConcrete(ctx.Identifier().text, (vars[0] as MCFPPValue<*>).value, *vars.toTypedArray()).apply {
                nullable = ctx.unionTemplateFieldType().QUEST() != null
            }
        }
        //是否是静态的
        `var`.isStatic = isStatic
        if (DataTemplate.currTemplate!!.field.containVar(ctx.Identifier().text)
        ) {
            LogProcessor.error("Duplicate defined variable name:" + ctx.Identifier().text)
            return null to null
        }
        //属性访问器
        currVar = `var`
        val properties = (ctx.accessor()?.let {visit(ctx.accessor())}?: Property.buildSimpleProperty(`var`)) as Property
        return `var` to properties
    }

    override fun visitTemplateConstructorDeclaration(ctx: mcfppParser.TemplateConstructorDeclarationContext): Any {
        Project.ctx = ctx
        //类构造函数
        //创建构造函数对象，注册函数
        val f = DataTemplateConstructor(DataTemplate.currTemplate!!, ctx.functionBody())
        f.addParamsFromContext(ctx.normalParams())
        return f
    }

    //endregion
}