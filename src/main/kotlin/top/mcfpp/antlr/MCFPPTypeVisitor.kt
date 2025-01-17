package top.mcfpp.antlr

import net.querz.nbt.io.SNBTUtil
import net.querz.nbt.tag.IntTag
import top.mcfpp.Project
import top.mcfpp.core.lang.MCAny
import top.mcfpp.core.lang.MCFPPValue
import top.mcfpp.core.lang.Var
import top.mcfpp.io.MCFPPFile
import top.mcfpp.model.*
import top.mcfpp.model.Enum
import top.mcfpp.model.field.GlobalField
import top.mcfpp.model.generic.ClassParam
import top.mcfpp.model.generic.GenericClass
import top.mcfpp.model.generic.GenericObjectClass
import top.mcfpp.model.generic.ImplementedGenericClass
import top.mcfpp.util.LogProcessor
import top.mcfpp.util.StringHelper

/**
 * 解析当前项目的类型
 */
class MCFPPTypeVisitor: mcfppParserBaseVisitor<Unit>() {

    /**
     * 遍历整个文件。一个文件包含了命名空间的声明，函数的声明，类的声明以及全局变量的声明。全局变量是可以跨文件调用的。
     * @param ctx the parse tree
     * @return null
     */
    override fun visitCompilationUnit(ctx: mcfppParser.CompilationUnitContext) {
        Project.ctx = ctx
        //命名空间
        if (ctx.namespaceDeclaration() != null) {
            //获取命名空间
            val namespaceStr = ctx.namespaceDeclaration().Identifier().joinToString(".") { it.text }
            Project.currNamespace = namespaceStr
            MCFPPFile.currFile!!.namespace = namespaceStr
        }
        if(!GlobalField.localNamespaces.containsKey(Project.currNamespace)){
            GlobalField.localNamespaces[Project.currNamespace] = Namespace(Project.currNamespace)
        }
        //导入库
        for (lib in ctx.importDeclaration()){
            visitImportDeclaration(lib)
        }
        //文件结构，类和函数
        for (t in ctx.typeDeclaration()) {
            visitTypeDeclaration(t)
        }
    }

    /**
     * 完成一次库的import
     * TODO 类型别名
     *
     * @param ctx
     */
    override fun visitImportDeclaration(ctx: mcfppParser.ImportDeclarationContext) {
        Project.ctx = ctx
        //获取命名空间和导入类型
        val nsp = ctx.Identifier().joinToString(".") { it.text }
        val type = ctx.cls.text
        MCFPPFile.currFile!!.unsolvedImports[nsp] = type
    }

    /**
     * 类或函数声明
     * <pre>
     * `classOrFunctionDeclaration
     * :   classDeclaration
     * |   functionDeclaration
     * |   nativeDeclaration
     * ;`
    </pre> *
     * @param ctx the parse tree
     * @return null
     */
    override fun visitDeclarations(ctx: mcfppParser.DeclarationsContext){
        Project.ctx = ctx
        if (ctx.globalDeclaration() != null) return
        super.visitDeclarations(ctx)
    }

    override fun visitInterfaceDeclaration(ctx: mcfppParser.InterfaceDeclarationContext){
        Project.ctx = ctx
        //注册类
        val id = ctx.classWithoutNamespace().text
        val nsp = GlobalField.localNamespaces[Project.currNamespace]!!
        if (nsp.field.hasDeclaredType(id)) {
            //重复声明
            LogProcessor.error("Type has been defined: $id in namespace ${Project.currNamespace}")
            Interface.currInterface = nsp.field.getInterface(id)
        } else {
            //如果没有声明过这个类
            val itf = Interface(id, Project.currNamespace)
            for (p in ctx.className()){
                //是否存在继承
                val nsn = StringHelper.splitNamespaceID(p.text)
                val namespace  = nsn.first
                val identifier = nsn.second
                val pc = GlobalField.getInterface(namespace, identifier)
                if(pc == null){
                    LogProcessor.error("Undefined Interface: " + p.text)
                }else{
                    itf.extends(pc)
                }
            }
            nsp.field.addInterface(id, itf)
        }
    }

    /**
     * 类的声明
     * @param ctx the parse tree
     * @return null
     */
    override fun visitClassDeclaration(ctx: mcfppParser.ClassDeclarationContext){
        Project.ctx = ctx
        //注册类
        val id = ctx.classWithoutNamespace().text
        val nsp = GlobalField.localNamespaces[Project.currNamespace]!!

        val cls = if(ctx.readOnlyParams() != null){
            //泛型类
            val qwq = GenericClass(id, Project.currNamespace, ctx.classBody())
            qwq.readOnlyParams.addAll(ctx.readOnlyParams().parameterList().parameter().map {
                ClassParam(it.type().text, it.Identifier().text)
            })
            qwq
        } else {
            Class(id, Project.currNamespace)
        }
        //如果没有声明过这个类
        if(nsp.field.hasDeclaredType(cls)){
            LogProcessor.error("Type has been defined: $cls in namespace ${Project.currNamespace}")
            return
        }
        cls.initialize()
        if(ctx.className().size != 0){
            for (p in ctx.className()){
                //是否存在继承
                val qwq = StringHelper.splitNamespaceID(p.text)
                val identifier: String = qwq.second
                val namespace : String? = qwq.first
                var pc : CompoundData? = GlobalField.getClass(namespace, identifier)
                if(pc == null){
                    pc = GlobalField.getInterface(namespace, identifier)
                    if(pc == null){
                        pc = Class.Companion.UndefinedClassOrInterface(identifier,namespace)
                    }
                }
                cls.extends(pc)
            }
        }else{
            //继承Any类
            cls.extends(Class.baseClass)
        }
        cls.isStaticClass = ctx.STATIC() != null
        cls.isAbstract = ctx.ABSTRACT() != null
        nsp.field.addClass(cls.identifier, cls)
    }

    override fun visitObjectClassDeclaration(ctx: mcfppParser.ObjectClassDeclarationContext) {
        Project.ctx = ctx
        //注册类
        val id = ctx.classWithoutNamespace().text
        val nsp = GlobalField.localNamespaces[Project.currNamespace]!!

        val objectClass = if(ctx.readOnlyParams() != null){
            //泛型类
            val qwq = GenericObjectClass(id, Project.currNamespace, ctx.classBody())
            qwq.readOnlyParams.addAll(ctx.readOnlyParams().parameterList().parameter().map {
                ClassParam(it.type().text, it.Identifier().text)
            })
            qwq
        } else {
            ObjectClass(id, Project.currNamespace)
        }
        //如果没有声明过这个类
        if(nsp.field.hasObject(id)){
            LogProcessor.error("Type has been defined: $id in namespace ${Project.currNamespace}")
            return
        }
        if(ctx.className().size != 0){
            for (p in ctx.className()){
                //是否存在继承
                val qwq = StringHelper.splitNamespaceID(p.text)
                val identifier: String = qwq.second
                val namespace : String? = qwq.first
                var pc : CompoundData? = GlobalField.getClass(namespace, identifier)
                if(pc == null){
                    pc = GlobalField.getInterface(namespace, identifier)
                    if(pc == null){
                        pc = GlobalField.getObject(namespace, identifier)
                        if(pc !is ObjectClass){
                            LogProcessor.error("Undefined class: " + p.text)
                            pc = Class.Companion.UndefinedClassOrInterface(identifier,namespace)
                        }
                    }
                }
                objectClass.extends(pc)
            }
        }else{
            //继承Any类
            objectClass.extends(Class.baseClass)
        }
        nsp.field.addObject(objectClass.identifier, objectClass)
    }

    override fun visitGenericClassImplement(ctx: mcfppParser.GenericClassImplementContext) {
        Project.ctx = ctx
        //注册类
        val id = ctx.classWithoutNamespace().text

        val readOnlyArgs: ArrayList<Var<*>> = ArrayList()
        val exprVisitor = MCFPPExprVisitor()
        for (expr in ctx.readOnlyArgs().expressionList().expression()) {
            val arg = exprVisitor.visit(expr)!!
            if(arg !is MCFPPValue<*>){
                LogProcessor.error("Generic class implement must be a value")
                return
            }
            readOnlyArgs.add(arg)
        }

        val genericClass = GlobalField.getClass(Project.currNamespace, id)
        if(genericClass == null){
            LogProcessor.error("Undefined generic class: $id in namespace ${Project.currNamespace}")
            return
        }
        if(genericClass !is GenericClass){
            LogProcessor.error("Class $id is not a generic class")
            return
        }

        val cls = ImplementedGenericClass(id, Project.currNamespace, readOnlyArgs, genericClass)

        if(ctx.className().size != 0){
            for (p in ctx.className()){
                //是否存在继承
                val qwq = StringHelper.splitNamespaceID(p.text)
                val identifier: String = qwq.second
                val namespace : String? = qwq.first
                cls.extends(Class.Companion.UndefinedClassOrInterface(identifier,namespace))
            }
        }else{
            //继承Any类
            cls.extends(MCAny.data)
        }
        cls.isStaticClass = ctx.STATIC() != null
        cls.isAbstract = ctx.ABSTRACT() != null
    }

    override fun visitTemplateDeclaration(ctx: mcfppParser.TemplateDeclarationContext) {
        Project.ctx = ctx
        //注册模板
        val id = ctx.classWithoutNamespace().text
        val nsp = GlobalField.localNamespaces[Project.currNamespace]!!
        if (nsp.field.hasDeclaredType(id)) {
            //重复声明
            LogProcessor.error("Type has been defined: $id in namespace ${Project.currNamespace}")
            DataTemplate.currTemplate = nsp.field.getTemplate(id)
        }
        val template = DataTemplate(id,Project.currNamespace)
        template.extends(DataTemplate.baseDataTemplate)
        nsp.field.addTemplate(id, template)
    }

    /**
     *
     */
    override fun visitObjectTemplateDeclaration(ctx: mcfppParser.ObjectTemplateDeclarationContext){
        Project.ctx = ctx
        //注册模板
        val id = ctx.classWithoutNamespace().text
        val nsp = GlobalField.localNamespaces[Project.currNamespace]!!
        if (nsp.field.hasObject(id)) {
            //重复声明
            LogProcessor.error("Type has been defined: $id in namespace ${Project.currNamespace}")
            return
        }
        val template = ObjectDataTemplate(id,Project.currNamespace)
        template.extends(DataTemplate.baseDataTemplate)
        nsp.field.addObject(id, template)
    }

    override fun visitEnumDeclaration(ctx: mcfppParser.EnumDeclarationContext) {
        Project.ctx = ctx
        //注册枚举
        val id = ctx.Identifier().text
        val nsp = GlobalField.localNamespaces[Project.currNamespace]!!
        if (nsp.field.hasDeclaredType(id)) {
            //重复声明
            LogProcessor.error("Type has been defined: $id in namespace ${Project.currNamespace}")
        }
        val enum = Enum(id, Project.currNamespace)
        nsp.field.addEnum(id, enum)
        //添加成员
        for (m in ctx.enumBody().enumMember()) {
            val value = enum.getNextMemberValue()
            val data = m.nbtValue()?.let {SNBTUtil.fromSNBT(it.text)}
            val member = EnumMember(m.Identifier().text, value, data?:IntTag(0))
            enum.addMember(member)
        }
    }
}