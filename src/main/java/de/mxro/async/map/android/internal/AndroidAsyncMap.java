package de.mxro.async.map.android.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import de.mxro.async.callbacks.SimpleCallback;
import de.mxro.async.callbacks.ValueCallback;
import de.mxro.async.map.AsyncMap;
import de.mxro.async.map.android.SQLiteConfiguration;
import de.mxro.async.map.operations.MapOperation;
import de.mxro.serialization.Serializer;
import de.mxro.serialization.jre.SerializationJre;
import de.mxro.serialization.jre.StreamDestination;
import de.mxro.serialization.jre.StreamSource;

public class AndroidAsyncMap<V> implements AsyncMap<String, V> {

	private final SQLiteConfiguration conf;

	
	private final Serializer<StreamSource, StreamDestination> serializer;
	
	
	private SQLiteDatabase db;

	@Override
	public void put(String key, V value, SimpleCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void get(String key, ValueCallback<V> callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(String key, SimpleCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public V getSync(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putSync(String key, V value) {
		final ContentValues cv = row(key);

		ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
		serializer.serialize(value,
				SerializationJre.createStreamDestination(os));

		cv.put(key, os.toByteArray());

		db.replaceOrThrow(conf.getTableName(), null, cv);
	}

	private ContentValues row(String key) {
		final ContentValues cv = new ContentValues();
		cv.put(conf.getKeyColumnName(), key);
		return cv;
	}

	@Override
	public void removeSync(String key) {

		db.delete(conf.getTableName(), conf.getKeyColumnName() + " = ?",
				new String[] { key });

	}

	@Override
	public void start(SimpleCallback callback) {
		db = SQLiteDatabase.openOrCreateDatabase(conf.getDatabasePath(), null);
		
		
		callback.onSuccess();
	}

	@Override
	public void stop(SimpleCallback callback) {
		db.close();
		
		callback.onSuccess();
	}

	@Override
	public void commit(SimpleCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void performOperation(MapOperation operation) {
		// TODO Auto-generated method stub

	}

}
