import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TreeTest {
    lateinit var testTree: BSTree<Int, String>
    var `threads-pool` = 6
    val executor: ExecutorService = Executors.newFixedThreadPool(`threads-pool`)

    companion object {
        val dataSameKeys = listOf(
            Pair(-100, "1"),
            Pair(-100, "2"),
            Pair(-100, "3"),
            Pair(-100, "4"),
            Pair(-100, "5"),
            Pair(-100, "6"),
            Pair(-100, "7")
        )
        val dataAnyKeys = listOf(
            listOf(
                Pair(345, "345"),
                Pair(342, "342"),
                Pair(657, "657"),
                Pair(-33, "gegr"),
                Pair(344, "f4t5"),
                Pair(466, "edvrgbgdsdfvf"),
                Pair(9457, "f5"),
                Pair(72, "72"),
                Pair(500, "45"),
                Pair(112, "0")
            ),
            listOf(
                Pair(-30, "666"),
                Pair(5783, "add"),
                Pair(152, "keys"),
                Pair(894, "to"),
                Pair(300, "tree"),
                Pair(9384, "345"),
                Pair(-435, "666"),
                Pair(-6666, "666"),
                Pair(100, "4"),
                Pair(10000, "wrje")
            ),
            listOf(
                Pair(8, "8"),
                Pair(10, "10"),
                Pair(5, "5"),
                Pair(2, "2"),
                Pair(7, "7"),
                Pair(6, "6"),
                Pair(-1, "-1")
            ),
            listOf(
                Pair(843, "843"),
                Pair(673, "673"),
                Pair(657, "657"),
                Pair(400, "gegr"),
                Pair(344, "f4t5"),
                Pair(466, "edvrgbgdsdfvf"),
                Pair(1234, "1234"),
                Pair(72, "72"),
                Pair(500, "45"),
                Pair(112, "0"),
                Pair(666, "666"),
                Pair(441, "342"),
                Pair(652, "652"),
                Pair(88, "88"),
                Pair(999, "999"),
                Pair(381, "381"),
                Pair(3742, "3742"),
                Pair(1, "1"),
                Pair(34, "34"),
                Pair(8848, "8848"),
                Pair(6744, "6744"),
                Pair(-100, "-100"),
                Pair(-50, "-50"),
                Pair(-23, "gegr"),
                Pair(763, "763"),
                Pair(453, "453"),
                Pair(1034, "1034"),
                Pair(9543, "9543"),
                Pair(-8888, "-8888"),
                Pair(56, "56")
            )
        )

        @JvmStatic
        fun data() = dataAnyKeys.map { Arguments.of(it) }

        @JvmStatic
        fun dataWithSameKeys() = dataAnyKeys
    }

    @BeforeEach
    fun beforeStart() {
        testTree = BSTree()
    }

    @Nested
    inner class SingleThreadTests {
        @Nested
        inner class Insert {

            //            Check if all inserted vertices are really contained in the tree
            @ParameterizedTest
            @MethodSource("TreeTest#data")
            fun `insert elements and check nodes`(input: List<Pair<Int, String>>) {
                input.forEach { testTree.insert(it.first, it.second) }
                input.forEach { assertTrue(Pair(it.first, testTree.get(it.first)) in input) }
            }

            //            Check if the vertices with the same key and different value are entered correctly.
//            My tree remembers the last value entered.
            @Test
            fun `values with same keys`() {
                testTree.insert(10, "1")
                testTree.insert(10, "2")
                testTree.insert(10, "3")
                testTree.insert(10, "4")
                testTree.insert(10, "5")
                assertTrue(testTree.get(10) == "5")
            }
        }

        @Nested
        inner class Remove {

            //            Check if the item was deleted correctly.As soon as an item is deleted, it should disappear
//            from the tree. Also here we check if the number of items in the tree is counted correctly.
            @ParameterizedTest
            @MethodSource("TreeTest#data")
            fun `remove elements and check`(input: List<Pair<Int, String>>) {
                input.forEach { testTree.insert(it.first, it.second) }
                assertTrue(testTree.size.get() == input.size)
                input.forEach {
                    assertTrue(testTree.contains(it.first))
                    assertTrue(testTree.remove(it.first))
                    assertFalse(testTree.contains(it.first))
                }
                assertTrue(testTree.size.get() == 0)
            }

            //            We try to remove the thing from the empty tree. The delete function should return null.
            @Test
            fun `remove from empty tree`() {
                assertFalse(testTree.remove(100))
                assertTrue(testTree.isEmpty())
            }

            //            Check the correctness of the removal. Remove only half of the vertices from the tree,
//            and then check that the deleted vertices have disappeared and the remaining vertices
//            are contained in the tree.
            @Test
            fun `remove half of the vertices`() {
                val l = listOf(
                    Pair(65, "65"),
                    Pair(94, "94"),
                    Pair(78, "78"),
                    Pair(32, "32"),
                    Pair(23, "23"),
                    Pair(48, "48"),
                    Pair(74, "74"),
                    Pair(90, "90"),
                    Pair(33, "33"),
                    Pair(15, "15")
                )
                l.forEach { testTree.insert(it.first, it.second) }
                for (i in 0..4) testTree.remove(l[2 * i].first)
                for (i in 0..4) {
                    assertFalse(testTree.contains(l[i * 2].first))
                    assertTrue(testTree.contains(l[i * 2 + 1].first))
                }
                assertTrue(testTree.size.get() == 5)
            }

        }

        @Nested
        inner class Get {

            //            Check that the search in the empty tree returns null.
            @ParameterizedTest
            @ValueSource(ints = [-1111, 1111, 0, 3728, -4738, 11])
            fun `search in empty tree`(input: Int) {
                assertTrue(testTree.get(input) == null)
            }
        }
    }

    @Nested
    inner class MultiThreadTests {

        //        Check the correctness of the parallel input. We check if the tree contains the elements that we added there.
        @ParameterizedTest
        @MethodSource("TreeTest#data")
        fun `parallel insert elements and check`(input: List<Pair<Int, String>>) {
            input.map {
                executor.submit {
                    testTree.insert(it.first, it.second)
                }
            }.forEach { future ->
                try {
                    future.get()
                } catch (e: Exception) {
                    println("error in thread completion")
                }
            }
            assertTrue(testTree.size.get() == input.size)
            input.forEach { assertTrue(Pair(it.first, testTree.get(it.first)) in input) }
        }

        //        Check the correctness of the parallel deletion. It is necessary that after deletion not a single element remains in the tree.
        @ParameterizedTest
        @MethodSource("TreeTest#data")
        fun `parallel insert and remove elements and check`(input: List<Pair<Int, String>>) {
            input.map {
                executor.submit {
                    testTree.insert(it.first, it.second)
                }
            }.forEach { future ->
                try {
                    future.get()
                } catch (e: Exception) {
                    println("error in thread completion")
                }
            }
            assertTrue(testTree.size.get() == input.size)
            input.map {
                executor.submit {
                    testTree.remove(it.first)
                }
            }.forEach { future ->
                try {
                    future.get()
                } catch (e: Exception) {
                    println("error in thread completion")
                }
            }
            input.forEach {
                assertFalse(testTree.contains(it.first))
            }
            assertTrue(testTree.size.get() == 0)
        }
    }
}

