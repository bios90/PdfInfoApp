package dimfcompany.com.pdfinfoapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActLiteMenu extends AppCompatActivity
{
    private static final String TAG = "ActLiteMenu";

    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    GlobalHelper gh;

    List<Model_PdfCell> listOfCells = new ArrayList<>();

    LinearLayout laForItems;

    File pdfDir;
    File imageDir;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lite_menu);
        init();
        preparePdfList();
        loadCellsToLA();
    }


    private void init()
    {
        gh = (GlobalHelper)getApplicationContext();
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        laForItems = findViewById(R.id.laForItems);

        String root = getApplicationContext().getExternalFilesDir(null).toString();
        pdfDir = new File(root + DownloadHelper.PDF_DIR);
        imageDir = new File(root+DownloadHelper.IMG_DIR);

    }

    private void preparePdfList()
    {
        String[] args = new String[]{""+0,""+1,""+2,""+3};
        String where = "Id=? OR Id=? OR Id=? OR Id=?";
        Cursor pdfCellData = db.query(DatabaseHelper.TABLE_PDF_CELLS,null,where,args,null,null,null);

        if(pdfCellData.getCount()>0)
        {
            while (pdfCellData.moveToNext())
            {
                Model_PdfCell pdfCell = new Model_PdfCell();

                pdfCell.setName(pdfCellData.getString(pdfCellData.getColumnIndex(DatabaseHelper.NAME)));
                pdfCell.setLocalImgName(pdfCellData.getString(pdfCellData.getColumnIndex(DatabaseHelper.LOCAL_IMG)));
                pdfCell.setLocalPdfName(pdfCellData.getString(pdfCellData.getColumnIndex(DatabaseHelper.LOCAL_PDF)));

                listOfCells.add(pdfCell);
            }
        }
    }

    private void loadCellsToLA()
    {
        for(int a=0;a<listOfCells.size();a++)
        {
            final Model_PdfCell cellModel = listOfCells.get(a);
            View cellView = getLayoutInflater().inflate(R.layout.item_pdf_cell,null);

            ImageView imgView = cellView.findViewById(R.id.img);
            TextView tv = cellView.findViewById(R.id.tv);

            File imgFile = new File(imageDir,cellModel.getLocalImgName()+DownloadHelper.PNG);
            final File pdfFile = new File(pdfDir,cellModel.getLocalPdfName()+DownloadHelper.PDF);

            tv.setText(cellModel.name);
            Picasso.get().load(imgFile).into(imgView);

            cellView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    gh.setPdfToShow(pdfFile);
                    Intent intent = new Intent(ActLiteMenu.this,ActPdfView.class);
                    startActivity(intent);
                }
            });

            ViewCompat.setElevation(cellView,10);

            laForItems.addView(cellView);
        }
    }


}
