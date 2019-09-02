# sdkwriter
Write events to backends from SDK

## How it works
The writer will perform the following steps:
- Read records from upstream kafka broker
- Aggregate records by client
- write to downstream kafka broker with the aggregated records
- Periodically read from downstream and write to cloud object storage

### Kafka Streams API
Kafka stream API is a Java library provides some stream operations. The 2 most important type we utilize here is:
#### KStream

#### KTable

## How to build
### build as standalone jar
```bash
cd sdkwriter
./mvnw package
```
### build as Docker image
```bash
cd sdkwriter
./mvnw install dockerfile:build
```
Default registry of the image is `docker-registry.dv-api.com`, 
you can change to other registry by adding `-Ddocker.registry=<YOUR_REGISTRY>` to the and.

## How to deploy
### deploy as standalone jar
```bash
java -Dspring.profiles.active=<PROFILE> -jar sdkwriter-<VERSION>.jar
```
### deploy as docker container
```bash
docker run -e SPRING_PROFILE_ACTIVE=<PROFILE> -p 8080:8080 -p 8888:8888 <REGISTRY>/sdkwriter:<VERSION>
```
### configuration
The configs are stored at `./config` folder, with name formatted as `application-<profile_name>.yml` or `sdkwriter-<profile_name>.yml`.
those config files can be overwritten by command arguments, jvm properties, etc. for more information, see:
[spring externalize config](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)

## Good to know
### service ports
- 8080 - the embedded config server
- 8888 - the management endpoints, for more infomation: 
[spring boot endpoints](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html)
### Monitoring
Monitoring endpoints are one of the endpoints under the `8888` service port
- /actuator/metrics - return json formatted counter names, access counter value by send another request to 
`/actuator/metrics/<counter_name>`
- /actuator/prometheus - return prometheus formatted metrics with values which can be directly used.
### config refresh
Some of the config can be refreshed at runtime, see [SdkWriterProerties](src/main/java/com/datavisor/sdkwriter/SdkwriterApplication.java)
for more information.
For refresh configs at runtime:
```bash
curl <server_host>:8888/actuator/refresh -XPOST
```
