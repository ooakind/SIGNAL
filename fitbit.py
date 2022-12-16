import os
import json
import logging
import requests

class Fitbit:
    def __init__(self):
        if os.path.exists("fitbit_client_info.json"):
            with open("fitbit_client_info.json", "r") as json_file:
                info = json.load(json_file)
                self.access_token = info["access_token"]
                self.userid = info["userid"]
            if self.access_token == "" or self.userid == "":
                logging.error("Please fill in the fields 'access_token' and 'userid' in 'fitbit_client_info.json' and try again.") 
                exit(1)   
        else:
            logging.error("Please fill in the fields 'access_token' and 'userid' in 'fitbit_client_info.json' and try again.")
            with open("fitbit_client_info.json", "w") as json_file:
                json.dump({"access_token": "", "userid": ""}, json_file)
            exit(1)

    def get_heartrate(self, date="today", period="1d", granularity="15min", filepath="heartrate.json"):
        uri = f'https://api.fitbit.com/1/user/{self.userid}/activities/heart/date/{date}/{period}/{granularity}.json'
        res = requests.get(uri, headers={"Authorization": f'Bearer {self.access_token}'})
        with open(filepath, 'w') as json_file:
            json.dump(res.json(), json_file)

    def get_activity(self, date="today", period="1d", granularity="15min", resource="calories", filepath="activity.json"):
        uri = f'https://api.fitbit.com/1/user/{self.userid}/activities/{resource}/date/{date}/{period}/{granularity}.json'
        res = requests.get(uri, headers={"Authorization": f'Bearer {self.access_token}'})
        with open(filepath, 'w') as json_file:
            json.dump(res.json(), json_file)
    
    # Get a list of all fitbit activities.
    def get_activity_all(self, filepath="activity_all.json"):
        uri = f'https://api.fitbit.com/1/activities.json'
        res = requests.get(uri, headers={"Authorization": f'Bearer {self.access_token}'})
        with open(filepath, 'w') as json_file:
            json.dump(res.json(), json_file)
    
    def get_activity_test(self, uri, filepath="activity_test.json"):
        res = requests.get(uri, headers={"Authorization": f'Bearer {self.access_token}'})
        with open(filepath, 'w') as json_file:
            json.dump(res.json(), json_file)        


