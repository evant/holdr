package me.tatarka.holdr.compile;

import com.sun.codemodel.*;
import me.tatarka.holdr.compile.model.*;
import me.tatarka.holdr.compile.util.FormatUtils;
import me.tatarka.holdr.compile.util.Pair;
import me.tatarka.holdr.model.HoldrConfig;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import static com.sun.codemodel.JExpr.*;
import static com.sun.codemodel.JMod.*;

public class HoldrGenerator implements Serializable {
    public static final String CLASS_PREFIX = "Holdr_";
    public static final String HOLDR_SUPERCLASS = "me.tatarka.holdr.Holdr";

    private final HoldrConfig config;

    public HoldrGenerator(HoldrConfig config) {
        this.config = config;
    }
    
    public String generate(Layout layout) throws IOException {
        StringWriter writer = new StringWriter();
        generate(layout, writer);
        return writer.toString();
    }

    public void generate(Layout layout, Writer writer) throws IOException {
        JCodeModel m = new JCodeModel();
        JPackage pkg = m._package(config.getHoldrPackage());

        try {
            Refs r = new Refs(m, config.getManifestPackage(), layout.name, layout.superclass);

            // public class MyLayoutViewModel {
            JDefinedClass clazz = pkg._class(PUBLIC, getClassName(layout.name))._extends(r.viewHolder);

            // public static final int LAYOUT = R.id.my_layout;
            JFieldVar layoutVar = clazz.field(PUBLIC | STATIC | FINAL, m.INT, "LAYOUT", r.layoutRef);

            Map<Ref, JFieldVar> fieldVarMap = genFields(r, clazz, layout.refs);
            
            Map<Listener.Type, ListenerType> listenerTypeMap = createListenerTypeMap(r);
            
            // public interface Listener {
            JClass listenerInterface = genListenerInterface(r, clazz, layout.listeners, listenerTypeMap);
            // }
            JFieldVar holdrListener = null;
            if (listenerInterface != null) {
                // private Listener _holdrListener; 
                holdrListener = clazz.field(PRIVATE, listenerInterface, "_holdrListener");
            }

            genConstructor(r, clazz, layout.refs, layout.listeners, fieldVarMap, holdrListener, listenerTypeMap);
            
            // public void setListener(Listener listener) {
            genSetListener(r, clazz, listenerInterface, holdrListener);

            m.build(new WriterCodeWriter(writer));
        } catch (JClassAlreadyExistsException e) {
            throw new IOException(e);
        }
    }

    private Map<Listener.Type,ListenerType> createListenerTypeMap(Refs r) {
        Map<Listener.Type, ListenerType> map = new HashMap<Listener.Type, ListenerType>();
        for (Listener.Type type : Listener.Type.values()) {
            map.put(type, new ListenerType(r, type));
        }
        return map;
    }

    public String getClassName(String layoutName) {
        return CLASS_PREFIX + FormatUtils.underscoreToUpperCamel(layoutName);
    }

    private Map<Ref, JFieldVar> genFields(Refs r, JDefinedClass clazz, Collection<Ref> refs) {
        Map<Ref, JFieldVar> fieldVarMap = new LinkedHashMap<Ref, JFieldVar>();
        for (Ref ref : refs) {
            String idPackage = (ref.isAndroidId ? "android" : r.rPackage) + ".R";
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

    private void genConstructor(Refs r, JDefinedClass clazz, Collection<Ref> refs, Listeners listeners, Map<Ref, JFieldVar> fieldVarMap, JFieldVar holderListener, Map<Listener.Type, ListenerType> listenerTypeMap) {
        // private MyLayoutViewModel(View view) {
        JMethod constructor = clazz.constructor(PUBLIC);
        JVar viewVar = constructor.param(r.viewClass, "view");
        JBlock body = constructor.body();

        // super(view);
        body.invoke("super").arg(viewVar);

        // myLinearLayout = (LinearLayout) view.findViewById(R.id.my_linear_layout);
        // myTextView = (TextView) myLinearLayout.findViewById(R.id.my_text_view);
        genInitFields(r, fieldVarMap, viewVar, refs, body);
        
        // myButton.setOnClickListener((view) -> { if (_holderListener != null) _holderListener.onMyButtonClick(myButton); });
        genListeners(r, fieldVarMap, holderListener, refs, listeners, body, listenerTypeMap);

        JDocComment doc = constructor.javadoc();
        doc.append("Constructs a new {@link me.tatarka.holdr.Holdr} for {@link " + r.rPackage + ".R.layout#" + r.layoutName + "}.");
        doc.addParam(viewVar).append("The root view to search for the holdr's views.");
    }

    private void genListeners(Refs r, Map<Ref, JFieldVar> fieldVarMap, JFieldVar holdrListener, Collection<Ref> refs, Listeners listeners, JBlock body, Map<Listener.Type, ListenerType> listenerTypeMap) {
        if (holdrListener == null) return;
        
        for (Ref ref : refs) {
            if (ref instanceof View) {
                JFieldVar fieldVar = fieldVarMap.get(ref);
                View view = (View) ref;
                
                if (view.isNullable) {
                    body = body._if(fieldVar.ne(_null()))._then();
                }

                for (Listener listener : listeners.forView(view)) {
                    ListenerType listenerType = listenerTypeMap.get(listener.type);
                    
                    JDefinedClass listenerClass = r.m.anonymousClass(listenerType.classType);
                    
                    JMethod method = listenerClass.method(PUBLIC, listenerType.methodReturn, listenerType.methodName);
                    
                    List<JVar> params = new ArrayList<JVar>();
                    for (Pair<JType, String> arg : listenerType.methodParams) {
                        JVar param = method.param(arg.first, arg.second);
                        params.add(arg.second.equals("view") ? fieldVar : param);
                    }
                    
                    method.annotate(r.overrideAnnotation);
                    JBlock innerBody = method.body();
                    JBlock innerIf = innerBody._if(holdrListener.ne(_null()))._then();
                    
                    JInvocation invokeHoldrListener;
                    if (listenerType.defaultReturn == null) {
                        invokeHoldrListener = innerIf.invoke(holdrListener, listener.name);
                    } else {
                        invokeHoldrListener = holdrListener.invoke(listener.name);
                        innerIf._return(invokeHoldrListener);
                        innerBody._return(listenerType.defaultReturn);
                    }

                    for (JVar param : params) {
                        invokeHoldrListener.arg(param);
                    }
                    
                    body.invoke(fieldVar, listenerType.setter).arg(_new(listenerClass));
                }
            }
        }
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
                body.assign(fieldVar, _new(includeType).arg(viewVar.invoke("findViewById").arg(idVar)));
            }
        }
    }
    private JClass genListenerInterface(Refs r, JDefinedClass clazz, Listeners listeners, Map<Listener.Type, ListenerType> listenerTypeMap) throws JClassAlreadyExistsException {
        JDefinedClass listenerInterface = null;
        
        for (Listener listener : listeners) {
            if (listenerInterface == null) {
                listenerInterface = clazz._interface(PUBLIC, "Listener");
            }

            ListenerType listenerType = listenerTypeMap.get(listener.type);

            JMethod method = listenerInterface.method(PUBLIC, listenerType.methodReturn, listener.name);
            for (Pair<JType, String> param : listenerType.methodParams) {
                if (param.second.equals("view")) {
                    // Replace view with reference to the field.  
                    method.param(r.ref(listener.viewType), listener.viewName);
                } else {
                    method.param(param.first, param.second);
                }
            }
        }
        
        return listenerInterface;
    }

    private void genSetListener(Refs r, JDefinedClass clazz, JClass listenerInterface, JFieldVar holdrListener) {
        if (listenerInterface == null) return;
        JMethod method = clazz.method(PUBLIC, r.m.VOID, "setListener");
        JVar listener = method.param(listenerInterface, "listener");
        method.body().assign(holdrListener, listener);
    }

    private static class Refs {
        public final JCodeModel m;
        public final String rPackage;
        public final String layoutName;
        public final JClass viewHolder;
        public final JClass viewClass;
        public final JClass androidRClass;
        public final JClass rClass;
        public final JClass nullableAnnotation;
        public final JClass overrideAnnotation;
        public final JFieldRef layoutRef;

        private Refs(JCodeModel m, String rPackage, String layoutName, String superclass) {
            this.m = m;
            this.rPackage = rPackage;
            this.layoutName = layoutName;

            viewHolder = m.ref(superclass);
            viewClass = m.ref("android.view.View");
            androidRClass = m.ref("android.R");
            rClass = m.ref(rPackage + ".R");
            nullableAnnotation = m.ref("android.support.annotation.Nullable");
            overrideAnnotation = m.ref("java.lang.Override");
            layoutRef = rClass.staticRef("layout").ref(layoutName);
        }

        public JClass ref(String className) {
            return m.ref(className);
        }
    }
    
    private static class ListenerType {
        public final String setter;
        public final JClass classType;
        public final String methodName;
        public final List<Pair<JType, String>> methodParams;
        public final JType methodReturn;
        public final JExpression defaultReturn;

        public ListenerType(Refs r, Listener.Type type) {
            switch (type) {
                case ON_TOUCH:
                    setter = "setOnTouchListener";
                    classType = r.ref("android.view.View.OnTouchListener");
                    methodName = "onTouch";
                    methodParams = Arrays.asList(
                            new Pair<JType, String>(r.viewClass, "view"),
                            new Pair<JType, String>(r.ref("android.view.MotionEvent"), "event")
                    );
                    methodReturn = r.m.BOOLEAN;
                    defaultReturn = FALSE;
                    break;
                case ON_CLICK:
                    setter = "setOnClickListener";
                    classType = r.ref("android.view.View.OnClickListener");
                    methodName = "onClick";
                    methodParams = Arrays.asList(new Pair<JType, String>(r.viewClass, "view"));
                    methodReturn = r.m.VOID;
                    defaultReturn = null;
                    break;
                case ON_LONG_CLICK:
                    setter = "setOnLongClickListener";
                    classType = r.ref("android.view.View.OnLongClickListener");
                    methodName = "onLongClick";
                    methodParams = Arrays.asList(new Pair<JType, String>(r.viewClass, "view"));
                    methodReturn = r.m.BOOLEAN;
                    defaultReturn = FALSE;
                    break;
                case ON_FOCUS_CHANGE:
                    setter = "setOnFocusChangeListener";
                    classType = r.ref("android.view.View.OnFocusChangeListener");
                    methodName = "onFocusChange";
                    methodParams = Arrays.asList(
                            new Pair<JType, String>(r.viewClass, "view"),
                            new Pair<JType, String>(r.m.BOOLEAN, "hasFocus")
                    );
                    methodReturn = r.m.VOID;
                    defaultReturn = null;
                    break;
                case ON_CHECKED_CHANGE:
                    setter = "setOnCheckedChangeListener";
                    classType = r.ref("android.widget.CompoundButton.OnCheckedChangeListener");
                    methodName = "onCheckedChanged";
                    methodParams = Arrays.asList(
                            new Pair<JType, String>(r.ref("android.widget.CompoundButton"), "view"),
                            new Pair<JType, String>(r.m.BOOLEAN, "isChecked")
                    );
                    methodReturn = r.m.VOID;
                    defaultReturn = null;
                    break;
                case ON_EDITOR_ACTION:
                    setter = "setOnEditorActionListener";
                    classType = r.ref("android.widget.TextView.OnEditorActionListener");
                    methodName = "onEditorAction";
                    methodParams = Arrays.asList(
                            new Pair<JType, String>(r.ref("android.widget.TextView"), "view"),
                            new Pair<JType, String>(r.m.INT, "actionId"),
                            new Pair<JType, String>(r.ref("android.view.KeyEvent"), "event")
                    );
                    methodReturn = r.m.BOOLEAN;
                    defaultReturn = FALSE;
                    break;
                case ON_ITEM_CLICK:
                    setter = "setOnItemClickListener";
                    classType = r.ref("android.widget.AdapterView.OnItemClickListener");
                    methodName = "onItemClick";
                    methodParams = Arrays.asList(
                            new Pair<JType, String>(r.ref("android.widget.AdapterView"), "view"),
                            new Pair<JType, String>(r.viewClass, "item"),
                            new Pair<JType, String>(r.m.INT, "position"),
                            new Pair<JType, String>(r.m.LONG, "id")
                    );
                    methodReturn = r.m.VOID;
                    defaultReturn = null;
                    break;
                case ON_ITEM_LONG_CLICK:
                    setter = "setOnItemLongClickListener";
                    classType = r.ref("android.widget.AdapterView.OnItemLongClickListener");
                    methodName = "onItemLongClick";
                    methodParams = Arrays.asList(
                            new Pair<JType, String>(r.ref("android.widget.AdapterView"), "view"),
                            new Pair<JType, String>(r.viewClass, "item"),
                            new Pair<JType, String>(r.m.INT, "position"),
                            new Pair<JType, String>(r.m.LONG, "id")
                    );
                    methodReturn = r.m.BOOLEAN;
                    defaultReturn = FALSE;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown listener type: " + type.toString());
            }
        }
    }
}
