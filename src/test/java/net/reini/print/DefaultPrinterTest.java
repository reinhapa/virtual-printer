package net.reini.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Stream;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.standard.PrinterName;

import org.junit.jupiter.api.Test;

public class DefaultPrinterTest {

  public static void main(String[] args) throws PrintException {
    PrintService printService = new DummyPrintService("MyDummyPrinter");
    DocPrintJob job = printService.createPrintJob();
    Doc doc = new SimpleDoc(new TestPage(printService.getName(), Collections.emptyList()),
        DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
    job.print(doc, null);
  }

  @Test
  public void lookupDefaultPrinter() {
    PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
    assertNotNull(defaultPrintService);

    System.out.println("Default printer:");
    System.out.println(defaultPrintService.getName());

    assertEquals("dummyPrinter", defaultPrintService.getName());
  }

  @Test
  public void lookupDummyPrinterByName() {
    AttributeSet attributes = new HashAttributeSet();
    attributes.add(new PrinterName("dummyPrinter", Locale.getDefault()));
    PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, attributes);

    assertEquals(1, printServices.length, Arrays.toString(printServices));
  }

  @Test
  public void lookupPrintServices() {
    PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
    assertNotNull(printServices);

    System.out.println("Known printers:");
    Stream.of(printServices).map(PrintService::getName).forEach(System.out::println);

    assertEquals(1, printServices.length);
    assertEquals("dummyPrinter", printServices[0].getName());
  }
}
