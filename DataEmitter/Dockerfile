# first stage
FROM python:3.8 AS builder
COPY requirements.txt .

# install dependencies to the local user directory (eg. /root/.local)
RUN pip install --user -r requirements.txt

# second unnamed stage
FROM python:3.8-slim
WORKDIR /code

# copy only the dependencies installation from the 1st stage image
COPY --from=builder /root/.local /root/.local
COPY src .
#ADD data/logs.txt /data/taxi_log_by_name.txt

# update PATH environment variable
ENV PATH=/root/.local:$PATH

# wait-for-it
RUN apt-get update
RUN apt-get -y install bash curl
RUN curl https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh > wait-for-it.sh
RUN chmod +x ./wait-for-it.sh

# start app
CMD bash -c "./wait-for-it.sh -t 0 ${WAIT_FOR} -- python -u ./main.py --dataset /dataset/ --sorted_logs /data/logs.txt --speed_multiplier ${SPEED_MULTIPLIER} --max_taxis ${MAX_TAXIS}"