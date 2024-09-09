package me.siansxint.sniper.claimer.config;

public record Configuration(int poolSize, int maxRetries, long retryDelay, String mongoUri) { }