from flask import Flask, request, jsonify
from flask_restx import Api, Resource
import json
import os

app = Flask(__name__)



@app.route('/getdata', methods=["POST"])
def get_data():
    data = json.loads(request.get_data())
    user_id = data["user_id"]
    if not os.path.exists("user/" + user_id + ".txt"):
        return {}
        
    d = {"latlng" : []}
    with open("user/" + user_id + ".txt", "r") as f:
        line = f.readline()
        d["type"] = line

        while True:
            line = f.readline()
            if not line: break
            location_data = line.split(" ")
            latlng = {"lat" : location_data[0], "lng": location_data[1]}
            d["latlng"].append(latlng)
    return jsonify(d)

@app.route('/setdata', methods=["POST"])
def set_data():
    data = json.loads(request.get_data())
    user_id = data["user_id"]
    latlng = data["latlng"]
    type_of_user = data["type"]

    with open("user/" + user_id + ".txt", "w") as f:
        f.write(type_of_user + "\n")
        for ll in latlng:
            f.write(ll["lat"] + " " + ll["lng"])
    return {}




if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0', port=80)
