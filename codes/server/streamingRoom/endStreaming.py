from django.views.decorators.http import require_POST
from django.http import HttpResponse
from files_cz import exceptions_cz
from files_cz import functions_cz
from django.db import transaction
from userSystem import models
import json, re


@require_POST
def end_streaming(request):
    with transaction.atomic():
        result = {'ret': '0'}
        request_path = request.path
        try:
            user_info = json.loads(request.body.decode())
            username = user_info.get('username')
            room_name = user_info.get('roomname')

            if re.match(r'^\w{1,15}$', username) is None or re.match(r'^\w{1,15}$', room_name) is None:
                raise exceptions_cz.ReFailed
            # check if the user exists or not
            user_queryset = models.User.objects.filter(userName=username, isAnchor=True)
            if user_queryset.exists() is False:
                raise exceptions_cz.ModelNotExist
            # find room of the user
            user_object = models.User.objects.get(userName=username, isAnchor=True)
            streaming_room = models.Room.objects.get(roomId=user_object)
            # change the status of the room
            streaming_room.isStreaming = 0
            streaming_room.roomName = room_name
            #==================================
            # calculate time difference
            startTime = streamingRoom.lastStreaming
            endTime = datetime.datetime.now()
            timeDiff = (endTime - startTime).seconds
            streamingRoom.streamingTime += timeDiff
            #==================================
            streaming_room.save()

            result['ret'] = '1'
            functions_cz.save_success_log(request_path, user_object)
        except json.JSONDecodeError:
            functions_cz.save_fail_log(request_path)
        # field empty case
        except TypeError:
            functions_cz.save_fail_log(request_path)
        except exceptions_cz.ReFailed:
            functions_cz.save_fail_log(request_path)
        except exceptions_cz.ModelNotExist:
            functions_cz.save_fail_log(request_path)

        return HttpResponse(json.dumps(result), content_type='application/json')
