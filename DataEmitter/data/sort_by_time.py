"""
Util script to sort taxi data by time and write all the records into one file.

"""

import datetime
import glob
import sys

start = datetime.datetime.now()

paths = glob.glob(sys.argv[1] + "*.txt")

records = []

print("Reading records...")

for p in paths:
    with open(p, "r") as f:
        for line in f:
            line = line.rstrip()
            taxi_id, time, lon, lat = line.split(',')
            time = datetime.datetime.strptime(time, '%Y-%m-%d %H:%M:%S')
            records.append(line.split(','))


print("Readed: ", len(records), " lines. Sorting...")

records.sort(key=lambda x: x[1])

print("Records sorted. Writing res to file...")

f = open("logs.txt", "w")
for record in records:
    s = ','.join(record)
    f.write(s + '\n')
    
f.close()

print("Completed")

end = datetime.datetime.now()
elapsed = end - start
print("Elapsed: ", elapsed)
