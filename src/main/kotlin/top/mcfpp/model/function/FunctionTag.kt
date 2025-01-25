package top.mcfpp.model.function

import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter
import top.mcfpp.Project
import java.io.Serializable

/**
 * 一个函数的标签
 */
class FunctionTag(namespace: String?, var identifier: String): Serializable {
    /**
     * 函数标签的命名空间
     */
    var namespace: String

    /**
     * 这个标签含有那些函数
     */
    @Transient
    var functions: ArrayList<Function> = ArrayList()

    val namespaceID: String
        get() = "$namespace:$identifier"

    val tagJSON: String
        get() {
            val json = JSONObject()
            val values = JSONArray()
            for (f in functions) {
                values.add(f.namespaceID)
            }
            json["values"] = values
            return json.toString(JSONWriter.Feature.PrettyFormat)
        }

    init {
        if(namespace == null){
            if (identifier == "tick" || identifier == "load") {
                this.namespace = MINECRAFT
            } else {
                this.namespace = Project.currNamespace
            }
        }else{
            this.namespace = namespace
        }
    }

    @Override
    override fun equals(other: Any?): Boolean {
        return if (other is FunctionTag) {
            other.namespace == namespace && other.identifier == identifier
        } else false
    }

    override fun hashCode(): Int {
        return namespace.hashCode()
    }

    companion object {
        const val MINECRAFT = "minecraft"
        val TICK = FunctionTag(MINECRAFT, "tick")
        val LOAD = FunctionTag(MINECRAFT, "load")
    }
}