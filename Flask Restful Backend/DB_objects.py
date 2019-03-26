from sqlalchemy import Column, String, create_engine, Integer, VARCHAR, ForeignKey, DATETIME, DECIMAL, BLOB
from sqlalchemy.orm import sessionmaker, relationship
from sqlalchemy.ext.declarative import declarative_base
from passlib.apps import custom_app_context as pwd_context

DB = declarative_base()


class Room(DB):
    __tablename__ = "room"
    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(VARCHAR)
    floor = Column(Integer)
    building_id = Column(Integer, ForeignKey('building.id'))
    user_id = Column(Integer, ForeignKey('user.id'))  # Creator


class Scan(DB):
    __tablename__ = "scan"
    id = Column(Integer, primary_key=True, autoincrement=True)
    add_time = Column(DATETIME)
    user_id = Column(Integer, ForeignKey('user.id'))  # Creator
    room_id = Column(Integer, ForeignKey('room.id'))
    building_id = Column(Integer, ForeignKey('building.id'))


class APData(DB):
    __tablename__ = "ap_data"
    id = Column(Integer, primary_key=True, autoincrement=True)
    BSSID = Column(VARCHAR)
    SSID = Column(VARCHAR)
    quality = Column(Integer)
    scan_id = Column(Integer, ForeignKey("scan.id"))


class Building(DB):
    __tablename__ = "building"
    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(VARCHAR)
    longitude = Column(DECIMAL)
    latitude = Column(DECIMAL)
    user_id = Column(Integer, ForeignKey('user.id'))  # Creator
    training_status = Column(VARCHAR)
    training_time = Column(DATETIME)


class Normalization(DB):
    __tablename__ = "normalization"
    user_id = Column(Integer, ForeignKey('user.id'), primary_key=True)
    building_id = Column(Integer, ForeignKey('building.id'), primary_key=True)
    normalization_factor = Column(Integer)


class User(DB):
    __tablename__ = "user"
    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(VARCHAR)
    password = Column(VARCHAR)
    email = Column(VARCHAR)

    def hash_password(self, password):
        self.password = pwd_context.encrypt(password)

    def verify_password(self, password):
        return pwd_context.verify(password, self.password)


class database:
    engine = create_engine('mysql+mysqlconnector://mradey13:password@localhost:3306/indoor_locator_db')
    #engine = create_engine('mysql+mysqlconnector://root:linyuxiang1998@localhost:3306/indoor_locator_db')
    DBSession = sessionmaker(bind=engine)

    # new_room = User(name='Room 2', password=2, email="lin.2453@osu.edu")
    # session.add(new_room)

    # print([user.email for user in
    # session.query(User).filter(User.name == "Room 2", User.email == "lin.2453@osu.edu").all()])
    # session.commit()
    # session.close()
