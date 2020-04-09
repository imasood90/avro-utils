package com.avro.spring;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cloud.stream.schema.SchemaNotFoundException;
import org.springframework.cloud.stream.schema.SchemaReference;
import org.springframework.cloud.stream.schema.client.ConfluentSchemaRegistryClient;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.MimeType;

import com.avro.test.AbstractIntegrationTest;
import com.avro.test.TestMessage;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.google.common.collect.Maps;

public class AvroJsonSchemaRegistryClientMessageConverterTest
        extends AbstractIntegrationTest {

    private AvroJsonSchemaRegistryClientMessageConverter converter;

    @Mock
    private AvroMapper mapper;

    @Mock
    private ConfluentSchemaRegistryClient client;

    @Override
    public void before() throws Exception {
        super.before();
        converter = new AvroJsonSchemaRegistryClientMessageConverter(mapper, client,
                new ConcurrentMapCacheManager());
        converter.afterPropertiesSet();
    }

    @Test(expected = MessageConversionException.class)
    public void shouldThrowExceptionWhenErrorOccursInMessageDeserialization()
            throws Exception {
        final byte[] payload = new byte[0];
        final Map<String, Object> map = new HashMap<>();
        map.put(MessageHeaders.CONTENT_TYPE,
                MimeType.valueOf("application/vnd.testmessage.v1+avro"));
        final MessageHeaders headers = new MessageHeaders(map);

        ObjectReader reader = Mockito.mock(ObjectReader.class);
        Mockito.when(mapper.readerFor(TestMessage.class)).thenReturn(reader);
        Mockito.when(reader.with(Mockito.any(FormatSchema.class))).thenReturn(reader);
        Mockito.when(reader.readValue(payload)).thenThrow(
                new IOException("Mock Exception"));

        final SchemaReference schemaReference = new SchemaReference("testmessage", 1,
                AvroJsonSchemaRegistryClientMessageConverter.AVRO_FORMAT);
        Mockito.when(client.fetch(schemaReference)).thenReturn(
                "{\"type\":\"record\",\"name\":\"TestMessage\",\"namespace\":\"com.avro.spring.test.TestMessage\",\"doc\":\"Message for testing\",\"fields\":[{\"name\":\"field1\",\"type\":\"string\",\"doc\":\"Required string field\"},{\"name\":\"field2\",\"type\":[{\"type\":\"long\",\"java-class\":\"java.lang.Long\"},\"null\"],\"doc\":\"Nullable long field wit default value of -1\",\"default\":-1}]}");

        final TestMessage message = (TestMessage) converter.fromMessage(
                new GenericMessage<>(payload, headers), TestMessage.class);

        assertThat(message, is(notNullValue()));
        assertThat(message.getField1(), is("Mock Field 1"));
        assertThat(message.getField2(), is(nullValue()));
    }

    @Test(expected = SchemaNotFoundException.class)
    public void shouldThrowExceptionWhenDynamicGenerationIsFalseAndNoSchemaExistsWhileSerializing() {
        final TestMessage testMessage = new TestMessage("Mock Field 1");

        converter.toMessage(testMessage,
                new MessageHeaders(Maps.<String, Object> newHashMap()));
    }

    @Test(expected = SchemaGenerationException.class)
    public void shouldThrowExceptionWhenUnableToGenerateDynamicSchema() throws Exception {
        Mockito.when(mapper.schemaFor(TestMessage.class)).thenThrow(
                JsonMappingException.fromUnexpectedIOE(new IOException("Mock Message")));

        final TestMessage testMessage = new TestMessage("Mock Field 1");
        converter.setDynamicSchemaGenerationEnabled(true);

        converter.toMessage(testMessage,
                new MessageHeaders(Maps.<String, Object> newHashMap()));
    }
}
