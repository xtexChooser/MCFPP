package top.mcfpp.doc

import top.mcfpp.util.LogProcessor

abstract class DocumentTag {
    abstract val tag: String
    abstract val content: String

    companion object{
        fun parse(input: String): DocumentTag {
            val part = input.split(" ", limit = 2)
            when(part[0]){
                "since" -> return SinceTag(part[1])
                "deprecated" -> return DeprecatedTag(part[1])
                "see" -> return SeeTag(part[1])
                "version" -> return VersionTag(part[1])
                "author" -> return AuthorTag(part[1])
                "base" -> return BaseTag(part[1])
                "param" -> {
                    val param = part[1].split(" ", limit = 2)
                    if(param.size == 1){
                        return ParamTag(param[0], "")
                    }
                    return ParamTag(param[0], param[1])
                }
                "return" -> return ReturnTag(part[1])
                "throws" -> {
                    val exception = part[1].split(" ", limit = 2)
                    if (exception.size == 1) {
                        return ThrowsTag(exception[0], "")
                    }
                    return ThrowsTag(exception[0], exception[1])
                }
                "context" -> {
                    val context = part[1].split(" ", limit = 2)
                    if(!ContextTag.availableContexts.contains(context[0])){
                        LogProcessor.warn("Unknown context: ${context[0]}")
                    }
                    return if(context.size == 1){
                        ContextTag(context[0], "")
                    }else{
                        ContextTag(context[0], context[1])
                    }
                }
                else -> throw Exception("Unknown tag: ${part[0]}")
            }
        }
    }
}

data class SinceTag(override val content: String) : DocumentTag(){
    override val tag: String = "since"
}

data class DeprecatedTag(override val content: String) : DocumentTag(){
    override val tag: String = "deprecated"
}

data class SeeTag(override val content: String) : DocumentTag(){
    override val tag: String = "see"
}

data class VersionTag(override val content: String) : DocumentTag(){
    override val tag: String = "version"
}

data class AuthorTag(override val content: String) : DocumentTag(){
    override val tag: String = "author"
}

data class BaseTag(override val content: String) : DocumentTag() {
    override val tag: String = "base"
}

data class ParamTag(val param: String , override val content: String) : DocumentTag() {
    override val tag: String = "param"
}

data class ReturnTag(override val content: String) : DocumentTag() {
    override val tag: String = "return"
}

data class ThrowsTag(val exception: String, override val content: String) : DocumentTag() {
    override val tag: String = "throws"
}

data class ContextTag(val context: String, override val content: String) : DocumentTag() {
    override val tag: String = "context"

    companion object {
        val availableContexts = listOf("entity","pos","rotation","dimension")
    }
}
