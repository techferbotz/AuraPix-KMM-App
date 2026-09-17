package com.ferbotz.aurapix

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

/**
 * `runTest` for a body that drives the real HTTP client over a `MockEngine`.
 *
 * Plain `runTest` runs on a virtual clock that skips delays, which makes Ktor's `HttpTimeout`
 * plugin fire its 30s killer the instant a request is awaited — every call fails with
 * `HttpRequestTimeoutException` before the engine can answer. Running the body on a real
 * dispatcher keeps the timeout measuring real time, which is what these tests want.
 */
fun runHttpTest(block: suspend CoroutineScope.() -> Unit): TestResult = runTest {
    withContext(Dispatchers.Default, block)
}
