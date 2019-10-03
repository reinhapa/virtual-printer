/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, 2019 Patrick Reinhart
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

import java.util.stream.Stream;

import javax.print.DocFlavor;
import javax.print.MultiDocPrintService;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.standard.PrinterName;

public final class DummyPrintServiceLookup extends PrintServiceLookup {
  private final PrintService[] printServices;
  private final MultiDocPrintService[] multiDocPrintServices;

  public DummyPrintServiceLookup() {
    printServices = new PrintService[] {new DummyPrintService("MyDummyPrinter")};
    multiDocPrintServices = new MultiDocPrintService[0];
  }

  @Override
  public PrintService[] getPrintServices(DocFlavor flavor, AttributeSet attributes) {
    return Stream.of(printServices) //
        .filter(ps -> flavor == null || ps.isDocFlavorSupported(flavor)) //
        .filter(ps -> attributes == null || serviceMatches(ps, attributes))
        .toArray(size -> new PrintService[size]);
  }

  private boolean serviceMatches(PrintService ps, AttributeSet attributes) {
    PrinterName attribute = (PrinterName) attributes.get(PrinterName.class);
    if (attribute == null) {
      return false;
    }
    return ps.getName().equals(attribute.getValue());
  }

  @Override
  public PrintService[] getPrintServices() {
    return printServices;
  }

  @Override
  public MultiDocPrintService[] getMultiDocPrintServices(DocFlavor[] flavors,
      AttributeSet attributes) {
    return multiDocPrintServices;
  }

  @Override
  public PrintService getDefaultPrintService() {
    return printServices[0];
  }

}
