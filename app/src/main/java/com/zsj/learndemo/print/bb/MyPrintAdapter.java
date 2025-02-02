package com.zsj.learndemo.print.bb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangshijie on 2019/7/11;
 */
public class MyPrintAdapter extends PrintDocumentAdapter {
    private Context context;
    private int pageHeight;
    private int pageWidth;
    private PdfDocument mPdfDocument;
    private int totalpages = 1;
    private String pdfPath;
    private List<Bitmap> mlist;

    public MyPrintAdapter(Context context,String pdfPath) {
        this.context = context;
        this.pdfPath = pdfPath;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal,
                         LayoutResultCallback callback,
                         Bundle metadata) {

        mPdfDocument = new PrintedPdfDocument(context, newAttributes); //创建可打印PDF文档对象

        pageHeight = newAttributes.getMediaSize().ISO_A4.getHeightMils() * 72 / 1000;  //设置尺寸
        pageWidth = newAttributes.getMediaSize().ISO_A4.getWidthMils() * 72 / 1000;

        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        ParcelFileDescriptor mFileDescriptor = null;
        PdfRenderer pdfRender = null;
        PdfRenderer.Page page = null;
        try {
            mFileDescriptor = ParcelFileDescriptor.open(new File(pdfPath), ParcelFileDescriptor.MODE_READ_ONLY);
            if (mFileDescriptor != null)
                pdfRender = new PdfRenderer(mFileDescriptor);

            mlist = new ArrayList<>();

            if (pdfRender.getPageCount() > 0) {
                totalpages = pdfRender.getPageCount();
                for (int i = 0; i < pdfRender.getPageCount(); i++) {
                    if(null != page)
                        page.close();
                    page = pdfRender.openPage(i);
                    Bitmap bmp = Bitmap.createBitmap(page.getWidth()*2,page.getHeight()*2, Bitmap.Config.ARGB_8888);
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    mlist.add(bmp);
                }
            }
            if(null != page)
                page.close();
            if(null != mFileDescriptor)
                mFileDescriptor.close();
            if (null != pdfRender)
                pdfRender.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (totalpages > 0) {
            PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                    .Builder("快速入门.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(totalpages);  //构建文档配置信息

            PrintDocumentInfo info = builder.build();
            callback.onLayoutFinished(info, true);
        } else {
            callback.onLayoutFailed("Page count is zero.");
        }
    }

    @Override
    public void onWrite(final PageRange[] pageRanges, final ParcelFileDescriptor destination, final CancellationSignal cancellationSignal,
                        final WriteResultCallback callback) {
        for (int i = 0; i < totalpages; i++) {
            if (pageInRange(pageRanges, i)) //保证页码正确
            {
                PdfDocument.PageInfo newPage = new PdfDocument.PageInfo.Builder(pageWidth,
                        pageHeight, i).create();
                PdfDocument.Page page =
                        mPdfDocument.startPage(newPage);  //创建新页面

                if (cancellationSignal.isCanceled()) {  //取消信号
                    callback.onWriteCancelled();
                    mPdfDocument.close();
                    mPdfDocument = null;
                    return;
                }
                drawPage(page, i);  //将内容绘制到页面Canvas上
                mPdfDocument.finishPage(page);
            }
        }

        try {
            mPdfDocument.writeTo(new FileOutputStream(
                    destination.getFileDescriptor()));
        } catch (IOException e) {
            callback.onWriteFailed(e.toString());
            return;
        } finally {
            mPdfDocument.close();
            mPdfDocument = null;
        }

        callback.onWriteFinished(pageRanges);
    }

    private boolean pageInRange(PageRange[] pageRanges, int page) {
        for (int i = 0; i < pageRanges.length; i++) {
            if ((page >= pageRanges[i].getStart()) &&
                    (page <= pageRanges[i].getEnd()))
                return true;
        }
        return false;
    }

    //页面绘制（渲染）
    private void drawPage(PdfDocument.Page page,int pagenumber) {
        Canvas canvas = page.getCanvas();
//        if(mlist != null){
//            Paint paint = new Paint();
//            Bitmap bitmap = mlist.get(pagenumber);
//            int bitmapWidth = bitmap.getWidth();
//            int bitmapHeight = bitmap.getHeight();
//            // 计算缩放比例
//            float scale = (float)pageWidth/(float)bitmapWidth;
//            // 取得想要缩放的matrix参数
//            Matrix matrix = new Matrix();
//            matrix.postScale(scale, scale);
//            canvas.drawBitmap(bitmap,matrix,paint);
//        }
//        int titleBaseLine = 72;
//        int leftMargin = 54;
//
//        Paint paint = new Paint();
//        paint.setColor(Color.BLACK);
//        paint.setTextSize(36);
//        canvas.drawText("Test Title", leftMargin, titleBaseLine, paint);
//
//        paint.setTextSize(11);
//        canvas.drawText("Test paragraph", leftMargin, titleBaseLine + 25, paint);
//
//        paint.setColor(Color.BLUE);
//        canvas.drawRect(100, 100, 172, 172, paint);

        Paint paint = new Paint();

        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(2);
        int left = 100;
        int top = 100;
//        canvas.drawCircle(200,200,40,paint);
//
//        canvas.drawRect(400,400,500,500,paint);
//
//        paint.setStrokeWidth(20);
//        paint.setStrokeCap(Paint.Cap.ROUND);
//        canvas.drawPaint(paint);

        paint.setTextSize(30);
        canvas.drawText("J&T", left, top+10, paint);

        canvas.drawRect(left + 130, top - 30, left + 300, top + 50, paint);

        paint.setTextSize(13);
        canvas.drawText("Thasya Ariedhana", left + 350, top, paint);

        canvas.drawText("085811794743", left + 350, top + 40, paint);

        //getWidth()表示绘制多宽后换行
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(13);
        textPaint.setColor(Color.BLACK);
        String data = "asdhkjh k aksdjfhkjdas kajshdkfjh adskjfhskdjhfkjsdhflk kajshdfkjhaskdjf hkajsdhfkj khaksjdfh kjashdlkjhlakjsdhf kjashdflkjhaslfdkjhasjkdhfljkashfdjkasdhfljkashdf jafd ";
        StaticLayout sl = new StaticLayout(data, textPaint, 800, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
        //从0,0开始绘制
        canvas.translate(left, top+100);
        sl.draw(canvas);
    }

}
