package top.mcfpp.core.lang.nbt

import net.querz.nbt.tag.ByteTag
import net.querz.nbt.tag.LongTag
import top.mcfpp.annotations.InsertCommand
import top.mcfpp.command.Commands
import top.mcfpp.core.lang.*
import top.mcfpp.core.lang.bool.ScoreBoolConcrete
import top.mcfpp.model.FieldContainer
import top.mcfpp.model.function.Function
import top.mcfpp.type.MCFPPBaseType
import top.mcfpp.type.MCFPPNBTType
import top.mcfpp.type.MCFPPType
import top.mcfpp.util.LogProcessor
import top.mcfpp.util.TextTranslator
import top.mcfpp.util.TextTranslator.translate
import java.util.*

open class MCByte: MCInt {

    override var type: MCFPPType = MCFPPNBTType.Byte
    constructor(
        curr: FieldContainer,
        identifier: String = UUID.randomUUID().toString()
    ) : super(curr, identifier)

    constructor(identifier: String = UUID.randomUUID().toString()) : super(identifier)

    constructor(b: MCByte) : super(b)

    constructor(b: MCInt): super(b)

    override fun doAssignedBy(b: Var<*>) : MCByte {
        return when (b) {
            is MCByte -> {
                MCByte(assignCommand(b))
            }

            is CommandReturn -> {
                if(parentClass() != null){
                    Function.addCommands(
                        Commands.selectRun(parent!!, b.command, false)
                    )
                }else{
                    Function.addCommand(b.command)
                }
                MCByte(this)
            }

            else -> {
                LogProcessor.error(TextTranslator.ASSIGN_ERROR.translate(b.type.typeName, type.typeName))
                this
            }
        }
    }

    override fun canAssignedBy(b: Var<*>): Boolean {
        if(!b.implicitCast(type).isError) return true
        return when(b){
            is CommandReturn -> true
            else -> false
        }
    }

    override fun implicitCast(type: MCFPPType): Var<*> {
        val re = super.implicitCast(type)
        if(!re.isError) return re
        return when (type) {
            MCFPPNBTType.Short -> MCShort(this)
            else -> re
        }
    }

}


class MCByteConcrete: MCByte, MCFPPValue<Byte> {

    override var value: Byte
    /**
     * 创建一个固定的string
     *
     * @param identifier 标识符
     * @param curr 域容器
     * @param value 值
     */
    constructor(
        curr: FieldContainer,
        value: Byte,
        identifier: String = UUID.randomUUID().toString()
    ) : super(curr.prefix + identifier) {
        this.value = value
    }

    /**
     * 创建一个固定的string。它的标识符和mc名一致/
     * @param identifier 标识符。如不指定，则为随机uuid
     * @param value 值
     */
    constructor(value: Byte, identifier: String = UUID.randomUUID().toString()) : super(identifier) {
        this.value = value
    }

    constructor(v: MCByte, value: Byte) : super(v) {
        this.value = value
    }

    constructor(v: MCByteConcrete) : super(v) {
        this.value = v.value
    }

    override fun clone(): MCByteConcrete {
        return MCByteConcrete(this)
    }

    override fun toDynamic(replace: Boolean): Var<*> {
        MCIntConcrete(this, value.toInt()).toDynamic(replace)
        return MCByte(this)
    }

    override fun plus(a: Var<*>): Var<*>? {
        //t = t + a
        when(a){
            is MCByteConcrete -> {
                value = (value + a.value).toByte()
                return this
            }
            is MCByte -> {
                return a.plus(this)
            }
            else -> return null
        }
    }

    override fun minus(a: Var<*>): Var<*>? {
        //t = t + a
        when(a){
            is MCByteConcrete -> {
                value = (value - a.value).toByte()
                return this
            }
            is MCByte -> {
                return a.minus(this)
            }
            else -> return this
        }
    }

    override fun multiple(a: Var<*>): Var<*>? {
        //t = t * a
        when(a){
            is MCByteConcrete -> {
                value = (value * a.value).toByte()
                return this
            }
            is MCByte -> {
                return a.multiple(this)
            }
            else -> return this
        }
    }

    override fun divide(a: Var<*>): Var<*>? {
        //t = t / a
        when(a){
            is MCByteConcrete -> {
                value = (value / a.value).toByte()
                return this
            }
            is MCByte -> {
                return a.divide(this)
            }
            else -> return this
        }
    }

    override fun modular(a: Var<*>): Var<*>? {
        //t = t % a
        when(a){
            is MCByteConcrete -> {
                value = (value % a.value).toByte()
                return this
            }
            is MCByte -> {
                return a.modular(this)
            }
            else -> return this
        }
    }

    override fun isBigger(a: Var<*>): Var<*>? {
        //re = t > a
        if (a !is MCByte) return null
        return if (a is MCByteConcrete) {
            ScoreBoolConcrete(value > a.value)
        } else {
            //注意大小于换符号！
            a.isSmaller(this)
        }
    }

    override fun isSmaller(a: Var<*>): Var<*>? {
        //re = t < a
        if (a !is MCByte) return null
        return if (a is MCByteConcrete) {
            ScoreBoolConcrete(value < a.value)
        } else {
            a.isBigger(this)
        }
    }

    override fun isSmallerOrEqual(a: Var<*>): Var<*>? {
        //re = t <= a
        if (a !is MCByte) return null
        return if (a is MCByteConcrete) {
            ScoreBoolConcrete(value <= a.value)
        } else {
            a.isBiggerOrEqual(this)
        }
    }

    override fun isBiggerOrEqual(a: Var<*>): Var<*>? {
        //re = t <= a
        if (a !is MCByte) return null
        return if (a is MCByteConcrete) {
            ScoreBoolConcrete(value >= a.value)
        } else {
            a.isSmallerOrEqual(this)
        }
    }

    override fun isEqual(a: Var<*>): Var<*>? {
        //re = t == a
        if (a !is MCByte) return null
        return if (a is MCByteConcrete) {
            ScoreBoolConcrete(value == a.value)
        } else {
            a.isEqual(this)
        }
    }

    override fun isNotEqual(a: Var<*>): Var<*>? {
        //re = t != a
        if (a !is MCByte) return null
        return if (a is MCByteConcrete) {
            ScoreBoolConcrete(value != a.value)
        } else {
            a.isNotEqual(this)
        }
    }

    /**
     * 获取临时变量
     *
     * @return 返回临时变量
     */
    override fun getTempVar(): MCByte {
        if (isTemp) return this
        return MCByteConcrete(value)
    }

}
