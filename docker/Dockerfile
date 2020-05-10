FROM maven:3.6.3-openjdk-8-slim
ARG VERSION
ENV WDM_JAR=webdrivermanager-${VERSION}-fat.jar
ENV BROWSER=server
ENV WDM_DIR=/wdm
RUN mkdir ${WDM_DIR}
COPY ${WDM_JAR} ${WDM_DIR}
EXPOSE 4041
WORKDIR ${WDM_DIR}
CMD ["sh", "-c", "java -jar ${WDM_JAR} ${BROWSER}"]
