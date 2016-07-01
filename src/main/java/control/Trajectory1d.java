package control;

/**
 * Representation of the trajectory function.
 *
 * @author Hoang Tung Dinh
 */
public interface Trajectory1d {
    /**
     * @param timeInSeconds the point in time to get the position for.
     * @return The desired value of the position in the dimenstion specified
     * by this object for the given point in time.
     */
    double getDesiredPosition(double timeInSeconds);

    /**
     * @param timeInSeconds the point in time to get the position for.
     * @return The desired value of the momentary velocity in the dimenstion
     * specified
     * by this object for the given point in time.
     */
    double getDesiredVelocity(double timeInSeconds);
}
