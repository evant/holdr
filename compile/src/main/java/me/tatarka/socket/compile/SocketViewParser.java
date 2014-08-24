package me.tatarka.socket.compile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class SocketViewParser {
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    private static final String APP_NS = "http://schemas.android.com/apk/res-auto";
    private static final String ID = "id";
    private static final String LAYOUT = "layout";
    private static final String INCLUDE = "include";
    private static final String SOCKET_IGNORE = "socket_ignore";
    private static final String SOCKET_INCLUDE = "socket_include";
    private static final String FIELD_NAME = "socket_field_name";

    public List<Ref> parse(Reader res) throws IOException {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(res);

            List<Ref> refs = new ArrayList<Ref>();
            String ignoreChildrenTag = null;
            String includeTagName = null;

            int tag;
            while ((tag = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (tag == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();

                    String idString = parser.getAttributeValue(ANDROID_NS, ID);
                    String id = parseId(idString);
                    boolean isAndroidId = parseIsAndroidId(idString);
                    SocketIgnore ignore = parseIgnore(parser.getAttributeValue(APP_NS, SOCKET_IGNORE));
                    SocketInclude include = parseInclude(parser.getAttributeValue(APP_NS, SOCKET_INCLUDE));
                    
                    String fieldName = parser.getAttributeValue(APP_NS, FIELD_NAME);

                    if (ignoreChildrenTag == null && ignore == SocketIgnore.CHILDREN) {
                        ignoreChildrenTag = tagName;
                    }

                    if (includeTagName == null && include == SocketInclude.CHILDREN) {
                        includeTagName = tagName;
                    }

                    if (id != null && include(include, includeTagName != null, ignore, ignoreChildrenTag != null)) {
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
                    if (ignoreChildrenTag != null && ignoreChildrenTag.equals(parser.getName())) {
                        ignoreChildrenTag = null;
                    }
                }
            }
            return refs;
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
    }

    private boolean include(SocketInclude include, boolean hasIncludeChildrenTag, SocketIgnore ignore, boolean hasIgnoreChildrenTag) {
        return (include == SocketInclude.VIEW) || hasIncludeChildrenTag || (ignore == SocketIgnore.NONE && !hasIgnoreChildrenTag);
    }
    
    private static String parseType(String type) {
        if (type == null) return null;
        if (type.contains(".")) return type;
        if (type.equals("View")) return "android.view.View";
        return "android.widget." + type;
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

    private static SocketIgnore parseIgnore(String ignore) {
        if (ignore == null) return SocketIgnore.NONE;
        if (ignore.equals("view")) {
            return SocketIgnore.VIEW;
        }
        if (ignore.equals("children")) {
            return SocketIgnore.CHILDREN;
        }
        return SocketIgnore.NONE;
    }
    
    private static SocketInclude parseInclude(String include) {
        if (include == null) return SocketInclude.NONE;
        if (include.equals("view")) {
            return SocketInclude.VIEW;
        }
        if (include.equals("children")) {
            return SocketInclude.CHILDREN;
        }
        return SocketInclude.NONE;
    }

    private static enum SocketIgnore {
        NONE, VIEW, CHILDREN
    }

    private static enum SocketInclude {
        NONE, VIEW, CHILDREN
    }
}
