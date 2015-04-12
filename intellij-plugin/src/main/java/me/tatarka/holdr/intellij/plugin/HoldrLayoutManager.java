package me.tatarka.holdr.intellij.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.CommonProcessors;
import com.intellij.util.indexing.FileBasedIndex;
import me.tatarka.holdr.model.CompositeLayout;
import me.tatarka.holdr.model.Layout;
import me.tatarka.holdr.model.SingleLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by evan on 2/14/15.
 */
public class HoldrLayoutManager {
    private static final Key<HoldrLayoutManager> KEY = Key.create("me.tatarka.holdr.HoldrLayoutManager");

    public static HoldrLayoutManager getInstance(Project project) {
        HoldrLayoutManager layoutManager = project.getUserData(KEY);
        if (layoutManager == null) {
            layoutManager = new HoldrLayoutManager(project);
            project.putUserData(KEY, layoutManager);
        }
        return layoutManager;
    }

    private Project myProject;
    // Reuse CompositeLayout to reduce number of times merge is needed.
    private Map<String, CompositeLayout> layoutCache = new HashMap<String, CompositeLayout>();

    private HoldrLayoutManager(Project project) {
        myProject = project;
    }

    @Nullable
    public Layout getLayout(String layoutName) {
        final GlobalSearchScope scope = GlobalSearchScope.allScope(myProject);

        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
        List<File> layoutFiles = getLayoutFiles(layoutName, fileBasedIndex);
        if (layoutFiles.isEmpty()) {
            return null;
        }

        final List<SingleLayout> layouts = new ArrayList<SingleLayout>();
        for (File layoutFile : layoutFiles) {
            fileBasedIndex.processValues(HoldrLayoutIndex.INDEX_ID, layoutFile, null, new FileBasedIndex.ValueProcessor<SingleLayout>() {
                @Override
                public boolean process(VirtualFile virtualFile, SingleLayout singleLayout) {
                    layouts.add(singleLayout);
                    return true;
                }
            }, scope);
        }

        CompositeLayout compositeLayout = layoutCache.get(layoutName);
        if (compositeLayout == null) {
            compositeLayout = new CompositeLayout();
            layoutCache.put(layoutName, compositeLayout);
        }
        for (SingleLayout layout : layouts) {
            compositeLayout.put(layout);
        }

        return compositeLayout;
    }

    @NotNull
    public List<VirtualFile> getLayoutFiles(String layoutName) {
        final GlobalSearchScope scope = GlobalSearchScope.allScope(myProject);
        List<VirtualFile> files = new ArrayList<VirtualFile>();
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
        final List<File> layoutFiles = getLayoutFiles(layoutName, fileBasedIndex);
        fileBasedIndex.processFilesContainingAllKeys(HoldrLayoutIndex.INDEX_ID, layoutFiles, scope, Condition.TRUE, new CommonProcessors.CollectProcessor<VirtualFile>(files));
        return files;
    }

    @NotNull
    private List<File> getLayoutFiles(String layoutName, FileBasedIndex fileBasedIndex) {
        List<File> layoutFiles = new ArrayList<File>();
        fileBasedIndex.processAllKeys(HoldrLayoutIndex.INDEX_ID, HoldrLayoutIndex.filterLayoutByName(layoutName, new CommonProcessors.CollectProcessor<File>(layoutFiles)), myProject);
        return layoutFiles;
    }
}
