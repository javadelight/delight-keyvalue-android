package de.mxro.async.map.android.tests

import android.database.sqlite.SQLiteDatabase
import de.mxro.async.map.android.AsyncMapAndorid
import de.mxro.async.map.android.SQLiteConfiguration
import de.mxro.serialization.jre.SerializationJre
import delight.async.AsyncCommon
import delight.async.Operation
import delight.async.callbacks.ValueCallback
import delight.async.jre.Async
import delight.functional.Success
import delight.keyvalue.Store
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowSQLiteDatabase

@RunWith(typeof(RobolectricTestRunner)) 
class TestThatValuesCanBeReadAndWritten {
	@Test 
	def void test() throws Exception {
		
		val SQLiteConfiguration conf = AsyncMapAndorid::createDefaultConfiguration()
		val SQLiteDatabase db = ShadowSQLiteDatabase::create(null)
		AsyncMapAndorid::assertTable(db, conf)
		val Store<String, Object> map = AsyncMapAndorid::createMap(conf, SerializationJre::newJavaSerializer(), db)
		Async::waitFor((
			[ValueCallback<Success> callback|map.start(AsyncCommon::asSimpleCallback(callback))] as Operation<Success>))
		map.putSync("one", 1)
		map.putSync("two", 2)
		map.putSync("three", 3)
		map.removeSync("three")
		Assert::assertEquals(1, map.getSync("one"))
		Assert::assertEquals(2, map.getSync("two"))
		Assert::assertEquals(null, map.getSync("three"))
		Async::waitFor((
			[ValueCallback<Success> callback|map.stop(AsyncCommon::asSimpleCallback(callback)) db.close()] as Operation<Success>)
		)
		
		
		
	}

}
