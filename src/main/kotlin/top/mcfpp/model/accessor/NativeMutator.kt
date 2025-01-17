package top.mcfpp.model.accessor

import top.mcfpp.Project
import top.mcfpp.annotations.MNIMutator
import top.mcfpp.core.lang.Var
import top.mcfpp.model.CanSelectMember
import top.mcfpp.model.CompoundData
import top.mcfpp.model.function.NativeFunction
import top.mcfpp.util.LogProcessor

class NativeMutator(javaRefer: String, d: CompoundData, field: Var<*>): AbstractMutator(field) {

    val function: NativeFunction = NativeFunction("set_${field.identifier}", d.namespace)

    init {
        function.returnType = field.type
        function.field.putVar("field", field)
        function.appendNormalParam(field.type, "value")
        function.field.putVar("value", field.type.build("value"))
        function.owner = d
        try {
            //根据JavaRefer找到类
            val clazz = Project.classLoader.loadClass(javaRefer)
            val methods = clazz.methods
            var hasFind = false
            for(method in methods){
                val mniMutator = method.getAnnotation(MNIMutator::class.java) ?: continue
                if(mniMutator.name == field.identifier){
                    hasFind = true
                    function.javaMethod = method
                    break
                }
            }
            if(!hasFind){
                LogProcessor.error("Cannot find mutator ${field.identifier} in class $javaRefer")
            }
        } catch (e: ClassNotFoundException) {
            LogProcessor.error("Cannot find java class: $javaRefer")
        }
    }

    override fun setter(caller: CanSelectMember, b: Var<*>): Var<*>{
        function.invoke(arrayListOf(b), caller)
        return function.returnVar
    }
}