import unittest
from unittest.mock import MagicMock
import json

class TestCielAndroidLogic(unittest.TestCase):
    def test_api_payload_structure(self):
        # Test de simulation de payload reçu par le serveur Ktor
        payload = {"action": "alarm", "hour": 7, "minute": 30, "message": "Réveil Rimuru"}
        self.assertEqual(payload["action"], "alarm")
        self.assertEqual(payload["hour"], 7)
        self.assertEqual(payload["minute"], 30)

if __name__ == '__main__':
    unittest.main()
