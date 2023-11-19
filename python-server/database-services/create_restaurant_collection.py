from pymongo.mongo_client import MongoClient
from pymongo.server_api import ServerApi
from dotenv import load_dotenv
import os

load_dotenv()

db_cluster=os.getenv("DB_CLUSTER")
db_username=os.getenv("DB_USERNAME")
db_passsword=os.getenv("DB_PASSWORD")
db_name=os.getenv("DB_NAME")

uri = "mongodb+srv://" + db_username + ":" + db_passsword + "@" + db_cluster

client = MongoClient(uri,server_api=ServerApi('1'))
db = client[db_name]

restaurants_schema = {
    "id": "int",
    "name": "string",
    "type_of_restaurant": "string",
    "rating": "float"
}

restaurants_collection = db["Restaurants"]
restaurants_collection.create_index("id", unique=True)

# sample_restaurants = [
#     {"id": 1, "name": "McDonalds", "type_of_restaurant": "Fastfood", "rating": 4.5},
#     {"id": 2, "name": "Subway", "type_of_restaurant": "Subs", "rating": 4.2},
#     {"id": 3, "name": "Pizza", "type_of_restaurant": "Italian", "rating": 4.0},
# ]

# restaurants_collection.insert_many(sample_restaurants)

print("Collection restaurant created successfully.")