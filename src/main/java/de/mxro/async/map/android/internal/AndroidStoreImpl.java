package de.mxro.async.map.android.internal;

import delight.async.Value;
import delight.async.callbacks.SimpleCallback;
import delight.async.callbacks.ValueCallback;
import delight.functional.Closure;
import delight.keyvalue.StoreEntry;
import delight.keyvalue.StoreImplementation;
import delight.keyvalue.internal.v01.StoreEntryData;
import delight.keyvalue.operations.StoreOperation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import de.mxro.async.map.android.AsyncMapAndorid;
import de.mxro.async.map.android.SQLiteConfiguration;
import de.mxro.serialization.Serializer;
import de.mxro.serialization.jre.SerializationJre;
import de.mxro.serialization.jre.StreamDestination;
import de.mxro.serialization.jre.StreamSource;

public class AndroidStoreImpl<V> implements StoreImplementation<String, V> {

    private final boolean ENABLE_LOG = false;

    private final SQLiteConfiguration conf;

    private final Serializer<StreamSource, StreamDestination> serializer;

    private final SQLiteDatabase db;

    @Override
    public void put(final String key, final V value, final SimpleCallback callback) {
        putSync(key, value);

        this.commit(callback);

    }

    @Override
    public void get(final String key, final ValueCallback<V> callback) {
        callback.onSuccess(this.getSync(key));
    }

    @Override
    public void remove(final String key, final SimpleCallback callback) {
        removeSync(key);

        this.commit(callback);

    }

    @SuppressWarnings("unchecked")
    @Override
    public V getSync(final String key) {

        final byte[] data = executeQueryImmidiately(createSelectStatement(), key);

        if (ENABLE_LOG) {
            if (data == null) {
                System.out.println(this + ": getSync " + key + " retrieved null");
            }
        }

        if (data == null) {
            return null;
        }

        final Object object = serializer
                .deserialize(SerializationJre.createStreamSource(new ByteArrayInputStream(data)));

        if (ENABLE_LOG) {
            System.out.println(this + ": getSync " + key + " retrieved " + object);
        }

        return (V) object;
    }

    private String createSelectStatement() {
        final String sql = "SELECT " + conf.getKeyColumnName() + ", " + conf.getValueColumnName() + " FROM "
                + conf.getTableName() + " WHERE " + conf.getKeyColumnName() + " = ?";

        return sql;
    }

    @Override
    public void putSync(final String key, final V value) {
        assert key.length() <= AsyncMapAndorid.KEY_LENGTH;

        if (ENABLE_LOG) {
            System.out.println(this + ": putSync " + key + " Value " + value);
        }

        final SQLiteStatement statement = createInsertStatement(key, value);

        executeInsertStatementImmidiately(statement);

    }

    private byte[] executeQueryImmidiately(final String sql, final String key) {

        final Cursor query = db.query(conf.getTableName(),
                new String[] { conf.getKeyColumnName(), conf.getValueColumnName() }, conf.getKeyColumnName() + "=?",
                new String[] { key }, null, null, null);
        if (query.getCount() == 0) {
            return null;
        }

        query.moveToFirst();
        final byte[] data = query.getBlob(1);
        query.close();
        return data;
    }

    @Override
    public void getAll(final String keyStartsWith, final Closure<StoreEntry<String, V>> onEntry,
            final SimpleCallback onCompleted) {
        executeMultiQueryImmidiately(keyStartsWith, onEntry, onCompleted);

    }

    @Override
    public void getAll(final String keyStartsWith, final int fromIdx, final int toIdx,
            final ValueCallback<List<StoreEntry<String, V>>> callback) {
        executeMultiQueryImmidiately(keyStartsWith, fromIdx, toIdx, callback);
    }

    private void executeMultiQueryImmidiately(final String keyStartsWith, final int fromIdx, final int toIdx,
            final ValueCallback<List<StoreEntry<String, V>>> callback) {

        final String sqlQuery;
        final Cursor query;
        if (!keyStartsWith.equals("")) {
            sqlQuery = "SELECT " + conf.getKeyColumnName() + ", " + conf.getValueColumnName() + " FROM "
                    + conf.getTableName() + " WHERE " + conf.getKeyColumnName() + " LIKE ?";
            query = db.rawQuery(sqlQuery, new String[] { keyStartsWith + "%" });
        } else {
            sqlQuery = "SELECT " + conf.getKeyColumnName() + ", " + conf.getValueColumnName() + " FROM "
                    + conf.getTableName();
            query = db.rawQuery(sqlQuery, null);
        }

        if (query.getCount() == 0) {
            onCompleted.onSuccess();
            return;
        }

        while (query.moveToNext()) {
            final byte[] data = query.getBlob(1);
            final String key = query.getString(0);

            final Object object = serializer
                    .deserialize(SerializationJre.createStreamSource(new ByteArrayInputStream(data)));

            onEntry.apply(new StoreEntryData<String, V>(key, (V) object));

        }

        query.close();
        onCompleted.onSuccess();
    }

    // TODO replace with more efficient implementation using a special SQL
    // query.
    @Override
    public void count(final String keyStartsWith, final ValueCallback<Integer> callback) {
        final Value<Integer> count = new Value<Integer>(0);
        getAll(keyStartsWith, new Closure<StoreEntry<String, V>>() {

            @Override
            public void apply(final StoreEntry<String, V> o) {
                count.set(count.get() + 1);
            }
        }, new SimpleCallback() {

            @Override
            public void onFailure(final Throwable t) {
                callback.onFailure(t);
            }

            @Override
            public void onSuccess() {
                callback.onSuccess(count.get());
            }
        });
    }

    private void executeUpdateOrDeleteStatementImmidiately(final SQLiteStatement statement) {
        db.beginTransaction();

        final int rowsAffected = statement.executeUpdateDelete();

        if (rowsAffected != 1) {
            throw new RuntimeException("No rows could be found for query " + statement.toString());
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void executeInsertStatementImmidiately(final SQLiteStatement statement) {
        db.beginTransaction();

        statement.executeInsert();

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private SQLiteStatement createInsertStatement(final String key, final V value) {
        final String sql = "INSERT OR REPLACE INTO " + conf.getTableName() + " (" + conf.getKeyColumnName() + ","
                + conf.getValueColumnName() + ") VALUES (?, ?)";
        final SQLiteStatement statement = db.compileStatement(sql);

        statement.bindString(1, key);

        final ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        serializer.serialize(value, SerializationJre.createStreamDestination(os));

        statement.bindBlob(2, os.toByteArray());
        return statement;
    }

    @Override
    public void removeSync(final String key) {

        executeUpdateOrDeleteStatementImmidiately(createRemoveStatement(key));

    }

    private SQLiteStatement createRemoveStatement(final String key) {
        final String sql = "DELETE FROM " + conf.getTableName() + " WHERE " + conf.getKeyColumnName() + " = ?";
        final SQLiteStatement statement = db.compileStatement(sql);

        statement.bindString(1, key);

        return statement;
    }

    private SQLiteStatement createRemoveAllStatement(final String keyStartsWith) {
        final String sql = "DELETE FROM " + conf.getTableName() + " WHERE " + conf.getKeyColumnName() + " LIKE ?";
        final SQLiteStatement statement = db.compileStatement(sql);

        statement.bindString(1, keyStartsWith + "%");

        return statement;
    }

    @Override
    public void removeAll(final String keyStartsWith, final SimpleCallback callback) {
        try {
            db.beginTransaction();
            createRemoveAllStatement(keyStartsWith).executeUpdateDelete();
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (final Throwable t) {
            callback.onFailure(t);
            return;
        }
        callback.onSuccess();
    }

    @Override
    public void get(final List<String> keys, final ValueCallback<List<V>> callback) {
        final List<V> results = new ArrayList<V>(keys.size());

        for (final String key : keys) {
            results.add(getSync(key));
        }

        callback.onSuccess(results);
    }

    @Override
    public void start(final SimpleCallback callback) {

        callback.onSuccess();
    }

    @Override
    public void stop(final SimpleCallback callback) {

        db.close();

        callback.onSuccess();
    }

    @Override
    public void commit(final SimpleCallback callback) {
        callback.onSuccess();
    }

    @Override
    public void performOperation(final StoreOperation<String, V> operation, final ValueCallback<Object> callback) {
        operation.applyOn(this, callback);
    }

    @Override
    public void clearCache() {
        SQLiteDatabase.releaseMemory();
    }

    public AndroidStoreImpl(final SQLiteConfiguration conf,
            final Serializer<StreamSource, StreamDestination> serializer, final SQLiteDatabase db) {
        super();
        this.conf = conf;
        this.serializer = serializer;
        this.db = db;
    }

}
