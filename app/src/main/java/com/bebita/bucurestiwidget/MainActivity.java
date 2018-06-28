package com.bebita.bucurestiwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.RemoteViews;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MainActivity extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ComponentName thisWidget = new ComponentName(context, MainActivity.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (final int widgetId : allWidgetIds) {
            // create some random data
            final RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.activity_main);
            // Register an onClickListener
            Intent intent = new Intent(context, MainActivity.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);

            new Thread(){
                public void run(){
                    Document doc = null;
                    try {
                        doc = Jsoup.connect("http://www.meteoromania.ro/anm2/").get();

                        String ora = doc.select("#bucuresti-filaret .subtitle").get(0).html();
                        String img = doc.select("#bucuresti-filaret .icon img").get(0).attr("src");
                        String text = doc.select("#bucuresti-filaret .text").get(0).html();

                        remoteViews.setTextViewText(R.id.update, Html.fromHtml(text));
                        remoteViews.setTextViewText(R.id.ora, Html.fromHtml(ora));
                        appWidgetManager.updateAppWidget(widgetId, remoteViews);

                        new DownloadImageTask(appWidgetManager, widgetId, remoteViews)
                                .execute(img);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        AppWidgetManager appWidgetManager;
        Integer widgetId;
        RemoteViews remoteViews;

        public DownloadImageTask(AppWidgetManager appWidgetManager, Integer widgetId, RemoteViews remoteViews) {
            this.appWidgetManager = appWidgetManager;
            this.widgetId = widgetId;
            this.remoteViews = remoteViews;
        }

        protected Bitmap doInBackground(String... urls) {
            Bitmap mIcon11 = null;
            SVG svg = null;

            try {
                URL url = new URL(urls[0]);
                InputStream is = url.openStream();
                SVGBuilder builder = new SVGBuilder();
                svg = builder.readFromInputStream(is).build();

                PictureDrawable pd = svg.getDrawable();
                Bitmap bitmap = Bitmap.createBitmap(pd.getIntrinsicWidth(), pd.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawPicture(pd.getPicture());
                mIcon11 = bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            remoteViews.setImageViewBitmap(R.id.img, result);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
