package com.kitappla.app.core.cache

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionNoticeTest {
    private var now = 0L
    private val notice = ConnectionNotice(clock = { now }, minGapMillis = 10_000)

    private fun collecting(block: (received: () -> Int) -> Unit) = runBlocking {
        var received = 0
        val job = launch(Dispatchers.Unconfined, start = CoroutineStart.UNDISPATCHED) {
            notice.events.collect { received++ }
        }
        block { received }
        job.cancel()
    }

    @Test
    fun burstOfReportsShowsOneNotice() = collecting { received ->
        notice.report()
        notice.report()
        notice.report()
        assertEquals(1, received())
    }

    @Test
    fun anotherNoticeIsAllowedAfterTheGap() = collecting { received ->
        notice.report()
        now += 10_000
        notice.report()
        assertEquals(2, received())
    }

    @Test
    fun reportWithoutListenerDoesNotThrow() {
        notice.report()
    }
}
