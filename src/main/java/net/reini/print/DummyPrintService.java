package net.reini.print;

import java.util.HashSet;
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

class DummyPrintService implements PrintService {
  private static final Class<?>[] supportedAttributeCategories = new Class[0];
  private static final DocFlavor[] emptyDocFlavors = new DocFlavor[0];

  private final String name;
  private final Set<DocFlavor> supportedFlavors;
  private final PrintServiceAttributeSet printServiceAttributeSet;

  public DummyPrintService(String name) {
    this.name = name;
    supportedFlavors = new HashSet<>();
    supportedFlavors.add(DocFlavor.SERVICE_FORMATTED.PAGEABLE);
    supportedFlavors.add(DocFlavor.SERVICE_FORMATTED.PRINTABLE);
    printServiceAttributeSet = new HashPrintServiceAttributeSet();
    printServiceAttributeSet.add(new PrinterName(name, null));
    printServiceAttributeSet.add(PrinterState.IDLE);
  }

  @Override
  public String toString() {
    return "Dummy Printer : " + getName();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public DocPrintJob createPrintJob() {
    return new DummyDocPrintJob(this);
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
