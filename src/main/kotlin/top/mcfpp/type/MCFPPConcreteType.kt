package top.mcfpp.type

import top.mcfpp.core.lang.JavaVar
import top.mcfpp.core.lang.MCFPPTypeVar
import top.mcfpp.core.lang.UnknownVar
import top.mcfpp.core.lang.Var
import top.mcfpp.model.Class
import top.mcfpp.model.CompoundData
import top.mcfpp.model.FieldContainer
import top.mcfpp.util.LogProcessor

open class MCFPPConcreteType(parentType: ArrayList<MCFPPType> = arrayListOf()): MCFPPType(parentType) {
    final override fun buildUnConcrete(identifier: String): Var<*> {
        LogProcessor.error("Cannot build variable '$typeName' as the compiler cannot track its type.")
        return UnknownVar(identifier)
    }

    final override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> {
        LogProcessor.error("Cannot build variable '$typeName' as the compiler cannot track its type.")
        return UnknownVar(identifier)
    }

    final override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> {
        LogProcessor.error("Cannot build variable '$typeName' as the compiler cannot track its type.")
        return UnknownVar(identifier)
    }

    object Type: MCFPPConcreteType(arrayListOf()){

        override val objectData: CompoundData
            get() = data

        override val typeName: String
            get() = "type"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            MCFPPTypeVar(identifier = identifier)
        override fun build(identifier: String): Var<*> = MCFPPTypeVar(identifier = identifier)
        override fun build(identifier: String, clazz: Class): Var<*> = MCFPPTypeVar(identifier = identifier)
    }

    object JavaVar: MCFPPConcreteType(arrayListOf(MCFPPBaseType.Any)){

        override val objectData: CompoundData
            get() = top.mcfpp.core.lang.JavaVar.data
        override val typeName: String
            get() = "JavaVar"

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            JavaVar(null, identifier)
        override fun build(identifier: String): Var<*> = JavaVar(null, identifier)
        override fun build(identifier: String, clazz: Class): Var<*> = JavaVar(null, identifier)
 }

}