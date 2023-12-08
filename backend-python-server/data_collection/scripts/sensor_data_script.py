# This file mocks sensor data for a user

import random
from datetime import datetime, timedelta, time
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

def mock_data_helper(start_time, timestamp, current_activity_meta):

    heart_rate = 0
    respiratory_rate = 0
    calories_burnt = 0
    steps_count = 0

    current_date = start_time

    # Rule 1: Sleep mode
    # s_sleep_time_rand = random.randrange(20, 24)
    # e_sleep_time_rand = random.randrange(4, 6)

    # s_random_minutes = random.randrange(0, 60)
    # e_random_minutes = random.randrange(0, 60)

    # sleep_start_time = datetime(current_date.year, current_date.month, current_date.day, s_sleep_time_rand, s_random_minutes)
    # sleep_end_time = datetime(current_date.year, current_date.month, current_date.day, e_sleep_time_rand, e_random_minutes)
    sleep_start_time = current_activity_meta['sleep_start_time']
    sleep_end_time = current_activity_meta['sleep_end_time']
    # print("sleep_start_time", sleep_start_time.time())
    # print("sleep_end_time", sleep_end_time.time())
    # print("current_date", current_date.time())
    sleep = 1 if sleep_start_time.time() <= current_date.time() or current_date.time() <= sleep_end_time.time() else 0

    # Initialize blood_oxygen outside the Gym and Jogging block
    blood_oxygen = random.randint(95, 100)

    # Rule 2: Gym and Jogging activities
    # g_s_random_minutes = random.randrange(0, 60)
    # g_e_random_minutes = random.randrange(0, 60)
    # gym_start_time = datetime(current_date.year, current_date.month, current_date.day, 18, g_s_random_minutes)
    # gym_end_time = datetime(current_date.year, current_date.month, current_date.day, 19, g_e_random_minutes)
    # jogging_start_time = datetime(current_date.year, current_date.month, current_date.day, 7, g_s_random_minutes)
    # jogging_end_time = datetime(current_date.year, current_date.month, current_date.day, 8, g_e_random_minutes)
    gym_start_time = current_activity_meta['gym_start_time'].time()
    gym_end_time = current_activity_meta['gym_end_time'].time()
    jogging_start_time = current_activity_meta['jogging_start_time'].time()
    jogging_end_time = current_activity_meta['jogging_end_time'].time()

    if (gym_start_time <= current_date.time() <= gym_end_time) or \
       (jogging_start_time <= current_date.time() <= jogging_end_time):
        print("Setting gym and jogging data")
        heart_rate = random.randint(120, 160)
        respiratory_rate = random.randint(20, 30)
        calories_burnt = random.randint(90, 150)
        steps_count = random.randint(200, 500)
    else:
        # Rule 3: Normal heart rate and respiratory rate during sleep
        if sleep:
            # print("Setting sleep data")
            heart_rate = random.randint(60, 80)
            respiratory_rate = random.randint(12, 18)
            steps_count = 0
            calories_burnt = random.randint(10, 15)
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
                    steps_count = random.randint(15, 30)
                    calories_burnt = random.randint(10, 20)
                else:
                    heart_rate = random.randint(70, 90)
                    respiratory_rate = random.randint(16, 22)
                    steps_count = random.randint(12, 25)
                    calories_burnt = random.randint(8, 15)
            else:
                # print("Setting normal data")
                # Rule 11: Random spikes during stressful situations
                if random.random() < 0.05:
                    heart_rate = random.randint(90, 120)
                    respiratory_rate = random.randint(18, 28)
                    steps_count = random.randint(10, 20)
                    calories_burnt = random.randint(10, 20)
                else:
                    heart_rate = random.randint(70, 90)
                    respiratory_rate = random.randint(12, 14)
                    steps_count = random.randint(8, 15)
                    calories_burnt = random.randint(8, 15)

    # Rule 12: Blood oxygen may drop slightly during intense activities
    # Rule 13: Steps count may vary based on daily routines
    blood_oxygen = random.randint(92, 98) if sleep else random.randint(95, 100)
    # steps_count += random.randint(10, 500)

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
    # user_attributes_collection = mongo.db.UserAttributes
    # data['user_id'] = user_id
    # data['timestamp'] = timestamp
    # result = user_attributes_collection.insert_one(data)
    # return result

def generate_mock_data(user_id):
    if not ObjectId(user_id):
        return jsonify({'error': UserAttributeLocales.INVALID_USER_ID_FORMAT}), 400

    mongo = mongoData(app).mongo
    user_attributes_collection = mongo.db.UserAttributes
    activity_meta_collection = mongo.db.ActivityMeta
    query_result = activity_meta_collection.find()
    activity_meta_result = {}
    for result in query_result:
        activity_meta_result[result['date_id']] = result

    result = []
    # Get the current date and time
    current_date = datetime.utcnow()

    # Set the start time 2 days earlier from the beginning of the day
    # start_time = datetime(current_date.year, current_date.month, current_date.day, 0, 0) - timedelta(days=10)
    start_time = datetime(current_date.year, 12, 5, 18, 1, 7)

    insert_counter = 0

    # Iterate through each 10-minute interval from start_time to current_date
    while start_time <= current_date:
        # Format the timestamp for insertion
        timestamp = start_time.strftime("%Y-%m-%dT%H:%M:%S.000Z")

        current_activity_meta = activity_meta_result[start_time.day]

        data = mock_data_helper(start_time, timestamp, current_activity_meta)

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
        user_attributes_collection = mongo.db.UserAttributes
        data['user_id'] = ObjectId(user_id)
        data['timestamp'] = datetime.fromisoformat(timestamp)
        print("Timestamp", data['timestamp'])

        if user_attributes_collection.count_documents({'user_id': ObjectId(user_id), 'timestamp': data['timestamp']}) == 0:
            result = user_attributes_collection.insert_one(data)

        insert_counter += 1

        if insert_counter == 25:
            time.sleep(1)  # Sleep for 1 second
            insert_counter = 0  # Reset the counter

        # Increment the start_time by 10 minutes
        start_time += timedelta(minutes=10)

    return result

generate_mock_data('655ad12b6ac4d71bf304c5eb')
