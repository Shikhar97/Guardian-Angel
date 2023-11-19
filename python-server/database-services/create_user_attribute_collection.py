from pymongo.mongo_client import MongoClient
from pymongo.server_api import ServerApi
from datetime import datetime
from dotenv import load_dotenv
import os

load_dotenv()

db_cluster=os.getenv("DB_CLUSTER")
db_username=os.getenv("DB_USERNAME")
db_passsword=os.getenv("DB_PASSWORD")
db_name=os.getenv("DB_NAME")
db_uri=os.getenv("DB_URI")

client = MongoClient(db_uri,server_api=ServerApi('1'))
db = client[db_name]

user_attributes_schema = {
    "user_id": "int",
    "heart_rate": "int",
    "respiratory_rate": "int",
    "steps_count": "int",
    "calories_burnt": "int",
    "blood_oxygen": "float",
    "sleep": "int",
    "timestamp": "datetime"
}

user_attributes_collection = db["user_attributes"]
user_attributes_collection.create_index("user_id")

# sample_user_attribute = {
#     "user_id": "123456",
#     "heart_rate": 75,
#     "respiratory_rate": 18,
#     "steps_count": 10000,
#     "calories_burnt": 500,
#     "blood_oxygen": 98.5,
#     "sleep": 1,
#     "timestamp": datetime.utcnow()
# }
# user_attributes_collection.insert_one(sample_user_attribute)

print("Collection user_attribute created successfully.")
