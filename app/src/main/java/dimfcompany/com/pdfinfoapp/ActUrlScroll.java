package dimfcompany.com.pdfinfoapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActUrlScroll extends AppCompatActivity
{
    GlobalHelper gh;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    List<Model_UrlCell> listOfCells = new ArrayList<>();

    LinearLayout laForItems;

    File pdfDir;
    File imageDir;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference rootRef;
    DatabaseReference pdfRef;
    DatabaseReference urlRef;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_scroll);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = firebaseDatabase.getReference();
        pdfRef = rootRef.child(DownloadHelper.FB_PDF_CELLS);
        urlRef = rootRef.child(DownloadHelper.FB_URL_CELLS);
    }

    private void preparePdfList()
    {
        Cursor urlCellData = db.query(DatabaseHelper.TABLE_URL_CELLS, null, null, null, null, null, DatabaseHelper.ID);

        if (urlCellData.getCount() > 0)
        {
            while (urlCellData.moveToNext())
            {
                Model_UrlCell urlCell = new Model_UrlCell();

                urlCell.setName(urlCellData.getString(urlCellData.getColumnIndex(DatabaseHelper.NAME)));
                urlCell.setLocalImgName(urlCellData.getString(urlCellData.getColumnIndex(DatabaseHelper.LOCAL_IMG)));
                urlCell.setV(urlCellData.getInt(urlCellData.getColumnIndex(DatabaseHelper.VERSION)));
                urlCell.setId(urlCellData.getInt(urlCellData.getColumnIndex(DatabaseHelper.ID)));
                urlCell.setUrl(urlCellData.getString(urlCellData.getColumnIndex(DatabaseHelper.URL)));

                listOfCells.add(urlCell);
            }
        }
    }


    private void loadCellsToLA()
    {
        for(int a=0;a<listOfCells.size();a++)
        {
            final Model_UrlCell cellModel = listOfCells.get(a);
            View cellView = getLayoutInflater().inflate(R.layout.item_url_cell,null);

            cellModel.setScrollView(cellView);

            ImageView imgView = cellView.findViewById(R.id.img);
            TextView tv = cellView.findViewById(R.id.tv);

            File imgFile = new File(imageDir,cellModel.getLocalImgName()+DownloadHelper.PNG);

            tv.setText(cellModel.name);
            Picasso.get().load(imgFile).into(imgView);

            cellView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!gh.isNetworkAvailable())
                    {
                        Toast.makeText(gh, "Для открытия ссылок необходим доступ в интернет", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    urlRef.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            Uri uri = null;
                            String url = dataSnapshot.child(cellModel.getId()+"").child(DownloadHelper.FB_URL).getValue(String.class);
                            uri = Uri.parse(url);

                            Intent intent= new Intent(Intent.ACTION_VIEW,uri);
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }
            });

            ViewCompat.setElevation(cellView,10);

            laForItems.addView(cellView);
        }
    }
}
