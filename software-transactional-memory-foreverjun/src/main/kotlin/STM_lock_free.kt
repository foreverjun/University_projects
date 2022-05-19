import java.util.concurrent.atomic.AtomicReference

enum class TxStatus {ACTIVE, COMMITTED, ABORTED}

class Transaction {
    private val _status = AtomicReference(TxStatus.ACTIVE)
    val status: TxStatus get() = _status.get()

    fun <T> TVar<T>.read(): T =
        readIn(this@Transaction)

    fun <T> TVar<T>.write(x:T): T =
        writeIn(this@Transaction, x)

    internal fun commit() : Boolean{
        return _status.compareAndSet(TxStatus.ACTIVE, TxStatus.COMMITTED)
    }

    internal fun abort(){
        _status.compareAndSet(TxStatus.ACTIVE, TxStatus.ABORTED)
    }
}

private val rootTx = Transaction().apply { commit() }

private class Loc<T>(
    val oldValue: T,
    val newValue: T,
    val owner: Transaction
) {
    fun valueIn(tx: Transaction, onActive: (Transaction) -> Unit): Any? =
        if (owner === tx) newValue
        else when (owner.status) {
            TxStatus.ABORTED ->oldValue
            TxStatus.COMMITTED ->newValue
            TxStatus.ACTIVE ->{
                onActive(owner)
                TxStatus.ACTIVE
            }
        }
}

class TVar<T>(initial: T){
    private val loc = AtomicReference(Loc(initial, initial, rootTx))

    private fun openIn(tx: Transaction, update: (T) -> T): T {
        while (true) {
            val curLoc = loc.get()
            val curValue = curLoc.valueIn(tx) {owner -> owner.abort() }

            if (curValue === TxStatus.ACTIVE) continue
            val updValue = update(curValue as T)

            if (loc.compareAndSet(curLoc, Loc(curValue, updValue, tx))){
                if (tx.status == TxStatus.ABORTED) throw AbortException()
                return updValue
            }
        }
    }

    internal fun readIn(tx: Transaction): T = openIn(tx) { it }

    internal fun writeIn(tx: Transaction, x: T) = openIn(tx) {x}
}

fun <T> atomic(block: Transaction.() -> T): T {
    while (true) {
        val transaction = Transaction()
        try {
            val result = block(transaction)
            if (!transaction.commit()) continue
            return result
        }   catch (e: Exception) {
            transaction.abort()
        }
    }
}

class AbortException : Exception()
