package dimfcompany.com.pdfinfoapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
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

import spencerstudios.com.bungeelib.Bungee;

public class DownloadHelper extends AppCompatActivity
{
    private static final String TAG = "DownloadHelper";

    Context ctx;
    GlobalHelper gh;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference rootRef;
    DatabaseReference pdfRef;
    DatabaseReference urlRef;

    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    List<Model_PdfCell> listPdfCellsDownload = new ArrayList<>();
    List<Model_UrlCell> listUrlCellsDownload = new ArrayList<>();

    List<File> filesToDelete = new ArrayList<>();

    Act_Download_Dialog dialogActivity;

    int tryNum = 0;


    //region FB Constants
    public static final String FB_PDF_CELLS = "PdfCells";
    public static final String FB_URL_CELLS = "UrlCells";
    public static final String FB_NAME = "name";
    public static final String FB_URL_IMG = "url_img";
    public static final String FB_URL = "url";
    public static final String FB_URL_PDF = "url_pdf";
    public static final String FB_V = "v";
    public static final String FB_CATEG = "categ";
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
        urlRef = rootRef.child(FB_URL_CELLS);

        dbHelper = new DatabaseHelper(ctx);
        db = dbHelper.getWritableDatabase();
        Intent intent = new Intent(ctx,Act_Download_Dialog.class);
        ctx.startActivity(intent);
        Bungee.shrink(ctx);
        gh.setDialogTitle("Соединение");
    }


    public void fullUpdate()
    {
        Log.e(TAG, "fullUpdate: begin Full Update" );

        final List<Model_PdfCell> pdfCellsLocal = new ArrayList<>();
        final List<Model_PdfCell> pdfCellsOnline = new ArrayList<>();

        final List<Model_UrlCell> urlCellsLocal = new ArrayList<>();
        final List<Model_UrlCell> urlCellsOnline = new ArrayList<>();

        //region collecting All local
        Cursor pdfCellData = db.query(DatabaseHelper.TABLE_PDF_CELLS,null,null,null,null,null,DatabaseHelper.ID);

        if(pdfCellData.getCount()>0)
        {
            while (pdfCellData.moveToNext())
            {
                Model_PdfCell pdfCell = new Model_PdfCell();

                pdfCell.setName(pdfCellData.getString(pdfCellData.getColumnIndex(DatabaseHelper.NAME)));
                pdfCell.setLocalImgName(pdfCellData.getString(pdfCellData.getColumnIndex(DatabaseHelper.LOCAL_IMG)));
                pdfCell.setLocalPdfName(pdfCellData.getString(pdfCellData.getColumnIndex(DatabaseHelper.LOCAL_PDF)));
                pdfCell.setV(pdfCellData.getInt(pdfCellData.getColumnIndex(DatabaseHelper.VERSION)));
                pdfCell.setId(pdfCellData.getInt(pdfCellData.getColumnIndex(DatabaseHelper.ID)));

                pdfCellsLocal.add(pdfCell);
            }
        }


        Cursor urlCellData = db.query(DatabaseHelper.TABLE_URL_CELLS,null,null,null,null,null,DatabaseHelper.ID);

        if(urlCellData.getCount()>0)
        {
            while (urlCellData.moveToNext())
            {
                Model_UrlCell urlCell = new Model_UrlCell();

                urlCell.setName(urlCellData.getString(urlCellData.getColumnIndex(DatabaseHelper.NAME)));
                urlCell.setLocalImgName(urlCellData.getString(urlCellData.getColumnIndex(DatabaseHelper.LOCAL_IMG)));
                urlCell.setUrl(urlCellData.getString(urlCellData.getColumnIndex(DatabaseHelper.URL)));
                urlCell.setV(urlCellData.getInt(urlCellData.getColumnIndex(DatabaseHelper.VERSION)));
                urlCell.setId(urlCellData.getInt(urlCellData.getColumnIndex(DatabaseHelper.ID)));

                urlCellsLocal.add(urlCell);
            }
        }
        //endregion

        //region collecting all Online

        final ValueEventListener urlCellEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for(DataSnapshot childSnap:dataSnapshot.getChildren())
                {
                    int id = Integer.parseInt(childSnap.getKey());

                    String name = (String) childSnap.child(FB_NAME).getValue();
                    String url = (String) childSnap.child(FB_URL).getValue();
                    String url_img = (String) childSnap.child(FB_URL_IMG).getValue();
                    int v = childSnap.child(FB_V).getValue(Integer.class);



                    Model_UrlCell urlCell = new Model_UrlCell();
                    urlCell.setId(id);
                    urlCell.setName(name);
                    urlCell.setUrl(url);
                    urlCell.setUrlImg(url_img);
                    urlCell.setV(v);

                    urlCellsOnline.add(urlCell);
                }

                Log.e(TAG, "onDataChange: collecting all Data finished, now wil jump to compare");
                //compare(pdfCellsLocal,pdfCellsOnline,urlCellsLocal,urlCellsOnline);
                newCompare(pdfCellsLocal,pdfCellsOnline,urlCellsLocal,urlCellsOnline);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };

        ValueEventListener pdfCellEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot childSnap:dataSnapshot.getChildren())
                {
                    int id = Integer.parseInt(childSnap.getKey());

                    String name = (String) childSnap.child(FB_NAME).getValue();
                    String url_pdf = (String) childSnap.child(FB_URL_PDF).getValue();
                    String url_img = (String) childSnap.child(FB_URL_IMG).getValue();
                    int categ = childSnap.child(FB_CATEG).getValue(Integer.class);
                    int v = childSnap.child(FB_V).getValue(Integer.class);


                    Model_PdfCell pdfCell = new Model_PdfCell();
                    pdfCell.setId(id);
                    pdfCell.setName(name);
                    pdfCell.setUrlPdf(url_pdf);
                    pdfCell.setUrlImg(url_img);
                    pdfCell.setV(v);
                    pdfCell.setCateg(categ);

                    pdfCellsOnline.add(pdfCell);
                }

                urlRef.addListenerForSingleValueEvent(urlCellEventListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };

        pdfRef.addListenerForSingleValueEvent(pdfCellEventListener);
        //endregion
    }

    void newCompare(List<Model_PdfCell> pdfLocal, List<Model_PdfCell> pdfOnline,List<Model_UrlCell> urlLocal,List<Model_UrlCell> urlOnline)
    {
        gh.sortPdfList(pdfLocal);
        gh.sortPdfList(pdfOnline);

        gh.sortUrlList(urlLocal);
        gh.sortUrlList(urlOnline);

        List<Model_PdfCell> pdfListToUpdateLocally = new ArrayList<>();
        List<Model_UrlCell> urlListToUpdateLocally = new ArrayList<>();

        String root = ctx.getApplicationContext().getExternalFilesDir(null).toString();

        final File pdfDir = new File(root + DownloadHelper.PDF_DIR);
        int pdfFileCount = pdfDir.list().length;

        if(pdfLocal.size() == pdfOnline.size() && urlLocal.size() == urlOnline.size() && pdfLocal.size() == pdfFileCount )
        {
            Log.e(TAG, "newCompare: All counts are == wil make update");

            for(int a = 0;a<pdfLocal.size();a++)
            {
                Log.e(TAG, "compare: now will be try number " + a );

                Model_PdfCell cellLocal = pdfLocal.get(a);
                Model_PdfCell cellOnline = pdfOnline.get(a);

                Log.e(TAG, "Pdf online v is" + cellOnline.getV() );
                Log.e(TAG, "pdf local name is" + cellLocal.getName() );
                Log.e(TAG, "pdf local v is  "+ cellLocal.getV() );


                if(cellLocal.getV() < cellOnline.getV())
                {
                    pdfListToUpdateLocally.add(cellOnline);
                }
            }

            for(int a=0;a<urlLocal.size();a++)
            {
                Model_UrlCell cellLocal = urlLocal.get(a);
                Model_UrlCell cellOnline = urlOnline.get(a);

                if(cellLocal.getV()<cellOnline.getV())
                {
                    urlListToUpdateLocally.add(cellOnline);
                }
            }

            listPdfCellsDownload = pdfListToUpdateLocally;
            listUrlCellsDownload = urlListToUpdateLocally;

            if(urlListToUpdateLocally.size()>0 || pdfListToUpdateLocally.size()>0)
            {
                Log.e(TAG, "Lists to update sizes > 0");
                Log.e(TAG, "urlToUpdate size is" + urlListToUpdateLocally.size() );
                Log.e(TAG, "PdfToupdate size is "+ pdfListToUpdateLocally.size() );

                savePdfCellsToSQL();
            }
            else
            {
                dialogActivity = gh.getCurrentDialog();
                dialogActivity.canExit = true;
                dialogActivity.onBackPressed();
                Toast.makeText(ctx, "У вас установленна последняя версия файлов.", Toast.LENGTH_SHORT).show();
            }
        }
        else
            {
                gh.fullDelete(ctx);
                firstDownload();
            }
    }

//    void compare(List<Model_PdfCell> pdfLocal,List<Model_PdfCell> pdfOnline,List<Model_UrlCell> urlLocal,List<Model_UrlCell> urlOnline)
//    {
//        List<Model_PdfCell> pdfLocalSorted = new ArrayList<>();
//        List<Model_PdfCell> pdfOnlineSorted = new ArrayList<>();
//
//        List<Model_PdfCell> pdfListToUpdateLocally = new ArrayList<>();
//
//        List<Model_UrlCell> urlLocalSorted = new ArrayList<>();
//        List<Model_UrlCell> urlOnlineSorted = new ArrayList<>();
//
//        List<Model_UrlCell> urlListToUpdateLocally = new ArrayList<>();
//
//        //region sort by Id
////        Log.e(TAG, "compare: sorting Began" );
////
////        for(int a = 0;a<pdfLocal.size();a++)
////        {
////            pdfLocalSorted.add(null);
////        }
////        for (Model_PdfCell cell : pdfLocal)
////        {
////            int id = cell.getId();
////            pdfLocalSorted.remove(id);
////            pdfLocalSorted.add(id,cell);
////        }
////
////        for(int a = 0;a<pdfOnline.size();a++)
////        {
////            pdfOnlineSorted.add(null);
////        }
////        for (Model_PdfCell cell : pdfOnline)
////        {
////            int id = cell.getId();
////            pdfOnlineSorted.remove(id);
////            pdfOnlineSorted.add(id,cell);
////        }
////
////
////
////
////
////
////        for(int a = 0;a<urlLocal.size();a++)
////        {
////            urlLocalSorted.add(null);
////        }
////        for(Model_UrlCell cell:urlLocal)
////        {
////            int id = cell.getId();
////            urlLocalSorted.remove(id);
////            urlLocalSorted.add(id,cell);
////        }
////
////        for(int a = 0;a<urlOnline.size();a++)
////        {
////            urlOnlineSorted.add(null);
////        }
////        for(Model_UrlCell cell:urlOnline)
////        {
////            int id = cell.getId();
////            urlOnlineSorted.remove(id);
////            urlOnlineSorted.add(id,cell);
////        }
////
////        Log.e(TAG, "pdfLocal before sorted "+pdfLocal.size()+" ---- pdfLocal after sorted "+ pdfLocalSorted.size());
////        Log.e(TAG, "pdfOnline before sorted "+ pdfOnline.size()+" ---- pdfOnline after sorted "+ pdfOnlineSorted.size());
////
////        Log.e(TAG, "urlLocal before sorted  "+urlLocal.size()+" ----- urlLocal after sorted "+urlLocalSorted.size());
////        Log.e(TAG, "urlOnline before sorted "+urlOnline.size() + " ----- urlOnline after sorted "+urlOnlineSorted.size());
//        //endregion
//
//
//
//        //region Forming List To Update
//        if(pdfLocalSorted.size() == pdfOnlineSorted.size() && urlLocalSorted.size() == urlOnlineSorted.size())
//        {
//            Log.e(TAG, "compare: all List sizes are ==" );
//
//            for(int a = 0;a<pdfLocalSorted.size();a++)
//            {
//                Log.e(TAG, "compare: now will be try number " + a );
//
//                Model_PdfCell cellLocal = pdfLocalSorted.get(a);
//                Model_PdfCell cellOnline = pdfOnlineSorted.get(a);
//
//                Log.e(TAG, "Pdf online v is" + cellOnline.getV() );
//                Log.e(TAG, "pdf local name is" + cellLocal.getName() );
//                Log.e(TAG, "pdf local v is  "+ cellLocal.getV() );
//                if(cellLocal.getV() < cellOnline.getV())
//                {
//                    pdfListToUpdateLocally.add(cellOnline);
//                }
//            }
//
//            for(int a=0;a<urlLocalSorted.size();a++)
//            {
//                Model_UrlCell cellLocal = urlLocalSorted.get(a);
//                Model_UrlCell cellOnline = urlOnlineSorted.get(a);
//
//                if(cellLocal.getV()<cellOnline.getV())
//                {
//                    urlListToUpdateLocally.add(cellOnline);
//                }
//            }
//
//            listPdfCellsDownload = pdfListToUpdateLocally;
//            listUrlCellsDownload = urlListToUpdateLocally;
//        }
//        else
//            {
//                if(pdfOnlineSorted.size()>pdfLocalSorted.size())
//                {
//                    listPdfCellsDownload = pdfOnlineSorted;
//                    List<Integer> idToRemove = new ArrayList<>();
//
//                    for (int a = 0; a < pdfLocalSorted.size(); a++)
//                    {
//                        Model_PdfCell cellLocal = pdfLocalSorted.get(a);
//                        Model_PdfCell cellOnline = pdfOnlineSorted.get(a);
//
//                        if (cellLocal.getV() == cellOnline.getV())
//                        {
//                            int id = cellOnline.getId();
//                            idToRemove.add(id);
//                        }
//                    }
//
//                    for(int i : idToRemove)
//                    {
//                        for(int b = listPdfCellsDownload.size() - 1;b >= 0; b--)
//                        {
//                            Model_PdfCell cell = listPdfCellsDownload.get(b);
//                            if (cell.getId() == i)
//                            {
//                                listPdfCellsDownload.remove(cell);
//                            }
//                        }
//                    }
//
//                    pdfListToUpdateLocally = listPdfCellsDownload;
//                }
//
//                else if(pdfOnlineSorted.size()<pdfLocalSorted.size())
//                {
//                    listPdfCellsDownload = pdfOnlineSorted;
//                    List<Integer> idToRemove = new ArrayList<>();
//
//                    for(int a = 0;a<pdfOnlineSorted.size();a++)
//                    {
//                        Model_PdfCell cellLocal = pdfLocalSorted.get(a);
//                        Model_PdfCell cellOnline = pdfOnlineSorted.get(a);
//
//                        if (cellLocal.getV() == cellOnline.getV())
//                        {
//                            int id = cellOnline.getId();
//                            idToRemove.add(id);
//                        }
//                    }
//
//                    for(int i : idToRemove)
//                    {
//                        for(int b = listPdfCellsDownload.size() - 1;b >= 0; b--)
//                        {
//                            Model_PdfCell cell = listPdfCellsDownload.get(b);
//                            if (cell.getId() == i)
//                            {
//                                listPdfCellsDownload.remove(cell);
//                            }
//                        }
//                    }
//
//                    pdfListToUpdateLocally = listPdfCellsDownload;
//                }
//
//
//                if(urlOnlineSorted.size()>urlLocalSorted.size())
//                {
//                    listUrlCellsDownload = urlOnlineSorted;
//                    List<Integer> idToRemove = new ArrayList<>();
//
//                    for(int a=0;a<urlLocalSorted.size();a++)
//                    {
//                        Model_UrlCell cellOnline = urlOnlineSorted.get(a);
//                        Model_UrlCell cellLocal = urlLocalSorted.get(a);
//
//                        if(cellLocal.getV()==cellOnline.getV())
//                        {
//                            int id = cellOnline.getId();
//                            idToRemove.add(id);
//                        }
//                    }
//
//                    for(int i : idToRemove)
//                    {
//                        for(int b = listUrlCellsDownload.size() - 1;b >= 0; b--)
//                        {
//                            Model_UrlCell cell = listUrlCellsDownload.get(b);
//                            if (cell.getId() == i)
//                            {
//                                listUrlCellsDownload.remove(cell);
//                            }
//                        }
//                    }
//
//                    urlListToUpdateLocally = listUrlCellsDownload;
//                }
//
//                else if(urlOnlineSorted.size()<urlLocalSorted.size())
//                {
//                    listUrlCellsDownload = urlOnlineSorted;
//                    List<Integer> idToRemove = new ArrayList<>();
//
//                    for(int a=0;a<urlOnlineSorted.size();a++)
//                    {
//                        Model_UrlCell cellOnline = urlOnlineSorted.get(a);
//                        Model_UrlCell cellLocal = urlLocalSorted.get(a);
//
//                        if(cellLocal.getV()==cellOnline.getV())
//                        {
//                            int id = cellOnline.getId();
//                            idToRemove.add(id);
//                        }
//                    }
//
//                    for(int i : idToRemove)
//                    {
//                        for(int b = listUrlCellsDownload.size() - 1;b >= 0; b--)
//                        {
//                            Model_UrlCell cell = listUrlCellsDownload.get(b);
//                            if (cell.getId() == i)
//                            {
//                                listUrlCellsDownload.remove(cell);
//                            }
//                        }
//                    }
//
//                    urlListToUpdateLocally = listUrlCellsDownload;
//                }
//            }
//            //endregion
//
//        if(urlListToUpdateLocally.size()>0 || pdfListToUpdateLocally.size()>0)
//        {
//            Log.e(TAG, "Lists to update sizes > 0");
//            Log.e(TAG, "urlToUpdate size is" + urlListToUpdateLocally.size() );
//            Log.e(TAG, "PdfToupdate size is "+ pdfListToUpdateLocally.size() );
//
//            savePdfCellsToSQL();
//        }
//        else
//        {
//            dialogActivity = gh.getCurrentDialog();
//            dialogActivity.canExit = true;
//            dialogActivity.onBackPressed();
//            Toast.makeText(ctx, "У вас установленна последняя версия файлов.", Toast.LENGTH_SHORT).show();
//        }
//
//    }

    public void firstDownload()
    {
        Log.e(TAG, "firstDownload: begin First Download");

        final ValueEventListener urlCellEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot childSnap:dataSnapshot.getChildren())
                {
                    int id = Integer.parseInt(childSnap.getKey());

                    String name = (String) childSnap.child(FB_NAME).getValue();
                    String url = (String) childSnap.child(FB_URL).getValue();
                    String url_img = (String) childSnap.child(FB_URL_IMG).getValue();
                    int v = childSnap.child(FB_V).getValue(Integer.class);

                    Model_UrlCell urlCell = new Model_UrlCell();
                    urlCell.setId(id);
                    urlCell.setName(name);
                    urlCell.setUrl(url);
                    urlCell.setUrlImg(url_img);
                    urlCell.setV(v);

                    listUrlCellsDownload.add(urlCell);
                }

                if(listUrlCellsDownload.size() > 0 || listPdfCellsDownload.size() > 0)
                {
                    Log.e(TAG, "onDataChange: Finished Getting Info from FB First Download, now will save to SQL" );
                    savePdfCellsToSQL();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };


        ValueEventListener pdfCellEventListener = new ValueEventListener()
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
                    int categ = childSnap.child(FB_CATEG).getValue(Integer.class);
                    int v = childSnap.child(FB_V).getValue(Integer.class);

                    Log.e(TAG, "fb cell name"+name );
                    Log.e(TAG, "fb url of file pdf"+url_pdf );

                    Model_PdfCell pdfCell = new Model_PdfCell();
                    pdfCell.setId(id);
                    pdfCell.setName(name);
                    pdfCell.setUrlPdf(url_pdf);
                    pdfCell.setUrlImg(url_img);
                    pdfCell.setV(v);
                    pdfCell.setCateg(categ);

                    listPdfCellsDownload.add(pdfCell);
                }


                urlRef.addListenerForSingleValueEvent(urlCellEventListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };

        pdfRef.addListenerForSingleValueEvent(pdfCellEventListener);

        Log.e(TAG, "firstDownload: trying to change dialog tv");

    }


    void savePdfCellsToSQL()
    {
        Log.e(TAG, "savePdfCellsToSQL: Begin saving to sql");

        List<String> allUrls = new ArrayList<>();
        List<File> allFiles = new ArrayList<>();

        String root = ctx.getApplicationContext().getExternalFilesDir(null).toString();

        final File pdfDir = new File(root + PDF_DIR);
        if (!pdfDir.exists())
        {
            pdfDir.mkdirs();
        }

        final File imagesDir = new File(root + IMG_DIR);
        if (!imagesDir.exists())
        {
            imagesDir.mkdirs();
        }


        for (Model_PdfCell pdfCell : listPdfCellsDownload)
        {


            String id = String.valueOf(pdfCell.Id);
            String[] args = new String[]{id};
            Log.e(TAG, "savePdfCellsToSQL: id is"+ args[0]);

            //Cursor pdfCellData = db.query(DatabaseHelper.TABLE_PDF_CELLS, null, dbHelper.ID + "=?", args, null, null, DatabaseHelper.ID);
            //db.delete(dbHelper.TABLE_PDF_CELLS, dbHelper.ID + "=?", args);
//            if (pdfCellData.getCount() > 0)
//            {
//                while (pdfCellData.moveToNext())
//                {
//                    Log.e(TAG, "savePdfCellsToSQL: find file to delete");
//                    String imgName = pdfCellData.getString(pdfCellData.getColumnIndex(DatabaseHelper.LOCAL_IMG));
//                    String pdfName = pdfCellData.getString(pdfCellData.getColumnIndex(DatabaseHelper.LOCAL_PDF));
//
//                    File imgFile = new File(imagesDir,imgName+DownloadHelper.PNG);
//                    File pdfFile = new File(pdfDir,pdfName+DownloadHelper.PNG);
//
//                    filesToDelete.add(imgFile);
//                    filesToDelete.add(pdfFile);
//                }
//            }
            db.delete(dbHelper.TABLE_PDF_CELLS,"Id=?",args);
            //db.delete(dbHelper.TABLE_PDF_CELLS, dbHelper.ID + "=?", args);

            ContentValues pdfCellValues=new ContentValues();

            String pdfLocalName = GlobalHelper.randomStr();
            String imgLocalName = GlobalHelper.randomStr();

            pdfCellValues.put(dbHelper.ID,id);
            pdfCellValues.put(dbHelper.NAME,pdfCell.name);
            pdfCellValues.put(dbHelper.URL_PDF,pdfCell.urlPdf);
            pdfCellValues.put(dbHelper.URL_IMG,pdfCell.urlImg);
            pdfCellValues.put(dbHelper.VERSION,pdfCell.v);
            pdfCellValues.put(dbHelper.CATEG,pdfCell.categ);

            pdfCellValues.put(dbHelper.LOCAL_PDF,pdfLocalName);
            pdfCellValues.put(dbHelper.LOCAL_IMG,imgLocalName);

            db.insert(dbHelper.TABLE_PDF_CELLS,null,pdfCellValues);
            Log.e(TAG, "savePdfCellsToSQL: inserted info to pdf cell table" );


            try
            {
                allUrls.add(pdfCell.urlPdf);
                final File pdfFile = new File(pdfDir, pdfLocalName + PDF);
                allFiles.add(pdfFile);

                allUrls.add(pdfCell.urlImg);
                final File imgFile = new File(imagesDir, imgLocalName + PNG);
                allFiles.add(imgFile);
            }
            catch (Exception e)
            {
                Log.e(TAG, "exeption on creating" + e.getMessage());
            }
        }

        for (Model_UrlCell urlCell : listUrlCellsDownload)
        {
            String id = String.valueOf(urlCell.Id);
            String[] args = new String[]{id};

            Cursor urlCellData = db.query(DatabaseHelper.TABLE_URL_CELLS, null, dbHelper.ID + "=?", args, null, null, DatabaseHelper.ID);

            if (urlCellData.getCount() > 0)
            {
                while (urlCellData.moveToNext())
                {
                    String imgName = urlCellData.getString(urlCellData.getColumnIndex(DatabaseHelper.LOCAL_IMG));
                    File imgFile = new File(imagesDir,imgName+DownloadHelper.PNG);

                    filesToDelete.add(imgFile);
                }
            }

            db.delete(dbHelper.TABLE_URL_CELLS, dbHelper.ID + "=?", args);

            ContentValues urlCellValues=new ContentValues();

            String imgLocalName = GlobalHelper.randomStr();

            urlCellValues.put(dbHelper.ID,id);
            urlCellValues.put(dbHelper.NAME,urlCell.name);
            urlCellValues.put(dbHelper.URL,urlCell.url);
            urlCellValues.put(dbHelper.VERSION,urlCell.v);
            urlCellValues.put(dbHelper.URL_IMG,urlCell.urlImg);

            urlCellValues.put(dbHelper.LOCAL_IMG,imgLocalName);

            db.insert(dbHelper.TABLE_URL_CELLS,null,urlCellValues);
            Log.e(TAG, "savePdfCellsToSQL: inserted in to table urlscells" );

            allUrls.add(urlCell.urlImg);
            final File imgFile = new File(imagesDir, imgLocalName + PNG);
            allFiles.add(imgFile);
        }

        if (listUrlCellsDownload.size() >0 || listPdfCellsDownload.size()>0)
        {
            DownloadFiles df = new DownloadFiles(allFiles, allUrls);
            df.execute();
        }
    }



    class DownloadFiles extends AsyncTask<String, String, String>
    {
        List<File> files;
        List<String> urls;

        public DownloadFiles(List<File> files, List<String> urls)
        {
            Log.e(TAG, "DownloadFiles: list of files more than 0 , begin to download" );
            this.files = files;
            this.urls = urls;

            dialogActivity = gh.getCurrentDialog();
            dialogActivity.getProgressBar().setMax(files.size());
            dialogActivity.getTv().setText("Загрузка");
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            Log.e(TAG, "onPostExecute: all downloaded succesfully" );
            if(filesToDelete.size()>0)
            {
                for (File f : filesToDelete)
                {
                    Log.e(TAG, "onPostExecute: deleting file" );
                    f.delete();
                }
            }
        }

        @Override
        protected String doInBackground(String... f_url)
        {
            for (int a = 0;a<urls.size();a++)
            {
                int count;
                try
                {
                    Log.e(TAG, "doInBackground: begin of download" );
                    URL url = new URL(urls.get(a));
                    URLConnection conection = url.openConnection();
                    conection.connect();


                    int lenghtOfFile = conection.getContentLength();

                    InputStream input = new BufferedInputStream(url.openStream(),
                            8192);

                    Log.e(TAG, "doInBackground: open Stream ok" );
                    Log.e(TAG, "doInBackground: Cuurent url is --- "+url );

                    OutputStream output = new FileOutputStream(files.get(a));

                    byte data[] = new byte[1024];

                    long total = 0;

                    Log.e(TAG, "doInBackground: middle of download" );

                    while ((count = input.read(data)) != -1)
                    {
                        total += count;
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();

                    tryNum = 0;
                    Log.e(TAG, "doInBackground: now will increment" );

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            dialogActivity.getProgressBar().incrementProgressBy(1);
                        }
                    });


                } catch (Exception e)
                {
                    Log.e(TAG, "Entered catch Block,will retry,retry num is "+tryNum );

                    if(tryNum>2)
                    {
                        dialogActivity = gh.getCurrentDialog();
                        dialogActivity.canExit = true;
                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                Toast.makeText(ctx, "Возникла ошибка при загрузке, пожалуйста обновите базу позже.", Toast.LENGTH_SHORT).show();
                                dialogActivity.onBackPressed();
                            }
                        });
                        gh.fullDelete(ctx);
                        return null;
                    }
                    a--;
                    tryNum++;
                }

            }
            return null;
        }

    }


}
