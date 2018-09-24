from django.http import HttpResponse
from userSystem import models
from django.conf import settings
import json
import os
import datetime as dt
from files_cz import functions_cz

'''
uploadPortrait接口实现方法
method: POST

request参数：{username(用户名)， headportrait(头像)}
返回：{ret : 0(上传失败)； 1(上传成功)}
'''


def uploadPortrait(request):
    if request.method == 'POST':
        # get information through analyzing json
        # username = json.loads(request.body.decode()).get('username')
        # headportrait = json.loads(request.body.decode()).get('image')
        username = request.POST.get('username')
        isAnchor = int(request.POST.get('is_anchor'))
        f = request.FILES.get("image")
        print(username)
        print(isAnchor)

        # 文件在服务器端的路径
        if f:
            uploadDir = os.path.abspath(settings.MEDIA_ROOT)
            # today = dt.datetime.today()
            # dirName = uploadDir + '/%d/%d/'% (today.year,today.month)
            if not os.path.exists(uploadDir):
                os.makedirs(uploadDir)
            filepath = os.path.join(uploadDir, f.name)
            with open(filepath, 'wb+') as destination:
                for chunk in f.chunks():
                    destination.write(chunk)
                destination.close()

        # check if the head portrait upload successfully
        try:
            obj = models.User.objects.get(userName=username, isAnchor=isAnchor)
            obj.headPortrait = settings.MEDIA_URL + f.name
            obj.save()
            # ret = 1
            ret = settings.MEDIA_URL + f.name
            print("upload sucessfully")
            # add a new logging
            functions_cz.save_success_log(request.path, obj)
        except models.User.DoesNotExist:
            print("upload failure")
            ret = '0'
            # add a new logging
            functions_cz.save_fail_log(request.path)
        except:
            print("upload failure")
            ret = '0'
            # add a new logging
            functions_cz.save_fail_log(request.path)

        data = {'ret': ret}
        return HttpResponse(json.dumps(data), content_type="application/json")
