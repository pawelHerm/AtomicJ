
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.gui;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public class ShapeSupplier  
{
    private static final float[][] STAR_COORDINATES = {{0.606253f, 0.220658f}, {0.919963f, 1.3639f}, {-0.0225158f, 
        0.644768f}, {-1.01286f, 1.2964f}, {-0.620169f, 
            0.177831f}, {-1.54595f, -0.562678f}, {-0.36077f, -0.534863f}, 
            {0.0574153f, -1.64416f}, {0.397201f, -0.508394f}, {1.58143f, -0.453468f}};

    private static final float HEART_X0 = 0f;
    private static final float HEART_Y0 = 1.f;
    private static final float[][] HEART_COORDINATES = {{0f,0.608f,0.874f,0.377f,0.925f,-0.418f},{.853f,-1.09f, 0.f, -1.21f, 0f, -0.358f},
            {0,-1.121f,-0.853f,-1.09f,-0.925f,-0.418f},
            {-0.874f,0.377f,0f,0.608f,0f,1f}};


    public static boolean isCurved(int index)
    {
        if(index > 10)
        {
            return true;
        }
        if(index == 1|| index == 6)
        {
            return true;
        }
        return false;
    }

    public static Shape createShape(int shapeNumber, float size) 
    {
        float delta = size / 2.f;
        int deltaInt = (int)Math.rint(delta);
        int[] xpoints;
        int[] ypoints;

        Shape result;

        switch(shapeNumber)
        {
        case 0: 
        {
            result = new Rectangle2D.Float(-delta, -delta, size, size);//square 
            break;
        }
        case 1: 
        {
            result = new Ellipse2D.Float(-delta, -delta, size, size);// circle
            break;
        }
        case 2: 
        {
            xpoints = new int[] {0, deltaInt, -deltaInt};// up-pointing triangle
            ypoints = new int[] {-deltaInt, deltaInt, deltaInt};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 3: 
        {
            xpoints = new int[] {0, deltaInt, 0, -deltaInt};// diamond
            ypoints = new int[] {-deltaInt, 0, deltaInt, 0};
            result = new Polygon(xpoints, ypoints, 4);
            break;
        }
        case 4: 
        {
            result = new Rectangle2D.Float(-delta, -delta / 2, size, size / 2);// horizontal rectangle
            break;
        }
        case 5: 
        {
            xpoints = new int[] {-deltaInt, deltaInt, 0};// down-pointing triangle
            ypoints = new int[] {-deltaInt, -deltaInt, deltaInt};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 6: 
        {
            result = new Ellipse2D.Float(-delta, -delta / 2, size, size / 2);// horizontal ellipse
            break;
        }
        case 7: 
        {
            xpoints = new int[] {-deltaInt, deltaInt, -deltaInt};// right-pointing triangle
            ypoints = new int[] {-deltaInt, 0, deltaInt};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 8: 
        {
            result = new Rectangle2D.Float(-delta / 2, -delta, size / 2, size);// vertical rectangle
            break;
        }
        case 9: 
        {
            xpoints = new int[] {-deltaInt, deltaInt, deltaInt};// left-pointing triangle
            ypoints = new int[] {0, -deltaInt, deltaInt};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 10: 
        {
            Path2D star = new GeneralPath();
            int n = STAR_COORDINATES.length;
            star.moveTo(delta*STAR_COORDINATES[0][0], delta*STAR_COORDINATES[0][1]);

            for(int i = 1; i<n; i++)
            {
                float[] p = STAR_COORDINATES[i];
                float x = delta*p[0];
                float y = delta*p[1];
                star.lineTo(x,y);
            }
            result = star;
            break;
        }
        case 11:
        {
            Path2D heart = new GeneralPath();
            int n = HEART_COORDINATES.length;
            heart.moveTo(delta*HEART_X0, delta*HEART_Y0);

            for(int i = 0; i<n; i++)
            {
                float[] p = HEART_COORDINATES[i];
                float x1 = delta*p[0];
                float y1 = delta*p[1];
                float x2 = delta*p[2];
                float y2 = delta*p[3];
                float x3 = delta*p[4];
                float y3 = delta*p[5];
                heart.curveTo(x1,y1,x2,y2,x3,y3);
            }
            heart.closePath();
            result = heart;
            break;
        }
        case 12:
        {
            GeneralPath cross = new GeneralPath();

            cross.moveTo(0,-delta);
            cross.lineTo(0, delta);

            cross.moveTo(-delta, 0);
            cross.lineTo(delta, 0);

            result = cross;
            break;
        }
        case 13:
        {
            GeneralPath ex = new GeneralPath();

            ex.moveTo(-delta,-delta);
            ex.lineTo(delta, delta);

            ex.moveTo(delta,-delta);
            ex.lineTo(-delta,delta);

            result = ex;
            break;
        }
        case 14:
        {
            GeneralPath starThin = new GeneralPath();

            for(int i = 0; i<4;i++)
            {
                double angle = i*45*Math.PI/180.;
                starThin.moveTo(-delta*Math.cos(angle), -delta*Math.sin(angle));
                starThin.lineTo(delta*Math.cos(angle), delta*Math.sin(angle));
            }

            result = starThin;
            break;

        }
        case 15:
        {
            GeneralPath starThick = new GeneralPath();

            for(int i = 0; i<6;i++)
            {
                double angle = i*30*Math.PI/180.;
                starThick.moveTo(-delta*Math.cos(angle), -delta*Math.sin(angle));
                starThick.lineTo(delta*Math.cos(angle), delta*Math.sin(angle));
            }

            result = starThick;
            break;
        }
        default: 
        {
            result = new Ellipse2D.Float(-delta, -delta, size, size);
        }
        }

        return result;
    }

    public static Shape createShape(int shapeNumber, float width, float height) 
    {
        int[] xpoints;
        int[] ypoints;

        int widthInt = (int)Math.rint(width);
        int heightInt = (int)Math.rint(height);

        Shape result;

        switch(shapeNumber)
        {
        case 0: 
        {
            result = new Rectangle2D.Float(-width/2.f, -height/ 2.f, width, height);//square TO CHECK !!!!
            break;
        }
        case 1: 
        {
            result = new Ellipse2D.Float(-width/2.f, -height / 2.f, width, height);
            break;
        }
        case 2: 
        {
            xpoints = new int[] {0, widthInt/2, -widthInt/2};// up-pointing triangle
            ypoints = new int[] {-heightInt/2, heightInt/2, heightInt/2};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 3: 
        {
            xpoints = new int[] {0, widthInt/2, 0, -widthInt/2};// diamond
            ypoints = new int[] {-heightInt/2, 0, heightInt/2, 0};
            result = new Polygon(xpoints, ypoints, 4);
            break;
        }
        case 4: 
        {
            result = new Rectangle2D.Float(-width/2.f, -height / 2.f, width, height);// horizontal rectangle
            break;
        }
        case 5: 
        {
            xpoints = new int[] {-widthInt/2, widthInt/2, 0};// down-pointing triangle
            ypoints = new int[] {-heightInt/2, -heightInt/2, heightInt/2};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 6: 
        {
            result = new Ellipse2D.Float(-width/2.f, -height / 2.f, width, height);
            break;
        }
        case 7: 
        {
            xpoints = new int[] {-widthInt/2, widthInt/2, -widthInt/2};
            ypoints = new int[] {-heightInt/2, 0, heightInt/2};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 8: 
        {
            result = new Rectangle2D.Float(-width / 2.f, -height/2.f, width, height);
            break;
        }
        case 9: 
        {
            xpoints = new int[] {-widthInt/2, widthInt/2, widthInt/2};
            ypoints = new int[] {0, -heightInt/2, heightInt/2};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 10:
        {
            Path2D star = new GeneralPath();
            int n = STAR_COORDINATES.length;
            star.moveTo(0.5*width*STAR_COORDINATES[0][0], 0.5*height*STAR_COORDINATES[0][1]);

            for(int i = 1; i<n; i++)
            {
                float[] p = STAR_COORDINATES[i];
                float x = 0.5f*width*p[0];
                float y = 0.5f*height*p[1];
                star.lineTo(x,y);
            }
            result = star;
            break;
        }
        case 11:
        {
            Path2D heart = new GeneralPath();
            int n = HEART_COORDINATES.length;
            heart.moveTo(0.5*width*HEART_X0, 0.5*height*HEART_Y0);

            for(int i = 0; i<n; i++)
            {
                float[] p = HEART_COORDINATES[i];
                float x1 = 0.5f*width*p[0];
                float y1 = 0.5f*height*p[1];
                float x2 = 0.5f*width*p[2];
                float y2 = 0.5f*height*p[3];
                float x3 = 0.5f*width*p[4];
                float y3 = 0.5f*height*p[5];
                heart.curveTo(x1,y1,x2,y2,x3,y3);
            }
            heart.closePath();
            result = heart;
            break;
        }

        default: 
        {
            result = new Ellipse2D.Float(-width/2, -height/2, width, height);
        }
        }

        return result;
    }

    public static Shape createShape(int shapeNumber, float xCenter, float yCenter, float size) 
    {
        return createShape(shapeNumber, xCenter, yCenter, size, size);
    }

    public static Shape createShape(int shapeNumber, float xCenter, float yCenter, float width, float height) 
    {
        int[] xpoints;
        int[] ypoints;

        int widthInt = (int)Math.rint(width);
        int heightInt = (int)Math.rint(height);
        int xCenterInt = (int)xCenter;
        int yCenterInt = (int)yCenter;

        Shape result;

        switch(shapeNumber)
        {
        case 0: 
        {
            result = new Rectangle2D.Float(xCenter - width/2.f,yCenter - height/ 2.f, width, height);//square TO CHECK !!!!
            break;
        }
        case 1: 
        {
            result = new Ellipse2D.Float(xCenter - width/2.f, yCenter - height / 2.f, width, height);
            break;
        }
        case 2: 
        {
            xpoints = new int[] {xCenterInt, xCenterInt + widthInt/2,xCenterInt - widthInt/2};// up-pointing triangle
            ypoints = new int[] {yCenterInt-heightInt/2,yCenterInt + heightInt/2,yCenterInt + heightInt/2};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 3: 
        {
            xpoints = new int[] {xCenterInt,xCenterInt + widthInt/2, xCenterInt, xCenterInt - widthInt/2};// diamond
            ypoints = new int[] {yCenterInt-heightInt/2, yCenterInt,yCenterInt + heightInt/2, yCenterInt};
            result = new Polygon(xpoints, ypoints, 4);
            break;
        }
        case 4: 
        {
            result = new Rectangle2D.Float(xCenter - width/2, yCenter - height / 2, width, height);// horizontal rectangle
            break;
        }
        case 5: 
        {
            xpoints = new int[] {xCenterInt - widthInt/2,xCenterInt + widthInt/2, xCenterInt};// down-pointing triangle
            ypoints = new int[] {yCenterInt -heightInt/2, yCenterInt -heightInt/2, yCenterInt+ heightInt/2};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 6: 
        {
            result = new Ellipse2D.Float(xCenter - width/2.f, yCenter - height / 2.f, width, height);
            break;
        }
        case 7: 
        {
            xpoints = new int[] {xCenterInt-widthInt/2,xCenterInt +  widthInt/2, xCenterInt-widthInt/2};
            ypoints = new int[] {yCenterInt -heightInt/2, yCenterInt,yCenterInt + heightInt/2};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 8: 
        {
            result = new Rectangle2D.Float(xCenter - width / 2.f, yCenter - height/2.f, width, height);
            break;
        }
        case 9: 
        {
            xpoints = new int[] {xCenterInt-widthInt/2,xCenterInt + widthInt/2,xCenterInt + widthInt/2};
            ypoints = new int[] {yCenterInt,yCenterInt -heightInt/2,yCenterInt + heightInt/2};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 10:
        {
            Path2D star = new GeneralPath();
            int n = STAR_COORDINATES.length;
            star.moveTo(0.5*width*STAR_COORDINATES[0][0] + xCenter, 0.5*height*STAR_COORDINATES[0][1] + yCenter);

            for(int i = 1; i<n; i++)
            {
                float[] p = STAR_COORDINATES[i];
                float x = 0.5f*width*p[0] + xCenter;
                float y = 0.5f*height*p[1] + yCenter;
                star.lineTo(x,y);
            }
            result = star;
            break;
        }
        case 11:
        {
            Path2D heart = new GeneralPath();
            int n = HEART_COORDINATES.length;
            heart.moveTo(0.5*width*HEART_X0 + xCenter, 0.5*height*HEART_Y0 + yCenter);

            for(int i = 0; i<n; i++)
            {
                float[] p = HEART_COORDINATES[i];
                float x1 = 0.5f*width*p[0] + xCenter;
                float y1 = 0.5f*height*p[1] + yCenter;
                float x2 = 0.5f*width*p[2] + xCenter;
                float y2 = 0.5f*height*p[3] + yCenter;
                float x3 = 0.5f*width*p[4] + xCenter;
                float y3 = 0.5f*height*p[5] + yCenter;
                heart.curveTo(x1,y1,x2,y2,x3,y3);
            }
            heart.closePath();
            result = heart;
            break;
        }

        default: 
        {
            result = new Ellipse2D.Float(xCenter - width/2.f, yCenter - height/2.f, width, height);
        }
        }

        return result;
    }

    public static Shape createIntrShape(int shapeNumber, float xCenter, float yCenter, float width, float height) 
    {
        int[] xpoints;
        int[] ypoints;

        int widthInt = (int)Math.rint(width);
        int heightInt = (int)Math.rint(height);
        int halfWidth = (int)Math.ceil(width/2.);
        int halfHeight = (int)Math.ceil(height/2.);
        int xCenterInt = (int)xCenter;
        int yCenterInt = (int)yCenter;

        Shape result;

        switch(shapeNumber)
        {
        case 0: 
        {
            result = new Rectangle2D.Float(xCenterInt - halfWidth,yCenterInt - halfHeight, 2*halfWidth, 2*halfHeight);//square TO CHECK !!!!
            break;
        }
        case 1: 
        {
            result = new Ellipse2D.Float(xCenterInt - halfWidth, yCenter - halfHeight, 2*halfWidth, 2*halfHeight);
            break;
        }
        case 2: 
        {
            xpoints = new int[] {xCenterInt, xCenterInt + widthInt/2,xCenterInt - widthInt/2};// up-pointing triangle
            ypoints = new int[] {yCenterInt-heightInt/2,yCenterInt + heightInt/2,yCenterInt + heightInt/2};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 3: 
        {
            xpoints = new int[] {xCenterInt,xCenterInt + widthInt/2, xCenterInt, xCenterInt - widthInt/2};// diamond
            ypoints = new int[] {yCenterInt-heightInt/2, yCenterInt,yCenterInt + heightInt/2, yCenterInt};
            result = new Polygon(xpoints, ypoints, 4);
            break;
        }
        case 4: 
        {
            result = new Rectangle2D.Float(xCenter - width/2, yCenter - halfHeight, width, height);// horizontal rectangle
            break;
        }
        case 5: 
        {
            xpoints = new int[] {xCenterInt - widthInt/2,xCenterInt + widthInt/2, xCenterInt};// down-pointing triangle
            ypoints = new int[] {yCenterInt -heightInt/2, yCenterInt -heightInt/2, yCenterInt+ halfHeight};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 6: 
        {
            result = new Ellipse2D.Float(xCenter - width/2.f, yCenter - halfHeight, width, height);
            break;
        }
        case 7: 
        {
            xpoints = new int[] {xCenterInt-widthInt/2,xCenterInt +  widthInt/2, xCenterInt-widthInt/2};
            ypoints = new int[] {yCenterInt -heightInt/2, yCenterInt,yCenterInt + heightInt/2};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 8: 
        {
            result = new Rectangle2D.Float(xCenter - width / 2.f, yCenter - height/2.f, width, height);
            break;
        }
        case 9: 
        {
            xpoints = new int[] {xCenterInt-widthInt/2,xCenterInt + widthInt/2,xCenterInt + widthInt/2};
            ypoints = new int[] {yCenterInt,yCenterInt -heightInt/2,yCenterInt + heightInt/2};
            result = new Polygon(xpoints, ypoints, 3);
            break;
        }
        case 10:
        {
            Path2D star = new GeneralPath();
            int n = STAR_COORDINATES.length;
            star.moveTo(0.5f*width*STAR_COORDINATES[0][0] + xCenter, 0.5f*height*STAR_COORDINATES[0][1] + yCenter);

            for(int i = 1; i<n; i++)
            {
                float[] p = STAR_COORDINATES[i];
                float x = 0.5f*width*p[0] + xCenter;
                float y = 0.5f*height*p[1] + yCenter;
                star.lineTo(x,y);
            }
            result = star;
            break;
        }
        case 11:
        {
            Path2D heart = new GeneralPath();
            int n = HEART_COORDINATES.length;
            heart.moveTo(0.5f*width*HEART_X0 + xCenter, 0.5f*height*HEART_Y0 + yCenter);

            for(int i = 0; i<n; i++)
            {
                float[] p = HEART_COORDINATES[i];
                float x1 = 0.5f*width*p[0] + xCenter;
                float y1 = 0.5f*height*p[1] + yCenter;
                float x2 = 0.5f*width*p[2] + xCenter;
                float y2 = 0.5f*height*p[3] + yCenter;
                float x3 = 0.5f*width*p[4] + xCenter;
                float y3 = 0.5f*height*p[5] + yCenter;
                heart.curveTo(x1,y1,x2,y2,x3,y3);
            }
            heart.closePath();
            result = heart;
            break;
        }
        default: 
        {
            result = new Ellipse2D.Float(xCenter - width/2.f, yCenter - height/2.f, width, height);
        }
        }

        return result;
    }
}
