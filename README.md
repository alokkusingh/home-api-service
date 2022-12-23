# Bank Account Statement Reader
Spring Boot Batch Processor

### How to run
````
````

#### Build base Image
   ````
   docker buildx build --platform linux/arm64 --build-arg GRAALVM_VERSION=22.3.0 --build-arg JAVA_VERSION=java17 -t alokkusingh/graalvm-ce:22.3.0-java17-arm64 --output=type=docker -f Dockerfile.ol8-java17 .
   ````
   ````
   
   ````
#### Build Application image
   ````
   docker build --progress=plain -f Dockerfile.native -t alokkusingh/home-api-service:latest -t alokkusingh/home-api-service:1.0.0 .
   ````
   ````
   docker push alokkusingh/home-api-service:latest
   ````
   ````
   docker push alokkusingh/home-api-service:1.0.0
   ````
   ````
   docker run -d -p 8081:8081 --rm --name home-api-service alokkusingh/home-api-service
   ````
### Manual commands - go inside and run binary manually
````
docker run -it --entrypoint /bin/bash -p 8081:8081 --rm --name home-api-service alokkusingh/home-api-service
````
````
java -Djava.security.egd=file:/dev/urandom -Dspring.profiles.active=prod -Dspring.datasource.url=jdbc:mysql://192.168.0.200:32306/home-stack -Dspring.datasource.hikari.minimum-idle=5 -Dspring.datasource.hikari.connection-timeout=20000 -Dspring.datasource.hikari.maximum-pool-size=10 -Dspring.datasource.hikari.idle-timeout=10000 -Dpring.datasource.hikari.max-lifetime=1000 -Dspring.datasource.hikari.auto-commit=true -jar /opt/app.jar
````
````
docker run -p 8081:8081 --rm --name home-api-service alokkusingh/home-api-service --java.security.egd=file:/dev/urandom --spring.profiles.active=prod --spring.datasource.url=jdbc:mysql://192.168.1.200:32306/home-stack --spring.datasource.hikari.minimum-idle=5 --spring.datasource.hikari.connection-timeout=20000 --spring.datasource.hikari.maximum-pool-size=10 --spring.datasource.hikari.idle-timeout=10000 --pring.datasource.hikari.max-lifetime=1000 --spring.datasource.hikari.auto-commit=true
````

## MQTT Commands
### Root Certificate - for client signer and domain signer
````
openssl genrsa -des3 -out mqtt-signer-ca.key 2048
````
````
openssl req -x509 -new -nodes -key mqtt-signer-ca.key -sha256 -days 365 -out mqtt-signer-ca.crt -subj /C=IN/ST=KA/L=Bengalury/O=Home/CN=alok-signer
````
#### Client Cert - alok
````
openssl genrsa -out mqtt.client.alok.key 2048
````
````
openssl req -new -sha256 -key mqtt.client.alok.key -subj /C=IN/ST=KA/L=S=Bengaluru/O=Home/CN=alok -out mqtt.client.alok.csr
````
````
openssl x509 -req -in mqtt.client.alok.csr -CA mqtt-signer-ca.crt -CAkey mqtt-signer-ca.key -CAcreateserial -out mqtt.client.alok.crt -days 365 -sha256
````

####  Server Domain Cert - localhost
````
openssl genrsa -out server.key 2048
````
````
openssl req -new -sha256 -out server.csr -key server.key -subj /C=IN/ST=KA/L=S=Bengaluru/O=Home/CN=localhost
````
````
openssl x509 -req -in server.csr -CA mqtt-signer-ca.crt -CAkey mqtt-signer-ca.key -CAcreateserial -out server.crt -days 360 -sha256
````

#### Add client alok cert to PKCS 12 keystore - then it is imported in JKS using KeyStore Explorer
````
openssl pkcs12 -export -out mqtt.client.alok.p12 -name "alok" -inkey mqtt.client.alok.key -in mqtt.client.alok.crt
````

#### Start Mosquito Broker
````
/opt/homebrew/opt/mosquitto/sbin/mosquitto -c /opt/homebrew/etc/mosquitto/mosquitto.conf
````

#### Publish using alok cert
````
mosquitto_pub --cafile mqtt-signer-ca.crt --cert mqtt.client.alok.crt --key mqtt.client.alok.key -d -h localhost -p 8883 -t test -m "Hello" --tls-version tlsv1.2 --debug
````

#### Client Cert - rachna
````
openssl genrsa -out mqtt.client.rachna.key 2048
````
````
openssl req -new -sha256 -key mqtt.client.rachna.key -subj /C=IN/ST=KA/L=S=Bengaluru/O=Home/CN=rachna -out mqtt.client.rachna.csr
````
````
openssl x509 -req -in mqtt.client.rachna.csr -CA mqtt-signer-ca.crt -CAkey mqtt-signer-ca.key -CAcreateserial -out mqtt.client.rachna.crt -days 365 -sha256
````

#### Publish/Subscribe using rachna cert
````
mosquitto_sub --cafile mqtt-signer-ca.crt --cert mqtt.client.rachna.crt --key mqtt.client.rachna.key -d -h localhost -p 8883 -t home/stack/stmt-res --tls-version tlsv1.2 --debug
````
````
mosquitto_pub --cafile mqtt-signer-ca.crt --cert mqtt.client.rachna.crt --key mqtt.client.rachna.key -d -h localhost -p 8883 -t home/stack/stmt-req -m "Hello" --tls-version tlsv1.2 --debug
````

#### Request Topic
````
home/stack/stmt-req
````
#### Sample Request Payload
````
{
"correlationId": "sdcsd1234",
"httpMethod": "GET",
"uri": "/fin/expense?yearMonth=current_month",
"body": ""
}
````