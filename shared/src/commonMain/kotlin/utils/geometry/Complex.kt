package utils.geometry

import kotlin.math.*

enum class ComplexForm { CARTESIAN, POLAR }

class Complex {
    val re: Double
    val im: Double
    val abs: Double
    val arg: Angle
    val form: ComplexForm

    constructor (re: Double, im: Double) {
        this.re = re
        this.im = im
        abs = hypot(re, im)
        arg = atan2(im, re).radians
        form = ComplexForm.CARTESIAN
    }

    constructor(re: Number, im: Number) : this(re.toDouble(), im.toDouble())

    constructor(abs: Double, arg: Angle) {
        re = abs * cos(arg.radians)
        im = abs * sin(arg.radians)
        this.abs = abs
        this.arg = arg
        form = ComplexForm.POLAR
    }

    constructor(abs: Number, arg: Angle) : this(abs.toDouble(), arg)
    fun copy(re : Double = this.re, im : Double = this.im) = Complex(re, im)


    override fun toString(): String {
        return "$re,$im"
    }

    operator fun component1(): Double = re
    operator fun component2(): Double = im
}

operator fun Complex.plus(other: Complex) = Complex(re + other.re, im + other.im)
operator fun Complex.plus(other: Number) = Complex(re + other.toDouble(), im)
infix operator fun Number.plus(other: Complex) = other + this

operator fun Complex.unaryMinus() = Complex(-re, -im)

operator fun Complex.not() = Complex(re, -im)

operator fun Complex.minus(other: Complex) = this + -other
operator fun Complex.minus(other: Number) = Complex(re - other.toDouble(), im)
operator fun Number.minus(other: Complex) = this + -other

operator fun Complex.times(other: Complex) = Complex(
    re = re * other.re - im * other.im,
    im = re * other.im + im * other.re
)

operator fun Complex.times(other: Number) = Complex(re * other.toDouble(), im * other.toDouble())
operator fun Number.times(other: Complex) = other * this

operator fun Complex.div(other: Number) = Complex(re / other.toDouble(), im / other.toDouble())

val Number.i: Complex
    get() = Complex(0, this)