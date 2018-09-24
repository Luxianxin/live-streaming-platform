from django.views.decorators.http import require_POST
from django.http import HttpResponse
from livestreaming import settings
from files_cz import exceptions_cz
from files_cz import functions_cz
from django.db import transaction
from protobuf import message_pb2
from userSystem import models
import json, re, socket


@require_POST
def register(request):
    with transaction.atomic():
        result = {'ret': '0'}
        request_path = request.path
        try:
            user_info = json.loads(request.body.decode())
            username = user_info.get('username')
            password = user_info.get('password')
            email = user_info.get('email')
            is_anchor = user_info.get('is_anchor')

            if re.match(r'^\w{1,15}$', username) is None \
                    or re.match(r'^\w{1,129}$', password) is None \
                    or re.match(r'^\w{1,20}@\w{1,20}\.com$', email) is None \
                    or is_anchor not in [0, 1]:
                raise exceptions_cz.ReFailed
            # find whether same username and email exists or not
            temp_user_1 = models.User.objects.filter(userName=username)
            temp_user_2 = models.User.objects.filter(eMail=email)
            # if they both not exist, the user can be registered
            if temp_user_1.exists() is False and temp_user_2.exists() is False:
                new_user = models.User(userName=username, passWord=password, isAnchor=is_anchor, eMail=email)
                new_user.save()
                if is_anchor is 1:
                    new_streaming_room = models.Room(roomId=new_user, roomName="%s's room" % username)
                    new_streaming_room.save()

                    msg = message_pb2.CdlpMessage()
                    msg.type = 3
                    msg.anchorName = username

                    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                    sock.connect((settings.SOCKET_HOST_IP, settings.SOCKET_HOST_PORT))
                    sock.send(msg.SerializeToString())
                    sock.send(b'\n')
                    sock.close()
                user_id = models.User.objects.get(userName=username, isAnchor=is_anchor)
                functions_cz.save_success_log(request.path, user_id)
                result['ret'] = '1'
        # catch errors that brought by inappropriate json format
        except json.decoder.JSONDecodeError:
            functions_cz.save_fail_log(request_path)
        # catch field missing error
        except TypeError:
            functions_cz.save_fail_log(request_path)
        except exceptions_cz.ReFailed:
            functions_cz.save_fail_log(request_path)

        return HttpResponse(json.dumps(result), content_type='application/json')
