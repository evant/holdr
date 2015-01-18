package me.tatarka.holdr.compile;

import me.tatarka.holdr.compile.model.Include;
import me.tatarka.holdr.compile.model.Listener;
import me.tatarka.holdr.compile.model.View;
import me.tatarka.holdr.model.HoldrConfig;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class HoldrLayoutParser implements Serializable{
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    private static final String APP_NS = "http://schemas.android.com/apk/res-auto";
    private static final String ID = "id";
    private static final String LAYOUT = "layout";
    private static final String INCLUDE = "include";
    private static final String MERGE = "merge";
    private static final String FRAGMENT = "fragment";
    private static final String CLASS = "class";

    private static final String HOLDR_PREFIX = "holdr_";
    private static final String HOLDR_IGNORE = HOLDR_PREFIX + "ignore";
    private static final String HOLDR_INCLUDE = HOLDR_PREFIX + "include";
    private static final String HOLDR_FIELD_NAME = HOLDR_PREFIX + "field_name";
    private static final String HOLDR_SUPERCLASS = HOLDR_PREFIX + "superclass";
    private static final String HOLDR_CLASS = HOLDR_PREFIX + "class";
    
    private static final String VIEW = "view";
    private static final String ALL = "all";

    private static final String PREFIX_VIEW = "android.view.";
    private static final String PREFIX_WIDGET = "android.widget.";
    private static final String PREFIX_WEBKIT = "android.webkit.";
    
    private static final Map<String, String> PREFIX_MAP = new HashMap<String, String>() {{
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
    
    private final HoldrConfig config;
    
    public HoldrLayoutParser(HoldrConfig config) {
        this.config = config;
    }

    public Layout.Builder parse(String layoutName, String res) throws IOException {
        return parse(layoutName, new StringReader(res));
    }
    
    public Layout.Builder parse(String layoutName, Reader res) throws IOException {
        Layout.Builder parsedLayoutBuilder = Layout.of(layoutName);
        
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(res);

            String ignoreAllTag = null;
            String includeAllTag = null;
            
            int tag;
            boolean isRootTag = true;
            
            while ((tag = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (tag == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();

                    if (isRootTag) {
                        String superclass = parser.getAttributeValue(APP_NS, HOLDR_SUPERCLASS);
                        if (superclass != null) {
                            parsedLayoutBuilder.superclass(superclass);
                        }
                        isRootTag = false;
                        
                        if (tagName.equals(MERGE)) {
                            parsedLayoutBuilder.rootMerge(true);
                        }
                    }

                    HoldrIgnore ignore = parseIgnore(parser.getAttributeValue(APP_NS, HOLDR_IGNORE));
                    HoldrInclude include = parseInclude(parser.getAttributeValue(APP_NS, HOLDR_INCLUDE));

                    if (ignoreAllTag == null && ignore == HoldrIgnore.ALL) {
                        ignoreAllTag = tagName;
                    }

                    if (includeAllTag == null && include == HoldrInclude.ALL) {
                        includeAllTag = tagName;
                    }

                    String idString = parser.getAttributeValue(ANDROID_NS, ID);
                    String id = parseId(idString);

                    if (include(id != null, include, includeAllTag != null, ignore, ignoreAllTag != null)) {
                        boolean isAndroidId = parseIsAndroidId(idString);
                        String fieldName = parser.getAttributeValue(APP_NS, HOLDR_FIELD_NAME);

                        if (tagName.equals(FRAGMENT)) {
                            // Skip since fragments are already managed through the fragment manager.
                        } else if (tagName.equals(INCLUDE)) {
                            String layout = parseId(parser.getAttributeValue(null, LAYOUT));
                            Include.Builder includeBuilder = Include.of(layout, id);
                            
                            if (isAndroidId) includeBuilder.androidId();
                            if (fieldName != null) includeBuilder.fieldName(fieldName);
                            parsedLayoutBuilder.include(includeBuilder);
                        } else {
                            String holdrClass = parseType(parser.getAttributeValue(APP_NS, HOLDR_CLASS));

                            String type;
                            if (holdrClass != null) {
                                type = holdrClass;
                            } else if (tagName.equals(VIEW)) {
                                type = parseClassType(parser.getAttributeValue(null, CLASS));
                            } else {
                                type = parseType(tagName);
                            }
                            
                            View.Builder viewBuilder = View.of(type, id);

                            for (Listener.Type listenerType : Listener.Type.values()) {
                                String listenerName = parser.getAttributeValue(APP_NS, HOLDR_PREFIX + listenerType.layoutName());
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
                            parsedLayoutBuilder.view(viewBuilder);
                        }
                    }
                } else if (tag == XmlPullParser.END_TAG) {
                    if (ignoreAllTag != null && ignoreAllTag.equals(parser.getName())) {
                        ignoreAllTag = null;
                    }
                }
            }
            
            return parsedLayoutBuilder;
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
    }

    private boolean include(boolean hasId, HoldrInclude include, boolean hasIncludeAllTag, HoldrIgnore ignore, boolean hasIgnoreAllTag) {
        if (!hasId) return false;
        if (config.getDefaultInclude()) {
            return (include == HoldrInclude.VIEW) || hasIncludeAllTag || (ignore == HoldrIgnore.NONE && !hasIgnoreAllTag);
        } else {
            return (include != HoldrInclude.NONE) || (hasIncludeAllTag && !hasIgnoreAllTag);
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

    private static enum HoldrIgnore {
        NONE, VIEW, ALL
    }

    private static enum HoldrInclude {
        NONE, VIEW, ALL
    }
}
