package me.siansxint.sniper.claimer.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.siansxint.sniper.common.Identity;
import net.raphimc.minecraftauth.step.java.StepMCProfile;
import net.raphimc.minecraftauth.step.java.StepMCToken;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;

import java.beans.ConstructorProperties;
import java.util.Objects;

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

    public static Account fromSession(StepFullJavaSession.FullJavaSession session) {
        StepMCProfile.MCProfile profile = session.getMcProfile();
        StepMCToken.MCToken token = session.getMcProfile().getMcToken();
        return new Account(
                profile.getId().toString(),
                profile.getName(),
                token.getAccessToken(),
                token.getXblXsts().getInitialXblSession().getMsaToken().getRefreshToken(),
                token.getExpireTimeMs()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;
        return expireTime == account.expireTime && Objects.equals(id, account.id) && Objects.equals(name, account.name) && Objects.equals(accessToken, account.accessToken) && Objects.equals(refreshToken, account.refreshToken);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(accessToken);
        result = 31 * result + Objects.hashCode(refreshToken);
        result = 31 * result + Long.hashCode(expireTime);
        return result;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", expireTime=" + expireTime +
                '}';
    }
}