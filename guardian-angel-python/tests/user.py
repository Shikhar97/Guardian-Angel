import unittest
from flask import Flask
from flask_pymongo import PyMongo
from mongomock import MongoClient
from main import app
from pymongo import IndexModel, ASCENDING
from data_access.mongoData import mongoData
import os
from auth_middleware import VERIFICATION_KEY
from bson import ObjectId
from locales import UserAttributeLocales, UserRegistrationLocales
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

class UserRegistration(unittest.TestCase):
    @classmethod
    def setUpClass(self):
        app.config['TESTING'] = True
        # self.mongo_client = MongoClient()
        # app.config['MONGO_URI'] = 'mongodb://localhost:27017/testdb'
        # self.mongo_client = MongoClient(app.config['MONGO_URI'])
        # self.mongo = self.mongo_client.db
        self.db = mongoData(app).mongo.db
        user_collection = self.db.get_collection('User')
        user_collection.create_indexes([IndexModel([('email', ASCENDING)], unique=True)])
        self.app = app.test_client()

    @classmethod
    def tearDownClass(self):
        self.db.get_collection('User').delete_many({})

    def test_register_user(self):
        data = {
            'name': 'John Doe',
            'email': 'john.doe18@example.com',
            'phone': '1234567890',
            'allergies': ['peanuts', 'gluten'],
            'emergency_contact_name': 'Emergency Contact',
            'emergency_contact_number': '9876543210'
        }
        headers = {'Content-Type': 'application/json', 'X-Api-Auth': VERIFICATION_KEY}
        response = self.app.post('/users/register', json=data, headers=headers)
        result = response.get_json()
        self.assertEqual(response.status_code, 200)
        self.assertIn('message', result)
        self.assertIn('user_id', result)
        self.assertEqual(result['message'], 'User registered successfully')

    def test_register_user_duplicate(self):
        data = {
            'name': 'John Doe',
            'email': 'john.doe18@example.com',
            'phone': '1234567890',
            'allergies': ['peanuts', 'gluten'],
            'emergency_contact_name': 'Emergency Contact',
            'emergency_contact_number': '9876543210'
        }

        headers = {'Content-Type': 'application/json', 'X-Api-Auth': VERIFICATION_KEY}
        response = self.app.post('/users/register', json=data, headers=headers)
        result = response.get_json()
        self.assertEqual(response.status_code, 400)
        self.assertIn('error', result)
        self.assertEqual(result['error'], 'Email address is already registered')

    def test_register_user_missing_keys(self):
        data = {
            'name': 'John Doe',
            'email': 'john.doe14@example.com',
            'phone': '1234567890'
        }

        headers = {'Content-Type': 'application/json', 'X-Api-Auth': VERIFICATION_KEY}
        response = self.app.post('/users/register', json=data, headers=headers)
        result = response.get_json()
        self.assertEqual(response.status_code, 400)
        self.assertIn('error', result)
        self.assertIn('Missing required field', result['error'])

class UserAttributesTest(unittest.TestCase):
    @classmethod
    def setUpClass(self):
        app.config['TESTING'] = True
        self.db = mongoData(app).mongo.db
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
        self.db.get_collection('User_attributes').delete_many({})
        self.db.get_collection('User').delete_many({})

    def test_add_user_attributes_success(self):
        user_id = self.user_id
        data = {
            'heart_rate': 65,
            'respiratory_rate': 13,
            'steps_count': 500,
            'calories_burnt': 300,
            'blood_oxygen': 98,
            'sleep': 1,
            'timestamp': '2023-11-18T13:30:00Z'
        }

        headers = {'Content-Type': 'application/json', 'X-Api-Auth': VERIFICATION_KEY}
        response = self.app.post(f'/users/{user_id}/user_attributes', json=data, headers=headers)
        result = response.get_json()

        self.assertEqual(response.status_code, 200)
        self.assertIn('message', result)
        self.assertEqual(result['message'], UserAttributeLocales.USER_ATTRIBUTES_ADDED_SUCCESSFULLY)

        inserted_data = self.db.User_attributes.find_one({'user_id': user_id})
        self.assertIsNotNone(inserted_data)
        self.assertEqual(inserted_data['heart_rate'], 65)
        self.assertEqual(inserted_data['respiratory_rate'], 13)
        self.assertEqual(inserted_data['steps_count'], 500)
        self.assertEqual(inserted_data['calories_burnt'], 300)
        self.assertEqual(inserted_data['blood_oxygen'], 98)
        self.assertEqual(inserted_data['sleep'], 1)
        self.assertEqual(inserted_data['timestamp'], datetime.fromisoformat('2023-11-18T13:30:00Z').replace(tzinfo=None))

    def test_add_user_attributes_missing_fields(self):
        user_id = self.user_id
        data = {
            'heart_rate': 65,
            'respiratory_rate': 13,
            'steps_count': 500,
            'calories_burnt': 300,
            'blood_oxygen': 98,
            'sleep': 1
        }

        headers = {'Content-Type': 'application/json', 'X-Api-Auth': VERIFICATION_KEY}
        response = self.app.post(f'/users/{user_id}/user_attributes', json=data, headers=headers)
        result = response.get_json()

        self.assertEqual(response.status_code, 400)
        self.assertIn('error', result)
        self.assertIn('Missing required field', result['error'])

    def test_add_user_attributes_invalid_timestamp(self):
        user_id = self.user_id
        data = {
            'heart_rate': 65,
            'respiratory_rate': 13,
            'steps_count': 500,
            'calories_burnt': 300,
            'blood_oxygen': 98,
            'sleep': 1,
            'timestamp': 'invalid_timestamp'
        }

        headers = {'Content-Type': 'application/json', 'X-Api-Auth': VERIFICATION_KEY}
        response = self.app.post(f'/users/{user_id}/user_attributes', json=data, headers=headers)
        result = response.get_json()

        self.assertEqual(response.status_code, 400)
        self.assertIn('error', result)
        self.assertIn('Invalid timestamp format', result['error'])

    def test_get_user_attributes_success(self):
        headers = {'X-Api-Auth': VERIFICATION_KEY}
        current_date = datetime.now(timezone.utc)
        from_date = current_date - timedelta(weeks=1)
        to_date = current_date.replace(hour=23, minute=59, second=59, microsecond=999999)

        from_date_str = from_date.strftime("%Y-%m-%dT%H:%M:%SZ")
        to_date_str = to_date.strftime("%Y-%m-%dT%H:%M:%SZ")

        mock_data = [
            {
                'user_id': self.user_id,
                'heart_rate': 65,
                'respiratory_rate': 13,
                'steps_count': 500,
                'calories_burnt': 300,
                'blood_oxygen': 95,
                'timestamp': datetime.now(timezone.utc)
            },
            {
                'user_id': self.user_id,
                'heart_rate': 70,
                'respiratory_rate': 15,
                'steps_count': 600,
                'calories_burnt': 400,
                'blood_oxygen': 92,
                'timestamp': datetime.now(timezone.utc)
            }
        ]

        user_attributes_collection = self.db.User_attributes
        user_attributes_collection.insert_many(mock_data)

        response = self.app.get(f'/users/{self.user_id}/user_attributes?keys=heart_rate,respiratory_rate,steps_count,calories_burnt,blood_oxygen&from={from_date_str}&to={to_date_str}', headers=headers)
        self.assertEqual(response.status_code, 200)
        result = response.get_json()

        self.assertIn('average_heart_rate', result)
        self.assertEqual(result['average_heart_rate'], (65 + 70) / 2)

        self.assertIn('average_respiratory_rate', result)
        self.assertEqual(result['average_respiratory_rate'], (13 + 15) / 2)

        self.assertIn('total_steps_count', result)
        self.assertEqual(result['total_steps_count'], 500 + 600)

        self.assertIn('total_calories_burnt', result)
        self.assertEqual(result['total_calories_burnt'], 300 + 400)

        self.assertIn('average_blood_oxygen', result)
        self.assertEqual(result['average_blood_oxygen'], (95 + 92) / 2)

        user_attributes_collection.delete_many({'user_id': self.user_id})

    def test_get_user_attributes_success_sleep_time(self):
        headers = {'X-Api-Auth': VERIFICATION_KEY}
        current_date = datetime.now(timezone.utc)
        yesterday = current_date - timedelta(days=1)
        current_date = yesterday.replace(hour=10, minute=0, second=0, microsecond=0)
        from_date = current_date.replace(hour=0, minute=0, second=0, microsecond=0)
        to_date = current_date.replace(hour=23, minute=59, second=59, microsecond=999999)

        from_date_str = from_date.strftime("%Y-%m-%dT%H:%M:%SZ")
        to_date_str = to_date.strftime("%Y-%m-%dT%H:%M:%SZ")

        mock_data = [
            {'user_id': self.user_id, 'sleep': 0, 'timestamp': current_date - timedelta(minutes=120)},
            {'user_id': self.user_id, 'sleep': 1, 'timestamp': current_date - timedelta(minutes=110)},
            {'user_id': self.user_id, 'sleep': 0, 'timestamp': current_date - timedelta(minutes=100)},
            {'user_id': self.user_id, 'sleep': 1, 'timestamp': current_date - timedelta(minutes=90)},
            {'user_id': self.user_id, 'sleep': 1, 'timestamp': current_date - timedelta(minutes=80)},
            {'user_id': self.user_id, 'sleep': 0, 'timestamp': current_date - timedelta(minutes=70)},
            {'user_id': self.user_id, 'sleep': 1, 'timestamp': current_date - timedelta(minutes=60)},
            {'user_id': self.user_id, 'sleep': 1, 'timestamp': current_date - timedelta(minutes=50)},
            {'user_id': self.user_id, 'sleep': 0, 'timestamp': current_date - timedelta(minutes=40)},
            {'user_id': self.user_id, 'sleep': 1, 'timestamp': current_date - timedelta(minutes=30)},
            {'user_id': self.user_id, 'sleep': 1, 'timestamp': current_date - timedelta(minutes=20)},
            {'user_id': self.user_id, 'sleep': 1, 'timestamp': current_date - timedelta(minutes=10)},
            {'user_id': self.user_id, 'sleep': 1, 'timestamp': current_date},
        ]

        user_attributes_collection = self.db.User_attributes
        user_attributes_collection.insert_many(mock_data)

        response = self.app.get(f'/users/{self.user_id}/user_attributes?keys=sleep&from={from_date_str}&to={to_date_str}', headers=headers)
        self.assertEqual(response.status_code, 200)
        result = response.get_json()

        # Asserting results for sleep time
        self.assertIn('sleep_time', result)
        self.assertEqual(result['sleep_time'], 5400)

        user_attributes_collection.delete_many({'user_id': self.user_id})

    def test_get_user_attributes_invalid_timestamp_format(self):
        user_id = self.user_id
        headers = {'X-Api-Auth': VERIFICATION_KEY}
        response = self.app.get(f'/users/{user_id}/user_attributes?keys=heart_rate&from=invalid_timestamp', headers=headers)
        self.assertEqual(response.status_code, 400)
        result = response.get_json()
        self.assertIn('error', result)
        self.assertIn('Invalid timestamp format', result['error'])

    def test_get_user_attributes_invalid_keys(self):
        user_id = self.user_id
        headers = {'X-Api-Auth': VERIFICATION_KEY}
        current_date = datetime.now(timezone.utc)
        from_date = current_date - timedelta(weeks=1)
        to_date = current_date.replace(hour=23, minute=59, second=59, microsecond=999999)
        from_date_str = from_date.strftime("%Y-%m-%dT%H:%M:%SZ")
        to_date_str = to_date.strftime("%Y-%m-%dT%H:%M:%SZ")
        response = self.app.get(f'/users/{user_id}/user_attributes?keys=ashwin&from={from_date_str}&to={to_date_str}', headers=headers)
        self.assertEqual(response.status_code, 400)
        result = response.get_json()
        self.assertIn('error', result)
        self.assertIn('Invalid keys', result['error'])

if __name__ == '__main__':
    unittest.main()
