
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


/*
 * This class is a modification of ChartPanel class, written by David Gilbert and other contributors, which is 
 * part of JFreeChart 1.0.14 library. The differences between original source code of ChartPanel class and
 * this class are due to Pawel Hermanowicz
 * The source code of the original ChartPanel class can be found at http://www.jfree.org/jfreechart/api/javadoc/index.html
 *
 * Below the original copyright statement from ChartPanel class:
 */

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2011, by Object Refinery Limited and Contributors.
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
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * ChartPanel.java
 * ---------------
 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Andrzej Porebski;
 *                   Soren Caspersen;
 *                   Jonathan Nash;
 *                   Hans-Jurgen Greiner;
 *                   Andreas Schneider;
 *                   Daniel van Enckevort;
 *                   David M O'Donnell;
 *                   Arnaud Lelievre;
 *                   Matthias Rose;
 *                   Onno vd Akker;
 *                   Sergei Ivanov;
 *                   Ulrich Voigt - patch 2686040;
 *                   Alessandro Borges - patch 1460845;
 *                   Martin Hoeller;
 *
 * Changes (from 28-Jun-2001)
 * --------------------------
 * 28-Jun-2001 : Integrated buffering code contributed by S???ren
 *               Caspersen (DG);
 * 18-Sep-2001 : Updated header and fixed DOS encoding problem (DG);
 * 22-Nov-2001 : Added scaling to improve display of charts in small sizes (DG);
 * 26-Nov-2001 : Added property editing, saving and printing (DG);
 * 11-Dec-2001 : Transferred saveChartAsPNG method to new ChartUtilities
 *               class (DG);
 * 13-Dec-2001 : Added tooltips (DG);
 * 16-Jan-2002 : Added an optional crosshair, based on the implementation by
 *               Jonathan Nash. Renamed the tooltips class (DG);
 * 23-Jan-2002 : Implemented zooming based on code by Hans-Jurgen Greiner (DG);
 * 05-Feb-2002 : Improved tooltips setup.  Renamed method attemptSaveAs()
 *               --> doSaveAs() and made it public rather than private (DG);
 * 28-Mar-2002 : Added a new constructor (DG);
 * 09-Apr-2002 : Changed initialisation of tooltip generation, as suggested by
 *               Hans-Jurgen Greiner (DG);
 * 27-May-2002 : New interactive zooming methods based on code by Hans-Jurgen
 *               Greiner. Renamed JFreeChartPanel --> ChartPanel, moved
 *               constants to ChartPanelConstants interface (DG);
 * 31-May-2002 : Fixed a bug with interactive zooming and added a way to
 *               control if the zoom rectangle is filled in or drawn as an
 *               outline. A mouse drag gesture towards the top left now causes
 *               an autoRangeBoth() and is a way to undo zooms (AS);
 * 11-Jun-2002 : Reinstated handleClick method call in mouseClicked() to get
 *               crosshairs working again (DG);
 * 13-Jun-2002 : Added check for null popup menu in mouseDragged method (DG);
 * 18-Jun-2002 : Added get/set methods for minimum and maximum chart
 *               dimensions (DG);
 * 25-Jun-2002 : Removed redundant code (DG);
 * 27-Aug-2002 : Added get/set methods for popup menu (DG);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 22-Oct-2002 : Added translation methods for screen <--> Java2D, contributed
 *               by Daniel van Enckevort (DG);
 * 05-Nov-2002 : Added a chart reference to the ChartMouseEvent class (DG);
 * 22-Nov-2002 : Added test in zoom method for inverted axes, supplied by
 *               David M O'Donnell (DG);
 * 14-Jan-2003 : Implemented ChartProgressListener interface (DG);
 * 14-Feb-2003 : Removed deprecated setGenerateTooltips method (DG);
 * 12-Mar-2003 : Added option to enforce filename extension (see bug id
 *               643173) (DG);
 * 08-Sep-2003 : Added internationalization via use of properties
 *               resourceBundle (RFE 690236) (AL);
 * 18-Sep-2003 : Added getScaleX() and getScaleY() methods (protected) as
 *               requested by Irv Thomae (DG);
 * 12-Nov-2003 : Added zooming support for the FastScatterPlot class (DG);
 * 24-Nov-2003 : Minor Javadoc updates (DG);
 * 04-Dec-2003 : Added anchor point for crosshair calculation (DG);
 * 17-Jan-2004 : Added new methods to set tooltip delays to be used in this
 *               chart panel. Refer to patch 877565 (MR);
 * 02-Feb-2004 : Fixed bug in zooming trigger and added zoomTriggerDistance
 *               attribute (DG);
 * 08-Apr-2004 : Changed getScaleX() and getScaleY() from protected to
 *               public (DG);
 * 15-Apr-2004 : Added zoomOutFactor and zoomInFactor (DG);
 * 21-Apr-2004 : Fixed zooming bug in mouseReleased() method (DG);
 * 13-Jul-2004 : Added check for null chart (DG);
 * 04-Oct-2004 : Renamed ShapeUtils --> ShapeUtilities (DG);
 * 11-Nov-2004 : Moved constants back in from ChartPanelConstants (DG);
 * 12-Nov-2004 : Modified zooming mechanism to support zooming within
 *               subplots (DG);
 * 26-Jan-2005 : Fixed mouse zooming for horizontal category plots (DG);
 * 11-Apr-2005 : Added getFillZoomRectangle() method, renamed
 *               setHorizontalZoom() --> setDomainZoomable(),
 *               setVerticalZoom() --> setRangeZoomable(), added
 *               isDomainZoomable() and isRangeZoomable(), added
 *               getHorizontalAxisTrace() and getVerticalAxisTrace(),
 *               renamed autoRangeBoth() --> restoreAutoBounds(),
 *               autoRangeHorizontal() --> restoreAutoDomainBounds(),
 *               autoRangeVertical() --> restoreAutoRangeBounds() (DG);
 * 12-Apr-2005 : Removed working areas, added getAnchorPoint() method,
 *               added protected accessors for tracelines (DG);
 * 18-Apr-2005 : Made constants final (DG);
 * 26-Apr-2005 : Removed LOGGER (DG);
 * 01-Jun-2005 : Fixed zooming for combined plots - see bug report
 *               1212039, fix thanks to Onno vd Akker (DG);
 * 25-Nov-2005 : Reworked event listener mechanism (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 01-Aug-2006 : Fixed minor bug in restoreAutoRangeBounds() (DG);
 * 04-Sep-2006 : Renamed attemptEditChartProperties() -->
 *               doEditChartProperties() and made public (DG);
 * 13-Sep-2006 : Don't generate ChartMouseEvents if the panel's chart is null
 *               (fixes bug 1556951) (DG);
 * 05-Mar-2007 : Applied patch 1672561 by Sergei Ivanov, to fix zoom rectangle
 *               drawing for dynamic charts (DG);
 * 17-Apr-2007 : Fix NullPointerExceptions in zooming for combined plots (DG);
 * 24-May-2007 : When the look-and-feel changes, update the popup menu if there
 *               is one (DG);
 * 06-Jun-2007 : Fixed coordinates for drawing buffer image (DG);
 * 24-Sep-2007 : Added zoomAroundAnchor flag, and handle clearing of chart
 *               buffer (DG);
 * 25-Oct-2007 : Added default directory attribute (DG);
 * 07-Nov-2007 : Fixed (rare) bug in refreshing off-screen image (DG);
 * 07-May-2008 : Fixed bug in zooming that triggered zoom for a rectangle
 *               outside of the data area (DG);
 * 08-May-2008 : Fixed serialization bug (DG);
 * 15-Aug-2008 : Increased default maxDrawWidth/Height (DG);
 * 18-Sep-2008 : Modified creation of chart buffer (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see patch 1607918 by
 *               Jess Thrysoee (DG);
 * 13-Jan-2009 : Fixed zooming methods to trigger only one plot
 *               change event (DG);
 * 16-Jan-2009 : Use XOR for zoom rectangle only if useBuffer is false (DG);
 * 18-Mar-2009 : Added mouse wheel support (DG);
 * 19-Mar-2009 : Added panning on mouse drag support - based on Ulrich 
 *               Voigt's patch 2686040 (DG);
 * 26-Mar-2009 : Changed fillZoomRectangle default to true, and only change
 *               cursor for CTRL-mouse-click if panning is enabled (DG);
 * 01-Apr-2009 : Fixed panning, and added different mouse event mask for
 *               MacOSX (DG);
 * 08-Apr-2009 : Added copy to clipboard support, based on patch 1460845
 *               by Alessandro Borges (DG);
 * 09-Apr-2009 : Added overlay support (DG);
 * 10-Apr-2009 : Set chartBuffer background to match ChartPanel (DG);
 * 05-May-2009 : Match scaling (and insets) in doCopy() (DG);
 * 01-Jun-2009 : Check for null chart in mousePressed() method (DG);
 * 08-Jun-2009 : Fixed bug in setMouseWheelEnabled() (DG);
 * 06-Jul-2009 : Clear off-screen buffer to fully transparent (DG);
 * 10-Oct-2011 : localization fix: bug #3353913 (MH);
 */


import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.EventListenerList;


import org.jfree.chart.*;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.event.OverlayChangeEvent;
import org.jfree.chart.event.OverlayChangeListener;
import org.jfree.chart.plot.Pannable;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Zoomable;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.Size2D;

import atomicJ.gui.editors.LiveChartEditor;
import atomicJ.gui.editors.LiveChartEditorFactory;
import atomicJ.gui.save.SimpleChartSaveDialog;
import atomicJ.utilities.Validation;


public class MultipleChartPanel<E extends CustomizableXYBaseChart<?>> extends JPanel implements ChartSupervisor, ChartChangeListener,
ChartProgressListener, ActionListener, MouseListener,MouseWheelListener,
MouseMotionListener, OverlayChangeListener, Printable, Serializable 
{	
    private static final long serialVersionUID = 1L;

    private static final String REGULAR_PREFERENCES_GROUP = "Regular";

    public static final boolean DEFAULT_BUFFER_USED = true;
    public static final int DEFAULT_WIDTH = 600;
    public static final int DEFAULT_HEIGHT = 420;
    public static final int DEFAULT_ZOOM_TRIGGER_DISTANCE = 10;
    public static final String ZOOM_IN_BOTH_COMMAND = "ZOOM_IN_BOTH";
    public static final String ZOOM_IN_DOMAIN_COMMAND = "ZOOM_IN_DOMAIN";
    public static final String ZOOM_IN_RANGE_COMMAND = "ZOOM_IN_RANGE";
    public static final String ZOOM_OUT_BOTH_COMMAND = "ZOOM_OUT_BOTH";
    public static final String ZOOM_OUT_DOMAIN_COMMAND = "ZOOM_DOMAIN_BOTH";
    public static final String ZOOM_OUT_RANGE_COMMAND = "ZOOM_RANGE_BOTH";
    public static final String ZOOM_RESET_BOTH_COMMAND = "ZOOM_RESET_BOTH";
    public static final String ZOOM_RESET_DOMAIN_COMMAND = "ZOOM_RESET_DOMAIN";
    public static final String ZOOM_RESET_RANGE_COMMAND = "ZOOM_RESET_RANGE";

    private E chart;

    private List<E> charts = new ArrayList<>();
    private transient EventListenerList chartMouseListeners = new EventListenerList();
    private transient EventListenerList chartMouseWheelListeners = new EventListenerList();

    /** A flag that indicates that the preferred size should be recalculated. */
    private boolean refreshSize;
    /** A flag that indicates that whether the preferred chart size should be used to calculate the size of the ChartPanel. */
    private boolean useChartSize = true;

    private double horizontalSpace;
    private double verticalSpace;

    private boolean useBuffer;
    private boolean refreshBuffer;
    private transient Image chartBuffer;
    private int chartBufferHeight;
    private int chartBufferWidth;
    private JPopupMenu popup;
    private final ChartRenderingInfo info;
    private Point2D anchor;
    private PlotOrientation orientation = PlotOrientation.VERTICAL;
    private boolean domainZoomable = false;
    private boolean rangeZoomable = false;
    private Point2D zoomPoint = null;
    private transient Rectangle2D zoomRectangle = null;
    private boolean fillZoomRectangle = true;
    private int zoomTriggerDistance;
    private boolean horizontalAxisTrace = false;
    private boolean verticalAxisTrace = false;
    private transient Line2D verticalTraceLine;
    private transient Line2D horizontalTraceLine;
    private JMenuItem zoomInBothMenuItem;
    private JMenuItem zoomInDomainMenuItem;
    private JMenuItem zoomInRangeMenuItem;
    private JMenuItem zoomOutBothMenuItem;
    private JMenuItem zoomOutDomainMenuItem;
    private JMenuItem zoomOutRangeMenuItem;
    private JMenuItem zoomResetBothMenuItem;
    private JMenuItem zoomResetDomainMenuItem;
    private JMenuItem zoomResetRangeMenuItem;
    private File defaultDirectoryForSaveAs;

    private boolean enforceFileExtensions;
    private boolean ownToolTipDelaysActive;
    private int originalToolTipInitialDelay;
    private int originalToolTipReshowDelay;
    private int originalToolTipDismissDelay;
    private int ownToolTipInitialDelay;
    private int ownToolTipReshowDelay;
    private int ownToolTipDismissDelay;
    private double zoomInFactor = 0.5;
    private double zoomOutFactor = 2.0;
    private boolean zoomAroundAnchor;
    private transient Paint zoomOutlinePaint;
    private transient Paint zoomFillPaint;
    private double panW, panH;
    private Point panLast;
    private int panMask = InputEvent.CTRL_MASK;
    private int index =  - 1;
    private LiveChartEditor editorDialog;
    private final LiveChartEditorFactory editorFactory = LiveChartEditorFactory.getInstance();   

    private boolean mouseWheelEnabled;
    private double zoomFactor = 0.1;

    private SimpleChartSaveDialog saveDialog;

    private final PropertiesAction propertiesAction = new PropertiesAction();
    private final SaveAction saveAction = new SaveAction();
    private final PrintAction printAction = new PrintAction();
    private final CopyAction copyAction = new CopyAction();
    private final LockAspectRatioAction lockAspectAction = new LockAspectRatioAction();

    private MouseInputMode mode = MouseInputModeStandard.NORMAL;
    private final Map<MouseInputType, MouseInputMode> accessoryModes = new HashMap<>();

    public MultipleChartPanel() 
    {
        this(null, DEFAULT_WIDTH, DEFAULT_HEIGHT,
                true, //useBuffer
                true,  // buildPopup
                true,   // tooltips
                true //mouseWheel
                );
    }

    public MultipleChartPanel(E chart) 
    {
        this(
                chart,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                DEFAULT_BUFFER_USED,
                true,  // buildPopup
                true,  // tooltips
                true //mouseWheel
                );
    }

    public MultipleChartPanel(E chart, boolean buildPopup) 
    {
        this(chart, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_BUFFER_USED, buildPopup, // buildPopup
                true, // tooltips
                true //mouseWheel
                );
    }

    public MultipleChartPanel(E chart, boolean buildPopup, boolean tooltips, boolean mouseWheel) 
    {
        this(chart,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                DEFAULT_BUFFER_USED,
                buildPopup,
                tooltips,
                mouseWheel
                );
    }

    public MultipleChartPanel(E chart, int width, int height, boolean useBuffer, boolean buildPopup,
            boolean tooltips, boolean mouseWheel) {

        setSelectedChart(chart);
        this.info = new ChartRenderingInfo();
        setPreferredSize(new Dimension(width, height));
        this.useBuffer = useBuffer;
        this.refreshBuffer = false;
        setMouseWheelEnabled(mouseWheel);
        this.zoomTriggerDistance = DEFAULT_ZOOM_TRIGGER_DISTANCE;

        // set up popup menu...
        if (buildPopup) 
        {
            popup = createPopupMenu(true, true, true, true, true);
        }

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
        setDisplayToolTips(tooltips);
        addMouseListener(this);
        addMouseMotionListener(this);

        this.defaultDirectoryForSaveAs = null;
        this.enforceFileExtensions = true;

        // initialize ChartPanel-specific tool tip delays with
        // values the from ToolTipManager.sharedInstance()
        ToolTipManager ttm = ToolTipManager.sharedInstance();
        this.ownToolTipInitialDelay = ttm.getInitialDelay();
        this.ownToolTipDismissDelay = ttm.getDismissDelay();
        this.ownToolTipReshowDelay = ttm.getReshowDelay();

        this.zoomAroundAnchor = false;
        this.zoomOutlinePaint = Color.blue;
        this.zoomFillPaint = new Color(0, 0, 255, 63);

        this.panMask = InputEvent.CTRL_MASK;
        // for MacOSX we can't use the CTRL key for mouse drags, see:
        // http://developer.apple.com/qa/qa2004/qa1362.html
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("mac os x")) 
        {
            this.panMask = InputEvent.ALT_MASK;
        }

        initInputAndActionMaps();

    }


    //METHODS INTRODUCED IN A PATCH BY PARODOXOFF
    /**
     * Sets a flag that indicates whether the preferred chart size should be used for the size of the chart panel.
     *
     * @param flag the flag.
     */
    public void setUseChartSize(boolean flag) {
        this.useChartSize = flag;
    }

    /**
     * Returns a flag that indicates whether the preferred chart size should be used for the size of the chart panel.
     *
     * @return The flag.
     */
    public boolean getUseChartSize() {
        return this.useChartSize;
    }

    public void lockAspectRatio(boolean lock)
    {        
        if(chart != null)
        {
            chart.setUseFixedChartAreaSize(lock);
        }
    }

    private void initInputAndActionMaps()
    {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);

        inputMap.put((KeyStroke) copyAction.getValue(Action.ACCELERATOR_KEY), copyAction.getValue(Action.NAME));

        inputMap.put((KeyStroke) saveAction.getValue(Action.ACCELERATOR_KEY), saveAction.getValue(Action.NAME));
        inputMap.put((KeyStroke) printAction.getValue(Action.ACCELERATOR_KEY), printAction.getValue(Action.NAME));
        inputMap.put((KeyStroke) propertiesAction.getValue(Action.ACCELERATOR_KEY), propertiesAction.getValue(Action.NAME));

        ActionMap actionMap =  getActionMap();
        actionMap.put(copyAction.getValue(Action.NAME), copyAction);
        actionMap.put(saveAction.getValue(Action.NAME), saveAction);
        actionMap.put(printAction.getValue(Action.NAME), printAction);
        actionMap.put(propertiesAction.getValue(Action.NAME), propertiesAction);
    }  

    public void setSelectedChart(int index)
    {
        Validation.requireValueEqualToOrBetweenBounds(index, 0, charts.size() - 1, "index");

        setSelectedChart(charts.get(index));
    }

    public void setNextChart()
    {
        setSelectedChart(index - 1);
    }

    public void setPreviousChart()
    {
        setSelectedChart(index + 1);
    }

    public int getSelectedChartIndex()
    {
        return index;
    }		

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //BE CAREFUL - IT RETURN ORIGINAL LIST, NOT A COPY!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public List<E> getCharts()
    {
        return charts;
    }

    public E getChartAt(int index)
    {
        Validation.requireValueEqualToOrBetweenBounds(index, 0, charts.size()-1, "index");

        return charts.get(index);
    }

    public int getChartCount()
    {
        return charts.size();
    }

    public void setCharts(List<E> chartsNew)
    {		     
        if(chartsNew != null && !chartsNew.isEmpty())
        {
            charts = chartsNew;
            setSelectedChart(chartsNew.get(0));
            handleChartAddition(charts);
        }
        else
        {
            setSelectedChart(null);
            charts.clear();
        }
    }

    public void setChartAt(E chart, int index)
    {
        if(index < 0)
        {
            return;
        }

        int chartsCount = charts.size();

        if(index >= chartsCount)
        {
            charts.addAll((Collection<? extends E>) Collections.nCopies(index - chartsCount + 1, null));
        }

        charts.set(index, chart);
        setSelectedChart(index);

        handleChartAddition(chart);
    }

    public void addChart(E chartNew)
    {
        charts.add(chartNew);
        handleChartAddition(chartNew);
    }

    public void addCharts(List<E> chartsNew)
    {
        charts.addAll(chartsNew);
        handleChartAddition(chartsNew);
    }

    protected void handleChartAddition(E chart)
    {
    }

    protected void handleChartAddition(List<E> charts)
    {
    }

    public void deleteChart(int index)
    {
        Validation.requireValueEqualToOrBetweenBounds(index, 0, charts.size()-1, "index");

        deleteChart(charts.get(index));
    }


    public void deleteCharts(int[] indices)
    {
        Arrays.sort(indices);
        boolean selectNew = false;
        int chartCount = charts.size();

        for(int i = indices.length - 1; i>= 0; i--)
        {
            if(0 <= i && i < chartCount)
            {
                int k = indices[i];
                E chartToDelete = charts.get(k);
                boolean isRemoved = charts.remove(chartToDelete);
                boolean isSelected = (this.chart == chartToDelete);

                selectNew = selectNew || (isRemoved && isSelected);
            }          
        }

        if(selectNew)
        {
            if(!charts.isEmpty())
            {
                setSelectedChart(charts.get(0));
            }
            else
            {
                setSelectedChart(null);
            }
        }
    }

    public void deleteChart(E c)
    {
        boolean isRemoved = charts.remove(c);
        boolean isSelected = (this.chart == c);
        if(isRemoved && isSelected)
        {
            if(!charts.isEmpty())
            {
                setSelectedChart(charts.get(0));
            }
            else
            {
                setSelectedChart(null);
            }
        }
    }

    public void deleteCharts(List<E> charts)
    {
        Validation.requireNonNullParameterName(charts, "charts");

        boolean isRemoved = charts.removeAll(charts);
        boolean isSelected = (charts.contains(this.chart));
        if(isRemoved && isSelected)
        {
            if(!charts.isEmpty())
            {
                setSelectedChart(charts.get(0));
            }
            else
            {
                setSelectedChart(null);
            }
        }
    }

    public void clear()
    {
        charts.clear();
        setSelectedChart(null);
    }

    public void updateOrCreateEditor()
    {
        E selectedChart = getSelectedChart();

        if(editorDialog != null)
        {
            editorDialog.dispose();
        }

        this.editorDialog = null;

        if(selectedChart != null)
        {			
            List<E> editableCharts = getStyleBoundedCharts();
            Component root = SwingUtilities.getRoot(this);
            Window rootWindow = (root instanceof Window) ? (Window)root : null;

            editorDialog = getEditorDialog(selectedChart, editableCharts, rootWindow);
        }
    }


    public List<E> getStyleBoundedCharts()
    {
        return getNonEmptyCharts();
    }

    public List<E> getNonEmptyCharts()
    {
        List<E> nonEmptyCharts = new ArrayList<>(charts);
        nonEmptyCharts.removeAll(Collections.singleton(null));

        return nonEmptyCharts;
    }

    protected LiveChartEditor getEditorDialog(E selectedChart, List<E> boundCharts, Window parent)
    {
        return editorFactory.getEditor(selectedChart, boundCharts, parent);
    }  

    public Point2D getDataPoint(Point screenPoint)
    {  
        Point2D java2DPoint = translateScreenToJava2D(screenPoint);
        E chart = getSelectedChart();
        Point2D dataPoint = null;
        if(chart != null)
        {
            PlotRenderingInfo info = getChartRenderingInfo().getPlotInfo();             
            dataPoint = chart.getDataPoint(java2DPoint, info);
        }

        return dataPoint;
    }   

    @Override
    public void doEditDomainAxisProperties(int index)
    {
        if(!charts.isEmpty())
        {
            updateOrCreateEditor();
            editorDialog.editDomainAxis(index);
            editorDialog.setVisible(true);
        }
    }


    @Override
    public void doEditRangeAxisProperties(int index)
    {
        if(!charts.isEmpty())
        {
            updateOrCreateEditor();
            editorDialog.editRangeAxis(index);
            editorDialog.setVisible(true);
        }
    }

    @Override
    public void doEditDepthAxisProperties(int index)
    {
        if(!charts.isEmpty())
        {
            updateOrCreateEditor();
            editorDialog.editDepthAxis(index);
            editorDialog.setVisible(true);
        }
    }

    @Override
    public void doEditTitleProperties()
    {
        if(!charts.isEmpty())
        {
            updateOrCreateEditor();
            editorDialog.editTitle();
            editorDialog.setVisible(true);
        }
    }

    @Override
    public void doEditDomainScaleBarProperties()
    {
        if(!charts.isEmpty())
        {
            updateOrCreateEditor();
            editorDialog.editDomainScaleBar();
            editorDialog.setVisible(true);
        }
    }

    @Override
    public void doEditRangeScaleBarProperties()
    {
        if(!charts.isEmpty())
        {
            updateOrCreateEditor();
            editorDialog.editRangeScaleBar();
            editorDialog.setVisible(true);
        }
    }

    @Override
    public void doEditLegendProperties(String legendName)
    {
        if(!charts.isEmpty())
        {
            updateOrCreateEditor();
            editorDialog.editLegend(legendName);
            editorDialog.setVisible(true);
        }
    }

    public void doEditChartProperties() 
    {
        if(!charts.isEmpty())
        {
            updateOrCreateEditor();
            editorDialog.setVisible(true);
        }
    }   

    public void doSaveAs() throws IOException 
    {
        if(saveDialog == null)
        {
            saveDialog = new SimpleChartSaveDialog(SwingUtilities.getWindowAncestor(this), this, getSavePreferencesGroup());
        }

        saveDialog.setChooserCurrentDirectory(defaultDirectoryForSaveAs);
        saveDialog.showDialog(getSelectedChart(), getChartRenderingInfo().getChartArea(), getScreenDataArea());
    }

    protected String getSavePreferencesGroup()
    {
        return REGULAR_PREFERENCES_GROUP;
    }

    public void setSelectedChartAndClearOld(E chart)
    {
        charts.clear();
        setSelectedChart(chart);
    }

    public E getSelectedChart() 
    {
        return this.chart;
    }

    public void setSelectedChart(E chart) 
    {        
        // stop listening for changes to the existing chart
        if (this.chart != null) 
        {
            removeChartMouseListener(this.chart);
            this.chart.removeChangeListener(this);
            this.chart.removeProgressListener(this);
            this.chart.setChartSupervisor(null);
        }

        if(this.info != null)
        {
            this.info.clear();
        }

        // add the new chart
        this.chart = chart;

        if (chart != null) 
        {
            addChartMouseListener(chart);


            this.chart.setMode(mode);
            this.chart.setAccessoryModes(accessoryModes);
            this.chart.addChangeListener(this);
            this.chart.addProgressListener(this);
            this.chart.setChartSupervisor(this);
            this.domainZoomable = false;
            this.rangeZoomable = false;

            Plot plot = chart.getPlot();
            if (plot instanceof Zoomable) 
            {
                Zoomable z = (Zoomable) plot;
                this.domainZoomable = z.isDomainZoomable();
                this.rangeZoomable = z.isRangeZoomable();
                this.orientation = z.getOrientation();
            }
            boolean isContained = charts.contains(chart);
            if(!isContained)
            {
                index = charts.size();
                addChart(chart);
            }
            else
            {
                index = charts.indexOf(chart);
            }
        }
        else 
        {
            this.index = -1;
            this.domainZoomable = false;
            this.rangeZoomable = false;
        }
        if (this.useBuffer) {
            this.refreshBuffer = true;
        }

        repaint();

    }

    public Point2D getAnchor() 
    {
        return this.anchor;
    }

    protected void setAnchor(Point2D anchor) 
    {
        this.anchor = anchor;
    }

    public JPopupMenu getPopupMenu() 
    {
        return this.popup;
    }

    public void setPopupMenu(JPopupMenu popup) 
    {
        this.popup = popup;
    }

    public ChartRenderingInfo getChartRenderingInfo() 
    {
        return this.info;
    }

    public void setMouseZoomable(boolean flag) 
    {
        setMouseZoomable(flag, true);
    }

    public void setMouseZoomable(boolean flag, boolean fillRectangle) 
    {
        setDomainZoomable(flag);
        setRangeZoomable(flag);
        setFillZoomRectangle(fillRectangle);
    }

    public boolean isDomainZoomable()
    {
        boolean zoomable = false;		

        E selectedChart = getSelectedChart();

        if(selectedChart == null)
        {
            return zoomable;
        }
        else
        {
            zoomable = this.domainZoomable && selectedChart.isDomainZoomable();
        }

        return zoomable;
    }

    public void setDomainZoomable(boolean flag) 
    {
        if (flag) 
        { 
            this.domainZoomable = flag && (this.chart.isDomainZoomable());
        }
        else 
        {
            this.domainZoomable = false;
        }
    }

    public boolean isRangeZoomable()
    {
        boolean zoomable = false;		

        E selectedChart = getSelectedChart();

        if(selectedChart == null)
        {
            return zoomable;
        }
        else
        {
            zoomable = this.rangeZoomable && selectedChart.isRangeZoomable();
        }
        return zoomable;
    }


    public void setRangeZoomable(boolean flag) 
    {
        if (flag) 
        {
            this.rangeZoomable = flag && (this.chart.isRangeZoomable());        
        }
        else 
        {
            this.rangeZoomable = false;
        }
    }

    public boolean getFillZoomRectangle() 
    {
        return this.fillZoomRectangle;
    }

    public void setFillZoomRectangle(boolean flag) 
    {
        this.fillZoomRectangle = flag;
    }

    public int getZoomTriggerDistance() 
    {
        return this.zoomTriggerDistance;
    }

    public void setZoomTriggerDistance(int distance) 
    {
        this.zoomTriggerDistance = distance;
    }

    public boolean getHorizontalAxisTrace() 
    {
        return this.horizontalAxisTrace;
    }

    public void setHorizontalAxisTrace(boolean flag) 
    {
        this.horizontalAxisTrace = flag;
    }

    protected Line2D getHorizontalTraceLine() 
    {
        return this.horizontalTraceLine;
    }

    protected void setHorizontalTraceLine(Line2D line) 
    {
        this.horizontalTraceLine = line;
    }

    public boolean getVerticalAxisTrace() 
    {
        return this.verticalAxisTrace;
    }

    public void setVerticalAxisTrace(boolean flag) 
    {
        this.verticalAxisTrace = flag;
    }

    protected Line2D getVerticalTraceLine() 
    {
        return this.verticalTraceLine;
    }

    protected void setVerticalTraceLine(Line2D line) 
    {
        this.verticalTraceLine = line;
    }

    public File getDefaultDirectoryForSaveAs() 
    {
        return this.defaultDirectoryForSaveAs;
    }

    public void setDefaultDirectoryForSaveAs(File directory) 
    {
        if (directory != null && !directory.isDirectory()) 
        {
            throw new IllegalArgumentException("The 'directory' argument is not a directory.");
        }

        this.defaultDirectoryForSaveAs = directory;
    }

    public boolean isEnforceFileExtensions() 
    {
        return this.enforceFileExtensions;
    }

    public void setEnforceFileExtensions(boolean enforce) 
    {
        this.enforceFileExtensions = enforce;
    }

    public boolean getZoomAroundAnchor() 
    {
        return this.zoomAroundAnchor;
    }

    public void setZoomAroundAnchor(boolean zoomAroundAnchor) 
    {
        this.zoomAroundAnchor = zoomAroundAnchor;
    }

    public Paint getZoomFillPaint() 
    {
        return this.zoomFillPaint;
    }

    public void setZoomFillPaint(Paint paint) 
    {        
        Validation.requireNonNullParameterName(paint, "paint");

        this.zoomFillPaint = paint;
    }

    public Paint getZoomOutlinePaint() 
    {
        return this.zoomOutlinePaint;
    }

    public void setZoomOutlinePaint(Paint paint) 
    {
        this.zoomOutlinePaint = paint;
    }

    public boolean isMouseWheelEnabled() 
    {
        return mouseWheelEnabled;
    }

    public void setMouseWheelEnabled(boolean flag) 
    {
        this.mouseWheelEnabled = flag;
        if (flag) 
        {
            addMouseWheelListener(this);
        }
        else 
        {
            removeMouseWheelListener(this);
        }
    }

    @Override
    public void overlayChanged(OverlayChangeEvent event) 
    {
        repaint();
    }

    public void setDisplayToolTips(boolean flag) 
    {
        if (flag) 
        {
            ToolTipManager.sharedInstance().registerComponent(this);
        }
        else 
        {
            ToolTipManager.sharedInstance().unregisterComponent(this);
        }
    }

    @Override
    public String getToolTipText(MouseEvent e) 
    {
        String tooltipText = null;

        if(chart != null && this.info != null)
        {
            Point2D java2DPoint = translateScreenToJava2D(e.getPoint());                
            tooltipText = this.chart.getDataTooltipText(java2DPoint, info);                                
        }

        return tooltipText;
    }

    public Point translateJava2DToScreen(Point2D java2DPoint) 
    {
        Insets insets = getInsets();
        int x = (int) (java2DPoint.getX() + insets.left + horizontalSpace);
        int y = (int) (java2DPoint.getY() + insets.top + verticalSpace);
        return new Point(x, y);
    }

    public Point2D translateScreenToJava2D(Point screenPoint) 
    {
        Insets insets = getInsets();
        double x = (screenPoint.getX() - insets.left - horizontalSpace);
        double y = (screenPoint.getY() - insets.top - verticalSpace);
        return new Point2D.Double(x, y);
    }

    public Rectangle2D scale(Rectangle2D rect) 
    {
        Insets insets = getInsets();
        double x = rect.getX() + insets.left + horizontalSpace;
        double y = rect.getY() + insets.top + verticalSpace;
        double w = rect.getWidth();
        double h = rect.getHeight();
        return new Rectangle2D.Double(x, y, w, h);
    }

    public ChartEntity getEntityForPoint(int viewX, int viewY) 
    {
        ChartEntity result = null;
        if (this.info != null) {
            Insets insets = getInsets();
            double x = (viewX - insets.left - horizontalSpace);
            double y = (viewY - insets.top - verticalSpace);
            EntityCollection entities = this.info.getEntityCollection();
            result = entities != null ? entities.getEntity(x, y) : null;
        }
        return result;
    }

    public boolean getRefreshBuffer() 
    {
        return this.refreshBuffer;
    }

    public void setRefreshBuffer(boolean flag) 
    {
        this.refreshBuffer = flag;
    }

    /*
    @Override
    public Insets getInsets()
    {
    	Insets insets = super.getInsets();
    	Insets newInsets = new Insets(insets.top + (int)verticalSpace, insets.left + (int)horizontalSpace,
    			insets.bottom+(int)verticalSpace, insets.right + (int)horizontalSpace);

    	return newInsets;

    }
     */

    @Override
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);

        if (this.chart == null) 
        {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();

        //CHANGES INTRODUCED IN A PATCH BY PARADOXOFF

        this.horizontalSpace = 0;
        this.verticalSpace = 0;

        if(this.chart.getUseFixedChartAreaSize() && useChartSize)
        {
            Dimension size = getSize();

            Insets insets = getInsets();
            Rectangle2D available = new Rectangle2D.Double(insets.left, insets.top,
                    size.getWidth() - insets.left - insets.right,
                    size.getHeight() - insets.top - insets.bottom);

            chart.updateFixedDataAreaSize(g2, available);

            Size2D chartSize = chart.getPreferredChartAreaSize(g2);

            horizontalSpace = 0.5*(size.getWidth() - chartSize.getWidth());
            verticalSpace = 0.5*(size.getHeight() - chartSize.getHeight());           	 
        }

        boolean fixedSize = false;
        if(this.chart.getUseFixedChartAreaSize() && useChartSize)
        {
            fixedSize = true;
            if(refreshSize)
            {
                Size2D chartSize = chart.getPreferredChartAreaSize(g2);
                int cwidth = (int)(chartSize.getWidth());
                int cheight = (int)(chartSize.getHeight());
                refreshSize = false;
                setPreferredSize(new Dimension(cwidth,cheight));
                invalidate();
                revalidate();
                this.anchor = null;
                this.verticalTraceLine = null;
                this.horizontalTraceLine = null;
                repaint();
            }
        }

        g2.translate(horizontalSpace, verticalSpace);

        // first determine the size of the chart rendering area...
        Dimension size = fixedSize ? getPreferredSize() : getSize();
        // END OF CHANGED INTRODUCED BY PARADOXOFF

        Insets insets = getInsets();
        Rectangle2D available = new Rectangle2D.Double(insets.left, insets.top,
                size.getWidth() - insets.left - insets.right,
                size.getHeight() - insets.top - insets.bottom);

        // work out if scaling is required...
        double drawWidth = available.getWidth();
        double drawHeight = available.getHeight();

        Rectangle2D chartArea = new Rectangle2D.Double(0.0, 0.0, drawWidth, drawHeight);

        // are we using the chart buffer?
        if (this.useBuffer && this.chartBufferWidth > 0 && this.chartBufferHeight > 0)
        {
            // do we need to resize the buffer?
            if ((this.chartBuffer == null)
                    || (this.chartBufferWidth != available.getWidth())
                    || (this.chartBufferHeight != available.getHeight())) 
            {
                this.chartBufferWidth = (int) available.getWidth();
                this.chartBufferHeight = (int) available.getHeight();
                GraphicsConfiguration gc = g2.getDeviceConfiguration();
                this.chartBuffer = gc.createCompatibleImage(
                        this.chartBufferWidth, this.chartBufferHeight,
                        Transparency.TRANSLUCENT);
                this.refreshBuffer = true;
            }

            // do we need to redraw the buffer?
            if (this.refreshBuffer) 
            {
                this.refreshBuffer = false; // clear the flag

                Rectangle2D bufferArea = new Rectangle2D.Double(0, 0, this.chartBufferWidth, this.chartBufferHeight);

                // make the background of the buffer clear and transparent
                Graphics2D bufferG2 = (Graphics2D)this.chartBuffer.getGraphics();
                Composite savedComposite = bufferG2.getComposite();
                bufferG2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
                Rectangle r = new Rectangle(0, 0, this.chartBufferWidth, this.chartBufferHeight);
                bufferG2.fill(r);
                bufferG2.setComposite(savedComposite);

                this.chart.draw(bufferG2, bufferArea, this.anchor, this.info);               
            }

            // zap the buffer onto the panel...
            g2.drawImage(this.chartBuffer, insets.left, insets.top, this);
        }

        // or redrawing the chart every time...
        else 
        {   

            AffineTransform saved = g2.getTransform();
            g2.translate(insets.left, insets.top);
            chart.draw(g2, chartArea, this.anchor, this.info);
            g2.setTransform(saved);
        }

        // redraw the zoom rectangle (if present) - if useBuffer is false,
        // we use XOR so we can XOR the rectangle away again without redrawing
        // the chart
        g2.translate(-horizontalSpace, -verticalSpace);
        drawZoomRectangle(g2, !this.useBuffer);

        g2.dispose();

        this.anchor = null;
        this.verticalTraceLine = null;
        this.horizontalTraceLine = null;
    }

    @Override
    public void chartChanged(ChartChangeEvent event)
    {    	
        chartChanged();
    }

    public void chartChanged()
    {       
        this.refreshBuffer = true;
        this.refreshSize = true;
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) 
        {
            Zoomable z = (Zoomable) plot;
            this.orientation = z.getOrientation();
        }
        boolean lockAspect = chart.getUseFixedChartAreaSize();
        lockAspectAction.putValue(Action.SELECTED_KEY, lockAspect);
        repaint();
    }


    @Override
    public void chartProgress(ChartProgressEvent event) {
        // does nothing - override if necessary

    }

    /**
     * Handles action events generated by the popup menu.
     *
     * @param event  the event.
     */
    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();

        // many of the zoom methods need a screen location - all we have is
        // the zoomPoint, but it might be null.  Here we grab the x and y
        // coordinates, or use defaults...
        double screenX = -1.0;
        double screenY = -1.0;
        if (this.zoomPoint != null) {
            screenX = this.zoomPoint.getX();
            screenY = this.zoomPoint.getY();
        }

        if (command.equals(ZOOM_IN_BOTH_COMMAND)) {
            zoomInBoth(screenX, screenY);
        }
        else if (command.equals(ZOOM_IN_DOMAIN_COMMAND)) {
            zoomInDomain(screenX, screenY);
        }
        else if (command.equals(ZOOM_IN_RANGE_COMMAND)) {
            zoomInRange(screenX, screenY);
        }
        else if (command.equals(ZOOM_OUT_BOTH_COMMAND)) {
            zoomOutBoth(screenX, screenY);
        }
        else if (command.equals(ZOOM_OUT_DOMAIN_COMMAND)) {
            zoomOutDomain(screenX, screenY);
        }
        else if (command.equals(ZOOM_OUT_RANGE_COMMAND)) {
            zoomOutRange(screenX, screenY);
        }
        else if (command.equals(ZOOM_RESET_BOTH_COMMAND)) {
            restoreAutoBounds();
        }
        else if (command.equals(ZOOM_RESET_DOMAIN_COMMAND)) {
            restoreAutoDomainBounds();
        }
        else if (command.equals(ZOOM_RESET_RANGE_COMMAND)) {
            restoreAutoRangeBounds();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) 
    {
        if (!this.ownToolTipDelaysActive) {
            ToolTipManager ttm = ToolTipManager.sharedInstance();

            this.originalToolTipInitialDelay = ttm.getInitialDelay();
            ttm.setInitialDelay(this.ownToolTipInitialDelay);

            this.originalToolTipReshowDelay = ttm.getReshowDelay();
            ttm.setReshowDelay(this.ownToolTipReshowDelay);

            this.originalToolTipDismissDelay = ttm.getDismissDelay();
            ttm.setDismissDelay(this.ownToolTipDismissDelay);

            this.ownToolTipDelaysActive = true;
        }
    }

    @Override
    public void mouseExited(MouseEvent e) 
    {
        if (this.ownToolTipDelaysActive) {
            // restore original tooltip dealys
            ToolTipManager ttm = ToolTipManager.sharedInstance();
            ttm.setInitialDelay(this.originalToolTipInitialDelay);
            ttm.setReshowDelay(this.originalToolTipReshowDelay);
            ttm.setDismissDelay(this.originalToolTipDismissDelay);
            this.ownToolTipDelaysActive = false;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) 
    {
        if (this.chart == null) 
        {
            return;
        }

        int mods = e.getModifiers();

        if ((mods & this.panMask) == this.panMask) 
        {
            // can we pan this plot?

            if (chart.canPossiblyBePannable()) 
            {
                Rectangle2D screenDataArea = getScreenDataArea(e.getX(),
                        e.getY());
                if (screenDataArea != null && screenDataArea.contains(
                        e.getPoint())) {
                    this.panW = screenDataArea.getWidth();
                    this.panH = screenDataArea.getHeight();
                    this.panLast = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(
                            Cursor.MOVE_CURSOR));
                }
            }
            // the actual panning occurs later in the mouseDragged() 
            // method
        }
        else if (this.zoomRectangle == null) 
        {

            Rectangle2D screenDataArea = getScreenDataArea(e.getX(), e.getY());
            if (screenDataArea != null) 
            {
                this.zoomPoint = getPointInRectangle(e.getX(), e.getY(),
                        screenDataArea);
            }
            else 
            {
                this.zoomPoint = null;
            }
            if (e.isPopupTrigger()) 
            {
                if (this.popup != null) 
                {
                    displayPopupMenu(e.getX(), e.getY());               	
                }
            }
        }

        //code to fire chartMousePressed event

        Object[] listeners = this.chartMouseListeners.getListeners(CustomChartMouseListener.class);
        if (listeners.length == 0) 
        {
            return;
        }

        Point2D p = translateScreenToJava2D(e.getPoint());

        ChartEntity entity = null;
        if (info != null) 
        {
            EntityCollection entities = info.getEntityCollection();
            if (entities != null) 
            {
                entity = entities.getEntity(p.getX(), p.getY());
            }
        }
        CustomChartMouseEvent chartEvent = new CustomChartMouseEvent(getSelectedChart(), info, e,entity,p);

        for (int i = listeners.length - 1; i >= 0; i -= 1) 
        {
            ((CustomChartMouseListener) listeners[i]).chartMousePressed(chartEvent);
        }
    }

    public boolean isPopupDisplayable(int x, int y)
    {
        return true;
    }

    private Point2D getPointInRectangle(int x, int y, Rectangle2D area) 
    {
        double xx = Math.max(area.getMinX(), Math.min(x, area.getMaxX()));
        double yy = Math.max(area.getMinY(), Math.min(y, area.getMaxY()));
        return new Point2D.Double(xx, yy);
    }

    @Override
    public void mouseDragged(MouseEvent e) 
    {
        // if the popup menu has already been triggered, then ignore dragging...
        if (this.popup != null && this.popup.isShowing()) {
            return;
        }

        // handle panning if we have a start point
        if (chart.isDomainPannable() || chart.isRangePannable()) 
        {
            if (this.panLast != null) 
            {
                double dx = e.getX() - this.panLast.getX();
                double dy = e.getY() - this.panLast.getY();
                if (dx == 0.0 && dy == 0.0) {
                    return;
                }
                double wPercent = -dx / this.panW;
                double hPercent = dy / this.panH;
                boolean old = this.chart.getPlot().isNotify();
                this.chart.getPlot().setNotify(false);
                Plot plot = this.chart.getPlot();
                if(plot instanceof Pannable)
                {
                    Pannable p = (Pannable)plot;
                    if (p.getOrientation() == PlotOrientation.VERTICAL) {
                        p.panDomainAxes(wPercent, this.info.getPlotInfo(),
                                this.panLast);
                        p.panRangeAxes(hPercent, this.info.getPlotInfo(),
                                this.panLast);
                    }
                    else {
                        p.panDomainAxes(hPercent, this.info.getPlotInfo(),
                                this.panLast);
                        p.panRangeAxes(wPercent, this.info.getPlotInfo(),
                                this.panLast);
                    }
                }

                this.panLast = e.getPoint();
                this.chart.getPlot().setNotify(old);
                return;
            }
        }


        // if no initial zoom point was set, ignore dragging...
        if (this.zoomPoint == null)
        {
            return;
        }

        Graphics2D g2 = (Graphics2D) getGraphics();

        // erase the previous zoom rectangle (if any).  We only need to do
        // this is we are using XOR mode, which we do when we're not using
        // the buffer (if there is a buffer, then at the end of this method we
        // just trigger a repaint)
        if (!this.useBuffer) 
        {
            drawZoomRectangle(g2, true);
        }

        boolean hZoom = false;
        boolean vZoom = false;
        if (this.orientation == PlotOrientation.HORIZONTAL) 
        {

            hZoom = isRangeZoomable();
            vZoom = isDomainZoomable();
        }
        else 
        {
            hZoom = isDomainZoomable();
            vZoom = isRangeZoomable();
        }
        Rectangle2D scaledDataArea = getScreenDataArea(
                (int) this.zoomPoint.getX(), (int) this.zoomPoint.getY());
        if (hZoom && vZoom) {
            // selected rectangle shouldn't extend outside the data area...
            double xmax = Math.min(e.getX(), scaledDataArea.getMaxX());
            double ymax = Math.min(e.getY(), scaledDataArea.getMaxY());
            this.zoomRectangle = new Rectangle2D.Double(
                    this.zoomPoint.getX(), this.zoomPoint.getY(),
                    xmax - this.zoomPoint.getX(), ymax - this.zoomPoint.getY());
        }
        else if (hZoom) {
            double xmax = Math.min(e.getX(), scaledDataArea.getMaxX());
            this.zoomRectangle = new Rectangle2D.Double(
                    this.zoomPoint.getX(), scaledDataArea.getMinY(),
                    xmax - this.zoomPoint.getX(), scaledDataArea.getHeight());
        }
        else if (vZoom) 
        {
            double ymax = Math.min(e.getY(), scaledDataArea.getMaxY());
            this.zoomRectangle = new Rectangle2D.Double(
                    scaledDataArea.getMinX(), this.zoomPoint.getY(),
                    scaledDataArea.getWidth(), ymax - this.zoomPoint.getY());
        }

        // Draw the new zoom rectangle...
        if (this.useBuffer) 
        {
            repaint();
        }
        else {
            // with no buffer, we use XOR to draw the rectangle "over" the
            // chart...
            drawZoomRectangle(g2, true);
        }
        g2.dispose();

        //code to fire chartMouseDragged event

        Object[] listeners = this.chartMouseListeners.getListeners(CustomChartMouseListener.class);

        if (listeners.length == 0) 
        {
            return;
        }

        Point2D p = translateScreenToJava2D(e.getPoint());

        ChartEntity entity = null;
        if (info != null) 
        {
            EntityCollection entities = info.getEntityCollection();
            if (entities != null) 
            {
                entity = entities.getEntity(p.getX(), p.getY());
            }
        }

        CustomChartMouseEvent chartEvent = new CustomChartMouseEvent(getSelectedChart(), info, e,entity,p);

        for (int i = listeners.length - 1; i >= 0; i -= 1) 
        {   
            ((CustomChartMouseListener) listeners[i]).chartMouseDragged(chartEvent);
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) 
    {	
        // if we've been panning, we need to reset now that the mouse is 
        // released...
        if (this.panLast != null) 
        {
            this.panLast = null;
            setCursor(Cursor.getDefaultCursor());
        }

        else if (this.zoomRectangle != null) 
        {
            boolean hZoom = false;
            boolean vZoom = false;
            if (this.orientation == PlotOrientation.HORIZONTAL) {
                hZoom = this.rangeZoomable;
                vZoom = this.domainZoomable;
            }
            else {
                hZoom = this.domainZoomable;
                vZoom = this.rangeZoomable;
            }

            boolean zoomTrigger1 = hZoom && Math.abs(e.getX() - this.zoomPoint.getX()) >= this.zoomTriggerDistance;
            boolean zoomTrigger2 = vZoom && Math.abs(e.getY() - this.zoomPoint.getY()) >= this.zoomTriggerDistance;
            if (zoomTrigger1 || zoomTrigger2) 
            {
                if ((hZoom && (e.getX() < this.zoomPoint.getX()))
                        || (vZoom && (e.getY() < this.zoomPoint.getY()))) 
                {
                    restoreAutoBounds();
                }
                else 
                {
                    double x, y, w, h;
                    Rectangle2D screenDataArea = getScreenDataArea(
                            (int) this.zoomPoint.getX(),
                            (int) this.zoomPoint.getY());
                    double maxX = screenDataArea.getMaxX();
                    double maxY = screenDataArea.getMaxY();
                    // for mouseReleased event, (horizontalZoom || verticalZoom)
                    // will be true, so we can just test for either being false;
                    // otherwise both are true
                    if (!vZoom) {
                        x = this.zoomPoint.getX();
                        y = screenDataArea.getMinY();
                        w = Math.min(this.zoomRectangle.getWidth(),
                                maxX - this.zoomPoint.getX());
                        h = screenDataArea.getHeight();
                    }
                    else if (!hZoom) {
                        x = screenDataArea.getMinX();
                        y = this.zoomPoint.getY();
                        w = screenDataArea.getWidth();
                        h = Math.min(this.zoomRectangle.getHeight(),
                                maxY - this.zoomPoint.getY());
                    }
                    else {
                        x = this.zoomPoint.getX();
                        y = this.zoomPoint.getY();
                        w = Math.min(this.zoomRectangle.getWidth(),
                                maxX - this.zoomPoint.getX());
                        h = Math.min(this.zoomRectangle.getHeight(),
                                maxY - this.zoomPoint.getY());
                    }
                    Rectangle2D zoomArea = new Rectangle2D.Double(x, y, w, h);
                    zoom(zoomArea);
                }
                this.zoomPoint = null;
                this.zoomRectangle = null;
            }
            else {
                // erase the zoom rectangle
                Graphics2D g2 = (Graphics2D) getGraphics();
                if (this.useBuffer) {
                    repaint();
                }
                else {
                    drawZoomRectangle(g2, true);
                }
                g2.dispose();
                this.zoomPoint = null;
                this.zoomRectangle = null;
            }

        }

        else if (e.isPopupTrigger()) 
        {
            if (this.popup != null) 
            {
                displayPopupMenu(e.getX(), e.getY());
            }
        }


        //code to fire chartMouseReleased event

        Object[] listeners = this.chartMouseListeners.getListeners(CustomChartMouseListener.class);
        if (listeners.length == 0) 
        {
            return;
        }

        Point2D java2DPoint = translateScreenToJava2D(e.getPoint());

        ChartEntity entity = null;
        if (info != null) 
        {
            EntityCollection entities = info.getEntityCollection();
            if (entities != null) 
            {
                entity = entities.getEntity(java2DPoint.getX(), java2DPoint.getY());
            }
        }
        CustomChartMouseEvent chartEvent = new CustomChartMouseEvent(getSelectedChart(), info, e,entity,java2DPoint);

        for (int i = listeners.length - 1; i >= 0; i -= 1) 
        {
            ((CustomChartMouseListener) listeners[i]).chartMouseReleased(chartEvent);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) 
    {
        Point2D java2DPoint = translateScreenToJava2D(e.getPoint());

        this.anchor = new Point2D.Double(java2DPoint.getX(), java2DPoint.getY());
        if (this.chart == null) 
        {
            return;
        }

        Object[] listeners = this.chartMouseListeners.getListeners(CustomChartMouseListener.class);
        if (listeners.length == 0) 
        {
            return;
        }

        ChartEntity entity = null;
        if (info != null) 
        {
            EntityCollection entities = info.getEntityCollection();
            if (entities != null) 
            {
                entity = entities.getEntity(java2DPoint.getX(), java2DPoint.getY());
            }
        }

        CustomChartMouseEvent chartEvent = new CustomChartMouseEvent(getSelectedChart(), info, e,entity,java2DPoint);

        for (int i = listeners.length - 1; i >= 0; i -= 1) 
        {
            ((CustomChartMouseListener) listeners[i]).chartMouseClicked(chartEvent);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) 
    {
        Graphics2D g2 = (Graphics2D) getGraphics();
        if (this.horizontalAxisTrace) 
        {
            drawHorizontalAxisTrace(g2, e.getX());
        }
        if (this.verticalAxisTrace) 
        {
            drawVerticalAxisTrace(g2, e.getY());
        }

        g2.dispose();

        Object[] listeners = this.chartMouseListeners.getListeners(CustomChartMouseListener.class);

        if (listeners.length == 0) 
        {
            return;
        }

        Point2D java2DPoint = translateScreenToJava2D(e.getPoint());

        ChartEntity entity = null;
        if (info != null) 
        {
            EntityCollection entities = info.getEntityCollection();
            if (entities != null) {
                entity = entities.getEntity(java2DPoint.getX(), java2DPoint.getY());
            }
        }

        if (chart != null) 
        {
            CustomChartMouseEvent event = new CustomChartMouseEvent(chart, info, e, entity, java2DPoint);
            for (int i = listeners.length - 1; i >= 0; i -= 1) 
            {
                ((CustomChartMouseListener) listeners[i]).chartMouseMoved(event);
            }
        }
    }

    public double getZoomFactor() 
    {
        return this.zoomFactor;
    }

    public void setZoomFactor(double zoomFactor) 
    {
        this.zoomFactor = zoomFactor;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) 
    {
        E chart = getSelectedChart();
        if (chart == null) {
            return;
        }

        Point2D p = translateScreenToJava2D(e.getPoint());

        ChartEntity entity = null;
        if (info != null) 
        {
            EntityCollection entities = info.getEntityCollection();
            if (entities != null) {
                entity = entities.getEntity(p.getX(), p.getY());
            }
        }

        if(!(entity instanceof RoamingTitleEntity))
        {
            Plot plot = chart.getPlot();
            if (plot instanceof Zoomable) {
                Zoomable zoomable = (Zoomable) plot;
                handleZoomable(zoomable, e);
            }
            else if (plot instanceof PiePlot) {
                PiePlot pp = (PiePlot) plot;
                pp.handleMouseWheelRotation(e.getWheelRotation());
            }

        }


        Object[] listeners = this.chartMouseWheelListeners.getListeners(CustomChartMouseWheelListener.class);

        CustomChartMouseWheelEvent event = new CustomChartMouseWheelEvent(chart, info, e, entity, p);
        for (int i = listeners.length - 1; i >= 0; i -= 1) 
        {
            ((CustomChartMouseWheelListener) listeners[i]).chartMouseWheelMoved(event);
        }

    }

    /**
     * Handle the case where a plot implements the {@link Zoomable} interface.
     *
     * @param zoomable  the zoomable plot.
     * @param e  the mouse wheel event.
     */
    private void handleZoomable(Zoomable zoomable, MouseWheelEvent e) 
    {    	
        // don't zoom unless the mouse pointer is in the plot's data area
        ChartRenderingInfo info = getChartRenderingInfo();
        PlotRenderingInfo pinfo = info.getPlotInfo();
        Point2D p = translateScreenToJava2D(e.getPoint());
        if (!pinfo.getDataArea().contains(p)) {
            return;
        }

        Plot plot = (Plot) zoomable;
        // do not notify while zooming each axis
        boolean notifyState = plot.isNotify();
        plot.setNotify(false);
        int clicks = e.getWheelRotation();
        double zf = 1.0 + this.zoomFactor;
        if (clicks < 0) {
            zf = 1.0 / zf;
        }
        if (isDomainZoomable()) 
        {
            zoomable.zoomDomainAxes(zf, pinfo, p, true);
        }
        if (isRangeZoomable()) 
        {
            zoomable.zoomRangeAxes(zf, pinfo, p, true);
        }
        plot.setNotify(notifyState);  // this generates the change event too
    }

    public void zoomInBoth(double x, double y) 
    {
        Plot plot = this.chart.getPlot();
        if (plot == null) {
            return;
        }
        // here we tweak the notify flag on the plot so that only
        // one notification happens even though we update multiple
        // axes...
        boolean savedNotify = plot.isNotify();
        plot.setNotify(false);
        zoomInDomain(x, y);
        zoomInRange(x, y);
        plot.setNotify(savedNotify);
    }

    public void zoomInDomain(double x, double y) 
    {
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) {
            // here we tweak the notify flag on the plot so that only
            // one notification happens even though we update multiple
            // axes...
            boolean savedNotify = plot.isNotify();
            plot.setNotify(false);
            Zoomable z = (Zoomable) plot;
            z.zoomDomainAxes(this.zoomInFactor, this.info.getPlotInfo(),
                    translateScreenToJava2D(new Point((int) x, (int) y)),
                    this.zoomAroundAnchor);
            plot.setNotify(savedNotify);
        }
    }

    public void zoomInRange(double x, double y) 
    {
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) {
            // here we tweak the notify flag on the plot so that only
            // one notification happens even though we update multiple
            // axes...
            boolean savedNotify = plot.isNotify();
            plot.setNotify(false);
            Zoomable z = (Zoomable) plot;
            z.zoomRangeAxes(this.zoomInFactor, this.info.getPlotInfo(),
                    translateScreenToJava2D(new Point((int) x, (int) y)),
                    this.zoomAroundAnchor);
            plot.setNotify(savedNotify);
        }
    }

    public void zoomOutBoth(double x, double y) 
    {
        Plot plot = this.chart.getPlot();
        if (plot == null) {
            return;
        }
        // here we tweak the notify flag on the plot so that only
        // one notification happens even though we update multiple
        // axes...
        boolean savedNotify = plot.isNotify();
        plot.setNotify(false);
        zoomOutDomain(x, y);
        zoomOutRange(x, y);
        plot.setNotify(savedNotify);
    }

    public void zoomOutDomain(double x, double y) 
    {
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) {
            // here we tweak the notify flag on the plot so that only
            // one notification happens even though we update multiple
            // axes...
            boolean savedNotify = plot.isNotify();
            plot.setNotify(false);
            Zoomable z = (Zoomable) plot;
            z.zoomDomainAxes(this.zoomOutFactor, this.info.getPlotInfo(),
                    translateScreenToJava2D(new Point((int) x, (int) y)),
                    this.zoomAroundAnchor);
            plot.setNotify(savedNotify);
        }
    }

    public void zoomOutRange(double x, double y) 
    {
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) {
            // here we tweak the notify flag on the plot so that only
            // one notification happens even though we update multiple
            // axes...
            boolean savedNotify = plot.isNotify();
            plot.setNotify(false);
            Zoomable z = (Zoomable) plot;
            z.zoomRangeAxes(this.zoomOutFactor, this.info.getPlotInfo(),
                    translateScreenToJava2D(new Point((int) x, (int) y)),
                    this.zoomAroundAnchor);
            plot.setNotify(savedNotify);
        }
    }

    public void zoom(Rectangle2D selection) 
    {

        // get the origin of the zoom selection in the Java2D space used for
        // drawing the chart (that is, before any scaling to fit the panel)
        Point2D selectOrigin = translateScreenToJava2D(new Point(
                (int) Math.ceil(selection.getX()),
                (int) Math.ceil(selection.getY())));
        PlotRenderingInfo plotInfo = this.info.getPlotInfo();
        Rectangle2D scaledDataArea = getScreenDataArea(
                (int) selection.getCenterX(), (int) selection.getCenterY());
        if ((selection.getHeight() > 0) && (selection.getWidth() > 0)) {

            double hLower = (selection.getMinX() - scaledDataArea.getMinX())
                    / scaledDataArea.getWidth();
            double hUpper = (selection.getMaxX() - scaledDataArea.getMinX())
                    / scaledDataArea.getWidth();
            double vLower = (scaledDataArea.getMaxY() - selection.getMaxY())
                    / scaledDataArea.getHeight();
            double vUpper = (scaledDataArea.getMaxY() - selection.getMinY())
                    / scaledDataArea.getHeight();

            Plot p = this.chart.getPlot();
            if (p instanceof Zoomable) {
                // here we tweak the notify flag on the plot so that only
                // one notification happens even though we update multiple
                // axes...
                boolean savedNotify = p.isNotify();
                p.setNotify(false);
                Zoomable z = (Zoomable) p;
                if (z.getOrientation() == PlotOrientation.HORIZONTAL) {
                    z.zoomDomainAxes(vLower, vUpper, plotInfo, selectOrigin);
                    z.zoomRangeAxes(hLower, hUpper, plotInfo, selectOrigin);
                }
                else {
                    z.zoomDomainAxes(hLower, hUpper, plotInfo, selectOrigin);
                    z.zoomRangeAxes(vLower, vUpper, plotInfo, selectOrigin);
                }
                p.setNotify(savedNotify);
            }

        }
    }

    public void restoreAutoBounds() 
    {
        Plot plot = this.chart.getPlot();
        if (plot == null) {
            return;
        }
        // here we tweak the notify flag on the plot so that only
        // one notification happens even though we update multiple
        // axes...
        boolean savedNotify = plot.isNotify();
        plot.setNotify(false);
        restoreAutoDomainBounds();
        restoreAutoRangeBounds();
        plot.setNotify(savedNotify);
    }

    public void restoreAutoDomainBounds() 
    {
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) {
            Zoomable z = (Zoomable) plot;
            // here we tweak the notify flag on the plot so that only
            // one notification happens even though we update multiple
            // axes...
            boolean savedNotify = plot.isNotify();
            plot.setNotify(false);
            // we need to guard against this.zoomPoint being null
            Point2D zp = (this.zoomPoint != null
                    ? this.zoomPoint : new Point());
            z.zoomDomainAxes(0.0, this.info.getPlotInfo(), zp);
            plot.setNotify(savedNotify);

        }
    }

    public void restoreAutoRangeBounds() 
    {
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) {
            Zoomable z = (Zoomable) plot;
            // here we tweak the notify flag on the plot so that only
            // one notification happens even though we update multiple
            // axes...
            boolean savedNotify = plot.isNotify();
            plot.setNotify(false);
            // we need to guard against this.zoomPoint being null
            Point2D zp = (this.zoomPoint != null
                    ? this.zoomPoint : new Point());
            z.zoomRangeAxes(0.0, this.info.getPlotInfo(), zp);
            plot.setNotify(savedNotify);
        }
    }

    public Rectangle2D getScreenDataArea() 
    {
        Rectangle2D dataArea = this.info.getPlotInfo().getDataArea();
        Insets insets = getInsets();
        double x = dataArea.getX() + insets.left + horizontalSpace;
        double y = dataArea.getY() + insets.top + verticalSpace;
        double w = dataArea.getWidth();
        double h = dataArea.getHeight();
        return new Rectangle2D.Double(x, y, w, h);
    }

    public Rectangle2D getScreenDataArea(int x, int y) {
        PlotRenderingInfo plotInfo = this.info.getPlotInfo();
        Rectangle2D result;
        if (plotInfo.getSubplotCount() == 0) {
            result = getScreenDataArea();
        }
        else {
            // get the origin of the zoom selection in the Java2D space used for
            // drawing the chart (that is, before any scaling to fit the panel)
            Point2D selectOrigin = translateScreenToJava2D(new Point(x, y));
            int subplotIndex = plotInfo.getSubplotIndex(selectOrigin);
            if (subplotIndex == -1) {
                return null;
            }
            result = scale(plotInfo.getSubplotInfo(subplotIndex).getDataArea());
        }
        return result;
    }

    public int getInitialDelay() {
        return this.ownToolTipInitialDelay;
    }

    public int getReshowDelay() {
        return this.ownToolTipReshowDelay;
    }

    public int getDismissDelay() {
        return this.ownToolTipDismissDelay;
    }

    public void setInitialDelay(int delay) {
        this.ownToolTipInitialDelay = delay;
    }

    public void setReshowDelay(int delay) {
        this.ownToolTipReshowDelay = delay;
    }

    public void setDismissDelay(int delay) {
        this.ownToolTipDismissDelay = delay;
    }

    public double getZoomInFactor() {
        return this.zoomInFactor;
    }

    public void setZoomInFactor(double factor) {
        this.zoomInFactor = factor;
    }

    public double getZoomOutFactor() {
        return this.zoomOutFactor;
    }

    public void setZoomOutFactor(double factor) {
        this.zoomOutFactor = factor;
    }

    private void drawZoomRectangle(Graphics2D g2, boolean xor) {
        if (this.zoomRectangle != null) {
            if (xor) {
                // Set XOR mode to draw the zoom rectangle
                g2.setXORMode(Color.gray);
            }
            if (this.fillZoomRectangle) {
                g2.setPaint(this.zoomFillPaint);
                g2.fill(this.zoomRectangle);
            }
            else {
                g2.setPaint(this.zoomOutlinePaint);
                g2.draw(this.zoomRectangle);
            }
            if (xor) {
                // Reset to the default 'overwrite' mode
                g2.setPaintMode();
            }
        }
    }

    private void drawHorizontalAxisTrace(Graphics2D g2, int x) 
    {
        Rectangle2D dataArea = getScreenDataArea();

        g2.setXORMode(Color.orange);
        if (((int) dataArea.getMinX() < x) && (x < (int) dataArea.getMaxX())) {

            if (this.verticalTraceLine != null) {
                g2.draw(this.verticalTraceLine);
                this.verticalTraceLine.setLine(x, (int) dataArea.getMinY(), x,
                        (int) dataArea.getMaxY());
            }
            else {
                this.verticalTraceLine = new Line2D.Float(x,
                        (int) dataArea.getMinY(), x, (int) dataArea.getMaxY());
            }
            g2.draw(this.verticalTraceLine);
        }

        // Reset to the default 'overwrite' mode
        g2.setPaintMode();
    }

    private void drawVerticalAxisTrace(Graphics2D g2, int y) 
    {
        Rectangle2D dataArea = getScreenDataArea();

        g2.setXORMode(Color.orange);
        if (((int) dataArea.getMinY() < y) && (y < (int) dataArea.getMaxY())) {

            if (this.horizontalTraceLine != null) {
                g2.draw(this.horizontalTraceLine);
                this.horizontalTraceLine.setLine((int) dataArea.getMinX(), y,
                        (int) dataArea.getMaxX(), y);
            }
            else {
                this.horizontalTraceLine = new Line2D.Float(
                        (int) dataArea.getMinX(), y, (int) dataArea.getMaxX(),
                        y);
            }
            g2.draw(this.horizontalTraceLine);
        }

        // Reset to the default 'overwrite' mode
        g2.setPaintMode();
    }

    //the changes introduced  by paradoxoff in his patch are applied
    public void doCopy() 
    {
        int width = getWidth();
        int height = getHeight();
        if(useChartSize && this.chart.getUseFixedChartAreaSize())
        {
            Size2D chartSize = chart.getPreferredChartAreaSize();
            width = (int)(chartSize.getWidth());
            height = (int)(chartSize.getHeight());
        }

        BufferedImage image = createBufferedImage(chart, width, height);
        ImageTransferable selection = new ImageTransferable(image);

        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        systemClipboard.setContents(selection, null);
    }

    private BufferedImage createBufferedImage(JFreeChart chart, int w, int h) {

        BufferedImage image = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        Rectangle2D chartArea = new Rectangle2D.Double(0.0, 0.0, w, h);

        chart.draw(g2, chartArea, null, null);
        g2.dispose();
        return image;
    }

    public void createChartPrintJob() 
    {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();
        PageFormat pf2 = job.pageDialog(pf);
        if (pf2 != pf) {
            job.setPrintable(this, pf2);
            if (job.printDialog()) 
            {
                try 
                {
                    job.print();
                }
                catch (PrinterException e) {
                    JOptionPane.showMessageDialog(this, e);
                }
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) 
    {
        if (pageIndex != 0) 
        {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2 = (Graphics2D) g;
        double x = pf.getImageableX();
        double y = pf.getImageableY();
        double w = pf.getImageableWidth();
        double h = pf.getImageableHeight();
        this.chart.draw(g2, new Rectangle2D.Double(x, y, w, h), this.anchor,
                null);
        return PAGE_EXISTS;

    }

    public void addChartMouseListener(CustomChartMouseListener listener) 
    {
        Validation.requireNonNullParameterName(listener, "listener");

        this.chartMouseListeners.add(CustomChartMouseListener.class, listener);
    }

    public void removeChartMouseListener(CustomChartMouseListener listener) 
    {
        this.chartMouseListeners.remove(CustomChartMouseListener.class, listener);
    }

    public void addChartMouseWheelListener(CustomChartMouseWheelListener listener) 
    {
        Validation.requireNonNullParameterName(listener, "listener");

        this.chartMouseWheelListeners.add(CustomChartMouseWheelListener.class, listener);
    }

    public void removeChartMouseWheelListener(CustomChartMouseWheelListener listener) 
    {
        this.chartMouseWheelListeners.remove(CustomChartMouseWheelListener.class, listener);
    }

    @Override
    public  <T extends EventListener> T[] getListeners(Class<T> listenerType) 
    {
        if (listenerType == ChartMouseListener.class) {
            // fetch listeners from local storage
            return this.chartMouseListeners.getListeners(listenerType);
        }
        else 
        {
            return super.getListeners(listenerType);
        }
    }

    protected final JPopupMenu createPopupMenu(boolean properties, boolean copy, boolean save, boolean print, boolean zoom) 
    {
        JPopupMenu result = new JPopupMenu("Chart:");
        boolean separator = false;
        if (properties) 
        {
            JMenuItem propertiesItem = new JMenuItem(propertiesAction);
            result.add(propertiesItem);
            separator = true;
        }

        if (copy) 
        {
            if (separator) 
            {
                result.addSeparator();
                separator = false;
            }
            JMenuItem copyItem = new JMenuItem(copyAction);
            result.add(copyItem);
            separator = !save;
        }

        if (save) 
        {
            if (separator) 
            {
                result.addSeparator();
                separator = false;
            }
            JMenuItem saveItem = new JMenuItem(saveAction);

            result.add(saveItem);
            separator = true;
        }

        if (print) {
            if (separator) {
                result.addSeparator();
                separator = false;
            }
            JMenuItem printItem = new JMenuItem(printAction);
            result.add(printItem);
            separator = true;
        }

        if (separator) 
        {
            result.addSeparator();
            separator = false;
        }

        JCheckBoxMenuItem lockAspectItem = new JCheckBoxMenuItem(lockAspectAction);
        result.add(lockAspectItem);

        if (zoom) 
        {
            JMenu zoomInMenu = new JMenu("Zoom in");

            zoomInBothMenuItem = new JMenuItem("All axes");
            zoomInBothMenuItem.setActionCommand(ZOOM_IN_BOTH_COMMAND);
            zoomInBothMenuItem.addActionListener(this);
            zoomInMenu.add(zoomInBothMenuItem);

            zoomInMenu.addSeparator();

            zoomInDomainMenuItem = new JMenuItem("Domain axis");
            zoomInDomainMenuItem.setActionCommand(ZOOM_IN_DOMAIN_COMMAND);
            zoomInDomainMenuItem.addActionListener(this);
            zoomInMenu.add(zoomInDomainMenuItem);

            zoomInRangeMenuItem = new JMenuItem("Range axis");
            zoomInRangeMenuItem.setActionCommand(ZOOM_IN_RANGE_COMMAND);
            zoomInRangeMenuItem.addActionListener(this);
            zoomInMenu.add(zoomInRangeMenuItem);

            result.add(zoomInMenu);

            JMenu zoomOutMenu = new JMenu("Zoom out");

            zoomOutBothMenuItem = new JMenuItem("All axes");
            zoomOutBothMenuItem.setActionCommand(ZOOM_OUT_BOTH_COMMAND);
            zoomOutBothMenuItem.addActionListener(this);
            zoomOutMenu.add(zoomOutBothMenuItem);

            zoomOutMenu.addSeparator();

            zoomOutDomainMenuItem = new JMenuItem("Domain axis");
            zoomOutDomainMenuItem.setActionCommand(
                    ZOOM_OUT_DOMAIN_COMMAND);
            zoomOutDomainMenuItem.addActionListener(this);
            zoomOutMenu.add(zoomOutDomainMenuItem);

            zoomOutRangeMenuItem = new JMenuItem("Range axis");
            zoomOutRangeMenuItem.setActionCommand(ZOOM_OUT_RANGE_COMMAND);
            zoomOutRangeMenuItem.addActionListener(this);
            zoomOutMenu.add(zoomOutRangeMenuItem);

            result.add(zoomOutMenu);

            JMenu autoRangeMenu = new JMenu("Axes auto range");

            zoomResetBothMenuItem = new JMenuItem("All axes");
            zoomResetBothMenuItem.setActionCommand(
                    ZOOM_RESET_BOTH_COMMAND);
            zoomResetBothMenuItem.addActionListener(this);
            autoRangeMenu.add(zoomResetBothMenuItem);

            autoRangeMenu.addSeparator();
            zoomResetDomainMenuItem = new JMenuItem("Domain axis");
            zoomResetDomainMenuItem.setActionCommand(
                    ZOOM_RESET_DOMAIN_COMMAND);
            zoomResetDomainMenuItem.addActionListener(this);
            autoRangeMenu.add(zoomResetDomainMenuItem);

            zoomResetRangeMenuItem = new JMenuItem("Range axis");
            zoomResetRangeMenuItem.setActionCommand(
                    ZOOM_RESET_RANGE_COMMAND);
            zoomResetRangeMenuItem.addActionListener(this);
            autoRangeMenu.add(zoomResetRangeMenuItem);

            result.addSeparator();
            result.add(autoRangeMenu);
            result.addSeparator();
        }

        return result;
    }

    protected void displayPopupMenu(int x, int y) 
    {
        if (this.popup == null|| this.chart == null || !isPopupDisplayable(x, y)) 
        {
            return;
        }

        // go through each zoom menu item and decide whether or not to
        // enable it...
        boolean isDomainZoomable = false;
        boolean isRangeZoomable = false;
        Plot plot = (this.chart != null ? this.chart.getPlot() : null);
        if (plot instanceof Zoomable) 
        {
            Zoomable z = (Zoomable) plot;
            isDomainZoomable = z.isDomainZoomable();
            isRangeZoomable = z.isRangeZoomable();
        }

        if (this.zoomInDomainMenuItem != null) 
        {
            this.zoomInDomainMenuItem.setEnabled(isDomainZoomable);
        }
        if (this.zoomOutDomainMenuItem != null) 
        {
            this.zoomOutDomainMenuItem.setEnabled(isDomainZoomable);
        }
        if (this.zoomResetDomainMenuItem != null) 
        {
            this.zoomResetDomainMenuItem.setEnabled(isDomainZoomable);
        }

        if (this.zoomInRangeMenuItem != null) 
        {
            this.zoomInRangeMenuItem.setEnabled(isRangeZoomable);
        }
        if (this.zoomOutRangeMenuItem != null) 
        {
            this.zoomOutRangeMenuItem.setEnabled(isRangeZoomable);
        }

        if (this.zoomResetRangeMenuItem != null) 
        {
            this.zoomResetRangeMenuItem.setEnabled(isRangeZoomable);
        }

        if (this.zoomInBothMenuItem != null) 
        {
            this.zoomInBothMenuItem.setEnabled(isDomainZoomable
                    && isRangeZoomable);
        }
        if (this.zoomOutBothMenuItem != null) 
        {
            this.zoomOutBothMenuItem.setEnabled(isDomainZoomable
                    && isRangeZoomable);
        }
        if (this.zoomResetBothMenuItem != null) 
        {
            this.zoomResetBothMenuItem.setEnabled(isDomainZoomable
                    && isRangeZoomable);
        }

        this.popup.show(this, x, y);

    }

    @Override
    public void updateUI() 
    {
        // here we need to update the UI for the popup menu, if the panel
        // has one...
        if (this.popup != null) {
            SwingUtilities.updateComponentTreeUI(this.popup);
        }
        super.updateUI();
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint(this.zoomFillPaint, stream);
        SerialUtilities.writePaint(this.zoomOutlinePaint, stream);
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.zoomFillPaint = SerialUtilities.readPaint(stream);
        this.zoomOutlinePaint = SerialUtilities.readPaint(stream);

        // we create a new but empty chartMouseListeners list
        this.chartMouseListeners = new EventListenerList();

        // register as a listener with sub-components...
        if (this.chart != null) {
            this.chart.addChangeListener(this);
        }

    }


    private class PropertiesAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PropertiesAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Live chart style");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            doEditChartProperties();
        }
    }

    private class CopyAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CopyAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Copy");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            doCopy();
        }
    }

    private class PrintAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PrintAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Print");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            createChartPrintJob();
        }
    }

    private class SaveAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Save as");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            try 
            {
                doSaveAs();
            }
            catch (IOException e) 
            {
                e.printStackTrace();
            }		
        }
    }

    private class LockAspectRatioAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public LockAspectRatioAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);		
            putValue(NAME,"Lock aspect");			
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            boolean locked = (boolean) getValue(SELECTED_KEY);
            lockAspectRatio(locked);
        }
    }

    public static class MultipleChartFactory implements 
    AbstractChartPanelFactory<MultipleChartPanel<CustomizableXYBaseChart<?>>>
    {
        private static final MultipleChartFactory INSTANCE = new MultipleChartFactory();

        public static MultipleChartFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public MultipleChartPanel<CustomizableXYBaseChart<?>> buildEmptyPanel() 
        {
            return new MultipleChartPanel<>();
        }    	
    }

    public static class MultipleChartFactoryUglyHack 
    implements AbstractChartPanelFactory<MultipleChartPanel<ChannelChart<Channel1DPlot>>>
    {
        private static final MultipleChartFactoryUglyHack INSTANCE = new MultipleChartFactoryUglyHack();

        public static MultipleChartFactoryUglyHack getInstance()
        {
            return INSTANCE;
        }

        @Override
        public MultipleChartPanel<ChannelChart<Channel1DPlot>> buildEmptyPanel() 
        {
            return new MultipleChartPanel<>();
        }

    }

    public MouseInputMode getMode()
    {
        return mode;
    }

    public void setMode(MouseInputMode mode)
    {
        this.mode = mode;

        E selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.setMode(mode);
        }   
    }


    public MouseInputMode getMode(MouseInputType inputType)
    {
        MouseInputMode accessoryMode = this.accessoryModes.get(inputType);

        if(accessoryMode != null)
        {
            return accessoryMode;
        }

        return this.mode;
    }

    public void setAccessoryMode(MouseInputType inputType, MouseInputMode modeNew)
    {        
        this.accessoryModes.put(inputType, modeNew);

        E selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.setAccessoryMode(inputType, modeNew);
        }   
    }

    @Override
    public void requestCursorChange(Cursor cursor) 
    {
        setCursor(cursor);
    }
}

