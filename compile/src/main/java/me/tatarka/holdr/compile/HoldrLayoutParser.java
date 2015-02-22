package me.tatarka.holdr.compile;

import me.tatarka.holdr.model.HoldrConfig;
import me.tatarka.holdr.model.Layout;
import me.tatarka.holdr.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;

public class HoldrLayoutParser implements Serializable {

    private final HoldrConfig config;
    private boolean isRootTag;

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
            final XmlPullParser parser = factory.newPullParser();
            parser.setInput(res);

            ParserUtils.IncludeIgnoreState state = new ParserUtils.IncludeIgnoreState();

            int tag;
            isRootTag = true;

            ParserUtils.Tag tagParser = new ParserUtils.Tag() {
                @Override
                public String getName() {
                    return parser.getName();
                }

                @Override
                public String getAttributeValue(String ns, String name) {
                    return parser.getAttributeValue(ns, name);
                }

                @Override
                public boolean isRoot() {
                    return isRootTag;
                }
            };

            while ((tag = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (tag == XmlPullParser.START_TAG) {
                    ParserUtils.parseTag(config, parsedLayoutBuilder, state, tagParser);
                } else if (tag == XmlPullParser.END_TAG) {
                    state.tagEnd(parser.getName());
                }
            }

            return parsedLayoutBuilder;
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
    }
}
