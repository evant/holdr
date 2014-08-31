package me.tatarka.holdr.compile;

import com.sun.codemodel.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import me.tatarka.holdr.compile.util.FormatUtils;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JMod.STATIC;

public class HoldrGenerator {
    public static final String CLASS_PREFIX = "Holdr_";
    
    private final String packageName;

    public HoldrGenerator(String packageName) {
        this.packageName = packageName;
    }

    public void generate(String layoutName, Collection<Ref> refs, Writer writer) throws IOException {
        JCodeModel m = new JCodeModel();
        JPackage pkg = m._package(packageName + "." + HoldrCompiler.PACKAGE);

        try {
            Refs r = new Refs(m, packageName, layoutName);

            // public class MyLayoutViewModel {
            JDefinedClass clazz = pkg._class(PUBLIC, getClassName(layoutName))._extends(r.viewHolder);

            // public static final int LAYOUT = R.id.my_layout;
            JFieldVar layoutVar = clazz.field(PUBLIC | STATIC | FINAL, m.INT, "LAYOUT", r.layoutRef);

            Map<Ref, JFieldVar> fieldVarMap = genFields(r, clazz, refs);

            genConstructor(r, clazz, refs, fieldVarMap);

            m.build(new WriterCodeWriter(writer));
        } catch (JClassAlreadyExistsException e) {
            throw new IOException(e);
        }
    }

    public String getClassName(String layoutName) {
        return CLASS_PREFIX + FormatUtils.underscoreToUpperCamel(layoutName);
    }

    private Map<Ref, JFieldVar> genFields(Refs r, JDefinedClass clazz, Collection<Ref> refs) {
        Map<Ref, JFieldVar> fieldVarMap = new LinkedHashMap<Ref, JFieldVar>();
        for (Ref ref : refs) {
            String idPackage = (ref.isAndroidId ? "android" : r.packageName) + ".R";
            JFieldVar var;
            if (ref instanceof View) {
                var = clazz.field(PUBLIC, r.ref(((View) ref).type), ref.fieldName);
                var.javadoc().append("View for {@link " + idPackage + ".id#" + ref.id + "}.");
                if (ref.isNullable) {
                    var.annotate(r.nullableAnnotation);
                }
            } else if (ref instanceof Include) {
                var = clazz.field(PUBLIC, r.ref(getClassName(((Include) ref).layout)), ref.fieldName);
                var.javadoc().append("Holdr for {@link " + idPackage + ".layout#" + ((Include) ref).layout + "}.");
            } else {
                throw new IllegalArgumentException("Unknown ref: " + ref);
            }
            fieldVarMap.put(ref, var);
        }
        return fieldVarMap;
    }

    private void genConstructor(Refs r, JDefinedClass clazz, Collection<Ref> refs, Map<Ref, JFieldVar> fieldVarMap) {
        // private MyLayoutViewModel(View view) {
        JMethod constructor = clazz.constructor(PUBLIC);
        JVar viewVar = constructor.param(r.viewClass, "view");
        JBlock body = constructor.body();

        // super(view);
        body.invoke("super").arg(viewVar);

        // myLinearLayout = (LinearLayout) view.findViewById(R.id.my_linear_layout);
        // myTextView = (TextView) myLinearLayout.findViewById(R.id.my_text_view);
        genInitFields(r, fieldVarMap, viewVar, refs, body);

        JDocComment doc = constructor.javadoc();
        doc.append("Constructs a new {@link me.tatarka.holdr.Holdr} for {@link " + r.packageName + ".R.layout#" + r.layoutName + "}.");
        doc.addParam(viewVar).append("The root view to search for the holdr's views.");
    }

    private void genInitFields(Refs r, Map<Ref, JFieldVar> fieldVarMap, JVar viewVar, Collection<Ref> refs, JBlock body) {
        for (Ref ref : refs) {
            JFieldVar fieldVar = fieldVarMap.get(ref);
            JFieldRef idVar = (ref.isAndroidId ? r.androidRClass : r.rClass).staticRef("id").ref(ref.id);
            if (ref instanceof View) {
                JClass viewType = r.ref(((View) ref).type);
                body.assign(fieldVar, cast(viewType, viewVar.invoke("findViewById").arg(idVar)));
            } else if (ref instanceof Include) {
                JClass includeType = r.ref(getClassName(((Include) ref).layout));
                body.assign(fieldVar, _new(includeType).arg(viewVar));
            }
        }
    }

    private static class Refs {
        public final JCodeModel m;
        public final String packageName;
        public final String layoutName;
        public final JClass viewHolder;
        public final JClass viewClass;
        public final JClass androidRClass;
        public final JClass rClass;
        public final JClass nullableAnnotation;
        public final JFieldRef layoutRef;

        private Refs(JCodeModel m, String packageName, String layoutName) {
            this.m = m;
            this.packageName = packageName;
            this.layoutName = layoutName;

            viewHolder = m.ref("me.tatarka.holdr.Holdr");
            viewClass = m.ref("android.view.View");
            androidRClass = m.ref("android.R");
            rClass = m.ref(packageName + ".R");
            nullableAnnotation = m.ref("android.support.annotation.Nullable");
            layoutRef = rClass.staticRef("layout").ref(layoutName);
        }

        public JClass ref(String className) {
            return m.ref(className);
        }
    }
}
