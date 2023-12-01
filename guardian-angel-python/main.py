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
from flasgger import Swagger
from constants import REGISTRATION_REQUIRED_FIELDS, USER_ATTRIBUTE_REQUIRED_FIELDS, USER_ATTRIBUTE_FETCH_KEYS, WEATHER_API_HOST
from locales import UserAttributeLocales, UserRegistrationLocales, RestaurantFoodLocales, WeatherLocales
import logging
from data_access.mongoData import mongoData
from jobs.scheduler import schedule_job, get_all_job_stats, delete_job, update_job
from feature_modules.sleep_wellness.controller import optimal_wake_up_time
from feature_modules.health_fuzzy_impl import health_monitoring_system
from datetime import datetime, timezone

app = Flask(__name__)
app.logger.setLevel(logging.DEBUG)
swagger = Swagger(app, template_file='swagger.yml', parse=True)


@app.route('/')
def hello():
    return "Hello World!"


db = mongoData(app).mongo.db
user_collection = db.User
existing_indexes = user_collection.index_information()
if 'email_1' not in existing_indexes:
    user_collection.create_indexes([IndexModel([('email', ASCENDING)], unique=True)])

# User Registration API
@app.route('/users/register', methods=['POST'])
@token_required
def register_user():
    try:
        data = request.get_json()
        for field in REGISTRATION_REQUIRED_FIELDS:
            if field not in data:
                return jsonify({'error': UserRegistrationLocales.MISSING_REQUIRED_FIELD.format(field)}), 400

        mongo = mongoData(app).mongo
        user_collection = mongo.db.User
        user_id = user_collection.insert_one(data).inserted_id

        return jsonify({'message': UserRegistrationLocales.USER_REGISTERED_SUCCESSFULLY, 'user_id': str(user_id)}), 200

    except Exception as e:
        print("Exception", e)
        if 'duplicate key' in str(e).lower():
            return jsonify({'error': UserRegistrationLocales.EMAIL_ALREADY_REGISTERED}), 400
        return jsonify({'error': f'{UserRegistrationLocales.ERROR}: {str(e)}'}), 500

# User Attributes API
@app.route('/users/<string:user_id>/user_attributes', methods=['POST'])
@token_required
def add_user_attributes(user_id):
    try:
        if not ObjectId(user_id):
            return jsonify({'error': UserAttributeLocales.INVALID_USER_ID_FORMAT}), 400

        data = request.get_json()

        for field in USER_ATTRIBUTE_REQUIRED_FIELDS:
            if field not in data:
                return jsonify({'error': UserAttributeLocales.MISSING_REQUIRED_FIELD.format(field)}), 400

        try:
            data['timestamp'] = datetime.fromisoformat(data['timestamp'])
        except ValueError:
            return jsonify({'error': UserAttributeLocales.INVALID_TIMESTAMP_FORMAT}), 400

        mongo = mongoData(app).mongo
        user_attributes_collection = mongo.db.User_attributes
        data['user_id'] = user_id
        user_attributes_collection.insert_one(data)
        return jsonify({'message': UserAttributeLocales.USER_ATTRIBUTES_ADDED_SUCCESSFULLY}), 200

    except Exception as e:
        return jsonify({'error': f'{UserAttributeLocales.ERROR}: {str(e)}'}), 500

# User Attributes GET API
@app.route('/users/<string:user_id>/user_attributes', methods=['GET'])
@token_required
def get_user_attributes(user_id):
    print("Entry point")
    try:
        if not ObjectId(user_id):
            return jsonify({'error': UserAttributeLocales.INVALID_USER_ID_FORMAT}), 400

        mongo = mongoData(app).mongo
        keys = request.args.get('keys', '').split(',')
        from_time = request.args.get('from', '')
        to_time = request.args.get('to', '')

        if from_time == '' or to_time == '':
            return jsonify({'error': UserAttributeLocales.INVALID_TIMESTAMP_FORMAT}), 400

        from_time, to_time = _parse_timestamps(from_time, to_time)
        if not all(key in USER_ATTRIBUTE_FETCH_KEYS for key in keys):
            return jsonify({'error': UserAttributeLocales.INVALID_KEYS}), 400
        query_filter = {
            'user_id': user_id,
            'timestamp': {'$gte': from_time, '$lte': to_time}
        }
        projection = {key: 1 for key in keys}
        projection['_id'] = 0
        projection['timestamp'] = 1
        user_attributes_collection = mongo.db.User_attributes
        results = user_attributes_collection.find(query_filter, projection)
        db_entries = [result for result in results]
        # print("DB entries", db_entries)
        keys_to_average = [key for key in keys if key not in ('sleep', 'steps_count', 'calories_burnt')]
        final_values = {}
        if keys_to_average:
            final_values = _calculate_average_values(keys_to_average, db_entries)

        if 'sleep' in keys:
            final_values['sleep_time'] = _calculate_sleep_time(db_entries)

        if 'steps_count' in keys:
            final_values['total_steps_count'] = sum(entry['steps_count'] for entry in db_entries)

        if 'calories_burnt' in keys:
            final_values['total_calories_burnt'] = sum(entry['calories_burnt'] for entry in db_entries)

        return jsonify(final_values), 200

    except Exception as e:
        print("Exception", e)
        return jsonify({'error': f'{UserAttributeLocales.ERROR}: {str(e)}'}), 500

# API endpoint to get all restaurants
@app.route('/restaurants', methods=['GET'])
@token_required
def get_restaurants():
    try:
        mongo = mongoData(app).mongo
        restaurants_collection = mongo.db.Restaurants
        projection = {'_id': 0}
        restaurants = list(restaurants_collection.find(projection=projection))

        serialized_restaurants = dumps({'restaurants': restaurants})
        deserialized_restaurants = json.loads(serialized_restaurants)

        return jsonify(deserialized_restaurants), 200

    except Exception as e:
        print("Exception", e)
        return jsonify({'error': str(e)}), 500

@app.route('/restaurants/<string:restaurant_id>/foods', methods=['GET'])
@token_required
def get_foods_for_restaurant(restaurant_id):
    try:
        mongo = mongoData(app).mongo
        restaurant_food_collection = mongo.db.Restaurant_Food

        # if not restaurant_id.isdigit():
        #     return jsonify({'error': RestaurantFoodLocales.INVALID_RESTAURANT_ID_FORMAT}), 400
        # projection = {'_id': 0}
        # foods = list(restaurant_food_collection.find({'restaurant_id': int(restaurant_id)}, projection=projection))

        projection = {'_id': 0}
        foods = list(restaurant_food_collection.find({'restaurant_id': ObjectId(restaurant_id)}, projection=projection))
        serialized_foods = dumps({'foods': foods})
        deserialized_foods = json.loads(serialized_foods)

        return jsonify(deserialized_foods), 200

    except Exception as e:
        print("Exception", e)
        return jsonify({'error': str(e)}), 500

@app.route('/weather', methods=['GET'])
@token_required
def get_weather():
    try:
        # Will be used in deployment
        api_key = os.getenv('openweathermap_api_key')
        lat = request.args.get('lat')
        lon = request.args.get('lon')
        city = request.args.get('city')

        if lat is not None and lon is not None:
            weather_api_url = f'{WEATHER_API_HOST}?lat={lat}&lon={lon}&appid={api_key}'
        elif city:
            weather_api_url = f'{WEATHER_API_HOST}?q={city}&appid={api_key}'
        else:
            return jsonify({'error': WeatherLocales.MISSING_PARAMETERS}), 400

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
            return jsonify({'error': WeatherLocales.DATA_FETCH_FAILED}), response.status_code

    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/jobs', methods=['POST'])
@token_required
def schedule_job_route():
    return schedule_job(request)

@app.route('/jobs/<string:job_id>', methods=['PUT'])
@token_required
def update_job_route(job_id):
    return update_job(request, job_id)

@app.route('/jobs/<string:job_id>', methods=['DELETE'])
@token_required
def delete_job_route(job_id):
    return delete_job(request, job_id)

@app.route('/jobs', methods=['GET'])
@token_required
def get_all_jobs():
    return get_all_job_stats()

@app.route('/users/<string:user_id>/wake_up_time', methods=['GET'])
@token_required
def wake_up_time(user_id):
    try:
        user_id = ObjectId(user_id)
    except Exception as e:
        return jsonify({'error': UserAttributeLocales.INVALID_USER_ID_FORMAT}), 400

    user_preference = request.args.get('user_preference', 'normal')
    if user_preference not in ('normal', 'early', 'late'):
        return jsonify({'error': UserAttributeLocales.INVALID_USER_PREFERENCE}), 400

    return jsonify({'wake_up_time': optimal_wake_up_time(user_id, user_preference)}), 200

@app.route('/healthFuzzy', methods=['GET'])
def get_health_update():
    try:
        hr = request.args.get('hr', type=int)
        rr = request.args.get('rr', type=int)
        sc = request.args.get('sc', type=int)

        health_update = health_monitoring_system(hr, rr, sc)
        health_update_json = {
            'health_update': health_update
        }
        return jsonify(health_update_json)
    except Exception as e:
        print("Exception", e)
        return jsonify({'error': f'{"ERROR"}: {str(e)}'}), 500

# Private functions

def _calculate_sleep_time(sleep_entries):
    sleep_time_total = 0

    sorted_sleep_entries = sorted(sleep_entries, key=lambda x: x['timestamp'])

    for i in range(1, len(sorted_sleep_entries)):
        if sorted_sleep_entries[i]['sleep'] == 0:
            continue
        sleep_start = sorted_sleep_entries[i - 1]['timestamp']
        sleep_end = sorted_sleep_entries[i]['timestamp']
        sleep_duration = sleep_end - sleep_start
        sleep_time_total += sleep_duration.total_seconds()

    return sleep_time_total

def _parse_timestamps(from_time, to_time):
    try:
        from_time = datetime.strptime(from_time, "%Y-%m-%dT%H:%M:%SZ") if from_time else None
        to_time = datetime.strptime(to_time, "%Y-%m-%dT%H:%M:%SZ") if to_time else None
    except ValueError:
        return jsonify({'error': UserAttributeLocales.INVALID_TIMESTAMP_FORMAT}), 400
    return from_time, to_time

def _calculate_average_values(keys, results):
    average_values = {f'average_{key}': 0 for key in keys}
    count_values = {f'average_{key}': 0 for key in keys}

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

    return average_values

if __name__ == '__main__':
    app.run(debug=True, use_reloader=False)
