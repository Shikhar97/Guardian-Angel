import os
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build
from datetime import datetime, timedelta

SCOPES = [
    'https://www.googleapis.com/auth/fitness.heart_rate.read',
    'https://www.googleapis.com/auth/fitness.respiratory_rate.read',
    'https://www.googleapis.com/auth/fitness.sleep.read',
    'https://www.googleapis.com/auth/fitness.activity.read'
]

def authenticate_google_fit():
    creds = None
    if os.path.exists('token.json'):
        creds = Credentials.from_authorized_user_file('token.json')

    if not creds or not creds.valid:
        if creds and creds.expired and creds.refresh_token:
            creds.refresh(Request())
        else:
            flow = InstalledAppFlow.from_client_secrets_file(
                'credentials.json', SCOPES)
            creds = flow.run_local_server(port=0)

        with open('token.json', 'w') as token:
            token.write(creds.to_json())

    return creds

def get_heart_rate_data(service, start_time, end_time):
    data_source = "derived:com.google.heart_rate.bpm:com.google.android.gms:merge_heart_rate_bpm"

    # Define the request parameters
    request_params = {
        'aggregateBy': [{
            'dataTypeName': 'com.google.heart_rate.bpm',
            'dataSourceId': data_source
        }],
        'startTimeMillis': start_time,
        'endTimeMillis': end_time,
        'bucketByTime': {'durationMillis': 86400000},  # 1 day interval
    }

    try:
        response = service.users().dataset().aggregate(userId='me', body=request_params).execute()
        if 'bucket' in response:
            for bucket in response['bucket']:
                if 'dataset' in bucket:
                    for dataset in bucket['dataset']:
                        for point in dataset['point']:
                            if 'value' in point:
                                heart_rate = point['value'][0]['fpVal']
                                timestamp = datetime.utcfromtimestamp(point['startTimeNanos'] / 1e9)
                                print(f"Timestamp: {timestamp}, Heart Rate: {heart_rate} BPM")
        else:
            print("No heart rate data available.")

    except Exception as e:
        print(f"Error retrieving heart rate data: {e}")

def get_respiratory_rate_data(service, start_time, end_time):
    data_source = "derived:com.google.respiratory_rate.bpm:com.google.android.gms:merge_respiratory_rate_bpm"
    request_params = {
        'aggregateBy': [{
            'dataTypeName': 'com.google.respiratory_rate.bpm',
            'dataSourceId': data_source
        }],
        'startTimeMillis': start_time,
        'endTimeMillis': end_time,
        'bucketByTime': {'durationMillis': 86400000},  # 1 day interval
    }

    try:
        response = service.users().dataset().aggregate(userId='me', body=request_params).execute()
        if 'bucket' in response:
            for bucket in response['bucket']:
                if 'dataset' in bucket:
                    for dataset in bucket['dataset']:
                        for point in dataset['point']:
                            if 'value' in point:
                                respiratory_rate = point['value'][0]['fpVal']
                                timestamp = datetime.utcfromtimestamp(point['startTimeNanos'] / 1e9)
                                print(f"Respiratory Rate - Timestamp: {timestamp}, Respiratory Rate: {respiratory_rate} BPM")
        else:
            print("No respiratory rate data available.")

    except Exception as e:
        print(f"Error retrieving respiratory rate data: {e}")

def get_sleep_data(service, start_time, end_time):
    data_source = "derived:com.google.activity.segment:com.google.android.gms:segments"
    request_params = {
        'bucketByActivitySegment': {'minDurationMillis': 60000},
        'startTimeMillis': start_time,
        'endTimeMillis': end_time,
    }

    try:
        response = service.users().sessions().list(userId='me', body=request_params).execute()
        if 'session' in response:
            for session in response['session']:
                if 'activityType' in session and session['activityType'] == 72:  # 72 represents sleep
                    start_time = datetime.utcfromtimestamp(session['startTimeMillis'] / 1000)
                    end_time = datetime.utcfromtimestamp(session['endTimeMillis'] / 1000)
                    print(f"Sleep Session - Start Time: {start_time}, End Time: {end_time}")
        else:
            print("No sleep data available.")

    except Exception as e:
        print(f"Error retrieving sleep data: {e}")

def get_steps_count_data(service, start_time, end_time):
    data_source = "derived:com.google.step_count.delta:com.google.android.gms:estimated_steps"
    request_params = {
        'aggregateBy': [{
            'dataTypeName': 'com.google.step_count.delta',
            'dataSourceId': data_source
        }],
        'startTimeMillis': start_time,
        'endTimeMillis': end_time,
        'bucketByTime': {'durationMillis': 86400000},  # 1 day interval
    }

    try:
        response = service.users().dataset().aggregate(userId='me', body=request_params).execute()
        if 'bucket' in response:
            for bucket in response['bucket']:
                if 'dataset' in bucket:
                    for dataset in bucket['dataset']:
                        for point in dataset['point']:
                            if 'value' in point:
                                steps_count = point['value'][0]['intVal']
                                timestamp = datetime.utcfromtimestamp(point['startTimeNanos'] / 1e9)
                                print(f"Steps Count - Timestamp: {timestamp}, Steps Count: {steps_count}")
        else:
            print("No steps count data available.")

    except Exception as e:
        print(f"Error retrieving steps count data: {e}")

def get_calories_burnt_data(service, start_time, end_time):
    data_source = "derived:com.google.calories.expended:com.google.android.gms:merge_calories_expended"
    request_params = {
        'aggregateBy': [{
            'dataTypeName': 'com.google.calories.expended',
            'dataSourceId': data_source
        }],
        'startTimeMillis': start_time,
        'endTimeMillis': end_time,
        'bucketByTime': {'durationMillis': 86400000},  # 1 day interval
    }

    result = []

    try:
        response = service.users().dataset().aggregate(userId='me', body=request_params).execute()
        if 'bucket' in response:
            for bucket in response['bucket']:
                if 'dataset' in bucket:
                    for dataset in bucket['dataset']:
                        for point in dataset['point']:
                            if 'value' in point:
                                calories_burnt = point['value'][0]['fpVal']
                                timestamp = datetime.utcfromtimestamp(point['startTimeNanos'] / 1e9)
                                result.append({"Timestamp": timestamp, "Calories Burnt": calories_burnt})
        else:
            result.append("No calories burnt data available.")

    except Exception as e:
        result.append(f"Error retrieving calories burnt data: {e}")

    return result


def main():
    creds = authenticate_google_fit()
    service = build('fitness', 'v1', credentials=creds)

    end_time = int(datetime.now().timestamp()) * 1000000000  # Current time in nanoseconds
    start_time = end_time - int(timedelta(days=7).total_seconds()) * 1000000000  # 7 days ago

    results = {}

    # Retrieve and store heart rate data
    results["HeartRate"] = get_heart_rate_data(service, start_time, end_time)

    # Retrieve and store respiratory rate data
    results["RespiratoryRate"] = get_respiratory_rate_data(service, start_time, end_time)

    # Retrieve and store sleep data
    results["SleepData"] = get_sleep_data(service, start_time, end_time)

    # Retrieve and store steps count data
    results["StepsCount"] = get_steps_count_data(service, start_time, end_time)

    results["CaloriesBurnt"] = get_calories_burnt_data(service, start_time, end_time)

    return results
if __name__ == '__main__':
    main()
