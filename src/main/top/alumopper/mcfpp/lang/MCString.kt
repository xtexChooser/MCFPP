package top.alumopper.mcfpp.lang

import top.alumopper.mcfpp.exception.VariableConverseException

class MCString : Var {
    var value: String? = null
    override var type = "string"

    constructor(value: String?) {
        this.value = value
        isTemp = true
    }

    constructor(value: String?, identifier: String?) {
        this.identifier = identifier!!
    }

    constructor(b: MCString) {
        value = b.value
        type = b.type
    }


    @Override
    override fun toString(): String {
        return value!!
    }

    @Override
    @Throws(VariableConverseException::class)
    override fun assign(b: Var?) {
    }

    @Override
    override fun cast(type: String): Var? {
        return null
    }

    @Override
    override fun clone(): MCString {
        TODO()
    }
}