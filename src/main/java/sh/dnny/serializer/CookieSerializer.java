package sh.dnny.serializer;

import burp.api.montoya.http.message.Cookie;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CookieSerializer extends JsonSerializer<Cookie> {
    @Override
    public void serialize(Cookie cookie,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("name", cookie.name());
        jsonGenerator.writeStringField("value", cookie.value());
        jsonGenerator.writeStringField("domain", cookie.domain());
        jsonGenerator.writeEndObject();
    }
}
