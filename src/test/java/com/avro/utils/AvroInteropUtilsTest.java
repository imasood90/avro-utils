package com.avro.utils;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import com.avro.test.AbstractTest;
import com.avro.test.TestMessage;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.junit.Test;

import com.fasterxml.jackson.dataformat.avro.AvroMapper;

public class AvroInteropUtilsTest extends AbstractTest {

    private AvroInteropUtils avro;

    private byte[] payload;

    private final TestMessage testMessage = new TestMessage("Mock Field 1");

    private final AvroMapper mapper = new AvroMapper();

    @Override
    public void before() throws Exception {
        super.before();
        avro = new AvroInteropUtils(mapper);

        final DatumWriter<Object> writer = new ReflectDatumWriter<>(
                mapper.schemaFor(TestMessage.class).getAvroSchema());
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Encoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
        writer.write(testMessage, encoder);
        encoder.flush();

        payload = baos.toByteArray();
    }

    @Test
    public void shouldDecode() throws Exception {

        avro.decode(payload, TestMessage.class);
    }

    @Test
    public void shouldDecodeUsingSchema() throws Exception {
        final String schema = "{\"type\":\"record\",\"name\":\"TestMessage\",\"namespace\":"
                + "\"com.avro.test\",\"doc\":\"Message for testing\",\"fields\":"
                + "[{\"name\":\"field1\",\"type\":\"string\",\"doc\":\"Required string field\"},"
                + "{\"name\":\"field2\",\"type\":[{\"type\":\"long\",\"java-class\":"
                + "\"java.lang.Long\"},\"null\"],\"doc\":\"Nullable long field wit default value"
                + " of -1\",\"default\":-1}]}";

        final TestMessage testMessage = avro.decode(payload, schema, TestMessage.class);
        assertThat(testMessage.getField1(), is("Mock Field 1"));
        assertThat(testMessage.getField2(), nullValue());
    }
}
