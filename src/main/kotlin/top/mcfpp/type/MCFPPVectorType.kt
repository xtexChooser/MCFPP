package top.mcfpp.type

import net.querz.nbt.tag.ListTag
import net.querz.nbt.tag.Tag
import top.mcfpp.core.lang.Var
import top.mcfpp.core.lang.VectorVar
import top.mcfpp.core.lang.VectorVarConcrete
import top.mcfpp.model.Class
import top.mcfpp.model.CompoundData
import top.mcfpp.model.FieldContainer

class MCFPPVectorType(val dimension: Int): MCFPPType(arrayListOf(MCFPPBaseType.Any)) {

    override val objectData: CompoundData
        get() = VectorVar.data

    override val typeName: String
        get() = "vec$dimension"

    override val nbtType: java.lang.Class<out Tag<*>>
        get() = ListTag::class.java

    companion object {
        val regex = Regex("^vec\\d+$")
    }
    override fun build(identifier: String, container: FieldContainer): Var<*> = VectorVarConcrete(Array(dimension){0}, container, identifier)
    override fun build(identifier: String): Var<*> = VectorVarConcrete(Array(dimension){0}, identifier)
    override fun build(identifier: String, clazz: Class): Var<*> = VectorVarConcrete(Array(dimension){0}, clazz, identifier)
    override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> = VectorVar(dimension, container, identifier)
    override fun buildUnConcrete(identifier: String): Var<*> = VectorVar(dimension, identifier)
    override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = VectorVar(dimension, clazz, identifier)

}