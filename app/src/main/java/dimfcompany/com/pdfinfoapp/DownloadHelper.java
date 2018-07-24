package dimfcompany.com.pdfinfoapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class DownloadHelper
{
    private static final String TAG = "DownloadHelper";

    Context ctx;
    GlobalHelper gh;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference rootRef;
    DatabaseReference pdfRef;

    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    List<Model_PdfCell> listPdfCellsDownload = new ArrayList<>();

    //region FB Constants
    public static final String FB_PDF_CELLS = "PdfCells";
    public static final String FB_NAME = "name";
    public static final String FB_URL_IMG = "url_img";
    public static final String FB_URL_PDF = "url_pdf";
    public static final String FB_V = "v";
    //endregion

    //region Local Folders
    public static final String PDF_DIR = "/PDFs";
    public static final String IMG_DIR = "/Images";

    public static final String PDF= ".pdf";
    public static final String PNG= ".png";
    //endregion

    public DownloadHelper(Context context)
    {
        this.ctx = context;
        init();
    }

    private void init()
    {
        gh = (GlobalHelper)ctx.getApplicationContext();
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = firebaseDatabase.getReference();
        pdfRef = rootRef.child(FB_PDF_CELLS);

        dbHelper = new DatabaseHelper(ctx);
        db = dbHelper.getWritableDatabase();
    }

    public void firstDownload()
    {
        Log.e(TAG, "firstDownload: entered firstDownload void");

        ValueEventListener eventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.e(TAG, "onDataChange: getting data from FB" );

                for (DataSnapshot childSnap:dataSnapshot.getChildren())
                {
                    int id = Integer.parseInt(childSnap.getKey());

                    String name = (String) childSnap.child(FB_NAME).getValue();
                    String url_pdf = (String) childSnap.child(FB_URL_PDF).getValue();
                    String url_img = (String) childSnap.child(FB_URL_IMG).getValue();
                    int v = childSnap.child(FB_V).getValue(Integer.class);

                    Model_PdfCell pdfCell = new Model_PdfCell();
                    pdfCell.setId(id);
                    pdfCell.setName(name);
                    pdfCell.setUrlPdf(url_pdf);
                    pdfCell.setUrlImg(url_img);
                    pdfCell.setV(v);
                    listPdfCellsDownload.add(pdfCell);
                }

                Log.e(TAG, "onDataChange: listOfCells Count "+ listPdfCellsDownload.size()  );
                savePdfCellsToSQL(listPdfCellsDownload);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };

        pdfRef.addValueEventListener(eventListener);

    }


    void savePdfCellsToSQL(List<Model_PdfCell> cells)
    {

        List <String> pdfUrlsToDownload = new ArrayList<>();
        List <String> pdfLocalNames = new ArrayList<>();

        List <String> imgUrlsToDownload = new ArrayList<>();
        List <String> imgLocalNames = new ArrayList<>();


        for (Model_PdfCell cell : cells)
        {
            String id = String.valueOf(cell.Id);
            String[] args = new String[]{id};
            db.delete(dbHelper.TABLE_PDF_CELLS, dbHelper.ID + "=?", args);

            ContentValues pdfCellValues=new ContentValues();

            String pdfLocalName = GlobalHelper.randomStr();
            String imgLocalName = GlobalHelper.randomStr();

            pdfCellValues.put(dbHelper.ID,id);
            pdfCellValues.put(dbHelper.NAME,cell.name);
            pdfCellValues.put(dbHelper.URL_PDF,cell.urlPdf);
            pdfCellValues.put(dbHelper.URL_IMG,cell.urlImg);
            pdfCellValues.put(dbHelper.VERSION,cell.v);

            pdfCellValues.put(dbHelper.LOCAL_PDF,pdfLocalName);
            pdfCellValues.put(dbHelper.LOCAL_IMG,imgLocalName);

            pdfUrlsToDownload.add(cell.urlPdf);
            pdfLocalNames.add(pdfLocalName);

            imgUrlsToDownload.add(cell.urlImg);
            imgLocalNames.add(imgLocalName);

            db.insert(dbHelper.TABLE_PDF_CELLS,null,pdfCellValues);
            Log.e(TAG, "savePdfCellsToSQL: inserted info to pdf cell table" );
        }

        downloadPDFs(pdfUrlsToDownload,pdfLocalNames);
        downloadImages(imgUrlsToDownload,imgLocalNames);
        Toast.makeText(ctx, "База обновленна до последней версии.", Toast.LENGTH_SHORT).show();
    }


    void downloadPDFs(List<String> urls,List<String> names)
    {
        try
        {
            List<File> files = new ArrayList<>();

            for (String url : urls)
            {
                String root = ctx.getApplicationContext().getExternalFilesDir(null).toString();
                final File pdfDir = new File(root + PDF_DIR);
                if (!pdfDir.exists())
                {
                    pdfDir.mkdirs();
                }

                int index = urls.indexOf(url);
                final File pdfFile = new File(pdfDir, names.get(index) + PDF);
                files.add(pdfFile);
            }

            String str_result = new DownloadFiles(files, urls).execute().get();
        }
        catch (Exception e)
        {

        }

    }

    private void downloadImages(List<String> imgUrlsToDownload, List<String> imgLocalNames)
    {
        try
        {
            List<File> files = new ArrayList<>();

            for (String url : imgUrlsToDownload)
            {
                String root = ctx.getApplicationContext().getExternalFilesDir(null).toString();
                final File imagesDir = new File(root + IMG_DIR);
                if (!imagesDir.exists())
                {
                    imagesDir.mkdirs();
                }

                int index = imgUrlsToDownload.indexOf(url);
                final File imgFile = new File(imagesDir, imgLocalNames.get(index) + PNG);
                files.add(imgFile);
            }

            String str_result = new DownloadFiles(files, imgUrlsToDownload).execute().get();
        }
        catch (Exception e)
        {

        }
    }


    class DownloadFiles extends AsyncTask<String, String, String>
    {
        List<File> files;
        List<String> urls;

        public DownloadFiles(List<File> files, List<String> urls)
        {
            this.files = files;
            this.urls = urls;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... f_url)
        {
            for (int a = 0;a<urls.size();a++)
            {
                int count;
                try
                {
                    URL url = new URL(urls.get(a));
                    URLConnection conection = url.openConnection();
                    conection.connect();

                    int lenghtOfFile = conection.getContentLength();

                    InputStream input = new BufferedInputStream(url.openStream(),
                            8192);

                    OutputStream output = new FileOutputStream(files.get(a));

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1)
                    {
                        total += count;
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();

                } catch (Exception e)
                {

                }

            }
            return null;
        }

    }


}
