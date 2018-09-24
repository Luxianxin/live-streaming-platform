from django.test import TestCase


class RegisterTest(TestCase):
    def register_post(self, content, result):
        response = self.client.post('/login/register', content, content_type='application/json')
        response_json = response.json()
        self.assertEquals(response_json['ret'], result)

    def test_bad_json_format(self):
        # lack of ,
        self.register_post(r'{"username": "a" "password": "bbb", "email": "111@qq.com", "is_anchor": 1}', '0')
        # lack of "
        self.register_post(r'{"username": "a", "password": "bbb", "email": "111@qq.com", "is_anchor: 1}', '0')
        # lack of :
        self.register_post(r'{"username": "a","password": "bbb","email": "111@qq.com“,"is_anchor" 1}', '0')

    def test_field_missing(self):
        # username missing
        self.register_post(r'{"password": "bbb", "email": "111@qq.com", "is_anchor": 1}', '0')

    def test_field_empty(self):
        # username shouldn't be empty
        self.register_post(r'{"username": "", "password": "bbb", "email": "111@qq.com", "is_anchor": 1}', '0')

    def test_re_failed(self):
        # username re failed
        self.register_post(r'{"username": "-", "password": "bbb", "email": "111@qq.com", "is_anchor": 1}', '0')
        # email re failed, not .com
        self.register_post(r'{"username": "a", "password": "bbb", "email": "111@qq.fku", "is_anchor": 1}', '0')
        # email re failed, lack of .
        self.register_post(r'{"username": "a", "password": "bbb", "email": "111@qqcom", "is_anchor": 1}', '0')
        # email re failed, lack of @
        self.register_post(r'{"username": "a", "password": "bbb", "email": "111qq.com", "is_anchor": 1}', '0')
        # is_anchor re failed
        self.register_post(r'{"username": "a","password": "bbb","email": "111@qq.com","is_anchor": 2}', '0')

    def test_register_successfully(self):
        self.register_post(r'{"username": "aa","password": "bbb","email": "11@qq.com","is_anchor": 1}', '1')

    def test_duplicated_uniqueField(self):
        # register once
        self.register_post(r'{"username": "bb","password": "bbb","email": "22@qq.com","is_anchor": 1}', '1')
        # register twice, same username exists, register should fail
        self.register_post(r'{"username": "bb","password": "bbb","email": "33@qq.com","is_anchor": 1}', '0')

        # register once
        self.register_post(r'{"username": "cc","password": "bbb","email": "44@qq.com","is_anchor": 1}', '1')
        # register twice, same email exists, register should fail
        self.register_post(r'{"username": "dd","password": "bbb","email": "44@qq.com","is_anchor": 1}', '0')
