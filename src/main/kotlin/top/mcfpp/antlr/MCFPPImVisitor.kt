package top.mcfpp.antlr

import org.antlr.v4.runtime.RuleContext
import top.mcfpp.Project
import top.mcfpp.annotations.InsertCommand
import top.mcfpp.antlr.RuleContextExtension.children
import top.mcfpp.antlr.mcfppParser.CompileTimeFuncDeclarationContext
import top.mcfpp.command.Command
import top.mcfpp.command.CommandList
import top.mcfpp.command.Commands
import top.mcfpp.core.lang.DataTemplateObjectConcrete
import top.mcfpp.core.lang.MCFPPValue
import top.mcfpp.core.lang.MCInt
import top.mcfpp.core.lang.Var
import top.mcfpp.core.lang.bool.BaseBool
import top.mcfpp.core.lang.bool.ExecuteBool
import top.mcfpp.core.lang.bool.ScoreBool
import top.mcfpp.core.lang.bool.ScoreBoolConcrete
import top.mcfpp.exception.VariableConverseException
import top.mcfpp.io.MCFPPFile
import top.mcfpp.lib.Execute
import top.mcfpp.lib.NBTPath
import top.mcfpp.model.*
import top.mcfpp.model.accessor.FunctionAccessor
import top.mcfpp.model.accessor.FunctionMutator
import top.mcfpp.model.accessor.Property
import top.mcfpp.model.field.GlobalField
import top.mcfpp.model.function.*
import top.mcfpp.model.function.Function
import top.mcfpp.model.function.FunctionParam.Companion.typeToStringList
import top.mcfpp.model.generic.Generic
import top.mcfpp.type.MCFPPBaseType
import top.mcfpp.type.MCFPPEnumType
import top.mcfpp.type.MCFPPGenericClassType
import top.mcfpp.type.MCFPPType
import top.mcfpp.util.LogProcessor
import top.mcfpp.util.TempPool
import top.mcfpp.util.TextTranslator
import top.mcfpp.util.TextTranslator.translate

open class MCFPPImVisitor: mcfppParserBaseVisitor<Any?>() {

    override fun visitTopStatement(ctx: mcfppParser.TopStatementContext): Any? {
        if(ctx.statement().size == 0) return null
        Function.currFunction = MCFPPFile.currFile!!.topFunction
        //注册函数
        GlobalField.localNamespaces[Project.currNamespace]!!.field.addFunction(Function.currFunction, force = false)
        super.visitTopStatement(ctx)
        Function.currFunction = Function.nullFunction
        return null
    }

    override fun visitFunctionDeclaration(ctx: mcfppParser.FunctionDeclarationContext): Any? {
        if(ctx.parent is CompileTimeFuncDeclarationContext) return null
        enterFunctionDeclaration(ctx)
        super.visitFunctionDeclaration(ctx)
        exitFunctionDeclaration(ctx)
        return null
    }

    private fun enterFunctionDeclaration(ctx: mcfppParser.FunctionDeclarationContext){
        Project.ctx = ctx
        val f: Function
        //获取函数对象
        val types = ctx.functionParams()?.let { FunctionParam.parseReadonlyAndNormalParamTypes(it) }
        //获取缓存中的对象
        f = GlobalField.getFunction(Project.currNamespace, ctx.Identifier().text, types?.first?.map { it.build("") }?:ArrayList(), types?.second?.map { it.build("") }?:ArrayList())
        Function.currFunction = f
    }

    private fun exitFunctionDeclaration(ctx: mcfppParser.FunctionDeclarationContext){
        Project.ctx = ctx
        //函数是否有返回值
        if(Function.currFunction !is Generic<*> && Function.currFunction.returnType !=  MCFPPBaseType.Void && !Function.currFunction.hasReturnStatement){
            LogProcessor.error("Function should return a value: " + Function.currFunction.namespaceID)
        }
        Function.currFunction = Function.nullFunction
        if (Class.currClass == null) {
            //不在类中
            Function.currFunction = Function.nullFunction
        } else {
            Function.currFunction = Class.currClass!!.classPreInit
        }
    }

    override fun visitFunctionBody(ctx: mcfppParser.FunctionBodyContext): Any? {
        if(ctx.parent is CompileTimeFuncDeclarationContext) return null
        if(Function.currFunction !is Generic<*>){
            super.visitFunctionBody(ctx)
        }
        return null
    }


    //泛型函数编译使用的入口
    fun visitFunctionBody(ctx: mcfppParser.FunctionBodyContext, function: Function){
        val lastFunction = Function.currFunction
        Function.currFunction = function
        super.visitFunctionBody(ctx)
        Function.currFunction = lastFunction
    }

    /**
     * 进入命名空间声明的时候
     * @param ctx the parse tree
     */
    override fun visitNamespaceDeclaration(ctx: mcfppParser.NamespaceDeclarationContext):Any? {
        Project.ctx = ctx
        Project.currNamespace = ctx.Identifier(0).text
        if(ctx.Identifier().size > 1){
            for (n in ctx.Identifier().subList(1,ctx.Identifier().size-1)){
                Project.currNamespace += ".$n"
            }
        }
        MCFPPFile.currFile!!.topFunction.namespace = Project.currNamespace
        return null
    }

    /**
     * 变量声明
     * @param ctx the parse tree
     */
    @InsertCommand
    override fun visitFieldDeclaration(ctx: mcfppParser.FieldDeclarationContext):Any? {
        Project.ctx = ctx
        //变量生成
        val fieldModifier = ctx.fieldModifier()?.text
        if (ctx.parent is mcfppParser.ClassMemberContext) {
            return null
        }
        if(ctx.VAR() != null){
            //自动判断类型
            val init: Var<*> = MCFPPExprVisitor().visit(ctx.expression())!!
            var `var` = if(fieldModifier == "import"){
                val qwq = init.type.buildUnConcrete(ctx.Identifier().text, Function.currFunction)
                qwq.hasAssigned = true
                qwq
            }else{
                init.type.build(ctx.Identifier().text, Function.currFunction)
            }
            `var`.nbtPath = NBTPath.getNormalStackPath(`var`)
            //变量赋值
            `var` = `var`.assignedBy(init)
            //一定是函数变量
            if (!Function.field.putVar(ctx.Identifier().text, `var`, false)) {
                LogProcessor.error("Duplicate defined variable name:" + ctx.Identifier().text)
            }
            when(fieldModifier){
                "const" -> {
                    if(!`var`.hasAssigned){
                        LogProcessor.error("The const field ${`var`.identifier} must be initialized.")
                    }
                    `var`.isConst = true
                }
                "dynamic" -> {
                    if(`var` is MCFPPValue<*>){
                        `var`.toDynamic(true)
                    }
                }
            }
        }else{
            //获取类型
            val type = MCFPPType.parseFromContext(ctx.type(), Function.currFunction.field)?: run {
                LogProcessor.error(TextTranslator.INVALID_TYPE_ERROR.translate(ctx.type().text))
                MCFPPBaseType.Any
            }
            for (c in ctx.fieldDeclarationExpression()){
                //函数变量，生成
                var `var` = if(fieldModifier == "import"){
                    val qwq = type.buildUnConcrete(c.Identifier().text, Function.currFunction)
                    qwq.hasAssigned = true
                    qwq
                }else{
                    type.build(c.Identifier().text, Function.currFunction)
                }
                //变量注册
                //一定是函数变量
                if (Function.field.containVar(c.Identifier().text)) {
                    LogProcessor.error("Duplicate defined variable name:" + c.Identifier().text)
                }
                Function.addComment("field: " + ctx.type().text + " " + c.Identifier().text + if (c.expression() != null) " = " + c.expression().text else "")
                `var`.nbtPath = NBTPath.getNormalStackPath(`var`)
                //变量初始化
                if (c.expression() != null) {
                    val init: Var<*> = MCFPPExprVisitor(if(type is MCFPPGenericClassType) type else null, if(type is MCFPPEnumType) type else null).visit(c.expression())!!
                    `var` = `var`.assignedBy(init)
                }
                when(fieldModifier){
                    "const" -> {
                        if(!`var`.hasAssigned){
                            LogProcessor.error("The const field ${`var`.identifier} must be initialized.")
                        }
                        `var`.isConst = true
                    }
                    "dynamic" -> {
                        if(`var` is MCFPPValue<*> && `var`.hasAssigned){
                            `var` = `var`.toDynamic(false)
                        }else if(`var` is MCFPPValue<*>){
                            `var` = type.buildUnConcrete(c.Identifier().text, Function.currFunction)
                        }
                    }
                }
                Function.field.putVar(`var`.identifier, `var`, true)
            }
        }
        return null
    }

    /**
     * 一个赋值的语句
     * @param ctx the parse tree
     */
    @InsertCommand
    override fun visitStatementExpression(ctx: mcfppParser.StatementExpressionContext):Any? {
        Project.ctx = ctx
        Function.addComment("expression: " + ctx.text)
        if(ctx.varWithSelector() != null){
            val left: Var<*> = McfppLeftExprVisitor().visit(ctx.varWithSelector())
            if (left.isConst) {
                LogProcessor.error("Cannot assign a constant repeatedly: " + left.identifier)
                return null
            }
            val type = left.type
            val right: Var<*> = MCFPPExprVisitor(if(type is MCFPPGenericClassType) type else null, if(type is MCFPPEnumType) type else null).visit(ctx.expression())!!
            try {
                if(right !is MCFPPValue<*> && left.parent is DataTemplateObjectConcrete){
                    left.parent = (left.parent as DataTemplateObjectConcrete).toDynamic(true)
                }
                left.replacedBy(left.assignedBy(right))
            } catch (e: VariableConverseException) {
                LogProcessor.error("Cannot convert " + right.javaClass + " to " + left.javaClass)
                throw e
            }
        }else{
            MCFPPExprVisitor().visit(ctx.expression())!!
        }
        Function.addComment("expression end: " + ctx.text)
        return null
    }

    override fun visitExtensionFunctionDeclaration(ctx: mcfppParser.ExtensionFunctionDeclarationContext): Any? {
        //是扩展函数
        enterExtensionFunctionDeclaration(ctx)
        super.visitExtensionFunctionDeclaration(ctx)
        exitExtensionFunctionDeclaration(ctx)

        return null
    }

    fun enterExtensionFunctionDeclaration(ctx: mcfppParser.ExtensionFunctionDeclarationContext) {
        val f: Function
        val data: CompoundData = if (ctx.type().typeWithoutExcl().className() == null) {
            when (ctx.type().text) {
                "int" -> MCInt.data
                else -> {
                    throw Exception("Cannot add extension function to ${ctx.type().text}")
                }
            }
        } else {
            val clsStr = ctx.type().typeWithoutExcl().className().text.split(":")
            val id: String
            val nsp: String?
            if (clsStr.size == 1) {
                id = clsStr[0]
                nsp = null
            } else {
                id = clsStr[1]
                nsp = clsStr[0]
            }
            val owo: Class? = GlobalField.getClass(nsp, id)
            if (owo == null) {
                val pwp = GlobalField.getTemplate(nsp, id)
                if (pwp == null) {
                    LogProcessor.error("Undefined class or struct:" + ctx.type().typeWithoutExcl().className().text)
                    f = UnknownFunction(ctx.Identifier().text)
                    Function.currFunction = f
                    return
                } else {
                    pwp
                }
            } else {
                owo
            }
        }
        //解析参数
        val types = FunctionParam.parseReadonlyAndNormalParamTypes(ctx.functionParams())
        val field = data.field
        //获取缓存中的对象
        f = field.getFunction(ctx.Identifier().text, types.first.map { it.build("") }, types.second.map { it.build("") })

        Function.currFunction = f
    }

    fun exitExtensionFunctionDeclaration(ctx: mcfppParser.ExtensionFunctionDeclarationContext) {
        Project.ctx = ctx
        //函数是否有返回值
        if (Function.currFunction.returnType != MCFPPBaseType.Void && !Function.currFunction.hasReturnStatement) {
            LogProcessor.error("A 'return' expression required in function: " + Function.currFunction.namespaceID)
        }
        Function.currFunction = Function.nullFunction
    }

//region 逻辑语句
    
    @InsertCommand
    override fun visitReturnStatement(ctx: mcfppParser.ReturnStatementContext):Any? {
        Project.ctx = ctx
        Function.addComment(ctx.text)
        if (ctx.expression() != null) {
            val ret: Var<*> = MCFPPExprVisitor().visit(ctx.expression())!!
            Function.currBaseFunction.assignReturnVar(ret)
        }
        if(Function.currFunction !is InternalFunction)
            Function.currFunction.hasReturnStatement = true
        Function.addCommand("return 1")
        return null
    }

    override fun visitIfStatement(ctx: mcfppParser.IfStatementContext): Any? {
        enterIfStatement(ctx)
        super.visitIfStatement(ctx)
        exitIfStatement(ctx)
        return null
    }

    /**
     * 进入if语句
     * Enter if statement
     *
     * @param ctx
     */
    @InsertCommand
    @Suppress("UNCHECKED_CAST")
    fun enterIfStatement(ctx: mcfppParser.IfStatementContext) {
        //进入if函数
        Project.ctx = ctx
        Function.addComment("if start")
        /*
        ifStatement
            :   IF'('expression')' ifBlock elseIfStatement* elseStatement?
            ;

        elseIfStatement
            :   ELSE IF '('expression')' ifBlock
            ;

        elseStatement
            :   ELSE ifBlock
            ;

        ifBlock
            :   block
            ;
         */
        //将if以后的语句插入到if的分支后面
        val index = ctx.parent.parent.children().indexOf(ctx.parent)
        val list = ctx.parent.parent.children().subList(index + 1, ctx.parent.parent.children().size) as List<mcfppParser.StatementContext>
        list.forEach { ctx.ifBlock().block().addChild(it) }
        list.forEach { l -> ctx.elseIfStatement().forEach { it.ifBlock().block().addChild(l) } }
        list.forEach { ctx.elseStatement()?.ifBlock()?.block()?.addChild(it) }
    }

    /**
     * 离开if语句
     * Exit if statement
     *
     * @param ctx
     */
    @InsertCommand
    fun exitIfStatement(ctx: mcfppParser.IfStatementContext) {
        Project.ctx = ctx
        //Function.currFunction = Function.currFunction.parent[0]
        Function.addComment("if end")
        //if以后的语句已经被全部打包到if分支里面，所以if语句之后的statement没有意义
        if(ctx.elseStatement() != null){
            Function.currFunction.isEnded = true
        }
    }

    override fun visitIfBlock(ctx: mcfppParser.IfBlockContext): Any? {
        enterIfBlock(ctx)
        super.visitIfBlock(ctx)
        exitIfBlock(ctx)
        return null
    }

    /**
     * 进入if分支的语句块
     * @param ctx the parse tree
     */

    @InsertCommand
    fun enterIfBlock(ctx: mcfppParser.IfBlockContext) {
        Project.ctx = ctx
        val parent = ctx.parent
        Function.addComment("if branch start")
        //匿名函数的定义
        val f = NoStackFunction(TempPool.getFunctionIdentify("if_branch"), Function.currFunction)
        //注册函数
        if(!GlobalField.localNamespaces.containsKey(f.namespace))
            GlobalField.localNamespaces[f.namespace] = Namespace(f.namespace)
        GlobalField.localNamespaces[f.namespace]!!.field.addFunction(f,false)
        if (parent is mcfppParser.IfStatementContext || parent is mcfppParser.ElseIfStatementContext) {
            //if()，需要进行条件计算
            parent as mcfppParser.IfStatementContext
            when(val exp = MCFPPExprVisitor().visit(parent.expression())){
                is ScoreBoolConcrete -> {
                    if (exp.value) {
                        //函数调用的命令
                        //给子函数开栈
                        Function.addCommand("function " + f.namespaceID)
                        LogProcessor.warn("The condition is always true. ")
                    } else {
                        Function.addComment("function " + f.namespaceID)
                        LogProcessor.warn("The condition is always false. ")
                    }
                }

                is ExecuteBool -> {
                    //给子函数开栈
                    Function.addCommand(
                        Command("execute").build(exp.toCommandPart()).build("run return run").build(Commands.function(f))
                    )
                }

                is BaseBool -> {
                    Function.addCommand(
                        Command("execute if").build(exp.toCommandPart()).build("run return run").build(Commands.function(f))
                    )
                }

                else -> {
                    LogProcessor.error("The condition must be a boolean expression.")
                    Function.addComment("[error/The condition must be a boolean expression]function " + f.namespaceID)
                }
            }
        }
        else {
            //else语句
            Function.addCommand("function " + f.namespaceID)
        }
        Function.currFunction = f
    }

    /**
     * 离开if语句块
     * @param ctx the parse tree
     */
    
    @InsertCommand
    fun exitIfBlock(ctx: mcfppParser.IfBlockContext) {
        Project.ctx = ctx
        //由于原来的调用if的函数已经被return命令返回，需要if_branch函数帮助清理它的栈
        Function.addCommand("data remove storage mcfpp:system default.stack_frame[0]")
        Function.currFunction = Function.currFunction.parent[0]
        Function.addComment("if branch end")
    }

    override fun visitWhileStatement(ctx: mcfppParser.WhileStatementContext): Any? {
        enterWhileStatement(ctx)
        super.visitWhileStatement(ctx)
        exitWhileStatement(ctx)
        return null
    }

    @InsertCommand
    fun enterWhileStatement(ctx: mcfppParser.WhileStatementContext) {
        //进入if函数
        Project.ctx = ctx
        Function.addComment("while start")
        val whileFunction = InternalFunction("_while_", Function.currFunction)
        Function.addCommand("function " + whileFunction.namespaceID)
        Function.addCommand("data remove storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame[0]")
        Function.currFunction = whileFunction
        if(!GlobalField.localNamespaces.containsKey(whileFunction.namespace))
            GlobalField.localNamespaces[whileFunction.namespace] = Namespace(whileFunction.namespace)
        GlobalField.localNamespaces[whileFunction.namespace]!!.field.addFunction(whileFunction,false)
    }

    
    @InsertCommand
    fun exitWhileStatement(ctx: mcfppParser.WhileStatementContext) {
        Project.ctx = ctx
        Function.currFunction = Function.currFunction.parent[0]
        //调用完毕，将子函数的栈销毁
        Function.addComment("while end")
    }


    override fun visitWhileBlock(ctx: mcfppParser.WhileBlockContext): Any? {
        enterWhileBlock(ctx)
        super.visitWhileBlock(ctx)
        exitWhileBlock(ctx)
        return null
    }

    /**
     * 进入while语句块
     * @param ctx the parse tree
     */
    
    @InsertCommand
    fun enterWhileBlock(ctx: mcfppParser.WhileBlockContext) {
        Project.ctx = ctx
        //入栈
        Function.addCommand("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
        Function.addComment("while start")
        val parent: mcfppParser.WhileStatementContext = ctx.parent as mcfppParser.WhileStatementContext
        val exp = MCFPPExprVisitor().visit(parent.expression())
        //匿名函数的定义
        val f: Function = InternalFunction("_while_block_", Function.currFunction)
        f.child.add(f)
        f.parent.add(f)
        if(!GlobalField.localNamespaces.containsKey(f.namespace))
            GlobalField.localNamespaces[f.namespace] = Namespace(f.namespace)
        GlobalField.localNamespaces[f.namespace]!!.field.addFunction(f,false)
        //条件判断
        when(exp){
            is ScoreBoolConcrete -> {
                if(exp.value){
                    //给子函数开栈
                    Function.addCommand("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
                    Function.addCommand("execute " +
                            "if function " + f.namespaceID + " " +
                            "run function " + Function.currFunction.namespaceID)
                }else{
                    Function.addComment("function " + f.namespaceID)
                }
            }

            is ExecuteBool -> {
                //给子函数开栈
                Function.addCommand("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
                Function.addCommand(Command("execute")
                    .build(exp.toCommandPart())
                    .build("if function " + f.namespaceID)
                    .build("run function " + Function.currFunction.namespaceID))
            }

            is BaseBool -> {
                //给子函数开栈
                Function.addCommand("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
                Function.addCommand(Command("execute")
                    .build("if").build(exp.toCommandPart())
                    .build("if function " + f.namespaceID)
                    .build("run function " + Function.currFunction.namespaceID)
                )
            }

            else -> {
                LogProcessor.error("The condition must be a boolean expression.")
                Function.addComment("[error/The condition must be a boolean expression]function " + f.namespaceID)
            }
        }
        Function.currFunction = f //后续块中的命令解析到递归的函数中

    }

    /**
     * 离开while语句块
     * @param ctx the parse tree
     */
    
    @InsertCommand
    fun exitWhileBlock(ctx: mcfppParser.WhileBlockContext) {
        Project.ctx = ctx
        //调用完毕，将子函数的栈销毁
        //由于在同一个命令中完成了两个函数的调用，因此需要在子函数内部进行子函数栈的销毁工作
        Function.addCommand("data remove storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame[0]")
        //这里取出while函数的栈
        Function.addCommand("data remove storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame[0]")
        Function.addCommand("return 1")
        Function.currFunction = Function.currFunction.parent[0]
        Function.addComment("while loop end")
        Function.currFunction.field.forEachVar {
            if(it.trackLost){
                it.trackLost = false
                if(it is MCFPPValue<*>) it.toDynamic(true)
            }
        }
    }

    override fun visitDoWhileStatement(ctx: mcfppParser.DoWhileStatementContext): Any? {
        enterDoWhileStatement(ctx)
        super.visitDoWhileStatement(ctx)
        exitDoWhileStatement(ctx)
        return null
    }

    private lateinit var doWhileFunction: InternalFunction
    @InsertCommand
    fun enterDoWhileStatement(ctx: mcfppParser.DoWhileStatementContext) {
        //进入do-while函数
        Project.ctx = ctx
        Function.addComment("do-while start")
        doWhileFunction = InternalFunction("_dowhile_", Function.currFunction)
        Function.currFunction = doWhileFunction
        if(!GlobalField.localNamespaces.containsKey(doWhileFunction.namespace))
            GlobalField.localNamespaces[doWhileFunction.namespace] = Namespace(doWhileFunction.namespace)
        GlobalField.localNamespaces[doWhileFunction.namespace]!!.field.addFunction(doWhileFunction,false)
    }



    /**
     * 离开do-while语句
     * @param ctx the parse tree
     */
    
    @InsertCommand
    fun exitDoWhileStatement(ctx: mcfppParser.DoWhileStatementContext) {
        Project.ctx = ctx
        Function.currFunction = Function.currFunction.parent[0]
        //调用完毕，将子函数的栈销毁
        Function.addComment("do-while end")
    }


    override fun visitDoWhileBlock(ctx: mcfppParser.DoWhileBlockContext): Any? {
        enterDoWhileBlock(ctx)
        super.visitDoWhileBlock(ctx)
        exitDoWhileBlock(ctx)
        return null
    }

    /**
     * 进入do-while语句块，开始匿名函数调用
     * @param ctx the parse tree
     */
    
    @InsertCommand
    fun enterDoWhileBlock(ctx: mcfppParser.DoWhileBlockContext) {
        Project.ctx = ctx
        Function.addComment("do while start")
        //匿名函数的定义
        val f: Function = InternalFunction("_dowhile_", Function.currFunction)
        f.child.add(f)
        f.parent.add(f)
        if(!GlobalField.localNamespaces.containsKey(f.namespace)) {
            GlobalField.localNamespaces[f.namespace] = Namespace(f.namespace)
        }
        GlobalField.localNamespaces[f.namespace]!!.field.addFunction(f,false)
        //给子函数开栈
        Function.currFunction.parent[0].commands.add("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
        Function.currFunction.parent[0].commands.add(
            "execute " +
                    "unless function " + f.namespaceID + " " +
                    "run return 1"
        )
        Function.currFunction.parent[0].commands.add("data remove storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame[0]")
        Function.currFunction.parent[0].commands.add("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
        Function.currFunction.parent[0].commands.add("function " + doWhileFunction.namespaceID)
        Function.currFunction.parent[0].commands.add("data remove storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame[0]")
        //递归调用
        val parent = ctx.parent as mcfppParser.DoWhileStatementContext
        when(val exp = MCFPPExprVisitor().visit(parent.expression())){
            is ScoreBoolConcrete -> {
                if(exp.value){
                    //给子函数开栈
                    Function.addCommand("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
                    Function.addCommand("execute " +
                            "if function " + f.namespaceID + " " +
                            "run function " + Function.currFunction.namespaceID)
                    LogProcessor.warn("The condition is always true. ")
                }else{
                    Function.addComment("function " + f.namespaceID)
                    LogProcessor.warn("The condition is always false. ")
                }
            }

            is ExecuteBool -> {
                //给子函数开栈
                Function.addCommand("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
                Function.addCommand(Command("execute")
                    .build(exp.toCommandPart())
                    .build("if function " + f.namespaceID)
                    .build("run function " + Function.currFunction.namespaceID))
            }

            is BaseBool -> {
                //给子函数开栈
                Function.addCommand("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
                Function.addCommand(Command("execute")
                    .build("if").build(exp.toCommandPart())
                    .build("if function " + f.namespaceID)
                    .build("run function " + Function.currFunction.namespaceID)
                )
            }

            else -> {
                LogProcessor.error("The condition must be a boolean expression.")
                Function.addComment("[error/The condition must be a boolean expression]function " + f.namespaceID)
            }
        }
        Function.currFunction = f //后续块中的命令解析到递归的函数中
    }

    
    @InsertCommand
    fun exitDoWhileBlock(ctx: mcfppParser.DoWhileBlockContext) {
        Project.ctx = ctx
        //调用完毕，将子函数的栈销毁
        Function.addCommand("data remove storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame[0]")
        //返回1
        Function.addCommand("return 1")
        Function.currFunction = Function.currFunction.parent[0]
        Function.addComment("do while end")
        Function.currFunction.field.forEachVar {
            if(it.trackLost){
                it.trackLost = false
                if(it is MCFPPValue<*>) it.toDynamic(true)
            }
        }
    }


    override fun visitForStatement(ctx: mcfppParser.ForStatementContext): Any? {
        enterForStatement(ctx)
        super.visitForStatement(ctx)
        exitForStatement(ctx)
        return null
    }

    /**
     * 整个for语句本身额外有一个栈，无条件调用函数
     * @param ctx the parse tree
     */
    
    @InsertCommand
    fun enterForStatement(ctx: mcfppParser.ForStatementContext) {
        Project.ctx = ctx
        Function.addComment("for start")
        //for语句对应的函数
        val forFunc: Function = InternalFunction("_for_", Function.currFunction)
        forFunc.parent.add(Function.currFunction)
        if(!GlobalField.localNamespaces.containsKey(forFunc.namespace))
            GlobalField.localNamespaces[forFunc.namespace] = Namespace(forFunc.identifier)
        GlobalField.localNamespaces[forFunc.namespace]!!.field.addFunction(forFunc,false)
        Function.addCommand("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
        Function.addCommand(Commands.function(forFunc))
        Function.addCommand("data remove storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame[0]")
        Function.currFunction = forFunc
    }

    
    @InsertCommand
    fun exitForStatement(ctx: mcfppParser.ForStatementContext) {
        Project.ctx = ctx
        Function.currFunction = Function.currFunction.parent[0]
        Function.addComment("for end")
    }

    override fun visitForInit(ctx: mcfppParser.ForInitContext): Any? {
        enterForInit(ctx)
        super.visitForInit(ctx)
        exitForInit(ctx)
        return null
    }
    
    @InsertCommand
    fun enterForInit(ctx: mcfppParser.ForInitContext) {
        Project.ctx = ctx
        Function.addComment("for init start")
    }

    
    @InsertCommand
    fun exitForInit(ctx: mcfppParser.ForInitContext) {
        Project.ctx = ctx
        Function.addComment("for init end")
        //进入for循环主体
        Function.addComment("for loop start")
        val forLoopFunc: Function = InternalFunction("_for_loop_", Function.currFunction)
        forLoopFunc.parent.add(Function.currFunction)
        if(!GlobalField.localNamespaces.containsKey(forLoopFunc.namespace))
            GlobalField.localNamespaces[forLoopFunc.namespace] = Namespace(forLoopFunc.identifier)
        GlobalField.localNamespaces[forLoopFunc.namespace]!!.field.addFunction(forLoopFunc,false)
        Function.addCommand("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
        Function.addCommand(Commands.function(forLoopFunc))
        Function.currFunction = forLoopFunc
    }

    override fun visitForBlock(ctx: mcfppParser.ForBlockContext): Any? {
        enterForBlock(ctx)
        super.visitForBlock(ctx)
        exitForBlock(ctx)
        return null
    }

    /**
     * 进入for block语句。此时当前函数为父函数
     * @param ctx the parse tree
     */
    @InsertCommand
    fun enterForBlock(ctx: mcfppParser.ForBlockContext) {
        Project.ctx = ctx
        //currFunction 是 forLoop
        val parent: mcfppParser.ForStatementContext = ctx.parent as mcfppParser.ForStatementContext
        val exp = MCFPPExprVisitor().visit(parent.forControl().expression())
        //匿名函数的定义。这里才是正式的for函数哦喵
        val f: Function = InternalFunction("_forblock_", Function.currFunction)
        f.child.add(f)
        f.parent.add(f)
        if(!GlobalField.localNamespaces.containsKey(f.namespace))
            GlobalField.localNamespaces[f.namespace] = Namespace(f.namespace)
        GlobalField.localNamespaces[f.namespace]!!.field.addFunction(f,false)
        //条件循环判断
        when(exp){
            is ScoreBoolConcrete -> {
                if(exp.value){
                    //给子函数开栈
                    Function.addCommand("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
                    Function.addCommand("execute " +
                            "if function " + f.namespaceID + " " +
                            "run function " + Function.currFunction.namespaceID)
                    LogProcessor.warn("The condition is always true. ")
                }else{
                    Function.addComment("function " + f.namespaceID)
                    LogProcessor.warn("The condition is always false. ")
                }
            }

            is ExecuteBool -> {
                //给子函数开栈
                Function.addCommand("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
                Function.addCommand(
                    Command("execute")
                        .build(exp.toCommandPart())
                        .build("if function " + f.namespaceID)
                        .build("run function " + Function.currFunction.namespaceID)
                )
            }

            is BaseBool -> {
                //给子函数开栈
                Function.addCommand("data modify storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame prepend value {}")
                Function.addCommand(
                    Command("execute")
                        .build("if").build(exp.toCommandPart())
                        .build("if function " + f.namespaceID)
                        .build("run function " + Function.currFunction.namespaceID)
                )
            }

            else -> {
                LogProcessor.error("The condition must be a boolean expression.")
                Function.addComment("[error/The condition must be a boolean expression]function " + f.namespaceID)
            }
        }
        //调用完毕，将子函数的栈销毁。这条命令仍然是在for函数中的。
        Function.addCommand("data remove storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame[0]")
        Function.currFunction = f //后续块中的命令解析到递归的函数中
    }

    /**
     * 离开for block语句。此时当前函数仍然是for的函数
     * @param ctx the parse tree
     */

    @InsertCommand
    fun exitForBlock(ctx: mcfppParser.ForBlockContext) {
        Project.ctx = ctx
        //for-update的命令压入
        Function.currFunction.commands.addAll(forUpdateCommands)
        forUpdateCommands.clear()
        //调用完毕，将子函数的栈销毁
        Function.addCommand("data remove storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame[0]")
        //继续销毁for-loop函数的栈
        Function.addCommand("data remove storage mcfpp:system " + Project.config.rootNamespace + ".stack_frame[0]")
        Function.addCommand("return 1")
        Function.currFunction = Function.currFunction.parent[0]
        Function.currFunction.field.forEachVar {
            if(it.trackLost){
                it.trackLost = false
                if(it is MCFPPValue<*>) it.toDynamic(true)
            }
        }
    }

    /**
     * 进入for update语句块。
     * 由于在编译过程中，编译器会首先编译for语句的for control部分，也就是for后面的括号，这就意味着forUpdate语句将会先forBlock
     * 被写入到命令函数中。因此我们需要将forUpdate语句中的命令临时放在一个列表内部，然后在forBlock调用完毕后加上它的命令
     *
     * @param ctx the parse tree
     */

    override fun visitForUpdate(ctx: mcfppParser.ForUpdateContext): Any? {
        enterForUpdate(ctx)
        super.visitForUpdate(ctx)
        exitForUpdate(ctx)
        return null
    }

    fun enterForUpdate(ctx: mcfppParser.ForUpdateContext) {
        Project.ctx = ctx
        forInitCommands = Function.currFunction.commands
        Function.currFunction.commands = forUpdateCommands
    }

    //暂存
    private var forInitCommands = CommandList()
    private var forUpdateCommands = CommandList()

    /**
     * 离开for update。暂存for update缓存，恢复主缓存，准备forblock编译
     * @param ctx the parse tree
     */
    
    fun exitForUpdate(ctx: mcfppParser.ForUpdateContext) {
        Project.ctx = ctx
        Function.currFunction.commands = forInitCommands
    }

//endregion

    /**
     * 使用原版命令
     */
    @InsertCommand
    override fun visitOrgCommand(ctx: mcfppParser.OrgCommandContext):Any? {
        Project.ctx = ctx
        val sb = StringBuilder()
        for (content in ctx.orgCommandContent()){
            if(content.OrgCommandText() != null){
                sb.append(content.OrgCommandText().text)
            }else{
                val exp = MCFPPExprVisitor().visit(content.orgCommandExpression().expression())
                if(exp is MCFPPValue<*>){
                    sb.append(exp.value)
                }else{
                    sb.append(exp)
                }
            }
        }
        Function.addCommand(sb.toString())
        return null
    }

    /**
     * 进入任意语句，检查此函数是否还能继续添加语句
     * @param ctx the parse tree
     */

    @InsertCommand
    override fun visitStatement(ctx: mcfppParser.StatementContext): Any? {
        Project.ctx = ctx
        if (Function.currFunction.isReturned) {
            LogProcessor.warn("Unreachable code: " + ctx.text)
            return null
        }
        if(Function.currFunction.isEnded){
            return null
        }
        super.visitStatement(ctx)
        return null
    }

    private var temp: ScoreBool? = null
    
    @InsertCommand
    override fun visitControlStatement(ctx: mcfppParser.ControlStatementContext):Any? {
        Project.ctx = ctx
        if (!inLoopStatement(ctx)) {
            LogProcessor.error("'continue' or 'break' can only be used in loop statements.")
            return null
        }
        Function.addComment(ctx.text)
        //return语句
        if(ctx.BREAK() != null){
            //break，完全跳出while
            Function.addCommand("return 0")
        }else{
            //continue，跳过当次
            Function.addCommand("return 1")
        }
        Function.currFunction.isReturned = true
        return null
    }

    //进入execute语句
    override fun visitExecuteStatement(ctx: mcfppParser.ExecuteStatementContext): Any? {
        val exec = Execute()
        for (context in ctx.executeContext()){
            var arg: Execute.WriteOnlyVar? = null
            for (exp in context.executeExpression().`var`()){
                if(arg == null){
                    val qwq = exec.data.field.getVar(exp.text) as Execute.WriteOnlyVar?
                    if(qwq == null){
                        LogProcessor.error("Cannot find argument: ${exp.text}")
                        continue
                    }
                    arg = qwq
                }else{
                    arg = arg.getData().field.getVar(exp.text) as Execute.WriteOnlyVar
                }
            }
            val value = MCFPPExprVisitor().visit(context.expression())
            arg?.assignedBy(value)
        }
        val execFunction = NoStackFunction(TempPool.getFunctionIdentify("execute"), Function.currFunction)
        GlobalField.localNamespaces[execFunction.namespace]!!.field.addFunction(execFunction, false)
        val l = Function.currFunction
        Function.currFunction = execFunction
        super.visitExecuteStatement(ctx)
        Function.currFunction = l
        Function.addCommand(exec.run(execFunction))
        return null
    }

    //region class
    override fun visitClassDeclaration(ctx: mcfppParser.ClassDeclarationContext): Any? {
        if(ctx.readOnlyParams() == null){
            super.visitClassDeclaration(ctx)
        }
        return null
    }

    override fun visitClassBody(ctx: mcfppParser.ClassBodyContext): Any? {
        enterClassBody(ctx)
        super.visitClassBody(ctx)
        exitClassBody(ctx)
        return null
    }

    /**
     * 进入类体。
     * @param ctx the parse tree
     */
    private fun enterClassBody(ctx: mcfppParser.ClassBodyContext) {
        Project.ctx = ctx
        //获取类的对象
        val parent = ctx.parent
        if(parent is mcfppParser.ClassDeclarationContext){
            val identifier = parent.classWithoutNamespace().text
            Class.currClass = GlobalField.getClass(Project.currNamespace, identifier)
        }else{
            parent as mcfppParser.ObjectClassDeclarationContext
            val identifier = parent.classWithoutNamespace().text
            Class.currClass = GlobalField.getObject(Project.currNamespace, identifier) as ObjectClass
        }
        //设置作用域
        Function.currFunction = Class.currClass!!.classPreInit
    }

    /**
     * 离开类体。将缓存重新指向全局
     * @param ctx the parse tree
     */

    private fun exitClassBody(ctx: mcfppParser.ClassBodyContext) {
        Project.ctx = ctx
        Class.currClass = null
        Function.currFunction = Function.nullFunction
    }

    override fun visitClassFunctionDeclaration(ctx: mcfppParser.ClassFunctionDeclarationContext): Any? {
        //是类的成员函数
        enterClassFunctionDeclaration(ctx)
        super.visitClassFunctionDeclaration(ctx)
        exitClassFunctionDeclaration(ctx)
        return null
    }

    private fun enterClassFunctionDeclaration(ctx: mcfppParser.ClassFunctionDeclarationContext) {
        Project.ctx = ctx
        //解析参数
        val types = FunctionParam.parseReadonlyAndNormalParamTypes(ctx.functionParams())
        //获取缓存中的对象
        val f = Class.currClass!!.field.getFunction(ctx.Identifier().text, types.first.map { it.build("") }, types.second.map { it.build("") })
        Function.currFunction = f
    }

    private fun exitClassFunctionDeclaration(ctx: mcfppParser.ClassFunctionDeclarationContext) {
        Project.ctx = ctx
        Function.currFunction = Class.currClass!!.classPreInit
    }

    override fun visitClassConstructorDeclaration(ctx: mcfppParser.ClassConstructorDeclarationContext): Any? {
        //是构造函数
        enterClassConstructorDeclaration(ctx)
        super.visitClassConstructorDeclaration(ctx)
        exitClassConstructorDeclaration(ctx)
        return null
    }

    private fun enterClassConstructorDeclaration(ctx: mcfppParser.ClassConstructorDeclarationContext) {
        Project.ctx = ctx
        val types = FunctionParam.parseNormalParamTypes(ctx.normalParams())
        val c = Class.currClass!!.getConstructorByString(types.typeToStringList())!!
        Function.currFunction = c
    }

    private fun exitClassConstructorDeclaration(ctx: mcfppParser.ClassConstructorDeclarationContext) {
        Project.ctx = ctx
        Function.currFunction = Class.currClass!!.classPreInit
    }

    private lateinit var currProperty: Property
    override fun visitClassFieldDeclaration(ctx: mcfppParser.ClassFieldDeclarationContext): Any? {
        val id = ctx.fieldDeclarationExpression().Identifier().text
        currProperty = Class.currClass!!.field.getProperty(id)!!
        return super.visitClassFieldDeclaration(ctx)
    }

    override fun visitGetter(ctx: mcfppParser.GetterContext): Any? {
        if(ctx.functionBody() != null){
            Function.currFunction = (currProperty.accessor as FunctionAccessor).function
        }
        return super.visitGetter(ctx)
    }

    override fun visitSetter(ctx: mcfppParser.SetterContext?): Any? {
        if(ctx!!.functionBody() != null){
            Function.currFunction = (currProperty.mutator as FunctionMutator).function
        }
        return super.visitSetter(ctx)
    }
    //endregion

    //region template

    /**
     * 进入类体。
     * @param ctx the parse tree
     */

    override fun visitTemplateBody(ctx: mcfppParser.TemplateBodyContext): Any? {
        enterTemplateBody(ctx)
        super.visitTemplateBody(ctx)
        exitTemplateBody(ctx)
        return null
    }
    
    private fun enterTemplateBody(ctx: mcfppParser.TemplateBodyContext) {
        Project.ctx = ctx
        //获取类的对象
        val parent = ctx.parent
        if(parent is mcfppParser.TemplateDeclarationContext){
            val identifier = parent.classWithoutNamespace().text
            DataTemplate.currTemplate = GlobalField.getTemplate(Project.currNamespace, identifier)
        }else if(parent is mcfppParser.ObjectTemplateDeclarationContext){
            val identifier = parent.classWithoutNamespace().text
            DataTemplate.currTemplate = GlobalField.getObject(Project.currNamespace, identifier) as ObjectDataTemplate
        }else{
            throw Exception("Unknown parent")
        }
        //设置作用域
    }

    /**
     * 离开类体。将缓存重新指向全局
     * @param ctx the parse tree
     */
    private fun exitTemplateBody(ctx: mcfppParser.TemplateBodyContext) {
        Project.ctx = ctx
        DataTemplate.currTemplate = null
    }

    override fun visitObjectTemplateDeclaration(ctx: mcfppParser.ObjectTemplateDeclarationContext?): Any? {

        return super.visitObjectTemplateDeclaration(ctx)
    }

    override fun visitTemplateFunctionDeclaration(ctx: mcfppParser.TemplateFunctionDeclarationContext): Any? {
        enterTemplateFunctionDeclaration(ctx)
        super.visitTemplateFunctionDeclaration(ctx)
        exitTemplateFunctionDeclaration(ctx)
        return null
    }

    private fun enterTemplateFunctionDeclaration(ctx: mcfppParser.TemplateFunctionDeclarationContext) {
        Project.ctx = ctx
        //解析参数
        val types = FunctionParam.parseReadonlyAndNormalParamTypes(ctx.functionParams())
        //获取缓存中的对象
        val f = DataTemplate.currTemplate!!.field.getFunction(ctx.Identifier().text, types.first.map { it.build("") }, types.second.map { it.build("") })
        Function.currFunction = f
    }

    fun exitTemplateFunctionDeclaration(ctx: mcfppParser.TemplateFunctionDeclarationContext) {
        Project.ctx = ctx
        Function.currFunction = Function.nullFunction
    }

    //endregion

    companion object {
        /**
         * 判断这个语句是否在循环语句中。包括嵌套形式。
         * @param ctx 需要判断的语句
         * @return 是否在嵌套中
         */
        public fun inLoopStatement(ctx: RuleContext): Boolean {
            if (ctx is mcfppParser.ForStatementContext) {
                return true
            }
            if (ctx is mcfppParser.DoWhileStatementContext) {
                return true
            }
            if (ctx is mcfppParser.WhileStatementContext) {
                return true
            }
            return if (ctx.parent != null) {
                inLoopStatement(ctx.parent)
            } else false
        }
    }
}