FROM adoptopenjdk:11-jre-hotspot

LABEL author="Selçuk SERT"
LABEL mail="ssert@gmail.com"
LABEL description="Sample multicast message receiver using Vert.x UDP utilities"

ENV TZ=Europe/Istanbul
ENV APP_USER=appuser
ENV APP_HOME=/opt/udp/receiver
ENV APP_BIN=/opt/udp/receiver/receiver.jar

USER root
RUN apt-get update && apt-get install -y tcpdump net-tools iputils-ping netcat iproute2
RUN useradd -ms /bin/bash $APP_USER && mkdir -p $APP_HOME
ADD ./target/message-receiver-0.0.1-SNAPSHOT.jar $APP_BIN

USER $APP_USER
WORKDIR $APP_HOME

# Use shell form instead of exec form to use envvar
ENTRYPOINT java -jar $APP_BIN -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -conf $APP_HOME/config/config.json