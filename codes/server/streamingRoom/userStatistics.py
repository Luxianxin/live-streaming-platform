from rank import Rank
from userSystem import models
import json
from django.http import HttpResponse
from django.db.models import Sum


def totalTime(request):
    if request.method == 'POST':
        # get information through analyzing json
        username = json.loads(request.body.decode()).get('username')
        print(username)
        try:
            userId = models.User.objects.get(userName=username, isAnchor=0).userId
            # get related statistics
            timeList = models.UserStatistics.objects.filter(userId=userId).aggregate(time=Sum('browsingTime'))
            time = timeList['time']
            print(timeList['time'])

        except:
            time = 0
        ret = {'time': time}
        return HttpResponse(json.dumps(ret), content_type="application/json")


def avgTime(request):
    if request.method == 'POST':
        # get information through analyzing json
        username = json.loads(request.body.decode()).get('username')
        try:
            userId = models.User.objects.get(userName=username, isAnchor=0).userId
            # get related statistics
            time = models.UserStatistics.objects.filter(userId=userId).aggregate(time=Sum('browsingTime'))
            num = models.UserStatistics.objects.filter(userId=userId).aggregate(num=Sum('num'))
            avg = time['time'] / num['num']
            print(time, num, avg)

        except:
            avg = 0
        ret = {'time': avg}
        return HttpResponse(json.dumps(ret), content_type="application/json")


def topThreeTime(request):
    if request.method == 'POST':
        username = json.loads(request.body.decode()).get('username')

        try:
            anchorDict = {}
            userId = models.User.objects.get(userName=username, isAnchor=0)
            # rank data
            room = models.UserStatistics.objects.filter(userId_id=userId).annotate(rank=Rank('browsingTime'))
            # get related statistics
            for i in range(room.count()):
                name = models.User.objects.get(userId=room[i].roomId_id).userName
                rank = room[i].rank
                time = room[i].browsingTime
                if (rank > 3):
                    break
                newDict = {name: {'rank': rank, 'time': time}}
                anchorDict.update(newDict)

        except:
            anchorDict = 0

    ret = anchorDict
    print(ret)

    return HttpResponse(json.dumps(ret), content_type="application/json")


def topThreeNum(request):
    if request.method == 'POST':
        username = json.loads(request.body.decode()).get('username')

        try:
            anchorDict = {}
            userId = models.User.objects.get(userName=username, isAnchor=0)
            # rank data
            room = models.UserStatistics.objects.filter(userId_id=userId).annotate(rank=Rank('num'))
            # get related statistics
            for i in range(room.count()):
                name = models.User.objects.get(userId=room[i].roomId_id).userName
                rank = room[i].rank
                num = room[i].num
                if (rank > 3):
                    break
                newDict = {name: {'rank': rank, 'num': num}}
                anchorDict.update(newDict)

        except:
            anchorDict = 0

    ret = anchorDict

    return HttpResponse(json.dumps(ret), content_type="application/json")
