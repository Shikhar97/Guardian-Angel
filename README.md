# Guardian Angel Health Monitoring System

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

![1](https://github.com/Shikhar97/Guardian-Angel/assets/122849057/22744241-153f-4afc-9579-f19e3c738967) ![2](https://github.com/Shikhar97/Guardian-Angel/assets/122849057/59a0c472-a156-449f-bc08-c4dbe29920dc)


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
