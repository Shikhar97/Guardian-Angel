import numpy as np
import skfuzzy as fuzz
from skfuzzy import control as ctrl

def sleep_wellness_fuzzy():
    # Create fuzzy variables
    avg_sleep_time = ctrl.Antecedent(np.arange(0, 13, 1), 'avg_sleep_time')
    calories_burnt = ctrl.Antecedent(np.arange(0, 3001, 1), 'calories_burnt')
    preference = ctrl.Antecedent(np.arange(0, 13, 1), 'preference')
    wake_up_time = ctrl.Consequent(np.arange(240, 720, 1), 'wake_up_time')

    # Modify the code accordingly
    avg_sleep_time['short'] = fuzz.trapmf(avg_sleep_time.universe, [0, 0, 3, 5])
    avg_sleep_time['moderate'] = fuzz.trimf(avg_sleep_time.universe, [3, 6, 9])
    avg_sleep_time['long'] = fuzz.trapmf(avg_sleep_time.universe, [7, 9, 12, 12])

    calories_burnt['low'] = fuzz.trimf(calories_burnt.universe, [0, 0, 1500])
    calories_burnt['moderate'] = fuzz.trimf(calories_burnt.universe, [0, 1500, 3000])
    calories_burnt['high'] = fuzz.trimf(calories_burnt.universe, [1500, 3000, 3000])

    # heart_rate['low'] = fuzz.trapmf(heart_rate.universe, [0, 0, 25, 55])
    # heart_rate['medium'] = fuzz.trimf(heart_rate.universe, [50, 65, 80])
    # heart_rate['high'] = fuzz.trapmf(heart_rate.universe, [75, 105, 140, 140])

    # preference['early'] = fuzz.trimf(preference.universe, [0, 0, 5])
    # preference['normal'] = fuzz.trimf(preference.universe, [0, 5, 10])
    # preference['late'] = fuzz.trimf(preference.universe, [5, 10, 10])

    preference['early'] = fuzz.trimf(preference.universe, [0, 0, 5])
    preference['normal'] = fuzz.trimf(preference.universe, [4, 6, 8])
    preference['late'] = fuzz.trimf(preference.universe, [7, 12, 12])

    # Wake up time should be number of hours from now
    # very early should be in the range of 4-5 hours
    # early should be 5.5 to 6.5 hours
    # on time should be 6.5 to 8 hours

    # wake_up_time['very_early'] = fuzz.trimf(wake_up_time.universe, [0, 0, 15])
    # wake_up_time['early'] = fuzz.trimf(wake_up_time.universe, [0, 15, 30])
    # wake_up_time['normal'] = fuzz.trimf(wake_up_time.universe, [15, 30, 45])
    # wake_up_time['late'] = fuzz.trimf(wake_up_time.universe, [30, 45, 60])

    # Wake up time should be number of hours from now
    wake_up_time['very_early'] = fuzz.trimf(wake_up_time.universe, [240, 300, 360])  # 4 to 5 hours
    wake_up_time['early'] = fuzz.trimf(wake_up_time.universe, [330, 390, 450])  # 5.5 to 6.5 hours
    wake_up_time['normal'] = fuzz.trimf(wake_up_time.universe, [390, 480, 540])  # 6.5 to 8 hours
    wake_up_time['late'] = fuzz.trimf(wake_up_time.universe, [480, 600, 720])  # 8 to 11 hours

    # Visualize the fuzzy sets (optional)
    # avg_sleep_time.view()
    # calories_burnt.view()
    # heart_rate.view()
    # respiratory_rate.view()
    # preference.view()
    # wake_up_time.view()

    rules = []

    # Rule 1
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['low'] &
            preference['early']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 2
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['low'] &
            preference['normal']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 3
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['low'] &
            preference['late']
        ),
        consequent=wake_up_time['late']
    ))

    # Rule 4
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['moderate'] &
            preference['early']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 5
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['moderate'] &
            preference['normal']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 6
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['moderate'] &
            preference['late']
        ),
        consequent=wake_up_time['late']
    ))

    # Rule 7
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['high'] &
            preference['early']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 8
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['high'] &
            preference['normal']
        ),
        consequent=wake_up_time['late']
    ))

    # Rule 9
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['short'] & calories_burnt['high'] &
            preference['late']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 10
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['low'] &
            preference['early']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 11
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['low'] &
            preference['normal']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 12
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['low'] &
            preference['late']
        ),
        consequent=wake_up_time['late']
    ))

    # Rule 13
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['moderate'] &
            preference['early']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 14
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['moderate'] &
            preference['normal']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 15
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['moderate'] &
            preference['late']
        ),
        consequent=wake_up_time['late']
    ))

    # Rule 16
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['high'] &
            preference['early']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 17
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['high'] &
            preference['normal']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 18
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['moderate'] & calories_burnt['high'] &
            preference['late']
        ),
        consequent=wake_up_time['late']
    ))

    # Rule 19
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['low'] &
            preference['early']
        ),
        consequent=wake_up_time['very_early']
    ))

    # Rule 20
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['low'] &
            preference['normal']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 21
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['low'] &
            preference['late']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 22
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['moderate'] &
            preference['early']
        ),
        consequent=wake_up_time['very_early']
    ))

    # Rule 23
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['moderate'] &
            preference['normal']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 24
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['moderate'] &
            preference['late']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 25
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['high'] &
            preference['early']
        ),
        consequent=wake_up_time['early']
    ))

    # Rule 26
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['high'] &
            preference['normal']
        ),
        consequent=wake_up_time['normal']
    ))

    # Rule 27
    rules.append(ctrl.Rule(
        antecedent=(
            avg_sleep_time['long'] & calories_burnt['high'] &
            preference['late']
        ),
        consequent=wake_up_time['normal']
    ))

    # Create the fuzzy control system
    wake_up_ctrl = ctrl.ControlSystem(rules)
    wake_up_time_prediction = ctrl.ControlSystemSimulation(wake_up_ctrl)

    user_input = "normal"  # Mock data for now (Will be changed in Project 5)

    if user_input == "early":
        user_preference = 3
    elif user_input == "normal":
        user_preference = 6
    elif user_input == "late":
        user_preference = 9

    # Set input values
    wake_up_time_prediction.input['preference'] = user_preference


    # Set input values (you would replace these with real data)
    wake_up_time_prediction.input['avg_sleep_time'] = 5
    wake_up_time_prediction.input['calories_burnt'] = 1500
    # wake_up_time_prediction.input['heart_rate'] = 55
    # wake_up_time_prediction.input['respiratory_rate'] = 18

    # Compute the result
    wake_up_time_prediction.compute()

    # Print the output
    print("Predicted Wake-up Time:", wake_up_time_prediction.output['wake_up_time'])

    return int(wake_up_time_prediction.output['wake_up_time'])

