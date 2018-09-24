from django.test import TestCase
import unittest
import requests

class uploadTest(TestCase):

    def setUp(self):
        self.base_url = "http://127.0.0.1:8000/userCenter/uploadPortrait"

    def test_upload_successful(self):
        data = {
            'username': 'lxx',
            'is_anchor': 0
        }
        files = {
            "image": open(r'C:/upload/image.png', "rb")
        }
        #headers = {'Content-Type': 'multipart/form-data'}

        response = requests.post(self.base_url, data=data, files=files)
        result = response.json()
        self.assertEqual(result['ret'], '/upload/image.png')

if __name__ == '__main__':
    unittest.main()
