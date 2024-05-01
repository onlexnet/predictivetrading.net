package onlexnet.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Empty;

import io.dapr.v1.AppCallbackGrpc.AppCallbackImplBase;
import io.dapr.v1.CommonProtos.InvokeRequest;
import io.dapr.v1.CommonProtos.InvokeResponse;
import io.dapr.v1.DaprAppCallbackProtos;
import io.dapr.v1.DaprAppCallbackProtos.BindingEventRequest;
import io.dapr.v1.DaprAppCallbackProtos.BindingEventResponse;
import io.dapr.v1.DaprAppCallbackProtos.ListInputBindingsResponse;
import io.dapr.v1.DaprAppCallbackProtos.ListTopicSubscriptionsResponse;
import io.dapr.v1.DaprAppCallbackProtos.TopicEventRequest;
import io.dapr.v1.DaprAppCallbackProtos.TopicEventResponse;
import io.dapr.v1.DaprAppCallbackProtos.TopicSubscription;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

// source: https://github.com/dapr/java-sdk/blob/master/examples/src/main/java/io/dapr/examples/pubsub/grpc/SubscriberGrpcService.java
/** Entry point of Grpc application services for DAPR */
@Component
@Slf4j
@RequiredArgsConstructor
public class DaprCallback extends AppCallbackImplBase {
    private final ObjectMapper objectMapper;

    private final List<TopicSubscription> topicSubscriptionList = new ArrayList<>();
    private final List<EventListener<?>> listeners;

    private final ConcurrentHashMap<String, EventListener<?>> topicListeners = new ConcurrentHashMap<>();

    // https://stackoverflow.com/a/47660919/1237627
    abstract class IgnoreSchemaProperty {
        // You have to use the correct package for JsonIgnore,
        // fasterxml or codehaus
        @JsonIgnore
        abstract void getSchema();
    }

    @PostConstruct
    void init() {
        log.info("listTopicSubscriptions invoked");
        for (var listener : listeners) {
            var eventClass = listener.getEventClass();

            // objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.addMixIn(eventClass, IgnoreSchemaProperty.class);

            var supportedClassCanonicalName = eventClass.getCanonicalName();
            var topic = "onlexnet:v1:" + supportedClassCanonicalName;
            topicListeners.put(topic, listener);
            registerConsumer("pubsub", topic, false);

        }
    }

    @Override
    public void onInvoke(InvokeRequest request, StreamObserver<InvokeResponse> responseObserver) {
        // TODO Auto-generated method stub
        super.onInvoke(request, responseObserver);
    }

    @Override
    public void listTopicSubscriptions(Empty request, StreamObserver<ListTopicSubscriptionsResponse> responseObserver) {

        try {
            var builder = ListTopicSubscriptionsResponse.newBuilder();
            topicSubscriptionList.forEach(builder::addSubscriptions);
            ListTopicSubscriptionsResponse response = builder.build();
            responseObserver.onNext(response);
        } catch (Throwable e) {
            responseObserver.onError(e);
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    @SneakyThrows
    public void onTopicEvent(TopicEventRequest request, StreamObserver<TopicEventResponse> responseObserver) {

        try {
            var topic = request.getTopic();
            if (topicListeners.containsKey(topic)) {
                var listener = topicListeners.get(topic);
                var eventAsString = request.getData().toStringUtf8();
                handleEvent(listener, eventAsString);
            }

            var response = DaprAppCallbackProtos.TopicEventResponse.newBuilder()
                    .setStatus(DaprAppCallbackProtos.TopicEventResponse.TopicEventResponseStatus.SUCCESS)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Throwable e) {
            log.error("errrrrrrrrrrrrr", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void listInputBindings(Empty request, StreamObserver<ListInputBindingsResponse> responseObserver) {
        // TODO Auto-generated method stub
        super.listInputBindings(request, responseObserver);
    }

    @Override
    public void onBindingEvent(BindingEventRequest request, StreamObserver<BindingEventResponse> responseObserver) {
        // TODO Auto-generated method stub
        super.onBindingEvent(request, responseObserver);
    }

    @SneakyThrows
    private <T> void handleEvent(EventListener<T> listener, String eventAsString) {
        var clazz = listener.getEventClass();
        var event = objectMapper.readValue(eventAsString, clazz);
        listener.onEvent(event);
    }

    /**
     * Add pubsub name and topic to topicSubscriptionList.
     * 
     * @param topic         the topic
     * @param pubsubName    the pubsub name
     * @param isBulkMessage flag to enable/disable bulk subscribe
     */
    public void registerConsumer(String pubsubName, String topic, boolean isBulkMessage) {
        topicSubscriptionList.add(TopicSubscription
                .newBuilder()
                .setPubsubName(pubsubName)
                .setTopic(topic)
                .setBulkSubscribe(DaprAppCallbackProtos.BulkSubscribeConfig.newBuilder().setEnabled(isBulkMessage))
                .build());
    }
}