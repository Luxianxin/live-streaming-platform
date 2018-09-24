from django.test import TestCase
import unittest
import requests
import json


class refreshRoomListTest(TestCase):

    def setUp(self):
        self.base_url = "http://127.0.0.1:8000/room/refresh"

    def test_get_list_successful(self):
        data = {}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertIsNotNone(result['ret'])


if __name__ == '__main__':
    unittest.main()
