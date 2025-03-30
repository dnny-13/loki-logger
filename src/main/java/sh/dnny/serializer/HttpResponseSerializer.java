package sh.dnny.serializer;

import burp.api.montoya.http.message.Cookie;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.responses.HttpResponse;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class HttpResponseSerializer extends JsonSerializer<HttpResponse> {
    @Override
    public void serialize(HttpResponse response, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("statusCode", response.statusCode());
        gen.writeStringField("reasonPhrase", response.reasonPhrase());

        // Serialize cookies
        gen.writeFieldName("cookies");
        gen.writeStartArray();
        for (Cookie cookie : response.cookies()) {
            gen.writeObject(cookie);
        }
        gen.writeEndArray();

        gen.writeObjectField("mimeType", response.mimeType());
        gen.writeStringField("httpVersion", response.httpVersion());
        gen.writeNumberField("bodyOffset", response.bodyOffset());
        gen.writeStringField("body", response.bodyToString());

        // Serialize headers
        gen.writeFieldName("headers");
        gen.writeStartArray();
        for (HttpHeader header : response.headers()) {
            gen.writeObject(header);
        }
        gen.writeEndArray();

//        // Serialize markers
//        gen.writeFieldName("markers");
//        gen.writeStartArray();
//        for (Marker marker : response.markers()) {
//            gen.writeObject(marker);
//        }
//        gen.writeEndArray();

        gen.writeEndObject();
    }
}