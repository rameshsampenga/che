/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.inject;

import static java.lang.String.format;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/** @author Dmytro Nochevnov */
public class SeleniumTestStatistics {
  private static final String statisticsTemplate =
      "Executing %d test(s) from suite(s) [%s]: passed = %d, failed = %d, skipped = %d.";

  private final ConcurrentMap<String, Integer> allTestNumber = new ConcurrentHashMap<>();
  private final AtomicInteger runTests = new AtomicInteger();
  private final AtomicInteger failedTests = new AtomicInteger();
  private final AtomicInteger passedTests = new AtomicInteger();
  private final AtomicInteger skippedTests = new AtomicInteger();

  private int allTestsNumber;
  private String allSuites = "";

  public void updateAllTestNumber(String suiteName, int suiteTestNumber) {
    this.allTestNumber.put(suiteName, suiteTestNumber);
    allTestsNumber = allTestNumber.values().stream().reduce(Integer::sum).get();
    allSuites = allTestNumber.keySet().stream().reduce((s, s2) -> s2 + ", " + s).get();
  }

  public int hitStart() {
    return runTests.incrementAndGet();
  }

  public int hitPass() {
    return passedTests.incrementAndGet();
  }

  public int hitFail() {
    return failedTests.incrementAndGet();
  }

  public int hitSkip() {
    return skippedTests.incrementAndGet();
  }

  @Override
  public String toString() {
    synchronized (this) {
      return format(
          statisticsTemplate,
          allTestsNumber,
          allSuites,
          passedTests.get(),
          failedTests.get(),
          skippedTests.get());
    }
  }
}
