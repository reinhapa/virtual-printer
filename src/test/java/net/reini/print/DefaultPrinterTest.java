/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2023 Patrick Reinhart
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
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
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class DefaultPrinterTest {

  public static void main(String[] args) throws PrintException {
    PrintService printService =
        new VirtualPrintService("MyVirtualPrinter", () -> fail("remove action not supported"));
    DocPrintJob job = printService.createPrintJob();
    Doc doc = new SimpleDoc(new TestPage(printService.getName(), Collections.emptyList()),
        DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
    job.print(doc, null);
  }

  @Test
  @EnabledIfSystemProperty(named = "default", matches = "true")
  void lookupDefaultPrinter() {
    PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
    assertNotNull(defaultPrintService);

    System.out.println("Default printer:");
    System.out.println(defaultPrintService.getName());

    assertEquals("MyVirtualPrinter", defaultPrintService.getName());
  }

  @Test
  void lookupVirtualPrinterByName() {
    AttributeSet attributes = new HashAttributeSet();
    attributes.add(new PrinterName("MyVirtualPrinter", Locale.getDefault()));
    PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, attributes);

    assertEquals(1, printServices.length, Arrays.toString(printServices));
  }

  @Test
  void lookupPrintServices() {
    PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
    assertNotNull(printServices);

    List<String> printerNames =
        Stream.of(printServices).map(PrintService::getName).collect(Collectors.toList());
    System.out.println("Known printers:");
    printerNames.forEach(System.out::println);

    assertTrue(printerNames.contains("MyVirtualPrinter"),
        () -> printerNames + " do not contain 'MyVirtualPrinter'");
  }
}
