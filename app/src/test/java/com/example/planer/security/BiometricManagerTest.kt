package com.example.planer.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BiometricManagerTest {

    private lateinit var biometricMock: BiometricMock
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        biometricMock = BiometricMock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ТЕСТ 1
    @Test
    fun testCheckAvailabilityReturnsUnavailableOnDeviceWithoutSensor() = runTest {
        val mockWithoutSensor = BiometricMock()
        mockWithoutSensor.setMockSensorAvailable(false)

        val result = mockWithoutSensor.checkAvailability()

        assertEquals(BiometricType.NONE, result)
    }

    // ТЕСТ 2
    @Test
    fun testAuthenticateReturnsSuccess() = runTest {
        biometricMock.setMockShouldSucceed(true)

        var successCalled = false

        biometricMock.authenticate(
            reason = "Test authentication",
            onSuccess = { successCalled = true }
        )

        testDispatcher.scheduler.advanceTimeBy(1500)

        assertTrue("Success callback should be called", successCalled)
    }

    // ТЕСТ 3
    @Test
    fun testAuthenticateReturnsFailed() = runTest {
        biometricMock.setMockShouldSucceed(false)

        var failureCalled = false

        biometricMock.authenticate(
            reason = "Test authentication",
            onFailure = { failureCalled = true }
        )

        testDispatcher.scheduler.advanceTimeBy(1500)

        assertTrue("Failure callback should be called", failureCalled)
    }

    // ТЕСТ 4
    @Test
    fun testIsEnabledByUserReadsSavedValue() = runTest {
        biometricMock.setEnabled(true)
        val enabledValue = biometricMock.isEnabledByUser()
        assertTrue("Enabled should be true", enabledValue)
    }

    // ТЕСТ 5
    @Test
    fun testIsEnabledByUserFalseByDefault() = runTest {
        val enabledValue = biometricMock.isEnabledByUser()
        assertFalse("Enabled should be false by default", enabledValue)
    }

    // ТЕСТ 6
    @Test
    fun testSettingsSaveAndReadWithoutCorruption() = runTest {
        biometricMock.setEnabled(true)
        assertEquals(true, biometricMock.isEnabledByUser())

        biometricMock.setEnabled(false)
        assertEquals(false, biometricMock.isEnabledByUser())
    }

    // ТЕСТ 7
    @Test
    fun testUiStateTransitionsToAuthenticating() = runTest {
        biometricMock.authenticate("Test")
        val currentState = biometricMock.authenticationState.value
        assertTrue("State should be Authenticating", currentState is BiometricAuthState.Authenticating)
    }

    // ТЕСТ 8
    @Test
    fun testUiStateTransitionsToSuccess() = runTest {
        biometricMock.setMockShouldSucceed(true)
        biometricMock.authenticate("Test")
        testDispatcher.scheduler.advanceTimeBy(1500)
        val currentState = biometricMock.authenticationState.value
        assertTrue("State should be Success", currentState is BiometricAuthState.Success)
    }

    // ТЕСТ 9
    @Test
    fun testCheckAvailabilityReturnsFingerprintOnMock() = runTest {
        val result = biometricMock.checkAvailability()
        assertEquals(BiometricType.FINGERPRINT, result)
    }

    // ТЕСТ 10
    @Test
    fun testSetEnabledChangesValue() = runTest {
        biometricMock.setEnabled(true)
        assertTrue(biometricMock.isEnabledByUser())

        biometricMock.setEnabled(false)
        assertFalse(biometricMock.isEnabledByUser())
    }

    // ТЕСТ 11
    @Test
    fun testMockSuccessScenario() = runTest {
        biometricMock.setMockShouldSucceed(true)
        var successCalled = false
        biometricMock.authenticate("Test", onSuccess = { successCalled = true })
        testDispatcher.scheduler.advanceTimeBy(1500)
        assertTrue(successCalled)
    }

    // ТЕСТ 12
    @Test
    fun testMockFailureScenario() = runTest {
        biometricMock.setMockShouldSucceed(false)
        var failureCalled = false
        biometricMock.authenticate("Test", onFailure = { failureCalled = true })
        testDispatcher.scheduler.advanceTimeBy(1500)
        assertTrue(failureCalled)
    }
}