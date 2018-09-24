from django.http import HttpResponse
import json

'''
delSession接口实现方法
method: POST
格式：JSON
返回：{ret:0(失败)；1(成功)}
'''


def delSession(request):
    if request.method == 'POST':
        print(request.body)

        # check if username and password are correct
        try:
            sessionKey = request.session.session_key
            print(sessionKey)
            print(request.session.get('userName'))
            if (sessionKey == None):
                ret = 0
            else:
                request.session.delete(sessionKey)
                ret = 1

        except:
            ret = 0

        print(ret)
        data = {"ret": ret}
        return HttpResponse(json.dumps(data), content_type="application/json")
