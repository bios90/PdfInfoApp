package dimfcompany.com.pdfinfoapp;

public class Model_PdfCell
{
    String name;
    String urlPdf;
    String urlImg;
    int v;

    int Id;

    String localPdfName;
    String localImgName;


    //region getter and setter
    public String getLocalPdfName()
    {
        return localPdfName;
    }

    public void setLocalPdfName(String localPdfName)
    {
        this.localPdfName = localPdfName;
    }

    public String getLocalImgName()
    {
        return localImgName;
    }

    public void setLocalImgName(String localImgName)
    {
        this.localImgName = localImgName;
    }

    public int getId()
    {
        return Id;
    }

    public void setId(int id)
    {
        Id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUrlPdf()
    {
        return urlPdf;
    }

    public void setUrlPdf(String urlPdf)
    {
        this.urlPdf = urlPdf;
    }

    public String getUrlImg()
    {
        return urlImg;
    }

    public void setUrlImg(String urlImg)
    {
        this.urlImg = urlImg;
    }

    public int getV()
    {
        return v;
    }

    public void setV(int v)
    {
        this.v = v;
    }
    //endregion

    //region Constructor
    public Model_PdfCell()
    {

    }
    //endregion
}