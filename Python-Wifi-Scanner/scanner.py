import os
from access_points import get_scanner
import csv
import re

class WIFI_Scanner:
    def __init__(self):
        self.wifi_scanner = get_scanner()
        self.bssid = []
        self.ssid = []
        self.quality = []

    # Print access points bssid, quality and ssid
    def print_access_point_data(self):
        for access_points in self.wifi_scanner.get_access_points():
            print(access_points.bssid,access_points.ssid,access_points.quality)

    # Scan the access point data for num times
    def scan(self, num=1):
        for index in range(0,num):
            # Get Access Point Data
            aps = self.wifi_scanner.get_access_points()
            print(aps)
            # Initialize this scan
            this_quality = [0]*len(self.bssid)
            for ap in aps:
                if ap.bssid not in self.bssid:
                    # If is not a seen AP
                    self.bssid.append(ap.bssid)
                    self.ssid.append(ap.ssid)
                    this_quality.append(ap.quality)
                else:
                    this_quality[self.bssid.index(ap.bssid)] = ap.quality
            # Make Update
            self.quality.append(this_quality)
            # Print Progress
            print('Scanning: {0}/{1}, {2:.1f}%'.format(index + 1, num, (index + 1) / num * 100))
        self._fill_with_zero()

    # Fill the quality data with 0 for missing data
    def _fill_with_zero(self):
        for quality in self.quality:
            if len(quality)<len(self.quality[-1]):
                quality+=[0]*(len(self.quality[-1])-len(quality))

    # Print the collected bssid, ssid, and quality data
    def print(self):
        print(self.bssid)
        print(self.ssid)
        for quality in self.quality:
            print(quality)

    # Return the bssid, ssid, and quality
    def report(self):
        return self.bssid, self.ssid, self.quality

    # Match scanned header from csv files
    # Will not update self.quality
    def report_with_header_from_csv(self,directory,has_room_num=True):
        # Get Header
        with open(directory, newline="") as file:
            csv_reader = csv.reader(file, delimiter=',')
            header = list(next(csv_reader))
        # Remove room number
        if has_room_num:
            header = header[1:]
        # Create zero matrix
        data = [[0]*len(header)]*len(self.quality)
        # Fill in data
        for bssid_index, bssid in enumerate(self.bssid):
            if bssid in header:
                for quality_index in range(0,len(self.quality)):
                    data[quality_index][header.index(bssid)] = self.quality[quality_index][bssid_index]
        return data,header

class CSV_Writer:
    # Constructor
    def __init__(self,csvDirectory,mode='w+'):
        self.csv_file = open(csvDirectory, mode, newline="")
        self.csv_writer = csv.writer(self.csv_file, delimiter=",")
        print("Output:",csvDirectory)

    def write_row(self,row):
        self.csv_writer.writerow(row)

    def write_rows(self,rows):
        for row in rows:
            self.write_row(row)

    def close(self):
        self.csv_file.close()

    # write WIFI data from *scanner.report()
    def write_WIFI_data(self,bssid,ssid,quality):
        self.write_row(bssid)
        self.write_row(ssid)
        self.write_rows(quality)

class CSV_Manager:
    def __init__(self):
        pass

    @staticmethod
    def merge_WIFI_data(directory1,directory2):
        with open(directory1,newline="") as file1:
            with open(directory2,newline="") as file2:
                # Read bssid and ssid from file1 and file2
                file1_content = csv.reader(file1, delimiter=',')
                file1_bssid = next(file1_content)
                file1_ssid = next(file1_content)
                file2_content = csv.reader(file2, delimiter=',')
                file2_bssid = next(file2_content)
                file2_ssid = next(file2_content)
                # Create csv_writer
                path = os.path.dirname(directory1)
                directory1 = os.path.basename(directory1).replace('.csv','')
                directory2 = os.path.basename(directory2).replace('.csv','')
                csv_writer = CSV_Writer(path+'/'+f'{directory1} and {directory2} merged.csv')
                # Merge bssids and create index_map
                merged_bssid = list(file1_bssid)
                merged_ssid = list(file1_ssid)
                index_map = {} # Map merged_bssid index to file2_bssid index
                for index, bssid in enumerate(file2_bssid):
                    if bssid not in merged_bssid:
                        merged_bssid.append(bssid)
                        merged_ssid.append(file2_ssid[index])
                    index_map[merged_bssid.index(bssid)] = index
                csv_writer.write_row(merged_bssid)
                csv_writer.write_row(merged_ssid)
                # Output file1 WIFI qualities directly
                for row in file1_content:
                    row+=[0]*(len(merged_bssid)-len(file1_bssid))
                    csv_writer.write_row(row)
                # Output file2 WIFI qualities according to index_map
                for row in file2_content:
                    this_row = []
                    for index in range(0,len(merged_bssid)):
                        if index in index_map.keys():
                            # meaning: file2 also has signal from that bssid
                            this_row.append(row[index_map[index]])
                        else:
                            this_row.append(0)
                    csv_writer.write_row(this_row)

    # Remove zero rows that contains only zeros (invalid data)
    @staticmethod
    def remove_zero(directory):
        with open(directory, newline="") as file:
            csv_reader = csv.reader(file, delimiter=',')
            csv_writer = CSV_Writer(directory.replace('.csv','')+" remove-zero.csv")
            for row in csv_reader:
                row_list = list(row)
                if row_list.count('0')!=len(row_list):
                    csv_writer.write_row(row_list)

    # Add room number to csv data
    # if no room_num provided, then imply from filename
    @staticmethod
    def add_room(directory,room_num=-1):
        if room_num==-1:
            room_num = re.search('Room\d+',directory).group(0).replace("Room","")
        with open(directory, newline="") as file:
            csv_reader = csv.reader(file, delimiter=',')
            csv_writer = CSV_Writer(directory.replace('.csv','') + " add-room-num.csv")
            row1 = ["room_num"]+list(next(csv_reader))
            row2 = ["room_num"]+list(next(csv_reader))
            csv_writer.write_row(row1)
            csv_writer.write_row(row2)
            for row in csv_reader:
                row_list = [room_num]+list(row)
                csv_writer.write_row(row_list)

    # Remove header of csv data
    @staticmethod
    def remove_header(directory, header_len=2):
        with open(directory, newline="") as file:
            csv_reader = csv.reader(file, delimiter=',')
            csv_writer = CSV_Writer(directory.replace('.csv','') + " remove-header.csv")
            for _ in range(0,header_len):
                next(csv_reader)
            for row in csv_reader:
                csv_writer.write_row(row)

    # Get header from csv data
    @staticmethod
    def get_header(directory):
        # Get Header
        with open(directory, newline="") as file:
            csv_reader = csv.reader(file, delimiter=',')
            return list(next(csv_reader))