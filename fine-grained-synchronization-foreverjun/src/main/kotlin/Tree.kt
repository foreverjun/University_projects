import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class BSTree<K : Comparable<K>, V> {
    class Node<K, V>(val key: K, var value: V) {
        var left: Node<K, V>? = null
        var right: Node<K, V>? = null
        val lock = ReentrantLock()
    }

    var size = AtomicInteger()
    private var root: Node<K, V>? = null
    private val treeLock = ReentrantLock() // general mutex for tree

    fun insert(key: K, value: V): Boolean {
        treeLock.lock()
        if (root == null) {
            root = Node(key, value)
            size.incrementAndGet()
            treeLock.unlock()
            return true
        }
        root!!.lock.lock()
        treeLock.unlock()
        var current = root!!
        var next: Node<K, V>? = null

        while (true) {
            when {  //  Find a place to insert a new node
                key < current.key -> {
                    if (current.left == null) {
                        current.left = Node(key, value)
                        size.incrementAndGet()
                        current.lock.unlock()
                        return true
                    } else {
                        next = current.left
                    }
                }

                key > current.key -> {
                    if (current.right == null) {
                        current.right = Node(key, value)
                        size.incrementAndGet()
                        current.lock.unlock()
                        return true
                    } else {
                        next = current.right
                    }
                }

                key == current.key -> {
                    current.value = value
                    current.lock.unlock()
                    return true
                }
            }
            next!!.lock.lock() // grab the node and release it on the next iteration
            current.lock.unlock()
            current = next
        }
    }

    fun remove(key: K): Boolean {
        treeLock.lock()
        if (root == null) {
            treeLock.unlock()
            return false
        }

        root!!.lock.lock()

        //Immediately check if the key of the node to be deleted is equal to the key of the root. If we need it, we do it by the following algorithm:
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (key == root!!.key) {
            when {
                    //  Cases where the crown has no more than one child
                (root!!.right == null && root!!.left == null) -> {
                    root = null
                }

                (root!!.right != null && root!!.left == null) -> {
                    root = root!!.right
                }

                (root!!.right == null && root!!.left != null) -> {
                    root = root!!.left
                }
                    // A case where the root has two children
                else -> {
                    val max = maxForRemove(root!!.left!!)
                    if (max == root!!.left) {
                        max.right = root!!.right
                    } else {
                        max.right = root!!.right
                        max.left = root!!.left
                    }
                    root!!.lock.unlock()
                    root = max
                    max.lock.unlock()
                }
            }
            size.decrementAndGet()
            treeLock.unlock()
            return true
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        treeLock.unlock()

        var parent: Node<K, V>? = null
        var next: Node<K, V>? = null
        var current = root!!
        // Look for the node that needs to be removed.
        while (true) {
            when {

                key > current.key -> {
                    if (current.right == null) {
                        current.lock.unlock()
                        parent?.lock?.unlock()
                        return false
                    } else {
                        next = current.right
                    }
                }

                key < current.key -> {
                    if (current.left == null) {
                        current.lock.unlock()
                        parent?.lock?.unlock()
                        return false
                    } else {
                        next = current.left
                    }

                }

                // When we have found a vertex to remove, we have several options
                key == current.key -> {
                    when {
                            // cases where the node does not have more than one child
                        (current.left == null && current.right == null) -> {
                            if (parent!!.right == current)
                                parent.right = null
                            else
                                parent.left = null
                            size.decrementAndGet()
                            parent.lock.unlock()
                            current.lock.unlock()
                            return true
                        }

                        (current.left == null && current.right != null) -> {
                            if (parent!!.right == current)
                                parent.right = current.right
                            else
                                parent.left = current.right
                            size.decrementAndGet()
                            parent.lock.unlock()
                            current.lock.unlock()
                            return true
                        }

                        (current.right == null && current.left != null) -> {
                            if (parent!!.right == current)
                                parent.right = current.left
                            else
                                parent.left = current.left
                            size.decrementAndGet()
                            parent.lock.unlock()
                            current.lock.unlock()
                            return true
                        }
                            // Node has two children
                        else -> {
                            val max = maxForRemove(current.left!!) //Find the maximal node in the left subtree
                            if (max == current.left) {
                                max.right = current.right
                            } else {
                                max.right = current.right
                                max.left = current.left
                            }
                            if (parent!!.right == current)
                                parent.right = max
                            else
                                parent.left = max
                            size.decrementAndGet()
                            parent.lock.unlock()
                            current.lock.unlock()
                            max.lock.unlock()
                            return true
                        }

                    }
                }
            }
            next!!.lock.lock()  //grab the node and release it on the next iteration
            parent?.lock?.unlock()
            parent = current
            current = next
        }
    }

    private fun maxForRemove(node: Node<K, V>): Node<K, V> {
        node.lock.lock()
        if (node.right == null)
            return node
        var current = node
        lateinit var parent: Node<K, V>
        var next: Node<K, V>?
        while (current.right != null) {
            parent = current
            next = current.right
            next!!.lock.lock()
            current.lock.unlock()
            current = next
        }
        parent.right = current.left
        return current
    }

    fun get(key: K): V? {
        treeLock.lock()
        if (root == null) {
            treeLock.unlock()
            return null
        }
        root!!.lock.lock()
        treeLock.unlock()
        var current = root!!
        var next: Node<K, V>? = null

        while (true) {
            when {
                key < current.key -> {
                    if (current.left == null) {
                        current.lock.unlock()
                        return null
                    } else {
                        next = current.left
                    }
                }

                key > current.key -> {
                    if (current.right == null) {
                        current.lock.unlock()
                        return null
                    } else {
                        next = current.right
                    }
                }

                key == current.key -> {
                    current.lock.unlock()
                    return current.value
                }
            }
            next!!.lock.lock() // grab the node and release it on the next iteration
            current.lock.unlock()
            current = next
        }
    }

    fun isEmpty(): Boolean {
        treeLock.lock()
        return if (root == null) {
            treeLock.unlock()
            true
        } else {
            treeLock.unlock()
            false
        }
    }

    fun contains(key: K): Boolean = get(key) != null

}