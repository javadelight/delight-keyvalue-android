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
    Serializer<StreamSource, StreamDestination> _newJavaSerializer = SerializationJre.newJavaSerializer();
    final Store<String, Object> map = AsyncMapAndorid.<Object>createMap(conf, _newJavaSerializer, db);
    final Procedure1<ValueCallback<Success>> _function = new Procedure1<ValueCallback<Success>>() {
      @Override
      public void apply(final ValueCallback<Success> callback) {
        SimpleCallback _asSimpleCallback = AsyncCommon.asSimpleCallback(callback);
        map.start(_asSimpleCallback);
      }
    };
    Async.<Success>waitFor(
      ((Operation<Success>) new Operation<Success>() {
          public void apply(ValueCallback<Success> null) {
            _function.apply(callback);
          }
      }));
    map.putSync("one", Integer.valueOf(1));
    map.putSync("two", Integer.valueOf(2));
    map.putSync("three", Integer.valueOf(3));
    map.removeSync("three");
    Object _sync = map.getSync("one");
    Assert.assertEquals(Integer.valueOf(1), _sync);
    Object _sync_1 = map.getSync("two");
    Assert.assertEquals(Integer.valueOf(2), _sync_1);
    Object _sync_2 = map.getSync("three");
    Assert.assertEquals(null, _sync_2);
    final Procedure1<ValueCallback<Success>> _function_1 = new Procedure1<ValueCallback<Success>>() {
      @Override
      public void apply(final ValueCallback<Success> callback) {
        SimpleCallback _asSimpleCallback = AsyncCommon.asSimpleCallback(callback);
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
