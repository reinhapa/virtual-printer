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

import java.io.FileOutputStream;
import java.io.IOException;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.StreamPrintService;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobListener;

class VirtualDocPrintJob implements DocPrintJob {
  private final PrintService printService;
  private final PrintJobAttributeSet printJobAttributeSet;

  public VirtualDocPrintJob(PrintService printService) {
    this.printService = printService;
    printJobAttributeSet = new HashPrintJobAttributeSet();
  }

  @Override
  public PrintService getPrintService() {
    return printService;
  }

  @Override
  public PrintJobAttributeSet getAttributes() {
    return printJobAttributeSet;
  }

  @Override
  public void addPrintJobAttributeListener(PrintJobAttributeListener listener,
      PrintJobAttributeSet attributeSet) {
  }

  @Override
  public void removePrintJobAttributeListener(PrintJobAttributeListener listener) {}

  @Override
  public void addPrintJobListener(PrintJobListener listener) {}

  @Override
  public void removePrintJobListener(PrintJobListener listener) {}

  @Override
  public void print(Doc doc, PrintRequestAttributeSet attributes)
      throws PrintException {
    DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
    String psMimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();
    StreamPrintServiceFactory[] factories =
        StreamPrintServiceFactory.lookupStreamPrintServiceFactories(flavor, psMimeType);
    if (factories.length == 0) {
      System.err.println("No suitable factories");
    } else {
      try (FileOutputStream fos = new FileOutputStream("out.ps")) {
        StreamPrintService sps = factories[0].getPrintService(fos);
        DocPrintJob pj = sps.createPrintJob();
        System.out.println("job name: " + attributes.get(JobName.class));
        System.out.println("copies: " + attributes.get(Copies.class));
        pj.print(doc, attributes);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
