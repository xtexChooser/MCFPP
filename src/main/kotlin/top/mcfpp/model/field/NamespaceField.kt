package top.mcfpp.model.field

import com.google.common.collect.ArrayListMultimap
import org.jetbrains.annotations.Nullable
import top.mcfpp.core.lang.Var
import top.mcfpp.model.*
import top.mcfpp.model.Enum
import top.mcfpp.model.function.Function
import top.mcfpp.model.function.UnknownFunction
import top.mcfpp.model.generic.Generic
import top.mcfpp.model.generic.GenericClass
import top.mcfpp.type.MCFPPType

/**
 * 一个域。在编译过程中，编译器读取到的变量，函数等会以键值对的方式储存在其中。键为函数的id或者变量的
 * 标识符，而值则是这个函数或者变量的对象。
 *
 * 同一个域中的函数
 *
 * 域应该是一个链式的结构，主要分为如下几种情况：<br></br>
 * * 类的静态变量 ---> 类的成员变量 --> 函数 ---> 匿名内部函数<br></br>
 *
 * * 类的静态变量 ---> 静态函数 ---> 匿名内部函数<br></br>
 *
 * * 函数 ---> 匿名内部函数<br></br>
 * 寻找变量的时候，应当从当前的作用域开始寻找。<br></br>
 *
 * 变量和类都分别储存在一张哈希表中，键名即它在声明的时候的名字，而值则代表了它的对象。
 * 值得注意的是，变量声明时的名字虽然和最终编译出的名字有关，但不相同。
 *
 * 函数储存在一个列表中
 */
open class NamespaceField(
    private val simpleFieldWithType: SimpleFieldWithType = SimpleFieldWithType(),
    private val simpleFieldWithEnum: SimpleFieldWithEnum = SimpleFieldWithEnum(),
    private val simpleFieldWithObject: SimpleFieldWithObject = SimpleFieldWithObject(),
    private val simpleFieldWithAnnotation: SimpleFieldWithAnnotation = SimpleFieldWithAnnotation(),
    private val simpleFieldWithVar: SimpleFieldWithVar = SimpleFieldWithVar()
    )
    : IFieldWithClass, IFieldWithFunction, IFieldWithTemplate, IFieldWithInterface,
    IFieldWithType by simpleFieldWithType,
    IFieldWithEnum by simpleFieldWithEnum,
    IFieldWithObject by simpleFieldWithObject,
    IFieldWithAnnotation by simpleFieldWithAnnotation,
    IFieldWithVar by simpleFieldWithVar
{
    /**
     * 变量
     */
    private val vars: HashMap<String, Var<*>> = HashMap()

    /**
     * 函数
     */
    private var functions: ArrayList<Function> = ArrayList()

    /**
     * 类
     */
    private var classes: ArrayListMultimap<String, Class> = ArrayListMultimap.create()

    /**
     * 模板
     */
    private var template: HashMap<String, DataTemplate> = HashMap()

    /**
     * 接口
     */
    private var interfaces: HashMap<String, Interface> = HashMap()

    /**
     * 父级域。命名空间的父级域应当是全局
     */
    @Nullable
    var parent = GlobalField

    /**
     * 这个缓存在哪一个容器中
     */
    @Nullable
    var container: FieldContainer? = GlobalField

    /**
     * 复制一个缓存。
     * @param cache 原来的缓存
     */
    constructor(cache: NamespaceField) : this(cache.simpleFieldWithType, cache.simpleFieldWithEnum) {
        parent = cache.parent
        //变量复制
        for (key in cache.vars.keys) {
            val `var`: Var<*>? = cache.vars[key]
            vars[key] = `var`!!.clone()
        }
        functions.addAll(cache.functions)
    }

    //region function

    override fun forEachFunction(operation: (Function) -> Any?){
        for (function in functions){
            operation(function)
        }
    }
    /**
     * 根据所给的函数名和参数获取一个函数
     * @param key 函数名
     * @param normalArgs 参数类型
     * @return 如果此缓存中存在这个函数，则返回这个函数的对象，否则返回null
     */
    @Nullable
    override fun getFunction(key: String, readOnlyArgs: List<Var<*>>, normalArgs: List<Var<*>>): Function {
        for (f in functions) {
            if(f is Generic<*> && f.isSelf(key, readOnlyArgs, normalArgs)){
                return f
            }
            if(f.isSelf(key, normalArgs)){
                return f
            }
        }
        for (f in functions) {
            if(f is Generic<*> && f.isSelfWithDefaultValue(key, readOnlyArgs, normalArgs)){
                return f
            }
            if(f.isSelfWithDefaultValue(key, normalArgs)){
                return f
            }
        }
        return UnknownFunction(key)
    }


    /**
     * 添加一个函数
     *
     * @param function
     */
    override fun addFunction(function: Function, force: Boolean): Boolean{
        if(hasFunction(function, true)){
            if(force){
                functions[functions.indexOf(function)] = function
                return true
            }
            return false
        }
        functions.add(function)
        return true
    }

    override fun hasFunction(function: Function, considerParent: Boolean): Boolean{
        return functions.contains(function)
    }
    //endregion

    //region class

    override fun forEachClass(operation: (Class) -> Any?){
        for (`class` in classes.values()){
            operation(`class`)
        }
    }

    /**
     * 根据所给的id获取一个类
     *
     * @param identifier
     */
    override fun getClass(identifier: String, readOnlyParam: List<MCFPPType>): GenericClass? {
        for (`class` in classes[identifier]) {
            if (`class` is GenericClass && `class`.isSelfByType(identifier, readOnlyParam)) {
                return `class`
            }
        }
        return null
    }

    override fun getClass(identifier: String): Class? {
        for (clazz in classes[identifier]){
            if(clazz !is GenericClass){
                return clazz
            }
        }
        return null
    }

    override fun hasClass(cls: Class): Boolean{
        return classes.containsValue(cls)
    }

    fun hasNotGenericClass(cls: String): Boolean{
        return classes.values().any { it.identifier == cls && it !is GenericClass }
    }

    fun hasNotGenericClass(cls: Class): Boolean{
        return classes.values().any { it == cls && it !is GenericClass }
    }


    override fun hasClass(identifier: String): Boolean{
        return classes.containsKey(identifier)
    }

    override fun addClass(identifier: String, cls: Class, force : Boolean): Boolean{
        return if (force){
            if(cls is GenericClass){
                classes.put(identifier, cls)
            }else{
                val c = getClass(identifier)
                if(c != null){
                    classes.remove(identifier, c)
                }
                classes.put(identifier, cls)
            }
            true
        }else{
            if(cls is GenericClass){
                classes.put(identifier, cls)
            }else{
                val c = getClass(identifier)
                if(c != null){
                    return false
                }
                classes.put(identifier, cls)
            }
            true
        }
    }

    override fun removeClass(identifier: String): Boolean {
        return if(classes.containsKey(identifier)) {
            classes.removeAll(identifier)
            true
        }else{
            false
        }
    }
    //endregion

    //region template

    override fun forEachTemplate(operation: (DataTemplate) -> Any?){
        for (t in template.values){
            operation(t)
        }
    }

    /**
     * 向域中添加一个模板
     *
     * @param identifier 模板的标识符
     * @param template 模板
     * @param force 是否强制添加。如果为true，则即使已经添加过相同标识符的模板，也会覆盖原来的模板进行添加。
     * @return 是否添加成功。如果已经存在相同标识符的模板，且不是强制添加则为false
     */
    override fun addTemplate(identifier: String, template: DataTemplate, force: Boolean): Boolean {
        return if (force){
            this.template[identifier] = template
            true
        }else{
            if(!this.template.containsKey(identifier)){
                this.template[identifier] = template
                true
            }else{
                false
            }
        }
    }

    /**
     * 移除一个模板
     *
     * @param identifier 这个模板的标识符
     * @return 是否移除成功。如果不存在此模板，则返回false
     */
    override fun removeTemplate(identifier: String): Boolean {
        return if(template.containsKey(identifier)) {
            template.remove(identifier)
            true
        }else{
            false
        }
    }

    /**
     * 获取一个模板。可能不存在
     *
     * @param identifier 模板的标识符
     * @return 获取到的模板。如果不存在，则返回null
     */
    override fun getTemplate(identifier: String): DataTemplate? {
        return template[identifier]
    }

    /**
     * 是否存在此模板
     *
     * @param identifier 模板的标识符
     * @return
     */
    override fun hasTemplate(identifier: String): Boolean {
        return template.containsKey(identifier)
    }

    /**
     * 是否存在此模板
     *
     * @param template 模板
     * @return
     */
    override fun hasTemplate(template: DataTemplate): Boolean {
        return this.template.containsKey(template.identifier)
    }

    //endregion

    //region interface

    override fun forEachInterface(operation: (Interface) -> Any?){
        for(`interface` in interfaces.values){
            operation(`interface`)
        }
    }

    /**
     * 向域中添加一个接口
     *
     * @param identifier 接口的标识符
     * @param itf 接口
     * @param force 是否强制添加。如果为true，则即使已经添加过相同标识符的接口，也会覆盖原来的接口进行添加。
     * @return 是否添加成功。如果已经存在相同标识符的接口，且不是强制添加则为false
     */
    override fun addInterface(identifier: String, itf: Interface, force: Boolean): Boolean {
        return if (force){
            interfaces[identifier] = itf
            true
        }else{
            if(!interfaces.containsKey(identifier)){
                interfaces[identifier] = itf
                true
            }else{
                false
            }
        }
    }

    /**
     * 移除一个接口
     *
     * @param identifier 这个接口的标识符
     * @return 是否移除成功。如果不存在此接口，则返回false
     */
    override fun removeInterface(identifier: String): Boolean {
        return if(interfaces.containsKey(identifier)) {
            interfaces.remove(identifier)
            true
        }else{
            false
        }
    }

    /**
     * 获取一个接口。可能不存在
     *
     * @param identifier 接口的标识符
     * @return 获取到的接口。如果不存在，则返回null
     */
    override fun getInterface(identifier: String): Interface? {
        return interfaces[identifier]
    }

    /**
     * 是否存在此接口
     *
     * @param identifier 接口的标识符
     * @return
     */
    override fun hasInterface(identifier: String): Boolean {
        return interfaces.containsKey(identifier)
    }

    /**
     * 是否存在此接口
     *
     * @param itf 接口
     * @return
     */
    override fun hasInterface(itf: Interface): Boolean {
        return interfaces.containsKey(itf.identifier)
    }
    //endregion

    fun hasDeclaredType(identifier: String): Boolean{
        return hasEnum(identifier) || hasTemplate(identifier) || hasInterface(identifier) || hasClass(identifier)
    }

    fun hasDeclaredType(type: CompoundData): Boolean{
        if(type !is GenericClass){
            val identifier = type.identifier
            return hasEnum(identifier) || hasTemplate(identifier) || hasInterface(identifier) || hasNotGenericClass(identifier)
        }else{
            return hasClass(type)
        }
    }

    fun getDeclaredType(identifier: String): CompoundData?{
        return getEnum(identifier) ?: getTemplate(identifier) ?: getInterface(identifier) ?: getClass(identifier)
    }

    fun addDeclaredType(type: CompoundData): Boolean {
         return when (type) {
             is Enum -> addEnum(type.identifier, type)

             is DataTemplate -> addTemplate(type.identifier, type)

             is Interface -> addInterface(type.identifier, type)

             is Class -> addClass(type.identifier, type)

             else -> throw IllegalArgumentException("Unknown type: $type")
         }
    }
}