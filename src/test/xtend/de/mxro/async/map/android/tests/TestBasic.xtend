package de.mxro.async.map.android.tests

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(typeof(RobolectricTestRunner)) 
class TestBasic {
	
	@Test
	def void test() {
		AndroidStorageTests.performAll
	}
	
}