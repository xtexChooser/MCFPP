package top.mcfpp.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.SourceSetContainer
import top.mcfpp.ProjectConfig
import top.mcfpp.compile
import top.mcfpp.io.MCFPPFile
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

class MCFPPGradlePlugin: Plugin<Project> {
    override fun apply(project: Project) {

        project.afterEvaluate { proj ->
            val repositories = proj.repositories
            listOf(
                "https://jitpack.io",
                "https://maven.aliyun.com/nexus/content/groups/public/",
                "https://libraries.minecraft.net"
            ).forEach {e ->
                if (repositories.none { it is MavenArtifactRepository && it.url.toString() == e }) {
                    repositories.maven { it.setUrl(e) }
                }
            }
        }

        project.plugins.apply("java")
        project.plugins.apply("org.jetbrains.kotlin.jvm")

        // 配置Java编译任务
        val javaCompile = project.tasks.named("compileJava")
        javaCompile.configure {
            it.dependsOn("compileKotlin")
        }

        val config = project.extensions.create("mcfpp", ProjectConfig::class.java)

        // 自定义任务来调用MCFPP
        project.tasks.register("mcfppCompile") {
            it.group = "build"
            it.description = "Compile mcfpp files"

            it.dependsOn("jar")

            if(config.root == null) config.root = project.rootDir.toPath()

            if(config.sourcePath == null) {
                config.sourcePath = Path(config.root!!.absolutePathString(), "src/main/mcfpp")
            }
            else if(config.sourcePath?.isAbsolute == false) {
                config.sourcePath = config.root!!.resolve(config.sourcePath!!)
            }

            if(config.targetPath == null) {
                config.targetPath = project.layout.buildDirectory.get().dir("datapack").asFile.toPath()
            }
            else if(config.targetPath?.isAbsolute == false) {
                config.targetPath = config.root!!.resolve(config.targetPath!!)
            }

            val jars = (project.tasks.named("jar").get() as org.gradle.api.tasks.bundling.Jar).destinationDirectory.get().files()

            it.doLast {
                for (jar in jars){
                    config.jars.add(jar.absolutePath);
                }
                compile(config)
            }

        }
    }
}