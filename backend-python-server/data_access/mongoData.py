from bson.objectid import ObjectId
from flask_pymongo import PyMongo
import os
from dotenv import load_dotenv
from pymongo.mongo_client import MongoClient

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
