from django.http import HttpResponse
from userSystem import models
import json

'''
login接口实现方法
method: POST
格式：JSON
request参数：{username(用户名)， password(密码)，is_anchor(是否为主播, 0:普通用户；1：主播)}
返回：{ret: 0(用户不存在)； 1(登陆成功)； 2(密码错误)}
'''


def login(request):
    if request.method == 'POST':
        print(request.body)
        # get information through analyzing json
        username = json.loads(request.body.decode()).get('username')
        password = json.loads(request.body.decode()).get('password')
        is_anchor = json.loads(request.body.decode()).get('is_anchor')
        # username='aaa'
        # password='123'
        # is_anchor=1
        print(username, password)

        # check if username and password are correct
        try:
            object = models.User.objects.get(userName=username, passWord=password, isAnchor=is_anchor)
            ret = 1
            print(object.userName, object.passWord)
            print("login successfully")

            # add a new logging
            newLogging = models.Logging(api=request.path, userId=object, success=1, message="login successfully")
            newLogging.save()

            # check if the same session else create new session
            if (request.session.get('isLogin', None) == None):
                request.session["userName"] = username
                request.session["isAnchor"] = is_anchor
                request.session["passWord"] = password
                request.session["isLogin"] = 1
                print(request.session.get('userName', None), request.session.get('isAnchor', None),
                      request.session.get('isLogin', None))
                request.session.save()
                print(request.session.session_key)
            else:
                print(request.session.get('userName', None), request.session.get('isAnchor', None),
                      request.session.get('isLogin', None))
                print(request.session.session_key)


        except:
            try:
                object = models.User.objects.get(userName=username, isAnchor=is_anchor)
                ret = 2
                print("password incorrect")

                # add a new logging
                newLogging = models.Logging(api=request.path, userId=object, success=0, message="password incorrect")
                newLogging.save()
            except:
                ret = 0
                print("user not exist")

                # add a new logging
                newLogging = models.Logging(api=request.path, success=0, message="user not exist")
                newLogging.save()

        data = {'ret': ret}
        return HttpResponse(json.dumps(data), content_type="application/json")
