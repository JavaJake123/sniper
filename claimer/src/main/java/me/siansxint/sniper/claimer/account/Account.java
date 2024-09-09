package me.siansxint.sniper.claimer.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.siansxint.sniper.common.Identity;

import java.beans.ConstructorProperties;

public final class Account implements Identity {

    private final String id;
    private final @JsonProperty("name") String name;

    private final @JsonProperty("accessToken") String accessToken;
    private final @JsonProperty("refreshToken") String refreshToken;

    private final @JsonProperty("expireTime") long expireTime;

    @ConstructorProperties({"_id", "name", "accessToken", "refreshToken", "expireTime"})
    public Account(String id, String name, String accessToken, String refreshToken, long expireTime) {
        this.id = id;
        this.name = name;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expireTime = expireTime;
    }

    @Override
    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String accessToken() {
        return accessToken;
    }

    public String refreshToken() {
        return refreshToken;
    }

    public long expireTime() {
        return expireTime;
    }
}