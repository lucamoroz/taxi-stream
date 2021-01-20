# Usage
1. ⚙ Configure the desired paramters in `.env`.
2. ▶ Run `docker-compose up` 
3. ⌛ Wait a bit: data preparation can take a while the first time.
4. 🥳 Go to `localhost:8080` in your browser

# Team Members
- 01427811 Bulatovic Djordje
- 01409784 Milasus Edzus
- 12016307 Moroldo Luca
- 01634877 Riegler Maximilian
- 01650767 Wolkowitsch Manuel 

# Work Distribution
- Maximilian Riegler
    - Docker setup
    - Storm setup
    - Integration of Kafka spout into storm topology
    - CalculateDistanceBolt
    - CalculateSpeedBolt
    - Dashboard (supporting Manuel Wolkowitsch)

- Manuel Wolkowitsch
    - Docker setup
    - Storm setup
    - CalculateSpeedBolt
    - Majority of dashboard (backend and frontend)

- Djordje Bulatovic
    - Docker setup
    - Storm setup
    - Kafka setup
    - NotifyBolts dashboard integration
    - Performance metrics
    - Performance optimizations
  
- Luca Moroldo
    - Data provider
    - Redis
    - Redis-Storm integration (Redis bolts)
    - Redis-Backend integration (Keyspace notifications sub)
    - Storm DistanceBolt
    - Storm throughput measurement
    - Storm performance optimiztion

- Edzus Milasus
    - Storm setup
    - Stateful Bolt implementation option analysis
    - Notify Speeding Bolt implementation
    - Notify Leaving Area Bolt implementation
    - Kafka Spout integration
    - Websocket implementation for Notify Bolts
    - Performance optimization and metrics

# Configuration

## Environment
Create a .env file containing the variables:
- `TAXI_DATASET`: the value must be the path to the directory containing the taxi logs.
- `MAX_TAXIS`: maximum number of distinct taxis that will be emitted by the data provider (`-1` to disable).
- `SPEED_MULTIPLIER`: changes the emission speed of the data provider. With `SPEED_MULTIPLIER=2` the emission speed is doubled.
- (optional) `MODE=DEBUG` to enable Storm throughput measurement

For example, if your dataset is located at `/aic/g1`, create the following `.env` file in the project root directory:
```
TAXI_DATASET=/aic/g1
MAX_TAXIS=100
SPEED_MULTIPLIER=1
```

# Technologies used
- Container Management
    - Docker
    - Docker Compose
- Streaming
    - Apache Storm
    - Apache Kafka
    - Java
- Data preparation and emission
    - Python
- Dashboard
    - NodeJS, NPM, Deno
    - React
    - WebSockets

# Use case

TODO: fill with uber use-case

# Optimizations
This section covers Task 5 (Stage 2).

The following optimizations have been applied:

1. The average speed bolt computes the incremental average speed, using O(1) memory per taxi. (+2% throughput)
2. ...

## Results
The results on the throughput and the processing time are the following:
- Throughput: from ... to ...
- Processing time: 
