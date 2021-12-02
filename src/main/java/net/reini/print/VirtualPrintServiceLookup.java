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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.DocFlavor;
import javax.print.MultiDocPrintService;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.standard.PrinterName;

public final class VirtualPrintServiceLookup extends PrintServiceLookup {
  private static final Logger LOG = Logger.getLogger(VirtualPrintServiceLookup.class.getName());

  private final List<PrintService> printServices;
  private final List<MultiDocPrintService> multiDocPrintServices;

  /**
   * Initializes the virtual print service lookup
   */
  public VirtualPrintServiceLookup() {
    printServices = new ArrayList<>();
    multiDocPrintServices = new ArrayList<>();
    initiallizePrinters();
  }

  private void initiallizePrinters() {
    try {
      Enumeration<URL> resources = getClass().getClassLoader().getResources("virtual-printer-names");
      while (resources.hasMoreElements()) {
        try (InputStream in = resources.nextElement().openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
          br.lines() //
              .map(String::trim) //
              .filter(l -> !l.isEmpty()) //
              .filter(l -> !l.startsWith("#")) //
              .forEach(this::addPrinter);
        }
      }
      // initialize default virtual printer if no other have been defined
      if (printServices.isEmpty()) {
        addPrinter("VirtualPrinter");
      }
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Failed to initialize printers", e);
    }
  }

  private void addPrinter(String printerName) {
    if (!printServices.stream().map(PrintService::getName).filter(printerName::equals).findFirst().isPresent()) {
      LOG.log(Level.INFO, () -> "Adding printer: " + printerName);
      printServices.add(new VirtualPrintService(printerName));
    }
  }

  private boolean serviceMatches(PrintService ps, AttributeSet attributes) {
    if (attributes == null) {
      return true;
    }
    PrinterName attribute = (PrinterName) attributes.get(PrinterName.class);
    if (attribute == null) {
      return false;
    }
    return ps.getName().equals(attribute.getValue());
  }

  private boolean flavorMatches(PrintService ps, DocFlavor[] flavors) {
    if (flavors == null) {
      return true;
    }
    for (DocFlavor flavor : flavors) {
      if (!ps.isDocFlavorSupported(flavor)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public PrintService[] getPrintServices(DocFlavor flavor, AttributeSet attributes) {
    return printServices.stream() //
        .filter(ps -> flavor == null || ps.isDocFlavorSupported(flavor)) //
        .filter(ps -> serviceMatches(ps, attributes)) //
        .toArray(size -> new PrintService[size]);
  }

  @Override
  public PrintService[] getPrintServices() {
    return printServices.toArray(size -> new PrintService[size]);
  }

  @Override
  public MultiDocPrintService[] getMultiDocPrintServices(DocFlavor[] flavors,
      AttributeSet attributes) {
    return multiDocPrintServices.stream() //
        .filter(ps -> flavorMatches(ps, flavors)) //
        .filter(ps -> serviceMatches(ps, attributes)) //
        .toArray(size -> new MultiDocPrintService[size]);
  }

  @Override
  public PrintService getDefaultPrintService() {
    if (printServices.isEmpty()) {
      return null;
    }
    return printServices.get(0);
  }

}
