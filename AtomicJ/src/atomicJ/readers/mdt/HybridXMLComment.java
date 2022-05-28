package atomicJ.readers.mdt;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;

public class HybridXMLComment
{
    private static final String FRAME_TYPE_PATH = "//FrameComment/Parameters/FrameType";
    private static final String HYBRID_FRAME_TYPE = "HybridForceVolumeExternalData";

    private static final String Z_AMPLITUDE_VALUE_PATH = "//FrameComment/Parameters/Hybrid/DevicePars/ZAmp";
    private static final String Z_AMPLITUDE_UNIT_PATH = "//FrameComment/Parameters/Hybrid/DevicePars/ZAmpUnits";

    private static final String EXTERNAL_DATA_FILE_PATH = "//FrameComment/Parameters/Data/ExternalDataFileName";

    private static final String AXES_DIRECTIONS_PATH = "//FrameComment/Parameters/Measurement/Spectra/Scanning/AxesDirections";

    private static final String SCAN_DIRECTION_PATH = "//FrameComment/Parameters/Measurement/Scanning/Location";

    private boolean isHybrid;
    private double ampValue = Double.NaN;
    private PrefixedUnit ampUnit = SimplePrefixedUnit.getNullInstance();
    private String externalDataPath = "";
    private HybridScanDirection hybridScanDirection;

    public HybridXMLComment(Document comment) throws UserCommunicableException
    {
        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            XPathExpression xPathExpr = xpath.compile(FRAME_TYPE_PATH);
            String nodeText = (String)xPathExpr.evaluate(comment, XPathConstants.STRING);
            this.isHybrid = HYBRID_FRAME_TYPE.equals(nodeText.trim());   

            XPathExpression ampValueExpr = xpath.compile(Z_AMPLITUDE_VALUE_PATH);
            this.ampValue = ((Number)ampValueExpr.evaluate(comment, XPathConstants.NUMBER)).doubleValue();

            XPathExpression ampUnitExpr = xpath.compile(Z_AMPLITUDE_UNIT_PATH);
            this.ampUnit = UnitUtilities.getSIUnit(ampUnitExpr.evaluate(comment, XPathConstants.STRING).toString());

            XPathExpression dataPathExpr = xpath.compile(EXTERNAL_DATA_FILE_PATH);
            this.externalDataPath = (String)dataPathExpr.evaluate(comment, XPathConstants.STRING);

            XPathExpression scanDiExpr = xpath.compile(SCAN_DIRECTION_PATH);
            int scanDirectionCode = ((Number)scanDiExpr.evaluate(comment, XPathConstants.NUMBER)).intValue();

            this.hybridScanDirection = HybridScanDirection.getHybriScanDirection(scanDirectionCode);

        } catch (XPathExpressionException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }
    }

    public Path getFullExternalDataPath(Path parent)
    {
        return Paths.get(parent.toString(), externalDataPath);
    }

    public HybridScanDirection getScanDirection()
    {
        return hybridScanDirection;
    }

    public double getZAmplitudeValue()
    {
        return ampValue;
    }

    public PrefixedUnit getZAmplitudeUnit()
    {
        return ampUnit;
    }

    public static boolean isHybrid(Document comment)
    {
        boolean isHybrid = false;

        if(comment != null)
        {
            XPath xpath = XPathFactory.newInstance().newXPath();

            try 
            {
                XPathExpression xPathExpr = xpath.compile(FRAME_TYPE_PATH);
                String nodeText = (String)xPathExpr.evaluate(comment, XPathConstants.STRING);
                isHybrid = HYBRID_FRAME_TYPE.equals(nodeText.trim());
            }
            catch (XPathExpressionException e) 
            {
                e.printStackTrace();
            }
        }

        return isHybrid;
    }
}