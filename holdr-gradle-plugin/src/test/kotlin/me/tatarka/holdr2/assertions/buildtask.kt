package me.tatarka.holdr2.assertions

import me.tatarka.assertk.Assert
import me.tatarka.assertk.assert
import me.tatarka.assertk.assertions.isEqualTo
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.TaskOutcome

fun Assert<BuildTask>.hasOutcome(outcome: TaskOutcome) {
    assert("outcome", actual.outcome).isEqualTo(outcome)
}

fun Assert<BuildTask>.isSuccess() {
    hasOutcome(TaskOutcome.SUCCESS)
}

fun Assert<BuildTask>.isUpToDate() {
    hasOutcome(TaskOutcome.UP_TO_DATE)
}
