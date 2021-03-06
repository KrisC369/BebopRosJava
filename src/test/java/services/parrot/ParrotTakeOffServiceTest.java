package services.parrot;

import org.mockito.ArgumentCaptor;
import org.ros.node.topic.Publisher;
import services.TakeOffService;
import std_msgs.Empty;

import static org.mockito.Mockito.mock;

/** @author Hoang Tung Dinh */
public class ParrotTakeOffServiceTest extends AbstractParrotServiceTest<Empty> {

  @Override
  String createTopicName() {
    return "/bebop/takeoff";
  }

  @Override
  Empty createNewMessage() {
    return mock(Empty.class);
  }

  @Override
  void createServiceAndSendMessage(Publisher<Empty> publisher) {
    final TakeOffService parrotTakeOffService = ParrotTakeOffService.create(publisher);
    parrotTakeOffService.sendTakingOffMessage();
  }

  @Override
  ArgumentCaptor<Empty> createArgumentCaptor() {
    return ArgumentCaptor.forClass(Empty.class);
  }
}
