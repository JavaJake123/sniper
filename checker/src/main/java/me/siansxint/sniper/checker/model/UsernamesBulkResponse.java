package me.siansxint.sniper.checker.model;

import java.beans.ConstructorProperties;

public record UsernamesBulkResponse(String id, String name) {

    @ConstructorProperties({"id", "name"})
    public UsernamesBulkResponse {
    }
}