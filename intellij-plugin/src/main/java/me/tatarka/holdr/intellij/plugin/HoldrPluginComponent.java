package me.tatarka.holdr.intellij.plugin;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetManagerAdapter;
import com.intellij.facet.ProjectFacetManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.messages.MessageBusConnection;

import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;

/**
 * Created by evan on 9/27/14.
 */
public class HoldrPluginComponent implements ProjectComponent {
    private Disposable myDisposable;
    private final Project myProject;

    public HoldrPluginComponent(Project myProject) {
        this.myProject = myProject;
    }

    @Override
    public void projectOpened() {
        myDisposable = new Disposable() {
            @Override
            public void dispose() {
            }
        };

        if (!ApplicationManager.getApplication().isUnitTestMode() &&
                !ApplicationManager.getApplication().isHeadlessEnvironment()) {

            if (ProjectFacetManager.getInstance(myProject).hasFacets(AndroidFacet.ID)) {
                createHoldrLayoutFilesListener();
            } else {
                final MessageBusConnection connection = myProject.getMessageBus().connect(myDisposable);

                connection.subscribe(FacetManager.FACETS_TOPIC, new FacetManagerAdapter() {
                    @Override
                    public void facetAdded(@NotNull Facet facet) {
                        if (facet instanceof AndroidFacet) {
                            createHoldrLayoutFilesListener();
                            connection.disconnect();
                        }
                    }
                });
            }
        }
    }

    private void createHoldrLayoutFilesListener() {
        final HoldrLayoutFilesListener listener = new HoldrLayoutFilesListener(myProject);
        Disposer.register(myDisposable, listener);
    }

    @Override
    public void projectClosed() {
        Disposer.dispose(myDisposable);
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
