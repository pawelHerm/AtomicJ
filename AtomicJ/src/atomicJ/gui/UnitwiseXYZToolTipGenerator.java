package atomicJ.gui;

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2007, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * --------------------------------
 * StandardXYZToolTipGenerator.java
 * --------------------------------
 * (C) Copyright 2004-2007, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 11-May-2003 : Version 1, split from StandardXYZItemLabelGenerator (DG);
 * 15-Jul-2004 : Switched getZ() and getZValue() methods (DG);
 *
 */


import java.io.Serializable;
import java.text.MessageFormat;
import java.text.NumberFormat;

import org.jfree.chart.labels.XYZToolTipGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

/**
 * A standard item label generator for use with {@link XYZDataset} data.  Each 
 * value can be formatted as a number or as a date.
 */
public class UnitwiseXYZToolTipGenerator implements XYZToolTipGenerator,
Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -2961577421889473503L;

    /** The default tooltip format. */
    public static final String DEFAULT_TOOL_TIP_FORMAT = "{0}: ({1}, {2}, {3})";

    private String formatString;   

    private final NumberFormat xFormat;  
    private final NumberFormat yFormat;
    private final NumberFormat zFormat;

    /** The string used to represent 'null' for the y-value. */
    private final String nullYString = "null";
    /**
     * Creates a new tool tip generator using default number formatters for the
     * x, y and z-values.
     */
    public UnitwiseXYZToolTipGenerator() {
        this(
                DEFAULT_TOOL_TIP_FORMAT,
                NumberFormat.getNumberInstance(),
                NumberFormat.getNumberInstance(),
                NumberFormat.getNumberInstance()
                );
    }

    /**
     * Constructs a new tool tip generator using the specified number 
     * formatters.
     *
     * @param formatString  the format string.
     * @param xFormat  the format object for the x values (<code>null</code> 
     *                 not permitted).
     * @param yFormat  the format object for the y values (<code>null</code> 
     *                 not permitted).
     * @param zFormat  the format object for the z values (<code>null</code> 
     *                 not permitted).
     */
    public UnitwiseXYZToolTipGenerator(String formatString, NumberFormat xFormat,
            NumberFormat yFormat, NumberFormat zFormat) 
    {       
        if (xFormat == null) {
            throw new IllegalArgumentException("Null 'xFormat' argument.");   
        }
        if (yFormat == null) {
            throw new IllegalArgumentException("Null 'yFormat' argument.");   
        }
        if (zFormat == null) {
            throw new IllegalArgumentException("Null 'zFormat' argument.");   
        }

        this.xFormat = xFormat;
        this.yFormat = yFormat;
        this.zFormat = zFormat;
    }

    public String getFormatString() {
        return this.formatString;
    }


    public NumberFormat getXFormat() {
        return this.xFormat;
    }

    public NumberFormat getYFormat() {
        return this.yFormat;
    }

    public NumberFormat getZFormat() {
        return this.zFormat;
    }

    @Override
    public String generateToolTip(XYZDataset dataset, int series, int item) {
        String result = null;    
        Object[] items = createItemArray(dataset, series, item);
        result = MessageFormat.format(getFormatString(), items);
        return result;
    }

    protected Object[] createItemArray(XYZDataset dataset, int series, int item) 
    {
        Object[] result = new Object[4];
        result[0] = dataset.getSeriesKey(series).toString();

        double x = dataset.getXValue(series, item);
        result[1] = this.xFormat.format(x);


        double y = dataset.getYValue(series, item);
        if (Double.isNaN(y) && dataset.getY(series, item) == null) 
        {
            result[2] = this.nullYString;
        }
        else 
        {
            result[2] = this.yFormat.format(y);
        }

        Number z = dataset.getZ(series, item);
        result[3] = this.zFormat.format(z);   

        return result;
    }

    @Override
    public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return null;
    }
}
