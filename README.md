# Graylog Extended Log Format (GELF) Implementation for Java Logging API

The Graylog Extended Log Format (GELF) is a modern, structured log format designed to overcome the limitations of 
traditional syslog used to send customs logs to Graylog Servers recommended for applications servers.

Learn more about GELF on the [Graylog Documentation](https://go2docs.graylog.org/current/getting_in_log_data/gelf.html).


## Advantages of GELF

GELF was created to provide a **structured, flexible, and reliable** alternative to syslog — especially for **application-level logging**.

Key features include:

- **Structured JSON format** – easy to parse and index.  
- **Supports large payloads** – ideal for exceptions, stack traces, and detailed messages.  
- **Data typing** – numbers remain numbers, strings remain strings.  
- **Compression support** – reduces bandwidth usage.  
- **Multiple transports** – can be sent over UDP, avoiding issues like timeouts or connection failures that might disrupt application flow.

## Motivation:

The available libraries use Log4J as the default GELF log appender (incompatible with more recent versions of JBoss custom-handlers).</br>
I couldn't find any standard logging customization libraries compatible with the latest Wildfly/JBoss versions (>= 35) + JDK 21 for sending logs to Graylog servers, so I decided to implement a library that natively integrates with application servers using the Java Logging API so we could add custom log handlers for this purpose.

## Compile and Deploy Project:

This project required Java >= 21

```
mvn clean package
```

Deploy

```
mvn clean javadoc:javadoc source:jar install deploy -Prelease
```

## Property Configurations:

 - **graylogPort**: UDP/TCP GELF Graylog Server port (mandatory);
 - **graylogHost**: Graylog Server Host/IP (mandatory);
 - **instanceName**: Custom field to identify the application name/instance (optional);
 - **graylogProtocol**: Protocol (tcp/udp) (default value is udp) (optional);
 - **additonalFields**: Additional fields supported by GELF protocol with simple key/value json format (optional):

```
{"key1":"value1","key2":"value2","key3":"value3"}
```

## How to use:

 - Configure Maven dependency:
 
 ```
<dependency>
	<groupId>dev.knebelhub</groupId>
	<artifactId>gelf-logger</artifactId>
	<version>1.1.23</version>
</dependency>
```
 

 - Example to usage:
 
 ```
import java.util.logging.Logger;

public class Main {

	public static void main(String[] args) {
		
		Logger logger = Logger.getLogger(Main.class.getName());
		GelfLoggingHandler handler = new GelfLoggingHandler();
		handler.setGraylogHost("127.0.0.1");
		handler.setGraylogPort(12201);
		handler.setInstanceName("application-name");
		handler.setGraylogProtocol("udp");
		handler.setAdditionalFields("{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\"}");
		logger.addHandler(handler);
		logger.info("sending log message to graylog server");
	}
}
```


 - The output GELF message send to Graylog:

```
{
  "version": "1.1",
  "host": "my-machine-name",
  "short_message": "sending log message to graylog server",
  "full_message": "sending log message to graylog server",
  "timestamp": 1760402047.565,
  "level": 6,
  "facility": "gelf-java",
  "_logger": "dev.knebelhub.logging.Main",
  "_thread": "main",
  "_instanceName": "application-name",
  "_key1": "value1",
  "_key2": "value2",
  "_key3": "value3"
}
```
 - Wildfly/JBoss Integration:

 
Wildfly/JBoss allows to configure custom handlers on logging subsystem.</br>
To send standard server log output to a log monitoring server, you need to add the gelf-loggin library to subsystem.

Obs: The **JBOSS_HOME** variable is a root Wildfly/JBoss installation path.

 - Create **/modules/system/layers/base/org/graylog/main** module path and copy the gelf-logger project compiled and gson dependency avaliable on 
 oficial [Maven Reposiory](https://mvnrepository.com/artifact/com.google.code.gson/gson/2.13.2).
 
```
mkdir -p $JBOSS_HOME/modules/system/layers/base/org/graylog/main
cp gson-2.13.2.jar $JBOSS_HOME/modules/system/layers/base/org/graylog/main
cp gelf-logger-1.1.21.jar $JBOSS_HOME/modules/system/layers/base/org/graylog/main

```

 - Create the module descriptor like:
 
 ```

cat > $JBOSS_HOME/modules/system/layers/base/org/graylog/main/module.xml <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<module name="org.graylog" xmlns="urn:jboss:module:1.9">
    <resources>
        <resource-root path="gelf-logger-1.1.21.jar" />
        <resource-root path="gson-2.13.2.jar" />
    </resources>
    <dependencies>
        <module name="javax.api" />
        <module name="org.slf4j" />
    </dependencies>
</module>
EOF
```

 - Configure custom handler to Wildfly/JBoss send the default log to Graylog Server:</br>
Obs: Don't forget to replace graylogHost and graylogPort to your server logs.


 ```
$JBOSS_HOME/bin/jboss-cli.sh <<EOF
embed-server --std-out=echo --server-config=standalone.xml
batch
/subsystem=logging/custom-handler=GRAYLOG:add(class="dev.knebelhub.logging.GelfLoggingHandler", module="org.graylog", properties={"graylogHost"=>"localhost","graylogPort"=>"12201","instanceName"=>"\${jboss.node.name}"})
/subsystem=logging/root-logger=ROOT:add-handler(name="GRAYLOG")
run-batch
stop-embedded-server
EOF

```

 - Enjoy! Now your application servers will also send standard logs to your Graylog server.




