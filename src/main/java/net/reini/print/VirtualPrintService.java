/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, 2019 Patrick Reinhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.reini.print;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.PrinterState;
import javax.print.event.PrintServiceAttributeListener;

class VirtualPrintService implements PrintService {
  private static final Class<?>[] supportedAttributeCategories = new Class[0];
  private static final DocFlavor[] emptyDocFlavors = new DocFlavor[0];

  private final String name;
  private final Set<DocFlavor> supportedFlavors;
  private final PrintServiceAttributeSet printServiceAttributeSet;

  public VirtualPrintService(String name) {
    this.name = name;
    supportedFlavors = new HashSet<>();
    supportedFlavors.add(DocFlavor.SERVICE_FORMATTED.PAGEABLE);
    supportedFlavors.add(DocFlavor.SERVICE_FORMATTED.PRINTABLE);
    printServiceAttributeSet = new HashPrintServiceAttributeSet();
    printServiceAttributeSet.add(new PrinterName(name, null));
    printServiceAttributeSet.add(new PrinterName(name, Locale.getDefault()));
    printServiceAttributeSet.add(PrinterState.IDLE);
  }

  @Override
  public String toString() {
    return "Virtual Printer : " + getName();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public DocPrintJob createPrintJob() {
    return new VirtualDocPrintJob(this, ByteArrayOutputStream::new);
  }

  @Override
  public boolean isDocFlavorSupported(DocFlavor flavor) {
    return supportedFlavors.contains(flavor);
  }

  @Override
  public <T extends PrintServiceAttribute> T getAttribute(Class<T> category) {
    return category.cast(printServiceAttributeSet.get(category));
  }

  @Override
  public PrintServiceAttributeSet getAttributes() {
    return printServiceAttributeSet;
  }

  @Override
  public DocFlavor[] getSupportedDocFlavors() {
    return supportedFlavors.toArray(emptyDocFlavors);
  }

  @Override
  public Object getDefaultAttributeValue(Class<? extends Attribute> category) {
    return null;
  }

  @Override
  public ServiceUIFactory getServiceUIFactory() {
    return null;
  }

  @Override
  public Class<?>[] getSupportedAttributeCategories() {
    return supportedAttributeCategories;
  }

  @Override
  public Object getSupportedAttributeValues(Class<? extends Attribute> category, DocFlavor flavor,
      AttributeSet attributes) {
    return null;
  }

  @Override
  public AttributeSet getUnsupportedAttributes(DocFlavor flavor, AttributeSet attributes) {
    return null;
  }

  @Override
  public boolean isAttributeCategorySupported(Class<? extends Attribute> category) {
    return false;
  }

  @Override
  public boolean isAttributeValueSupported(Attribute attrval, DocFlavor flavor,
      AttributeSet attributes) {
    return false;
  }

  @Override
  public void addPrintServiceAttributeListener(PrintServiceAttributeListener listener) {}

  @Override
  public void removePrintServiceAttributeListener(PrintServiceAttributeListener listener) {}
}
