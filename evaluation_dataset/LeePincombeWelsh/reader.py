# -*- coding: utf-8 -*-
import csv
import sys



fname = "LeePincombeWelshDocuments.txt"
fcsv = "LeePincombeWelshData.csv" 
with open(fname) as f:
	content = f.readlines()

fo = open(fcsv,'rb')
reader = csv.reader(fo)


reads = []
for row in reader:
	reads.append(row)

for row in reads[1:]:
	print row
	print content[int(row[1])-1]	
	print content[int(row[2])-1]
	raw_input()

