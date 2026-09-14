import io.edenx.androidpark.lib
import io.edenx.androidpark.libs
import io.edenx.androidpark.pluginId
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/** For modules with no Android dependency at all, such as :core:model. */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("kotlin-jvm"))

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        tasks.withType(KotlinCompile::class.java).configureEach {
            compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        }

        dependencies {
            add("testImplementation", libs.lib("junit4"))
        }
    }
}
