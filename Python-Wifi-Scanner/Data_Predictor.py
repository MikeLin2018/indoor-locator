from scanner import WIFI_Scanner
import requests

scanner = WIFI_Scanner()
scanner.scan(1)
scans = []
scan = []
for index, quality in enumerate(scanner.quality[0]):
    scan.append({"BSSID": scanner.bssid[index],"SSID":scanner.ssid[index],"quality":quality})
scans.append(scan)

payload = {"building_id": 18, "scans": scans}
print(payload)
print(requests.get("http://localhost:5000/predict", json=payload).text)