# from django.shortcuts import render
from userSystem import models
from django.shortcuts import HttpResponse
import json
import datetime


def leaveRoom(request):
    if request.method == 'POST':
        anchorName = json.loads(request.body.decode()).get('anchorUsername')
        userName = json.loads(request.body.decode()).get('userUsername')
        print(anchorName, userName)
        userId = models.User.objects.get(userName=userName, isAnchor=0)
        anchorId = models.User.objects.get(userName=anchorName, isAnchor=1)
        roomId = models.Room.objects.get(roomId=anchorId)
        print(userId, roomId)

        try:
            obj = models.RoomUser.objects.get(userId=userId, roomId=roomId)
            ret = 1
            obj.delete()
            print(userName + " " + "leaved this room!")

            #==================================
            browseRecord = models.UserStatistics.objects.get(userId=userId, roomId=roomId)
            startTime = browseRecord.lastBrowsing
            endTime = datetime.datetime.now()
            timeDiff = (endTime - startTime).seconds
            browseRecord.browsingTime += timeDiff
            browseRecord.save()
            #==================================

        except:
            ret = 0
            print("no such user! check your input!")

        print(ret)
        data = {'ret': ret}
        return HttpResponse(json.dumps(data), content_type="application/json")
