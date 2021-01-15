# Team Members
- Bulatovic Djordje
- Milasus Edzus
- Moroldo Luca
- Riegler Maximilian
- 01650767 Manuel Wolkowitsch

# Work Distribution
- Storm setup
- AverageSpeedBolt
- CalculateSpeedBolt
- NotifyLeavingAreaBolt
- NotifySpeedingBolt
- UpdateLocationBolt
- Data preparation and emission
- Kafka spout
- Redis setup
- Redis dashboard integration
- NotifyBolt dashboard integration
- Dashboard
- Performance metrics
- Performance optimizations

# Configuration

## Environment
Create a .env file containing the variable `TAXI_DATASET`, the value must be the path to the directory containing the taxi logs.

For example, if your dataset is located at `/aic/g1`, create the following `.env` file in the project root directory:
```
TAXI_DATASET=/aic/g1
```

# Technologies used
- Container Management
    - Docker
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

1. ⚙ Configure the wanted paramters in `.env`.
2. ▶ Run `docker-compose up` 
3. ⌛ Wait a bit. This can take a while 
4. 🥳 Go to `localhost:8080` in your browser