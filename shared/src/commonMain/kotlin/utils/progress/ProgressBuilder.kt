package utils.progress

import utils.delegates.once
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

interface CountableProgressReporter {
    fun init(all: Int)
    fun start()
    fun setDone(done: Int)
    fun incrementDone(done: Int)
    fun log(message: String)
}

interface ProgressWatcher {
    val timePassed: Duration?
    val estimatedTime: Duration?
    val estimatedTimeLeft: Duration?

    val progress: CountableProgress?
}

class CountableProgressBuilder {
    private var startMark: TimeMark? = null
    private var totalActions: Int? = null

    val progress by once {
        totalActions?.let { progressOf(it) }
    }

    val watcher = object : ProgressWatcher {
        override val timePassed: Duration? get() = startMark?.elapsedNow()
        override val estimatedTime: Duration? get() = timePassed?.div(progress?.proportion?.takeIf { it != 0.0 } ?: 1.0)
        override val estimatedTimeLeft: Duration? get() = estimatedTime?.minus(timePassed!!)
        override val progress: CountableProgress? get() = this@CountableProgressBuilder.progress
    }

    val reporter = object : CountableProgressReporter {
        override fun init(all: Int) {
            check(totalActions == null) { "already initialized" }
            totalActions = all
        }

        override fun start() {
            checkNotNull(totalActions) { "progress must be initialized" }
            check(startMark == null) { "ну круто" }
            startMark = TimeSource.Monotonic.markNow()
        }

        override fun setDone(done: Int) {
            val progress = progress!!
            checkNotNull(startMark) { "the process has not started yet" }
            check(done > progress.done) { "some things cannot be undone" }
            progress.done = done
        }

        override fun incrementDone(done: Int) {
            val progress = progress!!
            checkNotNull(startMark) { "the process has not started yet" }
            check(done > 0) { "some things cannot be undone" }
            progress.done += done
        }

        override fun log(message: String) {
            println(message)
        }

    }
}
