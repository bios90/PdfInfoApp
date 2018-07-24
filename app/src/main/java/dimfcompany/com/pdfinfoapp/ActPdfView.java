package dimfcompany.com.pdfinfoapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.barteksc.pdfviewer.PDFView;

public class ActPdfView extends AppCompatActivity
{
    PDFView pdfView;
    GlobalHelper gh;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);
        init();
        loadPdf();
    }



    private void init()
    {
        pdfView = findViewById(R.id.pdfView);
        gh = (GlobalHelper)getApplicationContext();
    }

    private void loadPdf()
    {
        try
        {
            pdfView.fromFile(gh.getPdfToShow()).enableDoubletap(true).spacing(4).load();
        }
        catch (Exception e)
        {

        }
    }
}
