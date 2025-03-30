package sh.dnny.serializer;

import burp.api.montoya.http.message.HttpHeader;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class HttpHeaderSerializer extends JsonSerializer<HttpHeader> {
    @Override
    public void serialize(HttpHeader httpHeader,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("name", httpHeader.name());
        jsonGenerator.writeStringField("value", httpHeader.value());
        jsonGenerator.writeEndObject();
    }
}
