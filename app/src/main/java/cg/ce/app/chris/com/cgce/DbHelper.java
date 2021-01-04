package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by chris on 31/08/16.
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME="ce_mobile.sqlite" ;
    private static final int DB_SCHEME_VERSION=2;
    public static final String TABLE_NAME = "odbc";
    public static final String CN_INTEGRA="integra";
    private static final String DATABASE_ALTER_INTEGRA = "ALTER TABLE "
            + TABLE_NAME + " ADD COLUMN " + CN_INTEGRA + " TEXT;";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_SCHEME_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DataBaseManager.CREATE_TABLE);
        db.execSQL(DataBaseManager.CREATE_TABLE_PRINTER);
        db.execSQL(DataBaseManager.CREATE_TABLE_MARCA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("oldVersion", String.valueOf(oldVersion));
        Log.w("newVersion", String.valueOf(newVersion));
        switch (newVersion){
            case 2:
                db.execSQL(DATABASE_ALTER_INTEGRA);
                break;
        }
    }
}
