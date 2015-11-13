package com.keysd.beaconscanner;

import org.junit.Test;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;


@Config(constants = BuildConfig.class)
public class ExampleUnitTest {
  @Test
  public void addition_isCorrect() throws Exception {
    assertEquals(4, 2 + 2);
  }
}