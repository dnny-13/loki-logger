package sh.dnny.model;

import burp.api.montoya.core.ToolSource;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.Instant;

public class LogLine {
    @JsonIgnore
    private Integer ID;
    private ToolSource source;
    private HttpRequest request;
    private HttpResponse response;
    private Instant requestTime;
    private Instant responseTime;

    public long getResponseTime() {
        return Duration.between(requestTime, responseTime).toMillis();
    }

    public void setRequestTime(Instant requestTime) {
        this.requestTime = requestTime;
    }

    public void setResponseTime(Instant responseTime) {
        this.responseTime = responseTime;
    }

    public ToolSource getSource() {
        return source;
    }

    public void setSource(ToolSource source) {
        this.source = source;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }
}
