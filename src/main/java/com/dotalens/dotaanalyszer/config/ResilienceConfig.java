package com.dotalens.dotaanalyszer.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;

/**
 * Resilience for the OpenDota external dependency.
 *
 * <p>Composition used at the call site (see OpenDotaService): the CircuitBreaker is the OUTER
 * decorator and Retry the INNER one, i.e. {@code CircuitBreaker(Retry(call))}. That means:
 * <ul>
 *   <li>transient blips (a single 5xx / timeout) are retried with exponential backoff;</li>
 *   <li>the circuit records the final outcome after retries; and</li>
 *   <li>once the circuit is OPEN, calls fail fast without even entering the retry loop.</li>
 * </ul>
 *
 * <p>4xx client errors (e.g. 404 "player not found", 400 bad steamId) are NOT the dependency
 * being down, so they are ignored — they neither trip the breaker nor trigger retries.
 */
@Configuration
public class ResilienceConfig {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);

    @Bean
    public CircuitBreaker openDotaCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)               // consider the last 10 calls
                .minimumNumberOfCalls(5)             // but don't judge until we have at least 5
                .failureRateThreshold(50.0f)         // open when >=50% of them failed
                .slowCallRateThreshold(80.0f)        // also open when >=80% are "slow"
                .slowCallDurationThreshold(Duration.ofSeconds(8))
                .waitDurationInOpenState(Duration.ofSeconds(20)) // stay open 20s, then probe
                .permittedNumberOfCallsInHalfOpenState(3)
                .ignoreExceptions(HttpClientErrorException.class) // 4xx == not a dependency outage
                .build();

        CircuitBreaker cb = CircuitBreaker.of("opendota", config);
        cb.getEventPublisher()
                .onStateTransition(e -> log.warn("[circuit:opendota] {} -> {}",
                        e.getStateTransition().getFromState(), e.getStateTransition().getToState()))
                .onCallNotPermitted(e -> log.warn("[circuit:opendota] call rejected (circuit OPEN)"));
        return cb;
    }

    @Bean
    public Retry openDotaRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)                      // 1 original + 2 retries
                .intervalFunction(IntervalFunction
                        .ofExponentialBackoff(Duration.ofMillis(500), 2.0)) // 500ms, 1s
                .retryExceptions(Exception.class)
                .ignoreExceptions(HttpClientErrorException.class) // don't retry 4xx
                .build();

        Retry retry = Retry.of("opendota", config);
        retry.getEventPublisher()
                .onRetry(e -> log.warn("[retry:opendota] attempt {} after failure: {}",
                        e.getNumberOfRetryAttempts(), e.getLastThrowable() == null
                                ? "n/a" : e.getLastThrowable().getMessage()));
        return retry;
    }
}
