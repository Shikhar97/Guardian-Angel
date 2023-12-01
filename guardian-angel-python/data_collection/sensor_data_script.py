# This file mocks sensor data for a user

import random
from datetime import datetime, timedelta
from bson import ObjectId
# from data_access.mongoData import mongoData
from flask_pymongo import PyMongo
import logging
from flask import Flask, request, jsonify, json

from bson.objectid import ObjectId
from flask_pymongo import PyMongo
import os
from dotenv import load_dotenv
from pymongo.mongo_client import MongoClient
import time

load_dotenv()

mongo = None
class mongoData:
    def __init__(self, app):
        self.app = app
        self.mongo = self.__get_mongo()

    def __get_mongo(self):
        if not self.app.config['TESTING']:
            self.app.config['MONGO_URI'] = os.getenv('DB_URI')
            # Local DB
            # self.app.config['MONGO_URI'] = 'mongodb://127.0.0.1:27017/GuardianAngel?directConnection=true&serverSelectionTimeoutMS=2000&appName=mongosh+2.0.2'
        else:
            self.app.config['MONGO_URI'] = os.getenv('TEST_DB_URI')

        return PyMongo(self.app)

    def mongo_client(self):
        return MongoClient(self.app.config['MONGO_URI'])


    def get_all(self):
        return self.__get_mongo().db.dataSources.find({})


app = Flask(__name__)
app.logger.setLevel(logging.DEBUG)

def mock_data_helper(start_time, timestamp):

    heart_rate = 0
    respiratory_rate = 0
    calories_burnt = 0
    steps_count = 0

    current_date = start_time

    # Rule 1: Sleep mode from 10pm to 6am
    s_sleep_time_rand = random.randrange(21, 24)
    sleep_start_time = datetime(current_date.year, current_date.month, current_date.day, s_sleep_time_rand, 0)
    e_sleep_time_rand = random.randrange(4, 6)
    sleep_end_time = datetime(current_date.year, current_date.month, current_date.day, e_sleep_time_rand, 0)
    sleep = 1 if sleep_start_time <= current_date or current_date <= sleep_end_time else 0

    # Initialize blood_oxygen outside the Gym and Jogging block
    blood_oxygen = random.randint(95, 100)

    # Rule 2: Gym and Jogging activities
    gym_start_time = datetime(current_date.year, current_date.month, current_date.day, 18, 0)
    gym_end_time = datetime(current_date.year, current_date.month, current_date.day, 19, 0)
    jogging_start_time = datetime(current_date.year, current_date.month, current_date.day, 7, 0)
    jogging_end_time = datetime(current_date.year, current_date.month, current_date.day, 8, 0)

    if (gym_start_time <= current_date <= gym_end_time) or \
       (jogging_start_time <= current_date <= jogging_end_time):
        # print("Setting gym and jogging data")
        heart_rate = random.randint(120, 160)
        respiratory_rate = random.randint(20, 30)
        calories_burnt = random.randint(200, 400)
        steps_count = random.randint(500, 1500)
    else:
        # Rule 3: Normal heart rate and respiratory rate during sleep
        if sleep:
            # print("Setting sleep data")
            heart_rate = random.randint(60, 80)
            respiratory_rate = random.randint(12, 18)
            steps_count = 0
            calories_burnt = random.randint(50, 150)
        else:
            # Rule 10: During office hours
            office_start_time = datetime(current_date.year, current_date.month, current_date.day, 9, 0)
            office_end_time = datetime(current_date.year, current_date.month, current_date.day, 17, 0)
            # print("Setting office data")
            if office_start_time <= current_date <= office_end_time:
                # Rule 14: Random spikes during office hours (15% of the time)
                if random.random() < 0.15:
                    print("Inside spikes")
                    heart_rate = random.randint(90, 120)
                    respiratory_rate = random.randint(18, 28)
                    steps_count = random.randint(200, 500)
                    calories_burnt = random.randint(50, 150)
                else:
                    heart_rate = random.randint(70, 90)
                    respiratory_rate = random.randint(16, 22)
                    steps_count = random.randint(500, 1500)
                    calories_burnt = random.randint(150, 300)
            else:
                # print("Setting normal data")
                # Rule 11: Random spikes during stressful situations
                if random.random() < 0.05:
                    heart_rate = random.randint(90, 120)
                    respiratory_rate = random.randint(18, 28)
                    steps_count = random.randint(200, 500)
                    calories_burnt = random.randint(100, 250)
                else:
                    heart_rate = random.randint(70, 90)
                    respiratory_rate = random.randint(12, 14)
                    steps_count = random.randint(200, 500)
                    calories_burnt = random.randint(50, 150)

    # Rule 12: Blood oxygen may drop slightly during intense activities
    # Rule 13: Steps count may vary based on daily routines
    blood_oxygen = random.randint(92, 98) if sleep else random.randint(95, 100)
    steps_count += random.randint(10, 500)

    data = {
        "heart_rate": heart_rate,
        "respiratory_rate": respiratory_rate,
        "blood_oxygen": blood_oxygen,
        "steps_count": steps_count,
        "calories_burnt": calories_burnt,
        "sleep": sleep
    }

    return data
    # mongo = mongoData(app).mongo
    # user_attributes_collection = mongo.db.User_attributes
    # data['user_id'] = user_id
    # data['timestamp'] = timestamp
    # result = user_attributes_collection.insert_one(data)
    # return result

def generate_mock_data(user_id):
    if not ObjectId(user_id):
        return jsonify({'error': UserAttributeLocales.INVALID_USER_ID_FORMAT}), 400

    mongo = mongoData(app).mongo
    user_attributes_collection = mongo.db.User_attributes

    # Get the current date and time
    current_date = datetime.utcnow()

    # Set the start time 2 days earlier from the beginning of the day
    start_time = datetime(current_date.year, current_date.month, current_date.day, 0, 0) - timedelta(days=2)

    insert_counter = 0

    # Iterate through each 10-minute interval from start_time to current_date
    while start_time <= current_date:
        # Format the timestamp for insertion
        timestamp = start_time.strftime("%Y-%m-%dT%H:%M:%S.000Z")

        data = mock_data_helper(start_time, timestamp)

        # Insert data into the database
        # data = {
        #     "heart_rate": heart_rate,
        #     "respiratory_rate": respiratory_rate,
        #     "blood_oxygen": blood_oxygen,
        #     "steps_count": steps_count,
        #     "calories_burnt": calories_burnt,
        #     "sleep": sleep,
        #     "user_id": user_id,
        #     "timestamp": timestamp
        # }

        mongo = mongoData(app).mongo
        user_attributes_collection = mongo.db.User_attributes
        data['user_id'] = user_id
        data['timestamp'] = datetime.fromisoformat(timestamp)
        # result = user_attributes_collection.insert_one(data)
        # return result
        result = user_attributes_collection.insert_one(data)

        insert_counter += 1

        if insert_counter == 25:
            time.sleep(1)  # Sleep for 1 second
            insert_counter = 0  # Reset the counter

        # Increment the start_time by 10 minutes
        start_time += timedelta(minutes=10)

    return result

generate_mock_data('655ff2802c6a0e4de1d9a9d4')
