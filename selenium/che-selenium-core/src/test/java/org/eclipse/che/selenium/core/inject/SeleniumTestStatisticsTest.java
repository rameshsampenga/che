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

import static java.util.Collections.EMPTY_MAP;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class SeleniumTestStatisticsTest {
  static final Logger LOG = LoggerFactory.getLogger(SeleniumTestStatisticsTest.class);

  @Test(dataProvider = "testData")
  @SuppressWarnings("FutureReturnValueIgnored")
  public void testSimultaneousUsage(
      Map<String, Integer> allTestNumbers,
      int hitStart,
      int hitPass,
      int hitFail,
      int hitSkip,
      String initialToString,
      String finalToString)
      throws InterruptedException {
    // given
    SeleniumTestStatistics seleniumTestStatistics = new SeleniumTestStatistics();

    // when
    allTestNumbers.forEach(seleniumTestStatistics::updateAllTestNumber);

    // then
    assertEquals(seleniumTestStatistics.toString(), initialToString);

    // when
    ExecutorService executor = Executors.newFixedThreadPool(10);

    IntStream.range(0, hitStart)
        .forEach(
            i ->
                executor.submit(
                    () -> {
                      seleniumTestStatistics.hitStart();
                      LOG.info("hitStart:{} - {}", i, seleniumTestStatistics.toString());
                    }));

    IntStream.range(0, hitPass)
        .forEach(
            i ->
                executor.submit(
                    () -> {
                      seleniumTestStatistics.hitPass();
                      LOG.info("hitPass:{} - {}", i, seleniumTestStatistics.toString());
                    }));

    IntStream.range(0, hitFail)
        .forEach(
            i ->
                executor.submit(
                    () -> {
                      seleniumTestStatistics.hitFail();
                      LOG.info("hitFail:{} - {}", i, seleniumTestStatistics.toString());
                    }));

    IntStream.range(0, hitSkip)
        .forEach(
            i ->
                executor.submit(
                    () -> {
                      seleniumTestStatistics.hitSkip();
                      LOG.info("hitSkip:{} - {}", i, seleniumTestStatistics.toString());
                    }));

    executor.awaitTermination(20, TimeUnit.SECONDS);

    // then
    assertEquals(seleniumTestStatistics.toString(), finalToString);
    assertEquals(seleniumTestStatistics.hitStart(), hitStart + 1);
    assertEquals(seleniumTestStatistics.hitPass(), hitPass + 1);
    assertEquals(seleniumTestStatistics.hitFail(), hitFail + 1);
    assertEquals(seleniumTestStatistics.hitSkip(), hitSkip + 1);
  }

  @DataProvider
  public Object[][] testData() {
    return new Object[][] {
      {
        EMPTY_MAP,
        0,
        0,
        0,
        0,
        "Executing 0 test(s) from suite(s) []: passed = 0, failed = 0, skipped = 0.",
        "Executing 0 test(s) from suite(s) []: passed = 0, failed = 0, skipped = 0."
      },
      {
        ImmutableMap.of("suite1", 10, "suite2", 20, "suite3", 5),
        31,
        21,
        5,
        4,
        "Executing 35 test(s) from suite(s) [suite1, suite2, suite3]: passed = 0, failed = 0, skipped = 0.",
        "Executing 35 test(s) from suite(s) [suite1, suite2, suite3]: passed = 21, failed = 5, skipped = 4."
      }
    };
  }
}
