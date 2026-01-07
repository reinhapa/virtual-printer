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

import static java.awt.print.Printable.NO_SUCH_PAGE;
import static java.awt.print.Printable.PAGE_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Supplier;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings
class VirtualDocPrintJobTest {
  @Mock(name = "service")
  PrintService service;
  @Mock(name = "outputStreamSupplier")
  Supplier<OutputStream> outputStreamSupplier;
  @Mock(name = "attributeListener")
  PrintJobAttributeListener attributeListener;
  @Mock(name = "jobListener")
  PrintJobListener jobListener;
  @Mock(name = "doc")
  Doc doc;
  @Mock(name = "printable")
  Printable printable;

  @InjectMocks
  VirtualDocPrintJob job;

  @AfterEach
  void verifyMocks() {
    verifyNoMoreInteractions(service, outputStreamSupplier, attributeListener, jobListener, doc,
        printable);
  }

  @Test
  void testGetPrintService() {
    assertThat(job.getPrintService()).isSameAs(service);
  }

  @Test
  void testGetAttributes() {
    assertThat(job.getAttributes()).isNotNull();
  }

  @Test
  void testPrintJobAttributeListener() {
    PrintJobAttributeSet attributes = new HashPrintJobAttributeSet();
    assertThatNoException().isThrownBy(() -> job.addPrintJobAttributeListener(null, null));
    assertThatNoException().isThrownBy(() -> job.removePrintJobAttributeListener(null));
    assertThatNoException()
        .isThrownBy(() -> job.removePrintJobAttributeListener(attributeListener));
    assertThatNoException()
        .isThrownBy(() -> job.addPrintJobAttributeListener(attributeListener, null));
    assertThatNoException()
        .isThrownBy(() -> job.addPrintJobAttributeListener(attributeListener, attributes));
    assertThatNoException()
        .isThrownBy(() -> job.removePrintJobAttributeListener(attributeListener));
    assertThatNoException()
        .isThrownBy(() -> job.removePrintJobAttributeListener(attributeListener));
  }

  @Test
  void testPrintJobListener() {
    assertThatNoException().isThrownBy(() -> job.addPrintJobListener(null));
    assertThatNoException().isThrownBy(() -> job.removePrintJobListener(null));
    assertThatNoException().isThrownBy(() -> job.removePrintJobListener(jobListener));
    assertThatNoException().isThrownBy(() -> job.addPrintJobListener(jobListener));
    assertThatNoException().isThrownBy(() -> job.addPrintJobListener(jobListener));
    assertThatNoException().isThrownBy(() -> job.removePrintJobListener(jobListener));
    assertThatNoException().isThrownBy(() -> job.removePrintJobListener(jobListener));
  }

  @Test
  void testCancel() {
    assertThatNoException().isThrownBy(job::cancel);
    assertThatNoException().isThrownBy(job::cancel);
  }

  @Test
  void testPrint() throws IOException, PrinterException {
    PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();

    when(outputStreamSupplier.get()).thenReturn(OutputStream.nullOutputStream());
    when(doc.getDocFlavor()).thenReturn(DocFlavor.SERVICE_FORMATTED.PRINTABLE);
    when(doc.getPrintData()).thenReturn(printable);
    when(printable.print(any(), any(), eq(0))).thenReturn(PAGE_EXISTS);
    when(printable.print(any(), any(), eq(1))).thenReturn(NO_SUCH_PAGE);

    job.addPrintJobListener(jobListener);
    assertThatNoException().isThrownBy(() -> job.print(doc, attributes));

    verify(doc, times(2)).getAttributes();
    verify(doc, times(2)).getDocFlavor();
    verify(jobListener).printJobCompleted(isA(PrintJobEvent.class));
    verify(jobListener).printJobNoMoreEvents(isA(PrintJobEvent.class));
  }

  @Test
  void testPrintWithError() throws IOException, PrinterException {
    PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
    PageFormat pagetFormat = new PageFormat();

    Pageable pageable = mock("pageable");

    when(outputStreamSupplier.get()).thenReturn(OutputStream.nullOutputStream());
    when(doc.getDocFlavor()).thenReturn(DocFlavor.SERVICE_FORMATTED.PAGEABLE);
    when(doc.getPrintData()).thenReturn(pageable);
    when(pageable.getNumberOfPages()).thenReturn(1);
    when(pageable.getPageFormat(0)).thenReturn(pagetFormat);
    when(pageable.getPrintable(0)).thenReturn(printable);
    when(printable.print(any(), any(), eq(0))).thenReturn(PAGE_EXISTS);

    job.addPrintJobListener(jobListener);
    assertThatNoException().isThrownBy(() -> job.print(doc, attributes));

    verify(doc, times(2)).getAttributes();
    verify(jobListener).printJobCompleted(isA(PrintJobEvent.class));
    // verify(jobListener).printJobFailed(isA(PrintJobEvent.class));
    verify(jobListener).printJobNoMoreEvents(isA(PrintJobEvent.class));
    verifyNoMoreInteractions(pageable);
  }
}
