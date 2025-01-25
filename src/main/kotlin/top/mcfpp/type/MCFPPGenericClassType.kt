package top.mcfpp.type

import top.mcfpp.core.lang.ClassPointer
import top.mcfpp.core.lang.MCFPPValue
import top.mcfpp.core.lang.Var
import top.mcfpp.model.Class
import top.mcfpp.model.FieldContainer
import top.mcfpp.model.UnsolvedGenericClass

class MCFPPGenericClassType (
    cls: Class,
    val genericVar : ArrayList<out MCFPPValue<*>>,
    parentType: ArrayList<out MCFPPType>
) : MCFPPClassType(cls, parentType) {

    override val typeName: String
        get() = "class(${cls.namespace}:${cls.identifier})[${
            genericVar.joinToString("_") { it.value.toString() }
        }]"

    override fun tryResolve() {
        if(cls is UnsolvedGenericClass){
            cls = (cls as UnsolvedGenericClass).resolve()
        }
    }

    override fun build(identifier: String, container: FieldContainer): Var<*> = ClassPointer(cls, identifier)
    override fun build(identifier: String): Var<*> = ClassPointer(cls, identifier)
    override fun build(identifier: String, clazz: Class): Var<*> = ClassPointer(this.cls, identifier)
    override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> = ClassPointer(cls, identifier)
    override fun buildUnConcrete(identifier: String): Var<*> = ClassPointer(cls, identifier)
    override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = ClassPointer(this.cls, identifier)

}