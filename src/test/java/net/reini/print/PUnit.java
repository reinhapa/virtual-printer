package net.reini.print;

/**
 * Represents a printable unit measure
 * 
 * @author Patrick Reinhart
 */
public class PUnit {
  /** dots per inch */
  private static final double INCH = 72;
  /** dots per millimeter (72 per inch) */
  private static final double MM = 2.83465;

  /**
   * @param millimeters represented by this unit
   * @return print unit
   */
  public static PUnit MM(double millimeters) {
    return new PUnit(millimeters * MM);
  }

  /**
   * @param inches represented by this unit
   * @return print unit
   */
  public static PUnit INCH(double inches) {
    return new PUnit(inches * INCH);
  }

  private final double _points;

  protected PUnit(double points) {
    _points = points;
  }

  /**
   * @return the points represented by this value
   */
  public double getPoints() {
    return _points;
  }
}
