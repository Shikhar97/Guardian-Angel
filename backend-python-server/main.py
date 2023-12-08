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
from constants import REGISTRATION_REQUIRED_FIELDS, USER_ATTRIBUTE_REQUIRED_FIELDS, USER_ATTRIBUTE_FETCH_KEYS, \
    WEATHER_API_HOST
from locales import UserAttributeLocales, UserRegistrationLocales, RestaurantFoodLocales, WeatherLocales
import logging
from data_access.mongoData import mongoData
from jobs.scheduler import schedule_job, get_all_job_stats, delete_job, update_job
from feature_modules.sleep_wellness.controller import optimal_wake_up_time
from feature_modules.health_fuzzy_impl import health_monitoring_system
from datetime import datetime, timezone
from data_processing.user_attributes import _parse_timestamps, _average_values_custom, _average_values_per_day, \
    _average_values_per_hour

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


# User GET API
@app.route('/users/<string:user_id>', methods=['GET'])
@token_required
def get_user_info(user_id):
    try:
        if not ObjectId(user_id):
            return jsonify({'error': UserAttributeLocales.INVALID_USER_ID_FORMAT}), 400

        mongo = mongoData(app).mongo
        user_collection = mongo.db.User
        user = user_collection.find_one({'_id': ObjectId(user_id)}, {'_id': 0})
        if not user:
            return jsonify({'error': UserAttributeLocales.USER_NOT_FOUND}), 404

        user['id'] = user_id
        return jsonify(user), 200

    except Exception as e:
        return jsonify({'error': f'{UserAttributeLocales.ERROR}: {str(e)}'}), 500


# User POST API
@app.route('/users/<string:user_id>', methods=['POST'])
@token_required
def update_user_info(user_id):
    try:
        if not ObjectId(user_id):
            return jsonify({'error': UserAttributeLocales.INVALID_USER_ID_FORMAT}), 400

        data = request.get_json()
        mongo = mongoData(app).mongo
        user_collection = mongo.db.User
        user = user_collection.find_one({'_id': ObjectId(user_id)}, {'_id': 0})
        if not user:
            return jsonify({'error': UserAttributeLocales.USER_NOT_FOUND}), 404

        user['id'] = user_id
        for key, value in data.items():
            user_collection.update_one(
                {'_id': ObjectId(user_id)},
                {
                    "$set": {
                        key: value
                    }
                })

        user = user_collection.find_one({'_id': ObjectId(user_id)}, {'_id': 0})
        return jsonify(user), 200

    except Exception as e:
        return jsonify({'error': f'{UserAttributeLocales.ERROR}: {str(e)}'}), 500


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
        user_attributes_collection = mongo.db.UserAttributes
        data['user_id'] = ObjectId(user_id)
        user_attributes_collection.insert_one(data)
        return jsonify({'message': UserAttributeLocales.USER_ATTRIBUTES_ADDED_SUCCESSFULLY}), 200

    except Exception as e:
        return jsonify({'error': f'{UserAttributeLocales.ERROR}: {str(e)}'}), 500


# User Attributes GET API
@app.route('/users/<string:user_id>/user_attributes', methods=['GET'])
@token_required
# group_by can be 'day' or 'hour'
def get_user_attributes(user_id):
    try:
        if not ObjectId(user_id):
            return jsonify({'error': UserAttributeLocales.INVALID_USER_ID_FORMAT}), 400

        user_id = ObjectId(user_id)
        mongo = mongoData(app).mongo
        keys = request.args.get('keys', '').split(',')
        from_time = request.args.get('from', '')
        to_time = request.args.get('to', '')
        filter_type = request.args.get('group_by', '')
        static_keys = request.args.get('static_keys', '')

        if from_time == '' or to_time == '':
            return jsonify({'error': UserAttributeLocales.INVALID_TIMESTAMP_FORMAT}), 400

        from_time, to_time = _parse_timestamps(from_time, to_time)

        if not all(key in USER_ATTRIBUTE_FETCH_KEYS for key in keys):
            return jsonify({'error': UserAttributeLocales.INVALID_KEYS}), 400

        if filter_type == 'day' and (to_time - from_time).days > 10:
            return jsonify({'error': 'Date range is too long. Try a shorter one'}), 400

        if filter_type == 'hour' and (to_time - from_time).days > 3:
            return jsonify({'error': 'Date range is too long. Try a shorter one'}), 400

        # from_time = from_time.replace(hour=0, minute=0, second=0, microsecond=0)
        # to_time = to_time.replace(hour=23, minute=59, second=59, microsecond=999999)

        query_filter = {
            'user_id': user_id,
            'timestamp': {'$gte': from_time, '$lte': to_time}
        }
        projection = {key: 1 for key in keys}
        projection['_id'] = 0
        projection['timestamp'] = 1
        user_attributes_collection = mongo.db.UserAttributes
        results = user_attributes_collection.find(query_filter, projection).sort('timestamp', 1)
        db_entries = [result for result in results]

        if filter_type == 'day':
            final_values = _average_values_per_day(keys, db_entries, static_keys)
        elif filter_type == 'hour':
            final_values = _average_values_per_hour(keys, db_entries, static_keys)
        else:
            final_values = _average_values_custom(keys, db_entries)

        return jsonify(final_values), 200

    except Exception as e:
        print("Exception", e)
        return jsonify({'error': f'{UserAttributeLocales.ERROR}: {str(e)}'}), 500


@app.route('/users/<string:user_id>/user_attributes/recent', methods=['GET'])
@token_required
def get_recent_user_attributes(user_id):
    try:
        if not ObjectId(user_id):
            return jsonify({'error': UserAttributeLocales.INVALID_USER_ID_FORMAT}), 400

        user_id = ObjectId(user_id)
        mongo = mongoData(app).mongo
        record_count = int(request.args.get('count', 7))

        if record_count <= 0:
            return jsonify({'error': 'Invalid count value'}), 400

        if record_count > 100:
            return jsonify({'error': 'Count value too high. Try a lower value'}), 400

        user_att_collection = mongo.db.UserAttributes
        results_cursor = user_att_collection.find({'user_id': user_id}, projection={'user_id': 0}).sort('timestamp',
                                                                                                        -1).limit(
            record_count)
        results_list = list(results_cursor)

        serialized_results = dumps({'user_attributes': results_list})
        deserialized_results = json.loads(serialized_results)

        for event in deserialized_results['user_attributes']:
            event['id'] = event['_id']['$oid']
            event['timestamp'] = event['timestamp']['$date']
            event.pop('_id', None)

        return jsonify(deserialized_results), 200

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
        restaurant_food_collection = mongo.db.RestaurantFood

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


@app.route('/users/<string:user_id>/events', methods=['GET'])
@token_required
def get_events(user_id):
    try:
        user_id = ObjectId(user_id)
    except Exception as e:
        return jsonify({'error': UserAttributeLocales.INVALID_USER_ID_FORMAT}), 400
    mongo = mongoData(app).mongo
    user_events_collection = mongo.db.UserEvents
    user_events = list(user_events_collection.find({'user_id': user_id}))
    serialized_user_events = dumps({'user_events': user_events})
    deserialized_user_events = json.loads(serialized_user_events)

    for event in deserialized_user_events['user_events']:
        event['id'] = event['_id']['$oid']
        event['user_id'] = str(user_id)
        event['timestamp'] = event['timestamp']['$date']
        event.pop('_id', None)

    return jsonify(deserialized_user_events), 200


@app.route('/users/<string:user_id>/events/<string:event_id>', methods=['GET'])
@token_required
def get_event(user_id, event_id):
    try:
        user_id = ObjectId(user_id)
    except Exception as e:
        return jsonify({'error': UserAttributeLocales.INVALID_USER_ID_FORMAT}), 400

    mongo = mongoData(app).mongo
    user_events_collection = mongo.db.UserEvents

    user_event = user_events_collection.find_one({'user_id': user_id, '_id': ObjectId(event_id)})

    if not user_event:
        return jsonify({'error': 'Invalid event_id'}), 400

    user_event['id'] = str(user_event['_id'])
    user_event['user_id'] = str(user_event['user_id'])
    user_event['timestamp'] = user_event['timestamp'].strftime("%Y-%m-%dT%H:%M:%SZ")
    user_event.pop('_id', None)

    serialized_user_event = dumps({'user_event': user_event})
    deserialized_user_event = json.loads(serialized_user_event)

    return jsonify(deserialized_user_event), 200


@app.route('/users/<string:user_id>/events/<string:event_id>', methods=['DELETE'])
@token_required
def delete_event(user_id, event_id):
    try:
        user_id = ObjectId(user_id)
    except Exception as e:
        return jsonify({'error': 'Invalid user_id format'}), 400

    mongo = mongoData(app).mongo
    user_events_collection = mongo.db.UserEvents

    result = user_events_collection.delete_one({'user_id': user_id, '_id': ObjectId(event_id)})

    if result.deleted_count == 0:
        return jsonify({'error': 'Event not found'}), 404

    return jsonify({'message': 'Event deleted successfully'}), 200


@app.route('/users/<string:user_id>/events', methods=['DELETE'])
@token_required
def delete_all_events(user_id):
    try:
        user_id = ObjectId(user_id)
    except Exception as e:
        return jsonify({'error': 'Invalid user_id format'}), 400

    mongo = mongoData(app).mongo
    user_events_collection = mongo.db.UserEvents
    result = user_events_collection.delete_many({'user_id': user_id})

    if result.deleted_count == 0:
        return jsonify({'error': 'No events found for the user'}), 200

    return jsonify({'message': 'All events deleted successfully'}), 200


@app.route('/users/<string:user_id>/events', methods=['POST'])
@token_required
def create_events(user_id):
    try:
        user_id = ObjectId(user_id)
    except Exception as e:
        return jsonify({'error': 'Invalid user_id format'}), 400

    mongo = mongoData(app).mongo
    user_events_collection = mongo.db.UserEvents

    try:
        events = request.json.get('events')

        if not events or not isinstance(events, list):
            return jsonify({'error': 'Invalid request format. Missing or invalid "events" key.'}), 400

        result = user_events_collection.insert_many([
            {
                'user_id': user_id,
                'event_name': event.get('event_name'),
                'event_description': event.get('event_description'),
                'timestamp': datetime.strptime(event.get('timestamp'), '%Y-%m-%dT%H:%M:%SZ')
            }
            for event in events
        ])

        inserted_ids = [str(inserted_id) for inserted_id in result.inserted_ids]
        event_names = [event.get('event_name') for event in events]

        response_data = [{'inserted_id': inserted_id, 'event_name': event_name} for inserted_id, event_name in
                         zip(inserted_ids, event_names)]

        return jsonify({'message': 'Events created successfully', 'events': response_data}), 201

    except Exception as e:
        return jsonify({'error': 'Error creating events', 'details': str(e)}), 500


# Private functions


if __name__ == '__main__':
    app.run(debug=True, use_reloader=False)
