package sh.dnny.config;

import burp.api.montoya.MontoyaApi;

public class Config {

    private String address = "127.0.0.1";
    private int port = 3100;
    private boolean useHttps = false;

    private String jobName = "burp-suite";

    private String authMethod = "None"; // or "Basic"
    private String username = "";
    private String password = "";

    private int uploadFrequencySeconds = 1;

    private boolean autostartAll = true;
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
        String address = api.persistence().preferences().getString("lokiLogger.address");
        this.setAddress(address == null || address.isEmpty() ? "127.0.0.1" : address);
        Integer integer = api.persistence().preferences().getInteger("lokiLogger.port");
        this.setPort(integer == null ? 3100 : integer);
        Boolean useHttps = api.persistence().preferences().getBoolean("lokiLogger.useHttps");
        this.setUseHttps(useHttps != null && useHttps);
        String jobName = api.persistence().preferences().getString("lokiLogger.jobName");
        this.setJobName(jobName == null || jobName.isEmpty() ? "burp-suite" : jobName);
        String authMethod = api.persistence().preferences().getString("lokiLogger.authMethod");
        this.setAuthMethod(authMethod == null || authMethod.isEmpty() ? "None" : authMethod);
        String username = api.persistence().preferences().getString("lokiLogger.username");
        this.setUsername(username);
        String password = api.persistence().preferences().getString("lokiLogger.password");
        this.setPassword(password);
        Integer uploadFrequency = api.persistence().preferences().getInteger("lokiLogger.uploadFrequencySeconds");
        this.setUploadFrequencySeconds(uploadFrequency == null ? 1 : uploadFrequency);
        Boolean autoStartAll = api.persistence().preferences().getBoolean("lokiLogger.autostartAll");
        this.setAutostartAll(autoStartAll == null || autoStartAll);
        Boolean autoStartProject = api.persistence().preferences().getBoolean("lokiLogger." + api.project().id() + ".autostartThis");
        this.setAutostartThis(autoStartProject == null || autoStartProject);
    }


    public void storeSettings(MontoyaApi api) {
        api.persistence().preferences().setString("lokiLogger.address", this.address);
        api.persistence().preferences().setInteger("lokiLogger.port", this.port);
        api.persistence().preferences().setString("lokiLogger.jobName", this.jobName);
        api.persistence().preferences().setString("lokiLogger.authMethod", this.authMethod);
        api.persistence().preferences().setString("lokiLogger.username", this.username);
        api.persistence().preferences().setString("lokiLogger.password", this.password);
        api.persistence().preferences().setBoolean("lokiLogger.useHttps", this.useHttps);
        api.persistence().preferences().setBoolean("lokiLogger." + api.project().id() + ".autostartThis", this.autostartThis);
        api.persistence().preferences().setBoolean("lokiLogger.autostartAll", this.autostartAll);
        api.persistence().preferences().setInteger("lokiLogger.uploadFrequencySeconds", this.uploadFrequencySeconds);
    }
}
