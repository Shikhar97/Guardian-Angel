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

restaurant_food_schema = {
    "id": "int",
    "restaurant_id": "int",
    "name": "string",
    "major_ingredients": ["string"],
    "calories": "float"
}

restaurant_food_collection = db["Restaurant_Food"]
restaurant_food_collection.create_index("id", unique=True)

# sample_restaurant_foods = [
#     {"id": 1, "restaurant_id": 1, "name": "Big Mac", "major_ingredients": ["Beef Patty", "Special Sauce", "Lettuce", "Cheese", "Pickles", "Onions", "Sesame Seed Bun"], "calories": 563},
#     {"id": 2, "restaurant_id": 2, "name": "Italian BMT", "major_ingredients": ["Ham", "Salami", "Pepperoni", "Lettuce", "Tomato", "Onions", "Olives", "Bread"], "calories": 550},
#     {"id": 3, "restaurant_id": 3, "name": "Margherita Pizza", "major_ingredients": ["Tomato Sauce", "Mozzarella Cheese", "Fresh Basil", "Olive Oil"], "calories": 250},
# ]

# restaurant_food_collection.insert_many(sample_restaurant_foods)

print("Collection restaurant_food created successfully.")