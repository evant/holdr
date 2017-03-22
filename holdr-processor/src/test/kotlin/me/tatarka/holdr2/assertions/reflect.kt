package me.tatarka.holdr2.assertions

import me.tatarka.assertk.Assert
import me.tatarka.assertk.assert
import me.tatarka.assertk.assertions.isEqualTo
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass


fun Assert<Method>.hasReturnType(kClass: KClass<*>) {
    assert("returnType", actual.returnType).isEqualTo(kClass.java)
}

fun Assert<Field>.hasType(kClass: KClass<*>) {
    assert("type", actual.type).isEqualTo(kClass.java)
}

fun Assert<Any>.hasSimpleClassName(name: String) {
    assert("simpleClassName", actual.javaClass.simpleName).isEqualTo(name)
}