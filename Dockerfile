FROM amazoncorretto:8

ENV SPRING_PROFILES_ACTIVE production

EXPOSE 8080
EXPOSE 8081


ADD lib/AI-Agent.xml /opt/appinsights/AI-Agent.xml
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.0.3/applicationinsights-agent-3.0.3.jar /opt/appinsights/applicationinsights-agent-3.0.3.jar

ADD build/libs/identity-service.jar /data/app.jar
CMD java -javaagent:/opt/appinsights/applicationinsights-agent-3.0.3.jar -jar /data/app.jar