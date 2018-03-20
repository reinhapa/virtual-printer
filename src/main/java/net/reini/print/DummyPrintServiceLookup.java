package net.reini.print;

import java.util.stream.Stream;

import javax.print.DocFlavor;
import javax.print.MultiDocPrintService;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;

public final class DummyPrintServiceLookup extends PrintServiceLookup {
  private final PrintService[] printServices;
  private final MultiDocPrintService[] multiDocPrintServices;

  public DummyPrintServiceLookup() {
    printServices = new PrintService[] {new DummyPrintService("dummyPrinter")};
    multiDocPrintServices = new MultiDocPrintService[0];
  }

  @Override
  public PrintService[] getPrintServices(DocFlavor flavor, AttributeSet attributes) {
    return Stream.of(printServices).filter(ps -> ps.isDocFlavorSupported(flavor))
        .toArray(size -> new PrintService[size]);
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
