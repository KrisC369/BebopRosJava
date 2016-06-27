package choreo;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import control.Trajectory4d;

import java.util.List;
import java.util.Queue;

/**
 * A choreography represents a sequence of different trajectories to be
 * executed for set durations.
 * The choreography is in itself a trajectory4d instance so it can be used as
 * a single trajectory from the lower level control point-of-view.
 * Using the builder, one can create choreography instances and configure
 * them with different trajectories to be executed in sequence.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class Choreography implements Trajectory4d {
    private final Queue<ChoreoSegment> segments;
    private double timeWindowShift;

    private Choreography(List<ChoreoSegment> segmentsArg) {
        segments = Queues.newArrayDeque(segmentsArg);
        timeWindowShift = 0d;
    }

    private void checkChoreoSegments(double timeInSeconds) {
        double normTime = normalize(timeInSeconds);
        if (normTime >= getCurrentSegment().getDuration()) {
            shiftSegments();
        }
    }

    private void shiftSegments() {
        if (this.segments.size() > 1) {
            this.timeWindowShift += segments.poll().getDuration();
        }
    }

    private double normalize(double timeInSeconds) {
        return timeInSeconds - timeWindowShift;
    }

    private ChoreoSegment getCurrentSegment() {
        return segments.peek();
    }

    @Override
    public double getDesiredPositionX(double timeInSeconds) {
        checkChoreoSegments(timeInSeconds);
        return getCurrentSegment().getTarget()
                .getDesiredPositionX(timeInSeconds);
    }

    @Override
    public double getDesiredVelocityX(double timeInSeconds) {
        checkChoreoSegments(timeInSeconds);
        return getCurrentSegment().getTarget()
                .getDesiredVelocityX(timeInSeconds);
    }

    @Override
    public double getDesiredPositionY(double timeInSeconds) {
        checkChoreoSegments(timeInSeconds);
        return getCurrentSegment().getTarget()
                .getDesiredPositionY(timeInSeconds);
    }

    @Override
    public double getDesiredVelocityY(double timeInSeconds) {
        checkChoreoSegments(timeInSeconds);
        return getCurrentSegment().getTarget()
                .getDesiredVelocityY(timeInSeconds);
    }

    @Override
    public double getDesiredPositionZ(double timeInSeconds) {
        checkChoreoSegments(timeInSeconds);
        return getCurrentSegment().getTarget()
                .getDesiredPositionZ(timeInSeconds);
    }

    @Override
    public double getDesiredVelocityZ(double timeInSeconds) {
        checkChoreoSegments(timeInSeconds);
        return getCurrentSegment().getTarget()
                .getDesiredVelocityZ(timeInSeconds);
    }

    @Override
    public double getDesiredAngleZ(double timeInSeconds) {
        checkChoreoSegments(timeInSeconds);
        return getCurrentSegment().getTarget().getDesiredAngleZ(timeInSeconds);
    }

    @Override
    public double getDesiredAngularVelocityZ(double timeInSeconds) {
        checkChoreoSegments(timeInSeconds);
        return getCurrentSegment().getTarget()
                .getDesiredAngularVelocityZ(timeInSeconds);
    }

    /**
     * @return A choreography builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A segment in the choreography specified by a target trajectory and a
     * duration for which to execute this trajectory.
     */
    @AutoValue
    public static abstract class ChoreoSegment {
        /**
         * @return The trajectory to be executed in this segment.
         */
        public abstract Trajectory4d getTarget();

        /**
         * @return The duration this trajectory should be executed for.
         */
        public abstract double getDuration();
    }

    /**
     * Builder class for building choreography instances.
     */
    public static class Builder {
        private final List<ChoreoSegment> segments;

        /**
         * Creates a new Builder for choreographies.
         */
        private Builder() {
            this.segments = Lists.newArrayList();
        }

        /**
         * @param trajectory The trajectory to add to the choreography.
         * @return A segmentBuilder instance to specify the duration to
         * execute given trajectory with.
         */
        public SegmentBuilder withTrajectory(Trajectory4d trajectory) {
            return new SegmentBuilder(trajectory);
        }

        private Builder getBuilder() {
            return this;
        }

        /**
         * @return A fully built choreography instance.
         */
        public Choreography build() {
            Choreography choreography = new Choreography(segments);
            return choreography;
        }

        /**
         * Builder for adding time duration information to supplied
         * trajectories.
         */
        public final class SegmentBuilder {

            private final Trajectory4d target;

            private SegmentBuilder(Trajectory4d target) {
                this.target = target;
            }

            /**
             * @param duration the duration of the previously added trajectory.
             * @return A Builder instance.
             */
            public Builder forTime(double duration) {
                segments.add(new AutoValue_Choreography_ChoreoSegment(target,
                        duration));
                return getBuilder();
            }
        }
    }
}
