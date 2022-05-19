import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.Executors

class WarshallGraph {
    private val nodes = mutableMapOf<Int, WarshallNode>()
    private val edges = mutableListOf<Edge>()
    fun getNodesMap() = nodes
    fun getEdgesList() = edges
    class WarshallNode(id: Int) {
        val outNodes = mutableListOf<Edge>()
    }

    fun calculateDistances(): Array<IntArray> {
        val numberOfNodes = nodes.size
        val distance2DArray = Array(numberOfNodes) { IntArray(numberOfNodes) { Int.MAX_VALUE } }
        val listOfNodeKeys = nodes.keys.sorted()
        edges.forEach {
            distance2DArray[listOfNodeKeys.indexOf(it.toId)][listOfNodeKeys.indexOf(it.fromId)] = it.weight
        }
        distance2DArray.forEachIndexed { index, it -> it[index] = 0 }

        for (k in 0 until numberOfNodes) {
            for (i in 0 until numberOfNodes) {
                if (distance2DArray[i][k] != Int.MAX_VALUE) {
                    for (j in 0 until numberOfNodes) {
                        val temp = distance2DArray[i][k] + distance2DArray[k][j]
                        if (temp > 0 && distance2DArray[i][j] > temp) distance2DArray[i][j] = temp
                    }
                }
            }
        }

        return distance2DArray
    }

    fun calculateDistancesParallel(numberOfThreads: Int = 1): Array<IntArray> {
        val numberOfNodes = nodes.size
        val distance2DArray = Array(numberOfNodes) { IntArray(numberOfNodes) { Int.MAX_VALUE } }
        val listOfNodeKeys = nodes.keys.sorted()
        edges.forEach {
            distance2DArray[listOfNodeKeys.indexOf(it.toId)][listOfNodeKeys.indexOf(it.fromId)] = it.weight
        }
        distance2DArray.forEachIndexed { index, it -> it[index] = 0 }

        val blocksSize = calculateBlocksSize(numberOfNodes, numberOfThreads)
        val borders = calculateBorders(blocksSize, numberOfThreads)
        val threadPool = Executors.newFixedThreadPool(numberOfThreads)
        for (k in 0 until numberOfNodes) {
            for (a in 1..numberOfThreads) {
                for (i in 0 + borders[a - 1] until borders[a - 1] + blocksSize[a]) {
                    for (b in 1..numberOfThreads) {
                        val task = Runnable {
                            for (j in 0 + borders[b - 1] until borders[b - 1] + blocksSize[b]) {
                                val temp = distance2DArray[i][k] + distance2DArray[k][j]
                                if (temp > 0 && distance2DArray[i][j] > temp) {
                                    distance2DArray[i][j] = temp
                                }
                            }
                        }
                        threadPool.execute(task)
                    }
                }
            }
        }

        threadPool.shutdown()
        while (!threadPool.isTerminated) {
        }

        return distance2DArray
    }

    private fun calculateBlocksSize(numberOfNodes: Int, numberOfThreads: Int): Array<Int> {
        val blocksSizeArr = Array(numberOfThreads + 1) { 0 }
        val size = numberOfNodes / numberOfThreads
        val remain = numberOfNodes % numberOfThreads
        for (i in 1..numberOfThreads)
            blocksSizeArr[i] = size
        blocksSizeArr[1] += remain
        return blocksSizeArr
    }

    private fun calculateBorders(blocksSize: Array<Int>, numberOfThreads: Int): Array<Int> {
        val borders = Array(numberOfThreads) { 0 }
        for (i in 1 until numberOfThreads) {
            for (j in 1..i) {
                borders[i] += blocksSize[j]
            }
        }
        return borders
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
                nodes[it.fromId] = WarshallNode(it.fromId)
            }
            nodes[it.fromId]?.outNodes?.add(it)
            if (!nodes.containsKey(it.toId))
                nodes[it.toId] = WarshallNode(it.toId)
        }
    }
}