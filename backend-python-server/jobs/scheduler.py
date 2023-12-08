from flask import Flask, jsonify, request
from flask_pymongo import PyMongo
from bson.objectid import ObjectId
from data_access.mongoData import mongoData
import logging
from dotenv import load_dotenv
import os
from apscheduler.jobstores.mongodb import MongoDBJobStore
from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.executors.pool import ThreadPoolExecutor, ProcessPoolExecutor
from pytz import utc
from data_collection.sensor_data import generate_mock_data

load_dotenv()

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(levelname)s - [Thread %(thread)d] - %(message)s')

app = Flask(__name__)

mongo_instance = mongoData(app)
db = mongo_instance.mongo.db
client = mongo_instance.mongo_client()
print(client)

jobstores = {
    'default': MongoDBJobStore(database=os.getenv('DB_NAME'), collection='SchedulerJobs', client=client)
}
executors = {
    'default': ThreadPoolExecutor(20),
    'processpool': ProcessPoolExecutor(5)
}
job_defaults = {
    'coalesce': False,
    'max_instances': 3
}

scheduler = BackgroundScheduler(jobstores=jobstores, executors=executors, job_defaults=job_defaults, timezone=utc)
scheduler.start()

scheduler_jobs_collection = db.SchedulerJobs

def job_function(user_id):
    print(f"Job executed for user_id: {user_id}")
    generate_mock_data(user_id)


def schedule_job(request):
    try:
        data = request.get_json()
        user_id = data.get('user_id')

        user_exists = db.User.find_one({'_id': ObjectId(user_id)})
        if not user_exists:
            return jsonify({'error': 'User not found'}), 404

        str_user_id = user_id
        user_id = ObjectId(user_id)
        job_exists = scheduler.get_job(str_user_id)
        if job_exists:
            return jsonify({'error': 'Job already scheduled for this user'}), 400

        interval = int(data.get('interval', 10))

        res = scheduler.add_job(
            job_function,
            'interval',
            seconds=interval,
            args=[user_id],
            id=str_user_id
        )

        return jsonify({'message': f'Job scheduled for user_id: {user_id} with interval: {interval} seconds'}), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500


def update_job(request, job_id):
    try:
        data = request.get_json()
        str_job_id = str(job_id)
        job_exists = scheduler.get_job(str_job_id)
        if not job_exists:
            return jsonify({'error': 'Job not found'}), 400

        interval = int(data.get('interval', 10))
        scheduler.reschedule_job(str_job_id, trigger='interval', seconds=interval)

        return jsonify({'message': f'Job updated for user_id: {str_job_id} with new interval: {interval} seconds'}), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500


def delete_job(request, job_id):
    try:
        str_job_id = str(job_id)

        job_exists = scheduler.get_job(str_job_id)
        if not job_exists:
            return jsonify({'error': 'Job not found'}), 400

        scheduler.remove_job(str_job_id)
        return jsonify({'message': f'Job deleted for user_id: {str_job_id}'}), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500


def get_all_job_stats():
    job_stats = []
    for job in scheduler.get_jobs():
        job_stat = {
            'job_id': job.id,
            'next_run_time': str(job.next_run_time) if job.next_run_time else None,
            'is_scheduled': job.next_run_time is not None
        }
        job_stats.append(job_stat)

    return jsonify({'job_stats': job_stats}), 200


if __name__ == '__main__':
    app.run(debug=True, use_reloader=False)
