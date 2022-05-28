package atomicJ.readers.mdt;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import atomicJ.utilities.FileInputUtilities;

public class ExtFrameHeader
{
    public static final int BYTE_SIZE = 76;

    private final int recordSize; //4 bytes, size of this record
    private final int totalHeaderSize; //4 bytes, Total header size
    //skip GUID of a frame, 16 bytes
    //skip Measurement GUID, 16 bytes
    private final int frameStatus; //4 bytes
    private final int nameSize; //4 bytes
    private final int commentSize; //4 bytes

    private final int specialStringsSize;//skip The size of frame's special string (reserved), 4 bytes

    private final int viewInfoSize; // 4 bytes
    private final int sourceInfoSize; //4 bytes
    private final int variablesSize; //4 bytes
    private final int dataOffset; //4 bytes The offset of the main frame's data (anywhere in a file!)
    private final int dataSize; //4 bytes, The size of the main frame's data

    private String name;
    private String specialStrings;
    private String viewInfo;
    private String sourceInfo;

    private Document comment;

    public ExtFrameHeader(ByteBuffer buffer)
    {
        this.recordSize = buffer.getInt();

        this.totalHeaderSize = buffer.getInt();

        FileInputUtilities.skipBytes(32, buffer);

        this.frameStatus = buffer.getInt();
        this.nameSize = buffer.getInt();
        this.commentSize = buffer.getInt();

        this.specialStringsSize = buffer.getInt();

        this.viewInfoSize = buffer.getInt();
        this.sourceInfoSize = buffer.getInt();
        this.variablesSize = buffer.getInt();
        this.dataOffset = buffer.getInt();
        this.dataSize = buffer.getInt();
    }

    public void readInTextElements(ByteBuffer buffer)
    {
        this.name = FileInputUtilities.readInStringFromBytes(nameSize, buffer);

        if(commentSize >0)
        {
            try 
            {
                this.comment = FileInputUtilities.readInXMLDocument2(commentSize, buffer);
                //                FileInputUtilities.printChildNodes(comment.getDocumentElement());

            }
            catch (ParserConfigurationException | SAXException | IOException e)
            {
                e.printStackTrace();
            }
        }

        this.specialStrings = specialStringsSize > 0 ? FileInputUtilities.readInStringFromBytes(specialStringsSize, buffer) : "";
        this.viewInfo = viewInfoSize > 0 ? FileInputUtilities.readInStringFromBytes(viewInfoSize, buffer) : "";
        this.sourceInfo = sourceInfoSize > 0 ? FileInputUtilities.readInStringFromBytes(sourceInfoSize, buffer) : "";
    }

    public String getName()
    {
        return name;
    }

    public Document getComment()
    {
        return comment;
    }

    public int getSize()
    {
        return recordSize;
    }

    public int getTotalHeaderSize()
    {
        return totalHeaderSize;
    }

    public int getFrameStatus()
    {
        return frameStatus;
    }

    public int getFrameNameSize()
    {
        return nameSize;
    }

    public int getFrameCommentSize()
    {
        return commentSize;
    }

    public int getFrameSpecialStringsSize()
    {
        return specialStringsSize;
    }

    public int getFrameViewInfoSize()
    {
        return viewInfoSize;
    }

    public int getFrameSourceInfoSize()
    {
        return sourceInfoSize;
    }

    public int getTextElementsByteSize()
    {
        int byteSize = Math.max(0, nameSize) + Math.max(0, commentSize) + Math.max(0, specialStringsSize) 
        + Math.max(0, viewInfoSize) + Math.max(0, sourceInfoSize);
        return byteSize;
    }

    public int getVariablesSize()
    {
        return variablesSize;
    }

    public int getDataOffset()
    {
        return dataOffset;
    }

    public int getDataSize()
    {
        return dataSize;
    }
}
