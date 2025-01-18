package top.mcfpp.io

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import top.mcfpp.Project
import top.mcfpp.model.*
import top.mcfpp.model.field.GlobalField
import top.mcfpp.model.function.ExtensionFunction
import top.mcfpp.model.function.Function
import top.mcfpp.util.LogProcessor
import top.mcfpp.util.StringHelper.toSnakeCase
import top.mcfpp.util.Utils
import java.io.*
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream.Collectors
import kotlin.io.path.absolutePathString
import kotlin.io.path.name


/**
 * 用于创建一个数据包的框架。
 * 一个完整的数据包包含(加粗者为重要组成部分，也是默认包含的部分):
 *
 *  * **进度(advancement)**
 *  * 聊天类型(chat_type)
 *  * 数据包(datapacks)
 *  * **函数(functions)**
 *  * **战利品表(loot_tables)**
 *  * **谓词(predicates)**
 *  * 结构(structures)
 *  * 配方(recipes)
 *  * **物品修饰器(item_modifiers)**
 *  * **标签(tags)**
 *  * 维度(dimension)
 *  * 维度类型(dimension_type)
 *  * 世界生成(worldgen)
 *
 * 加粗的部分表示对一般数据包的逻辑实现几乎必不可少的部分。
 *
 *
 */
object DatapackCreator {

    /**
     * 在指定的路径生成一个数据包的框架
     * @param path 路径
     */
    fun createDatapack(path: String) {
        LogProcessor.debug("Clearing output folder...")
        //清空原输出文件夹
        delAllFile(File("$path/${Project.config.name}"))
        LogProcessor.debug("Copy libs...")
        //复制库
        for (lib in Project.config.includes){
            val filePath = if(!lib.endsWith("/.mclib")) {
                "$lib/.mclib"
            }else{
                lib
            }
            //逐行读取
            val fileReader = FileReader(filePath)
            val jsonString = fileReader.readText()
            fileReader.close()
            //解析json
            val json = JSONObject.parse(jsonString) as JSONObject
            val scr = json.getString("src")
            if(scr != null){
                val scrPath = filePath.substring(0,filePath.lastIndexOf(".")) + scr
                val qwq = Paths.get(scrPath)
                // 获取所有子文件夹
                val subdirectories = Files.walk(qwq, 1)
                    .filter(Files::isDirectory)
                    .skip(1)
                    .collect(Collectors.toList())
                for (subdirectory in subdirectories) {
                    //复制文件夹
                    delAllFile(File(path + "\\" + subdirectory.name))
                    copyAllFiles(subdirectory.absolutePathString(),path + "\\" + subdirectory.name)
                }
            }
        }
        LogProcessor.debug("Creating datapack...")
        //生成
        val datapackMcMeta = DatapackMcMeta(
            DatapackMcMeta.Pack(
                Utils.getVersion(Project.config.version),
                Project.config.description
            )
        )
        val datapackMcMetaJson: String = JSON.toJSONString(datapackMcMeta)
        //创建文件夹
        try {
            Files.createDirectories(Paths.get("$path/${Project.config.name}/data"))
            //创建pack.mcmeta
            Files.write(Paths.get("$path/${Project.config.name}/pack.mcmeta"), datapackMcMetaJson.toByteArray())
            for(namespace in GlobalField.localNamespaces){
                genNamespace(path, namespace)
            }
            for (namespace in GlobalField.stdNamespaces){
                genNamespace(path, namespace)
            }
            //写入标签json文件
            for (tag in GlobalField.functionTags.values) {
                LogProcessor.debug("Writing File: " + path + "\\${Project.config.name}\\data\\" + tag.namespace + "\\tags\\function\\" + tag.tag + ".json")
                Files.createDirectories(Paths.get(path + "/${Project.config.name}/data/" + tag.namespace + "/tags/function"))
                Files.write(
                    Paths.get(path + "/${Project.config.name}/data/" + tag.namespace + "/tags/function/" + tag.tag + ".json"),
                    tag.tagJSON.toByteArray()
                )
            }
        } catch (e: IOException) {
            throw e
        }
    }

    private fun genFunction(currPath: String, f: Function){
        if (f is Native) return
        LogProcessor.debug("Writing File: $currPath\\${f.nameWithNamespace}.mcfunction")
        f.commands.analyzeAll()
        val path = if(f is ExtensionFunction){
            "$currPath\\ex"
        }else{
            currPath
        }
        Files.createDirectories(Paths.get(path))
        Files.write(Paths.get("$path\\${f.identifier}.mcfunction"), f.cmdStr.toByteArray())
        if(f.compiledFunctions.isNotEmpty()){
            for (cf in f.compiledFunctions.values) {
                LogProcessor.debug("Writing File: $currPath\\${cf.identifier}.mcfunction")
                f.commands.analyzeAll()
                Files.write(Paths.get("$path\\${f.identifier}.mcfunction"), cf.cmdStr.toByteArray())
            }
        }
    }

    private fun genTemplateFunction(currPath: String, f: Function){
        if (f is Native) return
        val path = if(f is ExtensionFunction) "$currPath\\ex" else currPath
        Files.createDirectories(Paths.get(path))
        for (cf in f.compiledFunctions.values) {
            LogProcessor.debug("Writing File: $currPath\\${cf.identifier}.mcfunction")
            f.commands.analyzeAll()
            Files.write(Paths.get("$path\\${f.identifier}.mcfunction"), cf.cmdStr.toByteArray())
        }
    }

    private fun genObject(currPath: String, obj: CompoundData){
        //成员
        obj.field.forEachFunction {
            genFunction("${currPath}\\function\\${obj.identifier.toSnakeCase()}\\static", it)
        }
    }

    private fun genTemplate(currPath: String, t: DataTemplate){
        //成员
        t.field.forEachFunction {
            genTemplateFunction("$currPath\\function\\${t.identifier.toSnakeCase()}", it)
        }
        t.constructors.forEach {
            genTemplateFunction("$currPath\\function\\${t.identifier.toSnakeCase()}", it)
        }
    }

    private fun genClass(currPath: String, cls: Class) {
        //成员
        cls.field.forEachFunction {
            genFunction("$currPath\\function\\${cls.identifier.toSnakeCase()}", it)
        }
        cls.constructors.forEach {
            genFunction("$currPath\\function\\${cls.identifier.toSnakeCase()}", it)
        }
    }

    private fun genNamespace(path: String, namespace: MutableMap.MutableEntry<String, Namespace>) {
        val currPath = "$path\\${Project.config.name}\\data\\${namespace.key}"

        namespace.value.field.forEachFunction {
            genFunction("$currPath\\function", it)
        }

        namespace.value.field.forEachClass {
            genClass(currPath, it)
        }

        namespace.value.field.forEachTemplate {
            genTemplate(currPath, it)
        }

        namespace.value.field.forEachObject {
            genObject(currPath, it)
        }
    }

    /**
     * 删除原有的数据包中的全部内容。
     * @param directory 文件或文件夹
     */
    private fun delAllFile(directory: File) {
        if (!directory.isDirectory) {
            directory.delete()
        } else {
            val files: Array<out File>? = directory.listFiles()
            // 空文件夹
            if (files!!.isEmpty()) {
                directory.delete()
                return
            }
            // 删除子文件夹和子文件
            for (file in files) {
                    if (file.isDirectory) {
                        delAllFile(file)
                    } else {
                        file.delete()
                    }
                }

            // 删除文件夹本身
            directory.delete()
        }
    }

    @Throws(IOException::class)
    fun copyAllFiles(src: String, dst: String) {
        val srcFolder = Paths.get(src)
        val dstFolder = Paths.get(dst)
        Files.walkFileTree(srcFolder, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                val targetPath = dstFolder.resolve(srcFolder.relativize(dir))
                try {
                    Files.copy(dir, targetPath, StandardCopyOption.REPLACE_EXISTING)
                } catch (e: FileAlreadyExistsException) {
                    if (!Files.isDirectory(targetPath)) throw e
                }
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.copy(file, dstFolder.resolve(srcFolder.relativize(file)), StandardCopyOption.REPLACE_EXISTING)
                return FileVisitResult.CONTINUE
            }
        })
    }
    /**
     * 数据包的元数据。用于创建pack.mcmeta文件。
     *
     * @property pack
     * @constructor Create empty Datapack mc meta
     */
    internal class DatapackMcMeta(var pack: Pack) {
        internal class Pack(var pack_format: Int, var description: String)
    }
}