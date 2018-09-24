class ReFailed(Exception):
    def __init__(self):
        super().__init__(self)
        self.error_info = 're failed!'

    def __str__(self):
        return self.error_info


class ModelNotExist(Exception):
    def __init__(self):
        super().__init__(self)
        self.error_info = 'model not exist!'

    def __str__(self):
        return self.error_info


class DuplicatedStatus(Exception):
    def __init__(self):
        super().__init__(self)
        self.error_info = 'anchor already streaming!'

    def __str__(self):
        return self.error_info
