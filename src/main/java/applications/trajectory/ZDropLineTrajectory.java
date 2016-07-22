package applications.trajectory;

import applications.trajectory.points.Point4D;
import choreo.Choreography;
import com.google.common.collect.Lists;
import control.FiniteTrajectory4d;
import control.Trajectory4d;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A straight line trajectory in xy plane with sudden drops in the z dimension.
 * Source and destination point shoudl be in the same z plane.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class ZDropLineTrajectory extends BasicTrajectory implements FiniteTrajectory4d {

    private final FiniteTrajectory4d target;
    private final Point4D src;
    private final Point4D dst;
    private final double velocity;

    ZDropLineTrajectory(Point4D before, Point4D after, double speed, double drops,
            double dropDistance) {
        checkArgument(before.getZ() == after.getZ(),
                "Origin and destination should be in the same horizontal plane.");
        this.src = before;
        this.dst = after;
        this.velocity = speed;
        Choreography.Builder builder = Choreography.builder();

        List<Point4D> points = Lists.newArrayList();
        List<Point4D> dropPoints = Lists.newArrayList();
        FiniteTrajectory4d traj = Trajectories.newStraightLineTrajectory(before, after, 1);
        double duration = traj.getTrajectoryDuration();
        initTraj(traj);
        for (int i = 0; i < drops; i++) {
            double mark = (duration / drops) * i;
            points.add(
                    Point4D.create(traj.getDesiredPositionX(mark), traj.getDesiredPositionY(mark),
                            traj.getDesiredPositionZ(mark), traj.getDesiredAngleZ(mark)));
        }
        points.add(after);
        for (Point4D p : points) {
            dropPoints
                    .add(Point4D.create(p.getX(), p.getY(), p.getZ() - dropDistance, p.getAngle()));
        }
        for (int i = 0; i < drops; i++) {
            builder.withTrajectory(
                    Trajectories.newStraightLineTrajectory(points.get(i), dropPoints.get(i), 1));
            builder.withTrajectory(Trajectories
                    .newStraightLineTrajectory(dropPoints.get(i), points.get(i + 1), 1));
        }
        target = builder.build();
    }

    private static final void initTraj(Trajectory4d traj) {
        traj.getDesiredVelocityX(0);
        traj.getDesiredVelocityY(0);
        traj.getDesiredVelocityZ(0);
        traj.getDesiredAngleZ(0);
    }

    @Override
    public double getDesiredPositionX(double timeInSeconds) {
        final double currentTime = getRelativeTime(timeInSeconds);
        return getTargetTrajectory().getDesiredPositionX(currentTime);
    }

    @Override
    public double getDesiredVelocityX(double timeInSeconds) {
        final double currentTime = getRelativeTime(timeInSeconds);
        return getTargetTrajectory().getDesiredVelocityX(currentTime);
    }

    @Override
    public double getDesiredPositionY(double timeInSeconds) {
        final double currentTime = getRelativeTime(timeInSeconds);
        return getTargetTrajectory().getDesiredPositionY(currentTime);
    }

    @Override
    public double getDesiredVelocityY(double timeInSeconds) {
        final double currentTime = getRelativeTime(timeInSeconds);
        return getTargetTrajectory().getDesiredVelocityY(currentTime);
    }

    @Override
    public double getDesiredPositionZ(double timeInSeconds) {
        final double currentTime = getRelativeTime(timeInSeconds);
        return getTargetTrajectory().getDesiredPositionZ(currentTime);
    }

    @Override
    public double getDesiredVelocityZ(double timeInSeconds) {
        final double currentTime = getRelativeTime(timeInSeconds);
        return getTargetTrajectory().getDesiredVelocityZ(currentTime);
    }

    @Override
    public double getDesiredAngleZ(double timeInSeconds) {
        final double currentTime = getRelativeTime(timeInSeconds);
        return getTargetTrajectory().getDesiredAngleZ(currentTime);
    }

    @Override
    public double getDesiredAngularVelocityZ(double timeInSeconds) {
        final double currentTime = getRelativeTime(timeInSeconds);
        return getTargetTrajectory().getDesiredAngularVelocityZ(currentTime);
    }

    @Override
    public String toString() {
        return "ZDropLineTrajectory{" + "velocity=" + velocity
                + ", src point=" + src + ", target point="
                + dst + '}';
    }

    @Override
    public double getTrajectoryDuration() {
        return getTargetTrajectory().getTrajectoryDuration();
    }

    private FiniteTrajectory4d getTargetTrajectory() {
        return target;
    }
}