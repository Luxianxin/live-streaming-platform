from django.test import TestCase


class StartStreamingTest(TestCase):
    def return_response(self, content, result):
        response = self.client.post('/streaming/anchor/start', content, content_type='application/json')
        response_json = response.json()
        self.assertEquals(response_json['ret'], result)

    def test_run_successfully(self):
        # register
        self.client.post('/login/register',
                         r'{"username": "e","password": "bbb","email": "11111@qq.com","is_anchor": 1}',
                         content_type='application/json')
        # start streaming
        self.return_response(r'{"username": "e", "roomname": "b"}', '1')

    def test_field_missing(self):
        self.return_response(r'{"roomname": "b"}', '0')

    def test_field_empty(self):
        self.return_response(r'{"username": "", "roomname": "b"}', '0')

    def test_re_failed(self):
        self.return_response(r'{"username": "-", "roomname": "b"}', '0')

    def test_bad_json_format(self):
        # lack of ,
        self.return_response(r'{"username": "e" "roomname": "b"}', '0')
        # lack of “
        self.return_response(r'{"username": "e", "roomname": b"}', '0')
        # lack of :
        self.return_response(r'{"username": "e", "roomname" "b"}', '0')

    def test_userNot_exist(self):
        self.return_response(r'{"username": "f", "roomname": "b"}', '0')
