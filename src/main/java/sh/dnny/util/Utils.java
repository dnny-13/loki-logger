package sh.dnny.util;

import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.ToolSource;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import sh.dnny.model.LogLine;
import sh.dnny.model.LokiStream;
import sh.dnny.serializer.*;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Utils {
    private static final Pattern TRACE_PATTERN = Pattern.compile("#Trace:(\\d+)#");
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LogLine.class, new LogLineSerializer());
        module.addSerializer(HttpRequest.class, new HttpRequestSerializer());
        module.addSerializer(HttpResponse.class, new HttpResponseSerializer());
        module.addSerializer(ToolSource.class, new ToolSourceSerializer());
        module.addSerializer(LogLine.class, new LogLineSerializer());
        module.addSerializer(LokiStream.LokiLogValue.class, new LokiLogValueSerializer());
        module.addSerializer(LokiStream.LokiLogValue.class, new LokiLogValueSerializer());
        module.addSerializer(HttpHeader.class, new HttpHeaderSerializer());
        objectMapper.registerModule(module);
    }

    public static String serialize(Object object)
            throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public static Annotations addTracingID(Integer id, Annotations annotations) {
        String originalNotes = Objects.toString(annotations.notes(), "");
        String updatedNotes = originalNotes + "#Trace:" + id + "#";
        return annotations.withNotes(updatedNotes);
    }

    public static AbstractMap.SimpleImmutableEntry<Integer, Annotations> removeTracingID(Annotations annotations) {
        String notes = Objects.toString(annotations.notes(), "");
        Matcher matcher = TRACE_PATTERN.matcher(notes);
        Integer tracingId = null;
        if (matcher.find()) {
            tracingId = Integer.parseInt(matcher.group(1));
            notes = matcher.replaceFirst("");
        }
        return new AbstractMap.SimpleImmutableEntry<>(tracingId, annotations.withNotes(notes));
    }
}
