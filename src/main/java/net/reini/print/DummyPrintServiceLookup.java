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
