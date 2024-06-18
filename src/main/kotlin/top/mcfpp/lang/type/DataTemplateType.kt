package top.mcfpp.lang.type

import net.querz.nbt.tag.CompoundTag
import top.mcfpp.model.*

/**
 * 模板类型
 * @see DataTemplate
 */
class DataTemplateType(
    var template: DataTemplate,
    override var parentType: List<MCFPPType>
) :MCFPPType(parentType, template) {

    override val typeName: String
        get() = "template(${template.namespace}:${template.identifier})"

    init {
        //registerType({it.contains(regex)}){
        //    val matcher = regex.find(it)!!.groupValues
        //    MCFPPTemplateType(
        //        Template(matcher[2], LazyWrapper(MCFPPBaseType.Int),matcher[1]), //TODO: 这里肯定有问题
        //        parentType
        //    )
        //}
    }

    companion object{
        val regex = Regex("^template\\((.+):(.+)\\)$")
    }


}