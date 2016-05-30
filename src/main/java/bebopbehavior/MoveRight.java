package bebopbehavior;

import comm.VelocityPublisher;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Command for moving to the right. It is a facade which uses {@link Move}.
 *
 * @author Hoang Tung Dinh
 */
public final class MoveRight implements Command {

    private final VelocityPublisher velocityPublisher;
    private final double speed;
    private final double durationInSeconds;

    private MoveRight(VelocityPublisher velocityPublisher, double speed, double durationInSeconds) {
        this.velocityPublisher = velocityPublisher;
        this.speed = speed;
        this.durationInSeconds = durationInSeconds;
    }

    public static MoveRight create(VelocityPublisher velocityPublisher, double speed, double durationInSeconds) {
        checkArgument(durationInSeconds > 0,
                String.format("Duration must be a positive value, but it is %f", durationInSeconds));
        checkArgument(speed > 0, String.format("Speed must be a positive value, but it is %f", speed));
        return new MoveRight(velocityPublisher, speed, durationInSeconds);
    }

    @Override
    public void execute() {
        final Velocity velocity = Velocity.builder().linearY(-speed).build();
        final Command move = Move.builder()
                .velocityPublisher(velocityPublisher)
                .velocity(velocity)
                .durationInSeconds(durationInSeconds)
                .build();
        move.execute();
    }
}
