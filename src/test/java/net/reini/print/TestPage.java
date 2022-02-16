/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2022 Patrick Reinhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.reini.print;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.net.InetAddress;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.MediaTray;

/**
 * Implements a simple test page output.
 * 
 * @author Patrick Reinhart
 */
public class TestPage implements Printable {
  private static final BasicStroke LINE_STROKE_02 =
      new BasicStroke((float) PUnit.MM(0.2).getPoints());
  private static final BasicStroke LINE_STROKE_04 =
      new BasicStroke((float) PUnit.MM(0.4).getPoints());
  private static final Float FONT_SIZE_TITLE = Float.valueOf(10);
  private static final Float FONT_SIZE = Float.valueOf(8);
  private static final String FONT_FAMILY = "courier";

  private final String printerName;
  private final List<MediaTray> trays;

  /**
   * Constructor
   * 
   * @param printerName the name of the printer shown on the test page
   * @param trays the list of trays listed on the test page
   */
  public TestPage(String printerName, List<MediaTray> trays) {
    this.printerName = printerName;
    this.trays = trays;
  }

  //
  // Printable interface
  //

  @Override
  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
      throws PrinterException {
    Graphics2D g2d = (Graphics2D) graphics;
    g2d.translate(pageFormat.getImageableX() + PUnit.MM(.2).getPoints(),
        pageFormat.getImageableY() + PUnit.MM(.2).getPoints());

    Point2D.Float titlePosition = new Point2D.Float();
    Point2D.Float valuePosition = new Point2D.Float();
    Point2D.Float fontPosition = new Point2D.Float();
    Line2D.Double line = new Line2D.Double();

    double verticalSpace = PUnit.MM(2).getPoints();
    double left = PUnit.MM(3).getPoints();
    double titleLeft = PUnit.MM(40).getPoints();
    double currentTop = verticalSpace + FONT_SIZE_TITLE.doubleValue();
    double width = pageFormat.getImageableWidth() - PUnit.MM(.4).getPoints();
    double height = pageFormat.getImageableHeight() - PUnit.MM(.4).getPoints();

    AttributedString astring;
    Map<TextAttribute, Object> attrs_title = new HashMap<>();
    Map<TextAttribute, Object> attrs_key = new HashMap<>();
    Map<TextAttribute, Object> attrs_value = new HashMap<>();

    attrs_title.put(TextAttribute.FAMILY, FONT_FAMILY);
    attrs_title.put(TextAttribute.SIZE, FONT_SIZE_TITLE);
    attrs_title.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);

    attrs_key.put(TextAttribute.FAMILY, FONT_FAMILY);
    attrs_key.put(TextAttribute.SIZE, FONT_SIZE);
    attrs_key.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);

    attrs_value.put(TextAttribute.FAMILY, FONT_FAMILY);
    attrs_value.put(TextAttribute.SIZE, FONT_SIZE);
    attrs_value.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);

    Rectangle2D.Double rectangle = new Rectangle2D.Double();
    rectangle.setRect(0, 0, width, height);
    g2d.draw(rectangle);

    titlePosition.setLocation(left, currentTop);
    valuePosition.setLocation(titleLeft, currentTop);
    astring = new AttributedString("Test Page", attrs_title);
    g2d.drawString(astring.getIterator(), titlePosition.x, titlePosition.y);
    currentTop += verticalSpace;
    g2d.setStroke(LINE_STROKE_04);
    line.setLine(0, currentTop, width, currentTop);
    g2d.draw(line);

    currentTop += verticalSpace + FONT_SIZE.doubleValue();
    titlePosition.setLocation(left, currentTop);
    valuePosition.setLocation(titleLeft, currentTop);
    astring = new AttributedString("Print date/time:", attrs_key);
    g2d.drawString(astring.getIterator(), titlePosition.x, titlePosition.y);
    astring = new AttributedString(RFC_1123_DATE_TIME.format(Instant.now()), attrs_value);
    g2d.drawString(astring.getIterator(), valuePosition.x, valuePosition.y);

    currentTop += FONT_SIZE.doubleValue();
    titlePosition.setLocation(left, currentTop);
    valuePosition.setLocation(titleLeft, currentTop);
    astring = new AttributedString("Host name:", attrs_key);
    g2d.drawString(astring.getIterator(), titlePosition.x, titlePosition.y);
    astring = new AttributedString(getHostName(), attrs_value);
    g2d.drawString(astring.getIterator(), valuePosition.x, valuePosition.y);

    currentTop += FONT_SIZE.doubleValue();
    titlePosition.setLocation(left, currentTop);
    valuePosition.setLocation(titleLeft, currentTop);
    astring = new AttributedString("OS name:", attrs_key);
    g2d.drawString(astring.getIterator(), titlePosition.x, titlePosition.y);
    astring = new AttributedString(System.getProperty("os.name", "unknown"), attrs_value);
    g2d.drawString(astring.getIterator(), valuePosition.x, valuePosition.y);

    currentTop += FONT_SIZE.doubleValue();
    titlePosition.setLocation(left, currentTop);
    valuePosition.setLocation(titleLeft, currentTop);
    astring = new AttributedString("OS architecture:", attrs_key);
    g2d.drawString(astring.getIterator(), titlePosition.x, titlePosition.y);
    astring = new AttributedString(System.getProperty("os.arch", "unknown"), attrs_value);
    g2d.drawString(astring.getIterator(), valuePosition.x, valuePosition.y);

    currentTop += FONT_SIZE.doubleValue();
    titlePosition.setLocation(left, currentTop);
    valuePosition.setLocation(titleLeft, currentTop);
    astring = new AttributedString("OS Version:", attrs_key);
    g2d.drawString(astring.getIterator(), titlePosition.x, titlePosition.y);
    astring = new AttributedString(System.getProperty("os.version", "unknown"), attrs_value);
    g2d.drawString(astring.getIterator(), valuePosition.x, valuePosition.y);

    currentTop += FONT_SIZE.doubleValue();
    titlePosition.setLocation(left, currentTop);
    valuePosition.setLocation(titleLeft, currentTop);
    astring = new AttributedString("Java vendor:", attrs_key);
    g2d.drawString(astring.getIterator(), titlePosition.x, titlePosition.y);
    astring = new AttributedString(System.getProperty("java.vendor", "unknown"), attrs_value);
    g2d.drawString(astring.getIterator(), valuePosition.x, valuePosition.y);

    currentTop += FONT_SIZE.doubleValue();
    titlePosition.setLocation(left, currentTop);
    valuePosition.setLocation(titleLeft, currentTop);
    astring = new AttributedString("Java version:", attrs_key);
    g2d.drawString(astring.getIterator(), titlePosition.x, titlePosition.y);
    astring = new AttributedString(System.getProperty("java.version", "unknown"), attrs_value);
    g2d.drawString(astring.getIterator(), valuePosition.x, valuePosition.y);

    currentTop += FONT_SIZE.doubleValue();
    titlePosition.setLocation(left, currentTop);
    valuePosition.setLocation(titleLeft, currentTop);
    astring = new AttributedString("Printer name:", attrs_key);
    g2d.drawString(astring.getIterator(), titlePosition.x, titlePosition.y);
    astring = new AttributedString(printerName != null ? printerName : "unknown", attrs_value);
    g2d.drawString(astring.getIterator(), valuePosition.x, valuePosition.y);

    if (trays != null) {
      DecimalFormat idFmt = new DecimalFormat("00");
      for (MediaTray element : trays) {
        currentTop += FONT_SIZE.doubleValue();
        titlePosition.setLocation(left, currentTop);
        valuePosition.setLocation(titleLeft, currentTop);
        astring = new AttributedString(
            element.getClass().getSimpleName() + " " + idFmt.format(element.getValue()) + ":",
            attrs_key);
        g2d.drawString(astring.getIterator(), titlePosition.x, titlePosition.y);
        astring = new AttributedString(element.toString().concat(" "), attrs_value);
        g2d.drawString(astring.getIterator(), valuePosition.x, valuePosition.y);
      }
    }

    currentTop += verticalSpace;
    g2d.setStroke(LINE_STROKE_02);
    line.setLine(0, currentTop, width, currentTop);
    g2d.draw(line);
    currentTop += verticalSpace;

    // print two columns of available fonts
    double fontTop = currentTop;
    double fontLeft = left;
    double maxHeight = height - FONT_SIZE.doubleValue();
    Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    for (Font element : fonts) {
      if (currentTop > maxHeight) {
        if (fontLeft > left) {
          break;
        } else {
          fontLeft += (width / 2);
          currentTop = fontTop;
        }
      }
      currentTop += FONT_SIZE.doubleValue();
      fontPosition.setLocation(fontLeft, currentTop);
      attrs_value.put(TextAttribute.SIZE, FONT_SIZE);
      attrs_value.put(TextAttribute.FAMILY, element.getFamily());
      astring = new AttributedString(element.getName(), attrs_value);
      g2d.drawString(astring.getIterator(), fontPosition.x, fontPosition.y);
    }

    currentTop += FONT_SIZE.doubleValue() + verticalSpace;
    g2d.setStroke(LINE_STROKE_04);

    return pageIndex;
  }

  private String getHostName() {
    String hostName = "unknown";
    try {
      InetAddress address = InetAddress.getLocalHost();
      if (address.getCanonicalHostName().equals(address.getHostAddress())) {
        hostName = address.getHostName() + " (" + address.getHostAddress() + ")";
      } else {
        hostName = address.getCanonicalHostName() + " (" + address.getHostAddress() + ")";
      }
    } catch (Exception e) {
      hostName = e.getMessage();
    }
    return hostName;
  }
}
