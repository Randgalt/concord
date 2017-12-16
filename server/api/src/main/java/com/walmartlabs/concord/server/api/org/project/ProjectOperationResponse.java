package com.walmartlabs.concord.server.api.org.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.server.api.OperationResult;

import java.io.Serializable;
import java.util.UUID;

public class ProjectOperationResponse implements Serializable {

    private final boolean ok = true;
    private final UUID id;
    private final OperationResult result;

    @JsonCreator
    public ProjectOperationResponse(@JsonProperty("id") UUID id,
                                    @JsonProperty("result") OperationResult result) {
        this.id = id;
        this.result = result;
    }

    public boolean isOk() {
        return ok;
    }

    public UUID getId() {
        return id;
    }

    public OperationResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "ProjectOperationResponse{" +
                "ok=" + ok +
                ", id=" + id +
                ", result=" + result +
                '}';
    }
}