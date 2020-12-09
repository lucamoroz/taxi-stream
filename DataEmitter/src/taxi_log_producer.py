from datetime import datetime
from time import sleep

from kafka import KafkaProducer

from taxi_log import TaxiLog


class TaxiLogProducer:

    CONST_TOPIC = "test"

    def __init__(self, server: str, logs_path: str):
        self.kafka_producer = \
            KafkaProducer(bootstrap_servers=server, value_serializer=str.encode, key_serializer=str.encode)
        self.logs_path = logs_path

    def send_logs(self, speed_multiplier: float, taxi_limit: int):
        """
        :param speed_multiplier: changes publishing speed, e.g. with speed_multiplier=2 it will publish logs two
                                 times faster
        :param taxi_limit: maximum number of taxis to follow. If taxi_limit=-1, then no limit is used.
        """

        taxis_to_follow = set()

        with open(self.logs_path, "r") as logs:

            prev_taxi_log = TaxiLog.parse_taxi_log(logs.readline())
            curr_taxi_log = prev_taxi_log

            start_processing_time = datetime.now()

            while True:
                # Filter log if necessary
                if taxi_limit > 0:
                    if len(taxis_to_follow) < taxi_limit:
                        taxis_to_follow.add(curr_taxi_log.taxi_id)

                    if curr_taxi_log.taxi_id in taxis_to_follow:
                        self._send_log(curr_taxi_log)
                else:
                    self._send_log(curr_taxi_log)

                prev_taxi_log = curr_taxi_log

                log = logs.readline()
                if log == '':
                    print("End of stram.")
                    return

                curr_taxi_log = TaxiLog.parse_taxi_log(log)

                # Sleep if next log is in the future
                time_delta = curr_taxi_log.datetime - prev_taxi_log.datetime
                if time_delta.seconds != 0:
                    now = start_processing_time
                    delta_processing_time = now - start_processing_time
                    secs_to_sleep = time_delta.seconds - delta_processing_time.seconds
                    if secs_to_sleep > 0:
                        sleep(secs_to_sleep / speed_multiplier)
                    else:
                        raise Exception("Logs not sorted by time (ascending).")
                    start_processing_time = datetime.now()

    def _send_log(self, log: TaxiLog):
        value = log.to_json()
        print("Sending: ", value)
        self.kafka_producer.send(topic=self.CONST_TOPIC, value=value, key=log.taxi_id)