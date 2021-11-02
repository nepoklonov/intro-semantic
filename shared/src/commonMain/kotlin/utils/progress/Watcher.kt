package utils.progress

import utils.collections.KeyElement
import utils.collections.KeySet
import utils.collections.MutableKeySet
import utils.collections.mutableKeySetOf
import kotlin.random.Random

class Task(
    val progressBuilder: CountableProgressBuilder
) : KeyElement<String>, AbstractTask {
    override val key = Random.nextLong().toString()
}

class ComplexTask : KeyElement<String>, AbstractTask, TaskOpener<Task>() {
    override val key = Random.nextLong().toString()
    override fun newTask(): Task = Task(progressBuilder = CountableProgressBuilder())
}

object Watcher : TaskOpener<ComplexTask>() {
    override fun newTask(): ComplexTask = ComplexTask()
}

interface AbstractTask : KeyElement<String>

abstract class TaskOpener<T : AbstractTask> {
    val tasks: KeySet<String, T> get() = innerTasks

    fun openTask(): T {
        return newTask().also {
            innerTasks += it
        }
    }


    private val innerTasks: MutableKeySet<String, T> = mutableKeySetOf()
    protected abstract fun newTask(): T
}