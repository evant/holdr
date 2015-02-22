package me.tatarka.holdr.intellij.plugin;

import com.android.SdkConstants;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.FilteringProcessor;
import com.intellij.util.Processor;
import com.intellij.util.containers.HashMap;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.text.CharArrayUtil;
import me.tatarka.holdr.model.*;
import me.tatarka.holdr.util.ParserUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.incremental.storage.FileKeyDescriptor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Retrieves necessary holdr info from layout files.
 */
public class HoldrLayoutIndex extends FileBasedIndexExtension<File, SingleLayout> {
    public static final ID<File, SingleLayout> INDEX_ID = ID.create("me.tatarka.holdr.layouts.index");

    private static final int REF_TYPE_VIEW = 0;
    private static final int REF_TYPE_INCLUDE = 1;
    private static final String REF_FIELD_NAME_DEFAULT = "";

    private static final DataIndexer<File, SingleLayout, FileContent> INDEXER =
            new DataIndexer<File, SingleLayout, FileContent>() {
                @Override
                @NotNull
                public Map<File, SingleLayout> map(@NotNull final FileContent inputData) {
                    Module module = ApplicationManager.getApplication().runReadAction(new Computable<Module>() {
                        @Override
                        public Module compute() {
                            return ModuleUtilCore.findModuleForFile(inputData.getFile(), inputData.getProject());
                        }
                    });
                    if (module == null) {
                        return Collections.emptyMap();
                    }

                    final HoldrModel holdrModel = HoldrModel.getInstance(module);
                    if (holdrModel == null) {
                        return Collections.emptyMap();
                    }

                    final PsiFile file = inputData.getPsiFile();

                    final CharSequence content = inputData.getContentAsText();

                    if (CharArrayUtil.indexOf(content, SdkConstants.NS_RESOURCES, 0) == -1) {
                        return Collections.emptyMap();
                    }

                    if (!(file instanceof XmlFile)) {
                        return Collections.emptyMap();
                    }

                    final File path = new File(inputData.getFile().getPath());
                    final Map<File, SingleLayout> result = new HashMap<File, SingleLayout>();
                    final ParserUtils.IncludeIgnoreState state = new ParserUtils.IncludeIgnoreState();
                    final SingleLayout.Builder layoutBuilder = SingleLayout.of(path);
                    final TagParser tagParser = new TagParser();

                    file.accept(new XmlRecursiveElementVisitor() {
                        @Override
                        public void visitXmlTag(final XmlTag tag) {
                            tagParser.tag = tag;
                            ParserUtils.parseTag(holdrModel.getConfig(), layoutBuilder, state, tagParser);
                            super.visitXmlTag(tag);
                            state.tagEnd(tag.getName());
                        }
                    });

                    SingleLayout layout = layoutBuilder.build();

                    result.put(path, layout);
                    return result;
                }
            };

    private static class TagParser implements ParserUtils.Tag {
        private XmlTag tag;

        @Override
        public String getName() {
            return tag.getName();
        }

        @Override
        public String getAttributeValue(String ns, String name) {
            return tag.getAttributeValue(name, ns);
        }

        @Override
        public boolean isRoot() {
            return tag.getParentTag() == null;
        }
    }

    private static final DataExternalizer<SingleLayout> DATA_EXTERNALIZER = new DataExternalizer<SingleLayout>() {
        @Override
        public void save(@NotNull DataOutput out, SingleLayout value) throws IOException {
            out.writeUTF(value.getPath().getPath());
            out.writeInt(value.size());
            for (Ref ref : value.getRefs()) {
                if (ref instanceof View) {
                    out.writeInt(REF_TYPE_VIEW);
                    out.writeUTF(((View) ref).type);
                } else if (ref instanceof Include) {
                    out.writeInt(REF_TYPE_INCLUDE);
                    out.writeUTF(((Include) ref).layout);
                } else {
                    throw new IOException("Unknown ref type: " + ref);
                }
                out.writeUTF(ref.id);
                if (ref.isFieldNameCustom) {
                    out.writeUTF(ref.fieldName);
                } else {
                    out.writeUTF(REF_FIELD_NAME_DEFAULT);
                }
                out.writeBoolean(ref.isAndroidId);
                out.writeBoolean(ref.isNullable);
            }

            Listeners listeners = value.getListeners();
            out.writeInt(listeners.size());
            for (Listener listener : listeners) {
                out.writeInt(listener.type.ordinal());
                out.writeUTF(listener.name);
                out.writeUTF(listener.viewType);
                out.writeUTF(listener.viewName);

                Collection<String> viewIds = listeners.getViewIds(listener);
                out.writeInt(viewIds.size());

                for (String viewId : viewIds) {
                    out.writeUTF(viewId);
                }
            }
        }

        @Override
        public SingleLayout read(@NotNull DataInput in) throws IOException {
            File path = new File(in.readUTF());

            SingleLayout.Builder layoutBuilder = SingleLayout.of(path);

            int refSize = in.readInt();
            verifySize(refSize);

            for (int i = 0; i < refSize; i++) {
                int refType = in.readInt();

                Ref.Builder refBuilder;
                if (refType == REF_TYPE_VIEW) {
                    String type = in.readUTF();
                    String id = in.readUTF();
                    refBuilder = View.of(type, id);
                } else if (refType == REF_TYPE_INCLUDE) {
                    String layout = in.readUTF();
                    String id = in.readUTF();
                    refBuilder = Include.of(layout, id);
                } else {
                    throw new IOException("Unknown ref type: " + refType);
                }

                String fieldName = in.readUTF();
                if (!REF_FIELD_NAME_DEFAULT.equals(fieldName)) {
                    refBuilder.fieldName(fieldName);
                }

                if (in.readBoolean()) {
                    refBuilder.androidId();
                }

                if (in.readBoolean()) {
                    refBuilder.nullable();
                }

                if (refType == REF_TYPE_VIEW) {
                    layoutBuilder.view((View.Builder) refBuilder);
                } else if (refType == REF_TYPE_INCLUDE) {
                    layoutBuilder.include((Include.Builder) refBuilder);
                }
            }

            int listenerSize = in.readInt();
            verifySize(listenerSize);

            Listeners.Builder listenersBuilder = Listeners.of();

            for (int i = 0; i < listenerSize; i++) {
                int type = in.readInt();
                Listener.Builder listenerBuilder = Listener.of(Listener.Type.values()[type]);
                listenerBuilder.name(in.readUTF());

                Listener listener = listenerBuilder.build(null, in.readUTF(), in.readUTF());

                int viewIdSize = in.readInt();
                verifySize(viewIdSize);

                List<String> viewIds = new ArrayList<String>(viewIdSize);
                for (int j = 0; j < viewIdSize; j++) {
                    viewIds.add(in.readUTF());
                }

                listenersBuilder.add(viewIds, listener);
            }

            layoutBuilder.listeners(listenersBuilder);

            return layoutBuilder.build();
        }
    };

    private static void verifySize(int size) throws IOException {
        if (size < 0 || size > 65535) {
            // Something is very wrong; trigger an index rebuild
            throw new IOException("Corrupt Index: Size " + size);
        }
    }

    private static final KeyDescriptor<File> KEY_DESCRIPTOR = new FileKeyDescriptor();

    @NotNull
    @Override
    public ID<File, SingleLayout> getName() {
        return INDEX_ID;
    }

    @NotNull
    @Override
    public DataIndexer<File, SingleLayout, FileContent> getIndexer() {
        return INDEXER;
    }

    @NotNull
    @Override
    public KeyDescriptor<File> getKeyDescriptor() {
        return KEY_DESCRIPTOR;
    }

    @NotNull
    @Override
    public DataExternalizer<SingleLayout> getValueExternalizer() {
        return DATA_EXTERNALIZER;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new DefaultFileTypeSpecificInputFilter(StdFileTypes.XML) {
            @Override
            public boolean acceptInput(@NotNull final VirtualFile file) {
                return file.isInLocalFileSystem();
            }
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    public static FilteringProcessor<File> filterLayoutByName(final String layoutName, Processor<File> processor) {
        return new FilteringProcessor<File>(new Condition<File>() {
            @Override
            public boolean value(File file) {
                return layoutName.equals(FileUtil.getNameWithoutExtension(file));
            }
        }, processor);
    }
}
