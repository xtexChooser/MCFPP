package top.mcfpp.io.info

import top.mcfpp.lib.SbObject
import java.io.Serializable

class GlobalFieldInfo(

    /**
     * 当前项目内声明的命名空间
     */
    val localNamespaces: Map<String, NamespaceInfo>,

    /**
     * 函数的标签
     */
    var functionTags: Map<String, FunctionTagInfo>,

    /**
     * 记分板
     */
    var scoreboards: Map<String, SbObject>
): Serializable