package org.piramalswasthya.sakhi.helpers

import kotlinx.coroutines.delay
import timber.log.Timber
import java.net.SocketTimeoutException

/**
 * Default number of attempts (including the initial call) used when a caller
 * does not supply an explicit value.
 */
const val DEFAULT_MAX_RETRY_ATTEMPTS: Int = 3

/**
 * Default delay before the second attempt, in milliseconds.
 */
const val DEFAULT_INITIAL_RETRY_DELAY_MS: Long = 1_000L

/**
 * Upper bound for the per-attempt delay so a long backoff chain cannot stall
 * a sync for an unbounded period.
 */
const val DEFAULT_MAX_RETRY_DELAY_MS: Long = 30_000L

/**
 * Multiplicative growth factor applied between attempts.
 */
const val DEFAULT_BACKOFF_FACTOR: Double = 2.0

/**
 * Executes [block] with bounded retries and exponential backoff.
 *
 * This replaces the legacy pattern where a repository function caught a
 * transient network exception and recursively re-invoked itself. With sustained
 * connectivity failures the unbounded recursion produced StackOverflowErrors
 * (AMRIT#156); switching to an iterative backoff also keeps the coroutine
 * cancellable while it waits because [delay] is suspending rather than
 * blocking.
 *
 * The default [retryOn] predicate matches only [SocketTimeoutException] to
 * preserve the historical scope of which failures are considered transient.
 * Callers needing a broader policy can pass their own predicate.
 *
 * @param maxAttempts total attempts including the initial call; must be >= 1
 * @param initialDelayMs delay before the second attempt; must be >= 0
 * @param maxDelayMs ceiling applied after each multiplicative step;
 *                   must be >= [initialDelayMs]
 * @param backoffFactor multiplicative growth applied between attempts;
 *                      must be > 0
 * @param retryOn predicate deciding whether a thrown [Throwable] is retryable
 * @param block the suspending body to execute
 * @return the result of the first successful invocation of [block]
 * @throws Throwable the last exception observed after attempts are exhausted,
 *                   or the first non-retryable exception encountered
 */
suspend fun <T> retryWithBackoff(
    maxAttempts: Int = DEFAULT_MAX_RETRY_ATTEMPTS,
    initialDelayMs: Long = DEFAULT_INITIAL_RETRY_DELAY_MS,
    maxDelayMs: Long = DEFAULT_MAX_RETRY_DELAY_MS,
    backoffFactor: Double = DEFAULT_BACKOFF_FACTOR,
    retryOn: (Throwable) -> Boolean = ::isTransientNetworkFailure,
    block: suspend () -> T
): T {
    require(maxAttempts >= 1) { "maxAttempts must be at least 1, was $maxAttempts" }
    require(initialDelayMs >= 0) { "initialDelayMs must be non-negative, was $initialDelayMs" }
    require(maxDelayMs >= initialDelayMs) { "maxDelayMs must be >= initialDelayMs" }
    require(backoffFactor > 0.0) { "backoffFactor must be positive, was $backoffFactor" }

    var currentDelay = initialDelayMs
    repeat(maxAttempts - 1) { attempt ->
        try {
            return block()
        } catch (t: Throwable) {
            if (!retryOn(t)) throw t
            Timber.w(
                t,
                "retryWithBackoff: attempt %d/%d failed; sleeping %dms before retry",
                attempt + 1, maxAttempts, currentDelay
            )
            delay(currentDelay)
            currentDelay = (currentDelay.toDouble() * backoffFactor)
                .toLong()
                .coerceAtMost(maxDelayMs)
        }
    }
    return block()
}

/**
 * Default retry predicate. Matches only [SocketTimeoutException]; aligned with
 * the pre-existing catch sites in repository classes so behaviour for
 * non-timeout failures (auth, parse, business errors) remains identical.
 */
fun isTransientNetworkFailure(t: Throwable): Boolean = t is SocketTimeoutException
