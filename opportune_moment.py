import json
import math
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError
import datetime
import os.path


WEIGHT_LOCATION = 1
WEIGHT_SCHEDULE = 1
WEIGHT_PREF = 1

THRESHOLD = 0

SCOPES = ['https://www.googleapis.com/auth/calendar.readonly']
TOKEN_FILE = "token.json"



def cal_location_score(dist):
    scale = 1000
    return 1 / (scale * dist + 1)

def cal_schedule_score(time):
    zero_thres = 0.5
    scale = math.tanh(zero_thres)
    return math.tanh(time - zero_thres) / scale if time < 2 * zero_thres else 1

def cal_pref_score(pref, time):
    zero_thres = 2
    scale = math.tanh(zero_thres)
    if pref == "fast":
        return -math.tanh(time - zero_thres) / scale if time < 2 * zero_thres else -1
    else:
        return math.tanh(time - zero_thres) / scale if time < 2 * zero_thres else 1


def get_location_score(user_id, current_location):
    
    with open("user/" + user_id + ".json", "r", encoding ='cp949') as f:
        json_object = json.load(f)
    
    pos_score = 0
    neg_score = 0

    for ll in json_object["latlng"]:
        dist = math.sqrt((float(ll["lat"]) - current_location["lat"]) ** 2 + (float(ll["lng"]) - current_location["lng"]) ** 2)
        if ll["type"] == "pref":
            pos_score = max(cal_location_score(dist), pos_score)
        else:
            neg_score = max(cal_location_score(dist), neg_score)

    return pos_score - neg_score

def get_schedule_score(cred_file):
    creds = None
    # The file token.json stores the user's access and refresh tokens, and is
    # created automatically when the authorization flow completes for the first
    # time.
    if os.path.exists(TOKEN_FILE):
        creds = Credentials.from_authorized_user_file(TOKEN_FILE, SCOPES)
    # If there are no (valid) credentials available, let the user log in.
    if not creds or not creds.valid:
        if creds and creds.expired and creds.refresh_token:
            creds.refresh(Request())
        else:
            flow = InstalledAppFlow.from_client_secrets_file(
                cred_file, SCOPES)
            creds = flow.run_local_server(port=0)
        # Save the credentials for the next run
        with open(TOKEN_FILE, 'w') as token:
            token.write(creds.to_json())

    try:
        service = build('calendar', 'v3', credentials=creds)

        # Call the Calendar API
        now = datetime.datetime.utcnow().isoformat() + 'Z'  # 'Z' indicates UTC time
        print('Getting the upcoming 10 events')
        events_result = service.events().list(calendarId='primary', timeMin=now,
                                              maxResults=10, singleEvents=True,
                                              orderBy='startTime').execute()
        events = events_result.get('items', [])

        if not events:
            return 1

        # Prints the start and name of the next 10 events
        
        start = events[0]['start'].get('dateTime', events[0]['start'].get('date'))
        start = datetime.datetime.strptime(start[:-6], "%Y-%m-%dT%H:%M:%S")
        curr_time = datetime.datetime.now()
        diff = (start - curr_time).seconds / 3600
        
        if diff < 0: # On schedule
            return -1

        return cal_schedule_score(diff)
    

    except HttpError as error:
        print('An error occurred: %s' % error)
    
    return 1

def get_preference_score(preference, emotion_detected_time):
    curr_time = datetime.datetime.now()
    emotion_detected_datetime = datetime.datetime(*emotion_detected_time)
    diff = (curr_time - emotion_detected_datetime).seconds / 3600
    
    return cal_pref_score(preference, diff)


def get_total_score(user_id, curr_loc):

    with open("user/" + user_id + ".json", "r", encoding ='cp949') as f:
        user_info = json.load(f)

    location_score = get_location_score(user_info["user_id"], curr_loc)
    schedule_score = get_schedule_score(user_info["cred_file"]) 
    preference_score = get_preference_score(user_info["type"], [2022, 12, 19, 16, 10, 20])
    
    print(location_score, schedule_score, preference_score)

    total_score =  location_score * WEIGHT_LOCATION \
        + schedule_score * WEIGHT_SCHEDULE \
        + preference_score * WEIGHT_PREF
    print(total_score)

    if total_score > THRESHOLD:
        with open("user/" + user_id + ".json", "w", encoding ='cp949') as f:
            user_info["is_emotion_detected"] = "y"
            json.dump(user_info, f, indent=2, ensure_ascii=False)

    return total_score

if __name__ == "__main__":
    get_total_score("test", {"lat": 36.010843, "lng" : 129.327789})
