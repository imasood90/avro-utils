package com.avro.spring;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.junit.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cloud.stream.schema.client.ConfluentSchemaRegistryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.MimeType;

import com.avro.test.AbstractIntegrationTest;
import com.avro.test.TestMessage;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.google.common.collect.Maps;

public class AvroJsonSchemaRegistryClientMessageConverterIntegrationTest
        extends AbstractIntegrationTest {

    private AvroJsonSchemaRegistryClientMessageConverter converter;

    private AvroMapper mapper = new AvroMapper();

    private final TestMessage testMessage = new TestMessage("Mock Field 1");

    private byte[] payload;

    @Override
    public void before() throws Exception {
        super.before();

        final ConfluentSchemaRegistryClient client = new ConfluentSchemaRegistryClient();
        client.setEndpoint(String.format("http://localhost:%d/", instanceRule.port()));

        converter = new AvroJsonSchemaRegistryClientMessageConverter(mapper, client,
                new ConcurrentMapCacheManager());
        converter.setDynamicSchemaGenerationEnabled(true);

        converter.afterPropertiesSet();
        final DatumWriter<Object> writer = new ReflectDatumWriter<>(
                mapper.schemaFor(TestMessage.class).getAvroSchema());
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Encoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
        writer.write(testMessage, encoder);
        encoder.flush();

        payload = baos.toByteArray();
    }

    @Test
    public void shouldConvertFromMessage() {
        stubFor(get(urlPathEqualTo("/subjects/testmessage/versions/1")) //
                .willReturn(aResponse() //
                        .withStatus(200) //
                        .withHeader(HttpHeaders.CONTENT_TYPE,
                                "application/vnd.schemaregistry.v1+json") //
                        .withBodyFile("testmessage_get.json")));

        final Map<String, Object> map = new HashMap<>();
        map.put(MessageHeaders.CONTENT_TYPE,
                MimeType.valueOf("application/vnd.testmessage.v1+avro"));
        final MessageHeaders headers = new MessageHeaders(map);

        final TestMessage message = (TestMessage) converter.fromMessage(
                new GenericMessage<>(payload, headers), TestMessage.class);

        assertThat(message, is(notNullValue()));
        assertThat(message.getField1(), is("Mock Field 1"));
        assertThat(message.getField2(), is(nullValue()));
    }

    @Test
    public void shouldConvertToMessage() {
        stubFor(post(urlPathEqualTo("/subjects/testmessage/versions")) //
                .willReturn(aResponse() //
                        .withStatus(200) //
                        .withHeader(HttpHeaders.CONTENT_TYPE,
                                "application/vnd.schemaregistry.v1+json") //
                        .withBody("{\"id\":1}")));

        stubFor(post(urlPathEqualTo("/subjects/testmessage")) //
                .willReturn(aResponse() //
                        .withStatus(200) //
                        .withHeader(HttpHeaders.CONTENT_TYPE,
                                "application/vnd.schemaregistry.v1+json") //
                        .withBodyFile("testmessage_get.json")));

        final Message<?> message = converter.toMessage(testMessage,
                new MessageHeaders(Maps.<String, Object> newHashMap()));
        assertThat(message, is(notNullValue()));
        assertThat((byte[]) message.getPayload(), is(payload));
    }
}
