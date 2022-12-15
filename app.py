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
    if not os.path.exists("user/" + user_id + ".txt"):
        return {"user_id" : user_id, "latlng" : []}
        
    d = {"user_id" : user_id, "latlng" : []}
    with open("user/" + user_id + ".txt", "r") as f:
        line = f.readline()
        d["type"] = space_remove(line)

        while True:
            line = f.readline()
            if not line: break
            _type = space_remove(line)

            line = f.readline()
            if not line: break
            _name = line.replace("\n", "")

            line = f.readline()
            if not line: break
            _latlng = line.split(" ")
            
            latlng = {"type" : _type, "name" : _name, "lat": space_remove(_latlng[0]), "lng": space_remove(_latlng[1])}
            d["latlng"].append(latlng)
    print(d)
    return jsonify(d)

@app.route('/setData', methods=["POST"]) # in : {"user_id", "latlng", "type"} // out : {"user_id", "latlng", "type"}
def set_data():
    data = json.loads(request.get_data())

    user_id = data["user_id"]
    
    latlng = []
    type_of_user = "unknown"

    if os.path.exists("user/" + user_id + ".txt"):
        with open("user/" + user_id + ".txt", "r") as f:
            line = f.readline()
            type_of_user = space_remove(line)
            while True:
                line = f.readline()
                if not line: break
                _type = space_remove(line)

                line = f.readline()
                if not line: break
                _name = line.replace("\n", "")

                line = f.readline()
                if not line: break
                _latlng = line.split(" ")
                
                ll = {"type" : _type, "name" : _name, "lat": space_remove(_latlng[0]), "lng": space_remove(_latlng[1])}
                latlng.append(ll)

    latlng = data.get("latlng", latlng)
    type_of_user = data.get("type", type_of_user)

    with open("user/" + user_id + ".txt", "w") as f:
        f.write(type_of_user + "\n")
        for ll in latlng:
            f.write(ll["type"] + "\n") 
            f.write(ll["name"] + "\n")
            f.write(ll["lat"] + " " + ll["lng"] + "\n")
            
    return {}




if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0', port=80)
