package me.tatarka.holdr.gradle

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

class HoldrPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {

        def holdr = project.extensions.create('holdr', HoldrExtension)

        project.plugins.withType(AppPlugin) {
            AppPlugin androidPlugin = project.plugins.getPlugin(AppPlugin)
            createHoldrTasks(project, androidPlugin, holdr)
        }
        
        project.plugins.withType(LibraryPlugin) {
            LibraryPlugin androidPlugin = project.plugins.getPlugin(LibraryPlugin)
            createHoldrTasks(project, androidPlugin, holdr)
        }
    }
    
    private static void createHoldrTasks(Project project, BasePlugin androidPlugin, HoldrExtension holdr) {
        project.dependencies {
            compile 'me.tatarka.holdr:holdr:1.1.0@aar'
        }
        
        def variants = androidPlugin instanceof AppPlugin ?
                ((AppExtension) androidPlugin.extension).applicationVariants :
                ((LibraryExtension) androidPlugin.extension).libraryVariants

        def applicationId 
        variants.all { BaseVariant variant ->
            if (applicationId == null) applicationId = getApplicationId(androidPlugin)
            
            def taskName = "generate${variant.name.capitalize()}Holdr"
            def outputDir = project.file("$project.buildDir/generated/source/holdr/$variant.name")
            HoldrTask task = project.task(taskName, dependsOn: [variant.mergeResources], type: HoldrTask) {
                packageName = applicationId
                resDirectories = getResDirectories(project, variant)
                outputDirectory = outputDir
                defaultInclude = holdr.defaultInclude
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
        VariantConfiguration.getManifestPackage(androidPlugin.defaultConfigData.sourceSet.manifestFile)
    }
}
