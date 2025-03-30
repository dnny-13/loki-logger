package sh.dnny.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import sh.dnny.model.LokiStream;

import java.io.IOException;

public class LokiLogValueSerializer extends JsonSerializer<LokiStream.LokiLogValue> {
    @Override
    public void serialize(LokiStream.LokiLogValue value,
                          JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        gen.writeStartArray();
        gen.writeString(value.getTs());
        gen.writeString(value.getLine());
        gen.writeEndArray();
    }

}
