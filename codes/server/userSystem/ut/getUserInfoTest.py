from django.test import TestCase
import unittest
import requests
import json

class forgetTest(TestCase):

    def setUp(self):
        self.base_url = "http://127.0.0.1:8000/userCenter/getUserInfo"

    def test_json_decode_error(self):
        data = {'username': 'aaa', 'is_anchor': 1}
        headers = {'Content-Type': 'application/json'}
        requests.post(self.base_url, headers=headers, data=data)
        self.assertRaises(json.decoder.JSONDecodeError)

    def test_verify_successful(self):
        data = {'username': 'aaa', 'is_anchor': 1}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertIsNotNone(result['email'])
        self.assertIsNotNone(result['headPortrait'])

    def test_username_not_exist(self):
        data = {'username': 'zzz', 'is_anchor': 1}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertIsNone(result['email'])
        self.assertIsNone(result['headPortrait'])

    def test_user_exist_anchor_not_exist(self):
        data = {'username': 'ddd', 'is_anchor': 1}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertIsNone(result['email'])
        self.assertIsNone(result['headPortrait'])

    def test_anchor_exist_user_not_exist(self):
        data = {'username': 'aaa', 'is_anchor': 0}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertIsNone(result['email'])
        self.assertIsNone(result['headPortrait'])

    def test_data_error_no_user(self):
        data = {'is_anchor': 1}
        headers = {'Content-Type': 'application/json'}
        response=requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertIsNone(result['email'])
        self.assertIsNone(result['headPortrait'])

    def test_data_error_no_isAnchor(self):
        data = {'username': 'aaa'}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertIsNone(result['email'])
        self.assertIsNone(result['headPortrait'])

if __name__ == '__main__':
    unittest.main()