package me.siansxint.sniper.checker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.siansxint.sniper.common.Identity;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.Objects;

public final class LastCheck implements Identity {

    private final String id;
    private final @JsonProperty("when") Instant when;

    @ConstructorProperties({"_id", "when"})
    public LastCheck(String id, Instant when) {
        this.id = id;
        this.when = when;
    }

    public Instant when() {
        return when;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LastCheck) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.when, that.when);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, when);
    }

    @Override
    public String toString() {
        return "LastCheck[" +
                "id=" + id + ", " +
                "when=" + when + ']';
    }
}