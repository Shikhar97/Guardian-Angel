import numpy as np
import skfuzzy as fuzz
from skfuzzy import control as ctrl

def health_monitoring_system(heart_rate_val, respiratory_rate_val, steps_count_val):
    heart_rate = ctrl.Antecedent(np.arange(0, 200, 1), 'heart_rate')
    respiratory_rate = ctrl.Antecedent(np.arange(0, 40, 1), 'respiratory_rate')
    steps_count = ctrl.Antecedent(np.arange(0, 10000, 1), 'steps_count')
    overall_well_being = ctrl.Consequent(np.arange(0, 100, 1), 'overall_well_being')

    heart_rate['low'] = fuzz.trimf(heart_rate.universe, [0, 60, 100])
    heart_rate['normal'] = fuzz.trimf(heart_rate.universe, [60, 100, 140])
    heart_rate['high'] = fuzz.trimf(heart_rate.universe, [100, 140, 200])

    respiratory_rate['low'] = fuzz.trimf(respiratory_rate.universe, [0, 10, 20])
    respiratory_rate['normal'] = fuzz.trimf(respiratory_rate.universe, [10, 20, 30])
    respiratory_rate['high'] = fuzz.trimf(respiratory_rate.universe, [20, 30, 40])

    steps_count['low'] = fuzz.trimf(steps_count.universe, [0, 3000, 5000])
    steps_count['normal'] = fuzz.trimf(steps_count.universe, [3000, 5000, 7000])
    steps_count['high'] = fuzz.trimf(steps_count.universe, [5000, 7000, 10000])

    overall_well_being['critical'] = fuzz.trimf(overall_well_being.universe, [0, 25, 50])
    overall_well_being['concerning'] = fuzz.trimf(overall_well_being.universe, [25, 50, 75])
    overall_well_being['normal'] = fuzz.trimf(overall_well_being.universe, [50, 75, 100])

    rules = []

    rules.append(ctrl.Rule(heart_rate['high'] & respiratory_rate['high'], overall_well_being['critical']))
    rules.append(ctrl.Rule((heart_rate['high'] | heart_rate['normal']) & steps_count['low'], overall_well_being['concerning']))
    rules.append(ctrl.Rule(respiratory_rate['low'] & (heart_rate['normal'] | heart_rate['low']), overall_well_being['concerning']))
    rules.append(ctrl.Rule(heart_rate['normal'] & respiratory_rate['normal'] & steps_count['normal'], overall_well_being['normal']))
    rules.append(ctrl.Rule(steps_count['low'], overall_well_being['concerning']))
    rules.append(ctrl.Rule(respiratory_rate['high'] & heart_rate['normal'], overall_well_being['concerning']))
    rules.append(ctrl.Rule(heart_rate['high'] & respiratory_rate['normal'], overall_well_being['concerning']))
    rules.append(ctrl.Rule(heart_rate['normal'] & respiratory_rate['normal'] & steps_count['high'], overall_well_being['normal']))
    rules.append(ctrl.Rule(heart_rate['low'] & respiratory_rate['normal'] & steps_count['normal'], overall_well_being['normal']))
    rules.append(ctrl.Rule(heart_rate['normal'] & respiratory_rate['low'] & steps_count['high'], overall_well_being['normal']))

    health_monitoring_ctrl = ctrl.ControlSystem(rules)
    health_monitoring = ctrl.ControlSystemSimulation(health_monitoring_ctrl)

    health_monitoring.input['heart_rate'] = heart_rate_val
    health_monitoring.input['respiratory_rate'] = respiratory_rate_val
    health_monitoring.input['steps_count'] = steps_count_val

    health_monitoring.compute()

    recommendation_value = health_monitoring.output['overall_well_being']

    wellbeing_labels = {
        0: 'Critical',
        50: 'Concerning',
        100: 'Normal'
    }

    result = health_monitoring.output['overall_well_being']
    nearest_label = min(wellbeing_labels.items(), key=lambda x: abs(x[0] - result))[1]

    print(result)
    return nearest_label
    #return health_monitoring.output['overall_well_being']

heart_rate_input = 80
respiratory_rate_input = 20
steps_count_input = 8000

result = health_monitoring_system(heart_rate_input, respiratory_rate_input, steps_count_input)
print("Overall Well-being:", result)
