package me.tatarka.holdr.compile

import me.tatarka.holdr.compile.model.Include
import me.tatarka.holdr.compile.model.Listener
import me.tatarka.holdr.compile.model.View
import spock.lang.Shared
import spock.lang.Specification

import static SpecHelpers.xml

class HoldrLayoutParserSpec extends Specification {
    @Shared
    def parser = new HoldrLayoutParser(true)

    def "a single non-id view parses as an empty list"() {
        expect:
        parser.parse(xml { it.'TextView'() }) == new ParsedLayout([])
    }

    def "a single view with an id parses as a single item"() {
        expect:
        parser.parse(xml {
            it.'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_text_view'
            )
        }) == new ParsedLayout([View.of('android.widget.TextView', 'my_text_view').build()])
    }

    def "a non-id view with 2 children parses as two items"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'('xmlns:android': 'http://schemas.android.com/apk/res/android') {
                'TextView'('android:id': '@+id/my_text_view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == new ParsedLayout([
                View.of('android.widget.TextView', 'my_text_view').build(),
                View.of('android.widget.ImageView', 'my_image_view').build()
        ])
    }

    def "an id view with non-id children parses as a single item"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_linear_layout'
            ) {
                'TextView'()
                'ImageView'()
            }
        }) == new ParsedLayout([
                View.of('android.widget.LinearLayout', 'my_linear_layout').build()
        ])
    }

    def "an id view with id children parses as a list of items"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_linear_layout'
            ) {
                'TextView'('android:id': '@+id/my_text_view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == new ParsedLayout([
                View.of('android.widget.LinearLayout', 'my_linear_layout').build(),
                View.of('android.widget.TextView', 'my_text_view').build(),
                View.of('android.widget.ImageView', 'my_image_view').build()
        ])
    }

    def "a view with an id but has a 'holdr_ignore=view' is not included"() {
        expect:
        parser.parse(xml {
            it.'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_text_view',
                    'app:holdr_ignore': 'view'
            )
        }) == new ParsedLayout([])
    }

    def "a view with an id but has a 'holdr_ignore=all' does not include itself or it's children"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_linear_layout',
                    'app:holdr_ignore': 'all'
            ) {
                'TextView'('android:id': '@+id/my_text_view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == new ParsedLayout([])
    }

    def "a view with 'holdr_ignore=all' can have a child with 'holdr_include=view' which will be included"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_linear_layout',
                    'app:holdr_ignore': 'all'
            ) {
                'TextView'('android:id': '@+id/my_text_view', 'app:holdr_include': 'view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == new ParsedLayout([View.of('android.widget.TextView', 'my_text_view').build()])
    }

    def "a view with 'holdr_ignore=all' can have a child with 'holdr_include=all' which will include all it's chilren"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_linear_layout',
                    'app:holdr_ignore': 'all'
            ) {
                'ImageView'('android:id': '@+id/my_image_view')
                'LinearLayout'('android:id': '@+id/my_child_linear_layout', 'app:holdr_include': 'all') {
                    'TextView'('android:id': '@+id/my_text_view')
                }
            }
        }) == new ParsedLayout([
                View.of('android.widget.LinearLayout', 'my_child_linear_layout').build(),
                View.of('android.widget.TextView', 'my_text_view').build(),
        ])
    }

    def "a view with a 'holdr_field_name' attribute has a custom field name"() {
        expect:
        parser.parse(xml {
            it.'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_text_view',
                    'app:holdr_field_name': 'my_field_name'
            )
        }) == new ParsedLayout([View.of('android.widget.TextView', 'my_text_view').fieldName('my_field_name').build()])
    }

    def "a view with and android id parses with that android id"() {
        expect:
        parser.parse(xml {
            it.'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@android:id/text1',
            )
        }) == new ParsedLayout([View.of('android.widget.TextView', 'text1').androidId().build()])
    }

    def "an include with an id parses as an include item"() {
        expect:
        parser.parse(xml {
            it.'include'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_include',
                    'layout': '@layout/my_layout'
            )
        }) == new ParsedLayout([Include.of('my_layout', 'my_include').build()])
    }

    def "an unqualified view in the 'view' namespace parses with the correct prefix"() {
        expect:
        parser.parse(xml {
            it."$view"(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_id',
            )
        }).refs.first().type == "android.view.$view"

        where:
        view << ['View', 'SurfaceView', 'TextureView', 'ViewStub']
    }

    def "an unqualified view in the 'webkit' namespace parses with the correct prefix"() {
        expect:
        parser.parse(xml {
            it."$view"(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_id',
            )
        }).refs.first().type == "android.webkit.$view"

        where:
        view << ['WebView']
    }

    def "an unqualified view in the 'widget' namespace parses with the correct prefix"() {
        expect:
        parser.parse(xml {
            it."$view"(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_id',
            )
        }).refs.first().type == "android.widget.$view"

        where:
        view << ['TextView', 'Button', 'ImageButton', 'EditText', 'ImageView', 'FrameLayout', 'LinearLayout', 'GridLayout']
    }

    def "a layout with a custom superclass parses with that superclass info"() {
        expect:
        parser.parse(xml {
            it.'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_text_view',
                    'app:holdr_superclass': 'test.TestHoldr'
            )
        }) == new ParsedLayout('test.TestHoldr', [View.of('android.widget.TextView', 'my_text_view').build()])
    }

    def "a layout with an onClick listener parses with that listener"() {
        expect:
        parser.parse(xml {
            it.'Button'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_button',
                    'app:holdr_onClick': 'true'
            )
        }) == new ParsedLayout([View.of('android.widget.Button', 'my_button')
                                        .listener(Listener.Type.ON_CLICK)
                                        .build()])
    }

    def "a layout with an onClick listener with a custom name parses with that listener"() {
        expect:
        parser.parse(xml {
            it.'Button'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_button',
                    'app:holdr_onClick': 'onTestButtonClick'
            )
        }) == new ParsedLayout([View.of('android.widget.Button', 'my_button')
                                        .listener(Listener.of(Listener.Type.ON_CLICK).name("onTestButtonClick"))
                                        .build()])
    }
}

