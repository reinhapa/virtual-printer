/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2026 Patrick Reinhart
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

import static javax.print.DocFlavor.SERVICE_FORMATTED.PAGEABLE;
import static javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE;
import static javax.print.DocFlavor.STRING.TEXT_PLAIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.print.CancelablePrintJob;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.PrinterMoreInfoManufacturer;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.PrinterState;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings
class VirtualPrintServiceTest {
  @Mock
  Runnable removeAction;
  VirtualPrintService printerService;

  @BeforeEach
  void setUp() {
    printerService = new VirtualPrintService("PrinterName", removeAction);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(removeAction);
  }

  @Test
  void getNameAndToString() {
    assertThat(printerService.getName()).isEqualTo("PrinterName");
    assertThat(printerService).hasToString("Virtual Printer : PrinterName");
  }

  @Test
  void createPrintJob() {
    assertThat(printerService.getRunning()).isZero();
    assertThat(printerService.getPrinterState()).isEqualTo("idle");

    assertThat(printerService.createPrintJob()).isNotNull();
    assertThat(printerService.getRunning()).isEqualTo(1);
    assertThat(printerService.getPrinterState()).isEqualTo("processing");
    assertThat(printerService.createPrintJob()).isNotNull();
    assertThat(printerService.getRunning()).isEqualTo(2);
    assertThat(printerService.getPrinterState()).isEqualTo("processing");
  }

  @Test
  void isDocFlavorSupported() {
    assertThat(printerService.isDocFlavorSupported(PAGEABLE)).isTrue();
    assertThat(printerService.isDocFlavorSupported(PRINTABLE)).isTrue();
  }

  @Test
  void getAttribute() {
    PrinterName printerName = printerService.getAttribute(PrinterName.class);
    assertThat(printerName).isNotNull().extracting(PrinterName::getValue).isEqualTo("PrinterName");

    PrinterState printerState = printerService.getAttribute(PrinterState.class);
    assertThat(printerState).isEqualTo(PrinterState.IDLE);

    PrinterMoreInfoManufacturer manufacturer =
        printerService.getAttribute(PrinterMoreInfoManufacturer.class);
    assertThat(manufacturer).isNull();
  }

  @Test
  void getAttributes() {
    PrintServiceAttributeSet attributes = printerService.getAttributes();

    assertThat(attributes.containsValue(new PrinterName("PrinterName", null))).isTrue();
    assertThat(attributes.containsValue(PrinterState.IDLE)).isTrue();
  }

  @Test
  void getSupportedDocFlavors() {
    List<DocFlavor> docFlavors = Arrays.asList(printerService.getSupportedDocFlavors());

    assertThat(docFlavors).contains(PAGEABLE, PRINTABLE);
  }

  @Test
  void getServiceUIFactory() {
    assertThat(printerService.getServiceUIFactory()).isNull();
  }

  @Test
  void getDefaultAttributeValue() {
    assertThat(printerService.getDefaultAttributeValue(null)).isNull();
  }

  @Test
  void getSupportedAttributeValues() {
    assertThat(printerService.getSupportedAttributeValues(null, null, null)).isNull();
  }

  @Test
  void getUnsupportedAttributes() {
    assertThat(printerService.getUnsupportedAttributes(null, null)).isNull();
  }

  @Test
  void isAttributeCategorySupported() {
    assertThat(printerService.isAttributeCategorySupported(null)).isFalse();
  }

  @Test
  void isAttributeValueSupported() {
    assertThat(printerService.isAttributeValueSupported(null, null, null)).isFalse();
  }

  @Test
  void getSupportedAttributeCategories() {
    Class<?>[] categories = printerService.getSupportedAttributeCategories();
    assertThat(categories).isNotNull().isEmpty();
  }

  @Test
  void activateAndSuspend() {
    assertThat(printerService.getAttribute(PrinterIsAcceptingJobs.class))
        .isEqualTo(PrinterIsAcceptingJobs.ACCEPTING_JOBS);
    printerService.suspend();
    assertThat(printerService.getAttribute(PrinterIsAcceptingJobs.class))
        .isEqualTo(PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS);
    printerService.activate();
    assertThat(printerService.getAttribute(PrinterIsAcceptingJobs.class))
        .isEqualTo(PrinterIsAcceptingJobs.ACCEPTING_JOBS);
  }

  @Test
  void addPrintServiceAttributeListener() {
    assertThatNoException().isThrownBy(() -> printerService.addPrintServiceAttributeListener(null));
  }

  @Test
  void removePrintServiceAttributeListener() {
    assertThatNoException()
        .isThrownBy(() -> printerService.removePrintServiceAttributeListener(null));
  }

  @Test
  void getCanceled() {
    assertThat(printerService.getCanceled()).isZero();
    assertThat(printerService.getRunning()).isZero();
    DocPrintJob printerjob = printerService.createPrintJob();
    assertThat(printerService.getRunning()).isEqualTo(1);
    assertThat(printerjob).isInstanceOf(CancelablePrintJob.class);
    assertThatNoException().isThrownBy(() -> ((CancelablePrintJob) printerjob).cancel());
    assertThat(printerService.getCanceled()).isEqualTo(1);
    assertThat(printerService.getRunning()).isZero();
    assertResetStatistics();
  }

  @ParameterizedTest
  @MethodSource("getCompletedParams")
  void getCompleted(Doc doc) {
    PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();

    assertThat(printerService.getCompleted()).isZero();
    assertThat(printerService.getRunning()).isZero();
    DocPrintJob printerjob = printerService.createPrintJob();
    assertThat(printerService.getRunning()).isEqualTo(1);
    assertThatNoException().isThrownBy(() -> printerjob.print(doc, attributes));
    assertThat(printerService.getCompleted()).isEqualTo(1);
    assertThat(printerService.getRunning()).isZero();
    assertResetStatistics();
  }

  static Stream<Doc> getCompletedParams() {
    return Stream.of(new SimpleDoc(new TestPage(null, null), PRINTABLE, null),
        new SimpleDoc("text only", TEXT_PLAIN, null));
  }

  @Test
  void getFailed() throws PrinterException {
    Pageable pageable = mock("pageable");
    Printable printable = mock("printable");
    Doc doc = new SimpleDoc(pageable, PAGEABLE, null);
    PageFormat pageFormat = new PageFormat();
    PrinterException printerException = new PrinterException();
    PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();

    when(pageable.getNumberOfPages()).thenReturn(1);
    when(pageable.getPageFormat(0)).thenReturn(pageFormat);
    when(pageable.getPrintable(0)).thenReturn(printable);
    when(printable.print(any(), eq(pageFormat), eq(0))).thenThrow(printerException);

    assertThat(printerService.getFailed()).isZero();
    assertThat(printerService.getRunning()).isZero();
    DocPrintJob printerjob = printerService.createPrintJob();
    assertThat(printerService.getRunning()).isEqualTo(1);
    assertThatExceptionOfType(PrintException.class)
        .isThrownBy(() -> printerjob.print(doc, attributes));
    assertThat(printerService.getFailed()).isEqualTo(1);
    assertThat(printerService.getRunning()).isZero();
    assertResetStatistics();
    verifyNoMoreInteractions(pageable, printable);
  }

  @Test
  void remove() {
    assertThatNoException().isThrownBy(printerService::remove);
    verify(removeAction).run();
  }

  void assertResetStatistics() {
    assertThatNoException().isThrownBy(printerService::resetStatistics);
    assertThat(printerService.getFailed()).isZero();
    assertThat(printerService.getCompleted()).isZero();
    assertThat(printerService.getCanceled()).isZero();
  }
}
