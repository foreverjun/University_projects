import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultDirectedWeightedGraph
import org.jgrapht.graph.DefaultWeightedEdge
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WarshallTest {
    private var graph = WarshallGraph()
    private val jgraph = DefaultDirectedWeightedGraph<Int, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)
    private val pathsGraph = DijkstraShortestPath(jgraph)

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
    fun `sequential algorithm`() {
        val distances = graph.calculateDistances()
        val nodes = graph.getNodesMap().keys.toList().sorted()
        for (i in distances.indices) {
            val buf1 = pathsGraph.getPaths(nodes[i])
            for (j in distances.indices) {
                if (i != j)
                    assertEquals(distances[i][j], buf1.getWeight(nodes[j]).toInt())
            }
        }
    }

    @Test
    fun `parallel algorithm`() {
        val distances = graph.calculateDistancesParallel()
        val nodes = graph.getNodesMap().keys.toList().sorted()
        for (i in distances.indices) {
            val jdistances = pathsGraph.getPaths(nodes[i])
            for (j in distances.indices) {
                if (i != j)
                    assertEquals(distances[i][j], jdistances.getWeight(nodes[j]).toInt())
            }
        }
    }
}