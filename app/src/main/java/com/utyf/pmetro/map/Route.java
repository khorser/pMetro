package com.utyf.pmetro.map;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.utyf.pmetro.Map_View;
import com.utyf.pmetro.settings.SET;
import com.utyf.pmetro.util.ExtPath;
import com.utyf.pmetro.util.ExtPointF;
import com.utyf.pmetro.util.StationsNum;

import java.util.ArrayList;

/**
 * Created by Utyf on 22.03.2015.
 *
 */

public class Route {

    public int   numTransfers;
    public float timeDiff;
    ArrayList<RouteNode> nodes;

    public Route() {}

    public Route(Route rt) {
        numTransfers = rt.numTransfers;
        nodes = new ArrayList<>(rt.nodes);
    }

    public boolean addNode(RouteNode rn) {
        float tm1, tm2, delDelta;
        if( nodes==null )  nodes = new ArrayList<>();

        delDelta = TRP.getLine(rn.trp,rn.line).delays.get();  // for remove one extra transfer time

        tm1=TRP.rt.toEnd.getTime(rn);    //trps[rn.trp].lines[rn.line].stns[rn.stn];
        tm2=TRP.rt.toEnd.getTime(TRP.routeStart); //trps[start.trp].lines[start.line].stns[start.stn];
        if( tm1==-1 || tm2==-1 )    return false;  // no route to node
        if( rn.delay==0 )  timeDiff = (tm1+rn.time-tm2)-delDelta; // remove one delay on a non transit stations
        else               timeDiff = tm1+rn.time-tm2;
        if( timeDiff > SET.rDif )   return false;  // expected time - best time > 10 min -> drop it

        for( RouteNode rn2 : nodes )  // do not add visited station
            if( rn.direction==rn2.direction && rn.isEqual(rn2) ) return false;

        if( !nodes.isEmpty() && (getLast().trp!=rn.trp || getLast().line!=rn.line) )
            if( ++numTransfers > SET.maxTransfer ) return false;

        //for( Route rr2 : TRP.routes )  // check all routes is there better or the same
        //    for( RouteNode rn2 : rr2.nodes )
        //        if( rn.isEqual( rn2 ) )
        //            if( rn.time+rn.delay >= rn2.time+rn2.delay && rn.direction==rn2.direction ) return false;

        nodes.add(rn);
        return true;
    }

    public RouteNode getLast() {
        if( nodes==null || nodes.isEmpty() ) return null;
        return nodes.get(nodes.size()-1);
    }

    /////     data for drawing
    private class DrawTransfer {
        PointF stn1,stn2;
        float  width;
    }
    private class RoutePart {
        float width;
        int color;
        Line line;
        ExtPath pth;
        PointF[] pnts;
        StationsNum[] stNums;  // todo  change to int and store only station num
    }
    private DrawTransfer[] trns;  // for Draw
    private RoutePart[] routeParts;
    private float mapScale, radius;

    void makePath() {    // Make data for draw route
        PointF       pnt1, pnt2=null;
        RouteNode    rn1,  rn2=null;
        RoutePart    rPrt=new RoutePart();
        DrawTransfer tt;
        TRP.Transfer TRPtr;
        ArrayList<RoutePart>  rPrts = new ArrayList<>();
        ArrayList<DrawTransfer> tts = new ArrayList<>();
        ArrayList<PointF>  pnts = new ArrayList<>();  // coordinates of stations in Part
        ArrayList<StationsNum>  stNums = new ArrayList<>();  // numbers of stations in Part

        mapScale = MapData.map.scale;
        radius = MapData.map.StationDiameter/2;

        for( int i=0; i<nodes.size(); i++ ) {
            Line  ll;
            rn1 = nodes.get(i);

            ll = MapData.map.getLine(rn1.trp, rn1.line);
            if( ll==null )    pnt1 = null;
            else      pnt1 = ll.getCoord( rn1.stn );

            if( !ExtPointF.isNull(pnt1) ) {                  // point exist - draw it
                if( ExtPointF.isNull(pnt2) || rn2.trp != rn1.trp || rn2.line != rn1.line ) { // start new route part.
                    if( pnts.size()>0 ) {                    // save previous route part
                        rPrt.pnts   = pnts.toArray(new PointF[pnts.size()]);
                        rPrt.stNums = stNums.toArray(new StationsNum[stNums.size()]);
                        rPrts.add(rPrt);

                        if( rn2!=null ) {
                            TRPtr = TRP.getTransfer(rn1, rn2);
                            if( TRPtr != null && !TRPtr.invisible && TRPtr.isCorrect()
                                    && !ExtPointF.isNull(pnt1) && !ExtPointF.isNull(pnt2) ) {
                                tt = new DrawTransfer();
                                tt.stn1 = pnt1;
                                tt.stn2 = pnt2;
                                tt.width = MapData.map.LinesWidth;
                                tts.add(tt);
                            }
                        }
                    }
                    rPrt = new RoutePart();
                    rPrt.pth = new ExtPath();
                    rPrt.line  = MapData.map.getLine(rn1.trp, rn1.line);
                    rPrt.color = rPrt.line.Color;
                    rPrt.width = rPrt.line.LinesWidth;
                    pnts.clear();
                    stNums.clear();
                }

                if( !ExtPointF.isNull(pnt2) && rn2.trp == rn1.trp && rn2.line == rn1.line )
                    ll.PathTo(rn2.stn, rn1.stn, rPrt.pth);

                pnts.add(pnt1);
                stNums.add(rn1);
            }
            rn2 = rn1;
            pnt2 = pnt1;
        }
        rPrt.pnts   = pnts.toArray( new PointF[pnts.size()] );          // add last part
        rPrt.stNums = stNums.toArray( new StationsNum[stNums.size()] ); // add last part
        rPrts.add(rPrt);

        trns = tts.toArray( new DrawTransfer[tts.size()] );
        routeParts = rPrts.toArray( new RoutePart[rPrts.size()] );
    }

    private Paint p2;
    public void Draw(Canvas c, Paint p) {
        if( p2==null ) p2 = new Paint(p);
        p.setStyle( Paint.Style.STROKE );
        p2.setStyle( Paint.Style.FILL );
        p2.setColor( 0xffffff00 );

        for( RoutePart prt : routeParts ) { // draw paths
            p.setColor( prt.color );
            p.setStrokeWidth( prt.width );
            if( prt.pth!=null )  c.drawPath( prt.pth, p );

            for( PointF pnt : prt.pnts) // draw yellow stations circle
                c.drawCircle(pnt.x, pnt.y, radius+mapScale, p2);
        }

        PointF pn1, pn2;
        p.setColor(0xff000000);  // draw black transfer edging
        p.setStrokeWidth( MapData.map.LinesWidth+6*mapScale );   // NPE here
        p2.setColor(0xff000000);
        float rr = radius+3*mapScale;
        for( DrawTransfer trn : trns ) {
            pn1 = trn.stn1;
            pn2 = trn.stn2;
            c.drawCircle(pn1.x, pn1.y, rr, p2);
            c.drawCircle(pn2.x, pn2.y, rr, p2);
            c.drawLine( pn1.x,pn1.y, pn2.x,pn2.y, p);
        }

        p.setColor(0xffffffff);  // draw white transfer edging
        p.setStrokeWidth(MapData.map.LinesWidth+4*mapScale);
        p2.setColor(0xffffffff);
        rr = radius+2*mapScale;
        for( DrawTransfer trn : trns ) {
            pn1 = trn.stn1;
            pn2 = trn.stn2;
            c.drawCircle(pn1.x, pn1.y, rr, p2);
            c.drawCircle(pn2.x, pn2.y, rr, p2);
            c.drawLine( pn1.x,pn1.y, pn2.x,pn2.y, p);
        }

        PointF pnt;
        for( RoutePart prt : routeParts) {
            if( prt.pnts.length==0 ) continue;
            p2.setColor(prt.color); // draw stations circle
            for( int i=0; i<prt.pnts.length; i++ ) {
                pnt = prt.pnts[i];
                c.drawCircle(pnt.x, pnt.y, radius, p2);
                prt.line.drawText(c,prt.stNums[i].stn);
            }

        }
    }
}