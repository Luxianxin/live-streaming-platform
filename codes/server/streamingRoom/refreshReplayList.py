from django.http import HttpResponse
import json
import requests
from userSystem import models

def replayList(request):
    if request.method == 'POST':
        print(request.body)
        ret=[]

        try:
            url = "http://192.168.1.67:8000/refreshVideoList/videoList"
            data = {}
            headers = {'Content-Type':'application/json'}
            response = requests.post(url, headers=headers, data=json.dumps(data))
            result = response.json()

            list = result['ret']
            for i in range(len(list)):
                username = list[i]['username']
                url = list[i]['url']
                portrait = models.User.objects.get(userName=username,isAnchor=1).headPortrait.name
                print(portrait)
                newDict = {'username':username,'url':url,'portrait':portrait}
                ret.append(newDict)
        except:
            ret=None

    data={'ret':ret}
    print(data)
    return HttpResponse(json.dumps(data), content_type="application/json")