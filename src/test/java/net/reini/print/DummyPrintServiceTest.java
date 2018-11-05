package net.reini.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.PrinterState;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DummyPrintServiceTest {
  private DummyPrintService printerService;

  @BeforeEach
  public void setUp() {
    printerService = new DummyPrintService("PrinterName");
  }

  @Test
  public void testGetName() {
    assertEquals("PrinterName", printerService.getName());
  }

  @Test
  public void testCreatePrintJob() {
    DocPrintJob printerjob = printerService.createPrintJob();
    assertNotNull(printerjob);
  }

  @Test
  public void testIsDocFlavorSupported() {
    assertTrue(printerService.isDocFlavorSupported(DocFlavor.SERVICE_FORMATTED.PAGEABLE));
    assertTrue(printerService.isDocFlavorSupported(DocFlavor.SERVICE_FORMATTED.PRINTABLE));
  }

  @Test
  public void testGetAttribute() {
    PrinterName printerName = (PrinterName) printerService.getAttribute(PrinterName.class);
    assertNotNull(printerName);
    assertEquals("PrinterName", printerName.getValue());

    PrinterState printerState = (PrinterState) printerService.getAttribute(PrinterState.class);
    assertEquals(PrinterState.IDLE, printerState);
  }

  @Test
  public void testGetAttributes() {
    PrintServiceAttributeSet attributes = printerService.getAttributes();

    assertTrue(attributes.containsValue(new PrinterName("PrinterName", null)));
    assertTrue(attributes.containsValue(PrinterState.IDLE));
  }

  @Test
  public void testGetSupportedDocFlavors() {
    List<DocFlavor> docFlavors = Arrays.asList(printerService.getSupportedDocFlavors());

    assertTrue(docFlavors.contains(DocFlavor.SERVICE_FORMATTED.PAGEABLE));
    assertTrue(docFlavors.contains(DocFlavor.SERVICE_FORMATTED.PRINTABLE));
  }

  @Test
  public void testGetSupportedAttributeCategories() {
    Class<?>[] categories = printerService.getSupportedAttributeCategories();
    assertNotNull(categories);
    assertEquals(0, categories.length);
  }

}
