# Team Members
- Bulatovic Djordje
- Milasus Edzus
- Moroldo Luca
- 01634877 Riegler Maximilian
- 01650767 Wolkowitsch Manuel 

# Work Distribution
- Docker setup
    - Maximilian Riegler
- Storm setup
    - Manuel Wolkowitsch
    - Maximilian Riegler
- AverageSpeedBolt
- CalculateDistanceBolt
    - Maximilian Riegler
- CalculateSpeedBolt
    - Manuel Wolkowitsch
    - Maximilian Riegler
- NotifyLeavingAreaBolt
- NotifySpeedingBolt
- UpdateLocationBolt
- Data preparation and emission
- Kafka spout
    - Maximilian Riegler
- Redis setup
- Redis dashboard integration
- NotifyBolt dashboard integration
- Dashboard
    - Manuel Wolkowitsch
    - Maximilian Riegler
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

1. ⚙ Configure the wanted paramters in `.env`.
2. ▶ Run `docker-compose up` 
3. ⌛ Wait a bit. This can take a while 
4. 🥳 Go to `localhost:8080` in your browser