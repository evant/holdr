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
            JClass viewHolder = m.ref("me.tatarka.socket.Socket");
            JClass viewClass = m.ref("android.view.View");
            JClass viewGroupClass = m.ref("android.view.ViewGroup");
            JClass layoutInflaterClass = m.ref("android.view.LayoutInflater");
            JClass androidRClass = m.ref("android.R");
            JClass rClass = m.ref(packageName + ".R");
            JClass activityClass = m.ref("android.app.Activity");
            JClass fragmentClass = m.ref("android.app.Fragment");

            // public class MyLayoutViewModel {
            JDefinedClass clazz = pkg._class(PUBLIC, className)._extends(viewHolder);

            // public static final int LAYOUT = R.id.my_layout;
            JFieldVar layoutVar = clazz.field(PUBLIC | STATIC | FINAL, m.INT, "LAYOUT", rClass.staticRef("layout").ref(layoutName));

            // public LinearLayout myLinearLayout;
            // public TextView myTextView;
            Map<View, JFieldVar> fieldVarMap = new LinkedHashMap<View, JFieldVar>();
            for (View view : views) {
                genFields(m, clazz, view, fieldVarMap);
            }

            // private MyLayoutViewModel(View view) {
            JMethod constructor = clazz.constructor(PRIVATE);
            JVar viewVar = constructor.param(viewClass, "view");
            JBlock body = constructor.body();

            // super(view);
            body.invoke("super").arg(viewVar);

            // myLinearLayout = (LinearLayout) view.findViewById(R.id.my_linear_layout);
            // myTextView = (TextView) myLinearLayout.findViewById(R.id.my_text_view);
            for (View view : views) {
                genInitFields(m, view, viewVar, body, rClass, fieldVarMap);
            }
            // }

            genFromView(m, clazz, viewClass);
            genFromActivity(m, clazz, activityClass, androidRClass);
            genFromFragment(m, clazz, fragmentClass);
            genInflate1(m, clazz, layoutInflaterClass, viewGroupClass);
            genInflate2(m, clazz, layoutInflaterClass, viewGroupClass);
            getListInflate(m, clazz, layoutInflaterClass, layoutVar, viewClass, viewGroupClass);

            m.build(new WriterCodeWriter(writer));
        } catch (JClassAlreadyExistsException e) {
            throw new IOException(e);
        }
    }

    private static void genFields(JCodeModel m, JDefinedClass clazz, View view, Map<View, JFieldVar> fieldVarMap) {
        fieldVarMap.put(view, clazz.field(PUBLIC, m.ref(view.type), view.fieldName));
        for (View child : view.children) {
            genFields(m, clazz, child, fieldVarMap);
        }
    }

    private static void genInitFields(JCodeModel m, View view, JVar viewVar, JBlock body, JClass rClass, Map<View, JFieldVar> fieldVarMap) {
        JClass viewType = m.ref(view.type);
        JFieldVar fieldVar = fieldVarMap.get(view);
        JFieldRef idVar = rClass.staticRef("id").ref(view.id);

        body.assign(fieldVar, cast(viewType, viewVar.invoke("findViewById").arg(idVar)));
        for (View child : view.children) {
            genInitFields(m, child, fieldVar, body, rClass, fieldVarMap);
        }
    }

    private static void genFromView(JCodeModel m, JDefinedClass clazz, JClass viewClass) {
        // public static MyLayoutViewModel from(View view) {
        JMethod method = clazz.method(PUBLIC | STATIC, clazz, "from");
        JVar viewVar = method.param(viewClass, "view");
        JBlock body = method.body();
        body._return(_new(clazz).arg(viewVar));
    }

    private static void genFromActivity(JCodeModel m, JDefinedClass clazz, JClass activityClass, JClass androidRClass) {
        // public static MyLayoutViewModel from(Activity activity) {
        JMethod method = clazz.method(PUBLIC | STATIC, clazz, "from");
        JVar activityVar = method.param(activityClass, "activity");
        JBlock body = method.body();
        body._return(_new(clazz).arg(activityVar.invoke("findViewById").arg(androidRClass.staticRef("id").ref("content"))));
    }

    private static void genFromFragment(JCodeModel m, JDefinedClass clazz, JClass fragmentClass) {
        // public static MyLayoutViewModel from(Fragment fragment) {
        JMethod method = clazz.method(PUBLIC | STATIC, clazz, "from");
        JVar fragmentVar = method.param(fragmentClass, "fragment");
        JBlock body = method.body();
        body._return(_new(clazz).arg(fragmentVar.invoke("getView")));
    }

    private static void genInflate1(JCodeModel m, JDefinedClass clazz, JClass layoutInflaterClass, JClass viewGroupClass) {
        // public static MyLayoutViewModel inflate(LayoutInflater layoutInflater, int resource, ViewGroup root, boolean attachToRoot) {
        JMethod method = clazz.method(PUBLIC | STATIC, clazz, "inflate");
        JVar layoutInflaterVar = method.param(layoutInflaterClass, "layoutInflater");
        JVar resourceVar = method.param(m.INT, "resource");
        JVar rootVar = method.param(viewGroupClass, "root");
        JVar attachToRootVar = method.param(m.BOOLEAN, "attachToRoot");
        JBlock body = method.body();
        body._return(_new(clazz).arg(layoutInflaterVar.invoke("inflate")
                .arg(resourceVar).arg(rootVar).arg(attachToRootVar)));
        // }
    }

    private static void genInflate2(JCodeModel m, JDefinedClass clazz, JClass layoutInflaterClass, JClass viewGroupClass) {
        // public static MyLayoutViewModel inflate(LayoutInflater layoutInflater, int resource, ViewGroup root) {
        JMethod method = clazz.method(PUBLIC | STATIC, clazz, "inflate");
        JVar layoutInflaterVar = method.param(layoutInflaterClass, "layoutInflater");
        JVar resourceVar = method.param(m.INT, "resource");
        JVar rootVar = method.param(viewGroupClass, "root");
        JBlock body = method.body();
        body._return(_new(clazz).arg(layoutInflaterVar.invoke("inflate")
                .arg(resourceVar).arg(rootVar)));
    }

    private static void getListInflate(JCodeModel m, JDefinedClass clazz, JClass layoutInflaterClass, JFieldVar layoutVar, JClass viewClass, JClass viewGroupClass) {
        // public static MyLayoutViewModel listInflate(LayoutInflater layoutInflater, int resource, View convertView, ViewGroup parent) {
        JMethod method = clazz.method(PUBLIC | STATIC, clazz, "listInflate");
        JVar layoutInflaterVar = method.param(layoutInflaterClass, "layoutInflater");
        JVar convertViewVar = method.param(viewClass, "convertView");
        JVar parentVar = method.param(viewGroupClass, "parent");
        JBlock body = method.body();

        // if (convertView != null) {
        JConditional i = body._if(convertViewVar.eq(_null()));
        JBlock ifTrue = i._then();
        JVar viewVar = ifTrue.decl(viewClass, "view", layoutInflaterVar.invoke("inflate")
                .arg(layoutVar).arg(parentVar).arg(FALSE));
        JVar socketVar = ifTrue.decl(clazz, "socket", _new(clazz).arg(viewVar));
        ifTrue.invoke(viewVar, "setTag").arg(socketVar);
        ifTrue._return(socketVar);
        // else {
        i._else()._return(cast(clazz, convertViewVar.invoke("getTag")));
    }
}
