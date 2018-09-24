from django.http import HttpResponse
from userSystem import models
import json
'''
changePwd接口实现方法
method: POST
格式：JSON
request参数：{username(用户名)，is_anchor(是否是主播), oldPwd(旧密码), newPwd(新密码)}
返回：{ret : 0(用户不存在)； 1(修改成功)；2(密码不正确)}
'''
def changePwd(request):
    if request.method == 'POST':
        # get information through analyzing json
        username = json.loads(request.body.decode("utf-8")).get('username')
        isAnchor = json.loads(request.body.decode("utf-8")).get('is_anchor')
        oldPwd = json.loads(request.body.decode("utf-8")).get('oldPwd')
        newPwd = json.loads(request.body.decode("utf-8")).get('newPwd')
        print(username, isAnchor, oldPwd, newPwd)

        # check if the password reset successfully
        try:
            object = models.User.objects.get(userName=username, isAnchor=isAnchor)
            if (object.passWord != oldPwd):
                ret = 2
                print("password incorrect")
                # add a new logging
                newLogging = models.Logging(api=request.path, userId=object, success=0, message="password incorrect")
                newLogging.save()
            else:
                object.passWord = newPwd
                object.save()
                ret = 1
                print(object.userName, object.passWord)
                print("change pwd sucessfully")
                # add a new logging
                newLogging = models.Logging(api=request.path, userId=object, success=1, message="change pwd sucessfully")
                newLogging.save()

        except:
                ret = 0
                print("change pwd failure")
                # add a new logging
                newLogging = models.Logging(api=request.path, success=0, message="user not exist")
                newLogging.save()

        data = {'ret': ret}
        return HttpResponse(json.dumps(data), content_type="application/json")