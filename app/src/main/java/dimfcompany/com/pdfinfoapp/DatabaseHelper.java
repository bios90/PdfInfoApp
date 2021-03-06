package dimfcompany.com.pdfinfoapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
    Context ctx;

    public static final String DATABASE_NAME = "PdfInfo_Database";

    //region PdfTable
    public static final String TABLE_PDF_CELLS ="Table_Pdf_Cells";
    public static final String TABLE_URL_CELLS ="Table_Url_Cells";

    public static final String ID ="Id";
    public static final String NAME ="Name";
    public static final String VERSION ="Version";
    public static final String CATEG ="Categ";
    public static final String URL_PDF ="Url_Pdf";
    public static final String URL_IMG ="Url_Img";

    public static final String LOCAL_IMG ="Local_Img";
    public static final String LOCAL_PDF ="Local_Pdf";

    public static final String URL ="Url";
    //endregion


    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME,null,1);
        this.ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table "+TABLE_PDF_CELLS+" ("+ID+" INTEGER ,"+CATEG+" INTEGER ,  "+NAME+" TEXT,  "+VERSION+" INTEGER, "+URL_PDF+" TEXT, "+URL_IMG+" TEXT,"+LOCAL_IMG+" TEXT,"+LOCAL_PDF+" TEXT)");
        db.execSQL("create table "+TABLE_URL_CELLS+" ("+ID+" INTEGER ,  "+NAME+" TEXT,  "+VERSION+" INTEGER, "+URL+" TEXT, "+URL_IMG+" TEXT,"+LOCAL_IMG+" TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {

    }

    public void deleteAllInSql()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PDF_CELLS,null,null);
        db.delete(TABLE_URL_CELLS,null,null);
    }
}
