package utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun <T> T.runIf(condition: Boolean, block: T.() -> T) = if (condition) block(this) else this

fun <T> T.letIf(condition: Boolean, block: (T) -> T) = if (condition) block(this) else this

@OptIn(ExperimentalContracts::class)
class IfValue<T>(val value: T, val condition: (T) -> Boolean) {
    fun continueLet(block: (T) -> T): T = if (condition(value)) block(value) else value
    fun continueRun(block: T.() -> T): T = if (condition(value)) block(value) else value
}

fun <T> T.letIf(condition: (T) -> Boolean) = IfValue(this, condition)

fun <T> T.runIf(condition: T.() -> Boolean) = IfValue(this, condition)