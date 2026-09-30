package com.example.popcornroute.auth.signin.presentation

import com.example.popcornroute.auth.domain.error.AuthError
import com.example.popcornroute.auth.domain.model.AuthResult
import com.example.popcornroute.auth.domain.model.AuthUser
import com.example.popcornroute.auth.domain.model.SessionState
import com.example.popcornroute.auth.domain.repository.AuthRepository
import com.example.popcornroute.auth.domain.usecase.SignInUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeRepository()
    private lateinit var viewModel: SignInViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = SignInViewModel(SignInUseCase(repository))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun typingUpdatesFields() {
        viewModel.onEmailChange("alex@example.com")
        viewModel.onPasswordChange("secret")

        assertEquals("alex@example.com", viewModel.uiState.value.email)
        assertEquals("secret", viewModel.uiState.value.password)
    }

    @Test
    fun invalidCredentialsShowsMessageAndStopsLoading() = runTest(dispatcher) {
        repository.result = AuthResult.Failure(AuthError.InvalidCredentials)
        viewModel.onEmailChange("alex@example.com")
        viewModel.onPasswordChange("wrong")

        viewModel.onSignInClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Incorrect email or password.", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun emptyFieldsShowRequiredMessageWithoutCallingRepository() = runTest(dispatcher) {
        viewModel.onSignInClicked()
        advanceUntilIdle()

        assertEquals("Enter your email.", viewModel.uiState.value.error)
        assertEquals(0, repository.signInCount)
    }

    @Test
    fun successClearsErrorAndStopsLoading() = runTest(dispatcher) {
        viewModel.onEmailChange("alex@example.com")
        viewModel.onPasswordChange("secret")

        viewModel.onSignInClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertFalse(state.isLoading)
        assertEquals(1, repository.signInCount)
    }

    @Test
    fun secondClickWhileLoadingIsIgnored() = runTest(dispatcher) {
        val gate = CompletableDeferred<AuthResult>()
        repository.gate = gate
        viewModel.onEmailChange("alex@example.com")
        viewModel.onPasswordChange("secret")

        viewModel.onSignInClicked()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLoading)
        viewModel.onSignInClicked()
        advanceUntilIdle()

        assertEquals(1, repository.signInCount)
        gate.complete(repository.result)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun unexpectedExceptionShowsGenericMessage() = runTest(dispatcher) {
        repository.failure = IllegalStateException("boom")
        viewModel.onEmailChange("alex@example.com")
        viewModel.onPasswordChange("secret")

        viewModel.onSignInClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Something went wrong. Please try again.", state.error)
        assertFalse(state.isLoading)
    }

    private class FakeRepository : AuthRepository {
        override val sessionState: StateFlow<SessionState> = MutableStateFlow(SessionState.Unauthenticated)

        var result: AuthResult = AuthResult.Success(AuthUser("id", "Alex", "alex@example.com"))
        var gate: CompletableDeferred<AuthResult>? = null
        var failure: Throwable? = null
        var signInCount = 0

        override suspend fun signUp(name: String, email: String, password: String): AuthResult = result

        override suspend fun signIn(email: String, password: String): AuthResult {
            signInCount++
            failure?.let { throw it }
            return gate?.await() ?: result
        }
    }
}
