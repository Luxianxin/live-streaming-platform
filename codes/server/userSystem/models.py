from django.db import models


class User(models.Model):
    userId = models.BigAutoField(max_length=11, primary_key=True, blank=False)
    userName = models.CharField(max_length=15, blank=False)
    passWord = models.CharField(max_length=256, blank=False)
    eMail = models.CharField(max_length=50, unique=True)
    isAnchor = models.BooleanField(blank=False)
    headPortrait = models.FileField(upload_to='upload')
    birthday = models.CharField(max_length=10, blank=True)
    level = models.PositiveSmallIntegerField(null=False, default=0)
    qq = models.CharField(max_length=11, blank=True)
    registerTime = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return \
            ("user id:%d,username:%s,password:%s,email:%s,"
             "is anchor:%d,birthday:%s,level:%d,qq:%s,register time::%s" % (
                 self.userId, self.userName, self.passWord, self.eMail,
                 self.isAnchor, self.birthday, self.level, self.qq, self.registerTime))

    def __repr__(self):
        return \
            ("user id:%d,username:%r,password:%r,email:%r,"
             "is anchor:%d,birthday:%r,level:%d,qq:%r,register time::%r" % (
                 self.userId, self.userName, self.passWord, self.eMail,
                 self.isAnchor, self.birthday, self.level, self.qq, self.registerTime))

    class Meta:
        db_table = 'user'


class Room(models.Model):
    roomId = models.OneToOneField(User, on_delete=models.CASCADE, primary_key=True, related_name='Room_User')
    roomName = models.CharField(max_length=30, blank=False)
    type = models.PositiveSmallIntegerField(blank=False, default=0)
    isStreaming = models.BooleanField(blank=False, default=0)
    lastStreaming = models.DateTimeField(auto_now=True)
    streamingTime = models.PositiveIntegerField(default=0)
    streamingNum = models.PositiveIntegerField(default=0)

    def __str__(self):
        return "room id:%s,room name:%s,type:%d,is streaming:%d,last streaming:%s" % (
            str(self.roomId), self.roomName, self.type, self.isStreaming, self.lastStreaming)

    def __repr__(self):
        return "room id:%r,room name:%r,type:%d,is streaming:%d,last streaming:%r" % (
            str(self.roomId), self.roomName, self.type, self.isStreaming, self.lastStreaming)

    class Meta:
        db_table = 'room'


class RoomUser(models.Model):
    userId = models.OneToOneField(User, on_delete=models.CASCADE, primary_key=True, related_name='RoomUser_User')
    roomId = models.ForeignKey(Room, on_delete=models.CASCADE, related_name='RoomUser_Room')

    def __str__(self):
        return "user id:%s,room id:%s" % (self.userId.__str__(), self.roomId.__str__())

    def __repr__(self):
        return "user id:%r,room id:%r" % (self.userId.__str__(), self.roomId.__str__())

    class Meta:
        db_table = 'room_user'


class Account(models.Model):
    accountId = models.BigAutoField(max_length=11, primary_key=True, blank=False)
    userId = models.ForeignKey(User, on_delete=models.CASCADE, related_name='Account_User')
    accountNumber = models.CharField(max_length=30, blank=False)
    accountType = models.PositiveSmallIntegerField(blank=False)

    def __str__(self):
        return "account id:%d,user id:%s,account number:%s,account type:%d" % (
            self.accountId, self.userId.__str__(), self.accountNumber, self.accountType)

    def __repr__(self):
        return "account id:%d,user id:%r,account number:%r,account type:%d" % (
            self.accountId, self.userId.__str__(), self.accountNumber, self.accountType)

    class Meta:
        db_table = 'account'


class BrowseHistory(models.Model):
    browseHistoryId = models.BigAutoField(max_length=11, primary_key=True, blank=False)
    userId = models.ForeignKey(User, on_delete=models.CASCADE, related_name='BrowseHistory_User')
    roomId = models.ForeignKey(Room, on_delete=models.CASCADE, related_name='BrowseHistory_Room')
    browseTime = models.DateTimeField(auto_now=True)

    def __str__(self):
        return "browse history id:%d,user id:%s,room id:%s,browse time:%s" % (
            self.browseHistoryId, self.userId.__str__(), self.roomId.__str__(), self.browseTime)

    def __repr__(self):
        return "browse history id:%d,user id:%r,room id:%r,browse time:%r" % (
            self.browseHistoryId, self.userId.__str__(), self.roomId.__str__(), self.browseTime)

    class Meta:
        db_table = 'browse_history'


class FavoriteList(models.Model):
    favoriteListId = models.BigAutoField(max_length=11, primary_key=True, blank=False)
    userId = models.ForeignKey(User, on_delete=models.CASCADE, related_name='FavoriteList_User')
    roomId = models.ForeignKey(Room, on_delete=models.CASCADE, related_name='FavoriteList_Room')
    subscribeTime = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return "favorite list id:%d,user id:%s,room id:%s,subscribe time:%s" % (
            self.favoriteListId, self.userId.__str__(), self.roomId.__str__(), self.subscribeTime)

    def __repr__(self):
        return "favorite list id:%d,user id:%r,room id:%r,subscribe time:%r" % (
            self.favoriteListId, self.userId.__str__(), self.roomId.__str__(), self.subscribeTime)

    class Meta:
        db_table = 'favorite_list'


class RecommendList(models.Model):
    recommendListId = models.BigAutoField(max_length=11, primary_key=True, blank=False)
    userId = models.ForeignKey(User, on_delete=models.CASCADE, related_name='RecommendList_User')
    roomId = models.ForeignKey(Room, on_delete=models.CASCADE, related_name='RecommendList_Room')

    def __str__(self):
        return "recommend list id:%d,user id:%s,room id:%s" % (
            self.recommendListId, self.userId.__str__(), self.roomId.__str__())

    def __repr__(self):
        return "recommend list id:%d,user id:%r,room id:%r" % (
            self.recommendListId, self.userId.__str__(), self.roomId.__str__())

    class Meta:
        db_table = 'recommend_list'


class Logging(models.Model):
    id = models.BigAutoField(max_length=256, primary_key=True, blank=False)
    time = models.DateTimeField(auto_now_add=True)
    api = models.CharField(max_length=256, blank=False)
    success = models.BooleanField(blank=False)
    message = models.CharField(max_length=256, blank=False)
    userId = models.ForeignKey(User, on_delete=models.CASCADE, null=True, related_name='Logging_User')


class UserStatistics(models.Model):
    id = models.BigAutoField(max_length=11, primary_key=True, blank=False)
    userId = models.ForeignKey(User, on_delete=models.CASCADE)
    roomId = models.ForeignKey(Room, on_delete=models.CASCADE)
    lastBrowsing = models.DateTimeField(auto_now=True)
    browsingTime = models.PositiveIntegerField(default=0)
    num = models.PositiveIntegerField(default=1)
