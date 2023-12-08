# locales.py

class UserRegistrationLocales:
    MISSING_REQUIRED_FIELD = 'Missing required field: {}'
    EMAIL_ALREADY_REGISTERED = 'Email address is already registered'
    USER_REGISTERED_SUCCESSFULLY = 'User registered successfully'
    ERROR = 'Error'

class UserAttributeLocales:
    INVALID_USER_ID_FORMAT = 'Invalid user_id format'
    INVALID_KEYS = 'Invalid keys. Allowed keys are: heart_rate, respiratory_rate, steps_count, calories_burnt, blood_oxygen'
    INVALID_TIMESTAMP_FORMAT = 'Invalid timestamp format. Use ISO 8601 format.'
    ERROR = 'Error'
    MISSING_REQUIRED_FIELD = 'Missing required field: {}'
    USER_ATTRIBUTES_ADDED_SUCCESSFULLY = 'User attributes added successfully'
    INVALID_USER_PREFERENCES = 'Invalid user preferences. Allowed values are: early, normal, late'
    INVALID_EVENT_ID = 'Invalid event_id'
    USER_NOT_FOUND = 'User not found'

class RestaurantFoodLocales:
    INVALID_RESTAURANT_ID_FORMAT = 'Invalid restaurant_id'

class WeatherLocales:
    MISSING_PARAMETERS = 'Missing required parameters: lat and lon or city'
    DATA_FETCH_FAILED = 'Failed to retrieve weather data'
