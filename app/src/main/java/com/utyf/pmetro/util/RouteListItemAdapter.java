package com.utyf.pmetro.util;import android.content.Context;import android.graphics.Canvas;import android.graphics.ColorFilter;import android.graphics.Paint;import android.graphics.PixelFormat;import android.graphics.Rect;import android.graphics.drawable.Drawable;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.ArrayAdapter;import android.widget.ImageView;import android.widget.LinearLayout;import android.widget.TextView;import com.utyf.pmetro.R;import com.utyf.pmetro.map.Route;/** * Created by Fedor on 10.04.2016. * * Adapter for displaying a route in the list of alternative routes */public class RouteListItemAdapter extends ArrayAdapter<Route> {    int resource;    Context context;    public RouteListItemAdapter(Context context, int resource, Route[] objects) {        super(context, resource, objects);        this.context = context;        this.resource = resource;    }    @Override    public View getView(int position, View convertView, ViewGroup parent) {        Route route = getItem(position);        LinearLayout view;        if (convertView == null) {            LayoutInflater inflater = LayoutInflater.from(context);            view = (LinearLayout)inflater.inflate(resource, null);        }        else {            view = (LinearLayout)convertView;            // Remove all children except TextView            view.removeViewsInLayout(1, view.getChildCount() - 1);        }        TextView textView = (TextView)view.findViewById(R.id.textView3);        int minutes = Math.round(route.time);        String time;        if (minutes <= 60) {            time = String.format("%d", minutes);        } else {            int hours = minutes / 60;            minutes %= 60;            time = String.format("%d:%02d", hours, minutes);        }        String text = String.format("%s", time);        textView.setText(text);        int[] lineColors = route.getRoutePartColors();        for (int lineColor : lineColors) {            LayoutInflater inflater = LayoutInflater.from(context);            ImageView imageView = (ImageView) inflater.inflate(R.layout.metro_line_icon, view, false);            imageView.setImageDrawable(new LineIcon(lineColor));            view.addView(imageView);        }        return view;    }    // todo: circle denotes icon of a station, but icon of a metro line is needed    class LineIcon extends Drawable {        private final Paint mPaint;        public LineIcon(int clr) {            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);            mPaint.setStyle(Paint.Style.FILL);            mPaint.setColor(0xff000000 | clr);        }        @Override        public void draw(Canvas canvas) {            Rect b = getBounds();            float x = b.width() /2;            float y = b.height() /2;            canvas.drawCircle(x, y, (x+y)*0.3f, mPaint);        }        @Override        public void setAlpha(int alpha) {}        @Override        public void setColorFilter(ColorFilter cf) {}        @Override        public int getOpacity() { return PixelFormat.TRANSLUCENT; }    }}