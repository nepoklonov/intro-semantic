package utils.geometry

interface TurtleMoving {
    fun forward(distance: Double): TurtleMoving
    fun rotate(angle: Angle): TurtleMoving
}

data class Turtle(
    var location: Complex = 0 + 0.i,
    var angle: Angle = 0.deg
) : TurtleMoving {
    override fun rotate(angle: Angle): Turtle = apply { this.angle += angle }
    override fun forward(distance: Double): Turtle = apply { location += Complex(distance, angle) }

    fun update(location: Complex = this.location, angle: Angle = this.angle) = apply {
        this.location = location
        this.angle = angle
    }
}

fun Turtle.forward(distance: Number) = forward(distance.toDouble())

fun Turtle.back(distance: Number) = forward(-distance.toDouble())

fun Complex.directionTo(other: Complex): Turtle = Turtle(this, (other - this).arg)

fun Complex.directionBy(angle: Angle): Turtle = Turtle(this, angle)

fun Turtle.useLocation(block: (Complex) -> Unit) = apply { block(location) }