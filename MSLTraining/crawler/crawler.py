'''
Created on Sep 3, 2015

@author: caominhvu
'''
import csv
import re
import urllib.request


def readcsv():
    with open('data_all.csv', 'rt') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            url = row['colpushtext_image']
            name = row['name_value']
            print(url)
            if len(url) > 0:
                if "png" in url:
                    tail = ".png"
                elif "jpg" in url:
                    tail = ".jpg"
                elif "gif" in url:
                    tail = ".gif"
                
                name = name.replace('/', "")
                urllib.request.urlretrieve(url, "data/" + name + tail)

if __name__ == '__main__':
    readcsv()
    pass