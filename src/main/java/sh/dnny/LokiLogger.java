package sh.dnny;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;
import sh.dnny.config.Config;
import sh.dnny.service.LogService;
import sh.dnny.ui.LokiLoggerUI;

public class LokiLogger implements BurpExtension {
    private LogService logService;
    private MontoyaApi api;
    private Config config;
    private LokiLoggerUI ui;
    private Registration menuTab;

    private static final String EXTENSION_NAME = "Loki Logger";

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        this.api = montoyaApi;
        api.extension().setName(EXTENSION_NAME);

        config = new Config();
        config.loadSettings(api);

        this.logService = new LogService(montoyaApi, this.config);
        ui = new LokiLoggerUI(config, api, this.logService);
        this.menuTab = api.userInterface().registerSuiteTab(ui.getCaption(), ui.getComponent());


        api.http().registerHttpHandler(this.logService.getHttpHandler());
        api.proxy().registerResponseHandler(this.logService.getProxyResponseHandler());
        if (config.isAutostartAll()) {
            logService.startLogging();
            api.logging().logToOutput("[+] Autostart enabled for all projects. Logging started.");
        } else if (config.isAutostartThis()) {
            logService.startLogging();
            api.logging().logToOutput("[+] Autostart enabled for this project. Logging started.");
        } else {
            logService.stopLogging();
            api.logging().logToOutput("[+] Autostart disabled. Logging is not started automatically.");
        }
        api.extension().registerUnloadingHandler(() -> {
            this.unloadExtension();
            api.logging().logToOutput("[+] Loki Logger resources released.");
        });
        api.logging().logToOutput("[+] " + EXTENSION_NAME + " loaded!");
    }

    public void unloadExtension() {
        menuTab.deregister();
        logService.shutdown();
    }
}
