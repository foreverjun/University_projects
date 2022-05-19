import java.io.File
import java.io.FileNotFoundException
import java.util.*

class DijkstraGraph{
    private val nodes = mutableMapOf<Int,DijkstraNode>()
    private val edges = mutableListOf<Edge>()
    fun getNodesMap() = nodes
    fun getEdgesList () = edges

    class DijkstraNode(id: Int):Comparable<DijkstraNode>{
        val outNodes = mutableListOf<Edge>()
        var distance = java.util.concurrent.atomic.AtomicInteger(Int.MAX_VALUE)
        var isVisited = false
        override fun compareTo(other: DijkstraNode) = distance.get().compareTo(other.distance.get())
    }

    fun calculateDistancesParallel(startNodeID: Int? = nodes.keys.minOrNull(), numberOfThreads: Int = 1): MutableMap<Int, Int> {
        if (nodes.isEmpty() || edges.isEmpty()) throw Exception("The list of nodes or edges is empty")
        val startNode = nodes[startNodeID] ?: nodes[nodes.keys.minOrNull()]
        ?: throw Exception("The value of the start node is null")
        nodes.forEach { (_, u) ->
            u.distance =  java.util.concurrent.atomic.AtomicInteger(Int.MAX_VALUE)
            u.isVisited = false
        }
        startNode.distance.set(0)
        val multiPQ = MultiPriorityQueue<DijkstraNode>(numberOfThreads)
        multiPQ.add(startNode)
        val executor = java.util.concurrent.Executors.newFixedThreadPool(numberOfThreads)

        (1..numberOfThreads).map{
            executor.submit{
                while (multiPQ.getCounter() > 0) {
                    val currentNode = multiPQ.poll() ?: continue
                    for (edge in currentNode.outNodes) {
                        val temp = nodes[edge.toId]!!
                        while (true) {
                            val oldDistance = temp.distance.get()
                            val newDistance = currentNode.distance.get() + edge.weight
                            if (newDistance < oldDistance) {
                                if (!temp.distance.compareAndSet(oldDistance, newDistance)) continue
                                multiPQ.add(temp)
                            }
                            break
                        }
                    }
                    multiPQ.decCounter()
                }
            }
        }.forEach { it.get() }
        executor.shutdown()

        val rMap = mutableMapOf<Int,Int>()
        nodes.toList().forEach { rMap.put(it.first, it.second.distance.get()) }

        return rMap
    }

    fun calculateDistances(startNodeID: Int? = nodes.keys.minOrNull()): MutableMap<Int, Int> {
        if (nodes.isEmpty() || edges.isEmpty()) throw Exception("The list of nodes or edges is empty")
        val startNode = nodes[startNodeID] ?: nodes[nodes.keys.minOrNull()]
        ?: throw Exception("The value of the start node is null")
        nodes.forEach { (_, u) ->
            u.distance =  java.util.concurrent.atomic.AtomicInteger(Int.MAX_VALUE)
            u.isVisited = false
        }
        startNode.distance.set(0)
        val queue = PriorityQueue<DijkstraNode>()
        queue.add(startNode)

        while (queue.isNotEmpty()) {
            val currentNode = queue.poll()
            for (edge in currentNode.outNodes) {
                val temp = nodes[edge.toId]!!
                if (!temp.isVisited) {
                    val newDistance = edge.weight + currentNode.distance.get()
                    if (newDistance < temp.distance.get()) {
                        queue.remove(temp)
                        temp.distance.set(newDistance)
                        queue.add(temp)
                    }
                }
            }
            currentNode.isVisited = true
        }
        val rMap = mutableMapOf<Int,Int>()
        nodes.toList().forEach { rMap.put(it.first, it.second.distance.get()) }
        return rMap
    }

    fun exportFromTxt(filePath: String) {
        this.edges.clear()
        this.nodes.clear()

        val file = File(filePath)
        val elementsInLine = 3
        if (!file.exists()) throw FileNotFoundException("File '$filePath' not found")

        file.readLines().forEachIndexed { index: Int, it ->
            val buffer = it.split(" ")
            if (buffer.size != elementsInLine) throw Exception("Input format error in line: ${index + 1}")
            edges.add(Edge(buffer[0].toInt(), buffer[1].toInt(), buffer[2].toInt()))
        }
        edges.distinctBy { Pair(it.fromId, it.toId) }
        edges.forEach {
            if (!nodes.containsKey(it.fromId)) {
                nodes[it.fromId] = DijkstraNode(it.fromId)
            }
            nodes[it.fromId]?.outNodes?.add(it)
            if (!nodes.containsKey(it.toId))
                nodes[it.toId] = DijkstraNode(it.toId)
        }
    }
}