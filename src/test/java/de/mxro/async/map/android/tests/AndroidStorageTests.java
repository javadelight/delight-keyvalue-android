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
import delight.keyvalue.tests.StoreTest;
import delight.keyvalue.tests.StoreTests;
import java.util.List;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.robolectric.shadows.ShadowSQLiteDatabase;

@SuppressWarnings("all")
public class AndroidStorageTests {
  public static void performAll() {
    List<StoreTest> _all = StoreTests.all();
    for (final StoreTest t : _all) {
      AndroidStorageTests.perform(t);
    }
  }
  
  public static void perform(final StoreTest test) {
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
          public void apply(ValueCallback<Success> null) {
            _function.apply(null);
          }
      }));
    test.test(map);
    final Procedure1<ValueCallback<Success>> _function_1 = new Procedure1<ValueCallback<Success>>() {
      @Override
      public void apply(final ValueCallback<Success> callback) {
        map.stop(AsyncCommon.<Success>asSimpleCallback(callback));
        db.close();
      }
    };
    Async.<Success>waitFor(
      ((Operation<Success>) new Operation<Success>() {
          public void apply(ValueCallback<Success> null) {
            _function_1.apply(null);
          }
      }));
  }
}
