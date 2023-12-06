import numpy as np
import skfuzzy as fuzz
from skfuzzy import control as ctrl

def weather_recommendation(uv_index, rain_intensity, temperature):
    # Antecedent (input) variables
    uv_index_var = ctrl.Antecedent(np.arange(0, 11, 1), 'UV Index')
    rain_intensity_var = ctrl.Antecedent(np.arange(0, 11, 1), 'Rain Intensity')
    temperature_var = ctrl.Antecedent(np.arange(0, 101, 1), 'Temperature')

    # Consequent (output) variable
    weather_recommendation_var = ctrl.Consequent(np.arange(0, 101, 1), 'Weather Recommendation')

    # Membership functions for UV Index
    uv_index_var['low'] = fuzz.trimf(uv_index_var.universe, [0, 0, 5])
    uv_index_var['medium'] = fuzz.trimf(uv_index_var.universe, [0, 5, 10])
    uv_index_var['high'] = fuzz.trimf(uv_index_var.universe, [5, 10, 10])

    # Membership functions for Rain Intensity
    rain_intensity_var['low'] = fuzz.trimf(rain_intensity_var.universe, [0, 0, 5])
    rain_intensity_var['medium'] = fuzz.trimf(rain_intensity_var.universe, [0, 5, 10])
    rain_intensity_var['high'] = fuzz.trimf(rain_intensity_var.universe, [5, 10, 10])

    # Membership functions for Temperature
    temperature_var['cool'] = fuzz.trimf(temperature_var.universe, [0, 0, 50])
    temperature_var['moderate'] = fuzz.trimf(temperature_var.universe, [0, 50, 100])
    temperature_var['hot'] = fuzz.trimf(temperature_var.universe, [50, 100, 100])

    # Membership functions for Weather Recommendation
    weather_recommendation_var['no_action'] = fuzz.trimf(weather_recommendation_var.universe, [0, 0, 50])
    weather_recommendation_var['carry_umbrella'] = fuzz.trimf(weather_recommendation_var.universe, [0, 50, 100])
    weather_recommendation_var['apply_sunscreen'] = fuzz.trimf(weather_recommendation_var.universe, [50, 100, 100])

    # Define the rules
    rule1 = ctrl.Rule(uv_index_var['high'] | rain_intensity_var['high'], weather_recommendation_var['carry_umbrella'])
    rule2 = ctrl.Rule(uv_index_var['medium'] | temperature_var['hot'], weather_recommendation_var['apply_sunscreen'])
    rule3 = ctrl.Rule(temperature_var['cool'], weather_recommendation_var['no_action'])

    rule4 = ctrl.Rule(uv_index_var['low'] & rain_intensity_var['low'], weather_recommendation_var['no_action'])
    rule5 = ctrl.Rule(uv_index_var['medium'] & rain_intensity_var['medium'] & temperature_var['cool'], weather_recommendation_var['no_action'])
    rule6 = ctrl.Rule(uv_index_var['high'] & rain_intensity_var['high'] & temperature_var['hot'], weather_recommendation_var['carry_umbrella'])
    rule7 = ctrl.Rule(uv_index_var['medium'] & rain_intensity_var['high'], weather_recommendation_var['carry_umbrella'])
    rule8 = ctrl.Rule(uv_index_var['low'] & temperature_var['cool'], weather_recommendation_var['no_action'])
    rule9 = ctrl.Rule(rain_intensity_var['medium'] & temperature_var['moderate'], weather_recommendation_var['no_action'])

    weather_ctrl = ctrl.ControlSystem([rule1, rule2, rule3, rule4, rule5, rule6, rule7, rule8, rule9])

    weather_simulation = ctrl.ControlSystemSimulation(weather_ctrl)

    weather_simulation.input['UV Index'] = uv_index
    weather_simulation.input['Rain Intensity'] = rain_intensity
    weather_simulation.input['Temperature'] = temperature

    weather_simulation.compute()

    weather_labels = {
        0: 'No Action',
        50: 'Carry Umbrella',
        100: 'Apply Sunscreen'
    }

    result = weather_simulation.output['Weather Recommendation']
    nearest_label = min(weather_labels.items(), key=lambda x: abs(x[0] - result))[1]

    print(result)
    return nearest_label
    #return weather_simulation.output['Weather Recommendation']

uv_index_input = 10
rain_intensity_input = 4
temperature_input = 60

result = weather_recommendation(uv_index_input, rain_intensity_input, temperature_input)
print("Weather Recommendation:", result)
