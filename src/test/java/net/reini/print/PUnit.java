/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2021 Patrick Reinhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
