package top.mcfpp.util

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import top.mcfpp.Project
import top.mcfpp.io.McfppFile

object LogProcessor {

    private var logger: Logger = LogManager.getLogger("mcfpp")

    var errorCount = 0

    var warningCount = 0

    fun debug(msg: String){
        logger.debug(msg)
    }

    fun info(msg: String){
        logger.info(msg)
    }

    fun warn(msg: String){
        logger.warn(msg)
        warningCount++
    }

    fun error(msg: String){
        logger.error(msg)
        errorCount++
    }

    fun compileWarn(msg: String){
        logger.warn(
            msg + if(Project.ctx !=null) {" at " + McfppFile.currFile!!.name + ":" + Project.ctx!!.getStart().line}else{""}
        )
        warningCount++
    }

    fun compileError(msg: String){
        logger.error(
            msg + if(Project.ctx !=null) {" at " + McfppFile.currFile!!.name + ":" + Project.ctx!!.getStart().line}else{""}
        )
        errorCount++
    }
}