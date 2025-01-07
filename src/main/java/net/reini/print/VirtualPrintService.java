/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2025 Patrick Reinhart
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

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.AttributeSetUtilities;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.PrinterState;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintServiceAttributeListener;

class VirtualPrintService implements PrintService, VirtualPrintServiceMXBean {
  private static final Class<?>[] supportedAttributeCategories = new Class<?>[0];
  private static final DocFlavor[] emptyDocFlavors = new DocFlavor[0];

  private final String name;
  private final Runnable removeAction;
  private final Set<DocFlavor> supportedFlavors;
  private final StatisticsListener statisticsListener;
  private final PrintServiceAttributeSet printServiceAttributeSet;

  private PrinterIsAcceptingJobs acceptingJobs;

  static class StatisticsListener extends PrintJobAdapter {
    final LongAdder canceled = new LongAdder();
    final LongAdder completed = new LongAdder();
    final LongAdder failed = new LongAdder();
    final AtomicInteger running = new AtomicInteger();

    private PrinterState defaultState = PrinterState.IDLE;

    DocPrintJob startJob(VirtualDocPrintJob virtualDocPrintJob) {
      virtualDocPrintJob.addPrintJobListener(this);
      running.incrementAndGet();
      return virtualDocPrintJob;
    }

    void reset() {
      completed.reset();
      canceled.reset();
      failed.reset();
    }

    PrinterState printerState() {
      if (running.get() > 0) {
        return PrinterState.PROCESSING;
      }
      return defaultState;
    }

    @Override
    public void printJobCanceled(PrintJobEvent pje) {
      canceled.increment();
    }

    @Override
    public void printJobCompleted(PrintJobEvent pje) {
      completed.increment();
    }

    @Override
    public void printJobFailed(PrintJobEvent pje) {
      failed.increment();
    }

    @Override
    public void printJobNoMoreEvents(PrintJobEvent pje) {
      running.decrementAndGet();
    }
  }

  public VirtualPrintService(String name, Runnable removeAction) {
    this.name = name;
    this.removeAction = removeAction;
    supportedFlavors = new HashSet<>();
    supportedFlavors.add(DocFlavor.SERVICE_FORMATTED.PAGEABLE);
    supportedFlavors.add(DocFlavor.SERVICE_FORMATTED.PRINTABLE);
    printServiceAttributeSet = new HashPrintServiceAttributeSet();
    statisticsListener = new StatisticsListener();
    activate();
  }

  @Override
  public long getCanceled() {
    return statisticsListener.canceled.longValue();
  }

  @Override
  public long getCompleted() {
    return statisticsListener.completed.longValue();
  }

  @Override
  public long getFailed() {
    return statisticsListener.failed.longValue();
  }

  @Override
  public int getRunning() {
    return statisticsListener.running.get();
  }

  @Override
  public void activate() {
    acceptingJobs = PrinterIsAcceptingJobs.ACCEPTING_JOBS;
  }

  @Override
  public void suspend() {
    acceptingJobs = PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS;
  }

  @Override
  public void remove() {
    removeAction.run();
  }

  @Override
  public void resetStatistics() {
    statisticsListener.reset();
  }

  @Override
  public String getPrinterState() {
    return statisticsListener.printerState().toString();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public DocPrintJob createPrintJob() {
    return statisticsListener.startJob(new VirtualDocPrintJob(this, ByteArrayOutputStream::new));
  }

  @Override
  public boolean isDocFlavorSupported(DocFlavor flavor) {
    return supportedFlavors.contains(flavor);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends PrintServiceAttribute> T getAttribute(Class<T> category) {
    final T result;
    if (category == PrinterName.class) {
      result = (T) new PrinterName(name, null);
    } else if (category == PrinterIsAcceptingJobs.class) {
      result = (T) acceptingJobs;
    } else if (category == PrinterState.class) {
      result = (T) statisticsListener.printerState();
    } else {
      result = category.cast(printServiceAttributeSet.get(category));
    }
    return result;
  }

  @Override
  public PrintServiceAttributeSet getAttributes() {
    PrintServiceAttributeSet attrs = new HashPrintServiceAttributeSet();
    attrs.addAll(printServiceAttributeSet);
    attrs.add(getAttribute(PrinterName.class));
    attrs.add(getAttribute(PrinterState.class));
    attrs.add(getAttribute(PrinterIsAcceptingJobs.class));
    return AttributeSetUtilities.unmodifiableView(attrs);
  }

  @Override
  public DocFlavor[] getSupportedDocFlavors() {
    return supportedFlavors.toArray(emptyDocFlavors);
  }

  @Override
  public Object getDefaultAttributeValue(Class<? extends Attribute> category) {
    return null;
  }

  @Override
  public ServiceUIFactory getServiceUIFactory() {
    return null;
  }

  @Override
  public Class<?>[] getSupportedAttributeCategories() {
    return supportedAttributeCategories;
  }

  @Override
  public Object getSupportedAttributeValues(Class<? extends Attribute> category, DocFlavor flavor,
      AttributeSet attributes) {
    return null;
  }

  @Override
  public AttributeSet getUnsupportedAttributes(DocFlavor flavor, AttributeSet attributes) {
    return null;
  }

  @Override
  public boolean isAttributeCategorySupported(Class<? extends Attribute> category) {
    return false;
  }

  @Override
  public boolean isAttributeValueSupported(Attribute attrval, DocFlavor flavor,
      AttributeSet attributes) {
    return false;
  }

  @Override
  public void addPrintServiceAttributeListener(PrintServiceAttributeListener listener) {
    // no action
  }

  @Override
  public void removePrintServiceAttributeListener(PrintServiceAttributeListener listener) {
    // no action
  }

  @Override
  public String toString() {
    return "Virtual Printer : " + name;
  }
}
