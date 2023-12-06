# Guardian-Angel
Guardian Angel is an innovative Android application designed to enhance the well-being and safety of users by monitoring and providing personalized suggestions for various aspects of their daily lives. This multifaceted app leverages real-time data, including vital signs, location, weather conditions, and reproductive health, to deliver timely and tailored recommendations.

# Health Monitoring System

The Guardian Angel Health Monitoring System is a comprehensive feature designed to continuously monitor vital signs, including heart rate, respiratory rate, and step count. This system provides valuable insights into the user's overall well-being, promptly detecting irregularities or concerning trends. In critical situations, the app prompts the user to seek medical attention and has the capability to notify emergency contacts.

## Components

### 1. Android App

The Android app serves as the user interface, facilitating interaction and displaying relevant information. It utilizes various sensors to gather data on heart rate, respiratory rate, and step counts. The collected data is stored in a hosted database for further analysis.

### 2. Backend Implementation (Python)

The backend, implemented in Python, plays a crucial role in processing the sensor data. The data undergoes analysis using fuzzy logic inference rules to diagnose the user's health condition. The fuzzy logic API is hosted on Heroku, providing an accessible endpoint for the Android app to retrieve the diagnosis.

## User Flow

1. **Home Page:**
   - The app features a home page with a "Get Updates" button.
   - Clicking the button triggers the backend implementation.

2. **Diagnosis Page:**
   - After processing, the user is directed to a new page displaying the diagnosis.
   - Notifications are sent to ensure the user receives the message.

3. **Navigation:**
   - A "Back" button on the diagnosis page allows users to return to the home page.

![1](https://github.com/Shikhar97/Guardian-Angel/assets/122849057/2d37267e-faec-401d-9e8c-91bfc7982f48)

## How It Works

1. **Data Collection:**
   - Sensors on the Android device collect heart rate, respiratory rate, and step count data.

2. **Database Storage:**
   - The collected data is stored in a hosted database.

3. **Backend Analysis:**
   - The Python backend uses fuzzy logic inference rules to analyze the sensor data.

4. **Diagnosis Retrieval:**
   - The Android app calls the hosted fuzzy logic API on Heroku to retrieve the user's diagnosis.

5. **User Notification:**
   - Notifications are sent to the user to ensure timely awareness of their health status.

## Emergency Contacts

The app securely stores emergency contact information. In critical situations, the system can initiate contact with the designated emergency contacts.

This Health Monitoring System aims to proactively manage user health, offering a comprehensive solution for continuous well-being assessment.


# Feature -  Dietary Guidance
This feature takes health and diet preferences into its functionality, and takes a proactive approach to constantly monitor user activity, especially when at a restaurant, the app provides tailored food suggestions aligned with the user's health and diet preferences. 

### Instructions to run
An APK is attached. Follow the steps to see the suggestions:
1. Install the APK.
2. Start and allow the permissions
3. Wait for the app to see the current user's location(mocked location)
4. The suggested item would be displayed in the box below

*Currently the suggestions would be same as dummy hardcoded values are being used, once the feature is integrated with other components it will work as expected.