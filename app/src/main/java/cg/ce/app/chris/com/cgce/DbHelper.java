package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chris on 31/08/16.
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME="ce_mobile.sqlite" ;
    private static final int DB_SCHEME_VERSION=1;
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
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}