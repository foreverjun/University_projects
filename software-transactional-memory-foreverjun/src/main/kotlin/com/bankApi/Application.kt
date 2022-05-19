package com.bankApi

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val digestFunction = getDigestFunction("SHA-256") { "${it.length}" }

fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        json()
    }
    install(Authentication) {
        basic("auth-basic") {
            realm = "Access to the '/' path"

            validate { credentials ->
                if (usersData.first[credentials.name]?.password.contentEquals(digestFunction(credentials.password))) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
    routing {
        routeWithAuth()
        createUser()
    }
}
