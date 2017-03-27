package de.mxro.async.map.android.tests;

import android.database.sqlite.SQLiteDatabase;
import de.mxro.async.map.android.AsyncMapAndorid;
import de.mxro.async.map.android.SQLiteConfiguration;
import de.mxro.serialization.jre.SerializationJre;
import delight.async.AsyncCommon;
import delight.async.Operation;
import delight.async.callbacks.ValueCallback;
import delight.async.jre.Async;
import delight.functional.Success;
import delight.keyvalue.Store;
import junit.framework.Assert;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowSQLiteDatabase;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("all")
public class TestThatValuesCanBeReadAndWritten {
  @Test
  public void test() throws Exception {
    final SQLiteConfiguration conf = AsyncMapAndorid.createDefaultConfiguration();
    final SQLiteDatabase db = ShadowSQLiteDatabase.create(null);
    AsyncMapAndorid.assertTable(db, conf);
    final Store<String, Object> map = AsyncMapAndorid.<Object>createMap(conf, SerializationJre.newJavaSerializer(), db);
    final Procedure1<ValueCallback<Success>> _function = new Procedure1<ValueCallback<Success>>() {
      @Override
      public void apply(final ValueCallback<Success> callback) {
        map.start(AsyncCommon.<Success>asSimpleCallback(callback));
      }
    };
    Async.<Success>waitFor(
      ((Operation<Success>) new Operation<Success>() {
          public void apply(ValueCallback<Success> callback) {
            _function.apply(callback);
          }
      }));
    map.putSync("one", Integer.valueOf(1));
    map.putSync("two", Integer.valueOf(2));
    map.putSync("three", Integer.valueOf(3));
    map.removeSync("three");
    Assert.assertEquals(Integer.valueOf(1), map.getSync("one"));
    Assert.assertEquals(Integer.valueOf(2), map.getSync("two"));
    Assert.assertEquals(null, map.getSync("three"));
    final Procedure1<ValueCallback<Success>> _function_1 = new Procedure1<ValueCallback<Success>>() {
      @Override
      public void apply(final ValueCallback<Success> callback) {
        map.stop(AsyncCommon.<Success>asSimpleCallback(callback));
        db.close();
      }
    };
    Async.<Success>waitFor(
      ((Operation<Success>) new Operation<Success>() {
          public void apply(ValueCallback<Success> callback) {
            _function_1.apply(callback);
          }
      }));
  }
}
