package top.mcfpp.io.info

import top.mcfpp.core.lang.Var
import top.mcfpp.model.field.CompoundDataField

data class FieldInfo(
    var vars: ArrayList<Var<*>>,
    var functions: ArrayList<AbstractFunctionInfo<*>>
): ModelInfo<CompoundDataField> {
    override fun get(): CompoundDataField {
        val field = CompoundDataField(ArrayList())
        vars.forEach {
            field.putVar(it.identifier, it, false)
        }
        functions.forEach {
            field.addFunction(it.get(), true)
        }
        return field
    }

    companion object {
        fun from(field: CompoundDataField): FieldInfo {
            val functions = ArrayList<AbstractFunctionInfo<*>>()
            field.forEachFunction {
                functions.add(AbstractFunctionInfo.from(it))
            }
            return FieldInfo(
                ArrayList(field.allVars),
                ArrayList(functions)
            )
        }
    }

}