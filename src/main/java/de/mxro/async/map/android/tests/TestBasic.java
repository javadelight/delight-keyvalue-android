package de.mxro.async.map.android.tests;

import de.mxro.async.map.android.tests.AndroidStorageTests;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("all")
public class TestBasic {
  @Test
  public void test() {
    AndroidStorageTests.performAll();
  }
}
