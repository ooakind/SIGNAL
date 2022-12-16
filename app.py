from flask import Flask, request, jsonify
from flask_restx import Api, Resource
import json
import os

app = Flask(__name__)


def space_remove(s):
    return s.replace(" ", "").replace("\n", "")

@app.route('/getData', methods=["POST"]) # in : {"user_id"} // out : {"user_id", "latlng", "type"}
def get_data():
    data = json.loads(request.get_data())
    user_id = data["user_id"]
    if not os.path.exists("user/" + user_id + ".json"):
        return {"user_id" : user_id, "latlng" : [], "type" : "unknown"}
        
    d = {"user_id" : user_id, "latlng" : []}
    with open("user/" + user_id + ".json", "r") as f:
        json_object = json.load(f)
    
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

    with open("user/" + user_id + ".json", "w") as f:
        json.dump(data, f, indent=2)
            
    return {}




if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0', port=80)
