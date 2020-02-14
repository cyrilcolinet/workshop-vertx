# -- Gradle builder -- #
# Build project from gradle image
FROM gradle:jdk8-alpine as builder

# Configure working directory of builder
WORKDIR /home/api
COPY --chown=gradle:gradle . .

# Fix permissions
USER root
RUN chown -R gradle:gradle /home/api
USER gradle

# Run and build project
RUN ["gradle", "build", "shadowJar"]


# -- Vert.x runner -- #
# Extend vert.x image
FROM vertx/vertx3

# Configure environment variables
ENV VERTICLE_HOME /home/api
ENV VERTICLE_NAME APIServer
ENV VERTICLE_FILE /home/api/build/libs/api-1.0.0-SNAPSHOT-fat.jar

# Configure working directory
WORKDIR $VERTICLE_HOME

# Allow port to accessed in networks
EXPOSE 8080

# Copy your verticle to the container
COPY --from=builder $VERTICLE_FILE $VERTICLE_HOME/

# Launch the verticle
ENTRYPOINT ["sh", "-c"]
CMD ["exec vertx run $VERTICLE_NAME -cp $VERTICLE_HOME/*"]