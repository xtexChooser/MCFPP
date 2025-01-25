package top.mcfpp.model.generic

import top.mcfpp.core.lang.Var
import top.mcfpp.model.CanSelectMember
import top.mcfpp.model.function.Function
import top.mcfpp.model.function.FunctionParam

interface Generic<T> where T : Function{

    val readOnlyParams: ArrayList<FunctionParam>

    fun invoke(readOnlyArgs: ArrayList<Var<*>>, normalArgs: ArrayList<Var<*>>, caller: CanSelectMember?): Var<*>

    //fun compile(readOnlyArgs: ArrayList<Var<*>>) : T

    fun isSelf(key: String, readOnlyArgs: List<Var<*>>, normalArgs: List<Var<*>>): Boolean

    fun isSelfWithDefaultValue(key: String, readOnlyArgs: List<Var<*>>, normalArgs: List<Var<*>>): Boolean
}