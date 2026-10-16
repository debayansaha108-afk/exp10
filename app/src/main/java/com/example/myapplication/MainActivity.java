package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_CONFIG = "config_value";

    private EditText editSharedPrefs;
    private Button btnCommitSettings;
    private Button btnRestoreSettings;
    private TextView tvSharedPrefsOutput;

    private EditText editSqliteRow;
    private Button btnExecuteInsert;
    private TextView tvSqliteOutput;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI Elements
        editSharedPrefs = findViewById(R.id.editSharedPrefs);
        btnCommitSettings = findViewById(R.id.btnCommitSettings);
        btnRestoreSettings = findViewById(R.id.btnRestoreSettings);
        tvSharedPrefsOutput = findViewById(R.id.tvSharedPrefsOutput);

        editSqliteRow = findViewById(R.id.editSqliteRow);
        btnExecuteInsert = findViewById(R.id.btnExecuteInsert);
        tvSqliteOutput = findViewById(R.id.tvSqliteOutput);

        dbHelper = new DatabaseHelper(this);

        // SharedPreferences Logic
        btnCommitSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = editSharedPrefs.getText().toString().trim();
                if (TextUtils.isEmpty(value)) {
                    Toast.makeText(MainActivity.this, "Cannot save blank configuration.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_CONFIG, value);
                editor.apply();

                Toast.makeText(MainActivity.this, "Configuration Committed!", Toast.LENGTH_SHORT).show();
                editSharedPrefs.setText("");
                updatePrefsDisplay();
            }
        });

        btnRestoreSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePrefsDisplay();
                Toast.makeText(MainActivity.this, "Configuration Restored!", Toast.LENGTH_SHORT).show();
            }
        });

        // SQLite Logic
        btnExecuteInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rowData = editSqliteRow.getText().toString().trim();
                if (TextUtils.isEmpty(rowData)) {
                    Toast.makeText(MainActivity.this, "Cannot save blank database row.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_TEXT, rowData);

                long newRowId = db.insert(DatabaseHelper.TABLE_NAME, null, values);
                if (newRowId != -1) {
                    Toast.makeText(MainActivity.this, "Row inserted successfully!", Toast.LENGTH_SHORT).show();
                    editSqliteRow.setText("");
                    refreshDisplay();
                } else {
                    Toast.makeText(MainActivity.this, "Error inserting row.", Toast.LENGTH_SHORT).show();
                }
                // We keep the db open because getWritableDatabase caches it, closing helper is usually done in onDestroy
            }
        });

        // Initial Data Load
        updatePrefsDisplay();
        refreshDisplay();
    }

    private void updatePrefsDisplay() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedValue = prefs.getString(KEY_CONFIG, "");
        if (TextUtils.isEmpty(savedValue)) {
            tvSharedPrefsOutput.setText("Cache state: Empty");
        } else {
            tvSharedPrefsOutput.setText("Cache state: " + savedValue);
        }
    }

    private void refreshDisplay() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        StringBuilder recordsBuilder = new StringBuilder();

        try {
            String[] projection = {DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_TEXT};
            cursor = db.query(
                    DatabaseHelper.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    DatabaseHelper.COLUMN_ID + " DESC" // Order by newest first
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID);
                    int textIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEXT);

                    long id = cursor.getLong(idIndex);
                    String text = cursor.getString(textIndex);

                    recordsBuilder.append("ID: ").append(id).append(" | Data: ").append(text).append("\n");
                } while (cursor.moveToNext());
            } else {
                recordsBuilder.append("No records found.");
            }
        } catch (Exception e) {
            recordsBuilder.append("Error reading database.");
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close(); // Safely close cursor to prevent memory leaks
            }
        }

        tvSqliteOutput.setText(recordsBuilder.toString().trim());
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }

    // --- Embedded SQLiteOpenHelper ---
    public static class DatabaseHelper extends SQLiteOpenHelper {

        public static final String DATABASE_NAME = "dashboard.db";
        public static final int DATABASE_VERSION = 1;

        public static final String TABLE_NAME = "records";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TEXT = "record_text";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_TEXT + " TEXT)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }
}