package me.siansxint.sniper.checker.config;

public record Configuration(int poolSize, int maxRetries, long retryDelay,
                            long rateLimitedDelay, long savingInterval, String mongoUri) {
}