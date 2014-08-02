package me.tatarka.socket.compile;

import com.sun.codemodel.*;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.tatarka.socket.compile.util.FormatUtils;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JMod.STATIC;

public class SocketGenerator {
    private final String packageName;

    public SocketGenerator(String packageName) {
        this.packageName = packageName;
    }

    public void generate(String layoutName, Set<Ref> refs, Writer writer) throws IOException {
        JCodeModel m = new JCodeModel();
        JPackage pkg = m._package(packageName + ".sockets");

        try {
            Refs r = new Refs(m, packageName);

            // public class MyLayoutViewModel {
            JDefinedClass clazz = pkg._class(PUBLIC, getClassName(layoutName))._extends(r.viewHolder);

            // public static final int LAYOUT = R.id.my_layout;
            JFieldVar layoutVar = clazz.field(PUBLIC | STATIC | FINAL, m.INT, "LAYOUT", r.rClass.staticRef("layout").ref(layoutName));

            Map<Ref, JFieldVar> fieldVarMap = genFields(r, clazz, refs);

            genConstructor(r, clazz, refs, fieldVarMap);

            m.build(new WriterCodeWriter(writer));
        } catch (JClassAlreadyExistsException e) {
            throw new IOException(e);
        }
    }

    public String getClassName(String layoutName) {
        return "Socket" + FormatUtils.underscoreToUpperCamel(layoutName);
    }

    private Map<Ref, JFieldVar> genFields(Refs r, JDefinedClass clazz, Set<Ref> refs) {
        Map<Ref, JFieldVar> fieldVarMap = new LinkedHashMap<Ref, JFieldVar>();
        for (Ref ref : refs) {
            if (ref instanceof View) {
                fieldVarMap.put(ref, clazz.field(PUBLIC, r.ref(((View) ref).type), ref.fieldName));
            } else if (ref instanceof Include) {
                fieldVarMap.put(ref, clazz.field(PUBLIC, r.ref(getClassName(((Include) ref).layout)), ref.fieldName));
            }
        }
        return fieldVarMap;
    }

    private void genConstructor(Refs r, JDefinedClass clazz, Set<Ref> refs, Map<Ref, JFieldVar> fieldVarMap) {
        // private MyLayoutViewModel(View view) {
        JMethod constructor = clazz.constructor(PUBLIC);
        JVar viewVar = constructor.param(r.viewClass, "view");
        JBlock body = constructor.body();

        // super(view);
        body.invoke("super").arg(viewVar);

        // myLinearLayout = (LinearLayout) view.findViewById(R.id.my_linear_layout);
        // myTextView = (TextView) myLinearLayout.findViewById(R.id.my_text_view);
        genInitFields(r, fieldVarMap, viewVar, refs, body);
    }

    private void genInitFields(Refs r, Map<Ref, JFieldVar> fieldVarMap, JVar viewVar, Set<Ref> refs, JBlock body) {
        for (Ref ref : refs) {
            JFieldVar fieldVar = fieldVarMap.get(ref);
            JFieldRef idVar = (ref.isAndroidId ? r.androidRClass : r.rClass).staticRef("id").ref(ref.id);
            if (ref instanceof View) {
                JClass viewType = r.ref(((View) ref).type);
                body.assign(fieldVar, cast(viewType, viewVar.invoke("findViewById").arg(idVar)));
            } else if (ref instanceof Include) {
                JClass includeType = r.ref(getClassName(((Include) ref).layout));
                body.assign(fieldVar, _new(includeType).arg(viewVar.invoke("findViewById").arg(idVar)));
            }
        }
    }

    private static class Refs {
        public final JCodeModel m;
        public final JClass viewHolder;
        public final JClass viewClass;
        public final JClass androidRClass;
        public final JClass rClass;

        private Refs(JCodeModel m, String packageName) {
            this.m = m;

            viewHolder = m.ref("me.tatarka.socket.Socket");
            viewClass = m.ref("android.view.View");
            androidRClass = m.ref("android.R");
            rClass = m.ref(packageName + ".R");
        }

        public JClass ref(String className) {
            return m.ref(className);
        }
    }
}
