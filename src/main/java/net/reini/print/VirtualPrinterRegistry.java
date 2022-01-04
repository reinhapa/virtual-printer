/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2022 Patrick Reinhart
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

import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.print.MultiDocPrintService;
import javax.print.PrintService;

final class VirtualPrinterRegistry implements VirtualPrinterRegistryMXBean {
  private static final Logger LOG = Logger.getLogger(VirtualPrinterRegistry.class.getName());

  private final MBeanServer mbeanServer;
  private final List<PrintService> printServices;
  private final List<MultiDocPrintService> multiDocPrintServices;

  private String defaultPrinterName;

  VirtualPrinterRegistry(MBeanServer mbeanServer) {
    this.mbeanServer = mbeanServer;
    printServices = new ArrayList<>();
    multiDocPrintServices = new ArrayList<>();
    registerInJmx();
    initiallizePrinters();
  }

  private void registerInJmx() {
    try {
      mbeanServer.registerMBean(this,
          ObjectName.getInstance("net.reini", "type", "VirtualPrinters"));
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unable to register management bean", e);
    }
  }

  private VirtualPrintService registerInJmx(VirtualPrintService virtualPrintService) {
    try {
      mbeanServer.registerMBean(virtualPrintService,
          ObjectName.getInstance("net.reini", table(virtualPrintService)));
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unable to register management bean", e);
    }
    return virtualPrintService;
  }

  private Hashtable<String, String> table(PrintService virtualPrintService) {
    Hashtable<String, String> table = new Hashtable<>();
    table.put("type", "virtual-printers");
    table.put("name", virtualPrintService.getName());
    return table;
  }

  private boolean unregisterFromJmxIfMatches(PrintService printService, String name) {
    if (printService.getName().equals(name)) {
      try {
        mbeanServer.unregisterMBean(ObjectName.getInstance("net.reini", table(printService)));
      } catch (Exception e) {
        LOG.log(Level.SEVERE, "Unable to unregister management bean", e);
      }
      return true;
    }
    return false;
  }

  private void initiallizePrinters() {
    try (Stream<URL> resources =
        Thread.currentThread().getContextClassLoader().resources("virtual-printer-names")) {
      if (resources.filter(this::addPrintersFromDefinition).count() == 0) {
        // initialize default virtual printer if no other have been defined
        addPrinter("VirtualPrinter");
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Failed to locate virtual printer definitions", e);
    }
  }

  private boolean addPrintersFromDefinition(URL url) {
    try (InputStream in = url.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
      br.lines() //
          .map(String::trim) //
          .filter(l -> !l.isEmpty()) //
          .filter(l -> !l.startsWith("#")) //
          .forEach(this::addPrinter);
      return true;
    } catch (IOException e) {
      LOG.log(Level.SEVERE, e, () -> "Failed to initialize printers from " + url);
      return false;
    }
  }

  @Override
  public String getDefaultPrinterName() {
    return defaultPrinterName;
  }

  @Override
  public void setDefaultPrinterName(String printerName) {
    if (printServices.stream().anyMatch(ps -> ps.getName().equals(printerName))) {
      defaultPrinterName = printerName;
    }
  }

  @Override
  public void addPrinter(String printerName) {
    requireNonNull(printerName, "printerName must not be null");
    if (printServices.stream().map(PrintService::getName).noneMatch(printerName::equals)) {
      LOG.log(Level.INFO, () -> "Adding printer: " + printerName);
      printServices.add(
          registerInJmx(new VirtualPrintService(printerName, () -> removePrinter(printerName))));
    }
  }

  @Override
  public void removePrinter(String printerName) {
    requireNonNull(printerName, "printerName must not be null");
    if (printerName.equals(defaultPrinterName)) {
      defaultPrinterName = null;
    }
    printServices.removeIf(ps -> unregisterFromJmxIfMatches(ps, printerName));
  }

  Stream<PrintService> printServices() {
    return printServices.stream();
  }

  Stream<MultiDocPrintService> multiDocPrintServices() {
    return multiDocPrintServices.stream();
  }

  PrintService defaultPrintService() {
    if (defaultPrinterName == null) {
      return null;
    }
    return printServices.stream() //
        .filter(ps -> ps.getName().endsWith(defaultPrinterName)) //
        .findFirst() //
        .orElse(null);
  }
}
