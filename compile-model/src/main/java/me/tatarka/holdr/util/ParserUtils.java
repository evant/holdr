package me.tatarka.holdr.util;

import java.util.HashMap;
import java.util.Map;

import me.tatarka.holdr.model.HoldrConfig;
import me.tatarka.holdr.model.Include;
import me.tatarka.holdr.model.Layout;
import me.tatarka.holdr.model.Listener;
import me.tatarka.holdr.model.View;

/**
 * Created by evan on 2/13/15.
 */
public class ParserUtils {
    public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    public static final String APP_NS = "http://schemas.android.com/apk/res-auto";
    public static final String ID = "id";
    public static final String LAYOUT = "layout";
    public static final String INCLUDE = "include";
    public static final String MERGE = "merge";
    public static final String FRAGMENT = "fragment";
    public static final String CLASS = "class";
    public static final String HOLDR_PREFIX = "holdr_";
    public static final String HOLDR_CLASS = HOLDR_PREFIX + "class";
    public static final String HOLDR_SUPERCLASS = HOLDR_PREFIX + "superclass";
    public static final String HOLDR_FIELD_NAME = HOLDR_PREFIX + "field_name";
    public static final String HOLDR_INCLUDE = HOLDR_PREFIX + "include";
    public static final String HOLDR_IGNORE = HOLDR_PREFIX + "ignore";
    public static final String VIEW = "view";
    public static final String ALL = "all";
    private static final String PREFIX_VIEW = "android.view.";
    private static final String PREFIX_WIDGET = "android.widget.";
    private static final String PREFIX_WEBKIT = "android.webkit.";
    public static final Map<String, String> PREFIX_MAP = new HashMap<String, String>() {{
        put("View", PREFIX_VIEW);
        put("ViewStub", PREFIX_VIEW);
        put("SurfaceView", PREFIX_VIEW);
        put("TextureView", PREFIX_VIEW);
        put("WebView", PREFIX_WEBKIT);
    }
        @Override
        public String get(Object key) {
            String result = super.get(key);
            return result == null ? PREFIX_WIDGET : result;
        }
    };
    
    public static void parseTag(HoldrConfig config, Layout.Builder layoutBuilder, IncludeIgnoreState state, Tag tag) {
        String tagName = tag.getName();

        if (tag.isRoot()) {
            String superclass = tag.getAttributeValue(APP_NS, HOLDR_SUPERCLASS);
            if (superclass != null) {
                layoutBuilder.superclass(superclass);
            }

            if (tagName.equals(MERGE)) {
                layoutBuilder.rootMerge(true);
            }
        }

        HoldrIgnore ignore = parseIgnore(tag.getAttributeValue(APP_NS, HOLDR_IGNORE));
        HoldrInclude include = parseInclude(tag.getAttributeValue(APP_NS, HOLDR_INCLUDE));

        state.tagBegin(ignore, include, tagName);

        String idString = tag.getAttributeValue(ANDROID_NS, ID);
        String id = parseId(idString);

        if (isIncluded(config.getDefaultInclude(), id != null, state)) {
            boolean isAndroidId = parseIsAndroidId(idString);
            String fieldName = tag.getAttributeValue(APP_NS, HOLDR_FIELD_NAME);

            if (tagName.equals(FRAGMENT)) {
                // Skip since fragments are already managed through the fragment manager.
            } else if (tagName.equals(INCLUDE)) {
                String layout = parseId(tag.getAttributeValue(null, LAYOUT));
                Include.Builder includeBuilder = Include.of(layout, id);

                if (isAndroidId) includeBuilder.androidId();
                if (fieldName != null) includeBuilder.fieldName(fieldName);
                layoutBuilder.include(includeBuilder);
            } else {
                String holdrClass = parseType(tag.getAttributeValue(APP_NS, HOLDR_CLASS));

                String type;
                if (holdrClass != null) {
                    type = holdrClass;
                } else if (tagName.equals(VIEW)) {
                    type = parseClassType(tag.getAttributeValue(null, CLASS));
                } else {
                    type = parseType(tagName);
                }

                View.Builder viewBuilder = View.of(type, id);

                for (Listener.Type listenerType : Listener.Type.values()) {
                    String listenerName = tag.getAttributeValue(APP_NS, HOLDR_PREFIX + listenerType.layoutName());
                    if (listenerName != null) {
                        Listener.Builder listener = Listener.of(listenerType);
                        if (!listenerName.equals("true")) {
                            listener.name(listenerName);
                        }
                        viewBuilder.listener(listener);
                    }
                }

                if (isAndroidId) viewBuilder.androidId();
                if (fieldName != null) viewBuilder.fieldName(fieldName);
                layoutBuilder.view(viewBuilder);
            }
        }
    }

    private static boolean isIncluded(boolean isDefaultInclude, boolean hasId, IncludeIgnoreState state) {
        if (!hasId) return false;
        
        boolean hasIncludeAllTag = state.includeAllTag != null;
        boolean hasIgnoreAllTag = state.ignoreAllTag != null;
        
        if (isDefaultInclude) {
            return (state.include == ParserUtils.HoldrInclude.VIEW) || hasIncludeAllTag || (state.ignore == ParserUtils.HoldrIgnore.NONE && !hasIgnoreAllTag);
        } else {
            return (state.include != ParserUtils.HoldrInclude.NONE) || (hasIncludeAllTag && !hasIgnoreAllTag);
        }
    }

    private static String parseType(String type) {
        if (type == null) return null;
        if (type.contains(".")) return type;
        return PREFIX_MAP.get(type) + type;
    }

    private static String parseClassType(String type) {
        if (type == null) return null;
        return type.replace('$', '.');
    }

    private static String parseId(String id) {
        if (id == null) return null;
        int sep = id.indexOf('/');
        if (sep == -1) return id;
        return id.substring(sep + 1);
    }

    private static boolean parseIsAndroidId(String id) {
        return id != null && id.startsWith("@android");
    }

    private static HoldrIgnore parseIgnore(String ignore) {
        if (ignore == null) return HoldrIgnore.NONE;
        if (ignore.equals(VIEW)) {
            return HoldrIgnore.VIEW;
        }
        if (ignore.equals(ALL)) {
            return HoldrIgnore.ALL;
        }
        return HoldrIgnore.NONE;
    }

    private static HoldrInclude parseInclude(String include) {
        if (include == null) return HoldrInclude.NONE;
        if (include.equals(VIEW)) {
            return HoldrInclude.VIEW;
        }
        if (include.equals(ALL)) {
            return HoldrInclude.ALL;
        }
        return HoldrInclude.NONE;
    }

    public static enum HoldrIgnore {
        NONE, VIEW, ALL
    }

    public static enum HoldrInclude {
        NONE, VIEW, ALL
    }
    
    public static class IncludeIgnoreState {
        private HoldrIgnore ignore;
        private HoldrInclude include;
        private String ignoreAllTag; 
        private String includeAllTag;
        
        public void tagBegin(HoldrIgnore ignore, HoldrInclude include, String tagName) {
            this.ignore = ignore;
            this.include = include;
            
            if (ignoreAllTag == null && ignore == ParserUtils.HoldrIgnore.ALL) {
                ignoreAllTag = tagName;
            }

            if (includeAllTag == null && include == ParserUtils.HoldrInclude.ALL) {
                includeAllTag = tagName;
            }
        }
        
        public void tagEnd(String tagName) {
            if (ignoreAllTag != null && ignoreAllTag.equals(tagName)) {
                ignoreAllTag = null;
            }
        }
    }
    
    public interface Tag {
        String getName();
        String getAttributeValue(String ns, String name);
        boolean isRoot();
    }
}
