from dataclasses import dataclass
from dataclasses_json import dataclass_json
import datetime as datetime


@dataclass_json
@dataclass
class TaxiLog:
    taxi_id: str
    datetime: datetime
    latitude: str
    longitude: str

    @staticmethod
    def parse_taxi_log(log: str):
        line = log.rstrip()
        taxi_id, time, lon, lat = line.split(',')
        time = datetime.datetime.strptime(time, '%Y-%m-%d %H:%M:%S')
        return TaxiLog(taxi_id, time, lon, lat)
