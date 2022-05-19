import com.bankApi.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Callable


@Nested
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class Test_BankAPI {

    @Nested
    @Order(1)
    inner class ConcurrentWork {
        //        This test shows that I use ReentrantReadWriteLock and therefore have no problem adding and removing users
        @RepeatedTest(3)
        fun `concurrent create and delete users`() {
            val numberOfThreads = 8
            val numberOfAccounts = 20
            val executor: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
            (1..numberOfAccounts).map {
                executor.submit(Callable {
                    withTestApplication({ module(testing = true) }) {
                        handleRequest(HttpMethod.Post, "/users") {
                            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            setBody("""{"username":"$it","password":"password$it"}""")
                        }.apply {
                            print("[LOG] $it ${response.content} ${response.status()}\n")
                        }
                    }
                })
            }.forEach { it.get() }

            assertEquals(usersData.first.size, numberOfAccounts)

            (1..numberOfAccounts).map {
                executor.submit(Callable {
                    withTestApplication({ module(testing = true) }) {
                        handleRequest(HttpMethod.Delete, "/users/$it") {
                            addHeader(
                                HttpHeaders.Authorization,
                                "Basic ${Base64.getEncoder().encodeToString("$it:password$it".toByteArray())}"
                            )
                        }.apply {
                            print("[LOG] $it ${response.content} ${response.status()}\n")
                        }
                    }
                })
            }.forEach { it.get() }
            assertTrue(usersData.first.isEmpty())
        }

        //        This test shows that during random parallels the total sum does not change
        @RepeatedTest(10)
        fun `concurrent transfer`() {
            val allMoney = 200000
            val numberOfAccount = 20
            val numberOfThreads = 8
            val numberOfTransfers = 200
            val amountOfTransfer = 2500
            val executor: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
            var sumOfAllUsersBalances = BigDecimal.ZERO

            withTestApplication({ module(testing = true) }) {
                (1..numberOfAccount).forEach {
                    handleRequest(HttpMethod.Post, "/users") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody("""{"username": "$it","password": "password$it"}""")
                    }.apply {
                        print("[LOG] User $it : ${response.status()} ${response.content}\n")
                    }
                    handleRequest(HttpMethod.Put, "/users/$it/cash_in") {
                        addHeader(
                            HttpHeaders.Authorization,
                            "Basic ${Base64.getEncoder().encodeToString("$it:password$it".toByteArray())}"
                        )
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody("""{"amount": "${allMoney / numberOfAccount}"}""")
                    }.apply {
                        print("[LOG] $it: ${response.content}  ${response.status()}\n")
                    }
                }
            }

            (1..numberOfTransfers).map {
                executor.submit(Callable {
                    withTestApplication({ module(testing = true) }) {
                        val random = (1..numberOfAccount).random()
                        val randomTo = (1..numberOfAccount).random()
                        handleRequest(HttpMethod.Post, "/transfer") {
                            addHeader(
                                HttpHeaders.Authorization,
                                "Basic ${Base64.getEncoder().encodeToString("$random:password$random".toByteArray())}"
                            )
                            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            setBody("""{"toUsername":"$randomTo","amount":"$amountOfTransfer"}""")
                        }.apply {
                            print("[LOG] $it ${response.content} ${response.status()}\n")
                        }
                    }
                })
            }.forEach { it.get() }

            withTestApplication({ module(testing = true) }) {
                (1..numberOfAccount).forEach {
                    handleRequest(HttpMethod.Get, "/users/$it") {
                        addHeader(
                            HttpHeaders.Authorization,
                            "Basic ${Base64.getEncoder().encodeToString("$it:password$it".toByteArray())}"
                        )
                    }.apply {
                        print("[LOG] $it: ${response.content} ${response.status()}\n")
                        sumOfAllUsersBalances = sumOfAllUsersBalances.plus(
                            BigDecimal(
                                Json.decodeFromString(
                                    ResponseUserData.serializer(),
                                    response.content.toString()
                                ).balance
                            )
                        )
                    }
                }

                withTestApplication({ module(testing = true) }) {
                    (1..numberOfAccount).forEach {
                        handleRequest(HttpMethod.Delete, "/users/$it") {
                            addHeader(
                                HttpHeaders.Authorization,
                                "Basic ${Base64.getEncoder().encodeToString("$it:password$it".toByteArray())}"
                            )
                        }.apply {
                            print("[LOG] $it: ${response.content}  ${response.status()}\n")
                        }
                    }
                }

                assertEquals(sumOfAllUsersBalances, allMoney.toBigDecimal().setScale(2))

            }
        }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    inner class EndpointsTests {

        @Test
        @Order(1)
        fun `correct create user`() {
            withTestApplication({ module(testing = true) }) {
                handleRequest(HttpMethod.Post, "/users") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"username": "test","password": "testpwd"}""")
                }.apply {
                    assertEquals(HttpStatusCode.Created, response.status())
                    assertEquals("User added correctly", response.content)
                }
            }
        }


        @Test
        @Order(2)
        fun `user already exist`() {
            withTestApplication({ module(testing = true) }) {
                handleRequest(HttpMethod.Post, "/users") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"username": "test","password": "testpwd"}""")
                }.apply {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("User already exists. Delete an existing account first.", response.content)
                }
            }
        }


        @Test
        @Order(3)
        fun `correct cash in and cash out`() {
            withTestApplication({ module(testing = true) }) {
                handleRequest(HttpMethod.Put, "/users/test/cash_in") {
                    addHeader(
                        HttpHeaders.Authorization,
                        "Basic ${Base64.getEncoder().encodeToString("test:testpwd".toByteArray())}"
                    )
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"amount": "100"}""")
                }.apply {
                    assertEquals(HttpStatusCode.Accepted, response.status())
                    assertEquals("Successful cash in", response.content)
                    assertEquals(
                        atomic { return@atomic usersData.first["test"]!!.balance.read().toString().toBigDecimal() },
                        BigDecimal("100.00")
                    )
                }
                handleRequest(HttpMethod.Put, "/users/test/cash_out") {
                    addHeader(
                        HttpHeaders.Authorization,
                        "Basic ${Base64.getEncoder().encodeToString("test:testpwd".toByteArray())}"
                    )
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"amount": "50"}""")
                }.apply {
                    assertEquals(HttpStatusCode.Accepted, response.status())
                    assertEquals("Successful cash out", response.content)
                    assertEquals(atomic {
                        return@atomic usersData.first["test"]!!.balance.read().toString().toBigDecimal()
                    }, BigDecimal("50.00"))
                }
            }

        }


        @Test
        @Order(4)
        fun `cash in cash out not auth`() {
            withTestApplication({ module(testing = true) }) {
                handleRequest(HttpMethod.Post, "/users") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"username": "test","password": "testpwd"}""")
                }
                handleRequest(HttpMethod.Put, "/users/test1/cash_in") {
                    addHeader(
                        HttpHeaders.Authorization,
                        "Basic ${Base64.getEncoder().encodeToString("test:testpwd".toByteArray())}"
                    )
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"amount": "100"}""")
                }.apply {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
                handleRequest(HttpMethod.Put, "/users/test2/cash_out") {
                    addHeader(
                        HttpHeaders.Authorization,
                        "Basic ${Base64.getEncoder().encodeToString("test:testpwd".toByteArray())}"
                    )
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"amount": "100"}""")
                }.apply {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }

        @Test
        @Order(5)
        fun `correct transfer`() {
            withTestApplication({ module(testing = true) }) {
                handleRequest(HttpMethod.Post, "/users") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"username": "test1","password": "testpwd"}""")
                }
                handleRequest(HttpMethod.Post, "/transfer") {
                    addHeader(
                        HttpHeaders.Authorization,
                        "Basic ${Base64.getEncoder().encodeToString("test:testpwd".toByteArray())}"
                    )
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"toUsername":"test1","amount":"30"}""")
                }.apply {
                    assertEquals(HttpStatusCode.Accepted, response.status())
                }
            }
        }

        @Test
        @Order(6)
        fun `incorrect transfer`() {
            withTestApplication({ module(testing = true) }) {
                handleRequest(HttpMethod.Post, "/transfer") {
                    addHeader(
                        HttpHeaders.Authorization,
                        "Basic ${Base64.getEncoder().encodeToString("test:testpwd".toByteArray())}"
                    )
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"toUsername":"test1","amount":"1000000"}""")
                }.apply {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals(response.content, "Insufficient funds in the account: 20.00")
                }
            }
        }
    }
}