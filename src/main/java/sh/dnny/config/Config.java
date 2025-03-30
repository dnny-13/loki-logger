package sh.dnny.config;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.burpsuite.BurpSuite;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Config {

    @JsonProperty("lokiLogger.address")
    private String address = "127.0.0.1";

    @JsonProperty("lokiLogger.port")
    private int port = 3100;

    @JsonProperty("lokiLogger.useHttps")
    private boolean useHttps = false;

    @JsonProperty("lokiLogger.indexName")
    private String jobName = "burp-suite";

    @JsonProperty("lokiLogger.authMethod")
    private String authMethod = "None"; // or "Basic"

    @JsonProperty("lokiLogger.username")
    private String username = "";

    @JsonProperty("lokiLogger.password")
    private String password = "";

    @JsonProperty("lokiLogger.uploadFrequencySeconds")
    private int uploadFrequencySeconds = 1;

    @JsonProperty("lokiLogger.autostartAll")
    private boolean autostartAll = true;

    @JsonProperty("lokiLogger.autostartThis")
    private boolean autostartThis = true;

    // Getters and setters
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    public boolean isUseHttps() {
        return useHttps;
    }
    public void setUseHttps(boolean useHttps) {
        this.useHttps = useHttps;
    }

    public String getJobName() {
        return jobName;
    }
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getAuthMethod() {
        return authMethod;
    }
    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public int getUploadFrequencySeconds() {
        return uploadFrequencySeconds;
    }
    public void setUploadFrequencySeconds(int uploadFrequencySeconds) {
        this.uploadFrequencySeconds = uploadFrequencySeconds;
    }

    public boolean isAutostartAll() {
        return autostartAll;
    }
    public void setAutostartAll(boolean autostartAll) {
        this.autostartAll = autostartAll;
    }

    public boolean isAutostartThis() {
        return autostartThis;
    }
    public void setAutostartThis(boolean autostartThis) {
        this.autostartThis = autostartThis;
    }


    public void loadSettings(MontoyaApi api) {
        BurpSuite burpSuite = api.burpSuite();
        String json = burpSuite.exportUserOptionsAsJson(
                "lokiLogger.address",
                "lokiLogger.port",
                "lokiLogger.useHttps",
                "lokiLogger.indexName",
                "lokiLogger.authMethod",
                "lokiLogger.username",
                "lokiLogger.password",
                "lokiLogger.uploadFrequencySeconds",
                "lokiLogger.autostartAll",
                "lokiLogger.autostartThis"
        );
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.readerForUpdating(this).readValue(json);
        } catch (Exception e) {
            api.logging().logToError("[-] Failed to load config: " + e.getMessage());
        }
    }


    public void saveSettings(MontoyaApi api) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(this);
            api.burpSuite().importUserOptionsFromJson(json);
        } catch (Exception e) {
            api.logging().logToError("[-] Failed to save config: " + e.getMessage());
        }
    }
}
