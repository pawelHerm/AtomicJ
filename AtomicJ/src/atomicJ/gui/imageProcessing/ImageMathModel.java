package atomicJ.gui.imageProcessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.gui.generalProcessing.BasicOperationModel;
import atomicJ.gui.generalProcessing.ProcessingModel;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.imageProcessing.AddImageGeometrically;
import atomicJ.imageProcessing.DivideImageGeometrically;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.MultiplyImageGeometrically;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.Channel2DResourceView;
import atomicJ.sources.Channel2DSource;
import atomicJ.utilities.MultiMap;


public class ImageMathModel extends ProcessingModel
{
    public static final String SOURCE = "Source";
    public static final String IDENTIFIER = "Identifier";    
    public static final String MATH_OPERATION = "MathOperation";
    public static final String CREATE_NEW_IMAGE = "CreateNewImage";
    public static final String EXPORT_ENABLED = "ExportEnabled";

    private Channel2DSource<?> source;

    private String identifier;

    private ImageMathOperation operation = ImageMathOperation.ADD;

    private boolean createNewImage = false;

    private boolean applyEnabled = true;

    private UndoableCommand command;

    private final Channel2DResourceView manager;

    public ImageMathModel(Channel2DResourceView manager)
    {
        super(manager.getDrawableROIs(), manager.getROIUnion(), manager.getUnitManager());
        this.manager = manager;

        Channel2DResource resource = manager.getSelectedResource();
        Set<Channel2DSource<?>> availableSources = resource.getSourceChannelIdentifierMaps().keySet();

        if(!availableSources.isEmpty())
        {
            this.source = availableSources.iterator().next();
        }

        List<String> identifiers = resource.getSourceChannelIdentifierMaps().get(source);

        if(!identifiers.isEmpty())
        {
            this.identifier = identifiers.get(0);
        }
    }


    public Channel2DSource<?> getSource()
    {
        return source;
    }

    private void ensureSourceIdentifierConsistency()
    {
        List<String> availableIdentifiers = getAvailableIdentifiers();

        if(!availableIdentifiers.contains(identifier))
        {
            String identifierNew = availableIdentifiers.isEmpty() ? null : availableIdentifiers.get(0);
            setIdentifier(identifierNew);
        }           
    }

    public void setSource(Channel2DSource<?> firstSourceNew)
    {       
        if(this.source != firstSourceNew)
        {
            Channel2DSource<?> firstSourceOld = this.source;
            this.source = firstSourceNew;

            firePropertyChange(SOURCE, firstSourceOld, firstSourceNew);

            ensureSourceIdentifierConsistency();
            checkIfInputSpecified();
        }
    }

    public String getFirstIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String firstIdentifierNew)
    {
        if(!Objects.equals(identifier, firstIdentifierNew))
        {
            String firstIdentifierOld = this.identifier;
            this.identifier = firstIdentifierNew;

            firePropertyChange(IDENTIFIER, firstIdentifierOld, firstIdentifierNew);

            checkIfInputSpecified();
        }
    }

    public ImageMathOperation getOperation()
    {
        return operation;
    }

    public void setOperation(ImageMathOperation operationNew)
    {
        if(this.operation != operationNew)
        {
            ImageMathOperation operationOld = this.operation;
            this.operation = operationNew;

            firePropertyChange(MATH_OPERATION, operationOld, operationNew);
        }
    }

    public boolean isCreateNewImage()
    {
        return createNewImage;
    }

    public void setCreateNewImage(boolean createNewImageNew)
    {
        if(this.createNewImage != createNewImageNew)
        {
            boolean createNewImageOld = this.createNewImage;
            this.createNewImage = createNewImageNew;

            firePropertyChange(CREATE_NEW_IMAGE, createNewImageOld, createNewImageNew);
        }
    }

    public List<String> getAvailableIdentifiers()
    {
        MultiMap<Channel2DSource<?>, String> allSources = getSourceChannelIdentifierMaps();


        List<String> identifiers = allSources.get(source);

        return identifiers;
    }

    public List<Channel2DSource<?>> getAvailableSources()
    {
        MultiMap<Channel2DSource<?>, String> allSources = getSourceChannelIdentifierMaps();


        List<Channel2DSource<?>> sources = new ArrayList<>(allSources.keySet());

        return sources;
    }

    public  MultiMap<Channel2DSource<?>, String> getSourceChannelIdentifierMaps()
    {
        MultiMap<Channel2DSource<?>, String> maps = new MultiMap<>();

        //adds the resources from the main manager
        for(Channel2DResource resource : manager.getResources())
        {
            maps.putAll(resource.getSourceChannelIdentifierMaps());
        }

        //adds the resources from the additional managers

        for(Channel2DResource resource : manager.getAdditionalResources())
        {
            maps.putAll(resource.getSourceChannelIdentifierMaps());
        }

        return maps;
    }

    private void checkIfInputSpecified()
    {
        MultiMap<Channel2DSource<?>, String> maps = getSourceChannelIdentifierMaps();

        boolean applyEnabledNew = maps.contains(source, identifier);

        if(this.applyEnabled != applyEnabledNew)
        {
            boolean applyEnabledOld = this.applyEnabled;
            this.applyEnabled = applyEnabledNew;

            firePropertyChange(BasicOperationModel.APPLY_ENABLED, applyEnabledOld, applyEnabledNew);
        }  
    }

    public UndoableCommand getExecutedCommand()
    {
        UndoableCommand executedCommand = null;

        if(command != null && command.isExecuted())
        {
            executedCommand = command;
        }

        return executedCommand;
    }

    @Override
    public void apply()
    {
        Channel2D channel = source.getChannel(identifier);
        Channel2DData channelData = channel.getChannelData();

        Channel2DDataInROITransformation tr;

        if(ImageMathOperation.ADD.equals(operation))
        {   
            tr = new AddImageGeometrically(channelData, InterpolationMethod2D.BILINEAR, 1, 1);
        }
        else if(ImageMathOperation.SUBTRACT.equals(operation))
        {                   
            tr = new AddImageGeometrically(channelData, InterpolationMethod2D.BILINEAR, 1, -1);
        }
        else if(ImageMathOperation.AVERAGE.equals(operation))
        {   
            tr = new AddImageGeometrically(channelData, InterpolationMethod2D.BILINEAR, 0.5, 0.5);
        }
        else if(ImageMathOperation.MULTIPLY.equals(operation))
        {
            tr = new MultiplyImageGeometrically(channelData, InterpolationMethod2D.BILINEAR, 1);
        }
        else if(ImageMathOperation.DIVIDE.equals(operation))
        {
            tr = new DivideImageGeometrically(channelData, InterpolationMethod2D.BILINEAR, 1);
        }
        else
        {
            throw new IllegalArgumentException("The ImageMathOperation is not supported");
        }

        Channel2DResource resource = manager.getSelectedResource();
        String type = manager.getSelectedType();
        ROIRelativePosition position = getROIPosition();
        ROI roi = getSelectedROI();

        this.command =
                new UndoableImageROICommand(manager, type, null, resource, tr, position, roi);
        command.execute();  

    }

    @Override
    public void reset()
    {        
        if(command != null)
        {
            command.undo();
        }
    }
}
