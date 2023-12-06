import unittest

import sys
import os

current_script_directory = os.path.dirname(os.path.realpath(__file__))
sys.path.append(os.path.join(current_script_directory, "..", ".."))

from apis.health_fuzzy_impl import health_monitoring_system

abnomral_trend_str = "Slightly abnormal trends noticed. It's possible you might be working out. Consider visiting a doctor otherwise"

class TestHealthMonitoringFuzzy(unittest.TestCase):


    def test_normal_case(self):
        result = health_monitoring_system(80, 15, 8000)
        self.assertEqual(result, "Normal")

    def test_critical_case(self):
        result = health_monitoring_system(300, 20, 8000)
        self.assertEqual(result, "Critical")

    def test_low_steps_case(self):
        result = health_monitoring_system(80, 20, 1000)
        self.assertEqual(result, "Very less steps walked")

    def test_extreme_case_deceased(self):
        result = health_monitoring_system(0, 20, 8000)
        self.assertEqual(result, "Deceased")

        result = health_monitoring_system(80, 0, 8000)
        self.assertEqual(result, "Deceased")

    def test_extreme_case_critical(self):
        result = health_monitoring_system(260, 20, 8000)
        self.assertEqual(result, "Critical")

    def test_extreme_case_walking_too_much(self):
        result = health_monitoring_system(80, 20, 16000)
        self.assertEqual(result, "Walking too much")

    def test_abnormal_trends(self):
        result = health_monitoring_system(80, 20, 8000)
        self.assertEqual(result, "Slightly abnormal trends noticed. It's possible you might be working out or sleeping. Consider visiting a doctor otherwise")

if __name__ == '__main__':
    unittest.main()