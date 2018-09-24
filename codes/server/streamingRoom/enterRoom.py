from django.http import HttpResponse
from userSystem import models
import json
import random

'''
enterRoom接口实现方法
method: POST
格式：JSON
request参数：{anchorUsername(主播用户名)， userUsername(用户用户名)}
返回：{}
'''


def enterRooom(request):
    if request.method == 'POST':
        # get information through analyzing json
        anchorUsername = json.loads(request.body.decode()).get('anchorUsername')
        userUsername = json.loads(request.body.decode()).get('userUsername')
        print(anchorUsername, userUsername)

        try:
            # find anchor and user object in User model
            anchorId = models.User.objects.get(userName=anchorUsername, isAnchor=1).userId
            anchor = models.Room.objects.get(roomId=anchorId)
            watcher = models.User.objects.get(userName=userUsername, isAnchor=0)
            # insert item in RoomUser
            newEnterRoomItem = models.RoomUser(roomId=anchor, userId=watcher)
            newEnterRoomItem.save()

            #=================================================================================
            # save browserecord
            browseRecord = models.UserStatistics.objects.filter(userId=watcher, roomId=anchor)
            if (browseRecord.count()==0):
                newBrowseRecord = models.UserStatistics(userId=watcher, roomId=anchor, num=1)
                newBrowseRecord.save()
            else:
                browseRecord = models.UserStatistics.objects.get(userId=watcher, roomId=anchor)
                browseRecord.num+=1
                browseRecord.save()
            #=================================================================================

            ret = 1
            # add a new logging
            #newLogging = models.Logging(api=request.path, userId=anchorId, success=1, message="enter successfully")
            #newLogging.save()
        except:
            print("insert item error")
            ret = 0
            # add a new logging
            #newLogging = models.Logging(api=request.path, userId=anchorId, success=0, message="enter fail")
            #newLogging.save()

        data = {'ret': ret}
        return HttpResponse(json.dumps(data), content_type="application/json")
