import os
import sys

from taxi_log_producer import TaxiLogProducer

if __name__ == "__main__":
    print("Running with speed multiplier: ", sys.argv[1])
    # Docker renaming logs.txt ?
    os.system("ls ../data")
    # TODO setup network 
    producer = TaxiLogProducer("localhost:9092", "../data/taxi_log_by_name.txt")
    producer.send_logs(float(sys.argv[1]))

