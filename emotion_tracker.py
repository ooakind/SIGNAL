import json
from fitbit import Fitbit

class EmotionTracker:
    def __init__(self):
        self.fitbit = Fitbit()
        self.hr_list = []
        self.act_calories_list = []
        self.act_distance_list = []
        self.act_steps_list = []
        self.act_elevation_list = []
        self.act_floors_list = []
        self.filepath_dict = {
            "hr": "heartrate.json",
            "act_calories": "activity_calories.json",
            "act_distance": "activity_distance.json",
            "act_steps": "activity_steps.json",
            "act_elevation": "activity_elevation.json",
            "act_floors": "activity_floors.json",
        }

    def _reload_fitbit_data(self, date="today"):
        self.fitbit.get_heartrate(date=date, granularity="1min")
        self.fitbit.get_activity(date=date, granularity="1min", filepath=self.filepath_dict["act_calories"])
        self.fitbit.get_activity(date=date, resource="distance", granularity="1min", filepath=self.filepath_dict["act_distance"])
        self.fitbit.get_activity(date=date, resource="steps", granularity="1min", filepath=self.filepath_dict["act_steps"])
        self.fitbit.get_activity(date=date, resource="elevation", granularity="1min", filepath=self.filepath_dict["act_elevation"])
        self.fitbit.get_activity(date=date, resource="floors", granularity="1min", filepath=self.filepath_dict["act_floors"])

        with open(self.filepath_dict["hr"], "r") as file:
            json_file = json.load(file)
            self.hr_list = json_file["activities-heart-intraday"]["dataset"]
        with open(self.filepath_dict["act_calories"], "r") as file:
            json_file = json.load(file)
            self.act_calories_list = json_file["activities-calories-intraday"]["dataset"]
        with open(self.filepath_dict["act_distance"], "r") as file:
            json_file = json.load(file)
            self.act_distance_list = json_file["activities-distance-intraday"]["dataset"]
        with open(self.filepath_dict["act_steps"], "r") as file:
            json_file = json.load(file)
            self.act_steps_list = json_file["activities-steps-intraday"]["dataset"]
        with open(self.filepath_dict["act_elevation"], "r") as file:
            json_file = json.load(file)
            self.act_elevation_list = json_file["activities-elevation-intraday"]["dataset"]
        with open(self.filepath_dict["act_floors"], "r") as file:
            json_file = json.load(file)
            self.act_floors_list = json_file["activities-floors-intraday"]["dataset"]

    def _is_activity(self):
        pass
    
    def _check_emotion_peak(self):
        pass

    def run(self, date="today"):
        self._reload_fitbit_data(date)

if __name__ == "__main__":
    emotion_tracker = EmotionTracker()
    emotion_tracker.run()
        




