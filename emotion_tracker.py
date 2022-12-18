import json
from fitbit import Fitbit
import time
import logging
import scipy.stats
import datetime
import numpy as np
from sklearn.preprocessing import normalize

MINUTES = 60

logging.basicConfig(filename="emotion_tracker.log", level=logging.INFO)

class EmotionTracker:
    def __init__(self):
        self.fitbit = Fitbit()
        self.time_window = 15
        self.change_rate_thr = 0.5

        self.date = ""
        self.last_timestamp = ""
        self.corr_start_timestamp_list = []
        self.corr_buffer = []
        self.last_corr_index = 0

        self.act_calories_dict = {}
        self.act_distance_dict = {}
        self.act_steps_dict = {}

        self.time_list = []
        self.time_int_list = []
        self.hr_list = []
        self.act_calories_list = []
        self.act_distance_list = []
        self.act_steps_list = []
        self.filepath_dict = {
            "hr": "heartrate.json",
            "act_calories": "activity_calories.json",
            "act_distance": "activity_distance.json",
            "act_steps": "activity_steps.json",
            "act_elevation": "activity_elevation.json",
            "act_floors": "activity_floors.json",
        }

    def _reload_fitbit_data(self, date="today", g_hr="1min", g_act="1min"):
        self.fitbit.get_heartrate(date=date, granularity=g_hr)
        self.fitbit.get_activity(date=date, granularity=g_act, filepath=self.filepath_dict["act_calories"])
        self.fitbit.get_activity(date=date, resource="distance", granularity=g_act, filepath=self.filepath_dict["act_distance"])
        self.fitbit.get_activity(date=date, resource="steps", granularity=g_act, filepath=self.filepath_dict["act_steps"])
       
        _hr_list = []
        _act_calories_list = []
        _act_distance_list = []
        _act_steps_list = []
        _date = ""

        with open(self.filepath_dict["hr"], "r") as file:
            json_file = json.load(file)
            _hr_list = json_file["activities-heart-intraday"]["dataset"]
            _date = json_file["activities-heart"][0]["dateTime"]
        with open(self.filepath_dict["act_calories"], "r") as file:
            json_file = json.load(file)
            _act_calories_list = json_file["activities-calories-intraday"]["dataset"]
        with open(self.filepath_dict["act_distance"], "r") as file:
            json_file = json.load(file)
            _act_distance_list = json_file["activities-distance-intraday"]["dataset"]
        with open(self.filepath_dict["act_steps"], "r") as file:
            json_file = json.load(file)
            _act_steps_list = json_file["activities-steps-intraday"]["dataset"]

        self.time_list = [item["time"] for item in _hr_list]
        self.hr_list = [item["value"] for item in _hr_list]
        self.act_calories_dict = dict([(item["time"], {"level": item["level"], "value": item["value"], "mets": item["mets"]}) for item in _act_calories_list])
        self.act_distance_dict = dict([(item["time"], item["value"]) for item in _act_distance_list])
        self.act_steps_dict = dict([(item["time"], item["value"]) for item in _act_steps_list])

        if _date != self.date:
            self.act_calories_list.clear()
            self.act_distance_list.clear()
            self.act_steps_list.clear()
            self.date = _date
    
    def _update_data_cache(self):
        _new_index = 0
        if self.last_timestamp != "":
            _new_index = self.time_list.index(self.last_timestamp) + 1        
        
        if len(self.time_list) == _new_index:
            logging.info(f'{self.last_timestamp}: fitbit data is pending.')
            return
        
        self.last_timestamp = self.time_list[-1]
        print(_new_index, len(self.time_list))

        for i in range(_new_index, len(self.time_list)):
            self.act_calories_list.append(self.act_calories_dict[self.time_list[i]]["value"])
            self.act_distance_list.append(self.act_distance_dict[self.time_list[i]])
            self.act_steps_list.append(self.act_steps_dict[self.time_list[i]])
        
        for i in range(_new_index, len(self.time_list), self.time_window):
            start = i
            end = i + self.time_window
            if len(self.hr_list[start:end]) < 2:
                self.last_timestamp = self.time_list[i-1]
                break
            # _hr_cal_corr = scipy.stats.pearsonr(self.hr_list[start:end], self.act_calories_list[start:end])
            _hr_cal_corr = scipy.stats.pearsonr(normalize([np.array(self.hr_list[start:end])])[0], normalize([np.array(self.act_calories_list[start:end])])[0])
            
            logging.info(f'_hr_cal_corr: {_hr_cal_corr} at {self.time_list[i]}')
            if _hr_cal_corr == np.NAN:
                logging.error(f'No correlation value at {self.time_list[i]}.')
                continue
            self.corr_buffer.append(_hr_cal_corr.statistic)
            self.corr_start_timestamp_list.append(self.time_list[i])   
    
    def _check_emotion_peak(self):
        for i in range(self.last_corr_index, len(self.corr_buffer) - 1):
            _change_rate = self.corr_buffer[i + 1] / self.corr_buffer[i]
            if _change_rate < self.change_rate_thr:
                self._send_push(_change_rate, self.corr_start_timestamp_list[i + 1])
        self.last_corr_index = len(self.corr_buffer) - 1

    def _send_push(self, change_rate, start_timestamp):
        logging.info(f'Corr. changed about {change_rate} at {start_timestamp}.')

    def run(self, date="today", g_hr="1min", g_act="1min"):
        self._reload_fitbit_data(date, g_hr, g_act)
        self._update_data_cache()
        self._check_emotion_peak()

    def deploy_run(self, date="today", g_hr="1min", g_act="1min"):
        while(1):
            logging.info(f'Reloading app at {datetime.datetime.now()}.')
            self.run(date, g_hr, g_act)
            time.sleep(self.time_window * MINUTES)

if __name__ == "__main__":
    emotion_tracker = EmotionTracker()
    emotion_tracker.deploy_run()
        




