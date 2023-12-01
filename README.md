# Guardian-Angel

Guardian Angel is an innovative Android application designed to enhance the well-being and safety of users by monitoring and providing personalized suggestions for various aspects of their daily lives. This multifaceted app leverages real-time data, including vital signs, location, weather conditions, and reproductive health, to deliver timely and tailored recommendations.

My assigned task involves the Sleep Wellness module within the app. When users activate the "Enable Sleep Wellness" toggle and specify their wake-up preference, the app retrieves historical sleep data and daily calorie expenditure from the backend. Utilizing this information, the Python server calculates the optimal wake-up time. Subsequently, an alarm with sound notification is configured to gently wake up the user at this optimal time. This functionality is designed to enhance the overall user experience by seamlessly integrating personalized sleep insights and health metrics, promoting a healthier wake-up routine for individuals using the app's Sleep Wellness feature.

## Steps to Run Android Code

1. Install Android Studio from [Download Android Studio](https://developer.android.com/studio#get-android-studio)
2. Open the project in Android Studio
3. Run the app on an emulator for better performance
4. Check if notification permission is given for the app. If not, kindly give the permission.

### Notes:
1. The app is designed to work on Android 10 and above
2. Kindly maintain a good internet connection since a poor internet connection can cause the HTTP requests to timeout

### For Immediate Testing:
The scheduler will run at 9 pm every day and set the alarm usually 5-9 hours later. To test the functionality, kindly follow the below steps:
1. To immediately test the functionality, in `AlarmSchedulerImpl.kt`, from line 38-45, kindly follow the instructions added as comments.
2. Also, it's a known issue that `AlarmSchedulerImpl.kt:53` `setRepeating` is unreliable on some devices. So, I've added `setExactAndAllowWhileIdle` as a backup.
3. In `AlarmReceiver.kt`, kindly follow the instructions added as comments for line 105-107.

### Issues That Can Be Faced:
1. No sound from the device (emulator) - set `hw.audioOutput=yes` in `config.ini`

## Steps to run python code
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

The python flask server is hosted on Heroku and can be accessed using the following link: [Guardian Angel Heroku](https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/)

I've written Swagger docs for the following APIs, which can be accessed [here](https://mc-guardian-angel-1fec5a1eb0b8.herokuapp.com/apidocs/).

To test it out in the hosted environment, kindly reach out to [aelango3@asu.edu](mailto:aelango3@asu.edu). I'll provide you with the auth token to access the APIs.

