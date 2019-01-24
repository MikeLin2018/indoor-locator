from scanner import WIFI_Scanner, CSV_Writer, CSV_Manager


# Sample 1: Use scanner to scan and print 1 data point
# scanner = WIFI_Scanner()
# scanner.print_access_point_data()

# Sample 2: Use scanner to scan 5 times and print the collected data
# scanner = WIFI_Scanner()
# scanner.scan(5)
# scanner.print()

# Sample 3: Write 100 scanned data into CSV file
# scanner = WIFI_Scanner()
# scanner.scan(100)
# csv_writer = CSV_Writer('./data/test.csv')
# csv_writer.write_WIFI_data(*scanner.report())
# csv_writer.close()

# Other:
# CSV_Manager.merge_WIFI_data('./data/Room1Data5.csv','./data/Room2Data1.csv')
# CSV_Manager.remove_zero('./data/Room2Data1.csv')
# CSV_Manager.add_room('./data/Room2Data1 remove-zero.csv')
# CSV_Manager.merge_WIFI_data('./data/Room1Data5 remove-zero add-room-num and Room2Data1 remove-zero add-room-num merged.csv','./data/Room3Data1 remove-zero add-room-num.csv')
# CSV_Manager.remove_header('./data/Room1Data5 remove-zero add-room-num and Room2Data1 remove-zero add-room-num merged and Room3Data1 remove-zero add-room-num merged.csv')
