package com.kitappla.app.ui.screens.admin

import com.kitappla.app.data.api.AdminApi
import com.kitappla.app.data.api.MessageApi
import com.kitappla.app.data.repo.AdminRepository
import com.kitappla.app.data.repo.MessageRepository
import com.kitappla.app.data.repo.testRetrofit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminUyelerViewModelTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        server = MockWebServer().also { it.start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
        Dispatchers.resetMain()
    }

    private fun usersListJson() =
        MockResponse().setBody("""[{"id":2,"name":"Ayşe Kaya","email":"ayse@deu.edu.tr","admin":false}]""")

    private fun conversationJson(id: Long = 10) =
        MockResponse().setBody("""{"id":$id,"kind":"SUPPORT","refId":2,"title":"Ayşe Kaya"}""")

    private fun chatMessageJson(id: Long = 100) =
        MockResponse().setBody("""{"id":$id,"body":"Merhaba","mine":true}""")

    private fun newVm(): AdminUyelerViewModel {
        val retrofit = testRetrofit(server)
        val adminApi = retrofit.create(AdminApi::class.java)
        val messageApi = retrofit.create(MessageApi::class.java)
        return AdminUyelerViewModel(
            adminRepository = AdminRepository(adminApi),
            messageRepository = MessageRepository(messageApi),
        )
    }

    private fun awaitState(vm: AdminUyelerViewModel, predicate: (AdminUyelerUiState) -> Boolean) =
        runBlocking { withTimeout(5_000) { vm.state.first(predicate) } }

    @Test
    fun loadsUsersOnInitialization() {
        server.enqueue(usersListJson())
        val vm = newVm()
        val s = awaitState(vm) { it.users.isNotEmpty() }

        assertEquals(1, s.users.size)
        assertEquals("Ayşe Kaya", s.users.first().name)
        assertEquals("GET /api/v1/admin/users", server.takeRequest().let { "${it.method} ${it.path}" })
    }

    @Test
    fun sendMessageToUserOpensConversationAndSendsMessage() {
        server.enqueue(usersListJson())
        val vm = newVm()
        awaitState(vm) { it.users.isNotEmpty() }

        // Enqueue openConversation response and sendMessage response
        server.enqueue(conversationJson(10))
        server.enqueue(chatMessageJson(100))

        var callbackCalled = false
        vm.sendMessageToUser(2L, "Merhaba Ayşe") {
            callbackCalled = true
        }

        val s = awaitState(vm) { it.actionMessage != null }
        assertEquals("Mesaj başarıyla gönderildi.", s.actionMessage)
        assertEquals(true, callbackCalled)

        // Clear initial users request
        server.takeRequest()
        val req1 = server.takeRequest()
        assertEquals("GET /api/v1/conversations/open/SUPPORT/2", "${req1.method} ${req1.path}")
        val req2 = server.takeRequest()
        assertEquals("POST /api/v1/conversations/10/messages", "${req2.method} ${req2.path}")
    }

    @Test
    fun openConversationWithUserNavigatesToChat() {
        server.enqueue(usersListJson())
        val vm = newVm()
        awaitState(vm) { it.users.isNotEmpty() }

        server.enqueue(conversationJson(42))

        var openedConvId: Long? = null
        vm.openConversationWithUser(2L) { convId ->
            openedConvId = convId
        }

        awaitState(vm) { !it.isActionLoading }
        assertEquals(42L, openedConvId)

        server.takeRequest() // initial users request
        val req1 = server.takeRequest()
        assertEquals("GET /api/v1/conversations/open/SUPPORT/2", "${req1.method} ${req1.path}")
    }
}
