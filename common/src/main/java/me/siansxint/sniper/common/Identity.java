package me.siansxint.sniper.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface Identity {

    @JsonProperty("_id")
    String id();
}