package com.avro.test;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonClassDescription("Message for testing")
public class TestMessage {

    @JsonPropertyDescription("Required string field")
    @JsonProperty(required = true)
    private final String field1;

    @JsonPropertyDescription("Nullable long field wit default value of -1")
    @JsonProperty(defaultValue = "-1")
    private Long field2;

    @JsonCreator
    public TestMessage(@JsonProperty("field1") final String field1) {
        this.field1 = field1;
    }

    public Long getField2() {
        return field2;
    }

    public void setField2(final Long field2) {
        this.field2 = field2;
    }

    public String getField1() {
        return field1;
    }

}
