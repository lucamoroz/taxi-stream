import sys

from taxi_log_producer import TaxiLogProducer

if __name__ == "__main__":
    print("Running with speed multiplier: ", sys.argv[1])
    # TODO setup network 
    producer = TaxiLogProducer("kafka:9092", "../data/logs.txt")
    producer.send_logs(float(sys.argv[1]))

