from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Optional as _Optional

DESCRIPTOR: _descriptor.FileDescriptor

class TimeClient(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class NewTime(_message.Message):
    __slots__ = ("yyyymmdd", "hhmm")
    YYYYMMDD_FIELD_NUMBER: _ClassVar[int]
    HHMM_FIELD_NUMBER: _ClassVar[int]
    yyyymmdd: int
    hhmm: int
    def __init__(self, yyyymmdd: _Optional[int] = ..., hhmm: _Optional[int] = ...) -> None: ...