from django.views.decorators.http import require_POST
from files_cz import functions_cz, exceptions_cz
from protobuf import message_pb2 as message
from django.http import HttpResponse
from django.db import transaction
from django.conf import settings
from userSystem import models
import json, socket, re
import datetime


@require_POST
def change_streaming_status(request, anchor_name, current_status):
    with transaction.atomic():
        result = 0
        request_path = request.path
        try:
            if re.match(r'^\w{1,15}$', anchor_name) is None:
                raise exceptions_cz.ReFailed
            # check if the user exists or not
            user_queryset = models.User.objects.filter(userName=anchor_name, isAnchor=True)
            if user_queryset.exists() is False:
                raise exceptions_cz.ModelNotExist
            # find room of the user
            user_object = models.User.objects.get(userName=anchor_name, isAnchor=True)
            streaming_room = models.Room.objects.get(roomId=user_object)

            if current_status == streaming_room.isStreaming:
                raise exceptions_cz.DuplicatedStatus

            #==================================
            if current_status == 0:
                # calculate time difference
                startTime = streaming_room.lastStreaming
                endTime = datetime.datetime.now()
                timeDiff = (endTime - startTime).seconds
                streaming_room.streamingTime += timeDiff
                streaming_room.streamingNum += 1
            #==================================


            # change the status of the room
            streaming_room.isStreaming = current_status
            streaming_room.save()

            msg = message.CdlpMessage()
            msg.type = 4
            msg.anchorName = anchor_name
            msg.level = current_status

            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.connect((settings.SOCKET_HOST_IP, settings.SOCKET_HOST_PORT))
            sock.send(msg.SerializeToString())
            sock.send(b'\n')
            sock.close()

            result = 1
            functions_cz.save_success_log(request_path, user_object)
        # field empty case
        except TypeError:
            functions_cz.save_fail_log(request_path)
        except exceptions_cz.ReFailed:
            functions_cz.save_fail_log(request_path)
        except exceptions_cz.ModelNotExist:
            functions_cz.save_fail_log(request_path)
        except exceptions_cz.DuplicatedStatus:
            functions_cz.save_fail_log(request_path)
        return result


@require_POST
def refresh(request):
    # get information through analyzing json
    print(request.body)
    try:
        action = json.loads(request.body.decode()).get('action')
        client_id = json.loads(request.body.decode()).get('client_id')
        ip = json.loads(request.body.decode()).get('ip')
        vhost = json.loads(request.body.decode()).get('vhost')
        app = json.loads(request.body.decode()).get('app')

        if "on_connect" == action:
            tcUrl = json.loads(request.body.decode()).get('tcUrl')
            pageUrl = json.loads(request.body.decode()).get('pageUrl')
            print(action, client_id, ip, vhost, app, tcUrl, pageUrl)

        elif "on_close" == action:
            send_bytes = json.loads(request.body.decode()).get('send_bytes')
            recv_bytes = json.loads(request.body.decode()).get('recv_bytes')
            print(action, client_id, ip, vhost, app, send_bytes, recv_bytes)

        elif "on_publish" == action:
            tcUrl = json.loads(request.body.decode()).get('tcUrl')
            stream = json.loads(request.body.decode()).get('stream')

            if change_streaming_status(request, stream, 1) is True:
                print(action, client_id, ip, vhost, app, tcUrl, stream)
            else:
                print("failed")

        elif "on_unpublish" == action:
            stream = json.loads(request.body.decode()).get('stream')

            if change_streaming_status(request, stream, 0) is True:
                print(action, client_id, ip, vhost, app, stream)
            else:
                print("failed")

        elif "on_play" == action:
            stream = json.loads(request.body.decode()).get('stream')
            pageUrl = json.loads(request.body.decode()).get('pageUrl')
            print(action, client_id, ip, vhost, app, stream, pageUrl)

        elif "on_stop" == action:
            stream = json.loads(request.body.decode()).get('stream')
            print(action, client_id, ip, vhost, app, stream)

        elif "on_dvr" == action:
            stream = json.loads(request.body.decode()).get('stream')
            cwd = json.loads(request.body.decode()).get('cwd')
            file = json.loads(request.body.decode()).get('file')
            print(action, client_id, ip, vhost, app, stream, cwd, file)
            # save file name to database
            '''
            try:
                anchor = models.User.objects.get(userName=stream, isAnchor=1)
                room = models.Room.objects.get(roomId=anchor)
                room.replayURL = file
                room.save()
            except:
                print('save file fail')
            '''
    except json.JSONDecodeError:
        functions_cz.save_fail_log(request.path)
    except:
        print("failed")
    return HttpResponse(0)
