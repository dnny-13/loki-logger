package sh.dnny.service;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.HttpMode;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.proxy.http.InterceptedResponse;
import burp.api.montoya.proxy.http.ProxyResponseHandler;
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction;
import burp.api.montoya.proxy.http.ProxyResponseToBeSentAction;
import com.fasterxml.jackson.core.JsonProcessingException;
import sh.dnny.config.Config;
import sh.dnny.model.LogLine;
import sh.dnny.model.LokiLog;
import sh.dnny.model.LokiStream;
import sh.dnny.util.Utils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.concurrent.*;

public class LogService {
    private static final String LOKI_EXTENSION_HEADER = "X-Burp-Loki-Logger";
    private volatile boolean loggingActive = true;

    private final MontoyaApi api;
    private final Config config;
    private final ConcurrentHashMap<Integer, LogLine> entries;
    private final ConcurrentLinkedQueue<LogLine> logQueue;

    // Scheduler and scheduled task reference.
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledTask;

    // Handlers
    private final HttpHandler httpHandler;
    private final ProxyResponseHandler proxyResponseHandler;

    public LogService(MontoyaApi montoyaApi, Config config) {
        this.api = montoyaApi;
        this.entries = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.logQueue = new ConcurrentLinkedQueue<>();
        this.httpHandler = httpHandler();
        this.proxyResponseHandler = proxyResponseHandler();
        this.config = config;

        schedulePushLog();
    }

    // Schedules the pushLog task based on the current frequency setting.
    private void schedulePushLog() {
        int frequency = config.getUploadFrequencySeconds();
        scheduledTask = scheduler.scheduleAtFixedRate(this::pushLog,
                frequency, // initial delay
                frequency, // period
                TimeUnit.SECONDS);
    }

    public void updateFrequency() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
        }
        schedulePushLog();
        api.logging().logToOutput("[+] PushLog scheduler updated to " + config.getUploadFrequencySeconds() + " seconds.");
    }

    public void stopLogging() {
        loggingActive = false;
    }

    public void startLogging() {
        loggingActive = true;
    }

    private void pushLog() {
        if (!loggingActive) {
            return;
        }
        if (logQueue.isEmpty()) {
            return;
        }
        while (!logQueue.isEmpty()) {
            Instant now = Instant.now();
            LogLine logLine = logQueue.poll();
            if (logLine != null && logLine.getID() != null) {
                this.entries.remove(logLine.getID());
            }
            String line = null;
            try {
                line = Utils.serialize(logLine);
//                api.logging().logToOutput(line);
            } catch (JsonProcessingException e) {
                api.logging().logToError("[-] Failed to serialize log line", e);
            }
            LokiLog log = new LokiLog();
            LokiStream lokiStream = new LokiStream();
            lokiStream.setStream(new LokiStream.LokiStreamLabel(config.getJobName()));
            LokiStream.LokiLogValue value = new LokiStream.LokiLogValue();
            value.setTs(String.valueOf(now.getEpochSecond() * 1_000_000_000L + now.getNano()));
            value.setLine(line);
            lokiStream.addValue(value);
            log.addStream(lokiStream);
            sendToLoki(log);
        }
    }

    private void sendToLoki(LokiLog log) {
        boolean useHttps = config.isUseHttps();
        String host = config.getAddress();
        int port = config.getPort();
        String protocol = useHttps ? "https" : "http";
        String url = protocol + "://" + host + ":" + port + "/loki/api/v1/push";

        HttpRequest request = HttpRequest.httpRequestFromUrl(url)
                .withMethod("POST")
                .withHeader("Content-Type", "application/json")
                .withHeader(LOKI_EXTENSION_HEADER, "true");
        if ("Basic".equals(config.getAuthMethod())
                && !config.getUsername().isEmpty()
                && !config.getPassword().isEmpty()) {
            String credentials = config.getUsername() + ":" + config.getPassword();
            String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            request = request.withHeader("Authorization", "Basic " + basicAuth);
        }
        try {
            request = request.withBody(Utils.serialize(log));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        api.http().sendRequest(request, HttpMode.AUTO);
    }

    private HttpHandler httpHandler() {
        return new HttpHandler() {

            @Override
            public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent httpRequestToBeSent) {
                if (!loggingActive) {
                    return RequestToBeSentAction.continueWith(httpRequestToBeSent);
                }
                Instant now = Instant.now();
                if (httpRequestToBeSent
                        .headers()
                        .stream()
                        .anyMatch(httpHeader -> httpHeader.name().equals(LOKI_EXTENSION_HEADER))) {
                    return RequestToBeSentAction.continueWith(httpRequestToBeSent);
                }
                LogLine newLog = new LogLine();
                newLog.setRequestTime(now);
                newLog.setSource(httpRequestToBeSent.toolSource());
                newLog.setRequest(httpRequestToBeSent);
                Integer id = System.identityHashCode(httpRequestToBeSent);
                newLog.setID(id);
                entries.put(id, newLog);
                return RequestToBeSentAction.continueWith(httpRequestToBeSent, Utils.addTracingID(id, httpRequestToBeSent.annotations()));
            }

            @Override
            public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived httpResponseReceived) {
                if (!loggingActive) {
                    return ResponseReceivedAction.continueWith(httpResponseReceived);
                }
                if (httpResponseReceived.toolSource().isFromTool(ToolType.PROXY) ||
                        httpResponseReceived.initiatingRequest()
                                .headers()
                                .stream()
                                .anyMatch(httpHeader -> httpHeader.name().equals(LOKI_EXTENSION_HEADER))) {
                    return ResponseReceivedAction.continueWith(httpResponseReceived);
                }
                Instant now = Instant.now();
                Annotations annotations = httpResponseReceived.annotations();
                AbstractMap.SimpleImmutableEntry<Integer, Annotations> tracer = Utils.removeTracingID(annotations);
                matchRequestResponse(tracer.getKey(), now, httpResponseReceived);
                return ResponseReceivedAction.continueWith(httpResponseReceived, tracer.getValue());
            }
        };
    }

    private void matchRequestResponse(Integer key,
                                      Instant now,
                                      HttpResponse httpResponseReceived) {
        if (key == null) {
            return;
        }
        if (entries.containsKey(key)) {
            LogLine logLine = entries.get(key);
            logLine.setResponseTime(now);
            logLine.setResponse(httpResponseReceived);
            this.logQueue.add(logLine);
        }
    }

    private ProxyResponseHandler proxyResponseHandler() {
        return new ProxyResponseHandler() {
            @Override
            public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
                return ProxyResponseReceivedAction.continueWith(interceptedResponse);
            }

            @Override
            public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
                if (!loggingActive) {
                    return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
                }
                Instant now = Instant.now();
                Annotations annotations = interceptedResponse.annotations();
                AbstractMap.SimpleImmutableEntry<Integer, Annotations> tracer = Utils.removeTracingID(annotations);
                matchRequestResponse(tracer.getKey(), now, interceptedResponse);
                return ProxyResponseToBeSentAction.continueWith(interceptedResponse, annotations);
            }
        };
    }

    public HttpHandler getHttpHandler() {
        return httpHandler;
    }

    public ProxyResponseHandler getProxyResponseHandler() {
        return proxyResponseHandler;
    }

    public void shutdown() {
        stopLogging();

        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
        }

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException ex) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
