"""livestreaming URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/2.0/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.conf.urls.static import static
from django.contrib import admin
from django.urls import path
from livestreaming import settings
from streamingRoom import refreshRoomList, refreshLiveStatus, anchorStatistics, userStatistics, enterRoom, leaveRoom, refreshReplayList
from userSystem import login, forgetPwd, register, resetPwd, getUserInfo, uploadPortrait, checkSession, delSession, \
    changePwd

urlpatterns = [

                  path('admin/', admin.site.urls),
                  path('login/login', login.login),
                  path('login/forgetPwd', forgetPwd.forgetPwd),
                  path('login/register', register.register),
                  path('login/resetPwd', resetPwd.resetPwd),
                  path('login/changePwd', changePwd.changePwd),
                  path('room/refresh', refreshRoomList.refreshRoomList),
                  path('login/session', checkSession.checkSession),
                  path('login/delSession', delSession.delSession),
                  path('refreshStatus/refresh', refreshLiveStatus.refresh),
                  path('userCenter/getUserInfo', getUserInfo.getUserInfo),
                  path('userCenter/uploadPortrait', uploadPortrait.uploadPortrait),

                  # streaming
                  path('streaming/enter', enterRoom.enterRooom),
                  path('streaming/leave', leaveRoom.leaveRoom),

                  # statistics module
                  path('statistics/anchor/totalTime', anchorStatistics.totalTime),
                  path('statistics/anchor/topThreeTime', anchorStatistics.topThreeTime),
                  path('statistics/anchor/topThreeNum', anchorStatistics.topThreeNum),
                  path('statistics/anchor/avgTime', anchorStatistics.avgTime),
                  path('statistics/user/totalTime', userStatistics.totalTime),
                  path('statistics/user/avgTime', userStatistics.avgTime),
                  path('statistics/user/topThreeTime', userStatistics.topThreeTime),
                  path('statistics/user/topThreeNum', userStatistics.topThreeNum),

                  # replay
                  path('streaming/replay', refreshReplayList.replayList)

              ] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
