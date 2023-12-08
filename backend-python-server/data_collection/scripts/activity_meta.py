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


# Function to generate random time within a given range
def generate_random_time(start_hour, end_hour):
    random_minutes = random.randrange(0, 60)
    return datetime.now().replace(hour=random.randrange(start_hour, end_hour), minute=random_minutes, second=0, microsecond=0)

# Insert data into ActivityMeta collection for each day of the month
for date_id in range(1, 32):
    s_sleep_time = generate_random_time(20, 24)
    e_sleep_time = generate_random_time(4, 6)

    g_start_time = generate_random_time(18, 19)
    g_end_time = generate_random_time(19, 20)

    j_start_time = generate_random_time(7, 8)
    j_end_time = generate_random_time(8, 9)

    # Create document to insert into collection
    activity_data = {
        "date_id": date_id,
        "sleep_start_time": s_sleep_time,
        "sleep_end_time": e_sleep_time,
        "gym_start_time": g_start_time,
        "gym_end_time": g_end_time,
        "jogging_start_time": j_start_time,
        "jogging_end_time": j_end_time
    }

    mongo = mongoData(app).mongo
    activity_meta_collection = mongo.db.ActivityMeta

    # Insert document into collection
    activity_meta_collection.insert_one(activity_data)

print("Data inserted successfully.")
