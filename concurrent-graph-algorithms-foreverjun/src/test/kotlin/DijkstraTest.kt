import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultDirectedWeightedGraph
import org.jgrapht.graph.DefaultWeightedEdge
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DijkstraTest {
    private var graph = DijkstraGraph()
    private val jgraph = DefaultDirectedWeightedGraph<Int, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)
    private val test = DijkstraShortestPath(jgraph)

    @BeforeAll
    fun sturtup() {
        graph.exportFromTxt("""D:\git\concurrent-graph-algorithms-foreverjun\src\test\resources\graph.txt""")
        graph.getNodesMap().toList().forEach { jgraph.addVertex(it.first) }
        graph.getEdgesList().forEach {
            jgraph.addEdge(it.fromId, it.toId)
            jgraph.setEdgeWeight(it.fromId, it.toId, it.weight.toDouble())
        }
    }

    @Test
    fun `sequential algorithm_1`() {
        val distances = graph.calculateDistances(startNodeID = 0)
        val nodes = graph.getNodesMap().keys.toList().sorted()
        val jdistances = test.getPaths(0)
        nodes.forEach { assertEquals(distances[it], jdistances.getWeight(it).toInt()) }
    }

    @Test
    fun `sequential algorithm_2`() {
        val distances = graph.calculateDistances(startNodeID = 595)
        val nodes = graph.getNodesMap().keys.toList().sorted()
        val jdistances = test.getPaths(595)
        nodes.forEach {
            assertEquals(distances[it], jdistances.getWeight(it).toInt()) }
    }

    @Test
    fun `parallel algorithm_1`() {
        val distances = graph.calculateDistancesParallel(startNodeID = 0, numberOfThreads = 8)
        val nodes = graph.getNodesMap().keys.toList().sorted()
        val jdistances = test.getPaths(0)
        nodes.forEach { assertEquals(distances[it], jdistances.getWeight(it).toInt()) }
    }

    @Test
    fun `parallel algorithm_2`() {
        val distances = graph.calculateDistancesParallel(startNodeID = 595, numberOfThreads = 8)
        val nodes = graph.getNodesMap().keys.toList().sorted()
        val jdistances = test.getPaths(595)
        nodes.forEach { assertEquals(distances[it], jdistances.getWeight(it).toInt()) }
    }
}