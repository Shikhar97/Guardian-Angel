# constants.py
REGISTRATION_REQUIRED_FIELDS = ['name', 'email', 'phone', 'allergies', 'emergency_contact_name', 'emergency_contact_number']

USER_ATTRIBUTE_REQUIRED_FIELDS = ['heart_rate', 'respiratory_rate', 'steps_count', 'calories_burnt', 'blood_oxygen', 'sleep', 'timestamp']

USER_ATTRIBUTE_FETCH_KEYS = {'heart_rate', 'respiratory_rate', 'steps_count', 'calories_burnt', 'blood_oxygen', 'sleep'}

WEATHER_API_HOST = 'https://api.openweathermap.org/data/2.5/weather'
