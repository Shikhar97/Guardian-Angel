# Guardian-Angel

Guardian Angel is an innovative Android application designed to enhance the well-being and safety of users by monitoring and providing personalized suggestions for various aspects of their daily lives. This multifaceted app leverages real-time data, including vital signs, location, weather conditions, and reproductive health, to deliver timely and tailored recommendations.

My particular functionality deals with the sleep wellness module. Get the historic sleep data, amount of calories burnt that day, and users' preference of when to wake up. An optimal wake-up time is calculated, and the user is notified to wake up at that time.

## Steps to run
1. Use Python 3.11
2. Run `pip install -r requirements.txt`
3. Create a `.env` file in the root directory and add the following variables:
    ```env
    DB_URI=mongodb+srv://hkeerth1:EcMvR8LEBvmb72dG@cluster0.sdycyfj.mongodb.net/Guardian-Angel?retryWrites=true&w=majority
    TEST_DB_URI=mongodb://127.0.0.1:27017/testdb?directConnection=true&serverSelectionTimeoutMS=2000&appName=mongosh+2.0.2
    DB_NAME=GuardianAngel
    ```
4. Run `python3 -u "<path to directory>/main.py"`
5. To run tests, run the following command:
    ```bash
    export auth_verification_key=test
    python -m unittest -vv tests.user
    python -m unittest -vv tests.restaurant
    python -m unittest -vv tests.weather
    ```

The project is hosted on Heroku and can be accessed using the following link: [Guardian Angel Heroku](https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/)

I've written Swagger docs for the following APIs, which can be accessed [here](https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/apidocs/).

To test it out in the hosted environment, kindly reach out to [aelango3@asu.edu](mailto:aelango3@asu.edu). I'll provide you with the auth token to access the APIs.
