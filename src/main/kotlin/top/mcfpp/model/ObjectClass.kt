package top.mcfpp.model

import top.mcfpp.Project
import top.mcfpp.core.lang.ClassPointer
import top.mcfpp.model.field.GlobalField
import top.mcfpp.model.generic.GenericObjectClass
import top.mcfpp.type.MCFPPObjectClassType
import top.mcfpp.type.MCFPPType
import top.mcfpp.util.LogProcessor
import top.mcfpp.util.MCUUID

open class ObjectClass(identifier: String, namespace: String = Project.currNamespace) : Class(identifier, namespace), ObjectCompoundData {

    var mcuuid: MCUUID = MCUUID.genFromString("$namespace:$identifier")

    var normalClass: Class? = null

    override val prefix: String
        get() = namespace + "_object_class_" + identifier + "_"

    /**
     * 获取这个类指针对于的marker的tag
     */
    override val tag: String
        get() = namespace + "_object_class_" + identifier

    override var getType : () -> MCFPPType = {
        MCFPPObjectClassType(this,
            parent.filterIsInstance<Class>().map { it.getType() }
        )
    }

    override fun newPointer(): ClassPointer {
        LogProcessor.error("Cannot instantiate an object class")
        return ClassPointer(this, "error_object_instance")
    }

    companion object{

        class UndefinedObjectClass(identifier: String, namespace: String?)
            : ObjectClass(identifier, namespace?: Project.currNamespace) {

            fun getDefinedClassOrInterface(): CompoundData?{
                var re : CompoundData? = GlobalField.getClass(namespace, identifier)
                if(re == null){
                    re = GlobalField.getInterface(namespace, identifier)
                }
                return re
            }

        }
    }
}
class CompiledGenericObjectClass(identifier: String, namespace: String = Project.currNamespace,
                           var originClass: GenericObjectClass
) : ObjectClass(identifier, namespace)