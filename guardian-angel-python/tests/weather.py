import unittest
from flask import Flask
from flask_pymongo import PyMongo
from mongomock import MongoClient
from main import app
from unittest.mock import patch
from auth_middleware import VERIFICATION_KEY

class WeatherTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        app.config['TESTING'] = True
        cls.app = app.test_client()

    def test_get_weather_by_coordinates(self):
        with patch('requests.get') as mock_get:
            mock_get.return_value.status_code = 200
            mock_get.return_value.json.return_value = {
                'main': {'temp': 22, 'humidity': 75},
                'weather': [{'description': 'Clear'}],
                'wind': {'speed': 5.5}
            }

            headers = {'X-Api-Auth': VERIFICATION_KEY}
            response = self.app.get('/weather?lat=35.6895&lon=139.6917', headers=headers)

            self.assertEqual(response.status_code, 200)
            result = response.get_json()
            self.assertIn('weather', result)
            weather_data = result['weather']
            self.assertEqual(weather_data['temperature'], 22)
            self.assertEqual(weather_data['description'], 'Clear')
            self.assertEqual(weather_data['humidity'], 75)
            self.assertEqual(weather_data['wind_speed'], 5.5)

    def test_get_weather_by_city(self):
        with patch('requests.get') as mock_get:
            mock_get.return_value.status_code = 200
            mock_get.return_value.json.return_value = {
                'main': {'temp': 22, 'humidity': 75},
                'weather': [{'description': 'Clear'}],
                'wind': {'speed': 5.5}
            }

            headers = {'X-Api-Auth': VERIFICATION_KEY}
            response = self.app.get('/weather?city=Tokyo', headers=headers)

            self.assertEqual(response.status_code, 200)
            result = response.get_json()
            self.assertIn('weather', result)
            weather_data = result['weather']
            self.assertEqual(weather_data['temperature'], 22)
            self.assertEqual(weather_data['description'], 'Clear')
            self.assertEqual(weather_data['humidity'], 75)
            self.assertEqual(weather_data['wind_speed'], 5.5)

    def test_get_weather_missing_parameters(self):
        headers = {'X-Api-Auth': VERIFICATION_KEY}
        response = self.app.get('/weather', headers=headers)
        self.assertEqual(response.status_code, 400)
        result = response.get_json()
        self.assertIn('error', result)
        self.assertIn('Missing required parameters', result['error'])

if __name__ == '__main__':
    unittest.main()
