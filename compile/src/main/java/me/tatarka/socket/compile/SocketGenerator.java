package me.tatarka.socket.compile;

import com.sun.codemodel.*;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.sun.codemodel.JExpr.FALSE;
import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PRIVATE;
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

            genFromView(refs, clazz);
            genFromActivity(refs, clazz);
            genFromFragment(refs, clazz);
            genInflate1(refs, clazz);
            genInflate2(refs, clazz);
            getListInflate(refs, clazz, layoutVar);

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
        JMethod constructor = clazz.constructor(PRIVATE);
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

    private static void genFromView(Refs refs, JDefinedClass clazz) {
        // public static MyLayoutViewModel from(View view) {
        JMethod method = clazz.method(PUBLIC | STATIC, clazz, "from");
        JVar viewVar = method.param(refs.viewClass, "view");
        JBlock body = method.body();
        body._return(_new(clazz).arg(viewVar));
    }

    private static void genFromActivity(Refs refs, JDefinedClass clazz) {
        // public static MyLayoutViewModel from(Activity activity) {
        JMethod method = clazz.method(PUBLIC | STATIC, clazz, "from");
        JVar activityVar = method.param(refs.activityClass, "activity");
        JBlock body = method.body();
        body._return(_new(clazz).arg(activityVar.invoke("findViewById").arg(refs.androidRClass.staticRef("id").ref("content"))));
    }

    private static void genFromFragment(Refs refs, JDefinedClass clazz) {
        // public static MyLayoutViewModel from(Fragment fragment) {
        JMethod method = clazz.method(PUBLIC | STATIC, clazz, "from");
        JVar fragmentVar = method.param(refs.fragmentClass, "fragment");
        JBlock body = method.body();
        body._return(_new(clazz).arg(fragmentVar.invoke("getView")));
    }

    private static void genInflate1(Refs refs, JDefinedClass clazz) {
        // public static MyLayoutViewModel inflate(LayoutInflater layoutInflater, int resource, ViewGroup root, boolean attachToRoot) {
        JMethod method = clazz.method(PUBLIC | STATIC, clazz, "inflate");
        JVar layoutInflaterVar = method.param(refs.layoutInflaterClass, "layoutInflater");
        JVar resourceVar = method.param(refs.m.INT, "resource");
        JVar rootVar = method.param(refs.viewGroupClass, "root");
        JVar attachToRootVar = method.param(refs.m.BOOLEAN, "attachToRoot");
        JBlock body = method.body();
        body._return(_new(clazz).arg(layoutInflaterVar.invoke("inflate")
                .arg(resourceVar).arg(rootVar).arg(attachToRootVar)));
        // }
    }

    private static void genInflate2(Refs refs, JDefinedClass clazz) {
        // public static MyLayoutViewModel inflate(LayoutInflater layoutInflater, int resource, ViewGroup root) {
        JMethod method = clazz.method(PUBLIC | STATIC, clazz, "inflate");
        JVar layoutInflaterVar = method.param(refs.layoutInflaterClass, "layoutInflater");
        JVar resourceVar = method.param(refs.m.INT, "resource");
        JVar rootVar = method.param(refs.viewGroupClass, "root");
        JBlock body = method.body();
        body._return(_new(clazz).arg(layoutInflaterVar.invoke("inflate")
                .arg(resourceVar).arg(rootVar)));
    }

    private static void getListInflate(Refs refs, JDefinedClass clazz, JFieldVar layoutVar) {
        // public static MyLayoutViewModel listInflate(LayoutInflater layoutInflater, int resource, View convertView, ViewGroup parent) {
        JMethod method = clazz.method(PUBLIC | STATIC, clazz, "listInflate");
        JVar layoutInflaterVar = method.param(refs.layoutInflaterClass, "layoutInflater");
        JVar convertViewVar = method.param(refs.viewClass, "convertView");
        JVar parentVar = method.param(refs.viewGroupClass, "parent");
        JBlock body = method.body();

        // if (convertView != null) {
        JConditional i = body._if(convertViewVar.eq(_null()));
        JBlock ifTrue = i._then();
        JVar viewVar = ifTrue.decl(refs.viewClass, "view", layoutInflaterVar.invoke("inflate")
                .arg(layoutVar).arg(parentVar).arg(FALSE));
        JVar socketVar = ifTrue.decl(clazz, "socket", _new(clazz).arg(viewVar));
        ifTrue.invoke(viewVar, "setTag").arg(socketVar);
        ifTrue._return(socketVar);
        // else {
        i._else()._return(cast(clazz, convertViewVar.invoke("getTag")));
    }

    private static class Refs {
        public final JCodeModel m;
        public final JClass viewHolder;
        public final JClass viewClass;
        public final JClass viewGroupClass;
        public final JClass layoutInflaterClass;
        public final JClass androidRClass;
        public final JClass rClass;
        public final JClass activityClass;
        public final JClass fragmentClass;

        private Refs(JCodeModel m, String packageName) {
            this.m = m;

            viewHolder = m.ref("me.tatarka.socket.Socket");
            viewClass = m.ref("android.view.View");
            viewGroupClass = m.ref("android.view.ViewGroup");
            layoutInflaterClass = m.ref("android.view.LayoutInflater");
            androidRClass = m.ref("android.R");
            rClass = m.ref(packageName + ".R");
            activityClass = m.ref("android.app.Activity");
            fragmentClass = m.ref("android.app.Fragment");
        }

        public JClass ref(String className) {
            return m.ref(className);
        }
    }
}
