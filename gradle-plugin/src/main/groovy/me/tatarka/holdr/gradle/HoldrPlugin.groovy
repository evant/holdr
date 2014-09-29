package me.tatarka.holdr.gradle
import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.android.builder.core.VariantConfiguration
import me.tatarka.holdr.compile.model.HoldrConfig
import me.tatarka.holdr.compile.model.HoldrConfigImpl
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.tooling.provider.model.ToolingModelBuilder
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

import javax.inject.Inject

class HoldrPlugin implements Plugin<Project> {
    private final ToolingModelBuilderRegistry registry
    private HoldrExtension extension
    private BasePlugin androidPlugin
    private String manifestPackage

    @Inject
    public HoldrPlugin(ToolingModelBuilderRegistry registry) {
        this.registry = registry
    }

    @Override
    void apply(Project project) {
        extension = project.extensions.create('holdr', HoldrExtension, this)

        project.plugins.withType(AppPlugin) {
            androidPlugin = project.plugins.getPlugin(AppPlugin)
            applyHoldrPlugin(project)
        }
        
        project.plugins.withType(LibraryPlugin) {
            androidPlugin = project.plugins.getPlugin(LibraryPlugin)
            applyHoldrPlugin(project)
        }
    }
    
    private void applyHoldrPlugin(Project project) {
        project.dependencies {
            compile 'me.tatarka.holdr:holdr:1.3.0-SNAPSHOT@aar'
        }
        
        def variants = androidPlugin instanceof AppPlugin ?
                ((AppExtension) androidPlugin.extension).applicationVariants :
                ((LibraryExtension) androidPlugin.extension).libraryVariants

        variants.all { BaseVariant variant ->
            def taskName = "generate${variant.name.capitalize()}Holdr"
            def outputDir = project.file("$project.buildDir/generated/source/holdr/$variant.name")
            def task = project.task(taskName, dependsOn: [variant.mergeResources], type: HoldrTask) {
                holdrPackage = extension.holdrPackage
                defaultInclude = extension.defaultInclude
                resDirectories = getResDirectories(project, variant)
                outputDirectory = outputDir
            }
            task.manifestPackage = getManifestPackage()
            variant.registerJavaGeneratingTask(task, outputDir)
            variant.addJavaSourceFoldersToModel(outputDir)
        }

        registry.register(new HoldrToolingModelBuilder(this))
    }
    
    private static FileCollection getResDirectories(Project project, BaseVariant variant) {
        project.files(variant.sourceSets*.resDirectories.flatten())
    }

    String getManifestPackage() {
        if (manifestPackage == null) {
            manifestPackage = VariantConfiguration.getManifestPackage(androidPlugin.defaultConfigData.sourceSet.manifestFile)
        }
        return manifestPackage
    }

    public HoldrExtension getExtension() {
        return extension
    }

    private static class HoldrToolingModelBuilder implements ToolingModelBuilder {
        HoldrPlugin plugin

        public HoldrToolingModelBuilder(HoldrPlugin plugin) {
            this.plugin = plugin
        }

        @Override
        boolean canBuild(String modelName) {
            println("can build: $modelName ? ${modelName == HoldrConfig.name}")
            modelName == HoldrConfig.name
        }

        @Override
        Object buildAll(String modelName, Project project) {
            println("build all: $modelName")
            return new HoldrConfigImpl(
                    plugin.manifestPackage,
                    plugin.extension.holdrPackage,
                    plugin.extension.defaultInclude
            )
        }
    }
}
