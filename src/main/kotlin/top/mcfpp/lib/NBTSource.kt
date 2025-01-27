package top.mcfpp.lib

import top.mcfpp.command.Command
import top.mcfpp.core.lang.SelectorVar
import top.mcfpp.core.lang.arg.Pos
import java.io.Serializable

interface NBTSource: Serializable {
    fun toCommand(): Command

    fun toChatComponentPart(): Command
}

class StorageSource(val storage: String): NBTSource{
    override fun toCommand(): Command {
        return Command("storage $storage")
    }

    override fun toChatComponentPart(): Command {
        return Command("\"storage\":\"$storage\"")
    }
}

class EntitySource(val entity: SelectorVar): NBTSource{
    override fun toCommand(): Command {
        return Command.build("entity").build(entity.value.toCommandPart())
    }

    override fun toChatComponentPart(): Command {
        return Command("\"entity\":\"${entity.value.toCommandPart()}\"")
    }
}

class BlockSource(val pos: Pos): NBTSource{
    override fun toCommand(): Command {
        return Command.build("block").build(pos.toCommandPart())
    }

    override fun toChatComponentPart(): Command {
        return Command("\"block\":\"${pos.toCommandPart()}\"")
    }
}