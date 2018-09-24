from django.test import TestCase
import unittest
import requests
import json


class enterRoomTest(TestCase):

    def setUp(self):
        self.base_url = "http://127.0.0.1:8000/streaming/user/enter"

    def test_json_decode_error(self):
        data = {'anchorUsername': 'aaa', 'userUsername': 'ddd'}
        headers = {'Content-Type': 'application/json'}
        requests.post(self.base_url, headers=headers, data=data)
        self.assertRaises(json.decoder.JSONDecodeError)

    def test_enter_room_successful(self):
        data = {'anchorUsername': 'aaa', 'userUsername': 'ddd'}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertEqual(result['ret'], 1)

    def test_anchor_error(self):
        data = {'anchorUsername': 'zzz', 'userUsername': 'ddd'}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertEqual(result['ret'], 0)

    def test_user_error(self):
        data = {'anchorUsername': 'aaa', 'userUsername': 'zzz'}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertEqual(result['ret'], 0)

    def test_data_error_no_anchor(self):
        data = {'userUsername': 'ddd'}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertEqual(result['ret'], 0)

    def test_data_error_no_user(self):
        data = {'anchorUsername': 'aaa'}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertEqual(result['ret'], 0)


if __name__ == '__main__':
    unittest.main()
