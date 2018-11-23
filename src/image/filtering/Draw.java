/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package image.filtering;

import static image.filtering.Do.label;
import static image.filtering.Do.panel;
import static image.filtering.Do.image;
import static image.filtering.Do.DDA;
import static image.filtering.Do.midPointCircle;
import static image.filtering.Do.antiAliased;
import static image.filtering.Do.thicknessField;
import static image.filtering.Do.superSampling;
import static image.filtering.Do.clipping;
import static image.filtering.Do.rectangle;
import static image.filtering.Do.edgeMake;
import static image.filtering.Do.closePolygon;
import static image.filtering.Do.fillPolygon;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 *
 * @author Habbab
 */
class myDouble {

    double value;

    public myDouble() {
    }

    public myDouble(double value) {
        this.value = value;
    }

}

class Rectangle {

    public double left, right, top, bottom;

    public Rectangle() {
    }

    public Rectangle(double left, double right, double top, double bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

}

class Point {

    double x, y;

    public Point() {
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

}

class Edge {

    Point p1, p2;

    public Edge() {
    }

    public Edge(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

}

class Polygon {

    public ArrayList<Edge> edges = new ArrayList<>();

    public Polygon() {
    }

    public void addEdge(Edge e) {
        edges.add(e);
    }
}

class AET implements Comparator<AET>{

    double ymax, x;
    double mInverse;

    public AET() {
    }

    public AET(double ymax, double x, double mInverse) {
        this.ymax = ymax;
        this.x = x;
        this.mInverse = mInverse;
    }

    @Override
    public int compare(AET t, AET t1) {
        return(int) (Math.ceil(t.x - t1.x));
    }

    
}

public class Draw {

    boolean first = true;
    int x1, x2, y1, y2;
    double thickness = 1;
    Rectangle rectangleClipping = new Rectangle();
    Polygon polygon = new Polygon();
    ArrayList<ArrayList<AET>> bucket = new ArrayList<>();
    { // intialize it with 1000 cells
        for (int i = 0; i < 1000; i++) {
            bucket.add(new ArrayList<>());
        }
    }
    int ymin = 1000000, ymax = 0;
    public Draw() {
        fillPolygon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                scanLine();
            }
        });
        closePolygon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (polygon.edges.size() % 2 == 0) {
                    lineDDA(x1, y1, (int) polygon.edges.get(0).p1.x, (int) polygon.edges.get(0).p1.y);
                    polygon.addEdge(new Edge(new Point(x1, y1), polygon.edges.get(0).p1));
                } else {
                    lineDDA(x2, y2, (int) polygon.edges.get(0).p1.x, (int) polygon.edges.get(0).p1.y);
                    polygon.addEdge(new Edge(new Point(x2, y2), polygon.edges.get(0).p1));
                }
            }
        });
        edgeMake.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                polygon.addEdge(new Edge(new Point(x1, y1), new Point(x2, y2)));
                if (polygon.edges.size() % 2 == 0) {
                    lineDDA(x1, y1, x2, y2);
                } else {
                    lineDDA(x2, y2, x1, y1);
                }
            }
        });
        rectangle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                DrawRectangle();
            }
        });
        clipping.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Clip();
            }
        });
        superSampling.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                superSampling();
            }
        });

        thicknessField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                thickness = Double.parseDouble(thicknessField.getText());
            }
        });
        antiAliased.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ThickAntialiasedLine(2);
            }
        });
        midPointCircle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                midPointCircle();
            }
        });

        DDA.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                lineDDA(x1, y1, x2, y2);
            }
        });
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (first) {
                    x1 = me.getX();
                    y1 = me.getY();
                    System.out.println("x1 = " + x1 + ", y1 = " + y1);
                } else {
                    x2 = me.getX();
                    y2 = me.getY();
                    System.out.println("x2 = " + x2 + ", y2 = " + y2);
                }
                first = !first;
            }
        });
    }

    void scanLine() {
        for (Edge e : polygon.edges) {
            Point p1 = e.p1;
            Point p2 = e.p2;
            double m = (p1.x - p2.x) / (p1.y - p2.y)  ;
            if (p1.y > p2.y) {
                bucket.get((int) p2.y).add(new AET((int) p1.y, (int) p2.x, m));
            } else {
                bucket.get((int) p1.y).add(new AET((int) p2.y, (int) p1.x, m));
            }
            ymin = Math.min(ymin, Math.min((int)p1.y, (int)p2.y));
            ymax = Math.max(ymax, Math.max((int)p1.y, (int)p2.y));
        }
        fillPolygon();
    }

    void fillPolygon(){
        ArrayList<AET> current = new ArrayList<>();
        int y = ymin;
        while (current.size() > 0 || y < ymax){
            // move current edges
            for (AET aet : bucket.get(y)) {
                current.add(aet);
            }
            
            Collections.sort(current, new AET());   // sort according to x
            for (int i = 0; i < current.size() ; ++i){
                if (current.get(i).ymax == y){
                    current.remove(i);
                    --i;
                }
            }
            // fill
            for (int i = 0; i < current.size() - 1; ++i){
                if (i % 2 ==0){ // inside
                    fillPixels((int)current.get(i).x, (int)current.get(i+1).x, y);
                }
            }
            ++y;
            for (AET aet : current) {
                aet.x += aet.mInverse;
            }
        }
        label.updateUI();
    }
    
    void fillPixels(int x1, int x2, int y){
        int black = new Color(0, 0, 0).getRGB();
        for (int i = x1 ; i <= x2 ; ++i){
            image.setRGB(i, y, black);
        }
    }
    void Clip() {
        LiangBarsky(new Point(x1, y1), new Point(x2, y2), rectangleClipping);
    }

    void DrawRectangle() {
        System.out.println("image.filtering.Draw.DrawRectangle()");
        rectangleClipping.bottom = Math.min(y1, y2);
        rectangleClipping.left = Math.min(x1, x2);
        rectangleClipping.right = Math.max(x1, x2);
        rectangleClipping.top = Math.max(y1, y2);

        for (int x = (int) rectangleClipping.left; x <= rectangleClipping.right; ++x) {
            image.setRGB(x, (int) rectangleClipping.bottom, new Color(0, 0, 0).getRGB());
            image.setRGB(x, (int) rectangleClipping.top, new Color(0, 0, 0).getRGB());
        }

        for (int y = (int) rectangleClipping.bottom; y <= rectangleClipping.top; ++y) {
            image.setRGB((int) rectangleClipping.left, y, new Color(0, 0, 0).getRGB());
            image.setRGB((int) rectangleClipping.right, y, new Color(0, 0, 0).getRGB());
        }
        label.updateUI();
    }

    boolean Clip(double denom, double numer, myDouble tE, myDouble tL) {
        if (denom == 0) { //Paralel line
            return numer > 0;
        }
        double t = numer / denom;

        if (denom > 0) { //PE
            if (t < tL.value) {
                tL.value = t;
            }
        } else {
            if (t > tE.value) {
                tE.value = t;
            }
        }
        return tE.value <= tL.value;
    }

    void LiangBarsky(Point p1, Point p2, Rectangle clip) {
        double dx = p2.x - p1.x, dy = p2.y - p1.y;
        myDouble tE = new myDouble(0), tL = new myDouble(1);

        double t = (clip.left - p1.x) / (dx);
        double xx = p1.x + t * dx;
        if (Clip(-dx, p1.x - clip.left, tE, tL)) {
            if (Clip(dx, clip.right - p1.x, tE, tL)) {
                if (Clip(-dy, p1.y - clip.bottom, tE, tL)) {
                    if (Clip(dy, clip.top - p1.y, tE, tL)) {
                        if (tL.value <= 1) {
                            p2.x = p1.x + dx * tL.value;
                            p2.y = p1.y + dy * tL.value;
                        }
                        if (tE.value >= 0) {
                            p1.x += dx * tE.value;
                            p1.y += dy * tE.value;
                        }
                        x1 = (int) p1.x;
                        y1 = (int) p1.y;
                        x2 = (int) p2.x;
                        y2 = (int) p2.y;
                        lineDDA(x1, y1, x2, y2);
                    }
                }
            }
        }
    }

    public double cov(double d, double r) {
        if (d >= r) {
            return 0;
        }
        double res = r * r * (Math.acos(d / r)) - d * Math.sqrt(r * r - d * d);
        double ret = 0.5 - (d * Math.sqrt(r * r - d * d) / (Math.PI * r * r)) - 1 / (Math.PI * Math.asin(d / r));
        if (ret > 1) {
            System.out.println("d = " + d + ", r = " + r);
        }

        return res;
    }

    public double coverage(double distance, double r) {
        double w = thickness / 2;
        if (w >= r) {
            if (w <= distance) {
                double ret = cov(distance - w, r);
                return ret;
            } else {
                double ret = 1 - cov(w - distance, r);
                return ret;
            }
        } else {
            if (w >= distance) {
                return 1 - cov(w - distance, r) - cov(w + distance, r);
            } else if (r - w >= distance) {
                return cov(distance - w, r) - cov(distance + w, r);
            } else {
                return cov(distance - w, r);
            }
        }

    }

    public void superSampling() {
        //lineDDA();

        BufferedImage after = new BufferedImage(image.getWidth() * 2, image.getHeight() * 2, image.getType());

        for (int i = 0; i < after.getHeight(); i++) {
            for (int j = 0; j < after.getWidth(); j++) {
                after.setRGB(j, i, new Color(255, 255, 255).getRGB());  // white
            }
        }

        x1 *= 2;
        y1 *= 2;
        x2 *= 2;
        y2 *= 2;

        double superThickness = thickness * 2;

        int dy = y2 - y1;
        int dx = x2 - x1;
        float m = ((float) dy) / dx;

        int black = new Color(0, 0, 0).getRGB();

        // draw horizontal
        {
            if (x2 < x1) {   // swap
                x1 = x1 ^ x2;
                x2 = x1 ^ x2;
                x1 = x1 ^ x2;

                y1 = y1 ^ y2;
                y2 = y1 ^ y2;
                y1 = y1 ^ y2;
            }
            float y = y1;

            for (int x = x1; x <= x2; x++) {

                for (int i = 0; i <= superThickness / 2; i++) {
                    if (Math.round(y + i) < after.getHeight()) {
                        after.setRGB(x, Math.round(y + i), black);  // copy rows
                    }
                    if (Math.round(y - i) >= 0) {
                        after.setRGB(x, Math.round(y - i), black);  // copy rows
                    }
                }

                y += m;
            }
        }

        // draw vertical
        {
            if (y2 < y1) {   // swap
                x1 = x1 ^ x2;
                x2 = x1 ^ x2;
                x1 = x1 ^ x2;

                y1 = y1 ^ y2;
                y2 = y1 ^ y2;
                y1 = y1 ^ y2;
            }

            float x = x1;
            // X_i+1 = x_i + 1/m
            for (int y = y1; y <= y2; y++) {

                for (int i = 0; i <= superThickness / 2; i++) {
                    if (Math.round(x + i) < after.getHeight()) {
                        after.setRGB(Math.round(x + i), y, black);  // copy rows
                    }
                    if (Math.round(x - i) >= 0) {
                        after.setRGB(Math.round(x - i), y, black);  // copy rows
                    }
                }

                x += 1 / m;
            }
        }

        for (int i = 0; i < after.getHeight(); i += 2) {
            for (int j = 0; j < after.getWidth(); j += 2) {

                int color = 0;
                for (int k = 0; k < 2; k++) {
                    for (int w = 0; w < 2; w++) {
                        color += new Color(after.getRGB(j + w, i + k)).getRed();
                    }
                }

                color /= 4;
                image.setRGB(j / 2, i / 2, new Color(color, color, color).getRGB());

            }
        }

        label.updateUI();

    }

    public void lineDDA(int x1, int y1, int x2, int y2) {

        int dy = y2 - y1;
        int dx = x2 - x1;
        float m = ((float) dy) / dx;

        int black = new Color(0, 0, 0).getRGB();

        if (Math.abs(dy) < Math.abs(dx)) {
            if (x2 < x1) {   // swap
                x1 = x1 ^ x2;
                x2 = x1 ^ x2;
                x1 = x1 ^ x2;

                y1 = y1 ^ y2;
                y2 = y1 ^ y2;
                y1 = y1 ^ y2;
            }
            float y = y1;

            for (int x = x1; x <= x2; x++) {

                for (int i = 0; i <= thickness / 2; i++) {
                    if (Math.round(y + i) < image.getHeight()) {
                        image.setRGB(x, Math.round(y + i), black);  // copy rows
                    }
                    if (Math.round(y - i) >= 0) {
                        image.setRGB(x, Math.round(y - i), black);  // copy rows
                    }
                }

                y += m;
            }
        } else {
            if (y2 < y1) {   // swap
                x1 = x1 ^ x2;
                x2 = x1 ^ x2;
                x1 = x1 ^ x2;

                y1 = y1 ^ y2;
                y2 = y1 ^ y2;
                y1 = y1 ^ y2;
            }

            float x = x1;
            // X_i+1 = x_i + 1/m
            for (int y = y1; y <= y2; y++) {

                for (int i = 0; i <= thickness / 2; i++) {
                    if (Math.round(x + i) < image.getHeight()) {
                        image.setRGB(Math.round(x + i), y, black);  // copy rows
                    }
                    if (Math.round(x - i) >= 0) {
                        image.setRGB(Math.round(x - i), y, black);  // copy rows
                    }
                }

                x += 1 / m;
            }
        }

        label.updateUI();
    }

    public boolean IntensifyPixel(int x, int y, double distance, double r) {
        if (x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight()) {
            return false;
        }

        distance = Math.abs(distance);
        System.out.println("Distance = " + distance);
        double cov = coverage(distance, r);

        if (cov > 0) {
            int lerp = 255 - (int) (255 * cov);
            if (lerp > 255) {
                System.out.println("lerp = " + lerp);
            } else if (lerp < 0) {
                coverage(distance, r);
                System.out.println("cov = " + cov);
                System.out.println("LESS");
            }
            image.setRGB(x, y, new Color(lerp, lerp, lerp).getRGB());
        }
        return cov > 0;
    }

    // use dy instead
    public void ThickAntialiasedLine(double thickness) {

//initial values in Bresenham;s algorithm
        int dx = x2 - x1, dy = y2 - y1;
        int dE = 2 * dy, dNE = 2 * (dy - dx);
        int d = 2 * dy - dx;
        int two_v_dx = 0; //numerator, v=0 for the first pixel
        int two_v_dy = 0;
        double invDenom = 1 / (2 * Math.sqrt(dx * dx + dy * dy)); //inverted denominator
        double two_dx_invDenom = 2 * dx * invDenom; //precomputed constant
        double two_dy_invDenom = 2 * dy * invDenom;

        if (x2 < x1) {   // swap
            x1 = x1 ^ x2;
            x2 = x1 ^ x2;
            x1 = x1 ^ x2;

            y1 = y1 ^ y2;
            y2 = y1 ^ y2;
            y1 = y1 ^ y2;
        }

        int x = x1, y = y1;
        double r = 0.5;
        IntensifyPixel(x, y, 0, r);
        for (int i = 1; IntensifyPixel(x, y + i, i * two_dx_invDenom, r); ++i);
        for (int i = 1; IntensifyPixel(x, y - i, i * two_dx_invDenom, r); ++i);

//        for (int i = 1; IntensifyPixel(x + i, y, thickness, i * two_dx_invDenom, r); ++i);
//        for (int i = 1; IntensifyPixel(x - i, y, thickness, i * two_dx_invDenom, r); ++i);
        while (x < x2) {
            ++x;
            if (d < 0) // move to E
            {
                two_v_dx = d + dx;
                d += dE;
            } else // move to NE
            {
                two_v_dx = d - dx;
                d += dNE;
                ++y;
            }
// Now set the chosen pixel and its neighbors
            IntensifyPixel(x, y, two_v_dx * invDenom, r);
            for (int i = 1; IntensifyPixel(x, y + i, i * two_dx_invDenom - two_v_dx * invDenom, r); ++i);
            for (int i = 1; IntensifyPixel(x, y - i, i * two_dx_invDenom + two_v_dx * invDenom, r); ++i);

//            for (int i = 1; IntensifyPixel(x + i, y, thickness, i * two_dx_invDenom, r); ++i);
//            for (int i = 1; IntensifyPixel(x - i, y, thickness, i * two_dx_invDenom, r); ++i);
        }

        System.out.println("DONE");
        label.updateUI();
    }

    public boolean inRange(int x, int y) {
        return x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight();
    }

    public void midPointCircle() {
        int xCenter = (x1 + x2) / 2;
        int yCenter = (y1 + y2) / 2;
        int R = Math.abs(x1 - x2) / 2;

        System.out.println("x = " + xCenter + ", y = " + yCenter + " R = " + R);

        int d = 1 - R;
        int x = 0;
        int y = R;

        int black = new Color(0, 0, 0).getRGB();

        // draw four points here
        if (inRange(xCenter, yCenter + R)) {
            image.setRGB(xCenter, yCenter + R, black);
        }
        if (inRange(xCenter, yCenter - R)) {
            image.setRGB(xCenter, yCenter - R, black);
        }
        if (inRange(xCenter + R, yCenter)) {
            image.setRGB(xCenter + R, yCenter, black);
        }
        if (inRange(xCenter - R, yCenter)) {
            image.setRGB(xCenter - R, yCenter, black);
        }

        while (y > x) {
            if (d < 0) {
                d += 2 * x + 3;
            } else {
                d += 2 * x - 2 * y + 5;
                --y;
            }
            ++x;

            for (int i = 0; i < thickness && inRange(x + xCenter + i, y + yCenter + i); ++i) {
                image.setRGB(x + xCenter, y + yCenter + i, black);
                image.setRGB(x + xCenter + i, y + yCenter, black);
            }

            for (int i = 0; i < thickness && inRange(x + xCenter + i, yCenter - y + i); ++i) {
                image.setRGB(x + xCenter + i, yCenter - y, black);
                image.setRGB(x + xCenter, yCenter - y + i, black);
            }
            for (int i = 0; i < thickness && inRange(xCenter - x + i, y + yCenter + i); ++i) {
                image.setRGB(xCenter - x + i, y + yCenter, black);
                image.setRGB(xCenter - x, y + yCenter + i, black);
            }

            for (int i = 0; i < thickness && inRange(xCenter - x + i, yCenter - y + i); ++i) {
                image.setRGB(xCenter - x + i, yCenter - y, black);
                image.setRGB(xCenter - x, yCenter - y + i, black);
            }

            for (int i = 0; i < thickness && inRange(xCenter - y + i, yCenter + x + i); ++i) {
                image.setRGB(xCenter - y + i, yCenter + x, black);
                image.setRGB(xCenter - y, yCenter + x + i, black);
            }
            for (int i = 0; i < thickness && inRange(xCenter - y + i, yCenter - x + i); ++i) {
                image.setRGB(xCenter - y + i, yCenter - x, black);
                image.setRGB(xCenter - y, yCenter - x + i, black);

            }
            for (int i = 0; i < thickness && inRange(xCenter + y + i, yCenter + x + i); ++i) {
                image.setRGB(xCenter + y + i, yCenter + x, black);
                image.setRGB(xCenter + y, yCenter + x + i, black);
            }
            for (int i = 0; i < thickness && inRange(xCenter + y + i, yCenter - x + i); ++i) {
                image.setRGB(xCenter + y + i, yCenter - x, black);
                image.setRGB(xCenter + y, yCenter - x + i, black);
            }

        }
        label.updateUI();
    }

}
