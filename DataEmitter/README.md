# Prerequisites
First we need to sort the logs into a single file.
This can be done running:

`python data/sort_by_time.py [PATH_TO_DATASET]`

where `PATH_TO_DATASET` is the path to the folder containing the `.txt` log files.
# Build
`docker build -t aic/dataemitter .`

# Run
` sudo docker run --mount type=bind,source="$(pwd)"/data/logs.txt,target=/data/logs.txt,readonly aic/dataemitter`

To change the speed of emission run:

`sudo docker run --mount type=bind,source="$(pwd)"/data/logs.txt,target=/data/logs.txt,readonly aic/dataemitter python main.py
 [SPEED_MULTIPLIER]`

Where `[SPEED_MULTIPLIER]` controls how fast the logs are emitted (e.g. with `SPEED_MULTIPLIER`=2 the speed is doubled).