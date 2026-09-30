package com.example.popcornroute.auth.data.repository

import android.app.Application
import android.os.Looper
import com.example.popcornroute.auth.domain.error.AuthError
import com.example.popcornroute.auth.domain.model.AuthResult
import com.example.popcornroute.auth.domain.model.SessionState
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Base64
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/** Real Firebase SDK requests against a local HTTP server; no production credentials or services. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class FirebaseAuthRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var app: FirebaseApp
    private lateinit var auth: FirebaseAuth
    private lateinit var repository: FirebaseAuthRepository
    private val dispatcher = StandardTestDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val releaseResponse = CountDownLatch(1)

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        app = FirebaseApp.initializeApp(
            RuntimeEnvironment.getApplication(),
            FirebaseOptions.Builder()
                .setApplicationId("1:123456789:android:test")
                // Firebase caches emulator endpoints by API key across test instances.
                .setApiKey("fake-api-key-${UUID.randomUUID()}")
                .setProjectId("demo-popcornroute")
                .build(),
            "repository-test-${UUID.randomUUID()}",
        )
        auth = FirebaseAuth.getInstance(app)
        auth.useEmulator("127.0.0.1", server.port)
        repository = FirebaseAuthRepository(auth)
        pumpUntil { repository.sessionState.value == SessionState.Unauthenticated }
    }

    @After
    fun tearDown() {
        releaseResponse.countDown()
        scope.cancel()
        dispatcher.scheduler.runCurrent()
        if (::auth.isInitialized) auth.signOut()
        shadowOf(Looper.getMainLooper()).idle()
        if (::app.isInitialized) app.delete()
        if (::server.isInitialized) server.shutdown()
    }

    @Test
    fun signedOutSessionIsUnauthenticated() {
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun successWaitsForProfileAndReturnsSavedName() {
        val profileRequested = CountDownLatch(1)
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path.orEmpty().contains("signupNewUser") -> accountResponse()
                request.path.orEmpty().contains("getAccountInfo") -> lookupResponse()
                request.path.orEmpty().contains("setAccountInfo") -> {
                    profileRequested.countDown()
                    check(releaseResponse.await(10, TimeUnit.SECONDS)) { "Profile response was not released" }
                    profileResponse()
                }
                else -> MockResponse().setResponseCode(404)
            }
        }
        val states = mutableListOf<SessionState>()
        scope.launch(UnconfinedTestDispatcher(dispatcher.scheduler)) {
            repository.sessionState.collect { states.add(it) }
        }
        val result = signUp()
        pumpUntil { profileRequested.count == 0L }
        // The SDK is already signed in, but the repository must not expose that yet.
        assertNotNull(auth.currentUser)
        assertNull(auth.currentUser!!.displayName)
        assertFalse(result.isCompleted)
        assertEquals(listOf(SessionState.Unauthenticated), states)

        releaseResponse.countDown()
        val success = finish(result) as AuthResult.Success
        assertEquals("test-user", success.user.id)
        assertEquals("Alex", success.user.name)
        assertEquals("alex@example.com", success.user.email)
        assertFalse(success.profileUpdateFailed)
        assertEquals(listOf(SessionState.Unauthenticated, SessionState.Authenticated), states)

        val createRequest = request()
        assertTrue(createRequest.path!!.contains("signupNewUser"))
        val credentials = JSONObject(createRequest.body.readUtf8())
        assertEquals("alex@example.com", credentials.getString("email"))
        assertEquals("password123", credentials.getString("password"))
        assertTrue(request().path!!.contains("getAccountInfo"))
        // The SDK also looks up the account before applying a profile update.
        assertTrue(request().path!!.contains("getAccountInfo"))
        val updateRequest = request()
        assertTrue(updateRequest.path!!.contains("setAccountInfo"))
        assertEquals("Alex", JSONObject(updateRequest.body.readUtf8()).getString("displayName"))
    }

    @Test
    fun profileFailureReturnsSuccessWithWarningAndKeepsSession() {
        server.enqueue(accountResponse())
        server.enqueue(lookupResponse())
        server.enqueue(lookupResponse())
        server.enqueue(errorResponse("INTERNAL_ERROR", 500))
        val result = finish(signUp()) as AuthResult.Success
        assertTrue(result.profileUpdateFailed)
        assertNull(result.user.name)
        assertEquals("test-user", result.user.id)
        assertEquals("test-user", auth.currentUser!!.uid)
        assertEquals(SessionState.Authenticated, repository.sessionState.value)
        assertEquals(4, server.requestCount)
        repeat(3) { request() }
        assertTrue(request().path!!.contains("setAccountInfo"))
    }

    @Test
    fun existingEmailFailsWithoutProfileRequest() {
        assertAccountFailure("EMAIL_EXISTS", AuthError.EmailAlreadyInUse)
    }

    @Test
    fun invalidEmailFailsWithoutProfileRequest() {
        assertAccountFailure("INVALID_EMAIL", AuthError.InvalidEmail)
    }

    @Test
    fun tooManyRequestsFailsWithoutProfileRequest() {
        assertAccountFailure("TOO_MANY_ATTEMPTS_TRY_LATER", AuthError.TooManyRequests)
    }

    @Test
    fun unexpectedServerErrorMapsToUnknown() {
        assertAccountFailure("SOMETHING_UNEXPECTED", AuthError.Unknown)
    }

    @Test
    fun weakPasswordMapsToWeakPassword() {
        server.enqueue(errorResponse("WEAK_PASSWORD : Password should be at least 6 characters"))
        val failure = finish(signUp()) as AuthResult.Failure
        assertTrue(failure.error is AuthError.WeakPassword)
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun newRepositoryRecognizesExistingSessionAndSignOut() {
        server.enqueue(accountResponse())
        server.enqueue(lookupResponse())
        server.enqueue(lookupResponse())
        server.enqueue(profileResponse())
        finish(signUp())
        val restoredRepository = FirebaseAuthRepository(auth)
        pumpUntil { restoredRepository.sessionState.value == SessionState.Authenticated }
        auth.signOut()
        pumpUntil { restoredRepository.sessionState.value == SessionState.Unauthenticated }
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
    }

    @Test
    fun cancellingProfileWaitPropagatesCancellationAndKeepsCreatedAccount() {
        val profileRequested = CountDownLatch(1)
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (request.path.orEmpty().contains("signupNewUser")) return accountResponse()
                if (request.path.orEmpty().contains("getAccountInfo")) return lookupResponse()
                profileRequested.countDown()
                check(releaseResponse.await(10, TimeUnit.SECONDS))
                return profileResponse()
            }
        }
        var returnedResult: AuthResult? = null
        val job = scope.async {
            repository.signUp("Alex", "alex@example.com", "password123")
                .also { returnedResult = it }
        }
        pumpUntil { profileRequested.count == 0L }
        job.cancel()
        pumpUntil { job.isCompleted }
        assertTrue(job.isCancelled)
        assertNull(returnedResult)
        assertNotNull(auth.currentUser)
        // Cancellation releases suppression; it does not promise profile completion.
        assertEquals(SessionState.Authenticated, repository.sessionState.value)
        releaseResponse.countDown()
        pumpUntil { auth.currentUser?.displayName == "Alex" }
    }

    @Test
    fun connectionFailureReturnsFailureAndKeepsSessionSignedOut() {
        server.shutdown()
        val result = finish(signUp())
        // Firebase can wrap a refused connection as an internal error (Unknown),
        // rather than FirebaseNetworkException. Do not assume a platform-specific mapping.
        assertTrue("Expected failure for a closed endpoint, got $result", result is AuthResult.Failure)
        assertNull(auth.currentUser)
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
    }

    @Test
    fun cancellingAccountCreationDoesNotReturnFailureButSdkMayStillSignIn() {
        val createRequested = CountDownLatch(1)
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path.orEmpty().contains("signupNewUser") -> {
                    createRequested.countDown()
                    check(releaseResponse.await(10, TimeUnit.SECONDS))
                    accountResponse()
                }
                request.path.orEmpty().contains("getAccountInfo") -> lookupResponse()
                else -> MockResponse().setResponseCode(404)
            }
        }
        var returnedResult: AuthResult? = null
        val job = scope.async {
            repository.signUp("Alex", "alex@example.com", "password123")
                .also { returnedResult = it }
        }
        pumpUntil { createRequested.count == 0L }
        job.cancel()
        pumpUntil { job.isCompleted }
        assertTrue(job.isCancelled)
        assertNull(returnedResult)
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)

        // Cancelling await() does not cancel the Firebase network operation.
        releaseResponse.countDown()
        pumpUntil { repository.sessionState.value == SessionState.Authenticated }
        assertNotNull(auth.currentUser)
        assertNull(auth.currentUser!!.displayName)
        assertEquals(2, server.requestCount) // Creation + lookup; no profile update.
    }

    /** Characterizes a known limitation, not the desired duplicate-submission behavior. */
    @Test
    fun overlappingFailureCanPublishAuthenticatedWhileFirstProfileIsPending() {
        val createCount = AtomicInteger()
        val profileRequested = CountDownLatch(1)
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path.orEmpty().contains("signupNewUser") -> {
                    if (createCount.incrementAndGet() == 1) accountResponse()
                    else errorResponse("EMAIL_EXISTS")
                }
                request.path.orEmpty().contains("getAccountInfo") -> lookupResponse()
                request.path.orEmpty().contains("setAccountInfo") -> {
                    profileRequested.countDown()
                    check(releaseResponse.await(10, TimeUnit.SECONDS))
                    profileResponse()
                }
                else -> MockResponse().setResponseCode(404)
            }
        }
        val first = signUp()
        pumpUntil { profileRequested.count == 0L }
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
        assertEquals(AuthResult.Failure(AuthError.EmailAlreadyInUse), finish(signUp()))

        // The second call's finally clears the shared flag before the first finishes.
        // A ViewModel submission guard is required with the current repository contract.
        assertFalse(first.isCompleted)
        assertNull(auth.currentUser!!.displayName)
        assertEquals(SessionState.Authenticated, repository.sessionState.value)
        releaseResponse.countDown()
        assertFalse((finish(first) as AuthResult.Success).profileUpdateFailed)
    }

    @Test
    fun signInSuccessReturnsUserAndPublishesAuthenticated() {
        server.enqueue(accountResponse())
        server.enqueue(lookupResponse())

        val success = finish(signIn()) as AuthResult.Success

        assertEquals("test-user", success.user.id)
        assertEquals("alex@example.com", success.user.email)
        assertFalse(success.profileUpdateFailed)
        assertEquals(SessionState.Authenticated, repository.sessionState.value)
        val credentials = JSONObject(request().body.readUtf8())
        assertEquals("alex@example.com", credentials.getString("email"))
        assertEquals("password123", credentials.getString("password"))
    }

    @Test
    fun signInWithWrongPasswordFailsWithInvalidCredentials() {
        assertSignInFailure("INVALID_LOGIN_CREDENTIALS", AuthError.InvalidCredentials)
    }

    @Test
    fun signInWithLegacyWrongPasswordCodeFailsWithInvalidCredentials() {
        assertSignInFailure("INVALID_PASSWORD", AuthError.InvalidCredentials)
    }

    @Test
    fun signInWithUnknownEmailFailsWithInvalidCredentials() {
        assertSignInFailure("EMAIL_NOT_FOUND", AuthError.InvalidCredentials)
    }

    @Test
    fun signInWithMalformedEmailFailsWithInvalidEmail() {
        assertSignInFailure("INVALID_EMAIL", AuthError.InvalidEmail)
    }

    @Test
    fun signInWithDisabledAccountFailsWithAccountDisabled() {
        assertSignInFailure("USER_DISABLED", AuthError.AccountDisabled)
    }

    @Test
    fun signInRateLimitedFailsWithTooManyRequests() {
        assertSignInFailure("TOO_MANY_ATTEMPTS_TRY_LATER", AuthError.TooManyRequests)
    }

    @Test
    fun signInUnexpectedServerErrorMapsToUnknown() {
        assertSignInFailure("SOMETHING_UNEXPECTED", AuthError.Unknown)
    }

    @Test
    fun signInConnectionFailureReturnsFailureAndStaysSignedOut() {
        server.shutdown()

        val result = finish(signIn())

        assertTrue("Expected failure for a closed endpoint, got $result", result is AuthResult.Failure)
        assertNull(auth.currentUser)
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
    }

    private fun assertSignInFailure(message: String, expected: AuthError) {
        server.enqueue(errorResponse(message))
        assertEquals(AuthResult.Failure(expected), finish(signIn()))
        assertNull(auth.currentUser)
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
        assertEquals(1, server.requestCount)
    }

    private fun signIn() = scope.async {
        repository.signIn("alex@example.com", "password123")
    }

    private fun assertAccountFailure(message: String, expected: AuthError) {
        server.enqueue(errorResponse(message))
        assertEquals(AuthResult.Failure(expected), finish(signUp()))
        assertNull(auth.currentUser)
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
        assertEquals(1, server.requestCount)
    }

    private fun signUp() = scope.async {
        repository.signUp("Alex", "alex@example.com", "password123")
    }

    private fun finish(result: Deferred<AuthResult>): AuthResult {
        pumpUntil { result.isCompleted }
        return result.getCompleted()
    }

    // Drain Android callbacks and coroutine continuations while real loopback HTTP runs.
    // Bounded polling avoids timing-based assumptions about request completion.
    private fun pumpUntil(condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15)
        do {
            shadowOf(Looper.getMainLooper()).idle()
            dispatcher.scheduler.runCurrent()
            if (condition()) return
            Thread.sleep(10)
        } while (System.nanoTime() < deadline)
        fail("Timed out waiting for Firebase callback; requests=${server.requestCount}")
    }

    private fun request() = checkNotNull(server.takeRequest(2, TimeUnit.SECONDS))

    private fun accountResponse(): MockResponse = jsonResponse(
        JSONObject()
            .put("localId", "test-user")
            .put("email", "alex@example.com")
            .put("idToken", token())
            .put("refreshToken", "fake-refresh-token")
            .put("expiresIn", "3600")
            .toString(),
    )

    private fun profileResponse() = jsonResponse(
        """{"localId":"test-user","email":"alex@example.com","displayName":"Alex"}""",
    )

    private fun lookupResponse() = jsonResponse(
        """{"users":[{"localId":"test-user","email":"alex@example.com","emailVerified":false,"providerUserInfo":[{"providerId":"password","email":"alex@example.com","rawId":"alex@example.com"}]}]}""",
    )

    private fun errorResponse(message: String, code: Int = 400) = jsonResponse(
        JSONObject().put("error", JSONObject().put("code", code).put("message", message)).toString(),
    ).setResponseCode(code)

    private fun jsonResponse(body: String) = MockResponse()
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    private fun token(): String {
        val now = System.currentTimeMillis() / 1000
        fun encode(value: String) = Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray())
        val payload = """{"sub":"test-user","iat":$now,"exp":${now + 3600},"auth_time":$now,"firebase":{"sign_in_provider":"password"}}"""
        return "${encode("""{"alg":"none"}""")}.${encode(payload)}."
    }
}
