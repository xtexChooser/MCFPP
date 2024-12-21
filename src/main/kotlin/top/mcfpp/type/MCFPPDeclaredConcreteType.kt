package top.mcfpp.type

import net.querz.nbt.tag.Tag
import top.mcfpp.core.lang.Var
import top.mcfpp.model.CompoundData
import top.mcfpp.model.FieldContainer

class MCFPPDeclaredConcreteType(val type: MCFPPType): MCFPPConcreteType(arrayListOf(type)) {

    override val objectData: CompoundData
        get() = type.objectData

    override val typeName: String
        get() = type.typeName

    override val nbtType: Class<out Tag<*>>
        get() = type.nbtType

    override fun build(identifier: String): Var<*> = type.build(identifier).apply { type = this@MCFPPDeclaredConcreteType }

    override fun build(identifier: String, container: FieldContainer): Var<*> = type.build(identifier, container).apply { type = this@MCFPPDeclaredConcreteType }

    override fun build(identifier: String, clazz: top.mcfpp.model.Class): Var<*> = type.build(identifier, clazz).apply { type = this@MCFPPDeclaredConcreteType }

}