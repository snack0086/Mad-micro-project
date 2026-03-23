package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String TABLE_ASSIGNMENTS = "assignments";
    private static final String DATABASE_NAME = "campus_connect.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    public static final String TABLE_MATERIALS = "learning_materials";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Learning materials table
        String createMaterialsTable =
                "CREATE TABLE " + TABLE_MATERIALS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "file_uri TEXT," +
                        "year TEXT" +
                        ")";

        db.execSQL(createMaterialsTable);

        String createAssignmentsTable =
                "CREATE TABLE " + TABLE_ASSIGNMENTS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "description TEXT," +
                        "year TEXT," +
                        "attachment_type TEXT," +
                        "attachment_uri TEXT" +
                        ")";

        db.execSQL(createAssignmentsTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MATERIALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSIGNMENTS);
        onCreate(db);
    }

    public boolean insertMaterial(String title, String fileUri, String year) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("title", title);
        values.put("file_uri", fileUri);
        values.put("year", year);

        long result = db.insert(TABLE_MATERIALS, null, values);
        return result != -1;
    }

    public boolean insertAssignment(
            String title,
            String description,
            String year,
            String attachmentType,
            String attachmentUri
    ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("title", title);
        values.put("description", description);
        values.put("year", year);
        values.put("attachment_type", attachmentType);
        values.put("attachment_uri", attachmentUri);

        long result = db.insert(TABLE_ASSIGNMENTS, null, values);
        return result != -1;
    }


}
