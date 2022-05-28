package atomicJ.gui;


import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.ObjectUtilities;

import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.PointRepositioning1DTransformation;
import atomicJ.curveProcessing.Translate1DTransformation;


class DataModificationManager implements MouseInputResponse
{
    private final ChannelChart<?> chart;    

    private Channel1DModificationSupervisor dataItemSupervisor;

    private int caughtItemIndex = -1;
    private boolean datasetPathCaught;

    private Channel1DDataset caughtDataset;
    private final Object movableDatasetKey;

    DataModificationManager(ChannelChart<?> chart, Object movableDatasetKey) 
    {
        this.chart = chart;
        this.movableDatasetKey = movableDatasetKey;
    }

    public void setDataItemSupervisor(Channel1DModificationSupervisor supervisor)
    {
        this.dataItemSupervisor = supervisor;
    }

    @Override
    public boolean isRightClickReserved(Rectangle2D dataArea, Point2D dataPoint)
    {
        return false;
    }

    @Override
    public void mousePressed(CustomChartMouseEvent event) 
    {                
        if(!this.chart.isMoveDataItems(MouseInputType.PRESSED, movableDatasetKey))
        {
            return;
        }

        this.caughtItemIndex = -1;
        this.caughtDataset = null;
        this.datasetPathCaught = false;

        Point2D java2DPoint = event.getJava2DPoint();

        ChartEntity entity = event.getEntity();

        if(this.chart.isMoveDataItems(MouseInputType.PRESSED, movableDatasetKey, DataModificationType.POINT_MOVEABLE) && entity instanceof XYItemEntity)
        {            
            XYItemEntity itemEntity = (XYItemEntity)entity;
            XYDataset dataset = itemEntity.getDataset();

            if(dataset instanceof Channel1DDataset)
            {
                Channel1DDataset processableDataset = (Channel1DDataset)dataset;
                boolean rightDataset = ObjectUtilities.equal(movableDatasetKey, processableDataset.getKey());
                Shape area = itemEntity.getArea();
                if(rightDataset && area.contains(java2DPoint))
                {
                    this.caughtDataset = processableDataset;                  
                    this.caughtItemIndex = itemEntity.getItem();
                }
            }
        }   

        if(this.chart.isMoveDataItems(MouseInputType.PRESSED, movableDatasetKey, DataModificationType.WHOLE_DATASET_MOVEABLE) && entity instanceof XYLinePathEntity)
        {
            XYLinePathEntity itemEntity = (XYLinePathEntity)entity;
            XYDataset dataset = itemEntity.getDataset();

            if(dataset instanceof Channel1DDataset)
            {
                Channel1DDataset processableDataset = (Channel1DDataset)dataset;
                boolean rightDataset = ObjectUtilities.equal(movableDatasetKey, processableDataset.getKey());
                Shape area = itemEntity.getArea();

                if(rightDataset && area.contains(java2DPoint))
                {
                    this.caughtDataset = processableDataset;   
                    this.datasetPathCaught = true;
                }
            }
        }
    }

    @Override
    public void mouseReleased(CustomChartMouseEvent event) 
    {
        if(!this.chart.isMoveDataItems(MouseInputType.RELEASED, movableDatasetKey))
        {
            return;
        }

        this.caughtDataset = null;
        this.caughtItemIndex = -1;
        this.datasetPathCaught = false;
    }

    @Override
    public void mouseDragged(CustomChartMouseEvent event) 
    {
        if(!this.chart.isMoveDataItems(MouseInputType.DRAGGED, movableDatasetKey))
        {
            return;
        }

        if(event.isConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED))
        {
            return;
        }

        if(caughtDataset != null)
        {
            Point2D dataPoint = event.getDataPoint();

            boolean pointMoveable = this.chart.isMoveDataItems(MouseInputType.DRAGGED, movableDatasetKey, DataModificationType.POINT_MOVEABLE);
            boolean wholeDatasetMoveable = this.chart.isMoveDataItems(MouseInputType.DRAGGED, movableDatasetKey, DataModificationType.WHOLE_DATASET_MOVEABLE);

            //moves single point
            if(pointMoveable && caughtItemIndex > -1 && dataItemSupervisor.isValidValue(caughtDataset.getDisplayedChannel(), caughtItemIndex, new double[] {dataPoint.getX(), dataPoint.getY()}))
            {
                Point2D correctedDataPoint = dataItemSupervisor.correctPosition(caughtDataset.getDisplayedChannel(), caughtItemIndex, dataPoint);
                Channel1DDataTransformation tr = new PointRepositioning1DTransformation(caughtItemIndex, correctedDataPoint.getX(), correctedDataPoint.getY());

                this.caughtDataset.transform(tr);
                dataItemSupervisor.itemMoved(caughtDataset.getDisplayedChannel(), caughtItemIndex, new double[] {correctedDataPoint.getX(), correctedDataPoint.getY()});
            }
            else if(wholeDatasetMoveable && datasetPathCaught)
            {
                //moves whole dataset

                Point2D caughtPoint = this.chart.getCaughtPoint();

                double tx = dataPoint.getX() - caughtPoint.getX();
                double ty = dataPoint.getY() - caughtPoint.getY();

                Channel1DDataTransformation tr = new Translate1DTransformation(tx, ty);
                this.caughtDataset.transform(tr);
                dataItemSupervisor.channelTranslated(caughtDataset.getDisplayedChannel());
            }           
        }              
    }

    @Override
    public void mouseMoved(CustomChartMouseEvent event) 
    {
        if(!this.chart.isMoveDataItems(MouseInputType.MOVED, movableDatasetKey))
        {
            return;
        }

        if(event.isConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED))
        {
            return;
        }

        Point2D java2DPoint = event.getJava2DPoint();

        ChartEntity entity = event.getEntity();

        if(this.chart.isMoveDataItems(MouseInputType.MOVED, movableDatasetKey, DataModificationType.WHOLE_DATASET_MOVEABLE) && entity instanceof XYLinePathEntity)
        {            
            XYLinePathEntity itemEntity = (XYLinePathEntity)entity;
            XYDataset dataset = itemEntity.getDataset();

            if(dataset instanceof ProcessableXYDataset<?>)
            {
                ProcessableXYDataset<?> processableDataset = (ProcessableXYDataset<?>)dataset;
                boolean rightDataset = ObjectUtilities.equal(movableDatasetKey, processableDataset.getKey());
                Shape area = itemEntity.getArea();
                if(rightDataset && area.contains(java2DPoint))
                {
                    this.dataItemSupervisor.requestCursorChange(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));        
                    event.setConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED, true);
                    return;
                }
            }
        }   

        this.dataItemSupervisor.requestCursorChange(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); 
    }

    @Override
    public void mouseClicked(CustomChartMouseEvent event) 
    {
        if(event.isConsumed(CustomizableXYBaseChart.CHART_EDITION))
        {
            return;
        }

        if(!this.chart.isMoveDataItems(MouseInputType.CLICKED, movableDatasetKey))
        {
            return;                      
        }
    }

    @Override
    public boolean isChartElementCaught() 
    {
        boolean pointCaught = this.chart.isMoveDataItems(MouseInputType.DRAGGED, movableDatasetKey, DataModificationType.POINT_MOVEABLE) && caughtItemIndex > -1;
        boolean wholeDatasetCaught = this.chart.isMoveDataItems(MouseInputType.DRAGGED, movableDatasetKey, DataModificationType.WHOLE_DATASET_MOVEABLE) && datasetPathCaught;

        boolean elementCaught = (pointCaught || wholeDatasetCaught);

        return elementCaught;
    }
}