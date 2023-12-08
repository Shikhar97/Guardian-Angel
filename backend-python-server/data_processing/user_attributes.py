from flask import Flask, request, jsonify, json
from flask_pymongo import PyMongo
from auth_middleware import token_required
from bson import ObjectId
from pymongo import IndexModel, ASCENDING
from datetime import datetime
from dateutil import parser
import requests
from bson.json_util import dumps
import traceback
from locales import UserAttributeLocales
from datetime import datetime, timedelta

def _average_values_per_day(keys, db_entries, static_keys):
    # print("DB entries", db_entries)
    try:
        daily_values = {}

        for entry in db_entries:
            timestamp = entry['timestamp'].date()
            if timestamp not in daily_values:
                daily_values[timestamp] = {f'{key}': [] for key in keys}

            for key in keys:
                if key in entry:
                    daily_values[timestamp][f'{key}'].append({"value": entry[key], "timestamp": entry['timestamp']})

        result = []
        # print(daily_values)
        for date, values in daily_values.items():
            daily_result = {str(date): {}}
            temp_result = _temp_calculation(daily_result, keys, values, str(date))
            result.append(temp_result)

        if static_keys == 'yes':
            post_result = []
            for res in result:
                temp_post_result = {}
                for date, metrics in res.items():
                    temp_post_result = {
                        "date": date,
                        "metrics": metrics
                    }
                post_result.append(temp_post_result)
            return post_result

        return result

    except Exception as e:
        exception_type = type(e).__name__
        print(f"Exception Type: {exception_type}")
        print(f"Exception Message: {str(e)}")
        traceback_info = traceback.format_exc()
        print(f"Traceback:\n{traceback_info}")

def _average_values_per_hour(keys, db_entries, static_keys):
    # print("DB entries", db_entries)
    try:
        hourly_values = {}
        for entry in db_entries:
            date_key = entry['timestamp'].strftime('%Y-%m-%d')
            hour_key = entry['timestamp'].hour
            if date_key not in hourly_values:
                hourly_values[date_key] = {hour_key: {f'{key}': [] for key in keys}}
            else:
                if hour_key not in hourly_values[date_key]:
                    hourly_values[date_key][hour_key] = {f'{key}': [] for key in keys}

            # for key in keys:
            #     if key in entry:
            #         hourly_values[date_key][str(hour_key)][f'{key}'].append({"value": entry[key], "timestamp": entry['timestamp']})

            for key in keys:
                if key in entry:
                    hourly_values[date_key][hour_key][f'{key}'].append({"value": entry[key], "timestamp": entry['timestamp']})
                    if key == "sleep":
                        if entry['timestamp'].hour == 0 and entry['timestamp'].minute == 0:
                            prev_date = (entry['timestamp'] - timedelta(days=1)).strftime('%Y-%m-%d')
                            if prev_date in hourly_values and 23 in hourly_values[prev_date]:
                                hourly_values[prev_date][23][f'{key}'].append({"value": entry[key], "timestamp": entry['timestamp']})
                        elif entry['timestamp'].hour == 23 and entry['timestamp'].minute == 50:
                            hourly_values[date_key][23][f'{key}'].append({"value": 1, "timestamp": entry['timestamp']+timedelta(minutes=10)})
                        elif entry['timestamp'].minute == 0:
                            prev_hour_key = (entry['timestamp'] - timedelta(hours=1)).hour
                            if prev_hour_key in hourly_values[date_key]:
                                hourly_values[date_key][prev_hour_key][f'{key}'].append({"value": entry[key], "timestamp": entry['timestamp']})

        # print("Hourly Values", hourly_values)
        result = []
        for date_key, hours in hourly_values.items():
            date_result = {str(date_key): {}}
            for hour_key, values in hours.items():
                date_result[str(date_key)][hour_key] = {}
                temp_result = _temp_calculation(date_result[str(date_key)], keys, values, hour_key)
                date_result[str(date_key)].update(temp_result)

            result.append(date_result)

        if static_keys == 'yes':
            post_result = []
            for day_data in result:
                day_data_hash = {}
                print("Day Data", day_data)
                for date, hourly_metrics in day_data.items():
                    day_data_hash['date'] = date
                    day_data_hash['data'] = []
                    for hour, metrics in hourly_metrics.items():
                        hourly_value_hash = {}
                        hourly_value_hash['hour'] = hour
                        
                        hourly_value_hash['metrics'] = metrics
                        day_data_hash['data'].append(hourly_value_hash)

                post_result.append(day_data_hash)
            return post_result

        return result

    except Exception as e:
        exception_type = type(e).__name__
        print(f"Exception Type: {exception_type}")
        print(f"Exception Message: {str(e)}")
        traceback_info = traceback.format_exc()
        print(f"Traceback:\n{traceback_info}")


def _temp_calculation(val_arr, keys, db_entries, factor):
    for key, value_list in db_entries.items():
        if key in ('blood_oxygen', 'heart_rate', 'respiratory_rate'):
            val_arr[factor][f'average_{key}'] = int(sum(entry['value'] for entry in value_list) / len(value_list)) if value_list else None
        else:
            if key == 'sleep':
                val_arr[factor]['sleep_time'] = _calculate_sleep_time('value', value_list)
            elif key == 'steps_count':
                val_arr[factor]['total_steps_count'] = sum(entry['value'] for entry in value_list)
            elif key == 'calories_burnt':
                val_arr[factor]['total_calories_burnt'] = sum(entry['value'] for entry in value_list)
    return val_arr

def _average_values_custom(keys, db_entries):
    keys_to_average = [key for key in keys if key in ('blood_oxygen', 'heart_rate', 'respiratory_rate')]
    final_values = {}
    if keys_to_average:
        final_values = _calculate_average_values(keys_to_average, db_entries)

    if 'sleep' in keys:
        final_values['sleep_time'] = _calculate_sleep_time('sleep', db_entries)

    if 'steps_count' in keys:
        final_values['total_steps_count'] = sum(entry['steps_count'] for entry in db_entries)

    if 'calories_burnt' in keys:
        final_values['total_calories_burnt'] = sum(entry['calories_burnt'] for entry in db_entries)

    return final_values

def _calculate_sleep_time(key, sleep_entries):
    sleep_time_total = 0

    sorted_sleep_entries = sorted(sleep_entries, key=lambda x: x['timestamp'])

    for i in range(1, len(sorted_sleep_entries)):
        if sorted_sleep_entries[i][key] == 0:
            continue
        sleep_start = sorted_sleep_entries[i - 1]['timestamp']
        sleep_end = sorted_sleep_entries[i]['timestamp']
        sleep_duration = sleep_end - sleep_start
        sleep_time_total += sleep_duration.total_seconds()

    return int(sleep_time_total)

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
            average_values[average_key] = round(average_values[average_key] / count_values[average_key])


    return average_values
