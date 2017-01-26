package de.mxro.async.map.android.tests;

import android.database.sqlite.SQLiteDatabase;
import de.mxro.async.map.android.AsyncMapAndorid;
import de.mxro.async.map.android.SQLiteConfiguration;
import de.mxro.serialization.Serializer;
import de.mxro.serialization.jre.SerializationJre;
import de.mxro.serialization.jre.StreamDestination;
import de.mxro.serialization.jre.StreamSource;
import delight.async.AsyncCommon;
import delight.async.Operation;
import delight.async.callbacks.SimpleCallback;
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
    Serializer<StreamSource, StreamDestination> _newJavaSerializer = SerializationJre.newJavaSerializer();
    final Store<String, Object> map = AsyncMapAndorid.<Object>createMap(conf, _newJavaSerializer, db);
    final Procedure1<ValueCallback<Success>> _function = new Procedure1<ValueCallback<Success>>() {
      @Override
      public void apply(final ValueCallback<Success> callback) {
        SimpleCallback _asSimpleCallback = AsyncCommon.<Success>asSimpleCallback(callback);
        map.start(_asSimpleCallback);
      }
    };
    Async.<Success>waitFor(
      ((Operation<Success>) new Operation<Success>() {
          public void apply(ValueCallback<Success> callback) {
            _function.apply(callback);
          }
      }));
    test.test(map);
    final Procedure1<ValueCallback<Success>> _function_1 = new Procedure1<ValueCallback<Success>>() {
      @Override
      public void apply(final ValueCallback<Success> callback) {
        SimpleCallback _asSimpleCallback = AsyncCommon.<Success>asSimpleCallback(callback);
        map.stop(_asSimpleCallback);
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
