from pymongo.mongo_client import MongoClient
from pymongo.server_api import ServerApi
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

user_schema = {
    "user_id": "int",
    "name": "string",
    "email": "string",
    "phone": "string",
    "allergies": ["string"],
    "emergency_contact": {
        "name": "string",
        "number": "string"
    }
}

user_collection = db["User"]
user_collection.create_index("user_id", unique=True)

# sample_user = {
#     "user_id": 1,
#     "name": "John Doe",
#     "email": "john@example.com",
#     "phone": "123-456-7890",
#     "allergies": ["Peanuts", "Shellfish"],
#     "emergency_contact": {
#         "name": "Jane Doe",
#         "number": "987-654-3210"
#     }
# }
# user_collection.insert_one(sample_user)

print("Collection User created successfully.")
