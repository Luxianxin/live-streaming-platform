from django.test import TestCase
import unittest
import requests
import json


class endStreamingTest(TestCase):

    def setUp(self):
        self.base_url = "http://127.0.0.1:8000/streaming/anchor/end"

    def test_json_decode_error(self):
        data = {'username': 'thy'}
        headers = {'Content-Type': 'application/json'}
        requests.post(self.base_url, headers=headers, data=data)
        self.assertRaises(json.decoder.JSONDecodeError)

    def test_end_successful(self):
        data = {'username': 'thy'}
        headers = {'Content-Type': 'application/json'}
        response = requests.post(self.base_url, headers=headers, data=json.dumps(data))
        result = response.json()
        self.assertEqual(result['ret'], 1)


if __name__ == '__main__':
    unittest.main()
