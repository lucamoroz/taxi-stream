import sys
import argparse
import os

from taxi_log_producer import TaxiLogProducer


def main(args=None):
    if not args:
        args = parse_args(sys.argv[1:])
    if not os.path.isfile(args.sorted_logs):
        print("Sorted logs not found!")
        sort_logs(args.dataset, args.sorted_logs)

    print("Running with speed multiplier: ", args.speed_multiplier, " max taxi number: ", args.max_taxis)
    producer = TaxiLogProducer("kafka:9092", args.sorted_logs)
    producer.send_logs(args.speed_multiplier, args.max_taxis)


def parse_args(args):
    parser = argparse.ArgumentParser(description="Run user-defined commands on gesture recognition.")
    parser.add_argument("--dataset", help="path to dataset containing taxi logs.", type=str, required=True)
    parser.add_argument("--sorted_logs",
                        help="path to file containing all the logs sorted by time. It'll be created if it doesn't exist",
                        type=str, required=True)
    parser.add_argument("--speed_multiplier",
                        help="changes the data emission speed (e.g. with speed_multiplier=2, the speed is doubled)",
                        default=1, type=float)
    parser.add_argument("--max_taxis", help="maximum number of taxis to follow, -1 to disable", default=-1, type=int)

    return parser.parse_args(args)


def sort_logs(logs_dir: str, dest_path: str):
    """
    Sort logs by time and save results to dest_path.
    :param logs_dir: path to dataset directory
    :param dest_path: path where the sorted logs file will be saved.
    """
    import datetime
    import glob

    start = datetime.datetime.now()

    print("Reading records in " + logs_dir + ", this may take a while...")

    paths = glob.glob(logs_dir + "*.txt")
    records = []
    total_files = len(paths)
    curr_file = 1
    for p in paths:
        with open(p, "r") as f:
            for line in f:
                line = line.rstrip()
                taxi_id, time, lon, lat = line.split(',')
                time = datetime.datetime.strptime(time, '%Y-%m-%d %H:%M:%S')
                records.append(line.split(','))

        print("Read: {curr}/{tot}".format(curr=curr_file, tot=total_files))
        curr_file += 1


    print("Readed: ", len(records), " lines. Sorting...")

    records.sort(key=lambda x: x[1])

    print("Records sorted. Writing res to file: " + dest_path)

    f = open(dest_path, "w")
    for record in records:
        s = ','.join(record)
        f.write(s + '\n')

    f.close()

    print("Completed")

    end = datetime.datetime.now()
    elapsed = end - start
    print("Elapsed: ", elapsed)


if __name__ == "__main__":
    main()
