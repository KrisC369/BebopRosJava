package services.ros_subscribers;

import com.google.common.base.Optional;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import org.ros.internal.message.Message;
import org.ros.message.MessageListener;
import org.ros.node.topic.Subscriber;

import java.util.LinkedList;
import java.util.Queue;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author mhct
 */
public class MessagesSubscriberService<T extends Message> {
    private final Subscriber<T> subscriber;
    private final MessagesListener<T> messagesListener;
    private final int maxMessageQueueSize;
    private boolean startedListeningToMessages = false;

    private static final int DEFAULT_MESSAGE_QUEUE__SIZE = 1;

    protected MessagesSubscriberService(Subscriber<T> subscriber, int maxMessageQueueSize) {
        checkArgument(maxMessageQueueSize >= 1,
                String.format("Queue size must be at least 1, but it is %d.", maxMessageQueueSize));
        this.subscriber = subscriber;
        this.messagesListener = MessagesListener.create(maxMessageQueueSize);
        this.maxMessageQueueSize = maxMessageQueueSize;
    }

    public static <Type extends Message> MessagesSubscriberService<Type> create(Subscriber<Type> subscriber) {
        return new MessagesSubscriberService<>(subscriber, DEFAULT_MESSAGE_QUEUE__SIZE);
    }

    public static <Type extends Message> MessagesSubscriberService<Type> create(Subscriber<Type> subscriber,
            int maxMessageQueueSize) {
        return new MessagesSubscriberService<>(subscriber, maxMessageQueueSize);
    }

    public void startListeningToMessages() {
        if (!startedListeningToMessages) {
            subscriber.addMessageListener(messagesListener);
            startedListeningToMessages = true;
        }
    }

    public Optional<T> getMostRecentMessage() {
        return messagesListener.getMostRecentMessage();
    }

    public Queue<T> getMessageQueue() {
        return messagesListener.getMessageQueue();
    }

    public int getMaxMessageQueueSize() {
        return maxMessageQueueSize;
    }

    private static final class MessagesListener<K extends Message> implements MessageListener<K> {
        private final Queue<K> messageQueue;

        private MessagesListener(int maxQueueSize) {
            messageQueue = Queues.synchronizedQueue(EvictingQueue.<K>create(maxQueueSize));
        }

        public static <Type extends Message> MessagesListener<Type> create(int maxQueueSize) {
            return new MessagesListener<>(maxQueueSize);
        }

        @Override
        public void onNewMessage(K t) {
            messageQueue.add(t);
        }

        Optional<K> getMostRecentMessage() {
            if (messageQueue.isEmpty()) {
                return Optional.<K>absent();
            } else {
                return Optional.<K>of(messageQueue.peek());
            }
        }

        Queue<K> getMessageQueue() {
            return new LinkedList<>(messageQueue);
        }
    }
}