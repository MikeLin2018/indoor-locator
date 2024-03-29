import datetime
from flask import Flask, request
from flask_restful import Resource, Api, reqparse
import DB_objects
from sqlalchemy import exists, func, and_, update
from sklearn.model_selection import train_test_split
from sklearn.svm import LinearSVC
from sklearn.linear_model import LogisticRegression
import pickle
import numpy as np
import operator

# Initialize Flask App
app = Flask(__name__)
api = Api(app)

# Initialize Parser
parser = reqparse.RequestParser()
parser.add_argument('name', type=str, help="No username specified.")
parser.add_argument('password', type=str, help="No password specified.")
parser.add_argument('email', type=str, help="No email specified.")
database = DB_objects.database()


class HelloWorld(Resource):
    def get(self):
        return {'Hi': 'This is Mike!'}


class NewUser(Resource):
    # Add a new user
    def post(self):
        # Parse arguments
        args = parser.parse_args()

        # Check arguments exist
        if None in [args.name, args.password, args.email]:
            return Response.check_none_response([args.name, args.password, args.email],
                                                ['Username', 'Password', 'Email'])

        # Get Session
        session = database.DBSession()

        # Check email already exist
        if session.query(exists().where(DB_objects.User.email == args.email)).scalar():
            return Response(success=False, messages='Email Already Exist.').text()

        # Add new user
        try:
            new_user = DB_objects.User(name=args.name, email=args.email)
            new_user.hash_password(args.password)
            session.add(new_user)
            session.commit()
            return Response(success=True, messages='New user is created.',
                            data={"id": new_user.id, "name": new_user.name, "email": new_user.email}).text()
        except:
            return Response(success=False, messages='Unknown issue when adding a new user.').text()


class VerifyUser(Resource):
    # Verify User
    def post(self):
        # Parse arguments
        args = parser.parse_args()

        # Check arguments exist
        if None in [args.password, args.email]:
            return Response.check_none_response([args.password, args.email], ['Password', 'Email']).text()

        # Get Session
        session = database.DBSession()

        # Get user tuple from database
        user = session.query(DB_objects.User).filter(DB_objects.User.email == args.email).first()

        # Check user exist
        if user is None:
            return Response(success=False, messages="User is not found.").text()

        # Verify user password
        if user.verify_password(args.password):
            return Response(success=True, messages="User verified.",
                            data={"id": user.id, "name": user.name, "email": user.email}).text()
        else:
            return Response(success=False, messages="User email and password not match.").text()


class UpdateUserPassword(Resource):
    def put(self):
        # Parse arguments
        args = parser.parse_args()

        # Check arguments exist
        if None in [args.password, args.email]:
            return Response.check_none_response([args.password, args.email], ['Password', 'Email']).text()

        # Get Session
        session = database.DBSession()

        # Create user object to hash password
        temp_user = DB_objects.User(name="", email="")
        temp_user.hash_password(args.password)
        hashed_pass = temp_user.get_hash_pass()

        # Update user with email = args.email and commit
        session.query(DB_objects.User). \
            filter(DB_objects.User.email == args.email). \
            update({"password": hashed_pass})
        session.commit()

        # Check to see if password changed and send response
        user = session.query(DB_objects.User).filter(DB_objects.User.email == args.email).first()
        if user is None:
            return Response(success=False, messages="User is not found.").text()

        if user.verify_password(args.password):
            return Response(success=True, messages="User password updated.",
                            data={"id": user.id, "name": user.name, "email": user.email}).text()
        else:
            return Response(success=False, messages="Password not updated").text()


class Building(Resource):
    # Add a new building
    def post(self):
        # Parse arguments
        building_parser = parser.copy()
        building_parser.add_argument('name', type=str, help="No building name specified.")
        building_parser.add_argument('longitude', type=float, help="No longitude specified.")
        building_parser.add_argument('latitude', type=float, help="No latitude specified.")
        args = building_parser.parse_args()

        # Check arguments exist
        if None in [args.email, args.name, args.longitude, args.latitude]:
            return Response.check_none_response([args.email, args.name, args.longitude, args.latitude],
                                                ['Email', 'Name', 'Longitude', 'Latitude'])

        # Get Session
        session = database.DBSession()

        # Check user exist
        user = session.query(DB_objects.User).filter(DB_objects.User.email == args.email).first()
        if user is None:
            return Response(success=False, messages="User is not found.").text()

        # Check duplicate building by name
        if session.query(exists().where(func.lower(DB_objects.Building.name) == args.name.lower())).scalar():
            return Response(success=False, messages="Building name duplicated").text()

        # Add a new building
        try:
            new_building = DB_objects.Building(name=args.name, longitude=args.longitude, latitude=args.latitude,
                                               user_id=user.id, training_status="notTrained")
            session.add(new_building)
            session.commit()
            return Response(success=True, messages='New building is created.',
                            data={"id": new_building.id, "name": new_building.name,
                                  "longitude": float(new_building.longitude),
                                  "latitude": float(new_building.latitude),
                                  "training_status": new_building.training_status}).text()
        except:
            return Response(success=False, messages='Unknown issue when adding a new building.').text()

    # Show all building
    def get(self):
        # Get Session
        session = database.DBSession()

        buildings = session.query(DB_objects.Building).all()
        return Response(success=True, messages="Building data lookup success.",
                        data=[{"id": building.id,
                               "name": building.name, "longitude": str(building.longitude),
                               "latitude": str(building.latitude),
                               "training_status": building.training_status,
                               "training_time": str(building.training_time),
                               "username": session.query(DB_objects.User).filter(
                                   DB_objects.User.id == building.user_id).first().name} for
                              building in buildings]).text()

    def delete(self):
        # Parse arguments
        building_parser = parser.copy()
        building_parser.add_argument('building_id', type=int, help="No building id specified.")
        args = building_parser.parse_args()

        # Check arguments exist
        if None in [args.password, args.email, args.building_id]:
            return Response.check_none_response([args.password, args.email, args.building_id],
                                                ['Password', 'Email', 'building_id']).text()

        # Get Session
        session = database.DBSession()

        # Get the user sending delete request
        user = session.query(DB_objects.User).filter(DB_objects.User.email == args.email).first()
        if user is None:
            return Response(success=False, messages="User is not found.").text()
        if not user.verify_password(args.password):
            return Response(success=False, messages="User password not correct.",
                            data={"id": user.id, "name": user.name, "email": user.email}).text()

        # Get building info of id for building to be deleted
        building = session.query(DB_objects.Building).filter(DB_objects.Building.id == args.building_id).first()

        # Prevent Delete when building creator is not the user
        if building.user_id is not user.id:
            return Response(success=False, messages="Only building creator can delete a building.").text()

        # Delete the building with id = args.id and userid = user.id
        session.query(DB_objects.Building). \
            filter(DB_objects.Building.id == args.building_id, DB_objects.Building.user_id == user.id). \
            delete()
        session.commit()

        # Check to see if password changed and send response
        deleted_building = session.query(DB_objects.Building).filter(DB_objects.Building.id == args.building_id).first()
        if deleted_building is None:
            return Response(success=True, messages="Building deleted.",
                            data={"id": building.id, "name": building.name, "longitude": float(building.longitude),
                                  "latitude": float(building.latitude)}).text()
        else:
            return Response(success=False, messages="Building not deleted").text()


class Room(Resource):
    # Add a new room
    def post(self):
        # Parse arguments
        room_parser = parser.copy()
        room_parser.add_argument('building_id', type=str, help="No building_id specified.")
        room_parser.add_argument('floor', type=int)
        args = room_parser.parse_args()

        # Check arguments exist
        if None in [args.email, args.name, args.building_id, args.floor]:
            return Response.check_none_response([args.email, args.name, args.building_id, args.floor],
                                                ['Email', 'Room Name', 'Building_id', 'Room Floor'])

        # Get Session
        session = database.DBSession()

        # Check user exist
        user = session.query(DB_objects.User).filter(DB_objects.User.email == args.email).first()
        if user is None:
            return Response(success=False, messages="User is not found.").text()

        # Check building exist
        if not session.query(exists().where(DB_objects.Building.id == args.building_id)).scalar():
            return Response(success=False, messages='Building does not exist.').text()

        # Check duplicate room by building_id, floor and name
        if session.query(exists().where(and_(DB_objects.Room.building_id == args.building_id,
                                             DB_objects.Room.floor == args.floor,
                                             func.lower(DB_objects.Room.name) == args.name.lower()))).scalar():
            return Response(success=False, messages="Room name duplicated").text()

        # Add a new room
        try:
            new_room = DB_objects.Room(name=args.name, floor=args.floor, building_id=args.building_id,
                                       user_id=user.id)
            session.add(new_room)
            session.commit()
            return Response(success=True, messages='New room is created.',
                            data={"room_id": new_room.id, "building_id": new_room.building_id, "name": new_room.name,
                                  "floor": new_room.floor,
                                  "email": user.email}).text()
        except:
            return Response(success=False, messages='Unknown issue when adding a new room.').text()

    # Show all the rooms of a building
    def get(self):
        # Parse arguments
        room_parser = parser.copy()
        room_parser.add_argument('building_id', type=str, help="No building_id specified.")
        args = room_parser.parse_args()

        building_parser = parser.copy()
        building_parser.add_argument('building_id', type=int, help="No building id specified.")
        building_parser.add_argument('email', type=str)
        building_parser.add_argument('password', type=str)
        args = building_parser.parse_args()

        # Check email is not None
        if None in [args.building_id]:
            return Response.check_none_response([args.building_id], ['Building_id'])

        # Get Session
        session = database.DBSession()

        # Get all qualified rooms
        rooms = session.query(DB_objects.Room).filter(DB_objects.Room.building_id == args.building_id).all()
        return Response(success=True, messages="Room data lookup success.",
                        data=[{"room_id": room.id, "building_id": room.building_id, "name": room.name,
                               "floor": room.floor} for room in rooms]).text()

    def delete(self):
        # Parse arguments
        room_parser = parser.copy()
        room_parser.add_argument('room_id', type=int, help="No building id specified.")
        args = room_parser.parse_args()

        # Check arguments exist
        if None in [args.password, args.email, args.room_id]:
            return Response.check_none_response([args.password, args.email, args.id],
                                                ['Password', 'Email', 'room_id']).text()

        # Get Session
        session = database.DBSession()

        # Get the user sending delete request
        user = session.query(DB_objects.User).filter(DB_objects.User.email == args.email).first()
        if user is None:
            return Response(success=False, messages="User is not found.").text()
        if not user.verify_password(args.password):
            return Response(success=False, messages="User password not correct.",
                            data={"id": user.id, "name": user.name, "email": user.email}).text()

        # Get room info of id for building to be deleted
        room = session.query(DB_objects.Room).filter(DB_objects.Room.id == args.room_id).first()

        # Prevent Delete when building creator is not the user
        if room.user_id is not user.id:
            return Response(success=False, messages="Only building creator can delete a building.").text()

        # Delete the room with id = args.id and userid = user.id
        session.query(DB_objects.Room). \
            filter(DB_objects.Room.id == args.room_id, DB_objects.Room.user_id == user.id). \
            delete()
        session.commit()

        # Check to see if password changed and send response
        deleted_room = session.query(DB_objects.Room).filter(DB_objects.Room.id == args.room_id).first()
        if deleted_room is None:
            return Response(success=True, messages="Room deleted.",
                            data={"id": room.id, "name": room.name, "building_id": room.building_id,
                                  "floor": room.floor}).text()
        else:
            return Response(success=False, messages="Room not deleted").text()


class Scan(Resource):
    def post(self):
        scan_parser = parser.copy()
        scan_parser.add_argument('building_id', type=str, help="No building_id specified.")
        scan_parser.add_argument('room_id', type=str, help="No room_id specified.")
        request_json = request.get_json()
        scans = None
        if request_json is not None and "scans" in request_json:
            scans = request_json["scans"]
            for scan in scans:
                for apdata in scan:
                    if apdata["BSSID"] is None or apdata["SSID"] is None or apdata["quality"] is None:
                        return Response.check_none_response([apdata["BSSID"], apdata["SSID"], apdata["quality"]],
                                                            ["BSSID", "SSID", "quality"])
        args = scan_parser.parse_args()

        # Check arguments exist
        if None in [args.email, args.room_id, args.building_id, scans]:
            return Response.check_none_response([args.email, args.room_id, args.building_id, scans],
                                                ["Email", "Room_id", "Building_id", "Scans"])

        # Get Session
        session = database.DBSession()

        # Check user exist
        user = session.query(DB_objects.User).filter(DB_objects.User.email == args.email).first()
        if user is None:
            return Response(success=False, messages="User is not found.").text()

        # Check building exist
        if not session.query(exists().where(DB_objects.Building.id == args.building_id)).scalar():
            return Response(success=False, messages='Building does not exist.').text()

        # Check room exist
        if not session.query(exists().where(DB_objects.Room.id == args.room_id)).scalar():
            return Response(success=False, messages='Room does not exist.').text()

        # Add new scans
        scan_count = 0
        try:
            scan_list = []
            for scan in scans:
                new_scan_data = DB_objects.Scan(add_time=datetime.datetime.now(), user_id=user.id, room_id=args.room_id,
                                                building_id=args.building_id)
                session.add(new_scan_data)
                session.commit()

                apdata_list = []
                for apdata in scan:
                    new_ap_data = DB_objects.APData(BSSID=apdata["BSSID"], SSID=apdata["SSID"],
                                                    quality=apdata["quality"],
                                                    scan_id=new_scan_data.id)
                    session.add(new_ap_data)
                    session.flush()
                    apdata_list.append(
                        {"apdata_id": new_ap_data.id, "BSSID": new_ap_data.BSSID, "SSID": new_ap_data.SSID,
                         "quality": new_ap_data.quality,
                         "scan_id": new_ap_data.scan_id})
                scan_list.append(apdata_list)
                scan_count += 1
            session.commit()
            # return Response(success=True, messages="New Scan data is created.",
            #                 data={"scan_id": new_scan_data.id, "add_time": str(new_scan_data.add_time),
            #                       "email": user.email,
            #                       "room_id": new_scan_data.room_id, "building_id": new_scan_data.building_id,
            #                       "scans": scan_list}).text()
            return Response(success=True, messages="New Scan data is created", data={
                "count": scan_count
            }).text()
        except:
            return Response(success=False, messages='Unknown issue when adding a new Scan/Ap data').text()

    def get(self):
        scan_parser = parser.copy()
        scan_parser.add_argument('room_id', type=str, help="No room_id specified.")
        args = scan_parser.parse_args()

        # Check arguments are not None
        if None in [args.room_id]:
            return Response.check_none_response([args.room_id], ['Building_id', 'Room_id'])

        # Get Session
        session = database.DBSession()

        # Check room exist
        if not session.query(exists().where(DB_objects.Room.id == args.room_id)).scalar():
            return Response(success=False, messages='Room does not exist.').text()

        # Get Scan data
        scans = session.query(DB_objects.Scan).filter(DB_objects.Scan.room_id == args.room_id).all()

        scans_data = []

        # Get the each scan info
        for scan in scans:
            apdata_list = session.query(DB_objects.APData).filter(DB_objects.APData.scan_id == scan.id).all()
            user = session.query(DB_objects.User).filter(DB_objects.User.id == scans[0].user_id).first()
            scans_data.append({
                "scan_id": scan.id, "add_time": str(scan.add_time), "email": user.email,
                "room_id": scan.room_id, "building_id": scan.building_id,
                "apdata": [{"apdata_id": apdata.id, "BSSID": apdata.BSSID, "SSID": apdata.SSID,
                            "quality": apdata.quality, "scan_id": apdata.scan_id} for apdata in apdata_list]
            })

        return Response(success=True, messages="Scan data lookup success.", data=scans_data).text()


class Train(Resource):
    def post(self):
        scan_parser = parser.copy()
        scan_parser.add_argument('building_id', type=str, help="No building_id specified.")
        args = scan_parser.parse_args()

        # Check arguments exist
        if None in [args.building_id]:
            return Response.check_none_response([args.building_id], ["Building_id"])

        # Get Session
        session = database.DBSession()

        # Check building exist
        building = session.query(DB_objects.Building).filter(DB_objects.Building.id == args.building_id)
        if building.first() is None:
            return Response(success=False, messages="Building is not found.").text()

        # Set Training_status to "training"
        building.update({"training_status": "training"})
        session.commit()

        # Get all scans
        scans = session.query(DB_objects.Scan).filter(DB_objects.Scan.building_id == args.building_id).all()

        # Enter AP data into dataset
        dataset = Dataset()
        for scan in scans:
            apdata_list = session.query(DB_objects.APData).filter(DB_objects.APData.scan_id == scan.id).all()
            dataset.add(
                [{"BSSID": apdata.BSSID, "SSID": apdata.SSID, "quality": apdata.quality} for apdata in apdata_list],
                scan.room_id)

        # try:
        # Training
        clf = LogisticRegression(multi_class='multinomial', solver='newton-cg')
        clf.fit(dataset.samples, dataset.rooms)
        model_filename = './models/' + str(args.building_id) + "_" + "model"
        BSSIDs_filename = './models/' + str(args.building_id) + "_" + "BSSIDs"
        with open(model_filename, "wb") as file:
            pickle.dump(clf, file)
        with open(BSSIDs_filename, "wb") as file:
            pickle.dump(dataset.BSSIDs, file)
        # building.update({"trained_model": model_binary, "trained_model_BSSIDs": header_binary})
        building.update({"training_status": "trained", "training_time": datetime.datetime.now()})
        session.commit()
        return Response(success=True, messages="Training Success.",
                        data={"training_status": building.first().training_status,
                              "training_time": str(
                                  building.first().training_time)}).text()

    # except:
    #     building.update({"training_status": "Not Trained"})
    #     db.session.commit()
    #     return Response(success=False, messages="Training Fail").text()

    def get(self):
        scan_parser = parser.copy()
        scan_parser.add_argument('building_id', type=str, help="No building_id specified.")
        args = scan_parser.parse_args()

        # Check arguments exist
        if None in [args.building_id]:
            return Response.check_none_response([args.building_id],
                                                ["Building_id"])

        # Get Session
        session = database.DBSession()

        # Check building exist
        building = session.query(DB_objects.Building).filter(DB_objects.Building.id == args.building_id).first()
        if building is None:
            return Response(success=False, messages="Building is not found.").text()

        return Response(success=True, messages="Building training status check success",
                        data={"training_status": building.training_status,
                              "training_time": str(building.training_time)}).text()


class Predict(Resource):
    def post(self):
        # Maximum Building Search Radius
        radius = 0.001  # 100m
        nradius = -0.001

        # Parse argument
        scan_parser = parser.copy()
        scan_parser.add_argument('longitude', type=float, help="No building_id specified.")
        scan_parser.add_argument('latitude', type=float, help="No building_id specified.")
        args = scan_parser.parse_args()

        # Check arguments exist
        if None in [args.longitude, args.latitude]:
            return Response.check_none_response([args.longitude, args.latitude], ["Longitude", "Latitude"])

        # Get Scans data
        request_json = request.get_json()
        scans = None
        if request_json is not None and "scans" in request_json:
            scans = request_json["scans"]
            for scan in scans:
                for apdata in scan:
                    if apdata["BSSID"] is None or apdata["SSID"] is None or apdata["quality"] is None:
                        return Response.check_none_response([apdata["BSSID"], apdata["SSID"], apdata["quality"]],
                                                            ["BSSID", "SSID", "quality"])

        # Get Session
        session = database.DBSession()

        # Check scans have data
        if len(scans) == 0:
            return Response(success=False, messages="Cannot predict on empty data.").text()

        # Get All Buildings
        buildings = session.query(DB_objects.Building).filter(DB_objects.Building.training_status == "trained").filter(
            and_(DB_objects.Building.longitude - args.longitude < radius,
                 DB_objects.Building.longitude - args.longitude > nradius,
                 DB_objects.Building.latitude - args.latitude < radius,
                 DB_objects.Building.latitude - args.latitude > nradius)).all()
        print("Lenge of Building for Prediction", len(buildings))

        # Coverage Test
        building_count = {}
        for building in buildings:
            building_count[building.id] = 0
            BSSIDs_filename = './models/' + str(building.id) + "_" + "BSSIDs"
            BSSIDs = []
            with open(BSSIDs_filename, 'rb') as file:
                BSSIDs = pickle.load(file)
            # Check for matches
            for scan in scans:
                for apdata in scan:
                    if apdata["BSSID"] in BSSIDs:
                        building_count[building.id] += 1

        # Response if no building is found in the range
        if (len(building_count)) == 0:
            return Response(success=False, messages="No trained building is found in the 100m radius.").text()

        # Find the building with maximum count
        best_building_id = max(building_count.items(), key=operator.itemgetter(1))[0]

        # Make prediction on best building
        # Get Building Model and Header
        model_filename = './models/' + str(best_building_id) + "_" + "model"
        BSSIDs_filename = './models/' + str(best_building_id) + "_" + "BSSIDs"

        # Setup data to be predicted
        BSSIDs = []
        with open(BSSIDs_filename, 'rb') as file:
            BSSIDs = pickle.load(file)
        apdata_list = scans[0]
        apdata_prediction = [0] * len(BSSIDs)
        for apdata in apdata_list:
            if apdata['BSSID'] in BSSIDs:
                apdata_prediction[BSSIDs.index(apdata['BSSID'])] = apdata['quality']

        # Get model from building
        with open(model_filename, 'rb') as file:
            clf = pickle.load(file)

        # Predict with model
        prediction_prob = np.multiply(100, clf.predict_proba(np.asarray(apdata_prediction).reshape(1, -1)))
        room_probs = {}
        for index, classes in enumerate(clf.classes_):
            prob = float(prediction_prob[0][index])
            room_probs[str(classes)] = prob

        # Find the room with maximum probability
        best_room = max(room_probs.items(), key=lambda item: item[1])  # format ('12', 98.03358894585917)
        best_room_id = best_room[0]
        probability = best_room[1]

        # Retrieve Room Information
        room = session.query(DB_objects.Room).filter(DB_objects.Room.id == best_room_id).first()

        return Response(success=True, messages=["Successfully predict room."], data={
            "room_id": room.id, "building_id": room.building_id, "name": room.name,
            "floor": room.floor, "probability": probability
        }).text()

    #
    # def get(self):
    #     scan_parser = parser.copy()
    #     scan_parser.add_argument('building_id', type=str, help="No building_id specified.")
    #
    #     # Get Scans data
    #     request_json = request.get_json()
    #     scans = None
    #     if request_json is not None and "scans" in request_json:
    #         scans = request_json["scans"]
    #         for scan in scans:
    #             for apdata in scan:
    #                 if apdata["BSSID"] is None or apdata["SSID"] is None or apdata["quality"] is None:
    #                     return Response.check_none_response([apdata["BSSID"], apdata["SSID"], apdata["quality"]],
    #                                                         ["BSSID", "SSID", "quality"])
    #
    #     args = scan_parser.parse_args()
    #
    #     # Get Session
    #     session = database.DBSession()
    #
    #     # Check building exist
    #     if not session.query(exists().where(DB_objects.Building.id == args.building_id)).scalar():
    #         return Response(success=False, messages='Building does not exist.').text()
    #
    #     # Check scans have data
    #     if len(scans) == 0:
    #         return Response(success=False, messages="Cannot predict on empty data.").text()
    #
    #     # Get Building Model and Header
    #     building = session.query(DB_objects.Building).filter(DB_objects.Building.id == args.building_id).first()
    #     model_filename = './models/' + str(args.building_id) + "_" + "model"
    #     BSSIDs_filename = './models/' + str(args.building_id) + "_" + "BSSIDs"
    #
    #     # Setup data to be predicted
    #     BSSIDs = []
    #     with open(BSSIDs_filename, 'rb') as file:
    #         BSSIDs = pickle.load(file)
    #     apdata_list = scans[0]
    #     apdata_prediction = [0] * len(BSSIDs)
    #     for apdata in apdata_list:
    #         if apdata['BSSID'] in BSSIDs:
    #             apdata_prediction[BSSIDs.index(apdata['BSSID'])] = apdata['quality']
    #
    #     # Get model from building
    #     with open(model_filename, 'rb') as file:
    #         clf = pickle.load(file)
    #
    #     # Predict with model
    #     prediction = clf.predict(np.asarray(apdata_prediction).reshape(1, -1))
    #
    #     print(prediction)
    #     return "OK"


class Response:
    def __init__(self, success, messages, data=None):
        self.success = success
        if type(messages) == str:
            messages = [messages]
        self.messages = messages
        self.data = data

    def text(self):
        return {'success': self.success, 'messages': self.messages, 'data': self.data}

    @staticmethod
    def check_none_response(fields, text):
        messages = []
        for index, field in enumerate(fields):
            if field is None:
                messages.append(text[index] + " cannot be empty.")
        return Response(success=False, messages=messages).text()


class Dataset:
    def __init__(self):
        self.BSSIDs = []
        self.SSIDs = []
        self.samples = []
        self.rooms = []  # Each room corresponding to a sample

    def add(self, apdata_list, room_id):
        # Each sample is a single scan
        new_sample = [0] * len(self.BSSIDs)
        for apdata in apdata_list:
            BSSID = apdata["BSSID"]
            SSID = apdata["SSID"]
            quality = apdata["quality"]
            if BSSID in self.BSSIDs:
                new_sample[self.BSSIDs.index(BSSID)] = quality
            else:  # BSSID is not in self.BSSIDs
                self.BSSIDs.append(BSSID)
                self.SSIDs.append(SSID)
                new_sample.append(quality)
                for sample in self.samples:
                    sample.append(0)
        self.rooms.append(room_id)
        self.samples.append(new_sample)


# URL Management
api.add_resource(HelloWorld, '/')
api.add_resource(NewUser, '/user/new')
api.add_resource(VerifyUser, '/user/verify')
api.add_resource(UpdateUserPassword, '/user/update')
api.add_resource(Building, '/building')
api.add_resource(Room, '/room')
api.add_resource(Scan, '/scan')
api.add_resource(Train, '/train')
api.add_resource(Predict, '/predict')

# Main Method
if __name__ == '__main__':
    app.run('0.0.0.0', debug=True, port=5000)
