package me.tatarka.holdr.intellij.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiFileReferenceHelper;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttributeValue;
import me.tatarka.holdr.model.Layout;
import me.tatarka.holdr.model.Ref;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.resourceManagers.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: evantatarka
 * Date: 10/3/14
 * Time: 5:15 PM
 */
public class HoldrPsiUtils {
    @Nullable
    public static PsiClass getClassForField(@NotNull PsiField field) {
        PsiTypeElement holdrClassType = field.getTypeElement();
        if (holdrClassType == null) {
            return null;
        }

        PsiJavaCodeReferenceElement elementReference = holdrClassType.getInnermostComponentReferenceElement();
        if (elementReference == null) {
            return null;
        }

        PsiElement element = elementReference.resolve();

        if (!(element instanceof PsiClass)) {
            return null;
        }

        return (PsiClass) element;
    }

    @Nullable
    public static List<PsiElement> findLayoutFiles(@NotNull HoldrModel holdrModel, @NotNull PsiClass holdrClass) {
        PsiManager manager = holdrClass.getManager();
        String layoutName = holdrModel.getLayoutName(holdrClass);
        List<VirtualFile> files = HoldrLayoutManager.getInstance(holdrClass.getProject()).getLayoutFiles(layoutName);
        List<PsiElement>  fileReferences = new ArrayList<PsiElement>(files.size());
        for (VirtualFile file : files) {
            PsiElement fileReference = PsiFileReferenceHelper.getPsiFileSystemItem(manager, file);
            fileReferences.add(fileReference);
        }
        return fileReferences;
    }

    @NotNull
    public static List<XmlAttributeValue> findIdReferences(@NotNull Layout layout, @NotNull PsiClass holdrClass, @NotNull String fieldName) {
        Ref ref = layout.findRefByFieldName(fieldName);
        if (ref == null) {
            return Collections.emptyList();
        }

        AndroidFacet androidFacet = AndroidFacet.getInstance(holdrClass);
        if (androidFacet == null) {
            return Collections.emptyList();
        }

        ResourceManager resourceManager = ref.isAndroidId
                ? androidFacet.getSystemResourceManager()
                : androidFacet.getLocalResourceManager();

        if (resourceManager == null) {
            return Collections.emptyList();
        }

        List<XmlAttributeValue> result = resourceManager.findIdDeclarations(ref.id);

        filterByHoldrName(layout.getName(), result);

        return result;
    }

    @Nullable
    public static PsiType findType(@NotNull String name, @NotNull Project project) {
        final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        PsiClass psiClass = javaPsiFacade.findClass(name, scope);
        if (psiClass == null) {
            return null;
        }
        return javaPsiFacade.getElementFactory().createType(psiClass);
    }

    private static void filterByHoldrName(String holdrLayout, Collection<? extends PsiElement> resourceList) {
        Iterator<? extends  PsiElement> iter = resourceList.iterator();
        while (iter.hasNext()) {
            PsiElement resource = iter.next();
            String layoutName = FileUtil.getNameWithoutExtension(resource.getContainingFile().getName());
            if (!holdrLayout.equals(layoutName)) {
                iter.remove();
            }
        }
    }
}
