package top.mcfpp.io.info

import top.mcfpp.model.function.ClassConstructor

data class ConstructorInfo(
    val normalParams: List<FunctionParamInfo>
): ModelInfo<ClassConstructor> {
    override fun get(): ClassConstructor {
        val constructor = ClassConstructor(AbstractClassInfo.currClass!!)
        normalParams.forEach {
            constructor.normalParams.add(it.get())
        }
        return constructor
    }

    companion object {
        fun from(constructor: ClassConstructor): ConstructorInfo {
            return ConstructorInfo(constructor.normalParams.map { FunctionParamInfo.from(it) })
        }
    }
}