package dimfcompany.com.pdfinfoapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import spencerstudios.com.bungeelib.Bungee;

public class ActItemsScroll extends AppCompatActivity
{
    private static final String TAG = "ActItemsScroll";

    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    GlobalHelper gh;

    List<Model_PdfCell> listOfCells = new ArrayList<>();

    LinearLayout laForItems;

    File pdfDir;
    File imageDir;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference rootRef;
    DatabaseReference pdfRef;
    DatabaseReference urlRef;

    boolean onResumeBool = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lite_menu);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        init();
        preparePdfList();
        loadCellsToLA();
        if(gh.isNetworkAvailable())
        {
            checkForUpdate();
        }
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

        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = firebaseDatabase.getReference();
        pdfRef = rootRef.child(DownloadHelper.FB_PDF_CELLS);
        urlRef = rootRef.child(DownloadHelper.FB_URL_CELLS);
    }

    private void preparePdfList()
    {
        int categId = gh.getCurrentCategToShow();
        String[] args = new String[]{"" + categId};
        String where = "Categ=?";
        Cursor pdfCellData = db.query(DatabaseHelper.TABLE_PDF_CELLS, null, where, args, null, null, DatabaseHelper.ID);

        if (pdfCellData.getCount() > 0)
        {
            while (pdfCellData.moveToNext())
            {
                Model_PdfCell pdfCell = new Model_PdfCell();

                pdfCell.setName(pdfCellData.getString(pdfCellData.getColumnIndex(DatabaseHelper.NAME)));
                pdfCell.setLocalImgName(pdfCellData.getString(pdfCellData.getColumnIndex(DatabaseHelper.LOCAL_IMG)));
                pdfCell.setLocalPdfName(pdfCellData.getString(pdfCellData.getColumnIndex(DatabaseHelper.LOCAL_PDF)));
                pdfCell.setV(pdfCellData.getInt(pdfCellData.getColumnIndex(DatabaseHelper.VERSION)));

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

            cellModel.setViewInScroll(cellView);

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
                    Intent intent = new Intent(ActItemsScroll.this,ActPdfView.class);
                    startActivity(intent);
                }
            });

            ViewCompat.setElevation(cellView,10);

            laForItems.addView(cellView);
        }
    }

    private void checkForUpdate()
    {
        final ArrayList<Model_PdfCell> listOnline = new ArrayList<>();

        Query query = pdfRef.orderByChild(DownloadHelper.FB_CATEG).equalTo(gh.getCurrentCategToShow());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot childSnap:dataSnapshot.getChildren())
                {
                    int id = Integer.parseInt(childSnap.getKey());

                    String name = (String) childSnap.child(DownloadHelper.FB_NAME).getValue();
                    String url_pdf = (String) childSnap.child(DownloadHelper.FB_URL_PDF).getValue();
                    String url_img = (String) childSnap.child(DownloadHelper.FB_URL_IMG).getValue();
                    int categ = childSnap.child(DownloadHelper.FB_CATEG).getValue(Integer.class);
                    int v = childSnap.child(DownloadHelper.FB_V).getValue(Integer.class);

                    Model_PdfCell pdfCell = new Model_PdfCell();
                    pdfCell.setId(id);
                    pdfCell.setName(name);
                    pdfCell.setUrlPdf(url_pdf);
                    pdfCell.setUrlImg(url_img);
                    pdfCell.setV(v);
                    pdfCell.setCateg(categ);

                    listOnline.add(pdfCell);
                }

                Log.e(TAG, "Count of this categ in FB is"+ listOnline.size() );
                compare(listOnline);
            }

            private void compare(ArrayList<Model_PdfCell> listOnline)
            {
                boolean hasToUpdate = false;
                List<Model_PdfCell> onlineSorted = GlobalHelper.sortPDfByID(listOnline);
                for(int a=0;a<listOfCells.size();a++)
                {
                    Model_PdfCell cellLocal = listOfCells.get(a);
                    Model_PdfCell cellOnline = onlineSorted.get(a);
                    View laUpdate = cellLocal.getViewInScroll().findViewById(R.id.laUpdate);

                    if(cellOnline.getV() > cellLocal.getV())
                    {
                        laUpdate.setVisibility(View.VISIBLE);
                        hasToUpdate = true;
                    }
                    else
                        {
                            laUpdate.setVisibility(View.GONE);
                        }
                }

                if(hasToUpdate == true)
                {
                    Intent intent = new Intent(ActItemsScroll.this,ActUpdateDialog.class);
                    startActivityForResult(intent,999);
                    Bungee.shrink(ActItemsScroll.this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        Log.e(TAG, "onActivityResult: callled" );
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_CANCELED)
        {
            for (Model_PdfCell cell : listOfCells)
            {
                View laUpdate = cell.getViewInScroll().findViewById(R.id.laUpdate);
                laUpdate.setVisibility(View.GONE);
            }
        }
    }
}
