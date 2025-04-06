package sh.dnny.ui;

import burp.api.montoya.MontoyaApi;
import sh.dnny.config.Config;
import sh.dnny.service.LogService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class LokiLoggerUI {
    private final JPanel mainPanel;
    private final Config config;
    private final MontoyaApi api;
    private final LogService logService;

    // UI components.
    private JTextField addressField;
    private JTextField portField;
    private JComboBox<String> protocolCombo;
    private JTextField indexField;
    private JComboBox<String> authCombo;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField uploadFreqField;
    private JCheckBox autostartAllProjects;
    private JCheckBox autostartThisProject;

    // Buttons.
    private JButton startButton;
    private JButton stopButton;
    private JButton saveButton;

    // New status label indicator.
    private JLabel statusLabel;

    public LokiLoggerUI(Config config,
                        MontoyaApi api,
                        LogService logService) {
        this.config = config;
        this.api = api;
        this.logService = logService;
        mainPanel = new JPanel(new BorderLayout(10, 10));
        buildUi();
    }

    private void buildUi() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (this.config.isAutostartThis() || this.config.isAutostartAll()) {
            statusLabel = new JLabel("● Running");
            statusLabel.setForeground(Color.GREEN);
        } else {
            statusLabel = new JLabel("● Stopped");
            statusLabel.setForeground(Color.RED);
        }
        statusLabel.setToolTipText("Current status of Loki Logger");
        statusPanel.add(new JLabel("Status: "));
        statusPanel.add(statusLabel);
        contentPanel.add(statusPanel);

        // 1. Connection Panel.
        JPanel connectionPanel = createTitledPanel("Connection");
        connectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = defaultGbc();

        // Address.
        addressField = new JTextField(config.getAddress(), 15);
        addressField.setToolTipText("Hostname or IP address of the Loki server (e.g., 127.0.0.1).");
        addLabeledField(connectionPanel, "Address:", addressField, gbc);

        // Port.
        portField = new JTextField(String.valueOf(config.getPort()), 5);
        portField.setToolTipText("The port Loki is listening on (e.g., 3100).");
        addLabeledField(connectionPanel, "Port:", portField, gbc);

        // Protocol.
        protocolCombo = new JComboBox<>(new String[]{"HTTP", "HTTPS"});
        protocolCombo.setSelectedItem(config.isUseHttps() ? "HTTPS" : "HTTP");
        protocolCombo.setToolTipText("Choose whether to use HTTP or HTTPS.");
        addLabeledField(connectionPanel, "Protocol:", protocolCombo, gbc);

        // Index / Job Label.
        indexField = new JTextField(config.getJobName(), 15);
        indexField.setToolTipText("Name of the Loki job (e.g., 'burp-logs').");
        addLabeledField(connectionPanel, "Index (Job Label):", indexField, gbc);

        contentPanel.add(connectionPanel);

        // 2. Authentication Panel.
        JPanel authPanel = createTitledPanel("Authentication");
        authPanel.setLayout(new GridBagLayout());
        GridBagConstraints authGbc = defaultGbc();

        // Auth Method.
        authCombo = new JComboBox<>(new String[]{"None", "Basic"});
        authCombo.setSelectedItem(config.getAuthMethod());
        authCombo.setToolTipText("Select 'Basic' if Loki requires basic auth.");
        authCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                boolean useBasic = "Basic".equals(authCombo.getSelectedItem());
                usernameField.setEnabled(useBasic);
                passwordField.setEnabled(useBasic);
            }
        });
        addLabeledField(authPanel, "Auth Method:", authCombo, authGbc);

        // Username.
        usernameField = new JTextField(config.getUsername(), 15);
        usernameField.setToolTipText("Basic auth username (if required).");
        addLabeledField(authPanel, "Username:", usernameField, authGbc);

        // Password.
        passwordField = new JPasswordField(config.getPassword(), 15);
        passwordField.setToolTipText("Basic auth password (if required).");
        addLabeledField(authPanel, "Password:", passwordField, authGbc);

        // Enable/disable fields based on current auth method.
        boolean useBasic = "Basic".equals(config.getAuthMethod());
        usernameField.setEnabled(useBasic);
        passwordField.setEnabled(useBasic);

        contentPanel.add(authPanel);

        // 3. Misc Panel.
        JPanel miscPanel = createTitledPanel("Misc");
        miscPanel.setLayout(new GridBagLayout());
        GridBagConstraints miscGbc = defaultGbc();

        // Upload Frequency.
        uploadFreqField = new JTextField(String.valueOf(config.getUploadFrequencySeconds()), 5);
        uploadFreqField.setToolTipText("Interval in seconds between log uploads.");
        addLabeledField(miscPanel, "Upload Frequency (Seconds):", uploadFreqField, miscGbc);

        // Autostart checkboxes.
        autostartAllProjects = new JCheckBox("Autostart Exporter (All Projects)", config.isAutostartAll());
        autostartAllProjects.setToolTipText("Automatically start Loki logging for all Burp projects.");
        autostartThisProject = new JCheckBox("Autostart Exporter (This Project)", config.isAutostartThis());
        autostartThisProject.setToolTipText("Automatically start Loki logging for the current project only.");

        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkPanel.add(autostartAllProjects);
        checkPanel.add(autostartThisProject);

        GridBagConstraints checkGbc = (GridBagConstraints) miscGbc.clone();
        checkGbc.gridwidth = 2;
        checkGbc.gridx = 0;
        checkGbc.gridy++;
        miscPanel.add(checkPanel, checkGbc);

        contentPanel.add(miscPanel);

        // 4. Button Panel.
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Start button.
        startButton = new JButton("Start");
        startButton.setToolTipText("Start sending logs to Loki.");
        startButton.addActionListener(e -> {
            this.logService.startLogging();
            updateStatusIndicator(true);
        });

        // Stop button.
        stopButton = new JButton("Stop");
        stopButton.setToolTipText("Stop sending logs to Loki.");
        stopButton.addActionListener(e -> {
            this.logService.stopLogging();
            updateStatusIndicator(false);
        });

        // Save button.
        saveButton = new JButton("Save Settings");
        saveButton.setToolTipText("Save your current settings.");
        saveButton.addActionListener(e -> saveSettings());

        Insets smallInsets = new Insets(2, 4, 2, 4);
        startButton.setMargin(smallInsets);
        stopButton.setMargin(smallInsets);
        saveButton.setMargin(smallInsets);

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(saveButton);

        contentPanel.add(Box.createVerticalStrut(10)); // Spacer.
        contentPanel.add(buttonPanel);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }


    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }


    private GridBagConstraints defaultGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }


    private void addLabeledField(JPanel panel, String labelText, JComponent field, GridBagConstraints gbc) {
        gbc.gridwidth = 1;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx++;
        panel.add(field, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
    }


    private void updateStatusIndicator(boolean isRunning) {
        if (isRunning) {
            statusLabel.setText("● Running");
            statusLabel.setForeground(Color.GREEN);
        } else {
            statusLabel.setText("● Stopped");
            statusLabel.setForeground(Color.RED);
        }
    }

    private void saveSettings() {
        try {
            int newFrequency = Integer.parseInt(uploadFreqField.getText().trim());
            boolean frequencyChanged = config.getUploadFrequencySeconds() != newFrequency;

            config.setAddress(addressField.getText().trim());
            config.setPort(Integer.parseInt(portField.getText().trim()));
            config.setUseHttps("HTTPS".equals(protocolCombo.getSelectedItem()));
            config.setJobName(indexField.getText().trim());
            config.setAuthMethod((String) authCombo.getSelectedItem());
            config.setUsername(usernameField.getText().trim());
            config.setPassword(new String(passwordField.getPassword()));
            config.setUploadFrequencySeconds(newFrequency);
            config.setAutostartAll(autostartAllProjects.isSelected());
            config.setAutostartThis(autostartThisProject.isSelected());
            config.storeSettings(api);

            api.logging().logToOutput("[+] Loki Logger settings saved successfully.");

            if (frequencyChanged) {
                this.logService.updateFrequency();
            }
        } catch (NumberFormatException ex) {
            api.logging().logToError("[-] Invalid number format in port or frequency fields.");
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Please enter a valid number for port and frequency.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    public Component getComponent() {
        return mainPanel;
    }

    public String getCaption() {
        return "Loki Logger";
    }
}
