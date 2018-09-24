from django.http import HttpResponse
from userSystem import models
import json
from files_cz import functions_cz

'''
refreshRoomList接口实现方法
method: POST
格式：JSON
request参数：void
返回：{ret: String[](主播用户名数组)}
'''


def save_success_log(request_path):
    new_log = models.Logging(api=request_path, success=1, message="sucessed")
    new_log.save()


def refreshRoomList(request):
    if request.method == 'POST':
        print(request.body)
        # get information through analyzing json

        anchorList = []
        try:
            # get all username of all anchors whose is streaming
            object = models.Room.objects.filter(isStreaming=1).values('roomId_id').distinct()
            for i in range(object.count()):
                username = models.User.objects.get(userId=object[i]['roomId_id']).userName
                portrait = models.User.objects.get(userId=object[i]['roomId_id']).headPortrait.name
                print(username,portrait)
                newDict = {"username":username,"portrait":portrait}
                anchorList.append(newDict)

            # add a new lauth_userogging
            save_success_log(request.path)

            '''
            object = models.streaming_user.objects.filter(is_anchor=1).values('username').distinct()
            for i in range(object.count()):
                print(object[i]['username'])
                anchorList.append(object[i]['username'])
            '''
        except json.JSONDecodeError:
            print('json decode error')
            anchorList = None
            functions_cz.save_fail_log(request.path)
        except models.User.DoesNotExist:
            print("does not exist")
            anchorList = None
            # add a new logging
            functions_cz.save_fail_log(request.path)

        data = {"ret": anchorList}
        return HttpResponse(json.dumps(data), content_type="application/json")
