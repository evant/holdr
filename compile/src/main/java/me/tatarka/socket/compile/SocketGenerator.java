package me.tatarka.socket.compile;

import com.sun.codemodel.*;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JMod.STATIC;

public class SocketGenerator {
    private final String packageName;

    public SocketGenerator(String packageName) {
        this.packageName = packageName;
    }

    public void generate(String layoutName, String className, List<View> views, Writer writer) throws IOException {
        JCodeModel m = new JCodeModel();
        JPackage pkg = m._package(packageName + ".sockets");

        try {
            Refs refs = new Refs(m, packageName);

            // public class MyLayoutViewModel {
            JDefinedClass clazz = pkg._class(PUBLIC, className)._extends(refs.viewHolder);

            // public static final int LAYOUT = R.id.my_layout;
            JFieldVar layoutVar = clazz.field(PUBLIC | STATIC | FINAL, m.INT, "LAYOUT", refs.rClass.staticRef("layout").ref(layoutName));

            Map<View, JFieldVar> fieldVarMap = genFields(refs, clazz, views);

            genConstructor(refs, clazz, views, fieldVarMap);

            m.build(new WriterCodeWriter(writer));
        } catch (JClassAlreadyExistsException e) {
            throw new IOException(e);
        }
    }

    private static Map<View, JFieldVar> genFields(Refs refs, JDefinedClass clazz, List<View> views) {
        // public LinearLayout myLinearLayout;
        // public TextView myTextView;
        Map<View, JFieldVar> fieldVarMap = new LinkedHashMap<View, JFieldVar>();
        for (View view : views) {
            genFields(refs, clazz, view, fieldVarMap);
        }
        return fieldVarMap;
    }

    private static void genFields(Refs refs, JDefinedClass clazz, View view, /* OUT */ Map<View, JFieldVar> fieldVarMap) {
        fieldVarMap.put(view, clazz.field(PUBLIC, refs.ref(view.type), view.fieldName));
        for (View child : view.children) {
            genFields(refs, clazz, child, fieldVarMap);
        }
    }

    private static void genConstructor(Refs refs, JDefinedClass clazz, List<View> views, Map<View, JFieldVar> fieldVarMap) {
        // private MyLayoutViewModel(View view) {
        JMethod constructor = clazz.constructor(PUBLIC);
        JVar viewVar = constructor.param(refs.viewClass, "view");
        JBlock body = constructor.body();

        // super(view);
        body.invoke("super").arg(viewVar);

        // myLinearLayout = (LinearLayout) view.findViewById(R.id.my_linear_layout);
        // myTextView = (TextView) myLinearLayout.findViewById(R.id.my_text_view);
        for (View view : views) {
            genInitFields(refs, fieldVarMap, viewVar, view, body);
        }
    }

    private static void genInitFields(Refs refs, Map<View, JFieldVar> fieldVarMap, JVar viewVar, View view, JBlock body) {
        JClass viewType = refs.ref(view.type);
        JFieldVar fieldVar = fieldVarMap.get(view);
        JFieldRef idVar = (view.isAndroidId ? refs.androidRClass : refs.rClass).staticRef("id").ref(view.id);

        body.assign(fieldVar, cast(viewType, viewVar.invoke("findViewById").arg(idVar)));
        for (View child : view.children) {
            genInitFields(refs, fieldVarMap, fieldVar, child, body);
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
