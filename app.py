from flask import Flask, request, jsonify
from flask_restx import Api, Resource
import json
import os
import opportune_moment

app = Flask(__name__)


def space_remove(s):
    return s.replace(" ", "").replace("\n", "")

@app.route('/getData', methods=["POST"]) # in : {"user_id"} // out : {"user_id", "latlng", "type"}
def get_data():
    data = json.loads(request.get_data())
    user_id = data["user_id"]
    if not os.path.exists("user/" + user_id + ".json"):
        return {"user_id" : user_id, "latlng" : [], "type" : "unknown"}
        
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

    if os.path.exists("user/" + user_id + ".json"):
        with open("user/" + user_id + ".json", "r", encoding ='cp949') as f:
            json_object = json.load(f)
            latlng = json_object["latlng"]
            type_of_user = json_object["type"]

    data['latlng'] = data.get("latlng", latlng)
    data['type'] = data.get("type", type_of_user)
    data['cred_file'] = 'credential_test.json'

    with open("user/" + user_id + ".json", "w", encoding ='cp949') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
            
    return {}

@app.route('/getState', methods=["POST"])
def get_state():
    data = json.loads(request.get_data())
    score = opportune_moment.get_total_score(data["user_id"], data["curr_loc"])

    return jsonify({"state": 1, "score": score})

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
        return jsonify({"user_id": user_id, "emotion_data": "", "error": f'No user information: {user_id}'})

if __name__ == "__main__":
    app.run(debug=False, host='0.0.0.0', port=5000)
