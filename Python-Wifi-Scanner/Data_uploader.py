import requests
import csv
import json

print(requests.get("http://localhost:5000/").text)

with open("./data/Room3Data1.csv") as file:
	csv_reader = csv.reader(file)
	BSSID = next(csv_reader)
	SSID = next(csv_reader)
	scans = []
	for index,row in enumerate(csv_reader):
		scan = []
		for index, quality in enumerate(row):
			scan.append({"BSSID": str(BSSID[index]),
					"SSID":str(SSID[index]),
					"quality":int(quality)})
		scans.append(scan)
	payload = {"building_id":18,"room_id":14,"email":"lin.2453@osu.edu","scans":scans}
	print(requests.post("http://localhost:5000/scan",json=payload).text)

		