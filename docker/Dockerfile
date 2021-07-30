FROM openjdk:11-jdk-slim

ARG VERSION

ENV WDM_JAR=webdrivermanager-${VERSION}-fat.jar
ENV ARGS=server
ENV HOME_DIR=/root
ENV CACHE_DIR=/wdm

RUN mkdir -p ${CACHE_DIR}

COPY ${WDM_JAR} ${HOME_DIR}

EXPOSE 4444

WORKDIR ${HOME_DIR}

CMD ["sh", "-c", "java -Dwdm.cachePath=${CACHE_DIR} -Dwdm.resolutionCachePath=${HOME_DIR} -jar ${WDM_JAR} ${ARGS}"]
