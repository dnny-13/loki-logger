package sh.dnny.serializer;

import burp.api.montoya.core.Marker;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class HttpRequestSerializer extends JsonSerializer<HttpRequest> {
    @Override
    public void serialize(HttpRequest request, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();
        gen.writeBooleanField("inScope", request.isInScope());
        gen.writeStringField("url", request.url());
        gen.writeStringField("method", request.method());
        gen.writeStringField("path", request.path());
        gen.writeStringField("query", request.query());
        gen.writeStringField("pathWithoutQuery", request.pathWithoutQuery());
        gen.writeStringField("fileExtension", request.fileExtension());
        gen.writeObjectField("contentType", request.contentType());

        gen.writeFieldName("parameters");
        gen.writeStartArray();
        for (ParsedHttpParameter param : request.parameters()) {
            gen.writeObject(param);
        }
        gen.writeEndArray();

        gen.writeFieldName("headers");
        gen.writeStartArray();
        for (HttpHeader header : request.headers()) {
            gen.writeObject(header);
        }
        gen.writeEndArray();

        gen.writeStringField("httpVersion", request.httpVersion());
        gen.writeNumberField("bodyOffset", request.bodyOffset());
        gen.writeStringField("body", request.bodyToString());

        // Serialize markers if present
//        gen.writeFieldName("markers");
//        gen.writeStartArray();
//        for (Marker marker : request.markers()) {
//            gen.writeObject(marker);
//        }
//        gen.writeEndArray();

        gen.writeEndObject();
    }
}