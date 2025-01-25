package top.mcfpp.core.lang.value

import top.mcfpp.core.lang.Var
import top.mcfpp.model.Member
import top.mcfpp.model.function.Function
import top.mcfpp.type.MCFPPConcreteType
import top.mcfpp.type.MCFPPGenericParamType
import top.mcfpp.type.MCFPPType

/**
 * type T;
 * 这里T既是值也是类型
 * 作为值的时候，存储的是一个类型
 */

class MCTypeValue(
    identifier:String,
    var parentType: ArrayList<out MCFPPType>
): Var<MCTypeValue>(identifier) {

    /**
     * **只读，不可更改**。此值的类型，为MCFPPBaseType.Type
     */
    override var type: MCFPPType = MCFPPConcreteType.Type
    
    override fun doAssignedBy(b: Var<*>): MCTypeValue {
        TODO("Not yet implemented")
    }

    override fun canAssignedBy(b: Var<*>): Boolean {
        TODO("Not yet implemented")
    }

    fun toType(): MCFPPGenericParamType {
        return MCFPPGenericParamType(identifier,parentType)
    }

    override fun clone(): MCTypeValue {
        TODO("Not yet implemented")
    }

    override fun getTempVar(): MCTypeValue {
        TODO("Not yet implemented")
    }

    override fun storeToStack() {
        TODO("Not yet implemented")
    }

    override fun getFromStack() {
        TODO("Not yet implemented")
    }

    override fun getMemberVar(key: String, accessModifier: Member.AccessModifier): Pair<Var<*>?, Boolean> {
        TODO("Not yet implemented")
    }

    override fun getMemberFunction(
        key: String,
        readOnlyArgs: List<Var<*>>,
        normalArgs: List<Var<*>>,
        accessModifier: Member.AccessModifier
    ): Pair<Function, Boolean> {
        TODO("Not yet implemented")
    }
}