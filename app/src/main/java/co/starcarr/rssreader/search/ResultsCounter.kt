package co.starcarr.rssreader.search

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor

@ObsoleteCoroutinesApi
object ResultsCounter {
    // private fields

    private val dispatcher: ExecutorCoroutineDispatcher = newSingleThreadContext("counter")
    private var counter = 0
    private val _notifications = Channel<Int>(Channel.CONFLATED) // drop all but last notification

    private enum class Action {
        INCREASE,
        RESET,
    }

    private val actor = GlobalScope.actor<Action>(dispatcher) {
        for (msg in channel) {
            when(msg) {
                Action.INCREASE -> counter++
                Action.RESET -> counter = 0
            }
            _notifications.send(counter)
        }
    }

    // public interface

    suspend fun increment() = actor.send(Action.INCREASE)
    suspend fun reset() = actor.send(Action.RESET)
    val notifications: ReceiveChannel<Int>
        get() = _notifications // expose a property w/ coerced type so callers may not write to channel
}