#!/bin/sh

JAVA=/usr/bin/java
LOG_CONF=logging.properties
OPTS=

$JAVA -jar -Djava.util.logging.config.file=$LOG_CONF \
	-Djava.library.path=/usr/lib/jni \
	-Djava.net.preferIPv4Stack=true \
	raspager-c9000-1.0.0-SNAPSHOT.jar $OPTS "$@"
