from django.http import HttpResponse
from userSystem import models
import json

'''
resetPwd接口实现方法
method: POST
格式：JSON
request参数：{username(用户名)， password(密码)}
返回：{ret : 0(重置失败)； 1(重置成功)}
'''


def resetPwd(request):
    if request.method == 'POST':
        # get information through analyzing json
        username = json.loads(request.body.decode("utf-8")).get('username')
        password = json.loads(request.body.decode("utf-8")).get('password')
        print(username, password)

        # check if the password reset successfully
        try:
            obj = models.User.objects.get(userName=username)
            obj.passWord = password
            obj.save()

            ret = 1
            print("reset pwd sucessfully")

            # add a new logging
            newLogging = models.Logging(api=request.path, userId=obj, success=1, message="reset pwd sucessfully")
            newLogging.save()
        except:
            ret = 0
            print("reset pwd fail")
            # add a new logging
            newLogging = models.Logging(api=request.path, userId=obj, success=0, message="reset pwd fail")
            newLogging.save()

        data = {'ret': ret}
        return HttpResponse(json.dumps(data), content_type="application/json")
