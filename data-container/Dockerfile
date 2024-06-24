# Build as `docker build -t sample-agent-container-image .`
FROM openjdk:19-slim
ADD ./data /app/data
ADD ./target/appassembler/ /app
ENTRYPOINT ["/app/bin/main-kt"]
