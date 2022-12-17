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
        
    with open("user/" + user_id + ".json", "r") as f:
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
        with open("user/" + user_id + ".json", "r") as f:
            json_object = json.load(f)
            latlng = json_object["latlng"]
            type_of_user = json_object["type"]

    data['latlng'] = data.get("latlng", latlng)
    data['type'] = data.get("type", type_of_user)
    data['cred_file'] = 'credential_test.json'

    with open("user/" + user_id + ".json", "w") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
            
    return {}

@app.route('/getState', methods=["POST"])
def get_state():
    data = json.loads(request.get_data())
    score = opportune_moment.get_total_score(data["user_id"], data["curr_loc"])

    return jsonify({"state": 1, "score": score})


if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0', port=80)
