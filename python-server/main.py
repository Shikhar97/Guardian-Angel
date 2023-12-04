from flask import Flask, request, jsonify, json
from flask_pymongo import PyMongo
from auth_middleware import token_required
from bson import ObjectId
from pymongo import IndexModel, ASCENDING
from datetime import datetime
from dateutil import parser
import requests
from bson.json_util import dumps
import os

app = Flask(__name__)

# MongoDB configuration
app.config['MONGO_URI'] = 'mongodb://127.0.0.1:27017/GuardianAngel?directConnection=true&serverSelectionTimeoutMS=2000&appName=mongosh+2.0.2'
mongo = PyMongo(app)

@app.route('/')
def hello():
    return "Hello World!"

user_collection = mongo.db.users
user_collection.create_indexes([IndexModel([('email', ASCENDING)], unique=True)])

# Sample curl
# curl --location 'http://127.0.0.1:5000/users/register' \
# --header 'Content-Type: application/json' \
# --header 'X-Api-Auth: dummy_verification_key' \
# --data-raw '{
#   "name": "John Doe",
#   "email": "john.doe@example.com",
#   "phone": "1234567890",
#   "allergies": ["peanuts", "gluten"],
#   "emergency_contact_name": "Emergency Contact",
#   "emergency_contact_number": "9876543210"
# }'

# User Registration API
@app.route('/users/register', methods=['POST'])
@token_required
def register_user():
    try:
        data = request.get_json()

        required_fields = ['name', 'email', 'phone', 'allergies', 'emergency_contact_name', 'emergency_contact_number']
        for field in required_fields:
            if field not in data:
                return jsonify({'error': f'Missing required field: {field}'}), 400

        user_id = user_collection.insert_one(data).inserted_id

        return jsonify({'message': 'User registered successfully', 'user_id': str(user_id)}), 200

    except Exception as e:
        if 'duplicate key' in str(e).lower():
            return jsonify({'error': 'Email address is already registered'}), 400
        return jsonify({'error': str(e)}), 500


# Sample curl
# curl --location 'http://127.0.0.1:5000/users/6558771738c07c2825a35a16/user_attributes' \
# --header 'Content-Type: application/json' \
# --header 'X-Api-Auth: dummy_verification_key' \
# --data '{
#   "heart_rate": 65,
#   "respiratory_rate": 14,
#   "steps_count": 5300,
#   "calories_burnt": 320,
#   "blood_oxygen": 98,
#   "sleep": 1,
#   "timestamp": "2023-11-18T12:30:00Z"
# }'
# User Attributes API

@app.route('/users/<string:user_id>/user_attributes', methods=['POST'])
@token_required
def add_user_attributes(user_id):
    try:
        try:
            ObjectId(user_id)
        except:
            return jsonify({'error': 'Invalid user_id format'}), 400

        data = request.get_json()

        required_fields = ['heart_rate', 'respiratory_rate', 'steps_count', 'calories_burnt', 'blood_oxygen', 'sleep', 'timestamp']
        for field in required_fields:
            if field not in data:
                return jsonify({'error': f'Missing required field: {field}'}), 400

        try:
            data['timestamp'] = datetime.fromisoformat(data['timestamp'])
        except ValueError:
            return jsonify({'error': 'Invalid timestamp format. Use ISO 8601 format.'}), 400

        user_attributes_collection = mongo.db.user_attributes
        data['user_id'] = ObjectId(user_id)
        user_attributes_collection.insert_one(data)

        return jsonify({'message': 'User attributes added successfully'}), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500

# Sample curl
# curl "http://127.0.0.1:5000/users/6558771738c07c2825a35a16/user_attributes?keys[]=heart_rate&keys[]=respiratory_rate&keys[]=steps_count&from=2023-11-01T00:00:00Z&to=2023-12-01T23:59:59Z"
# --header 'X-Api-Auth: dummy_verification_key'

ALLOWED_KEYS = {'heart_rate', 'respiratory_rate', 'steps_count', 'calories_burnt', 'blood_oxygen'}

# User Attributes GET API
@app.route('/users/<string:user_id>/user_attributes', methods=['GET'])
@token_required
def get_user_attributes(user_id):
    try:
        if not ObjectId.is_valid(user_id):
            return jsonify({'error': 'Invalid user_id format'}), 400

        keys = request.args.getlist('keys[]')
        from_time = request.args.get('from', '')
        to_time = request.args.get('to', '')

        try:
            from_time = datetime.strptime(from_time, "%Y-%m-%dT%H:%M:%SZ")
        except ValueError:
            return jsonify({'error': 'Invalid "from" timestamp format. Use ISO 8601 format.'}), 400
        try:
            to_time = datetime.strptime(to_time, "%Y-%m-%dT%H:%M:%SZ")
        except ValueError:
            return jsonify({'error': 'Invalid "to" timestamp format. Use ISO 8601 format.'}), 400

        if not all(key in ALLOWED_KEYS for key in keys):
            return jsonify({'error': 'Invalid keys. Allowed keys are: heart_rate, respiratory_rate, steps_count, calories_burnt, blood_oxygen'}), 400

        query_filter = {
            'user_id': ObjectId(user_id),
            'timestamp': {'$gte': from_time, '$lte': to_time}
        }

        projection = {key: 1 for key in keys}
        projection['_id'] = 0

        user_attributes_collection = mongo.db.user_attributes
        results = user_attributes_collection.find(query_filter, projection)

        average_values = {}
        count_values = {}
        for key in keys:
            average_key = f'average_{key}'
            average_values[average_key] = 0
            count_values[average_key] = 0

        for result in results:
            for key in keys:
                average_key = f'average_{key}'
                if key in result:
                    average_values[average_key] += result[key]
                    count_values[average_key] += 1

        for key in keys:
            average_key = f'average_{key}'
            if count_values[average_key] > 0:
                average_values[average_key] /= count_values[average_key]

        return jsonify(average_values), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500

# curl --location 'http://127.0.0.1:5000/restaurants' \
# --header 'X-Api-Auth: dummy_verification_key'
# API endpoint to get all restaurants
@app.route('/restaurants', methods=['GET'])
@token_required
def get_restaurants():
    try:
        restaurants_collection = mongo.db.restaurants
        restaurants = list(restaurants_collection.find())

        serialized_restaurants = dumps({'restaurants': restaurants})
        deserialized_restaurants = json.loads(serialized_restaurants)

        for restaurant in deserialized_restaurants['restaurants']:
            restaurant['id'] = restaurant.pop('_id')['$oid']

        return jsonify(deserialized_restaurants), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500

# curl --location 'http://127.0.0.1:5000/restaurants/655962a2fb034040ec9c74a4/foods' \
# --header 'X-Api-Auth: dummy_verification_key'
# API endpoint to get restaurant foods
@app.route('/restaurants/<string:restaurant_id>/foods', methods=['GET'])
@token_required
def get_foods_for_restaurant(restaurant_id):
    try:
        restaurant_food_collection = mongo.db.restaurant_food
        if not ObjectId.is_valid(restaurant_id):
            return jsonify({'error': 'Invalid restaurant_id format'}), 400

        foods = list(restaurant_food_collection.find({'restaurant_id': ObjectId(restaurant_id)}))

        serialized_foods = dumps({'foods': foods})
        deserialized_foods = json.loads(serialized_foods)

        for food in deserialized_foods['foods']:
            food['id'] = food.pop('_id')['$oid']
            food['restaurant_id'] = food.pop('restaurant_id')['$oid']

        return jsonify(deserialized_foods), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500

# API endpoint to get weather
# curl --location 'http://127.0.0.1:5000/weather?city=London' \
# --header 'X-Api-Auth: dummy_verification_key'
@app.route('/weather', methods=['GET'])
@token_required
def get_weather():
    try:
        # Will be used in deployment
        # api_key = os.getenv('openweathermap_api_key')
        api_key = '<api_token>'
        city = request.args.get('city')

        if not city:
            return jsonify({'error': 'City parameter is missing'}), 400

        weather_api_url = f'https://api.openweathermap.org/data/2.5/weather?q={city}&appid={api_key}'
        print(weather_api_url)
        response = requests.get(weather_api_url)
        if response.status_code == 200:
            weather_data = response.json()
            formatted_weather = {
                'temperature': weather_data['main']['temp'],
                'description': weather_data['weather'][0]['description'],
                'humidity': weather_data['main']['humidity'],
                'wind_speed': weather_data['wind']['speed']
            }

            return jsonify({'weather': formatted_weather}), 200
        else:
            return jsonify({'error': 'Failed to retrieve weather data'}), response.status_code

    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)
