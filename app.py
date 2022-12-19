from flask import Flask, request, jsonify
from flask_restx import Api, Resource
import json
import os
import opportune_moment
import logging

app = Flask(__name__)


def space_remove(s):
    return s.replace(" ", "").replace("\n", "")

@app.route('/getData', methods=["POST"]) # in : {"user_id"} // out : {"user_id", "latlng", "type"}
def get_data():
    data = json.loads(request.get_data())
    user_id = data["user_id"]
    if not os.path.exists("user/" + user_id + ".json"):
        return {"user_id" : user_id, "latlng" : [], "type" : "unknown", "is_emotion_detected" : "n", "push_to_partner" : "n"}
        
    with open("user/" + user_id + ".json", "r", encoding ="cp949") as f:
        json_object = json.load(f)

    del(json_object["cred_file"])
    
    return jsonify(json_object)

@app.route('/setData', methods=["POST"]) # in : {"user_id", "latlng", "type"} // out : {"user_id", "latlng", "type"}
def set_data():
    data = json.loads(request.get_data())

    user_id = data["user_id"]

    latlng = []
    type_of_user = "unknown"
    is_emotion_detected = "n"
    push_to_partner = "n"

    if os.path.exists("user/" + user_id + ".json"):
        with open("user/" + user_id + ".json", "r", encoding ='cp949') as f:
            json_object = json.load(f)
            latlng = json_object["latlng"]
            type_of_user = json_object["type"]
            is_emotion_detected = json_object["is_emotion_detected"]
            push_to_partner = json_object["push_to_partner"]


    data['latlng'] = data.get("latlng", latlng)
    data['type'] = data.get("type", type_of_user)
    data['cred_file'] = 'credential_test.json'
    data["is_emotion_detected"] = data.get("is_emotion_detected", is_emotion_detected)
    data["push_to_partner"] = data.get("push_to_partner", push_to_partner)


    with open("user/" + user_id + ".json", "w", encoding ='cp949') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
            
    return {}

@app.route('/getState', methods=["POST"])
def get_state():
    data = json.loads(request.get_data())
    score = opportune_moment.get_total_score(data["user_id"], data["curr_loc"])

    return jsonify({"state": 1, "score": score})

@app.route('/getPartnerState', methods=["GET"])
def get_partner_state():

    user_id = request.args.get("user_id", "test")
    with open("user/" + user_id + ".json", "r", encoding='cp949') as f:
        data = json.load(f)
        

    if data["push_to_partner"] == "y":
        state = 1
        data["push_to_partner"] = "n"
        data["is_emotion_detected"] = "n"
        with open("user/" + user_id + ".json", "w", encoding="cp949") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
    else:
        state = 0

    return jsonify({"state": state})


@app.route('/getEmotionData/<user_id>', methods=["GET"])
def get_emotion_data(user_id):
    datapath = f'emotion_data/{user_id}.json'

    if os.path.exists(datapath):
        with open(datapath, 'r') as f:
            _emotion_data = json.load(f)
        emotion_data = _emotion_data["records"]
        _emotion_data["records"].clear()
        with open(datapath, 'w') as f:
            json.dump(_emotion_data, f)
        return jsonify({"user_id": user_id,"emotion_data": emotion_data, "error": ""})

    else:
        return jsonify({"user_id": user_id, "emotion_data": [], "error": f'No user information: {user_id}'})

@app.route('/sendTargetNotificationData', methods=["POST"])
def send_target_notification_data():
    isDetected = "n"
    data = request.get_json()
    user_id = data["user_id"]
    timestamp = data["timestamp"]
    q1_answer = data["q1_answer"]
    q2_answer = data["q2_answer"]
    click_more_cnt = data["click_more_cnt"]

    score = 0

    if q1_answer == "eating":
        score += 0
    elif q1_answer == "working":
        score += 0.7
    elif q1_answer == "talking":
        score += 0.7
    elif q1_answer == "hobby":
        score += 0.5
    elif q1_answer == "moving":
        score += 0.2
    else:
        score += 0.5
    
    if q2_answer == "yes":
        score += 0.5
    elif q2_answer == "no":
        score += 0
    
    score += click_more_cnt * 0.3

    if score >= 1:
        isDetected = "y"

    savepath = f'user/{user_id}.json'
    if os.path.exists(savepath):
        with open(savepath, 'r', encoding="cp949") as f:
            user_data = json.load(f)
        user_data["is_emotion_detected"] = isDetected
        user_data["emotion_detect_time"] = timestamp
        with open(savepath, "w", encoding="cp949") as f:
            json.dump(f)
    else:
        logging.warn(f'{savepath} file does not exists.')
            
    return jsonify({"is_emotion_detected": isDetected})

if __name__ == "__main__":
    app.run(debug=False, host='0.0.0.0', port=5000)
