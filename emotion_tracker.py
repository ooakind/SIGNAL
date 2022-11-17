import requests
import json
from fitbit import Fitbit

class EmotionTracker:
    def __init__(self):
        self.fitbit = Fitbit()

    def _reload_fitbit_data(self):
        self.fitbit.get_heartrate()
        self.fitbit.get_activity()
    
    def _is_activity(self):
        pass
    
    def _check_emotion_peak(self):
        pass

    def run(self):
        # Keep check if emotional peak is detected.
        self._reload_fitbit_data()

if __name__ == "__main__":
    emotion_tracker = EmotionTracker()
    emotion_tracker.run()
        




