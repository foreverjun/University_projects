import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class MultiPriorityQueue<Elem : Comparable<Elem>>(private val numOfQueues: Int) {
    private val puckOfQueue = mutableListOf<PriorityQueueForMultiQueue>()
    private val counter = AtomicInteger()

    private inner class PriorityQueueForMultiQueue {
        var isLocked = AtomicBoolean()
        val queue = PriorityQueue<Elem>()
    }

    init {
        for (i in 0..numOfQueues) puckOfQueue.add(PriorityQueueForMultiQueue())
    }

    fun getCounter() = counter.get()

    fun incCounter() = counter.incrementAndGet()

    fun decCounter() = counter.decrementAndGet()

    fun add(value: Elem) {
        val random = ThreadLocal<Int>()
        random.set(getRandom(0, numOfQueues + 1))

        while (!puckOfQueue[random.get()].isLocked.compareAndSet(false, true))
            random.set(getRandom(0, numOfQueues + 1))

        puckOfQueue[random.get()].queue.add(value)
        counter.incrementAndGet()
        puckOfQueue[random.get()].isLocked.compareAndSet(true, false)
    }

    fun poll(): Elem? {
        val random1 = ThreadLocal<Int>()
        val random2 = ThreadLocal<Int>()

        random1.set(getRandom(0, numOfQueues + 1))
        do random2.set(getRandom(0, numOfQueues + 1))
        while (random1.get() == random2.get())

        while (true) {
            if (counter.get() < 1) return null

            while (!puckOfQueue[random1.get()].isLocked.compareAndSet(false, true)) {
                if (counter.get() < 1) return null
                random1.set(getRandom(0, numOfQueues + 1))
            }

            while (!puckOfQueue[random2.get()].isLocked.compareAndSet(false, true)) {
                random2.set(getRandom(0, numOfQueues + 1))
            }

            when {
                (!puckOfQueue[random1.get()].queue.isEmpty() && puckOfQueue[random2.get()].queue.isEmpty()) -> {
                    val returnBuf = puckOfQueue[random1.get()].queue.poll()
                    puckOfQueue[random1.get()].isLocked.compareAndSet(true, false)
                    puckOfQueue[random2.get()].isLocked.compareAndSet(true, false)
                    return returnBuf
                }

                (puckOfQueue[random1.get()].queue.isEmpty() && !puckOfQueue[random2.get()].queue.isEmpty()) -> {
                    val returnBuf = puckOfQueue[random2.get()].queue.poll()
                    puckOfQueue[random1.get()].isLocked.compareAndSet(true, false)
                    puckOfQueue[random2.get()].isLocked.compareAndSet(true, false)
                    return returnBuf
                }

                (puckOfQueue[random1.get()].queue.isEmpty() && puckOfQueue[random2.get()].queue.isEmpty()) -> {
                    puckOfQueue[random1.get()].isLocked.compareAndSet(true, false)
                    puckOfQueue[random2.get()].isLocked.compareAndSet(true, false)
                    random1.set(getRandom(0, numOfQueues + 1))
                    random2.set(getRandom(0, numOfQueues + 1))
                    continue
                }

                (!puckOfQueue[random1.get()].queue.isEmpty() && !puckOfQueue[random2.get()].queue.isEmpty()) -> {
                    if (puckOfQueue[random1.get()].queue.peek() < puckOfQueue[random2.get()].queue.peek()) {
                        val returnBuf = puckOfQueue[random1.get()].queue.poll()
                        puckOfQueue[random1.get()].isLocked.compareAndSet(true, false)
                        puckOfQueue[random2.get()].isLocked.compareAndSet(true, false)
                        return returnBuf
                    } else {
                        val returnBuf = puckOfQueue[random2.get()].queue.poll()
                        puckOfQueue[random1.get()].isLocked.compareAndSet(true, false)
                        puckOfQueue[random2.get()].isLocked.compareAndSet(true, false)
                        return returnBuf
                    }
                }
            }
        }
    }

    private fun getRandom(leftBound: Int, rightBound: Int): Int {
        return java.util.concurrent.ThreadLocalRandom.current().nextInt(leftBound, rightBound)
    }
}