package atomicJ.utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.tiff.IFD;
import loci.formats.tiff.IFDType;
import loci.formats.tiff.TiffIFDEntry;
import loci.formats.tiff.TiffParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import atomicJ.gui.UserCommunicableException;

public class FileInputUtilities 
{
    public static int getUnsigned(byte x)
    {
        return x & 0xff;
    }

    public static int getUnsigned(short x)
    {
        return x & 0x0000ffff;
    }

    public static long getUnsigned(int x) 
    {
        return x & 0x00000000ffffffffL;
    }

    public static void populateCharArrayWithBytes(char[] array, ByteBuffer buffer)
    {
        for(int i = 0; i<array.length; i++) 
        {
            array[i] = ((char) (buffer.get() & 0xFF));
        }
    }

    public static void populateCharArrayWithBytes2D(char[][] array, ByteBuffer buffer)
    {
        for(int i = 0; i<array.length; i++) 
        {
            char[] row = array[i];

            for(int j = 0; j<row.length; j++) 
            {
                row[j] = ((char) (buffer.get() & 0xFF));
            }
        }
    }

    public static void populateCharArray(char[] array, ByteBuffer buffer)
    {
        for(int i = 0; i<array.length; i++) 
        {
            array[i] = buffer.getChar();
        }
    }


    public static void populateIntArray(int[] array, ByteBuffer buffer)
    {
        for(int i = 0; i<array.length; i++) 
        {
            array[i] = buffer.getInt();
        }
    }

    public static void populateDoubleArray(double[] array, ByteBuffer buffer)
    {
        for(int i = 0; i<array.length; i++) 
        {
            array[i] = buffer.getDouble();
        }
    }

    public static String readInStringFromBytes(int stringLength, ByteBuffer buffer)
    {
        char[] nameChars = new char[stringLength];
        FileInputUtilities.populateCharArrayWithBytes(nameChars, buffer);

        String string =  FileInputUtilities.convertBytesToString(nameChars);
        return string;
    }

    public static String readInString(int stringLength, ByteBuffer buffer) 
    {
        char[] nameChars = new char[stringLength];
        FileInputUtilities.populateCharArray(nameChars, buffer);

        String string =  FileInputUtilities.convertBytesToString(nameChars);
        return string;
    }

    public static BigInteger readUnsignedInteger(ByteBuffer buffer, int length, boolean littleEndian)
    {
        return new BigInteger(1, readBytes(buffer, length, littleEndian));
    }

    private static byte[] readBytes(ByteBuffer buffer, int length, boolean reversed) 
    {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[reversed ? length - 1 - i : i] = buffer.get();
        }
        return bytes;
    }

    public static Document readInXMLDocument(int length, ByteBuffer dataBuffer) throws ParserConfigurationException, SAXException, IOException
    {
        byte[] bytes = new byte[length];
        dataBuffer.get(bytes);
        InputStream mainIndexFileInputStream = new ByteArrayInputStream(bytes);

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(mainIndexFileInputStream);

        return document;
    }

    public static Document readInXMLDocument2(int length, ByteBuffer dataBuffer) throws ParserConfigurationException, SAXException, IOException
    {
        byte[] bytes = new byte[length];
        dataBuffer.get(bytes);
        InputStream mainIndexFileInputStream = new ByteArrayInputStream(bytes);
        InputSource is = new InputSource(new InputStreamReader(mainIndexFileInputStream, "utf-16le"));

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(is);

        return document;
    }

    public static Document readInXMLDocument(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException
    {
        return readInXMLDocument(inputStream, false);
    }

    public static Document readInXMLDocument(InputStream inputStream, boolean namespaceAware) throws ParserConfigurationException, SAXException, IOException
    {
        InputSource xmlSource = new InputSource(inputStream);    

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        documentFactory.setNamespaceAware(namespaceAware);

        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(xmlSource);

        return document;
    }

    public static void printChildNodes(Node node)
    {
        NodeList nodes = node.getChildNodes();

        for(int i = 0; i<nodes.getLength();i++)
        {
            if(nodes.item(i) instanceof Element)
            {
                System.out.println("------------------");
                System.out.println(nodes.item(i).getNodeName());

                printAttributes(nodes.item(i));
                System.out.println("------------------");

                printChildNodes(nodes.item(i));
            }
            else if(nodes.item(i) instanceof Text)
            {
                System.out.println(((Text)nodes.item(i)).getData());
            }
        }
    }

    //trims tags
    public static List<Element> getChildElementsWithTag(Node node, String tag)
    {
        List<Element> elements = new ArrayList<>();
        NodeList nodes = node.getChildNodes();

        String trimmedTag = tag.trim();

        for(int i = 0; i<nodes.getLength();i++)
        {
            Node currentNode = nodes.item(i);
            if(currentNode instanceof Element)
            {
                Element el = (Element)currentNode;
                if(trimmedTag.equals(el.getTagName().trim()))
                {
                    elements.add(el);
                }
            }
        }
        return elements;
    }

    //trims tags
    public static Element getFirstChildElementWithTag(Node node, String tag)
    {
        Element element = null;

        String trimmedTag = tag.trim();
        NodeList nodes = node.getChildNodes();

        for(int i = 0; i<nodes.getLength();i++)
        {
            Node currentNode = nodes.item(i);
            if(currentNode instanceof Element)
            {
                Element el = (Element)currentNode;
                if(trimmedTag.equals(el.getTagName().trim()))
                {
                    element = el;
                    break;
                }
            }
        }
        return element;
    }

    public static void printAttributes(Node node)
    {
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++)
        {
            Node attribute = attributes.item(i);
            String name = attribute.getNodeName();
            String value = attribute.getNodeValue();

            System.out.println(name + " = " + value);
        }
    }

    public static void printTags(IFD ifd) throws FormatException
    {
        System.out.println("------------------------------------------------------------------");           

        for (Integer key : ifd.keySet()) 
        {
            int k = key.intValue();

            String name = IFD.getIFDTagName(k);
            String value = prettyValue(ifd.getIFDValue(k), 0);

            System.out.println("NAME: " + name + " = " + value);
        }

        System.out.println("------------------------------------------------------------------");
    }


    private static String prettyValue(Object value, int indent) 
    {
        if (!value.getClass().isArray()) return value.toString();
        char[] spaceChars = new char[indent];
        Arrays.fill(spaceChars, ' ');
        String spaces = new String(spaceChars);
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (int i=0; i<Array.getLength(value); i++) {
            sb.append(spaces);
            sb.append(" ");
            Object component = Array.get(value, i);
            sb.append(prettyValue(component, indent + 2));
            sb.append("\n");
        }
        sb.append(spaces);
        sb.append("}");
        return sb.toString();
    }

    public static String convertBytesToString(char[] chars)
    {
        return String.valueOf(chars, 0, chars.length);
    }

    public static void skipBytes(int skipCount, ByteBuffer buffer)
    {
        int initPosition = buffer.position();
        buffer.position(initPosition + skipCount);
    }

    //Converts a long value with seconds since 1/1/1904 to Date.
    public static Date convertSecondsToDate(long secondsSince) {
        return new Date((secondsSince - 2082844800L) * 1000L);}


    public static ByteBuffer readBytesToBuffer(ReadableByteChannel channel, int size)  throws UserCommunicableException
    {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        FileInputUtilities.readBytes(channel, buffer);        
        buffer.flip();

        return buffer;
    }

    public static ByteBuffer readBytesToBuffer(ReadableByteChannel channel, int size, ByteOrder order)  throws UserCommunicableException
    {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        FileInputUtilities.readBytes(channel, buffer);        
        buffer.flip();
        buffer.order(order);

        return buffer;
    }

    public static void readBytes(ReadableByteChannel channel, ByteBuffer byteBuffer) throws UserCommunicableException
    {
        while(byteBuffer.hasRemaining())
        {
            try {
                int readInCount = channel.read(byteBuffer);
                //reached end of file
                if(readInCount == -1) 
                {                     
                    throw new UserCommunicableException("Error occured while reading the file");
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new UserCommunicableException("Error occured while reading the file", e);
            } 
        }
    }

    public static void skipBytes(SeekableByteChannel channel, int byteCount) throws UserCommunicableException
    {      
        try 
        {
            long oldPosition = channel.position();
            channel.position(oldPosition + byteCount);
        } catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }
    }

    public static byte[] readInBytes(TiffParser parser, TiffIFDEntry entry) throws IOException
    {
        IFDType type = entry.getType();
        int bytePerElement = type.getBytesPerElement();

        int count = entry.getValueCount();
        long offset = entry.getValueOffset();

        RandomAccessInputStream in = parser.getStream();

        if (offset != in.getFilePointer()) 
        {
            in.seek(offset);
        }

        byte[] bytes = new byte[bytePerElement*count];
        in.readFully(bytes);

        return bytes;
    }

    //no encoding specified, 
    //one may consider using StandardCharsets.US_ASCII.decode()
    public static String readInNullTerminatedString(ByteBuffer byteBuffer)
    {
        StringBuilder sb = new StringBuilder(byteBuffer.limit());
        while (byteBuffer.remaining() > 0) 
        {
            char c = (char)byteBuffer.get();
            if (c == '\0') break;
            sb.append(c);
        }
        return sb.toString();
    }

    public static final Element getFirstElementByTagName(Element el, String name)
    {
        if(el == null)
        {
            return null;
        }

        NodeList allNodes = el.getElementsByTagName(name);
        //we can cast it safely because according to API, NodeList contains only Elements
        Element firstNode =  (Element)(allNodes.getLength() > 0 ? allNodes.item(0) : null);

        return firstNode;
    }

    public static final Element getFirstImmediateChildByTagName(Node node, String name)
    {
        NodeList children = node.getChildNodes();

        for(int i = 0; i< children.getLength();i++)
        {
            Node child = children.item(i);
            if(child.getNodeType() == Node.ELEMENT_NODE)
            {
                Element childElement = (Element)child;
                boolean hit = Objects.equals(childElement.getTagName(), name);
                if(hit)
                {
                    return childElement;
                }
            }
        }

        return null;
    }

    public static final List<Element> getImmediateChildElementsdByTagName(Node node, String name)
    {
        List<Element> foundElements = new ArrayList<>();

        NodeList children = node.getChildNodes();

        for(int i = 0; i< children.getLength();i++)
        {
            Node child = children.item(i);
            if(child.getNodeType() == Node.ELEMENT_NODE)
            {
                Element childElement = (Element)child;
                boolean hit = Objects.equals(childElement.getTagName(), name);
                if(hit)
                {
                    foundElements.add(childElement);
                }
            }
        }

        return foundElements;
    }

    public static Double parseSafelyDouble(String st)
    {
        Double value = null;

        if(st != null)
        {
            try
            {
                value = Double.parseDouble(st);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        return value;
    }

    public static double parseSafelyDouble(String st, double defaultValue)
    {
        double value = defaultValue;

        if(st != null)
        {
            try
            {
                value = Double.parseDouble(st);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        return value;
    }

    public static boolean canBeParsedToDouble(String st)
    {
        boolean canBeParsed = false;

        if(st != null)
        {
            try
            {
                Double.parseDouble(st);
                canBeParsed = true;
            }
            catch(Exception e)
            {
                e.printStackTrace();
                canBeParsed = false;
            }
        }

        return canBeParsed;
    }

    public static Integer parseSafelyInt(String st)
    {
        Integer value = null;

        if(st != null)
        {
            try
            {
                value = Integer.parseInt(st);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        return value;
    }

    public static int parseSafelyInt(String st, int defaultValue)
    {
        int value = defaultValue;

        if(st != null)
        {
            try
            {
                value = Integer.parseInt(st);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        return value;
    }

    public static boolean canBeParsedToInteger(String st)
    {
        boolean canBeParsed = false;

        if(st != null)
        {
            try
            {
                Integer.parseInt(st);
                canBeParsed = true;
            }
            catch(Exception e)
            {
                e.printStackTrace();
                canBeParsed = false;
            }
        }

        return canBeParsed;
    }

    public static Boolean parseSafelyBoolean(String st)
    {
        Boolean value = null;

        if(st != null)
        {
            try
            {
                value = Boolean.parseBoolean(st);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        return value;
    }

    public static boolean parseSafelyBoolean(String st, boolean defaultValue)
    {
        boolean value = defaultValue;

        if(st != null)
        {
            try
            {
                value = Boolean.parseBoolean(st);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        return value;
    }
}
