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

import static javax.print.event.PrintJobEvent.DATA_TRANSFER_COMPLETE;
import static javax.print.event.PrintJobEvent.JOB_CANCELED;
import static javax.print.event.PrintJobEvent.JOB_COMPLETE;
import static javax.print.event.PrintJobEvent.JOB_FAILED;
import static javax.print.event.PrintJobEvent.NO_MORE_EVENTS;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.StreamPrintService;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSetUtilities;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.DocumentName;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.JobOriginatingUserName;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

class VirtualDocPrintJob implements DocPrintJob {
  private final PrintService service;
  private final Supplier<OutputStream> outputStreamSupplier;

  private boolean printing;
  private PrintJobAttributeSet jobAttrSet;
  private PrintRequestAttributeSet reqAttrSet;
  private List<PrintJobListener> jobListeners;
  private List<PrintJobAttributeListener> attrListeners;
  private List<PrintJobAttributeSet> listenedAttributeSets;

  VirtualDocPrintJob(PrintService service, Supplier<OutputStream> outputStreamSupplier) {
    this.service = service;
    this.outputStreamSupplier = outputStreamSupplier;
  }

  private void notifyEvent(int reason) {
    synchronized (this) {
      if (jobListeners != null) {
        PrintJobEvent event = new PrintJobEvent(this, reason);
        for (PrintJobListener listener : jobListeners) {
          switch (reason) {
            case JOB_CANCELED:
              listener.printJobCanceled(event);
              break;
            case JOB_FAILED:
              listener.printJobFailed(event);
              break;
            case DATA_TRANSFER_COMPLETE:
              listener.printDataTransferCompleted(event);
              break;
            case NO_MORE_EVENTS:
              listener.printJobNoMoreEvents(event);
              break;
            case JOB_COMPLETE:
              listener.printJobCompleted(event);
              break;
            default:
              break;
          }
        }
      }
    }
  }

  /*
   * There's some inefficiency here as the job set is created even though it may never be requested.
   */
  private synchronized void initializeAttributeSets(Doc doc, PrintRequestAttributeSet reqSet) {
    reqAttrSet = new HashPrintRequestAttributeSet();
    if (reqSet != null) {
      reqAttrSet.addAll(reqSet);
      for (Attribute attribute : reqSet.toArray()) {
        if (attribute instanceof PrintJobAttribute) {
          jobAttrSet.add(attribute);
        }
      }
    }
    jobAttrSet = new HashPrintJobAttributeSet();
    DocAttributeSet docSet = doc.getAttributes();
    if (docSet != null) {
      for (Attribute attribute : docSet.toArray()) {
        if (attribute instanceof PrintJobAttribute) {
          jobAttrSet.add(attribute);
        }
        if (attribute instanceof PrintRequestAttribute) {
          reqAttrSet.add(attribute);
        }
      }
    }
    // add the user name to the job
    String userName = "";
    try {
      userName = System.getProperty("user.name");
    } catch (SecurityException se) {
    }
    if (userName == null || userName.equals("")) {
      RequestingUserName ruName = (RequestingUserName)reqSet.get(RequestingUserName.class);
      if (ruName != null) {
        jobAttrSet.add(new JobOriginatingUserName(ruName.getValue(), ruName.getLocale()));
      } else {
        jobAttrSet.add(new JobOriginatingUserName("", null));
      }
    } else {
      jobAttrSet.add(new JobOriginatingUserName(userName, null));
    }
    // if no job name supplied use doc name (if supplied), if none and its a URL use that, else finally anything ..
    if (jobAttrSet.get(JobName.class) == null) {
      JobName jobName;
      if (docSet != null && docSet.get(DocumentName.class) != null) {
        DocumentName docName = (DocumentName)docSet.get(DocumentName.class);
        jobName = new JobName(docName.getValue(), docName.getLocale());
        jobAttrSet.add(jobName);
      } else {
        String str = "VPS Job:" + doc;
        try {
          Object printData = doc.getPrintData();
          if (printData instanceof URL) {
            str = ((URL)(doc.getPrintData())).toString();
          }
        } catch (IOException e) {
        }
        jobName = new JobName(str, null);
        jobAttrSet.add(jobName);
      }
    }
    jobAttrSet = AttributeSetUtilities.unmodifiableView(jobAttrSet);
  }

  @Override
  public PrintService getPrintService() {
    return service;
  }

  @Override
  public PrintJobAttributeSet getAttributes() {
    synchronized (this) {
      if (jobAttrSet == null) {
        /* just return an empty set until the job is submitted */
        PrintJobAttributeSet jobSet = new HashPrintJobAttributeSet();
        return AttributeSetUtilities.unmodifiableView(jobSet);
      } else {
        return jobAttrSet;
      }
    }
  }

  @Override
  public void addPrintJobAttributeListener(PrintJobAttributeListener listener,
      PrintJobAttributeSet attributes) {
    synchronized (this) {
      if (listener == null) {
        return;
      }
      if (attrListeners == null) {
        attrListeners = new ArrayList<>();
        listenedAttributeSets = new ArrayList<>();
      }
      attrListeners.add(listener);
      if (attributes == null) {
        attributes = new HashPrintJobAttributeSet();
      }
      listenedAttributeSets.add(attributes);
    }
  }

  @Override
  public void removePrintJobAttributeListener(PrintJobAttributeListener listener) {
    synchronized (this) {
      if (listener == null || attrListeners == null) {
        return;
      }
      int index = attrListeners.indexOf(listener);
      if (index == -1) {
        return;
      } else {
        attrListeners.remove(index);
        listenedAttributeSets.remove(index);
        if (attrListeners.isEmpty()) {
          attrListeners = null;
          listenedAttributeSets = null;
        }
      }
    }
  }

  @Override
  public void addPrintJobListener(PrintJobListener listener) {
    synchronized (this) {
      if (listener == null) {
          return;
      }
      if (jobListeners == null) {
        jobListeners = new ArrayList<>();
      }
      jobListeners.add(listener);
    }
  }

  @Override
  public void removePrintJobListener(PrintJobListener listener) {
    synchronized (this) {
      if (listener == null || jobListeners == null) {
        return;
      }
      jobListeners.remove(listener);
      if (jobListeners.isEmpty()) {
        jobListeners = null;
      }
    }
  }

  @Override
  public void print(Doc doc, PrintRequestAttributeSet attributes)
      throws PrintException {
    synchronized (this) {
      if (printing) {
        throw new PrintException("already printing");
      } else {
        printing = true;
      }
    }
    initializeAttributeSets(doc, attributes);
    final DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
    final String psMimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();
    final StreamPrintServiceFactory[] factories = StreamPrintServiceFactory.lookupStreamPrintServiceFactories(flavor, psMimeType);
    if (factories.length == 0) {
      System.err.println("No suitable factories");
      notifyEvent(JOB_FAILED);
    } else {
      try (OutputStream fos = outputStreamSupplier.get()) {
        StreamPrintService sps = factories[0].getPrintService(fos);
        DocPrintJob pj = sps.createPrintJob();
        pj.print(doc, reqAttrSet);
        notifyEvent(JOB_COMPLETE);
      } catch (IOException e) {
        e.printStackTrace();
        notifyEvent(JOB_FAILED);
      }
    }
  }
}
