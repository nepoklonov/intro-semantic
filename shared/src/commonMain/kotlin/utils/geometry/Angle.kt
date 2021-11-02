package utils.geometry

import kotlin.math.PI

data class Angle(val radians: Double) {
    operator fun plus(other: Angle) = Angle(radians + other.radians)
    operator fun minus(other: Angle) = Angle(radians - other.radians)
    operator fun times(coefficient: Number) = Angle(radians * coefficient.toDouble())
    operator fun div(coefficient: Number) = Angle(radians / coefficient.toDouble())
    operator fun unaryMinus(): Angle = Angle(-radians)
}

val Number.deg: Angle
    get() = Angle(toDouble() * PI / 180)

val Number.radians: Angle
    get() = Angle(toDouble())