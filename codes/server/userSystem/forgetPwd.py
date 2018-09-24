from django.http import HttpResponse
from userSystem import models
import json
from files_cz import functions_cz

'''
foregtPwd接口实现方法
method: POST
格式：JSON
request参数：{username(用户名)， email(邮箱)}
返回：{ret : 0(用户不存在)； 1(登陆成功)； 2(信息不匹配)}
'''


def forgetPwd(request):
    if request.method == 'POST':
        # get information through analyzing json
        username = json.loads(request.body.decode()).get('username')
        email = json.loads(request.body.decode()).get('email')
        isAnchor = json.loads(request.body.decode()).get('is_anchor')
        print(username, email)

        # check if username and email are correct

    try:
        object = models.User.objects.get(userName=username, eMail=email)
        ret = 1
        print(object.userName, object.eMail)
        print("verify successfully")
        # add a new logging
        functions_cz.save_success_log(request.path, object)

    except models.User.DoesNotExist:
        print('doesnotexist')

        try:
            models.User.objects.get(userName=username, isAnchor=isAnchor)
            ret = 2
            print("info not correct")
            # add a new logging
            functions_cz.save_fail_log(request.path)
        except models.User.DoesNotExist:
            print('doesnotexist')
        except:
            ret = 0
            print("user not exist")
            # add a new logging
            functions_cz.save_fail_log(request.path)

    data = {'ret': ret}
    return HttpResponse(json.dumps(data), content_type="application/json")
