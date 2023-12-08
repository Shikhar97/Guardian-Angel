import numpy as np
import skfuzzy as fuzz
from skfuzzy import control as ctrl
from data_access.mongoData import mongoData
from flask import Flask, request, jsonify, json
from flask_pymongo import PyMongo
from datetime import datetime, timezone,timedelta
import pytz

app = Flask(__name__)
def create_fuzzy_variables():
    avg_sleep_time = ctrl.Antecedent(np.arange(0, 13, 1), 'avg_sleep_time')
    calories_burnt = ctrl.Antecedent(np.arange(0, 3001, 1), 'calories_burnt')
    preference = ctrl.Antecedent(np.arange(0, 13, 1), 'preference')
    wake_up_time = ctrl.Consequent(np.arange(240, 720, 1), 'wake_up_time')

    return avg_sleep_time, calories_burnt, preference, wake_up_time

def define_fuzzy_sets(avg_sleep_time, calories_burnt, preference, wake_up_time):
    avg_sleep_time['short'] = fuzz.trapmf(avg_sleep_time.universe, [0, 0, 3, 5])
    avg_sleep_time['moderate'] = fuzz.trimf(avg_sleep_time.universe, [3, 6, 9])
    avg_sleep_time['long'] = fuzz.trapmf(avg_sleep_time.universe, [7, 9, 12, 12])

    calories_burnt['low'] = fuzz.trimf(calories_burnt.universe, [0, 0, 1500])
    calories_burnt['moderate'] = fuzz.trimf(calories_burnt.universe, [0, 1500, 3000])
    calories_burnt['high'] = fuzz.trimf(calories_burnt.universe, [1500, 3000, 3000])

    preference['early'] = fuzz.trimf(preference.universe, [0, 0, 5])
    preference['normal'] = fuzz.trimf(preference.universe, [4, 6, 8])
    preference['late'] = fuzz.trimf(preference.universe, [7, 12, 12])

    wake_up_time['very_early'] = fuzz.trimf(wake_up_time.universe, [240, 300, 360])  # 4 to 5 hours
    wake_up_time['early'] = fuzz.trimf(wake_up_time.universe, [330, 390, 450])  # 5.5 to 6.5 hours
    wake_up_time['normal'] = fuzz.trimf(wake_up_time.universe, [390, 480, 540])  # 6.5 to 8 hours
    wake_up_time['late'] = fuzz.trimf(wake_up_time.universe, [480, 600, 720])  # 8 to 11 hours

def create_fuzzy_rules(avg_sleep_time, calories_burnt, preference, wake_up_time):
    rules = []

    # Rule 1
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['low'] &
            preference['early']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 2
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['low'] &
            preference['normal']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 3
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['low'] &
            preference['late']
        ),
        consequent=wake_up_time['late']
    ))

    # Rule 4
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['moderate'] &
            preference['early']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 5
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['moderate'] &
            preference['normal']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 6
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['moderate'] &
            preference['late']
        ),
        consequent=wake_up_time['late']
    ))

    # Rule 7
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['high'] &
            preference['early']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 8
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['high'] &
            preference['normal']
        ),
        consequent=wake_up_time['late']
    ))

    # Rule 9
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['high'] &
            preference['late']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 10
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['low'] &
            preference['early']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 11
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['low'] &
            preference['normal']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 12
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['low'] &
            preference['late']
        ),
        consequent=wake_up_time['late']
    ))

    # Rule 13
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['moderate'] &
            preference['early']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 14
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['moderate'] &
            preference['normal']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 15
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['moderate'] &
            preference['late']
        ),
        consequent=wake_up_time['late']
    ))

    # Rule 16
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['high'] &
            preference['early']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 17
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['high'] &
            preference['normal']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 18
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['high'] &
            preference['late']
        ),
        consequent=wake_up_time['late']
    ))

    # Rule 19
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['low'] &
            preference['early']
        ),
        consequent=wake_up_time['very_early']
    ))

    # Rule 20
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['low'] &
            preference['normal']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 21
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['low'] &
            preference['late']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 22
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['moderate'] &
            preference['early']
        ),
        consequent=wake_up_time['very_early']
    ))

    # Rule 23
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['moderate'] &
            preference['normal']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 24
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['moderate'] &
            preference['late']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 25
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['high'] &
            preference['early']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 26
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['high'] &
            preference['normal']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 27
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['high'] &
            preference['late']
        ),
        consequent=wake_up_time['normal']
    ))

    return rules

def create_fuzzy_system(rules):
    return ctrl.ControlSystem(rules)

def predict_wake_up_time(user_id, user_input, wake_up_time_prediction):

    # Get user_input from user_attributes
    user_preference = {"early": 3, "normal": 6, "late": 9}.get(user_input, 6)

    wake_up_time_prediction.input['preference'] = user_preference
    wake_up_time_prediction.input['avg_sleep_time'] = get_average_sleep_time(user_id)
    wake_up_time_prediction.input['calories_burnt'] = get_average_calories_burnt(user_id)

    print("wake_up_time_prediction", wake_up_time_prediction)
    wake_up_time_prediction.compute()

    return int(wake_up_time_prediction.output['wake_up_time'])

def optimal_wake_up_time(user_id, user_input):
    avg_sleep_time, calories_burnt, preference, wake_up_time = create_fuzzy_variables()
    define_fuzzy_sets(avg_sleep_time, calories_burnt, preference, wake_up_time)

    rules = create_fuzzy_rules(avg_sleep_time, calories_burnt, preference, wake_up_time)

    wake_up_ctrl = create_fuzzy_system(rules)
    wake_up_time_prediction = ctrl.ControlSystemSimulation(wake_up_ctrl)

    # user_input = "normal"  # Mock data for now (Will be changed in Project 5)
    predicted_wake_up_time = predict_wake_up_time(user_id, user_input, wake_up_time_prediction)

    print("Predicted Wake-up Time in minutes:", predicted_wake_up_time)
    return predicted_wake_up_time

def get_average_sleep_time(user_id):
    try:
        mongo = mongoData(app).mongo
        keys = ['sleep']

        # Get the current date and time in UTC
        current_utc_time = datetime.utcnow()

        # Define the Phoenix timezone
        phoenix_timezone = pytz.timezone('America/Phoenix')

        # Convert UTC time to Phoenix time
        current_phx_time = current_utc_time.replace(tzinfo=pytz.utc).astimezone(phoenix_timezone)
        p_from_time = current_phx_time - timedelta(days=3)
        p_from_time = p_from_time.replace(hour=0, minute=0, second=0, microsecond=0)

        from_time = datetime(current_utc_time.year, current_utc_time.month, p_from_time.day, 0, 0, 0)
        to_time = datetime(current_utc_time.year, current_utc_time.month, current_phx_time.day, 23, 59, 59)

        # from_time = datetime.strptime('2023-11-28T00:00:00Z', "%Y-%m-%dT%H:%M:%SZ")
        # to_time = datetime.strptime('2023-11-29T23:59:59Z', "%Y-%m-%dT%H:%M:%SZ")

        print("Sleep From time", from_time)
        print("To time", to_time)
        query_filter = {
            'user_id': user_id,
            'timestamp': {'$gte': from_time, '$lte': to_time}
        }
        projection = {key: 1 for key in keys}
        projection['_id'] = 0
        projection['timestamp'] = 1
        user_attributes_collection = mongo.db.UserAttributes
        results = user_attributes_collection.find(query_filter, projection)
        db_entries = [result for result in results]
        final_values = {}
        sleep_time = _calculate_sleep_time(db_entries)

        return sleep_time/60/60/2 # in hours per day

    except Exception as e:
        print("Exception", e)


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

def get_average_calories_burnt(user_id):
    try:
        mongo = mongoData(app).mongo
        keys = ['calories_burnt']
        # Get the current date and time in UTC
        current_utc_time = datetime.utcnow()

        # Define the Phoenix timezone
        phoenix_timezone = pytz.timezone('America/Phoenix')

        # Convert UTC time to Phoenix time
        current_phx_time = current_utc_time.replace(tzinfo=pytz.utc).astimezone(phoenix_timezone)

        from_time = datetime(current_utc_time.year, current_utc_time.month, current_phx_time.day, 0, 0, 0)
        to_time = datetime(current_utc_time.year, current_utc_time.month, current_phx_time.day, 23, 59, 59)

        # from_time = datetime.strptime(from_time, "%Y-%m-%dT%H:%M:%SZ")
        # to_time = datetime.strptime(to_time, "%Y-%m-%dT%H:%M:%SZ")

        print("From time", from_time)
        print("To time", to_time)

        # from_time = datetime.strptime('2023-11-30T00:00:00Z', "%Y-%m-%dT%H:%M:%SZ")
        # to_time = datetime.strptime('2023-11-30T23:59:59Z', "%Y-%m-%dT%H:%M:%SZ")
        query_filter = {
            'user_id': user_id,
            'timestamp': {'$gte': from_time, '$lte': to_time}
        }
        projection = {key: 1 for key in keys}
        projection['_id'] = 0
        projection['timestamp'] = 1
        user_attributes_collection = mongo.db.UserAttributes
        results = user_attributes_collection.find(query_filter, projection)
        db_entries = [result for result in results]
        calories_burnt_per_day = sum(entry['calories_burnt'] for entry in db_entries)
        return calories_burnt_per_day
    except Exception as e:
        print("Exception", e)

