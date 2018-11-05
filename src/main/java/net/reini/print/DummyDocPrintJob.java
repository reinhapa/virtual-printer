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

class DummyDocPrintJob implements DocPrintJob {
  private final PrintService printService;
  private final PrintJobAttributeSet printJobAttributeSet;

  public DummyDocPrintJob(PrintService printService) {
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
      PrintJobAttributeSet printJobAttributeSet) {}

  @Override
  public void removePrintJobAttributeListener(PrintJobAttributeListener listener) {}

  @Override
  public void addPrintJobListener(PrintJobListener listener) {}

  @Override
  public void removePrintJobListener(PrintJobListener listener) {}

  @Override
  public void print(Doc doc, PrintRequestAttributeSet printRequestAttributeSet)
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
        System.out.println("job name: " + printRequestAttributeSet.get(JobName.class));
        System.out.println("copies: " + printRequestAttributeSet.get(Copies.class));
        pj.print(doc, printRequestAttributeSet);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
