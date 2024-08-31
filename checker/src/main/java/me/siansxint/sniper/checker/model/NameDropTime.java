package me.siansxint.sniper.checker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.siansxint.sniper.common.Identity;

import java.beans.ConstructorProperties;
import java.util.Objects;

public final class NameDropTime implements Identity {

    private final String id;

    private final @JsonProperty("from") long from;
    private final @JsonProperty("to") long to;

    @ConstructorProperties({"_id", "from", "to"})
    public NameDropTime(String id, long from, long to) {
        this.id = id;
        this.from = from;
        this.to = to;
    }

    @Override
    public String id() {
        return id;
    }

    public long from() {
        return from;
    }

    public long to() {
        return to;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (NameDropTime) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.from, that.from) &&
                Objects.equals(this.to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, from, to);
    }

    @Override
    public String toString() {
        return "NameDropTime[" +
                "id=" + id + ", " +
                "from=" + from + ", " +
                "to=" + to + ']';
    }
}