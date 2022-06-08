FROM openjdk:11.0.15-jre as runner
WORKDIR runner
COPY **/target/app.jar runner/
CMD java -jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000  runner/app.jar 
