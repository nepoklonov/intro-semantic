package utils.progress

import kotlinx.serialization.Serializable

fun progressOf(amount: Int) = CountableProgress(all = amount, done = 0)

interface Progress {
    val proportion: Double
}

val Progress.percentage: Double get() = proportion * 100

@Serializable
data class ApproximateProgress(
    override val proportion: Double
) : Progress

@Serializable
data class CountableProgress(
    val all: Int,
    var done: Int = 0,
) : Progress {
    override val proportion: Double
        get() = if (all == 0) 0.0 else done.toDouble() / all
}