package applications.parrot.tumsim.multidrone;

import applications.parrot.tumsim.AbstractTumSimulatorExample;
import applications.trajectory.Trajectories;
import applications.trajectory.geom.point.Point3D;
import applications.trajectory.geom.point.Point4D;
import choreo.Choreography;
import control.FiniteTrajectory4d;
import control.Trajectory4d;

/**
 * Example trajectory for a corkscrew motion with possible parametrization for multiple drone
 * instantiation. Place drone at (0,0,1)
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class TumSimulatorMultiDroneCorkscrewExample1 extends AbstractTumSimulatorExample {
  public static final double DEFAULT_VELOCITY = 0.1;
  public static final double DEFAULT_FREQUENCY = 0.2;
  public static final double DEFAULT_RADIUS = 0.5;
  private static final double STARTX = 1;
  private static final double STARTY = 0;
  private static final double STARTZ = 1;
  private static final double ENDX = 2.5;
  private static final double ENDY = -3.0;
  private static final double ENDZ = 1.5;
  private static final double ORIENTATION = -Math.PI / 2;
  private static final double DISPLACEMENT = 0.5;
  private final double radius;
  private final double frequency;
  private final double velocity;
  private final Point4D start;
  private final Point3D end;
  private final double phaseShift;

  /** Default Constructor. */
  public TumSimulatorMultiDroneCorkscrewExample1() {
    this(0);
  }

  /**
   * Constructor with a phase shift in place for multi drone flight.
   *
   * @param phaseShift the phase shift.
   */
  public TumSimulatorMultiDroneCorkscrewExample1(double phaseShift) {
    super("TumRunZDropTrajectory2");
    this.radius = DEFAULT_RADIUS;
    this.frequency = DEFAULT_FREQUENCY;
    this.velocity = DEFAULT_VELOCITY;
    this.start = Point4D.create(STARTX, STARTY, STARTZ, ORIENTATION);
    this.end = Point3D.create(ENDX, ENDY, ENDZ);
    this.phaseShift = phaseShift;
  }

  @Override
  public FiniteTrajectory4d getConcreteTrajectory() {

    Point4D startShift =
        Point4D.create(STARTX - StrictMath.cos(phaseShift), STARTY, STARTZ, ORIENTATION);
    Trajectory4d init = Trajectories.newHoldPositionTrajectory(startShift);
    FiniteTrajectory4d first =
        Trajectories.newCorkscrewTrajectory(start, end, velocity, radius, frequency, phaseShift);
    Point3D endShift =
        Point3D.create(
            ENDX - StrictMath.cos(phaseShift) * DISPLACEMENT,
            ENDY,
            ENDZ - StrictMath.cos(phaseShift) * DISPLACEMENT);
    Trajectory4d inter =
        Trajectories.newHoldPositionTrajectory(Point4D.from(endShift, ORIENTATION));
    return Choreography.builder()
        .withTrajectory(init)
        .forTime(4)
        .withTrajectory(first)
        .forTime(first.getTrajectoryDuration())
        .withTrajectory(inter)
        .forTime(5)
        .build();
  }
}
