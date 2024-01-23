package top.mcfpp.lang

import top.mcfpp.lib.Class
import top.mcfpp.lib.ExtensionFunction
import top.mcfpp.lib.Function
import top.mcfpp.lib.Member

/**
 * 此抽象类对类的基本性质进行了一些声明，例如类的地址。和mcfpp类有关的类都应当继承于此抽象方法
 *
 */
abstract class ClassBase: Var {

    constructor(b: Var):super(b)

    constructor()

    /**
     * 它的类型
     */
    abstract var clsType: Class

    /**
     * 表示这个类的对象的实体拥有的标签
     */
    abstract val tag: String

    override fun getAccess(function: Function): Member.AccessModifier {
        return if(function !is ExtensionFunction && function.ownerType == Function.Companion.OwnerType.CLASS){
            function.parentClass()!!.getAccess(clsType)
        }else if(function !is ExtensionFunction && function.ownerType == Function.Companion.OwnerType.STRUCT){
            function.parentStruct()!!.getAccess(clsType)
        }else{
            Member.AccessModifier.PUBLIC
        }
    }
}