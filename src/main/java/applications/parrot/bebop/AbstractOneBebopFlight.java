package applications.parrot.bebop;

import applications.ExampleFlight;
import applications.trajectory.TrajectoryServer;
import com.google.common.collect.ImmutableList;
import commands.Command;
import commands.WaitForLocalizationDecorator;
import commands.bebopcommands.BebopFollowTrajectory;
import commands.bebopcommands.BebopHover;
import commands.bebopcommands.BebopLand;
import commands.bebopcommands.BebopTakeOff;
import control.FiniteTrajectory4d;
import control.PidParameters;
import control.localization.BebopStateEstimatorWithPoseStampedAndOdom;
import control.localization.StateEstimator;
import geometry_msgs.PoseStamped;
import nav_msgs.Odometry;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.FlyingStateService;
import services.LandService;
import services.MinDiffBodyFrameVelocityFilter;
import services.ResetService;
import services.TakeOffService;
import services.Velocity4dService;
import services.parrot.BebopServiceFactory;
import services.parrot.ParrotServiceFactory;
import services.rossubscribers.MessagesSubscriberService;
import taskexecutor.Task;
import taskexecutor.TaskType;
import time.RosTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Main abstract entry for running Bebop. Implementation of this abstract class must provide a
 * trajectory for the drone by implementing {@link TrajectoryServer#getConcreteTrajectory()}.
 *
 * @author Hoang Tung Dinh
 */
public abstract class AbstractOneBebopFlight extends AbstractNodeMain implements TrajectoryServer {
  private static final Logger logger = LoggerFactory.getLogger(AbstractOneBebopFlight.class);
  private static final String DRONE_NAME = "bebop";
  private final String nodeName;

  protected AbstractOneBebopFlight(String nodeName) {
    this.nodeName = nodeName;
  }

  private static PidParameters getPidParameters(
      ConnectedNode connectedNode,
      String argKp,
      String argKd,
      String argKi,
      String argLagTimeInSeconds) {
    final double pidLinearXKp = connectedNode.getParameterTree().getDouble(argKp);
    final double pidLinearXKd = connectedNode.getParameterTree().getDouble(argKd);
    final double pidLinearXKi = connectedNode.getParameterTree().getDouble(argKi);
    final double pidLagTimeInSeconds =
        connectedNode.getParameterTree().getDouble(argLagTimeInSeconds);
    return PidParameters.builder()
        .setKp(pidLinearXKp)
        .setKd(pidLinearXKd)
        .setKi(pidLinearXKi)
        .setLagTimeInSeconds(pidLagTimeInSeconds)
        .build();
  }

  private static MessagesSubscriberService<PoseStamped> getPoseSubscriber(
      ConnectedNode connectedNode) {
    final String poseTopic = "/arlocros/pose";
    logger.info("Subscribed to {} for getting pose.", poseTopic);
    return MessagesSubscriberService.create(
        connectedNode.<PoseStamped>newSubscriber(poseTopic, PoseStamped._TYPE),
        RosTime.create(connectedNode));
  }

  private static MessagesSubscriberService<Odometry> getOdometrySubscriber(
      ConnectedNode connectedNode) {
    final String odometryTopic = "/" + DRONE_NAME + "/odom";
    logger.info("Subscribed to {} for getting odometry", odometryTopic);
    return MessagesSubscriberService.create(
        connectedNode.<Odometry>newSubscriber(odometryTopic, Odometry._TYPE),
        RosTime.create(connectedNode));
  }

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of(nodeName);
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {
    final PidParameters pidLinearX =
        getPidParameters(
            connectedNode,
            "beswarm/pid_linear_x_kp",
            "beswarm/pid_linear_x_kd",
            "beswarm/pid_linear_x_ki",
            "beswarm/pid_lag_time_in_seconds");
    final PidParameters pidLinearY =
        getPidParameters(
            connectedNode,
            "beswarm/pid_linear_y_kp",
            "beswarm/pid_linear_y_kd",
            "beswarm/pid_linear_y_ki",
            "beswarm/pid_lag_time_in_seconds");
    final PidParameters pidLinearZ =
        getPidParameters(
            connectedNode,
            "beswarm/pid_linear_z_kp",
            "beswarm/pid_linear_z_kd",
            "beswarm/pid_linear_z_ki",
            "beswarm/pid_lag_time_in_seconds");
    final PidParameters pidAngularZ =
        getPidParameters(
            connectedNode,
            "beswarm/pid_angular_z_kp",
            "beswarm/pid_angular_z_kd",
            "beswarm/pid_angular_z_ki",
            "beswarm/pid_lag_time_in_seconds");

    final ParrotServiceFactory parrotServiceFactory =
        BebopServiceFactory.create(connectedNode, DRONE_NAME);
    final LandService landService = parrotServiceFactory.createLandService();
    final FlyingStateService flyingStateService = parrotServiceFactory.createFlyingStateService();
    final Velocity4dService velocity4dService =
        MinDiffBodyFrameVelocityFilter.create(
            parrotServiceFactory.createVelocity4dService(), 0.000015);
    final TakeOffService takeOffService = parrotServiceFactory.createTakeOffService();
    final ResetService resetService = parrotServiceFactory.createResetService();
    final StateEstimator stateEstimator =
        BebopStateEstimatorWithPoseStampedAndOdom.create(
            getPoseSubscriber(connectedNode), getOdometrySubscriber(connectedNode));
    final Task flyTask =
        createFlyTask(
            connectedNode,
            pidLinearX,
            pidLinearY,
            pidLinearZ,
            pidAngularZ,
            landService,
            flyingStateService,
            velocity4dService,
            takeOffService,
            resetService,
            stateEstimator);

    final Task emergencyTask = createEmergencyTask(landService, flyingStateService);

    final ExampleFlight exampleFlight = ExampleFlight.create(connectedNode, flyTask, emergencyTask);

    // without this code, the take off message cannot be sent properly (I don't understand why).
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      logger.info("Warm up time is interrupted.", e);
      Thread.currentThread().interrupt();
    }

    exampleFlight.fly();
  }

  private static Task createEmergencyTask(
      LandService landService, FlyingStateService flyingStateService) {
    final Command land = BebopLand.create(landService, flyingStateService);
    return Task.create(ImmutableList.of(land), TaskType.FIRST_ORDER_EMERGENCY);
  }

  private Task createFlyTask(
      ConnectedNode connectedNode,
      PidParameters pidLinearX,
      PidParameters pidLinearY,
      PidParameters pidLinearZ,
      PidParameters pidAngularZ,
      LandService landService,
      FlyingStateService flyingStateService,
      Velocity4dService velocity4dService,
      TakeOffService takeOffService,
      ResetService resetService,
      StateEstimator stateEstimator) {
    final FiniteTrajectory4d trajectory4d = getConcreteTrajectory();

    final Collection<Command> commands = new ArrayList<>();

    final Command takeOff = BebopTakeOff.create(takeOffService, flyingStateService, resetService);
    commands.add(takeOff);

    final Command hoverFiveSeconds =
        BebopHover.create(5, RosTime.create(connectedNode), velocity4dService, stateEstimator);
    commands.add(hoverFiveSeconds);

    final Command followTrajectory =
        BebopFollowTrajectory.builder()
            .withVelocity4dService(velocity4dService)
            .withStateEstimator(stateEstimator)
            .withTimeProvider(RosTime.create(connectedNode))
            .withTrajectory4d(trajectory4d)
            .withDurationInSeconds(trajectory4d.getTrajectoryDuration())
            .withPidLinearXParameters(pidLinearX)
            .withPidLinearYParameters(pidLinearY)
            .withPidLinearZParameters(pidLinearZ)
            .withPidAngularZParameters(pidAngularZ)
            .withControlRateInSeconds(0.01)
            .build();

    final Command waitForLocalizationThenFollowTrajectory =
        WaitForLocalizationDecorator.create(stateEstimator, followTrajectory);
    commands.add(waitForLocalizationThenFollowTrajectory);

    final Command hoverThreeSeconds =
        BebopHover.create(3, RosTime.create(connectedNode), velocity4dService, stateEstimator);
    commands.add(hoverThreeSeconds);

    final Command land = BebopLand.create(landService, flyingStateService);
    commands.add(land);

    return Task.create(ImmutableList.copyOf(commands), TaskType.NORMAL_TASK);
  }
}