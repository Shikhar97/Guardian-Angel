import flask
import os
from flask import Flask, json, jsonify, request
from fuzzy_impl import health_monitoring_system

app = flask.Flask(__name__)

@app.route('/fuzzy')
def favicon():
    return "Fuzzy"

@app.route('/')
@app.route('/home')
def home():
    return "Hello World"

@app.route('/healthFuzzy', methods=['GET'])
def get_user_attributes():
    try:
        hr = request.args.get('hr', type=int)
        rr = request.args.get('rr', type=int)
        sc = request.args.get('sc', type=int)

        health_update = health_monitoring_system(hr, rr, sc)
        health_update_json = {
            'update': health_update
        }
        return jsonify(health_update_json)
    except Exception as e:
        print("Exception", e)
        return jsonify({'error': f'{"UserAttributeLocales.ERROR"}: {str(e)}'}), 500


if __name__ == "__main__":
    app.run(debug=True, use_reloader=False)
