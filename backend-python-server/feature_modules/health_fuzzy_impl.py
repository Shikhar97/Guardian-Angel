import numpy as np
import skfuzzy as fuzz
from skfuzzy import control as ctrl

def health_monitoring_system(heart_rate_val, respiratory_rate_val, steps_count_val):

    extremeCaseStr = handleExtremeCases(heart_rate_val, respiratory_rate_val, steps_count_val)
    if(len(extremeCaseStr) != 0):
        return extremeCaseStr

    heart_rate = ctrl.Antecedent(np.arange(20, 250, 1), 'heart_rate')
    respiratory_rate = ctrl.Antecedent(np.arange(5, 30, 1), 'respiratory_rate')
    steps_count = ctrl.Antecedent(np.arange(2000, 15000, 1), 'steps_count')
    overall_well_being = ctrl.Consequent(np.arange(0, 100, 1), 'overall_well_being')

    heart_rate['low'] = fuzz.trimf(heart_rate.universe, [20, 40, 60])
    heart_rate['normal'] = fuzz.trimf(heart_rate.universe, [50, 75, 120])
    heart_rate['high'] = fuzz.trimf(heart_rate.universe, [110, 160, 250])

    respiratory_rate['low'] = fuzz.trimf(respiratory_rate.universe, [5, 8, 12])
    respiratory_rate['normal'] = fuzz.trimf(respiratory_rate.universe, [8, 16, 22])
    respiratory_rate['high'] = fuzz.trimf(respiratory_rate.universe, [18, 24, 30])

    steps_count['low'] = fuzz.trimf(steps_count.universe, [2000, 3000, 5000])
    steps_count['normal'] = fuzz.trimf(steps_count.universe, [4000, 7000, 9000])
    steps_count['high'] = fuzz.trimf(steps_count.universe, [8000, 12000, 15000])

    overall_well_being['critical'] = fuzz.trimf(overall_well_being.universe, [0, 25, 50])
    overall_well_being['concerning'] = fuzz.trimf(overall_well_being.universe, [25, 50, 75])
    overall_well_being['normal'] = fuzz.trimf(overall_well_being.universe, [50, 75, 100])

    rules = []



    rules.append(ctrl.Rule(heart_rate['low'] & respiratory_rate['low'], overall_well_being['critical']))
    rules.append(ctrl.Rule(heart_rate['normal'] & respiratory_rate['normal'], overall_well_being['normal']))
    rules.append(ctrl.Rule(heart_rate['high'] & respiratory_rate['high'], overall_well_being['concerning']))
    rules.append(ctrl.Rule(heart_rate['low'] | respiratory_rate['low'], overall_well_being['concerning']))
    rules.append(ctrl.Rule(heart_rate['high'] | respiratory_rate['high'], overall_well_being['concerning']))
    rules.append(ctrl.Rule(heart_rate['normal'] & respiratory_rate['high'], overall_well_being['concerning']))
    rules.append(ctrl.Rule(heart_rate['high'] & respiratory_rate['normal'], overall_well_being['concerning']))
    rules.append(ctrl.Rule(heart_rate['low'] & respiratory_rate['high'], overall_well_being['critical']))
    rules.append(ctrl.Rule(heart_rate['high'] | respiratory_rate['low'], overall_well_being['concerning']))
    rules.append(ctrl.Rule(heart_rate['normal'] | respiratory_rate['normal'], overall_well_being['normal']))
    rules.append(ctrl.Rule((heart_rate['high'] | heart_rate['normal']) & steps_count['low'], overall_well_being['concerning']))
    rules.append(ctrl.Rule(heart_rate['normal'] & respiratory_rate['normal'] & steps_count['normal'], overall_well_being['normal']))
    rules.append(ctrl.Rule(heart_rate['normal'] & respiratory_rate['normal'] & steps_count['high'], overall_well_being['normal']))
    rules.append(ctrl.Rule(heart_rate['low'] & respiratory_rate['normal'] & steps_count['normal'], overall_well_being['normal']))
    rules.append(ctrl.Rule(heart_rate['normal'] & respiratory_rate['low'] & steps_count['high'], overall_well_being['normal']))

    health_monitoring_ctrl = ctrl.ControlSystem(rules)
    health_monitoring = ctrl.ControlSystemSimulation(health_monitoring_ctrl)

    health_monitoring.input['heart_rate'] = heart_rate_val
    health_monitoring.input['respiratory_rate'] = respiratory_rate_val
    health_monitoring.input['steps_count'] = steps_count_val

    health_monitoring.compute()

    result = health_monitoring.output['overall_well_being']

    return analyseResult(result, steps_count_val)

def handleExtremeCases(heart_rate_val, respiratory_rate_val, steps_count_val):
    if (heart_rate_val == 0 or respiratory_rate_val == 0):
        return "Deceased"
    if (heart_rate_val > 250 or heart_rate_val < 20 or respiratory_rate_val > 30 or heart_rate_val < 5):
        return "Critical"
    return ""

def analyseResult(result, steps_count_val):
    if (result < 35):
        return "Critical! Visit a doctor ASAP"
    elif (result < 60):
        return "Concerning. Please visit a doctor if you are unwell"
    elif (result < 70):
        return "Slightly abnormal trends noticed. Based on the health score, it's possible you might be working out or sleeping. Consider visiting a doctor otherwise"
    elif (result < 80):
        if (steps_count_val > 15000):
            return "Walked too much in the last hour. Otherwise you are healthy"
        elif (steps_count_val < 2000):
            return "Very less steps walked in the last hour. Otherwise you are healthy"
        return "Normal"
    else:
        if (steps_count_val > 15000):
            return "Walked too much in the last hour. Otherwise you are healthy"
        elif (steps_count_val < 2000):
            return "Very less steps walked in the last hour. Otherwise you are healthy"
        return "Please share your fitness secrets"

heart_rate_input = 80
respiratory_rate_input = 20
steps_count_input = 8000

result = health_monitoring_system(heart_rate_input, respiratory_rate_input, steps_count_input)
print("Overall Well-being:", result)
