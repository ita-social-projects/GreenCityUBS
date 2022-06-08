FROM openjdk:11.0.15-jre as runner
WORKDIR runner
COPY **/target/app.jar runner/
CMD java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:0.0.0.0:5005 -jar runner/app.jar
