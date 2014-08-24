package me.tatarka.socket.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.builder.core.VariantConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

class SocketPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.dependencies {
            compile 'me.tatarka.socket:socket:0.1'
        }

        project.plugins.withType(AppPlugin) {
            AppPlugin androidPlugin = project.plugins.getPlugin(AppPlugin)
            createSocketTask(project, androidPlugin)
        }
        
        project.plugins.withType(LibraryPlugin) {
            LibraryPlugin androidPlugin = project.plugins.getPlugin(LibraryPlugin)
            createSocketTask(project, androidPlugin)
        }
    }
    
    private static void createSocketTask(Project project, BasePlugin androidPlugin) {
        
        def variants = androidPlugin instanceof AppPlugin ?
                ((AppExtension) androidPlugin.extension).applicationVariants :
                ((LibraryExtension) androidPlugin.extension).libraryVariants

        def applicationId 
        variants.all { BaseVariant variant ->
            if (applicationId == null) applicationId = getApplicationId(androidPlugin)
            
            def taskName = "generateSockets${variant.name.capitalize()}"
            def outputDir = project.file("$project.buildDir/generated/source/socket/$variant.name")
            SocketTask task = project.task(taskName, dependsOn: [variant.mergeResources], type: SocketTask) {
                packageName = applicationId
                resDirectories = getResDirectories(project, variant)
                outputDirectory = outputDir
            }
            variant.registerJavaGeneratingTask(task, outputDir)
            variant.addJavaSourceFoldersToModel(outputDir)
        }
    }
    
    private static FileCollection getResDirectories(Project project, BaseVariant variant) {
        project.files(variant.sourceSets*.resDirectories.flatten())
    }

    /**
     * Gets the package name from the android plugin default configuration or the manifest if not
     * defined.
     * @param project the project with the android plugin
     * @return the main package name
     */
    private static String getApplicationId(BasePlugin androidPlugin) {
        def applicationId = androidPlugin.defaultConfigData.productFlavor.applicationId
        if (applicationId == null) {
            applicationId = VariantConfiguration.getManifestPackage(androidPlugin.defaultConfigData.sourceSet.manifestFile)
        }
        applicationId
    }
}