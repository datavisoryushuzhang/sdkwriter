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
```bash
cd sdkwriter
./mvnw install dockerfile:build
```
## How to deploy
```bash
docker run -e SPRING_PROFILES_ACTIVE=<your_profile> —name sdkwriter -d docker-registry.dv-api.com/library/sdkwriter:0.1.0
```
