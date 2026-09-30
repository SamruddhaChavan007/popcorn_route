package com.example.popcornroute.auth.domain.usecase

import com.example.popcornroute.auth.domain.error.AuthError
import com.example.popcornroute.auth.domain.model.AuthResult
import com.example.popcornroute.auth.domain.model.AuthUser
import com.example.popcornroute.auth.domain.model.SessionState
import com.example.popcornroute.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignInUseCaseTest {
    private val repository = RecordingAuthRepository()
    private val useCase = SignInUseCase(repository)

    @Test
    fun blankEmailFailsWithoutCallingRepository() = runTest {
        val result = useCase(email = "   ", password = "password123")

        assertEquals(AuthResult.Failure(AuthError.EmailRequired), result)
        assertTrue(repository.signInCalls.isEmpty())
    }

    @Test
    fun emptyPasswordFailsWithoutCallingRepository() = runTest {
        val result = useCase(email = "alex@example.com", password = "")

        assertEquals(AuthResult.Failure(AuthError.PasswordRequired), result)
        assertTrue(repository.signInCalls.isEmpty())
    }

    @Test
    fun emailIsTrimmedButPasswordIsPassedThroughUntouched() = runTest {
        useCase(email = "  alex@example.com  ", password = " pass word ")

        assertEquals(listOf("alex@example.com" to " pass word "), repository.signInCalls)
    }

    @Test
    fun repositoryResultIsReturnedUnchanged() = runTest {
        repository.result = AuthResult.Failure(AuthError.InvalidCredentials)

        val result = useCase(email = "alex@example.com", password = "wrong")

        assertEquals(AuthResult.Failure(AuthError.InvalidCredentials), result)
    }
}

internal class RecordingAuthRepository : AuthRepository {
    override val sessionState: StateFlow<SessionState> = MutableStateFlow(SessionState.Unauthenticated)

    val signInCalls = mutableListOf<Pair<String, String>>()
    var result: AuthResult = AuthResult.Success(AuthUser(id = "id", name = "Alex", email = "alex@example.com"))

    override suspend fun signUp(name: String, email: String, password: String): AuthResult = result

    override suspend fun signIn(email: String, password: String): AuthResult {
        signInCalls += email to password
        return result
    }
}
