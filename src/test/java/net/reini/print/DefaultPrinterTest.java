package net.reini.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.junit.jupiter.api.Test;

public class DefaultPrinterTest {

  @Test
  public void lookupDefaultPrinter() {
    PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
    assertNotNull(defaultPrintService);

    System.out.println("Default printer:");
    System.out.println(defaultPrintService.getName());
  }

  @Test
  public void lookupPrintServices() {
    PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
    assertNotNull(printServices);
    assertEquals(2, printServices.length);

    System.out.println("Known printers:");
    Stream.of(printServices).map(PrintService::getName).forEach(System.out::println);
  }
}
