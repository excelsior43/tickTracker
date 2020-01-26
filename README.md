= Tick Transfer Application

image:https://github.com/excelsior43/tickTracker/blob/master/TickTracker.jpg[link="https://github.com/excelsior43/tickTracker/blob/master/TickTracker.jpg"]

== Assumptions 

1. Past ticks with timestamp less than 60 seconds are ignored with HTTP No Content status (204) and NOT considered for statistics aggrigation calculations.
2. It exposes 3 service mappings
- /ticks that is a sole input end point into the system
- /statistics displays aggrigated data on all ticks
- /statistics/{instrument} displays instrument specific data on the supplied instrument.

== Building

To launch your tests:
```
./mvnw clean test

```

To package application:
```
./mvnw clean package

```

```
## Tick capture Service
- Application is available here http://localhost:8080/ticks
- Ticks can be posted to it in parallel. 
###### Request
```json
   {
      "instrument": "Google",
      "price": 100.3,
      "timestamp": 12345678
   }
```
###### Response
Status code of 200 indicate that the data is consumed
Status code of 200 indicate that the data is not considered

## Aggrigated Ticks aggrigation

#### Create Account  GET : http://localhost:8080/statistics

###### Response
```json
   {
     "count": "1",
     "max": "100.1",
     "min": "100.1",
     "avg": "100.1",
   }
```


== Run the application normally

```
java -cp target/tickTracker-0.0.1-SNAPSHOT.jar  
```

