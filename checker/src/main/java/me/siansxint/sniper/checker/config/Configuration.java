package me.siansxint.sniper.checker.config;

public record Configuration(int poolSize, long rateLimitDelay, long savingInterval, String mongoUri) {}