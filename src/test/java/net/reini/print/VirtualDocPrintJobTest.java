/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2022 Patrick Reinhart
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.io.OutputStream;
import java.util.function.Supplier;

import javax.print.PrintService;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobListener;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings
class VirtualDocPrintJobTest {
  @Mock
  PrintService service;
  @Mock
  Supplier<OutputStream> outputStreamSupplier;
  @Mock
  PrintJobAttributeListener attributeListener;
  @Mock
  PrintJobListener jobListener;

  @InjectMocks
  VirtualDocPrintJob job;

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
}
