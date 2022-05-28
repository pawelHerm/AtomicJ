package atomicJ.readers.mdt;

import java.io.IOException;
import static java.nio.ByteOrder.*;
import java.nio.channels.FileChannel;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.FileInputUtilities;


public class CurvesDataFrameExtension
{
    private final int nameSize;
    private final String name;

    private final int commentSize;
    private Document comment;

    private final int frameSpecialSize;
    private final String frameSpecial;

    private final int viewInfoSize;
    private final String viewInfo;

    private final int sourceInfoSize;
    private final String sourceInfo;

    private CurvesDataFrameExtension(FileChannel channel) throws UserCommunicableException
    {

        this.nameSize = FileInputUtilities.readBytesToBuffer(channel, 4, LITTLE_ENDIAN).getInt();
        this.name = FileInputUtilities.readInStringFromBytes(nameSize, FileInputUtilities.readBytesToBuffer(channel, nameSize, LITTLE_ENDIAN));

        this.commentSize = FileInputUtilities.readBytesToBuffer(channel, 4, LITTLE_ENDIAN).getInt();

        if(commentSize >0)
        {
            try {
                this.comment = FileInputUtilities.readInXMLDocument2(commentSize, FileInputUtilities.readBytesToBuffer(channel, commentSize, LITTLE_ENDIAN));

                FileInputUtilities.printChildNodes(comment.getDocumentElement());
            } catch (ParserConfigurationException | SAXException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        FileInputUtilities.printChildNodes(comment.getDocumentElement());

        this.frameSpecialSize = FileInputUtilities.readBytesToBuffer(channel, 4, LITTLE_ENDIAN).getInt();
        this.frameSpecial = FileInputUtilities.readInStringFromBytes(frameSpecialSize,
                FileInputUtilities.readBytesToBuffer(channel, frameSpecialSize, LITTLE_ENDIAN));

        this.viewInfoSize = FileInputUtilities.readBytesToBuffer(channel, 4, LITTLE_ENDIAN).getInt();
        this.viewInfo = FileInputUtilities.readInStringFromBytes(viewInfoSize,
                FileInputUtilities.readBytesToBuffer(channel, viewInfoSize, LITTLE_ENDIAN));

        this.sourceInfoSize = FileInputUtilities.readBytesToBuffer(channel, 4, LITTLE_ENDIAN).getInt();
        this.sourceInfo = FileInputUtilities.readInStringFromBytes(sourceInfoSize, 
                FileInputUtilities.readBytesToBuffer(channel, sourceInfoSize, LITTLE_ENDIAN));

        //skips GUIDSize, FramGUID and MeasurementGUID
        try {
            channel.position(channel.position() + 36);
        } catch (IOException e) {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while readin a file", e);
        }
    }

    public static CurvesDataFrameExtension readIn(FileChannel channel) throws UserCommunicableException
    {

        CurvesDataFrameExtension frameExtension = new CurvesDataFrameExtension(channel);

        return frameExtension;
    }
}
