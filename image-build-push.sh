#!/usr/bin/env bash
# Build and Publish
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

protoc --proto_path=src --java_out=src/main/java src/main/resources/proto/getInvestmentsResponse.proto
protoc --proto_path=src --java_out=src/main/java src/main/resources/proto/getInvestmentsRorMetricsResponse.proto
protoc --proto_path=src --java_out=src/main/java src/main/resources/proto/getRawInvestmentsResponse.proto

docker build --progress=plain -f Dockerfile.native -t alokkusingh/home-api-service:latest -t alokkusingh/home-api-service:2.0.0 .
docker push alokkusingh/home-api-service:latest
docker push alokkusingh/home-api-service:2.0.0
