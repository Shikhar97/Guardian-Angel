import unittest
from flask import Flask, json
from flask_pymongo import PyMongo
from main import app
from data_access.mongoData import mongoData
from auth_middleware import VERIFICATION_KEY

class RestaurantsTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        app.config['TESTING'] = True
        cls.db = mongoData(app).mongo.db
        cls.app = app.test_client()

        mock_data = [
            {
                "name": "McDonalds",
                "rating": 4.5,
                "type_of_restaurant": "Fastfood"
            },
            {
                "name": "Subway",
                "rating": 4.2,
                "type_of_restaurant": "Sandwich"
            },
            {
                "name": "Pizza Hut",
                "rating": 4,
                "type_of_restaurant": "Italian"
            }
        ]
        cls.db.Restaurants.insert_many(mock_data)

    @classmethod
    def tearDownClass(cls):
        cls.db.get_collection('Restaurants').delete_many({})

    def test_get_restaurants(self):
        headers = {'Content-Type': 'application/json', 'X-Api-Auth': VERIFICATION_KEY}
        response = self.app.get('/restaurants', headers=headers)
        result = response.get_json()

        self.assertEqual(response.status_code, 200)
        self.assertIn('restaurants', result)
        restaurants = result['restaurants']
        self.assertEqual(len(restaurants), 3)

class RestaurantFoodsTest(unittest.TestCase):
    @classmethod
    def setUpClass(self):
        app.config['TESTING'] = True
        self.db = mongoData(app).mongo.db
        self.app = app.test_client()
        mock_restaurant_data = [
            {
                "name": "McDonalds",
                "rating": 4.5,
                "type_of_restaurant": "Fastfood"
            },
            {
                "name": "Subway",
                "rating": 4.2,
                "type_of_restaurant": "Sandwich"
            },
            {
                "name": "Pizza Hut",
                "rating": 4,
                "type_of_restaurant": "Italian"
            }
        ]
        self.db.Restaurants.insert_many(mock_restaurant_data)
        self.subway_id = self.db.Restaurants.find_one({'name': 'Subway'})['_id']
        mock_data = [
            {
                "restaurant_id": self.subway_id,
                "food_name": "Subway Burger",
                "price": 5.99,
                "calories": 550
            },
            {
                "restaurant_id": self.subway_id,
                "food_name": "Subway Sandwich",
                "price": 6.99,
                "calories": 450
            },
            {
                "restaurant_id": 2,
                "food_name": "Margherita Pizza",
                "price": 8.99,
                "calories": 950
            }
        ]
        self.db.Restaurant_Food.insert_many(mock_data)

    @classmethod
    def tearDownClass(self):
        self.db.get_collection('Restaurant_Food').delete_many({})
        self.db.get_collection('Restaurants').delete_many({})

    def test_get_foods_for_restaurant(self):
        restaurant_id = self.subway_id
        headers = {'Content-Type': 'application/json', 'X-Api-Auth': VERIFICATION_KEY}
        response = self.app.get(f'/restaurants/{restaurant_id}/foods', headers=headers)
        result = response.get_json()

        self.assertEqual(response.status_code, 200)
        self.assertIn('foods', result)
        foods = result['foods']
        self.assertEqual(len(foods), 2)

if __name__ == '__main__':
    unittest.main()
