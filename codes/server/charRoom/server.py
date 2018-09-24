from livestreaming import settings
from protobuf import message_pb2
from enum import Enum
import asyncio, queue, json, requests, time

# room_list is a dict whose key is room name and value is a dict
# the latter dict's key is username and its value is user's charRoom
room_list = [{'test1': {}}, {'bbb': {}}, {'czcz': {}}]

total_requests = 0
time_start = time_now = time.time()


class MsgType(Enum):
    LEAVE = 0
    ENTER = 1
    CHAT = 2
    CREATE = 3
    STATUS = 4


def tps():
    global total_requests, time_start, time_now
    total_requests += 1
    time_now = time.time()

    print('tps:%d' % (total_requests / (time_now - time_start)))
    # if int(time_now - time_start) == 300:
    #     print('tps:%d(5 min average)' % (requests_per_five_minute / 300))


def leave(room_name, user_name, cdlp_message, msg_type, user_level):
    for room in room_list:
        if room_name in room:
            # ==================================

            # print('leave', room_name, user_name)
            # if room_name != user_name:
            #     url = 'http://192.168.1.71:8000/streaming/leave'
            #     data = {'anchorUsername': room_name, 'userUsername': user_name}
            #     headers = {'Content-Type': 'application/json'}
            #     response = requests.post(url, headers=headers, data=json.dumps(data))
            #     print(response.json())

            # ==================================
            room[room_name].pop(user_name)
            if msg_type != MsgType.LEAVE.value:
                ret_cdlp_msg = message_pb2.CdlpMessage()
                ret_cdlp_msg.type = MsgType.LEAVE.value
                ret_cdlp_msg.userName = user_name
                ret_cdlp_msg.anchorName = room_name
                ret_cdlp_msg.level = user_level
                for transport_object in list(room[room_name].values()):
                    transport_object.write(ret_cdlp_msg.SerializeToString())
                tps()
            else:
                for transport_object in list(room[room_name].values()):
                    transport_object.write(cdlp_message.SerializeToString())
                tps()


def enter(transport, room_name, user_name, cdlp_message):
    for room in room_list:
        if room_name in room:
            # =================================================================================

            # print('enter', room_name, user_name)
            # if room_name != user_name:
            #     url = 'http://192.168.1.71:8000/streaming/enter'
            #     data = {'anchorUsername': room_name, 'userUsername': user_name}
            #     headers = {'Content-Type': 'application/json'}
            #     response = requests.post(url, headers=headers, data=json.dumps(data))
            #     print(response.json())

            # =================================================================================
            room[room_name][user_name] = transport
            for transport_object in list(room[room_name].values()):
                transport_object.write(cdlp_message.SerializeToString())
            tps()


def create_room(room_name):
    room_list.append({room_name: {}})
    tps()


def chat_and_status(room_name, cdlp_message):
    for room in room_list:
        if room_name in room:
            for transport_object in list(room[room_name].values()):
                transport_object.write(cdlp_message.SerializeToString())
            tps()


class Server(asyncio.Protocol):
    def __init__(self):
        # super().__init__(self), should be deleted, why?
        self.transport = asyncio.StreamWriter.transport
        self.cdlp_message = message_pb2.CdlpMessage()
        self.data_queue = queue.Queue()
        self.left_data = b''
        self.room_name = ''
        self.user_name = ''
        self.msg_type = 0
        self.user_level = 0
        self.create = False

    def connection_made(self, transport):
        self.transport = transport

    def data_received(self, data):
        if data is not b'':
            if b'\n' in data:
                split_data_array = data.split(b'\n')

                if self.left_data is not b'':
                    self.data_queue.put(b''.join([self.left_data, split_data_array[0]]))
                    split_data_array.pop(0)
                    self.left_data = b''

                if split_data_array[-1] is not b'':
                    self.left_data = split_data_array.pop()
                else:
                    split_data_array.pop()

                for split_data in split_data_array:
                    self.data_queue.put(split_data)

                while self.data_queue.qsize() is not 0:
                    self.cdlp_message.ParseFromString(self.data_queue.get())
                    self.room_name = self.cdlp_message.anchorName
                    self.user_name = self.cdlp_message.userName
                    self.msg_type = self.cdlp_message.type
                    self.user_level = self.cdlp_message.level

                    # print(self.cdlp_message)

                    if self.msg_type == MsgType.ENTER.value:
                        enter(self.transport, self.room_name, self.user_name, self.cdlp_message)
                    elif self.msg_type == MsgType.CHAT.value or self.msg_type == MsgType.STATUS.value:
                        chat_and_status(self.room_name, self.cdlp_message)
                    elif self.msg_type == MsgType.CREATE.value:
                        create_room(self.room_name)
                        self.create = True
            else:
                self.left_data = b''.join([self.left_data, data])

    def connection_lost(self, exc):
        if isinstance(exc, Exception):
            if self.msg_type != MsgType.CREATE.value and self.msg_type != MsgType.STATUS.value:
                leave(self.room_name, self.user_name, self.cdlp_message, self.msg_type, self.user_level)
        elif exc is None:
            if self.msg_type != MsgType.CREATE.value and self.msg_type != MsgType.STATUS.value:
                leave(self.room_name, self.user_name, self.cdlp_message, self.msg_type, self.user_level)


def main():
    loop = asyncio.get_event_loop()
    factory = loop.create_server(Server, '192.168.1.51', 8001)
    server = loop.run_until_complete(factory)
    loop.run_forever()


if __name__ == '__main__':
    main()
