package applications.trajectory;

import control.Trajectory1d;
import control.Trajectory2d;
import control.Trajectory4d;

/**
 * Swing trajectory in 3D space as a 4D trajectory.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
class SwingTrajectory4D extends PeriodicTrajectory
        implements Trajectory4d {
    private final Trajectory2d swing;
    private final double yFactor;
    private final double xFactor;
    private final Trajectory1d angularMotion;

    SwingTrajectory4D(Point4D origin, double phase, double xzPlaneAngle,
            double radius,
            double frequency) {
        super(phase, origin, radius, frequency);
        xFactor = StrictMath.cos(xzPlaneAngle);
        yFactor = StrictMath.sin(xzPlaneAngle);
        this.swing = new PendulumTrajectory2D.Builder().setRadius(radius)
                .setFrequency(frequency).setOrigin(origin).build();
        //        this.angularMotion = new
        // ConstantVelocityAngularTrajectory1D(frequency,
        //                origin.getAngle() + phase);
        //keep constant yaw:
        this.angularMotion = new ConstantVelocityAngularTrajectory1D(0,
                0);
    }

    static Builder builder() {
        return new Builder();
    }

    @Override
    public double getDesiredPositionX(double timeInSeconds) {
        return xFactor * swing.getDesiredPositionAbscissa(timeInSeconds);
    }

    @Override
    public double getDesiredVelocityX(double timeInSeconds) {
        return xFactor * swing.getDesiredVelocityAbscissa
                (timeInSeconds);
    }

    @Override
    public double getDesiredPositionY(double timeInSeconds) {
        return yFactor * swing.getDesiredPositionAbscissa(timeInSeconds);
    }

    @Override
    public double getDesiredVelocityY(double timeInSeconds) {
        return yFactor * swing.getDesiredVelocityAbscissa(timeInSeconds);
    }

    @Override
    public double getDesiredPositionZ(double timeInSeconds) {
        return swing.getDesiredPositionOrdinate(timeInSeconds);
    }

    @Override
    public double getDesiredVelocityZ(double timeInSeconds) {
        return swing.getDesiredVelocityOrdinate(timeInSeconds);
    }

    @Override
    public double getDesiredAngleZ(double timeInSeconds) {
        return angularMotion.getDesiredPosition(timeInSeconds);
    }

    @Override
    public double getDesiredAngularVelocityZ(double timeInSeconds) {
        return angularMotion.getDesiredVelocity(timeInSeconds);
    }

    public static class Builder {
        private Point4D origin = Point4D.origin();
        private double xzPlaneAngle = 0;
        private double radius = 0;
        private double frequency = 0;
        private double phase = 0;

        public Builder setOrigin(Point4D origin) {
            this.origin = origin;
            return this;
        }

        public Builder setXzPlaneAngle(double xzPlaneAngle) {
            this.xzPlaneAngle = xzPlaneAngle;
            return this;
        }

        public Builder setRadius(double radius) {
            this.radius = radius;
            return this;
        }

        public Builder setFrequency(double frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder setPhase(double phase) {
            this.phase = phase;
            return this;
        }

        public SwingTrajectory4D build() {
            return new SwingTrajectory4D(origin, phase, xzPlaneAngle, radius,
                    frequency
            );
        }
    }
}
