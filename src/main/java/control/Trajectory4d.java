package control;

/**
 * @author Hoang Tung Dinh
 */
public interface Trajectory4d {
    double getDesiredPositionX(double timeInSeconds);

    double getDesiredVelocityX(double timeInSeconds);

    double getDesiredPositionY(double timeInSeconds);

    double getDesiredVelocityY(double timeInSeconds);

    double getDesiredPositionZ(double timeInSeconds);

    double getDesiredVelocityZ(double timeInSeconds);

    double getDesiredAngleZ(double timeInSeconds);

    double getDesiredAngularVelocityZ(double timeInSeconds);

    //    /**
    //     * This method returns the trajectory of the yaw of the drone. The
    // yaw is defined according to the right hand
    //     * rule w.r.t. the z axis. The yaw equals to zero when the
    // orientation of the drone is [x=1, y=0, z=0]. For an
    //     * illustration, See
    // <a href="https://en.wikipedia.org/wiki/Euler_angles">this wikipedia
    // page</a>. The yaw angle
    //     * is in radians, ranging from -infinite to infinite.
    //     *
    //     * @return the trajectory of the yaw
    //     */
    //    Trajectory1d getTrajectoryAngularZ();
}
