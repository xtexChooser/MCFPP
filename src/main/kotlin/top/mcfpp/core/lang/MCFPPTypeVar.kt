package top.mcfpp.core.lang

import top.mcfpp.model.CompoundData
import top.mcfpp.model.function.Function
import top.mcfpp.model.Member
import top.mcfpp.type.*
import top.mcfpp.util.LogProcessor
import java.util.UUID

class MCFPPTypeVar : Var<MCFPPTypeVar>, MCFPPValue<MCFPPType> {

    override var value: MCFPPType

    override var type: MCFPPType = MCFPPConcreteType.Type

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(type: MCFPPType = MCFPPBaseType.Any, identifier: String = UUID.randomUUID().toString()) : super(identifier) {
        this.value = type
    }

    override fun doAssignedBy(b: Var<*>) : MCFPPTypeVar {
        if(b is MCFPPTypeVar){
            this.value = b.value
            hasAssigned = true
        } else {
            LogProcessor.error("Cannot assign a ${b.type} to a MCFPPTypeVar")
        }
        return this
    }

    override fun canAssignedBy(b: Var<*>): Boolean {
        return !b.implicitCast(type).isError
    }


    override fun clone(): MCFPPTypeVar {
        return this
    }

    override fun getTempVar(): MCFPPTypeVar {
        return MCFPPTypeVar(value)
    }

    override fun storeToStack() {}

    override fun getFromStack() {}

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

    override fun toDynamic(replace: Boolean): Var<*> {
        TODO("Not yet implemented")
    }

    override fun replaceMemberVar(v: Var<*>) {
        when(val type = type){
            is MCFPPClassType ->{
                type.cls.field.putVar(v.identifier, v, true)
            }
            is MCFPPCompoundType -> {
                type.objectData.field.putVar(v.identifier, v, true)
            }
            else -> TODO()
        }
    }

    companion object {
        val data = CompoundData("type","mcfpp")
    }
}