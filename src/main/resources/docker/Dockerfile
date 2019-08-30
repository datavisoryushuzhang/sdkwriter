FROM adoptopenjdk:8-jre-hotspot
ARG JAR_FILE
WORKDIR /
COPY ${JAR_FILE} app.jar
COPY entrypoint.sh .

ENTRYPOINT ["sh","-c","chmod +x /entrypoint.sh && sync && /entrypoint.sh"]