package me.tatarka.holdr.compile

import me.tatarka.holdr.model.Include
import me.tatarka.holdr.model.Layout
import me.tatarka.holdr.model.Listener
import me.tatarka.holdr.model.View
import spock.lang.Shared
import spock.lang.Specification

import static SpecHelpers.xml
import static me.tatarka.holdr.compile.SpecHelpers.testHoldrConfig
import static me.tatarka.holdr.compile.SpecHelpers.testPath

class HoldrLayoutParserSpec extends Specification {
    @Shared
    def parser = new HoldrLayoutParser(testHoldrConfig())

    def "a single non-id view parses as an empty list"() {
        expect:
        parser.parse(testPath(), xml { 'TextView'() }) == Layout.of(testPath()).build()
    }

    def "a single view with an id parses as a single item"() {
        expect:
        parser.parse(testPath(), xml {
            'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_text_view'
            )
        }) == Layout.of(testPath()).view(View.of('android.widget.TextView', 'my_text_view')).build()
    }

    def "a non-id view with 2 children parses as two items"() {
        expect:
        parser.parse(testPath(), xml {
            'LinearLayout'('xmlns:android': 'http://schemas.android.com/apk/res/android') {
                'TextView'('android:id': '@+id/my_text_view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == Layout.of(testPath())
                .view(View.of('android.widget.TextView', 'my_text_view'))
                .view(View.of('android.widget.ImageView', 'my_image_view')).build()

    }

    def "an id view with non-id children parses as a single item"() {
        expect:
        parser.parse(testPath(), xml {
            'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_linear_layout'
            ) {
                'TextView'()
                'ImageView'()
            }
        }) == Layout.of(testPath())
                .view(View.of('android.widget.LinearLayout', 'my_linear_layout')).build()

    }

    def "an id view with id children parses as a list of items"() {
        expect:
        parser.parse(testPath(), xml {
            'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_linear_layout'
            ) {
                'TextView'('android:id': '@+id/my_text_view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == Layout.of(testPath())
                .view(View.of('android.widget.LinearLayout', 'my_linear_layout'))
                .view(View.of('android.widget.TextView', 'my_text_view'))
                .view(View.of('android.widget.ImageView', 'my_image_view')).build()

    }

    def "a view with an id but has a 'holdr_ignore=view' is not included"() {
        expect:
        parser.parse(testPath(), xml {
            'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_text_view',
                    'app:holdr_ignore': 'view'
            )
        }) == Layout.of(testPath()).build()
    }

    def "a view with an id but has a 'holdr_ignore=all' does not include itself or it's children"() {
        expect:
        parser.parse(testPath(), xml {
            'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_linear_layout',
                    'app:holdr_ignore': 'all'
            ) {
                'TextView'('android:id': '@+id/my_text_view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == Layout.of(testPath()).build()
    }

    def "a view with 'holdr_ignore=all' can have a child with 'holdr_include=view' which will be included"() {
        expect:
        parser.parse(testPath(), xml {
            'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_linear_layout',
                    'app:holdr_ignore': 'all'
            ) {
                'TextView'('android:id': '@+id/my_text_view', 'app:holdr_include': 'view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == Layout.of(testPath()).view(View.of('android.widget.TextView', 'my_text_view')).build()
    }

    def "a view with 'holdr_ignore=all' can have a child with 'holdr_include=all' which will include all it's chilren"() {
        expect:
        parser.parse(testPath(), xml {
            'LinearLayout'(
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
        }) == Layout.of(testPath())
                .view(View.of('android.widget.LinearLayout', 'my_child_linear_layout'))
                .view(View.of('android.widget.TextView', 'my_text_view')).build()

    }

    def "a view with a 'holdr_field_name' attribute has a custom field name"() {
        expect:
        parser.parse(testPath(), xml {
            'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_text_view',
                    'app:holdr_field_name': 'my_field_name'
            )
        }) == Layout.of(testPath())
                .view(View.of('android.widget.TextView', 'my_text_view').fieldName('my_field_name')).build()

    }

    def "a view with and android id parses with that android id"() {
        expect:
        parser.parse(testPath(), xml {
            'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@android:id/text1',
            )
        }) == Layout.of(testPath())
                .view(View.of('android.widget.TextView', 'text1').androidId()).build()

    }

    def "an include with an id parses as an include item"() {
        expect:
        parser.parse(testPath(), xml {
            'include'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_include',
                    'layout': '@layout/my_layout'
            )
        }) == Layout.of(testPath())
                .include(Include.of('my_layout', 'my_include')).build()

    }

    def "an unqualified view in the 'view' namespace parses with the correct prefix"() {
        expect:
        parser.parse(testPath(), xml {
            "$view"(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_id',
            )
        }).refs.first().type == "android.view.$view"

        where:
        view << ['View', 'SurfaceView', 'TextureView', 'ViewStub']
    }

    def "an unqualified view in the 'webkit' namespace parses with the correct prefix"() {
        expect:
        parser.parse(testPath(), xml {
            "$view"(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_id',
            )
        }).refs.first().type == "android.webkit.$view"

        where:
        view << ['WebView']
    }

    def "an unqualified view in the 'widget' namespace parses with the correct prefix"() {
        expect:
        parser.parse(testPath(), xml {
            "$view"(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_id',
            )
        }).refs.first().type == "android.widget.$view"

        where:
        view << ['TextView', 'Button', 'ImageButton', 'EditText', 'ImageView', 'FrameLayout', 'LinearLayout', 'GridLayout']
    }

    def "a layout with a custom superclass parses with that superclass info"() {
        expect:
        parser.parse(testPath(), xml {
            'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_text_view',
                    'app:holdr_superclass': 'test.TestHoldr'
            )
        }) == Layout.of(testPath())
                .superclass('test.TestHoldr')
                .view(View.of('android.widget.TextView', 'my_text_view')).build()

    }

    def "a layout with an onClick listener parses with that listener"() {
        expect:
        parser.parse(testPath(), xml {
            'Button'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_button',
                    'app:holdr_onClick': 'true'
            )
        }) == Layout.of(testPath())
                .view(View.of('android.widget.Button', 'my_button')
                .listener(Listener.Type.ON_CLICK)).build()

    }

    def "a layout with an onClick listener with a custom name parses with that listener"() {
        expect:
        parser.parse(testPath(), xml {
            'Button'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_button',
                    'app:holdr_onClick': 'onTestButtonClick'
            )
        }) == Layout.of(testPath())
                .view(View.of('android.widget.Button', 'my_button')
                .listener(Listener.of(Listener.Type.ON_CLICK).name("onTestButtonClick"))).build()

    }

    def "a custom view defined as the tag parses as that view"() {
        expect:
        parser.parse(testPath(), xml {
            'test.Test'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_test_view'
            )
        }) == Layout.of(testPath()).view(View.of('test.Test', 'my_test_view')).build()
    }

    def "a custom view defined as a class on a <view/> tag parses as that view"() {
        expect:
        parser.parse(testPath(), xml {
            'view'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'class': 'test.Test',
                    'android:id': '@+id/my_test_view'
            )
        }) == Layout.of(testPath()).view(View.of('test.Test', 'my_test_view')).build()
    }

    def "an inner-class custom view defined as a class on a <view/> tag parses as that view"() {
        expect:
        parser.parse(testPath(), xml {
            'view'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'class': 'test.Test$Inner',
                    'android:id': '@+id/my_test_view'
            )
        }) == Layout.of(testPath()).view(View.of('test.Test.Inner', 'my_test_view')).build()
    }

    def "a view with custom holdr_class parses with that class instead of the on defined by the tag"() {
        expect:
        parser.parse(testPath(), xml {
            'test.CustomTextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_text_view',
                    'app:holdr_class': 'TextView',
            )
        }) == Layout.of(testPath()).view(View.of("android.widget.TextView", "my_text_view")).build()
    }

    def "a view with a fragment with an id is ignored"() {
        expect:
        parser.parse(testPath(), xml {
            'fragment'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_fragment'
            )
        }) == Layout.of(testPath()).build()
    }

    def "a view with a root merge tag is detected as such"() {
        expect:
        parser.parse(testPath(), xml {
            'merge'('xmlns:android': 'http://schemas.android.com/apk/res/android') {
                'TextView'(
                        'android:id': '@+id/my_text_view'
                )
            }
        }) == Layout.of(testPath()).rootMerge(true).view(View.of('android.widget.TextView', 'my_text_view')).build()
    }
}


