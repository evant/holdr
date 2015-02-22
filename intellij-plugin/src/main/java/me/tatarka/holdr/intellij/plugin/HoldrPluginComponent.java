package me.tatarka.holdr.intellij.plugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by evan on 9/27/14.
 */
public class HoldrPluginComponent implements ProjectComponent {
    private final Project myProject;

    public HoldrPluginComponent(Project myProject) {
        this.myProject = myProject;
    }

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {
    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "HoldrPluginComponent";
    }
}
