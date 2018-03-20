package net.reini.print;

import javax.print.Doc;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
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
    System.out.println("job name: " + printRequestAttributeSet.get(JobName.class));
    System.out.println("copies: " + printRequestAttributeSet.get(Copies.class));
  }
}
