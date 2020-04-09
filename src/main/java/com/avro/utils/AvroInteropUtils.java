package com.avro.utils;

import java.io.IOException;

import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;

/**
 * Testing Utility functions related to Apache Avro
 *
 *
 */
public class AvroInteropUtils {

    private final AvroMapper mapper;

    /**
     * Constructor for Avro Test Utils
     *
     * @param mapper {@link AvroMapper} instance to use for decoding the messages
     */
    public AvroInteropUtils(final AvroMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Decodes the given payload as an instance of the given clazz
     *
     * @param payload the payload to decode
     * @param clazz the java type/class the message should be decoded as
     * @return decoded payload
     * @throws IOException is thrown when there is an error in decoding payload
     */
    public <T> T decode(final byte[] payload, final Class<T> clazz) throws IOException {
        final AvroSchema schema = mapper.schemaFor(clazz);
        return mapper.readerFor(clazz).with(schema).readValue(payload);
    }

    /**
     * Decodes the given payload using provided avro schema as an instance of the given clazz
     *
     * @param payload the payload to decode
     * @param schema avro schmea
     * @param clazz the java type/class the message should be decoded as
     * @return decoded payload
     * @throws IOException is thrown when there is an error in decoding payload
     */
    public <T> T decode(final byte[] payload, final String schema, final Class<T> clazz)
            throws IOException {
        final AvroSchema avroschema = mapper.schemaFrom(schema);
        return mapper.readerFor(clazz).with(avroschema).readValue(payload);
    }
}
