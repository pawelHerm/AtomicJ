package atomicJ.readers.regularImage;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.util.List;

import atomicJ.data.Coordinate4D;
import atomicJ.data.units.UnitExpression;
import atomicJ.utilities.ColorUtilities;

public class BufferedImageChannelProvider implements ChannelProvider
{    
    private final BufferedImage image;
    private final Channel2DSourceMetadata imageMetadata;

    public BufferedImageChannelProvider(BufferedImage image)
    {
        this(image, DummyDensityMetadata.getInstance());
    }

    public BufferedImageChannelProvider(BufferedImage image, Channel2DSourceMetadata imageMetadata)
    {
        this.image = image;
        this.imageMetadata = imageMetadata;
    }

    @Override
    public Channel2DSourceMetadata getImageMetadata()
    {
        return imageMetadata;
    }

    @Override
    public UnitExpression getXLength()
    {
        return null;
    }

    @Override
    public UnitExpression getYLength()
    {
        return null;
    }


    @Override
    public int getRowCount()
    {
        return image.getHeight();
    }

    @Override
    public int getColumnCount()
    {
        return image.getWidth();
    }

    @Override
    public String getColorSpaceName()
    {
        ColorModel colorModel = image.getColorModel();
        ColorSpace colorSpace = colorModel.getColorSpace();

        return ColorUtilities.getColorSpaceName(colorSpace.getType());
    }

    @Override
    public List<String> getChannelNames()
    {

        ColorModel colorModel = image.getColorModel();
        ColorSpace colorSpace = colorModel.getColorSpace();

        return ColorUtilities.getColorChannelNames(colorSpace, colorModel.hasAlpha());
    }

    @Override
    public double[][][] getChannelData(ImageInterpretationModel model)
    {
        int columnCount = model.getImageColumnCount();
        int rowCount = model.getImageRowCount();

        double[][][] bands = null;
        Object outData = null;
        int[] components = null;

        Raster raster = image.getData();
        ColorModel colorModel = image.getColorModel();

        int originX = raster.getMinX();
        int originY = raster.getMinY();
        int maxY = originY + rowCount;
        int maxX = originX + columnCount;


        int numComponents = colorModel.getNumComponents();

        if(model.isCombineChannels())
        {
            double[][] data = new double[rowCount][columnCount];

            bands = new double[][][] {data};

            double[] coefficients = model.getCombinationCoefficients();

            for(int i = originY; i<maxY; i++)
            {
                for(int j = originX; j<maxX; j++)
                {
                    outData = raster.getDataElements(j, i,  outData);
                    components = colorModel.getComponents(outData, components, 0);

                    double val = 0;

                    for(int k = 0; k<numComponents; k++)
                    {
                        int colorComponent = components[k];
                        double coeff = coefficients[k];

                        val += coeff*colorComponent;
                    }

                    data[maxY - 1 - i][j] = val; 
                }               
            }
        }
        else
        {
            bands = new double[numComponents][rowCount][columnCount]; 

            for(int i = originY; i<maxY; i++)
            {
                for(int j = originX; j<maxX; j++)
                {
                    outData = raster.getDataElements(j, i,  outData);

                    components = colorModel.getComponents(outData, components, 0);

                    for(int k = 0; k<numComponents; k++)
                    {
                        int colorComponent = components[k];
                        bands[k][maxY - 1 - i][j] = colorComponent; 
                    }

                    //                    byte R = byteArray[3*(j+i*width)];
                    //                    byte G = byteArray[1+3*(j+i*width)];
                    //                    byte B = byteArray[2+3*(j+i*width)];
                    //                    
                    //                    bands[0][maxY - 1 - i][j] = (int) R & 0xFF;
                    //                    bands[1][maxY - 1 - i][j] = (int) G & 0xFF;
                    //                    bands[2][maxY - 1 - i][j] = (int) B & 0xFF;
                }               
            }
        }

        return bands;
    }

    @Override
    public Coordinate4D getChannelCoordinates(int channelIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Coordinate4D getCombinedChannelCoordinates() {
        // TODO Auto-generated method stub
        return null;
    }
}