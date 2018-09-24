import requests
from userSystem import models
import json
from django.shortcuts import HttpResponse

'''
replay接口实现方法
method: POST
格式：JSON
request参数：{username(主播用户名)}
返回：{0:重播失败；1：重播成功；2：无直播历史} 
'''


def replay(request):
    if request.method == 'POST':
        anchorName = json.loads(request.body.decode()).get('username')
        print(anchorName)

        try:
            # get user info
            room = models.User.objects.get(userName=anchorName, isAnchor=1)
            file = models.Room.objects.get(roomId=room).replayURL
            print(room, file)
            if file == '':
                # no streaming history
                print('no streaming history')
                ret = 2
            else:
                # transfer file url to srs system
                url = "http://192.168.1.67:8000/replay"
                data = {'room': anchorName, 'file': file}
                headers = {'Content-Type': 'application/json'}
                response = requests.post(url, headers=headers, data=json.dumps(data))
                result = response.json()
                print(result)

                if (result['ret'] == 1):
                    ret = 1
                    print('replay successful')
                else:
                    ret = 0
                    print('replay fail')

        except:
            ret = 0
            print("replay fail")

        data = {'ret': ret}
        return HttpResponse(json.dumps(data), content_type="application/json")