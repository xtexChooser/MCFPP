package top.mcfpp.type

import net.querz.nbt.tag.CompoundTag
import top.mcfpp.core.lang.DataTemplateObject
import top.mcfpp.core.lang.DataTemplateObjectConcrete
import top.mcfpp.core.lang.UnknownVar
import top.mcfpp.core.lang.Var
import top.mcfpp.mni.annotation.NoInstance
import top.mcfpp.model.Class
import top.mcfpp.model.CompoundData
import top.mcfpp.model.DataTemplate
import top.mcfpp.model.FieldContainer
import top.mcfpp.util.LogProcessor

/**
 * 模板类型
 * @see DataTemplate
 */
open class MCFPPDataTemplateType(
    var template: DataTemplate,
    override var parentType: List<MCFPPType>
) : MCFPPType(parentType) {

    override val objectData: CompoundData
        get() = template

    override val typeName: String
        get() = "template(${template.namespace}:${template.identifier})"

    override fun defaultValue(): CompoundTag {
        val tag = CompoundTag()
        for (member in template.field.allVars){
            if(member.nullable) continue
            tag.put(member.identifier, member.type.defaultValue())
        }
        return tag
    }

    init {
        //registerType({it.contains(regex)}){
        //    val matcher = regex.find(it)!!.groupValues
        //    MCFPPTemplateType(
        //        Template(matcher[2], LazyWrapper(MCFPPBaseType.Int),matcher[1]), //TODO: 这里肯定有问题
        //        parentType
        //    )
        //}
    }

    override fun build(identifier: String, container: FieldContainer): Var<*> {
        if (template.annotations.any { it is NoInstance }){
            LogProcessor.error("Template ${template.namespaceID} is not allowed to be instantiated.")
            return UnknownVar(identifier)
        }else{
            return DataTemplateObjectConcrete(template, CompoundTag(), identifier)
        }
    }

    override fun build(identifier: String): Var<*> {
        if (template.annotations.any { it is NoInstance }){
            LogProcessor.error("Template ${template.namespaceID} is not allowed to be instantiated.")
            return UnknownVar(identifier)
        }else{
            return DataTemplateObjectConcrete(template, CompoundTag(), identifier)
        }
    }

    override fun build(identifier: String, clazz: Class): Var<*> {
        if (template.annotations.any { it is NoInstance }){
            LogProcessor.error("Template ${template.namespaceID} is not allowed to be instantiated.")
            return UnknownVar(identifier)
        }else{
            return DataTemplateObjectConcrete(template, CompoundTag(), identifier)
        }
    }
    override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> {
        if (template.annotations.any { it is NoInstance }){
            LogProcessor.error("Template ${template.namespaceID} is not allowed to be instantiated.")
            return UnknownVar(identifier)
        }else{
            return DataTemplateObject(template, identifier)
        }
    }
    override fun buildUnConcrete(identifier: String): Var<*> {
        if (template.annotations.any { it is NoInstance }){
            LogProcessor.error("Template ${template.namespaceID} is not allowed to be instantiated.")
            return UnknownVar(identifier)
        }else{
            return DataTemplateObject(template, identifier)
        }
    }
    override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> {
        if (template.annotations.any { it is NoInstance }){
            LogProcessor.error("Template ${template.namespaceID} is not allowed to be instantiated.")
            return UnknownVar(identifier)
        }else{
            return DataTemplateObject(template, identifier)
        }
    }

    companion object{
        val regex = Regex("^template\\((.+):(.+)\\)$")
    }

}