This project realizes a monitoring dashboard, which is updated in real-time based on spatial streaming data obtained from IoT devices. To provide information in real-time, data is processed by a sequence of different stream processing operators, such as filters and aggregators. These individual stream processing operators form a stream processing topology. For a more detailed description, please refer to the file Assignment.pdf.

# Usage
1. ⚙ Configure the desired paramters in `.env`.
2. ▶ Run `docker-compose up` 
3. ⌛ Wait a bit: data preparation can take a while the first time.
4. 🥳 Go to `localhost:8080` in your browser

# Team Members
- Bulatovic Djordje
- Milasus Edzus
- Moroldo Luca
- Riegler Maximilian
- Wolkowitsch Manuel 

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
One basic use-case for this application would be the monitoring of a vehicle fleet for a company like Uber. Each Uber driver relies heavily on the smartphone to provide them with pickup jobs and to display them the most efficient routes to take. The positional data can then be taken from each app and published into the kafka server that acts as a spout for the storm topology. While traversing through the topology this data is used in many different calculations that are then later displayed at the monitoring dashboard. Here the company can see each drivers location on a map, how many drivers there are in total and the total distance that has been driven. In addition, it shows notifications for drivers that left a certain radius from a specific center point and if someone exceeds a given speed limit. This dashboard can be a useful tool for companies like Uber to quickly evaluate e.g. the total coverage of a city and can help penalizing drivers that drive too fast and therefore provide a risk for the company.

# Optimizations
We measured the throughput and the processing time of the bolts. We took following measures:

 * The average speed bolt computes the incremental average speed, using O(1) memory per taxi. (+2% throughput)

 * We have tried to make a separate Bolt (StoreToRedisBolt) which stores the values to Redis. The point of the optimization
would be that this task was repetitive inside 3 Bolts (AverageSpeedBolt, CalculateDistanceBolt and UpdateLocationBolt).
We sent data from these Bolts into our new Bolt that stores the data to Redis, but we did not reach an optimization. The
average processing time of these bolts was not improved, therefore based on the data we collected we agreed to remove 
this extra Bolt.

The maximum emission speed that the implementation can handle, running on a Dell XPS 15 9570 laptop, is 80.
   
