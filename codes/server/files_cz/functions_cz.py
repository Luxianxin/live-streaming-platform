from userSystem import models


def save_fail_log(request_path):
    new_log = models.Logging(api=request_path, success=0, message="failed")
    new_log.save()


def save_success_log(request_path, user_id):
    new_log = models.Logging(api=request_path, userId=user_id, success=1, message="sucessed")
    new_log.save()
