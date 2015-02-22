package me.tatarka.holdr.util;

import me.tatarka.holdr.model.Listener;

/**
 * Created by evan on 2/15/15.
 */
public class GeneratorUtils {
    public static enum Type {
        VIEW_CLASS(),
        MOTION_EVENT("android.view.MotionEvent"),
        COMPOUND_BUTTON("android.widget.CompoundButton"),
        TEXT_VIEW("android.widget.TextView"),
        KEY_EVENT("android.view.KeyEvent"),
        ADAPTER_VIEW("android.widget.AdapterView"),
        BOOLEAN("boolean"),
        INT("int"),
        LONG("long"),
        VOID("void");

        private String className;

        public String getClassName() {
            return className;
        }

        Type() {

        }

        Type(String className) {
            this.className = className;
        }
    }

    public static enum Value {
        FALSE
    }

    public static enum ListenerType {
        ON_TOUCH(
                "setOnTouchListener",
                "android.view.View.OnTOuchListener",
                "onTouch",
                new Pair[]{
                        Pair.create(Type.VIEW_CLASS, "view"),
                        Pair.create(Type.MOTION_EVENT, "event")
                },
                Type.BOOLEAN,
                Value.FALSE
        ),

        ON_CLICK(
                "setOnClickListener",
                "android.view.View.OnClickListener",
                "onClick",
                Pair.create(Type.VIEW_CLASS, "view")
        ),

        ON_LONG_CLICK(
                "setOnLongClickListener",
                "android.view.View.OnLongClickListener",
                "onLongClick",
                new Pair[]{
                        Pair.create(Type.VIEW_CLASS, "view")
                },
                Type.BOOLEAN,
                Value.FALSE
        ),

        ON_FOCUS_CHANGE(
                "setOnFocusChangeListener",
                "android.view.View.OnFocusChangeListener",
                "onFocusChange",
                Pair.create(Type.VIEW_CLASS, "view"),
                Pair.create(Type.BOOLEAN, "hasFocus")
        ),

        ON_CHECKED_CHANGE(
                "setOnCheckedChangeListener",
                "android.widget.CompoundButton.OnCheckedChangeListener",
                "onCheckedChanged",
                Pair.create(Type.COMPOUND_BUTTON, "view"),
                Pair.create(Type.BOOLEAN, "isChecked")
        ),

        ON_EDITOR_ACTION(
                "setOnEditorActionListener",
                "android.widget.TextView.OnEditorActionListener",
                "onEditorAction",
                new Pair[]{
                        Pair.create(Type.TEXT_VIEW, "view"),
                        Pair.create(Type.INT, "actionId"),
                        Pair.create(Type.KEY_EVENT, "event")
                },
                Type.BOOLEAN,
                Value.FALSE
        ),

        ON_ITEM_CLICK(
                "setOnItemClickListener",
                "android.widget.AdapterView.OnItemClickListener",
                "onItemClick",
                Pair.create(Type.ADAPTER_VIEW, "view"),
                Pair.create(Type.VIEW_CLASS, "item"),
                Pair.create(Type.INT, "position"),
                Pair.create(Type.LONG, "id")
        ),

        ON_ITEM_LONG_CLICK(
                "setOnItemLongClickListener",
                "android.widget.AdapterView.OnItemLongClickListener",
                "onItemLongClick",
                new Pair[]{
                        Pair.create(Type.ADAPTER_VIEW, "view"),
                        Pair.create(Type.VIEW_CLASS, "item"),
                        Pair.create(Type.INT, "position"),
                        Pair.create(Type.LONG, "id")
                },
                Type.BOOLEAN,
                Value.FALSE
        );

        public static ListenerType fromType(Listener.Type type) {
            switch (type) {
                case ON_TOUCH: return ON_TOUCH;
                case ON_CLICK: return ON_CLICK;
                case ON_LONG_CLICK: return ON_LONG_CLICK;
                case ON_FOCUS_CHANGE: return ON_FOCUS_CHANGE;
                case ON_CHECKED_CHANGE: return ON_CHECKED_CHANGE;
                case ON_EDITOR_ACTION: return ON_EDITOR_ACTION;
                case ON_ITEM_CLICK: return ON_ITEM_CLICK;
                case ON_ITEM_LONG_CLICK: return ON_ITEM_LONG_CLICK;
                default:
                    throw new IllegalArgumentException("Unknown type: " + type);
            }
        }

        private String setter;
        private String className;
        private String methodName;
        private Pair<Type, String>[] params;
        private Type returnType;
        private Value defaultReturn;

        @SafeVarargs
        ListenerType(String setter, String className, String methodName, Pair<Type, String>... params) {
            this(setter, className, methodName, params, Type.VOID, null);
        }

        ListenerType(String setter, String className, String methodName, Pair<Type, String>[] params, Type returnType, Value defaultReturn) {
            this.setter = setter;
            this.className = className;
            this.methodName = methodName;
            this.params = params;
            this.returnType = returnType;
            this.defaultReturn = defaultReturn;
        }

        public String getSetter() {
            return setter;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public Pair<Type, String>[] getParams() {
            return params;
        }

        public GeneratorUtils.Type getReturnType() {
            return returnType;
        }

        public Value getDefaultReturn() {
            return defaultReturn;
        }
    }
}
