package net.reini.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.junit.jupiter.api.Test;

public class DefaultPrinterTest {

  @Test
  public void lookupDefaultPrinter() {
    PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
    assertNotNull(defaultPrintService);
  }

  @Test
  public void lookupPrintServices() {
    PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
    assertNotNull(printServices);
    assertEquals(2, printServices.length);
  }
}
