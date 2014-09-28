package me.tatarka.holdr.intellij.plugin;

import com.android.resources.ResourceFolderType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidCommonUtils;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by evan on 9/27/14.
 */
public class HoldrLayoutFilesListener extends BulkFileListener.Adapter implements Disposable {
    private final MergingUpdateQueue myQueue;
    private final Project myProject;

    public HoldrLayoutFilesListener(@NotNull Project project) {
        myProject = project;
        myQueue = new MergingUpdateQueue("HoldrLayoutFilesListener", 300, true, null, this, null, false);
        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(VirtualFileManager.VFS_CHANGES, this);
    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        final Set<VirtualFile> filesToProcess = getFilesToProcess(events);

        if (filesToProcess.size() > 0) {
            myQueue.queue(new MyUpdate(filesToProcess));
        }
    }

    @NotNull
    private static Set<VirtualFile> getFilesToProcess(@NotNull List<? extends VFileEvent> events) {
        final Set<VirtualFile> result = new HashSet<VirtualFile>();

        for (VFileEvent event : events) {
            final VirtualFile file = event.getFile();

            if (file != null && shouldScheduleUpdate(file)) {
                result.add(file);
            }
        }
        return result;
    }

    private static boolean shouldScheduleUpdate(@NotNull VirtualFile file) {
        final FileType fileType = file.getFileType();

        if (fileType == StdFileTypes.XML) {
            final VirtualFile parent = file.getParent();

            if (parent != null && parent.isDirectory()) {
                final String resType = AndroidCommonUtils.getResourceTypeByDirName(parent.getName());
                return ResourceFolderType.LAYOUT.getName().equals(resType);
            }
        }
        return false;
    }

    @Override
    public void dispose() {

    }

    private class MyUpdate extends Update {
        private final Set<VirtualFile> myFiles;

        public MyUpdate(@NotNull Set<VirtualFile> files) {
            super(files);
            myFiles = files;
        }

        @Override
        public void run() {
            if (ApplicationManager.getApplication().isUnitTestMode()) {
                return;
            }

            ApplicationManager.getApplication().runReadAction(new Runnable() {
                @Override
                public void run() {
                    updateHoldrModel();
                }
            });
        }

        private void updateHoldrModel() {
            for (VirtualFile file : myFiles) {
                final Set<Module> holdrModulesToInvalidate = new HashSet<Module>();

                final Module module = ModuleUtilCore.findModuleForFile(file, myProject);

                if (module == null || module.isDisposed()) {
                    continue;
                }
                final AndroidFacet facet = AndroidFacet.getInstance(module);

                if (facet == null) {
                    continue;
                }

                final VirtualFile parent = file.getParent();
                final VirtualFile gp = parent != null ? parent.getParent() : null;
                final List<VirtualFile> resourceDirs =  facet.getAllResourceDirectories();

                for (VirtualFile resourceDir : resourceDirs) {
                    if (gp != null &&
                            Comparing.equal(gp, resourceDir) &&
                            ResourceFolderType.LAYOUT.getName().equals(AndroidCommonUtils.getResourceTypeByDirName(parent.getName()))) {
                        holdrModulesToInvalidate.add(module);
                    }
                }

                invalidHoldrModules(holdrModulesToInvalidate);
            }
        }

        private void invalidHoldrModules(Set<Module> modules) {
            for (Module module : AndroidUtils.getSetWithBackwardDependencies(modules)) {
                HoldrModel holdrModel = HoldrModel.getInstance(AndroidFacet.getInstance(module));

                if (holdrModel != null) {
                    holdrModel.invalidateLayouts();
                }
            }
        }
    }
}
