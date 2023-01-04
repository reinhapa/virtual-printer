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

public interface VirtualPrintServiceMXBean {

  /**
   * Returns the name of the virtual printer.
   * 
   * @return virtual printer name
   */
  String getName();

  /**
   * Returns the current state of the virtual printer.
   * 
   * @return current virtual printer name
   */
  String getPrinterState();

  /**
   * Returns the amount of canceled jobs.
   * 
   * @return total count of canceled jobs
   */
  long getCanceled();

  /**
   * Returns the amount of completed jobs.
   * 
   * @return total count of completed jobs
   */
  long getCompleted();

  /**
   * Returns the amount of failed jobs.
   * 
   * @return total count of failed jobs
   */
  long getFailed();

  /**
   * Returns the amount of running jobs.
   * 
   * @return total count of running jobs
   */
  int getRunning();

  /**
   * Suspends the virtual printer, so that it does no longer accepting jobs.
   */
  void suspend();

  /**
   * Resumes the virtual printer, so that it starts accepting jobs.
   */
  void activate();

  /**
   * Removes the current virtual printer
   */
  void remove();

  /**
   * Resets the statistic counters for completed, failed and canceled jobs.
   */
  void resetStatistics();
}
