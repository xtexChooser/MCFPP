package top.mcfpp.io

import com.esotericsoftware.kryo.io.Input
import top.mcfpp.io.KryoManager.kryo
import top.mcfpp.io.info.GlobalFieldInfo
import top.mcfpp.model.field.GlobalField
import java.io.FileInputStream
import java.io.InputStream

object LibBinReader {

    fun read(path: String) {
        FileInputStream(path).use { stream ->
            readFromStream(stream)
        }
    }

    fun readFromStream(stream: InputStream) {
        Input(stream).use { input ->
            val info = kryo.readObject(input, GlobalFieldInfo::class.java)
            GlobalField.mergeInfo(info)
            //解析命名空间
            GlobalField.libNamespaces.values.forEach { it.resolve() }
            GlobalField.stdNamespaces.values.forEach { it.resolve() }
        }
    }
}
