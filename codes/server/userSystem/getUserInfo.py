from django.http import HttpResponse
from userSystem import models
import json
from files_cz import functions_cz

'''
getUserInfo接口实现方法
method: POST
格式：JSON
request参数：{username(用户名)， is_anchor(0:普通用户；1:主播)}
返回：{email(邮箱)；headPortrait(头像图片url)}
'''


def getUserInfo(request):
    if request.method == 'POST':
        # get information through analyzing json
        username = json.loads(request.body.decode("utf-8")).get('username')
        isAnchor = json.loads(request.body.decode("utf-8")).get('is_anchor')
        print(username, isAnchor)

        # check if the password reset successfully
        obj = models.User.objects.filter(userName=username, isAnchor=isAnchor)
        if obj.exists() is True:
            obj2 = models.User.objects.get(userName=username, isAnchor=isAnchor)
            email = obj2.eMail
            headPortrait = obj2.headPortrait
            print(email, headPortrait.name)
            print("get info successfully")
            # add a new logging
            functions_cz.save_success_log(request.path, obj2)
            data = {'email': email, 'headPortrait': headPortrait.name}
        else:
            print("get info failed")
            # add a new logging
            functions_cz.save_fail_log(request.path)
            data = {'email': '', 'headPortrait': ''}
        return HttpResponse(json.dumps(data), content_type="application/json")
