import json
import logging
from typing import Any
from cloudevents.sdk.event import v1
from dapr.clients import DaprClient

def as_topic_name(event: Any) -> str:
  # schema prefix allow us to recognize our topics from other topics existing in the same ecosystem
  schema_prefix = "onlexnet:v1"
  # by practice, all events generated by avro has egnerated RECORD_SCHEMA property
  # so that we use them to extract full type name, used by convention as part of topic name
  topic_name = f"{schema_prefix}:{event.RECORD_SCHEMA.fullname}"
  return topic_name

def cont(dc: DaprClient, pubsub_name: str, event: Any, reply_to: v1.Event):

  topic_name = as_topic_name(event)
  traceid, exists = reply_to.Get("traceid")
  assert exists
  
  event_as_dict = event.to_avro_writable()
  as_json = json.dumps(event_as_dict)
  publish_metadata={ 'ttlInSeconds': '10', 'cloudevent.traceid': traceid}
  dc.publish_event(pubsub_name=pubsub_name, topic_name=topic_name, data = as_json, data_content_type="application/json", publish_metadata=publish_metadata)

def publish(dc: DaprClient, pubsub_name: str, event: Any, ):

  topic_name = as_topic_name(event)

  event_as_dict = event.to_avro_writable()
  as_json = json.dumps(event_as_dict)
  dc.publish_event(pubsub_name=pubsub_name, topic_name=topic_name, data = as_json, data_content_type="application/json", publish_metadata={ "ttlInSeconds": "10" })

