from django.test import TestCase
import unittest
import requests
import json


class leaveRoomTest(TestCase):

    def setUp(self):
        self.base_url = "http://127.0.0.1:8000/streaming/user/leave"

    def test_json_decode_error(self):
        data = {'anchorUsername': 'thy', 'userUsername': 'lxx'}
        headers = {'Content-Type': 'application/json'}
        requests.post(self.base_url, headers=headers, data=data)
        self.assertRaises(json.decoder.JSONDecodeError)

    def test_leave_room_successful(self):
        data = {'anchorUsername': 'thy', 'userUsername': 'lxx'}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertEqual(result['ret'], 1)

    def test_anchor_error(self):
        data = {'anchorUsername': 'aaa', 'userUsername': 'lxx'}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertEqual(result['ret'], 0)

    def test_user_error(self):
        data = {'anchorUsername': 'thy', 'userUsername': 'lxxxx'}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertEqual(result['ret'], 0)


if __name__ == '__main__':
    unittest.main()
