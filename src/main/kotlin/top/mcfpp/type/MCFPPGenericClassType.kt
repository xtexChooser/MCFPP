package top.mcfpp.type

import top.mcfpp.core.lang.ClassPointer
import top.mcfpp.core.lang.Var
import top.mcfpp.model.Class
import top.mcfpp.model.FieldContainer

class MCFPPGenericClassType (
    cls: Class,
    parentType: List<MCFPPType>
) : MCFPPClassType(cls, parentType) {

    val genericVar : ArrayList<Var<*>> = ArrayList()

    override fun build(identifier: String, container: FieldContainer): Var<*> = ClassPointer(cls, identifier)
    override fun build(identifier: String): Var<*> = ClassPointer(cls, identifier)
    override fun build(identifier: String, clazz: Class): Var<*> = ClassPointer(this.cls, identifier)
    override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> = ClassPointer(cls, identifier)
    override fun buildUnConcrete(identifier: String): Var<*> = ClassPointer(cls, identifier)
    override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = ClassPointer(this.cls, identifier)

}