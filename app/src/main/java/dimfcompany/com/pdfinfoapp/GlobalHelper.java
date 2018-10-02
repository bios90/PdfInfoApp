package dimfcompany.com.pdfinfoapp;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dimfcompany.com.pdfinfoapp.Interfaces.IGetPassCallback;

public class GlobalHelper extends Application
{
    private static final String TAG = "GlobalHelper";

    public static final String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static Random RANDOM = new Random();

    private File pdfToShow;
    private Act_Download_Dialog currentDialog;
    private String dialogTitle;

    private int currentCategToShow;

    private boolean needToLogin = true;

    public static String GET_CURRENT_PASS_URL = "http://www.eikei.ru/alisadb/getLockPass.php";

    public static String randomStr()
    {
        int len = 20;
        StringBuilder sb = new StringBuilder(len);
        for(int a = 0;a<=len;a++)
        {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }
        return sb.toString();
    }

    public boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static List<Model_PdfCell> sortPDfByID(List<Model_PdfCell> pdfToSort)
    {
        List<Model_PdfCell> pdfSorted = new ArrayList<>();

        for(int a = 0;a<pdfToSort.size();a++)
        {
            pdfSorted.add(null);
        }
        for (Model_PdfCell cell : pdfToSort)
        {
            int id = pdfToSort.indexOf(cell);
            pdfSorted.remove(id);
            pdfSorted.add(id,cell);
        }

        return pdfSorted;
    }



    public void sortPdfList(List<Model_PdfCell> pdfToSort)
    {
        Collections.sort(pdfToSort, new Comparator<Model_PdfCell>()
        {
            public int compare(Model_PdfCell obj1, Model_PdfCell obj2)
            {
                 return Integer.valueOf(obj1.getId()).compareTo(obj1.getId());
            }
        });

    }

    public void sortUrlList(List<Model_UrlCell> urlToSort)
    {
        Collections.sort(urlToSort, new Comparator<Model_UrlCell>()
        {
            public int compare(Model_UrlCell obj1, Model_UrlCell obj2)
            {
                return Integer.valueOf(obj1.getId()).compareTo(obj1.getId());
            }
        });
    }

    void fullDelete(Context ctx)
    {
        DatabaseHelper db =new DatabaseHelper(ctx);
        db.deleteAllInSql();

        String root = ctx.getApplicationContext().getExternalFilesDir(null).toString();

        final File pdfDir = new File(root + DownloadHelper.PDF_DIR);
        if (pdfDir.isDirectory())
        {
            String[] children = pdfDir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(pdfDir, children[i]).delete();
            }
        }

        final File imgDir = new File(root + DownloadHelper.IMG_DIR);
        if (imgDir.isDirectory())
        {
            String[] children = imgDir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(imgDir, children[i]).delete();
            }
        }
    }

    public void getCurrentPass(Context ctx, final IGetPassCallback callback)
    {
        Response.Listener<String> successListener = new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                if(response != null && !response.equals(""))
                {
                    callback.onSuccess(response);
                }
                else
                {
                    callback.onError();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                callback.onError();
            }
        };

        StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_CURRENT_PASS_URL, successListener, errorListener)
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> myParams = new HashMap<>();
                myParams.put("passrequest", "_passrequest");
                return myParams;
            }
        };

        Volley.newRequestQueue(ctx).add(stringRequest);
    }


    //region getter and setter
    public boolean isNeedToLogin()
    {
        return needToLogin;
    }

    public void setNeedToLogin(boolean needToLogin)
    {
        this.needToLogin = needToLogin;
    }

    public int getCurrentCategToShow()
    {
        return currentCategToShow;
    }

    public void setCurrentCategToShow(int currentCategToShow)
    {
        this.currentCategToShow = currentCategToShow;
    }

    public String getDialogTitle()
    {
        return dialogTitle;
    }

    public void setDialogTitle(String dialogTitle)
    {
        this.dialogTitle = dialogTitle;
    }

    public Act_Download_Dialog getCurrentDialog()
    {
        return currentDialog;
    }

    public void setCurrentDialog(Act_Download_Dialog currentDialog)
    {
        this.currentDialog = currentDialog;
    }

    public File getPdfToShow()
    {
        return pdfToShow;
    }

    public void setPdfToShow(File pdfToShow)
    {
        this.pdfToShow = pdfToShow;
    }
    //endregion
}
