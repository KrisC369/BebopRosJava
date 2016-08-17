package time;

/**
 * Time provider.
 *
 * @author Hoang Tung Dinh
 */
public interface TimeProvider {
  /**
   * Gets the current time in nano seconds.
   *
   * @return the current time in nano seconds
   */
  long getCurrentTimeNanoSeconds();
}
