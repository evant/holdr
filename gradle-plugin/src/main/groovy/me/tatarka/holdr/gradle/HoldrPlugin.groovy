package me.tatarka.holdr.gradle

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import me.tatarka.holdr.compile.HoldrConfigImpl
import me.tatarka.holdr.model.HoldrConfig
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.tooling.provider.model.ToolingModelBuilder
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

import javax.inject.Inject

class HoldrPlugin implements Plugin<Project> {
    private static final String holdrVersion = '1.5.2'

    private final ToolingModelBuilderRegistry registry
    private BaseExtension androidExtension
    private HoldrExtension extension
    private String manifestPackage

    @Inject
    public HoldrPlugin(ToolingModelBuilderRegistry registry) {
        this.registry = registry
    }

    @Override
    void apply(Project project) {
        extension = project.extensions.create('holdr', HoldrExtension, this)

        project.plugins.withType(AppPlugin) { AppPlugin plugin ->
            androidExtension = project.extensions.findByName("android") as BaseExtension
            applyHoldrPlugin(project, ((AppExtension) androidExtension).applicationVariants)
        }

        project.plugins.withType(LibraryPlugin) {
            androidExtension = project.extensions.findByName("android") as BaseExtension
            applyHoldrPlugin(project, ((LibraryExtension) androidExtension).libraryVariants)
        }
    }

    private void applyHoldrPlugin(Project project, DomainObjectSet<? extends BaseVariant> variants) {
        project.dependencies.add("compile", "me.tatarka.holdr:holdr:${holdrVersion}@aar")

        variants.all { BaseVariant variant ->
            def taskName = "generate${variant.name.capitalize()}Holdr"
            def outputDir = project.file("$project.buildDir/generated/source/holdr/$variant.name")
            HoldrTask task = project.task(taskName, type: HoldrTask) as HoldrTask
            task.holdrPackage = extension.holdrPackage
            task.defaultInclude = extension.defaultInclude
            task.resDirectories = getResDirectories(project, variant)
            task.outputDirectory = outputDir
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
            def mainSourceSet = androidExtension.sourceSets.getByName(androidExtension.defaultConfig.name);
            manifestPackage = PackageNameFinder.packageName(mainSourceSet.manifest.srcFile)
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
            modelName == HoldrConfig.name
        }

        @Override
        Object buildAll(String modelName, Project project) {
            return new HoldrConfigImpl(
                    plugin.manifestPackage,
                    plugin.extension.holdrPackage,
                    plugin.extension.defaultInclude,
            )
        }
    }
}
