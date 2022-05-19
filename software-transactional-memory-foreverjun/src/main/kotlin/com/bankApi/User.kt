package com.bankApi

import TVar
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal
import java.util.concurrent.locks.ReentrantReadWriteLock

@Serializable
data class UserAuthData(val username: String, val password: String)

@Serializable
data class CashINorOUTData(@Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal)

@Serializable
data class ResponseUserData(val username: String, val balance: String)

@Serializable
data class TransferData(
    val toUsername: String,
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal
)

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toString())
    }
}

data class User(val username: String, val password: ByteArray, var balance: TVar<BigDecimal>)

val usersData = Pair(mutableMapOf<String, User>(), ReentrantReadWriteLock())