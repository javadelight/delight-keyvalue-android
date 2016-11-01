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
import delight.keyvalue.tests.StoreTest
import org.robolectric.shadows.ShadowSQLiteDatabase
import delight.keyvalue.tests.StoreTests

class AndroidStorageTests {

	def static void performAll() {
		for (StoreTest t: StoreTests.all) {
			perform(t)
		}
	}
	
	def static void perform(StoreTest test) {
		val SQLiteConfiguration conf = AsyncMapAndorid::createDefaultConfiguration()
		val SQLiteDatabase db = ShadowSQLiteDatabase::create(null)
		AsyncMapAndorid::assertTable(db, conf)
		val Store<String, Object> map = AsyncMapAndorid::createMap(conf, SerializationJre::newJavaSerializer(), db)
		Async::waitFor((
			[ValueCallback<Success> callback|map.start(AsyncCommon::asSimpleCallback(callback))] as Operation<Success>))
			
		test.test(map)	
			
			
		Async::waitFor((
			[ValueCallback<Success> callback|map.stop(AsyncCommon::asSimpleCallback(callback)) db.close()] as Operation<Success>)
		)
	}
	
}