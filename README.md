## Intoit Bank Account Balance Exercise
#### Author: Tamir Mayblat

### Instructions:
* Make sure you have a working Kafka with Zookeeper (basic config.).
* You can set Kafka url in the application.properties file.
* You can use Confluent Kafka Platform as describe below to create and run a local Kafka. 


### Confluent Kafka Platform

version used: 6.0.0

https://www.confluent.io/product/confluent-platform/

https://docs.confluent.io/platform/current/quickstart/ce-quickstart.html#ce-quickstart

### Needed Topics

Once Kafka is running, please create the following topics: 

```
intobank.command
intobank.account
intobank.account.accepted
intobank.account.refused
intobank.account.read
intobank.transfer
```

### Build the Application
Java 8 (or above), Maven 3.x.x

`mvn clean package`

### Running the Application
`java -jar ./target/intobank-1.0-spring-boot.jar`

Wait until server starts... 
```
Server online at http://localhost:8080/
Press RETURN to stop...
```

### Testing the Application

#### Reading an Account Balance

``` 
curl -X GET \
  http://localhost:8080/account/ACC-120 \
  -H 'Content-Type: application/json' \
```
* Expected response should look like this:

``` 
{
    "command": {
        "account": "ACC-120"
    },
    "id": "932abv-0a45-3221-a4o9-7a6d35bb8c0c",
    "response": {
        "timestamp":1609868114000
        "status": "READ_ACCEPTED",
        "balance": 0
    }
}
```
#### Credit/Debit an Account (should add value of 852 to Balance)

``` 
curl -X POST \
  http://localhost:8080/command \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "932abv-0a45-3221-a4o9-7a6d35bb8c0c",
  "type": "CREDIT",
  "command": {
    "account": "ACC-120",
    "value": 852
  }
}'
```

``` 
curl -X POST \
  http://localhost:8080/command \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "932abv-0a45-3221-a4o9-7a6d35bb8c0c",
  "type": "DEBIT",
  "command": {
    "account": "ACC-125",
    "value": 234
  }
}'
```
