from protobuf import message_pb2
import socket, time, asyncio


# def msg(message_per_loop):
#     new_msg = message_pb2.CdlpMessage()
#     new_msg.anchorName = 'czcz'
#     new_msg.userName = 'cz'
#     i = 0
#     while True:
#
#         new_msg.type = 1
#         yield new_msg.SerializeToString()
#
#         if message_per_loop > 1:
#             new_msg.type = 2
#             new_msg.chat = str(i)
#             yield new_msg.SerializeToString()
#             if message_per_loop > 2:
#                 new_msg.type = 0
#                 new_msg.chat = ''
#                 yield new_msg.SerializeToString()
#
#         i += 1


def msg():
    new_msg = message_pb2.CdlpMessage()
    new_msg.anchorName = 'czcz'
    new_msg.userName = 'cz'
    new_msg.type = 1
    new_msg.level = 1
    yield new_msg.SerializeToString()
    i = 0
    new_msg.type = 2
    while True:
        new_msg.chat = str(i)
        yield new_msg.SerializeToString()

        i += 1


def tps_main(sock_list, msg_generator):
    for sock in sock_list:
        for i in range(3):
            sock.send(next(msg_generator))
            sock.send(b'\n')


def tps(socket_number, message_number=-1):
    ''' actual number of message is message_number * 3,cause every client needs to enter,chat and leave'''
    sock_list = []
    for i in range(socket_number):
        new_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        new_sock.connect(('192.168.1.53', 8001))
        sock_list.append(new_sock)
        print('current number of socket: %d' % (i + 1))

    msg_generator = msg()
    try:
        if message_number is -1:
            while True:
                tps_main(sock_list, msg_generator)
        else:
            for i in range(message_number):
                tps_main(sock_list, msg_generator)
    except KeyboardInterrupt:
        pass

    for sock in sock_list:
        sock.close()


async def response_time_run(pair_dict, msg_generator, time_start, turns, requests_per_turn):
    for reader in pair_dict:
        for i in range(3):
            pair_dict[reader].write(next(msg_generator))
            pair_dict[reader].write(b'\n')
            await reader.read(1024)
    total_time = time.time() - time_start
    print('current response time:%f' % (total_time / requests_per_turn / turns))


async def response_time_main(socket_number, message_number, loop):
    pair_dict = {}
    for i in range(socket_number):
        reader, writer = await asyncio.open_connection('192.168.1.52', 8001, loop=loop)
        pair_dict[reader] = writer
        print('current number of socket: %d' % (i + 1))

    msg_generator = msg()
    time_start = time.time()
    requests_per_turn = len(pair_dict) / 3
    try:
        if message_number is -1:
            turns = 1
            while True:
                await response_time_run(pair_dict, msg_generator, time_start, turns, requests_per_turn)
                turns += 1
        else:
            for turns in range(message_number):
                await response_time_run(pair_dict, msg_generator, time_start, turns + 1, requests_per_turn)
    except KeyboardInterrupt:
        pass

    for writer in pair_dict.values():
        writer.close()


def response_time_init(socket_number, message_number=-1):
    ''' actual number of message is message_number * 3,cause every client needs to enter,chat and leave'''
    loop = asyncio.get_event_loop()
    loop.run_until_complete(response_time_main(socket_number, message_number, loop))
    loop.close()


if __name__ == '__main__':
    tps(1)
