from rank import Rank
from userSystem import models
import json
from django.http import HttpResponse


def totalTime(request):
    if request.method == 'POST':
        # get information through analyzing json
        username = json.loads(request.body.decode()).get('username')
        try:
            anchorId = models.User.objects.get(userName=username, isAnchor=1).userId
            anchor = models.Room.objects.get(roomId=anchorId)
            # get related statistics
            time = anchor.streamingTime
        except:
            time = 0
        ret = {'time': time}
        return HttpResponse(json.dumps(ret), content_type="application/json")


def avgTime(request):
    if request.method == 'POST':
        # get information through analyzing json
        username = json.loads(request.body.decode()).get('username')
        try:
            anchorId = models.User.objects.get(userName=username, isAnchor=1).userId
            anchor = models.Room.objects.get(roomId=anchorId)
            # get related statistics
            time = anchor.streamingTime
            num = anchor.streamingNum
            avg = time / num
            print(time, num, avg)

        except:
            avg = 0
        ret = {'time': avg}
        return HttpResponse(json.dumps(ret), content_type="application/json")


def topThreeTime(request):
    if request.method == 'POST':
        username = json.loads(request.body.decode()).get('username')

        try:
            userDict = {}
            anchorId = models.User.objects.get(userName=username, isAnchor=1).userId
            roomId = models.Room.objects.get(roomId=anchorId)
            # rank data
            user = models.UserStatistics.objects.filter(roomId_id=roomId).annotate(rank=Rank('browsingTime'))
            # get related statistics
            for i in range(user.count()):
                name = models.User.objects.get(userId=user[i].userId_id).userName
                rank = user[i].rank
                time = user[i].browsingTime
                if (rank > 3):
                    break
                newDict = {name: {'rank': rank, 'time': time}}
                userDict.update(newDict)
            print(userDict)
        except:
            userDict = 0

    ret = userDict
    print(ret)
    return HttpResponse(json.dumps(ret), content_type="application/json")


def topThreeNum(request):
    if request.method == 'POST':
        username = json.loads(request.body.decode()).get('username')

        try:
            userDict = {}
            anchorId = models.User.objects.get(userName=username, isAnchor=1).userId
            roomId = models.Room.objects.get(roomId=anchorId)
            # rank data
            user = models.UserStatistics.objects.filter(roomId_id=roomId).annotate(rank=Rank('num'))
            # get related statistics
            for i in range(user.count()):
                name = models.User.objects.get(userId=user[i].userId_id).userName
                rank = user[i].rank
                num = user[i].num
                if (rank > 3):
                    break
                newDict = {name: {'rank': rank, 'num': num}}
                userDict.update(newDict)
            print(userDict)
        except:
            userDict = 0

    ret = userDict
    return HttpResponse(json.dumps(ret), content_type="application/json")
