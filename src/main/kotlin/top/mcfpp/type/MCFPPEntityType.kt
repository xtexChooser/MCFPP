package top.mcfpp.type

import net.querz.nbt.tag.IntArrayTag
import net.querz.nbt.tag.Tag
import top.mcfpp.core.lang.EntityVar
import top.mcfpp.core.lang.EntityVarConcrete
import top.mcfpp.core.lang.SelectorVar
import top.mcfpp.core.lang.Var
import top.mcfpp.lib.EntitySelector
import top.mcfpp.model.Class
import top.mcfpp.model.CompoundData
import top.mcfpp.model.FieldContainer

class MCFPPEntityType {

    object EntityBase: MCFPPType(listOf(MCFPPBaseType.Any)){

            override val objectData: CompoundData
                get() = EntityVar.data

            override val typeName: String
                get() = "entitybase"
    }

    object Entity: MCFPPType(listOf(EntityBase)){

        override val objectData: CompoundData
            get() = EntityVar.data

        override val typeName: String
            get() = "entity"

        override val nbtType: java.lang.Class<out Tag<*>>
            get() = IntArrayTag::class.java

        override fun build(identifier: String, container: FieldContainer): Var<*> =
            EntityVarConcrete(IntArrayTag(intArrayOf(0, 0, 0, 0)), identifier)
        override fun build(identifier: String): Var<*> =
            EntityVarConcrete(IntArrayTag(intArrayOf(0, 0, 0, 0)), identifier)
        override fun build(identifier: String, clazz: Class): Var<*> =
            EntityVarConcrete(IntArrayTag(intArrayOf(0, 0, 0, 0)), identifier)
        override fun buildUnConcrete(identifier: String, container: FieldContainer): Var<*> =
            EntityVar(identifier)
        override fun buildUnConcrete(identifier: String): Var<*> = EntityVar(identifier)
        override fun buildUnConcrete(identifier: String, clazz: Class): Var<*> = EntityVar(identifier)
    }

    object Selector: MCFPPConcreteType(listOf(EntityBase)){

        override val objectData: CompoundData
            get() = SelectorVar.data

        override val typeName: String
            get() = "selector"

        override fun build(identifier: String, container: FieldContainer): Var<*>
            = SelectorVar(EntitySelector(EntitySelector.Companion.SelectorType.ALL_ENTITIES), identifier)

        override fun build(identifier: String): Var<*>
            = SelectorVar(EntitySelector(EntitySelector.Companion.SelectorType.ALL_ENTITIES), identifier)

        override fun build(identifier: String, clazz: Class): Var<*>
            = SelectorVar(EntitySelector(EntitySelector.Companion.SelectorType.ALL_ENTITIES), identifier)

    }

    class LimitedSelectorType(val limit: Int): MCFPPConcreteType(listOf(Entity)){

        override val objectData: CompoundData
            get() = SelectorVar.data

        override val typeName: String
            get() = "selector[$limit]"

        override fun build(identifier: String, container: FieldContainer): Var<*>
            = SelectorVar(EntitySelector(EntitySelector.Companion.SelectorType.ALL_ENTITIES).limit(limit), identifier)
        override fun build(identifier: String): Var<*>
            = SelectorVar(EntitySelector(EntitySelector.Companion.SelectorType.ALL_ENTITIES).limit(limit), identifier)
        override fun build(identifier: String, clazz: Class): Var<*>
            = SelectorVar(EntitySelector(EntitySelector.Companion.SelectorType.ALL_ENTITIES).limit(limit), identifier)
    }
}