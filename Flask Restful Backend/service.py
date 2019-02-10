import datetime
from flask import Flask
from flask_restful import Resource, Api, reqparse
import DB_objects
from sqlalchemy import exists, func, and_

# Initialize Flask App
app = Flask(__name__)
api = Api(app)

# Initialize Parser
parser = reqparse.RequestParser()
parser.add_argument('name', type=str, help="No username specified.")
parser.add_argument('password', type=str, help="No password specified.")
parser.add_argument('email', type=str, help="No email specified.")
db = DB_objects.database()


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

        # Check email already exist
        if db.session.query(exists().where(DB_objects.User.email == args.email)).scalar():
            return Response(success=False, messages='Email Already Exist.').text()

        # Add new user
        try:
            new_user = DB_objects.User(name=args.name, email=args.email)
            new_user.hash_password(args.password)
            db.session.add(new_user)
            db.session.commit()
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

        # Get user tuple from database
        user = db.session.query(DB_objects.User).filter(DB_objects.User.email == args.email).first()

        # Check user exist
        if user is None:
            return Response(success=False, messages="User is not found.").text()

        # Verify user password
        if user.verify_password(args.password):
            return Response(success=True, messages="User verified.",
                            data={"id": user.id, "name": user.name, "email": user.email}).text()
        else:
            return Response(success=False, messages="User email and password not match.").text()


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

        # Check user exist
        user = db.session.query(DB_objects.User).filter(DB_objects.User.email == args.email).first()
        if user is None:
            return Response(success=False, messages="User is not found.").text()

        # Check duplicate building by name
        if db.session.query(exists().where(func.lower(DB_objects.Building.name) == args.name.lower())).scalar():
            return Response(success=False, messages="Building name duplicated").text()

        # Add a new building
        try:
            new_building = DB_objects.Building(name=args.name, longitude=args.longitude, latitude=args.latitude,
                                               user_id=user.id, training_status="Not Trained")
            db.session.add(new_building)
            db.session.commit()
            return Response(success=True, messages='New building is created.',
                            data={"id": new_building.id, "name": new_building.name,
                                  "longitude": float(new_building.longitude),
                                  "latitude": float(new_building.latitude),
                                  "training_status": new_building.training_status}).text()
        except:
            return Response(success=False, messages='Unknown issue when adding a new building.').text()

    # Show all building
    def get(self):
        buildings = db.session.query(DB_objects.Building).all()
        return [building.name for building in buildings]


class Room(Resource):
    # Add a new room
    def post(self):
        # Parse arguments
        room_parser = parser.copy()
        room_parser.add_argument('building_id', type=str, help="No building_id specified.")
        room_parser.add_argument('floor', type=str, help="No floor specified.")
        args = room_parser.parse_args()

        # Check arguments exist
        if None in [args.email, args.name, args.building_id, args.floor]:
            return Response.check_none_response([args.email, args.name, args.building_id, args.floor],
                                                ['Email', 'Room Name', 'Building_id', 'Room Floor'])

        # Check user exist
        user = db.session.query(DB_objects.User).filter(DB_objects.User.email == args.email).first()
        if user is None:
            return Response(success=False, messages="User is not found.").text()

        # Check building exist
        if not db.session.query(exists().where(DB_objects.Building.id == args.building_id)).scalar():
            return Response(success=False, messages='Building does not exist.').text()

        # Check duplicate room by building_id, floor and name
        if db.session.query(exists().where(and_(DB_objects.Building.id == args.building_id,
                                                DB_objects.Room.floor == args.floor,
                                                func.lower(DB_objects.Room.name) == args.name.lower()))).scalar():
            return Response(success=False, messages="Room name duplicated").text()

        # Add a new room
        try:
            new_room = DB_objects.Room(name=args.name, floor=args.floor, building_id=args.building_id,
                                       user_id=user.id)
            db.session.add(new_room)
            db.session.commit()
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

        # Check email is not None
        if None in [args.building_id]:
            return Response.check_none_response([args.building_id], ['Building_id'])

        # Get all qualified rooms
        rooms = db.session.query(DB_objects.Room).filter(DB_objects.Room.building_id == args.building_id).all()
        return [{"room_id": room.id, "building_id": room.building_id, "name": room.name,
                 "floor": room.floor} for room in rooms]


class APData(Resource):
    # Add a new APData
    def post(self):
        # Parse arguments
        ap_data_parser = parser.copy()
        ap_data_parser.add_argument('BSSID', type=str, help="No BSSID specified.")
        ap_data_parser.add_argument('SSID', type=str, help="No SSID specified.")
        ap_data_parser.add_argument('quality', type=str, help="No quality specified.")
        ap_data_parser.add_argument('building_id', type=str, help="No building_id specified.")
        ap_data_parser.add_argument('room_id', type=str, help="No room_id specified.")
        args = ap_data_parser.parse_args()

        # Check arguments exist
        if None in [args.email, args.BSSID, args.SSID, args.quality, args.building_id, args.room_id]:
            return Response.check_none_response(
                [args.email, args.BSSID, args.SSID, args.quality, args.building_id, args.room_id],
                ['Email', 'BSSID', 'SSID', 'quality', 'building_id', 'room_id'])

        # Check user exist
        user = db.session.query(DB_objects.User).filter(DB_objects.User.email == args.email).first()
        if user is None:
            return Response(success=False, messages="User is not found.").text()

        # Check building exist
        if not db.session.query(exists().where(DB_objects.Building.id == args.building_id)).scalar():
            return Response(success=False, messages='Building does not exist.').text()

        # Check room exist
        if not db.session.query(exists().where(DB_objects.Room.id == args.room_id)).scalar():
            return Response(success=False, messages='Room does not exist.').text()

        # Add a new APData
        try:
            new_ap_data = DB_objects.APData(BSSID=args.BSSID, SSID=args.SSID, quality=args.quality,
                                            building_id=args.building_id,
                                            room_id=args.room_id,
                                            user_id=user.id,
                                            add_time=datetime.datetime.now())
            db.session.add(new_ap_data)
            db.session.commit()
            return Response(success=True, messages='New AP data is created.', data={
                'BSSID': new_ap_data.BSSID, "SSID": new_ap_data.SSID, "quality": new_ap_data.quality,
                "building_id": new_ap_data.building_id, "room_id": new_ap_data.room_id,
                "add_time": str(new_ap_data.add_time), "email": user.email
            }).text()
        except:
            return Response(success=False, messages='Unknown issue when adding a new AP data.').text()

    # Show all APData of a room
    def get(self):
        # Parse arguments
        ap_data_parser = parser.copy()
        ap_data_parser.add_argument('building_id', type=str, help="No building_id specified.")
        ap_data_parser.add_argument('room_id', type=str, help="No room_id specified.")
        args = ap_data_parser.parse_args()

        # Check arguments are not None
        if None in [args.building_id, args.room_id]:
            return Response.check_none_response([args.building_id, args.room_id], ['Building_id', 'Room_id'])

        # Get all qualified rooms
        APDatas = db.session.query(DB_objects.APData).filter(DB_objects.APData.building_id == args.building_id,
                                                             DB_objects.APData.room_id == args.room_id).all()
        return [{
            'BSSID': APData.BSSID, "SSID": APData.SSID, "quality": APData.quality,
            "building_id": APData.building_id, "room_id": APData.room_id,
            "add_time": str(APData.add_time)
        } for APData in APDatas]


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


# URL Management
api.add_resource(HelloWorld, '/')
api.add_resource(NewUser, '/user/new')
api.add_resource(VerifyUser, '/user/verify')
api.add_resource(Building, '/building')
api.add_resource(Room, '/room')
api.add_resource(APData, '/apdata')

# Main Method
if __name__ == '__main__':
    app.run('0.0.0.0', debug=True, port=5000)
