# Home API Service 
Home Stack API Service

## Build
### Set JAVA_HOME (in case mvn run through terminal)
```shell
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
```
### Build Application image
   ```shell
   protoc --proto_path=src --java_out=src/main/java src/main/resources/proto/getInvestmentsResponse.proto
   protoc --proto_path=src --java_out=src/main/java src/main/resources/proto/getInvestmentsRorMetricsResponse.proto
   protoc --proto_path=src --java_out=src/main/java src/main/resources/proto/getRawInvestmentsResponse.proto
   ```
   ```shell
   docker build --progress=plain -f Dockerfile.native -t alokkusingh/home-api-service:latest -t alokkusingh/home-api-service:2.0.0 .
   ```
   ```shell
   docker push alokkusingh/home-api-service:latest
   ```
   ```shell
   docker push alokkusingh/home-api-service:2.0.0
   ```
   ```shell
   docker run -p 8081:8081 --rm --name home-api-service alokkusingh/home-api-service
   ```
### Manual commands - go inside and run binary manually
```shell
docker run -it --entrypoint /bin/bash -p 8081:8081 --rm --name home-api-service alokkusingh/home-api-service
```
```shell
./home-api-service --java.security.egd=file:/dev/urandom --spring.profiles.active=prod --spring.datasource.url=jdbc:mysql://192.168.1.200:32306/home-stack \
--spring.datasource.hikari.minimum-idle=5 --spring.datasource.hikari.connection-timeout=20000 --spring.datasource.hikari.maximum-pool-size=10 \
--spring.datasource.hikari.idle-timeout=10000 --spring.datasource.hikari.max-lifetime=1000 --spring.datasource.hikari.auto-commit=true
```
```shell
docker run -p 8081:8081 --rm --name home-api-service alokkusingh/home-api-service --java.security.egd=file:/dev/urandom --spring.profiles.active=prod \
--spring.datasource.url=jdbc:mysql://192.168.1.200:32306/home-stack --spring.datasource.hikari.minimum-idle=5 --spring.datasource.hikari.connection-timeout=20000 \
--spring.datasource.hikari.maximum-pool-size=10 --spring.datasource.hikari.idle-timeout=10000 --spring.datasource.hikari.max-lifetime=1000 \
--spring.datasource.hikari.auto-commit=true
```

## MQTT Commands
### Root Certificate - for client signer and domain signer
```shell
openssl genrsa -des3 -out mqtt-signer-ca.key 2048
```
```shell
openssl req -x509 -new -nodes -key mqtt-signer-ca.key -sha256 -days 365 -out mqtt-signer-ca.crt -subj /C=IN/ST=KA/L=Bengalury/O=Home/CN=alok-signer
```
#### Client Cert - alok
```shell
openssl genrsa -out mqtt.client.alok.key 2048
```
```shell
openssl req -new -sha256 -key mqtt.client.alok.key -subj /C=IN/ST=KA/L=S=Bengaluru/O=Home/CN=alok -out mqtt.client.alok.csr
```
```shell
openssl x509 -req -in mqtt.client.alok.csr -CA mqtt-signer-ca.crt -CAkey mqtt-signer-ca.key -CAcreateserial -out mqtt.client.alok.crt -days 365 -sha256
```

####  Server Domain Cert - localhost
```shell
openssl genrsa -out server.key 2048
```
```shell
openssl req -new -sha256 -out server.csr -key server.key -subj /C=IN/ST=KA/L=S=Bengaluru/O=Home/CN=localhost
```
```shell
openssl x509 -req -in server.csr -CA mqtt-signer-ca.crt -CAkey mqtt-signer-ca.key -CAcreateserial -out server.crt -days 360 -sha256
```

#### Add client alok cert to PKCS 12 keystore - then it is imported in JKS using KeyStore Explorer
```shell
openssl pkcs12 -export -out mqtt.client.alok.p12 -name "alok" -inkey mqtt.client.alok.key -in mqtt.client.alok.crt
```

#### Start Mosquito Broker
```shell
/opt/homebrew/opt/mosquitto/sbin/mosquitto -c /opt/homebrew/etc/mosquitto/mosquitto.conf
```

#### Publish using alok cert
```shell
mosquitto_pub --cafile mqtt-signer-ca.crt --cert mqtt.client.alok.crt --key mqtt.client.alok.key -d -h localhost -p 8883 -t test -m "Hello" --tls-version tlsv1.2 --debug
```

#### Client Cert - rachna
```shell
openssl genrsa -out mqtt.client.rachna.key 2048
```
```shell
openssl req -new -sha256 -key mqtt.client.rachna.key -subj /C=IN/ST=KA/L=S=Bengaluru/O=Home/CN=rachna -out mqtt.client.rachna.csr
```
```shell
openssl x509 -req -in mqtt.client.rachna.csr -CA mqtt-signer-ca.crt -CAkey mqtt-signer-ca.key -CAcreateserial -out mqtt.client.rachna.crt -days 365 -sha256
```

#### Publish/Subscribe using rachna cert
```shell
mosquitto_sub --cafile mqtt-signer-ca.crt --cert mqtt.client.rachna.crt --key mqtt.client.rachna.key -d -h localhost -p 8883 -t home/stack/stmt-res --tls-version tlsv1.2 --debug
```
```shell
mosquitto_pub --cafile mqtt-signer-ca.crt --cert mqtt.client.rachna.crt --key mqtt.client.rachna.key -d -h localhost -p 8883 -t home/stack/stmt-req -m "Hello" --tls-version tlsv1.2 --debug
```

#### Request Topic
````
home/stack/stmt-req
````
#### Sample Request Payload
```json
{
"correlationId": "sdcsd1234",
"httpMethod": "GET",
"uri": "/fin/expense?yearMonth=current_month",
"body": ""
}
```
#### Test Rest API - localhost is excluded in security
```shell
curl --location 'http://localhost:8081/home/api/expense/sum_by_year' | jq .
```
```shell
curl --location 'http://localhost:8081/home/api/expense/sum_by_category_month' | jq .
```
```shell
curl --location 'http://localhost:8081/home/api/expense/sum_by_category_year' | jq .
```
```shell
curl --location 'http://localhost:8081/home/api/odion/accounts' | jq .
```
