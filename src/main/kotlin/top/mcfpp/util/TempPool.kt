package top.mcfpp.util

object TempPool {

    var varCount = 0

    var functionCount = 0

    var anonymousTemplateCount = 0
    fun getVarId(): Int {
        return varCount++
    }

    fun getVarIdentify(): String {
        return "temp_${getVarId()}"
    }

    fun getFunctionId(): Int {
        return functionCount++
    }

    fun getFunctionIdentify(prefix: String): String {
        return "${prefix}_${getFunctionId()}"
    }

    fun getAnonymousTemplateID(): Int {
        return anonymousTemplateCount++
    }

    fun getAnonymousTemplateIdentify(): String {
        return "data_${getAnonymousTemplateID()}"
    }
}