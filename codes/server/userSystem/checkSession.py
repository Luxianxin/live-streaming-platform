from django.http import HttpResponse
from userSystem import models
import json

'''
checkSession接口实现方法
method: POST
格式：JSON
request参数：{is_anchor(是否为主播, 0:普通用户；1：主播)}
返回：{username(用户名)}
'''


def checkSession(request):
    if request.method == 'POST':
        print(request.body)

        # check if username and password are correct
        try:
            # check if the same session
            if (request.session.get('isLogin', None) != None):
                userName = request.session["userName"]
                passWord = request.session["passWord"]
                isAnchor = json.loads(request.body.decode()).get('is_anchor')
                # isAnchor = request.session["is_anchor"]

                # verify user info
                try:
                    # object = models.User.objects.get(userName=userName, passWord=passWord, isAnchor=isAnchor)
                    # print(object.userName, object.passWord, object.isAnchor)
                    if (request.session["isLogin"] == 0):
                        request.session["isLogin"] = 1
                    request.session.save()
                    data = {'userName': userName}
                except:
                    data = {'userName': 0}
            else:
                data = {'userName': 0}
        except:
            data = {'userName': 0}

        return HttpResponse(json.dumps(data), content_type="application/json")
