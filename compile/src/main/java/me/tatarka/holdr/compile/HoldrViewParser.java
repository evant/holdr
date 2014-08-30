package me.tatarka.holdr.compile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoldrViewParser {
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    private static final String APP_NS = "http://schemas.android.com/apk/res-auto";
    private static final String ID = "id";
    private static final String LAYOUT = "layout";
    private static final String INCLUDE = "include";
    private static final String HOLDR_IGNORE = "holdr_ignore";
    private static final String HOLDR_INCLUDE = "holdr_include";
    private static final String FIELD_NAME = "holdr_field_name";
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
    
    private boolean defaultInclude;
    
    public HoldrViewParser(boolean defaultInclude) {
        this.defaultInclude = defaultInclude;
    }

    public List<Ref> parse(Reader res) throws IOException {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(res);

            List<Ref> refs = new ArrayList<Ref>();
            String ignoreAllTag = null;
            String includeAllTag = null;

            int tag;
            while ((tag = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (tag == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();

                    String idString = parser.getAttributeValue(ANDROID_NS, ID);
                    String id = parseId(idString);
                    boolean isAndroidId = parseIsAndroidId(idString);
                    HoldrIgnore ignore = parseIgnore(parser.getAttributeValue(APP_NS, HOLDR_IGNORE));
                    HoldrInclude include = parseInclude(parser.getAttributeValue(APP_NS, HOLDR_INCLUDE));
                    
                    String fieldName = parser.getAttributeValue(APP_NS, FIELD_NAME);

                    if (ignoreAllTag == null && ignore == HoldrIgnore.ALL) {
                        ignoreAllTag = tagName;
                    }

                    if (includeAllTag == null && include == HoldrInclude.ALL) {
                        includeAllTag = tagName;
                    }

                    if (include(id != null, include, includeAllTag != null, ignore, ignoreAllTag != null)) {
                        Ref.Builder ref;
                        if (tagName.equals(INCLUDE)) {
                            String layout = parseId(parser.getAttributeValue(null, LAYOUT));
                            ref = Include.of(layout, id);
                        } else {
                            String type = parseType(tagName);
                            ref = View.of(type, id);
                        }

                        if (isAndroidId) {
                            ref.androidId();
                        }

                        if (fieldName != null) {
                            ref.fieldName(fieldName);
                        }

                        refs.add(ref.build());
                    }
                } else if (tag == XmlPullParser.END_TAG) {
                    if (ignoreAllTag != null && ignoreAllTag.equals(parser.getName())) {
                        ignoreAllTag = null;
                    }
                }
            }
            return refs;
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
    }

    private boolean include(boolean hasId, HoldrInclude include, boolean hasIncludeAllTag, HoldrIgnore ignore, boolean hasIgnoreAllTag) {
        if (!hasId) return false;
        if (defaultInclude) {
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
