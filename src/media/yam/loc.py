import os

i=0
for (dir, subdirs, files) in os.walk('.'):
    for fs in files:
        f = open(fs)
        for l in f:
            i+=1

print i
raw_input()