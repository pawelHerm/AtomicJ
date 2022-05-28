package atomicJ.gui;

import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.ObjectUtilities;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.PointAddition1DTransformation;
import atomicJ.curveProcessing.PointRepositioning1DTransformation;
import atomicJ.curveProcessing.Translate1DTransformation;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.Channel1DStandard;
import atomicJ.data.ChannelGroupTag;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.ModificationConstraint1D;
import atomicJ.data.units.Quantity;

class DrawingManager implements MouseInputResponse
{
    private final ChannelChart<?> chart;

    private Channel1DModificationSupervisor drawingSupervisor;

    private Channel1DDataset datasetUnderConstruction;   

    private int caughtItemIndex = -1;
    private boolean datasetPathCaught;

    private Channel1DDataset caughtDataset;

    private final Object datasetGroupTagId;
    private final ModificationConstraint1D modificationConstraint;
    private final int maxPointCount;

    DrawingManager(ChannelChart<?> chart, Object datasetGroupTag, ModificationConstraint1D modificationConstraint, int maxPointCount)
    {
        this.chart = chart;
        this.datasetGroupTagId = datasetGroupTag;
        this.modificationConstraint = modificationConstraint;
        this.maxPointCount = maxPointCount;
    }

    public void setDataItemSupervisor(Channel1DModificationSupervisor supervisor)
    {
        this.drawingSupervisor = supervisor;
    }

    private Channel1DDataset getLineMousedDataset(ChartEntity entity)
    {
        Channel1DDataset lineMousedDataset = null;      

        if(entity instanceof XYLinePathEntity)
        {
            XYLinePathEntity lineEntity = (XYLinePathEntity)entity;
            XYDataset dataset = lineEntity.getDataset();

            if(dataset instanceof Channel1DDataset)
            {
                lineMousedDataset = (Channel1DDataset)dataset;
            }
        }

        return lineMousedDataset;
    }

    private Channel1DDataset getItemMousedDataset(ChartEntity entity)
    {
        Channel1DDataset itemMousedDataset = null;      

        if(entity instanceof XYItemEntity)
        {
            XYItemEntity itemEntity = (XYItemEntity)entity;
            XYDataset dataset = itemEntity.getDataset();

            if(dataset instanceof Channel1DDataset)
            {
                itemMousedDataset = (Channel1DDataset)dataset;
            }
        }

        return itemMousedDataset;
    }

    private Channel1DDataset getMousedDataset(ChartEntity entity)
    {
        Channel1DDataset lineMousedDataset = getLineMousedDataset(entity);
        Channel1DDataset mousedDataset = (lineMousedDataset != null) ? lineMousedDataset : getItemMousedDataset(entity);

        return mousedDataset;
    }

    @Override
    public void mousePressed(CustomChartMouseEvent event) 
    {
        if(!this.chart.isDrawDatasetMode(MouseInputType.PRESSED, datasetGroupTagId))
        {
            return;
        }

        Point2D java2DPoint = event.getJava2DPoint();

        ChartEntity entity = event.getEntity();


        if(!isDatasetUnderConstruction())
        {
            if(this.chart.isDrawDatasetMode(MouseInputType.PRESSED, datasetGroupTagId) && entity instanceof XYItemEntity)
            {            
                XYItemEntity itemEntity = (XYItemEntity)entity;
                XYDataset dataset = itemEntity.getDataset();

                if(dataset instanceof Channel1DDataset)
                {
                    Channel1DDataset processableDataset = (Channel1DDataset)dataset;
                    boolean rightDataset = ObjectUtilities.equal(datasetGroupTagId, processableDataset.getGroupTagId());

                    Shape area = itemEntity.getArea();
                    if(rightDataset && area.contains(java2DPoint))
                    {
                        this.caughtDataset = processableDataset;                  
                        this.caughtItemIndex = itemEntity.getItem();
                    }
                }
            }   

            if(this.chart.isDrawDatasetMode(MouseInputType.PRESSED, datasetGroupTagId) && entity instanceof XYLinePathEntity)
            {
                XYLinePathEntity lineEntity = (XYLinePathEntity)entity;
                XYDataset dataset = lineEntity.getDataset();

                if(dataset instanceof Channel1DDataset)
                {
                    Channel1DDataset processableDataset = (Channel1DDataset)dataset;
                    boolean rightDataset = ObjectUtilities.equal(datasetGroupTagId, processableDataset.getGroupTagId());
                    Shape area = lineEntity.getArea();
                    if(rightDataset && area.contains(java2DPoint))
                    {
                        this.caughtDataset = processableDataset;   
                        this.datasetPathCaught = true;
                    }
                }
            }
        }        
    }

    @Override
    public void mouseReleased(CustomChartMouseEvent event) 
    {
        if(!this.chart.isDrawDatasetMode(MouseInputType.RELEASED, datasetGroupTagId))
        {
            return;
        }

        caughtItemIndex = -1;
        datasetPathCaught = false;
        caughtDataset = null;
    }

    @Override
    public void mouseDragged(CustomChartMouseEvent event) 
    {
        if(!this.chart.isDrawDatasetMode(MouseInputType.DRAGGED, datasetGroupTagId))
        {
            return;
        }

        if(event.isConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED))
        {
            return;
        }

        Point2D dataPoint = event.getDataPoint();

        if(caughtDataset != null)
        {
            Channel1D caughtChannel = caughtDataset.getDisplayedChannel();

            //moves single point
            if(caughtItemIndex > -1 && drawingSupervisor.isValidValue(caughtChannel, caughtItemIndex, new double[] {dataPoint.getX(), dataPoint.getY()}))
            {
                Point2D correctedDataPoint = drawingSupervisor.correctPosition(caughtChannel, caughtItemIndex, dataPoint);
                Channel1DDataTransformation tr = new PointRepositioning1DTransformation(caughtItemIndex, correctedDataPoint.getX(), correctedDataPoint.getY());

                this.caughtDataset.transform(tr);
                drawingSupervisor.itemMoved(caughtChannel, caughtItemIndex, new double[] {correctedDataPoint.getX(), correctedDataPoint.getY()});
            }
            else if(datasetPathCaught)
            {
                //moves whole dataset

                Point2D caughtPoint = this.chart.getCaughtPoint();

                double tx = dataPoint.getX() - caughtPoint.getX();
                double ty = dataPoint.getY() - caughtPoint.getY();

                Channel1DDataTransformation tr = new Translate1DTransformation(tx, ty);
                this.caughtDataset.transform(tr);
                drawingSupervisor.channelTranslated(caughtChannel);
            }           
        }           
    }

    @Override
    public void mouseMoved(CustomChartMouseEvent event) 
    {        
        if(!this.chart.isDrawDatasetMode(MouseInputType.MOVED, datasetGroupTagId))
        {
            return;
        }

        if(event.isConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED))
        {
            return;
        }

        Point2D dataPoint = event.getDataPoint();

        drawingSupervisor.requestCursorChange(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        if(isDatasetUnderConstruction())
        {
            moveEndpointInCurrentDataset(dataPoint);
        }
        else 
        {       
            ChartEntity entity = event.getEntity();

            if(entity instanceof XYLinePathEntity)
            {            
                XYLinePathEntity lineEntity = (XYLinePathEntity)entity;
                XYDataset dataset = lineEntity.getDataset();

                if(dataset instanceof Channel1DDataset)
                {
                    Channel1DDataset processableDataset = (Channel1DDataset)dataset;  

                    if(Objects.equals(datasetGroupTagId, processableDataset.getGroupTagId()))
                    {
                        this.drawingSupervisor.requestCursorChange(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));        
                        event.setConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED, true);
                        return;
                    }
                }
            }   
            else if(entity instanceof XYItemEntity)
            {
                XYItemEntity itemEntity = (XYItemEntity)entity;
                XYDataset dataset = itemEntity.getDataset();
                Channel1DDataset processableDataset = (Channel1DDataset)dataset;                    
                if(Objects.equals(datasetGroupTagId, processableDataset.getGroupTagId()))
                {
                    this.drawingSupervisor.requestCursorChange(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); 
                    return;
                }
            }
        } 
    }

    @Override
    public void mouseClicked(CustomChartMouseEvent event) 
    {        
        if(!this.chart.isDrawDatasetMode(MouseInputType.CLICKED, datasetGroupTagId))
        {
            return;
        }

        if(event.isMultiple())
        {
            removeDataset(event);
        }
        else if(event.isLeft())
        {
            handleLeftSingleClick(event);
        }
        else 
        {    
            handleRightSingleClick(event);  
        }        
    }

    private void removeDataset(CustomChartMouseEvent event)
    {
        ChartEntity entity = event.getEntity();
        Channel1DDataset mousedDataset = getMousedDataset(entity);

        if(mousedDataset != null)
        {
            CustomizableXYPlot plot = this.chart.getCustomizablePlot();
            plot.removeLayer(mousedDataset.getKey());

            Channel1D mousedChannel = mousedDataset.getDisplayedChannel();
            this.drawingSupervisor.channelRemoved(mousedChannel);
        }
    }

    private void handleLeftSingleClick(CustomChartMouseEvent event)
    {
        Point2D dataPoint = event.getDataPoint();

        ChartEntity entity = event.getEntity();

        boolean isEmptySpace = (getLineMousedDataset(entity) == null);  

        if(isEmptySpace)
        {
            if(canNewPointBeAddedToDatasetUnderConstruction())
            {
                addNodePointToCurrentDataset(dataPoint);
            }
            else
            {
                beginOrFinishDataset(dataPoint);
            }
        }
    }

    private void handleRightSingleClick(CustomChartMouseEvent event)
    {
        if(canNewPointBeAddedToDatasetUnderConstruction())
        {
            addNodePointToCurrentDataset(event.getDataPoint());
            finishDataset();
        }
    }

    private void addNodePointToCurrentDataset(Point2D dataPoint)
    {
        Channel1DDataTransformation tr = new PointAddition1DTransformation(dataPoint.getX(), dataPoint.getY());

        this.datasetUnderConstruction.transform(tr);
        drawingSupervisor.itemAdded(datasetUnderConstruction.getDisplayedChannel(), new double[] {dataPoint.getX(), dataPoint.getY()}); 
    }

    @Override
    public boolean isRightClickReserved(Rectangle2D dataArea, Point2D dataPoint)
    {
        return canNewPointBeAddedToDatasetUnderConstruction();       
    }

    @Override
    public boolean isChartElementCaught() 
    {
        boolean caught = (caughtDataset != null);
        return caught;
    }

    private void beginOrFinishDataset(Point2D p)
    {
        if(!isDatasetUnderConstruction())
        {
            beginNewDataset(p);
        }
        else 
        {
            moveEndpointInCurrentDataset(p);
            finishDataset();
        }
    }

    private void beginNewDataset(Point2D anchor)
    {      
        if(this.chart.isDrawDatasetMode(datasetGroupTagId))
        {
            Quantity xQuantity = this.chart.getDomainDataQuantity();
            Quantity yQuantity = this.chart.getRangeDataQuantity();

            Channel1DData channelData = new FlexibleChannel1DData(new double[][] {{anchor.getX(), anchor.getY()}, {anchor.getX(), anchor.getY()}}, xQuantity, yQuantity, SortedArrayOrder.ASCENDING);

            ChannelGroupTag channelTag = drawingSupervisor.getNextGroupMemberTag(datasetGroupTagId); 
            String channelIdentifier = channelTag.getDefaultChannelIdentifier();

            Channel1D channel = new Channel1DStandard(channelData, channelIdentifier, channelIdentifier, modificationConstraint, channelTag);

            drawingSupervisor.channelAdded(channel);

            this.datasetUnderConstruction = new Channel1DDataset(channel, channelIdentifier);

            CustomizableXYPlot plot = this.chart.getCustomizablePlot();

            XYItemRenderer oldRender = plot.getLayerRenderer(channelIdentifier);
            StyleTag style = new IndexedStyleTag(channelTag.getGroupId(), channelTag.getIndex());
            XYItemRenderer renderer = (oldRender != null) ? oldRender : RendererFactory.getChannel1DRenderer(channel, style);
            plot.addOrReplaceLayer(channelIdentifier, datasetUnderConstruction, renderer);         
        }       
    }

    private void moveEndpointInCurrentDataset(Point2D endpoint)
    {
        if(isDatasetUnderConstruction())
        {
            Channel1D channelUnderConstruction = datasetUnderConstruction.getDisplayedChannel();
            int lastItemIndex = datasetUnderConstruction.getItemCount(0) - 1;

            Point2D correctedDataPoint = drawingSupervisor.correctPosition(channelUnderConstruction, lastItemIndex, endpoint);


            Channel1DDataTransformation tr = new PointRepositioning1DTransformation(lastItemIndex, correctedDataPoint.getX(), correctedDataPoint.getY());

            this.datasetUnderConstruction.transform(tr);
            drawingSupervisor.itemMoved(channelUnderConstruction, lastItemIndex, new double[] {correctedDataPoint.getX(), correctedDataPoint.getY()});
        }                           
    }

    private void finishDataset()
    {   
        datasetUnderConstruction = null;
    }

    public boolean isDatasetUnderConstruction()
    {
        return datasetUnderConstruction != null;
    }

    private boolean canNewPointBeAddedToDatasetUnderConstruction()
    {
        return datasetUnderConstruction != null && datasetUnderConstruction.getItemCount(0) < maxPointCount;
    }
}