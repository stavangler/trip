FROM azul/zulu-openjdk-alpine:15-jre

CMD mkdir /opt
COPY build/libs/trip-1.0-SNAPSHOT-all.jar /opt/app.jar

EXPOSE 8080

WORKDIR /opt
CMD ["java", "-XshowSettings:vm", "-XX:+PrintFlagsFinal", "-XX:MaxRAMPercentage=70", "-Djava.security.egd=file:/dev/./urandom", "-Djava.awt.headless=true", "-jar", "/opt/app.jar"]

