package com.bankApi

import TVar
import atomic
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.math.BigDecimal
import java.math.RoundingMode

fun Route.routeWithAuth() {

    authenticate("auth-basic") {
        get("/users/{username}") {
            val username = call.parameters["username"] ?: return@get call.respondText(
                "Missing or malformed username",
                status = HttpStatusCode.BadRequest
            )
            if (username != call.principal<UserIdPrincipal>()?.name) return@get call.respondText(
                "User name does not match the name of the authenticated user",
                status = HttpStatusCode.Unauthorized
            )
            usersData.second.readLock().lock()
            val user = usersData.first[username]
            if (user == null) {
                usersData.second.readLock().unlock()
                return@get call.respondText(
                    "Missing or malformed username",
                    status = HttpStatusCode.BadRequest
                )
            }
            val balance = atomic {
                return@atomic user.balance.read()
            }
            usersData.second.readLock().unlock()
            call.respond(ResponseUserData(user.username, balance.toString()))
        }
        post("/transfer") {
            val transferData = call.receive<TransferData>()
            val fromUsername = call.principal<UserIdPrincipal>()?.name ?: return@post call.respondText(
                "Failed to create translation, error getting user name",
                status = HttpStatusCode.BadRequest
            )
            if (transferData.toUsername == fromUsername) return@post call.respondText(
                "You cannot transfer money to yourself",
                status = HttpStatusCode.BadRequest
            )

            usersData.second.readLock().lock()
            val response = atomic {
                if (usersData.first.containsKey(fromUsername) && usersData.first.containsKey(transferData.toUsername)) {
                    val senderBalanceBuf = usersData.first[fromUsername]!!.balance.read()
                    val reciverBalanceBuf = usersData.first[transferData.toUsername]!!.balance.read()
                    if (senderBalanceBuf < transferData.amount) {
                        return@atomic Pair(
                            "Insufficient funds in the account: $senderBalanceBuf",
                            HttpStatusCode.BadRequest
                        )
                    }
                    usersData.first[transferData.toUsername]!!.balance.write(reciverBalanceBuf.plus(transferData.amount.setScale(2, RoundingMode.DOWN)))
                    usersData.first[fromUsername]!!.balance.write(senderBalanceBuf.minus(transferData.amount.setScale(2, RoundingMode.DOWN)))
                    return@atomic Pair("Successful transfer", HttpStatusCode.Accepted)
                } else {
                    return@atomic Pair("No sender or recipient account was found", HttpStatusCode.NotFound)
                }
            }
            usersData.second.readLock().unlock()
            call.respondText(response.first, status = response.second)
        }
        delete("/users/{username}") {
            val username = call.parameters["username"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (username != call.principal<UserIdPrincipal>()?.name) return@delete call.respondText(
                "User name does not match the name of the authenticated user",
                status = HttpStatusCode.Unauthorized
            )
            usersData.second.writeLock().lock()
            if (usersData.first.remove(username) != null) {
                call.respondText("Customer removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
            usersData.second.writeLock().unlock()
        }

        put("/users/{username}/cash_in") {
            val username = call.parameters["username"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            if (username != call.principal<UserIdPrincipal>()?.name) return@put call.respondText(
                "Username does not match the name of the authenticated user",
                status = HttpStatusCode.Unauthorized
            )
            val amount = call.receive<CashINorOUTData>().amount.setScale(2, RoundingMode.DOWN)
            usersData.second.readLock().lock()
            val response = atomic {
                if (usersData.first.containsKey(username)) {
                    val balanceBuf = usersData.first[username]!!.balance.read()
                    usersData.first[username]!!.balance.write(balanceBuf.plus(amount))
                    return@atomic Pair("Successful cash in", HttpStatusCode.Accepted)
                } else return@atomic Pair("User not found", HttpStatusCode.NotFound)
            }
            usersData.second.readLock().unlock()
            call.respondText(response.first, status = response.second)
        }
        put("/users/{username}/cash_out") {
            val username = call.parameters["username"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            if (username != call.principal<UserIdPrincipal>()?.name) return@put call.respondText(
                "User name does not match the name of the authenticated user",
                status = HttpStatusCode.Unauthorized
            )
            val amount = call.receive<CashINorOUTData>().amount.setScale(2, RoundingMode.DOWN)
            usersData.second.readLock().lock()
            val response = atomic {
                if (usersData.first.containsKey(username)) {
                    val balanceBuf = usersData.first[username]!!.balance.read()
                    if (balanceBuf < amount) {
                        return@atomic Pair("Insufficient funds in the account: $balanceBuf", HttpStatusCode.BadRequest)
                    }
                    usersData.first[username]!!.balance.write(balanceBuf.minus(amount))
                    return@atomic Pair("Successful cash out", HttpStatusCode.Accepted)
                } else return@atomic Pair("User not found", HttpStatusCode.NotFound)
            }
            usersData.second.readLock().unlock()
            call.respondText(response.first, status = response.second)
        }
    }
}

fun Route.createUser() {
    post("/users") {
        val user = call.receive<UserAuthData>()
        usersData.second.writeLock().lock()
        if (usersData.first.containsKey(user.username)) {
            call.respondText(
                "User already exists. Delete an existing account first.",
                status = HttpStatusCode.BadRequest
            )
        } else {
            usersData.first[user.username] =
                User(user.username, digestFunction(user.password), TVar(BigDecimal.ZERO))
            call.respondText("User added correctly", status = HttpStatusCode.Created)
        }
        usersData.second.writeLock().unlock()
    }
}