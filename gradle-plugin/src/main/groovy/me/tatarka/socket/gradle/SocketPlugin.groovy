package me.tatarka.socket.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.core.VariantConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project

class SocketPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.dependencies {
            compile 'me.tatarka.socket:socket:0.1'
        }

        project.plugins.withType(AppPlugin) {
            AppPlugin androidPlugin = project.plugins.getPlugin(AppPlugin)
            
            project.afterEvaluate {
                def applicationId = getApplicationId(androidPlugin)

                if (androidPlugin instanceof AppPlugin) {
                    ((AppExtension) androidPlugin.extension).applicationVariants.all { ApplicationVariant variant ->
                        def taskName = "generateSockets${variant.name.capitalize()}"
                        def out = project.file("$project.buildDir/generated/source/socket/$variant.name")
                        def task = project.task(taskName, dependsOn: [variant.mergeResources], type: SocketTask) {
                            packageName = applicationId
                            resDir = variant.mergeResources.outputDir
                            outputDir = out
                        }
                        variant.registerJavaGeneratingTask(task, out)
                        variant.addJavaSourceFoldersToModel(out)
                    }
                }
            }
        }
    }

    /**
     * Gets the package name from the android plugin default configuration or the manifest if not
     * defined.
     * @param project the project with the android plugin
     * @return the main package name
     */
    private static String getApplicationId(BasePlugin androidPlugin) {
        def packageName = androidPlugin.defaultConfigData.productFlavor.applicationId
        if (packageName == null) {
            packageName = VariantConfiguration.getManifestPackage(androidPlugin.defaultConfigData.sourceSet.manifestFile)
        }
        packageName
    }
}