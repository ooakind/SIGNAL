import json
import math


WEIGHT_LOCATION = 1
WEIGHT_SCHEDULE = 1
WEIGHT_PREF = 1


def cal_location_score(dist):
    scale = 1000
    return 1 / (scale * dist + 1)

def cal_schedule_score(time):
    return 1 - 1 / (1 + time)

def cal_pref_score(pref, time):
    thres = 10
    if pref == "preference":
        return 1 if time < thres else 1 / (time - thres  + 1)
    else:
        return 1 if time > thres else 1 / (thres - time  + 1)


def get_location_score(user_id, current_location):
    
    with open("user/" + user_id + ".json", "r") as f:
        json_object = json.load(f)
    
    pos_score = 0
    neg_score = 0

    for ll in json_object["latlng"]:
        dist = math.sqrt((ll["lat"] - current_location["lat"]) ** 2 + (ll["lng"] - current_location["lng"]) ** 2)
        if ll["type"] == "pref":
            pos_score = max(cal_location_score(dist), pos_score)
        else:
            neg_score = max(cal_location_score(dist), neg_score)

    return pos_score - neg_score

def get_schedule_score(google_account):
    

    return

def get_preference_score(preference):
    
    return


def get_total_score(user_info):

    return get_location_score(user_info["user_id"], user_info["current_location"]) * WEIGHT_LOCATION \
        + get_schedule_score(user_info["google_account"]) * WEIGHT_SCHEDULE \
        + get_preference_score(user_info["preference"]) * WEIGHT_PREF
