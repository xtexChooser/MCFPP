package top.mcfpp.core.lang.bool

import top.mcfpp.command.Command
import top.mcfpp.core.lang.Var
import top.mcfpp.core.lang.nbt.NBTBasedData
import top.mcfpp.model.Member
import top.mcfpp.model.function.Function
import top.mcfpp.model.function.UnknownFunction
import top.mcfpp.type.MCFPPBaseType
import top.mcfpp.type.MCFPPType
import top.mcfpp.util.TempPool

abstract class BaseBool : Var<BaseBool> {

    override var type: MCFPPType = MCFPPBaseType.Bool

    /**
     * 创建一个bool值。它的标识符和mc名相同。
     * @param identifier identifier
     */
    constructor(identifier: String = TempPool.getVarIdentify()) : super(identifier)

    abstract override fun negation(): Var<*>?

    abstract override fun and(a: Var<*>): Var<*>?

    abstract override fun or(a: Var<*>): Var<*>?

    abstract fun toCommandPart(): Command

    override fun toNBTVar(): NBTBasedData {
        return toScoreBool().toNBTVar()
    }

    /**
     * 复制一个bool
     * @param b 被复制的int值
     */
    constructor(b: ScoreBool) : super(b)

    abstract fun toScoreBool(): ScoreBool

    override fun canAssignedBy(b: Var<*>): Boolean {
        return b is BaseBool
    }

    override fun getMemberVar(key: String, accessModifier: Member.AccessModifier): Pair<Var<*>?, Boolean> {
        return null to true
    }

    override fun getMemberFunction(
        key: String,
        readOnlyArgs: List<Var<*>>,
        normalArgs: List<Var<*>>,
        accessModifier: Member.AccessModifier
    ): Pair<Function, Boolean> {
        return UnknownFunction(key) to true
    }

}