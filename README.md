
# Loki Logger
<p float="left">
  <img src="https://dannyshblog.b-cdn.net/loki-logger-1.png" width="49%" />
  <img src="https://dannyshblog.b-cdn.net/loki-logger-2.png" width="50%"/>
</p>
LokiLogger is a custom Burp Suite extension that captures HTTP requests and responses from all tools (Proxy, Repeater, Extensions, Intruder, ...) and sends them directly to a Loki server.

This extension is designed for:
- Security professionals;
- Penetration testers;
- Bug bounty hunters 

who want to centralize log management and leverage Grafana’s powerful visualization capabilities.

## Demo

[![Demo](https://dannyshblog.b-cdn.net/loki-logger-thumbnail.png)](https://dannyshblog.b-cdn.net/loki-logger.mp4)

## Usage/Examples
### Running the Local Stack (Loki + Grafana)
To quickly spin up a local environment for Loki and Grafana, use the provided Docker Compose configuration.
#### Prerequisites
- [Docker](https://docs.docker.com/engine/install/)
- [Docker Compose](https://docs.docker.com/compose/install/)

1. Download [latest release](https://github.com/dnny-13/loki-logger/releases/download/v0.0.1-beta/v0.0.1-beta.zip)
```shell
$ unzip v0.0.1-beta.zip
$ cd v0.0.1-beta/
```
2. Verify Configs
   - `docker-compose.yml`: Contains services for `Loki`, `Grafana`, and `nginx`
   - `loki-config.yml`: Loki’s configuration
   - `nginx.conf`: Nginx reverse proxy configuration.
   - `htpasswd`: Contains basic authN creds
   - `/grafana` : Contains grafana's provisioning files (default dashboard & default datasource [loki])
3. Start it
```shell
docker-compose up -d
```
This will spin up:
- Loki on the internal docker network. 
- Nginx listening on `0.0.0.0` port `3100`
- Grafana on `0.0.0.0` port `3000`
4. Access Grafana
   - Open your browser and navigate to: http://localhost:3000.
   - Log in with your Grafana credentials (default: `admin:admin`)
   - Navigate to: http://127.0.0.1:3000/dashboards -> `Main`
### Configuring the Extension in Burp Suite
1. Install the Extension
   - Open Burp Suite and navigate to the Extensions tab
   - Click on "Add" and load the LokiLogger JAR file.
2. Configure Settings
   -  Open the LokiLogger tab in the Burp Suite UI.
   -  Enter your Loki server settings:
      - `Address` (e.g., the hostname or IP where Loki is running, typically pointing to the nginx reverse proxy)
      - `Port` 
      - `Protocol` (e.g. `HTTP`, `HTTPS`)
      - `Job Label` (e.g. `burp-suite`)
      - `Authentication` (e.g. `None` if Loki is directly exposed or `Basic` if it's behind nginx)
      - `Upload frequency` (in seconds)
3. `Save Settings` to persist configuration
>_Note: Verify in extension's output
> ![](https://dannyshblog.b-cdn.net/loki-logger-extension-output.png)_


### Using the Extension
1. Start Logging:
   - In the LokiLogger tab, click the “Start” button to begin logging HTTP traffic. 
   - The extension will now capture HTTP requests and responses and send them to your Loki server.
2. Monitor Logs in Grafana:
3. Stop Logging
## Build Locally

1. Clone repository
```shell
git clone https://github.com/dnny-13/loki-logger
cd loki-logger/
```
2. Build via `maven`
```shell
JAVA_HOME=/usr/lib/jvm/java-1.21.0-openjdk-amd64 mvn clean package
# Make sure JAVA_HOME points to java 21's JDK
```
3.  Navigate to the output directory
```shell
dnny@dnn:~/opt/loki-logger$ cd target/
dnny@dnn:~/opt/loki-logger/target$ ls
archive-tmp  classes  generated-sources  loki-logger-0.0.1-beta.jar  loki-logger-0.0.1-beta-jar-with-dependencies.jar  maven-archiver  maven-status
```
>_Note: `loki-logger-0.0.1-beta-jar-with-dependencies.jar`'s jar contain all additional dependencies_
---
Alternatively, [IntelliJ IDEA](https://www.jetbrains.com/idea/) (or any other IDE) can be used;
1. New -> Project from Existing Sources
2. Select project's `pom.xml`
3. Open Project
>_Note: Make sure the correct JDK is configured/used!_
## License

[MIT](https://choosealicense.com/licenses/mit/)

