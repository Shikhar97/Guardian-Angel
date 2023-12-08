import unittest
from unittest.mock import MagicMock, patch
from main import app
from feature_modules.sleep_wellness.controller  import optimal_wake_up_time, get_average_sleep_time, get_average_calories_burnt
from auth_middleware import VERIFICATION_KEY
from flask import Flask
from flask_pymongo import PyMongo
from mongomock import MongoClient
from pymongo import IndexModel, ASCENDING
from data_access.mongoData import mongoData
import secrets
import string
from datetime import datetime, timezone, timedelta

def generate_random_string(length):
    characters = string.ascii_lowercase + string.digits
    return ''.join(secrets.choice(characters) for _ in range(length))

def generate_random_email():
    username = generate_random_string(8)
    domain = generate_random_string(6) + ".com"
    return f"{username}@{domain}"

class TestWakeUpTimeEndpoint(unittest.TestCase):

    @classmethod
    def setUpClass(self):
        app.config['TESTING'] = True
        self.db = mongoData(app).mongo.db
        user_collection = self.db.get_collection('User')
        user_collection.create_indexes([IndexModel([('email', ASCENDING)], unique=True)])
        self.app = app.test_client()
        data = {
            'name': 'John Doe',
            'email': generate_random_email(),
            'phone': '1234567890',
            'allergies': ['peanuts', 'gluten'],
            'emergency_contact_name': 'Emergency Contact',
            'emergency_contact_number': '9876543210'
        }
        headers = {'Content-Type': 'application/json', 'X-Api-Auth': VERIFICATION_KEY}
        response = self.app.post('/users/register', json=data, headers=headers)
        result = response.get_json()
        self.user_id = result['user_id']

    @classmethod
    def tearDownClass(self):
        self.db.get_collection('User').delete_many({})

    @patch('feature_modules.sleep_wellness.controller.optimal_wake_up_time')
    def test_wake_up_time_endpoint(self, mock_optimal_wake_up_time):
        mock_optimal_wake_up_time.return_value = 420
        headers = {'Content-Type': 'application/json', 'X-Api-Auth': VERIFICATION_KEY}

        response = self.app.get(f'/users/{self.user_id}/wake_up_time?user_preference=normal', headers=headers)

        # Assertions
        self.assertEqual(response.status_code, 200)
        result = response.get_json()
        self.assertIn('wake_up_time', result)

    @patch('feature_modules.sleep_wellness.controller.get_average_sleep_time')
    @patch('feature_modules.sleep_wellness.controller.get_average_calories_burnt')
    def test_optimal_wake_up_time(self, mock_get_average_sleep_time, mock_get_average_calories_burnt):
        mock_get_average_sleep_time.return_value = 7  # Mocking average sleep time in hours
        mock_get_average_calories_burnt.return_value = 2000  # Mocking average calories burnt per day

        optimal_wake_up_time(self.user_id, 'normal')

        mock_get_average_sleep_time.assert_called_with(self.user_id)
        mock_get_average_calories_burnt.assert_called_with(self.user_id)

    @patch('feature_modules.sleep_wellness.controller.optimal_wake_up_time')
    def test_wake_up_time_endpoint_with_invalid_user_id(self, mock_optimal_wake_up_time):
        # Mock optimal_wake_up_time function
        mock_optimal_wake_up_time.return_value = 420
        headers = {'Content-Type': 'application/json', 'X-Api-Auth': VERIFICATION_KEY}

        # Make a request with an invalid user_id
        response = self.app.get('/users/invalid_user_id/wake_up_time?user_preference=normal', headers=headers)

        # Assertions
        self.assertEqual(response.status_code, 400)

    @patch('feature_modules.sleep_wellness.controller.optimal_wake_up_time')
    def test_wake_up_time_endpoint_without_auth_header(self, mock_optimal_wake_up_time):
        # Mock optimal_wake_up_time function
        mock_optimal_wake_up_time.return_value = 420

        # Make a request without the X-Api-Auth header
        response = self.app.get(f'/users/{self.user_id}/wake_up_time?user_preference=normal')

        # Assertions
        self.assertEqual(response.status_code, 401)

    @patch('feature_modules.sleep_wellness.controller.optimal_wake_up_time')
    def test_wake_up_time_endpoint_with_invalid_auth_header(self, mock_optimal_wake_up_time):
        # Mock optimal_wake_up_time function
        mock_optimal_wake_up_time.return_value = 420
        headers = {'Content-Type': 'application/json', 'X-Api-Auth': 'invalid_key'}

        # Make a request with an invalid X-Api-Auth header
        response = self.app.get(f'/users/{self.user_id}/wake_up_time?user_preference=normal', headers=headers)

        # Assertions
        self.assertEqual(response.status_code, 401)

if __name__ == '__main__':
    unittest.main()
