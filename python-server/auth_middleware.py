from functools import wraps
from flask import request
import os

# Will be used in deployment
# VERIFICATION_KEY = os.getenv('THEIA_CORE_VERIFICATION_KEY')
VERIFICATION_KEY = 'dummy_verification_key'

def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        if "X-Api-Auth" in request.headers:
            token = request.headers["X-Api-Auth"]
        if not token:
            return {
                "message": "Authentication Token is missing!",
                "data": None,
                "error": "Unauthorized"
            }, 401

        try:
            if token != VERIFICATION_KEY:
                return {
                    "message": "Invalid Authentication token!",
                    "data": None,
                    "error": "Unauthorized"
                }, 401

        except Exception as e:
            return {
                "message": "Something went wrong",
                "data": None,
                "error": str(e)
            }, 500

        # Pass the extracted variables to the decorated function
        return f(*args, **kwargs)

    return decorated
