package no.stelar7.cdragon.util.readers;


import no.stelar7.cdragon.util.types.*;
import org.joml.Quaternionf;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.*;
import java.nio.file.Path;
import java.util.*;

public class RandomAccessReader implements AutoCloseable
{
    private ByteBuffer buffer;
    
    private Path      path;
    private byte[]    rawBytes;
    private ByteOrder byteOrder;
    
    public static RandomAccessReader create(RandomAccessReader raf)
    {
        if (raf.getPath() != null)
        {
            return new RandomAccessReader(raf.getPath(), raf.byteOrder);
        } else
        {
            return new RandomAccessReader(raf.getRawBytes(), raf.byteOrder);
        }
    }
    
    public static RandomAccessReader create(File file, ByteOrder order)
    {
        return new RandomAccessReader(file.toPath(), order);
    }
    
    public RandomAccessReader(Path path, ByteOrder order)
    {
        try
        {
            this.path = path;
            RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");
            
            this.buffer = raf.getChannel().map(MapMode.READ_ONLY, 0, raf.getChannel().size());
            this.buffer.order(order);
            raf.close();
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Invalid file?");
        }
    }
    
    public RandomAccessReader(byte[] dataBytes, ByteOrder order)
    {
        this.rawBytes = dataBytes;
        this.buffer = ByteBuffer.wrap(dataBytes);
        this.buffer.order(order);
    }
    
    public byte[] getRawBytes()
    {
        return rawBytes;
    }
    
    @Override
    public void close()
    {
        /*
         This is not ideal, but its a workaround to http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4469299
         and Java9 made it even worse...
         */
        
        buffer = null;
    }
    
    public int pos()
    {
        return buffer.position();
    }
    
    public int remaining()
    {
        return buffer.remaining();
    }
    
    
    public void seek(int pos)
    {
        buffer.position(pos);
    }
    
    public String readString(int length)
    {
        return new String(readBytes(length), StandardCharsets.UTF_8);
    }
    
    public String readString(int length, Charset charset)
    {
        return new String(readBytes(length), charset).trim();
    }
    
    
    private int  bitsLeft;
    private int  totalBitsRead;
    private byte bits;
    
    /**
     * Only counts bits read with the readBits(int) method!
     */
    public int getTotalBitsRead()
    {
        return totalBitsRead;
    }
    
    private byte readBit()
    {
        if (bitsLeft == 0)
        {
            bits = readByte();
            bitsLeft = 8;
        }
        
        bitsLeft--;
        totalBitsRead++;
        return (((bits & 0xFF) & (0x80 >> bitsLeft)) != 0) ? (byte) 1 : (byte) 0;
    }
    
    public int readBits(int count)
    {
        int result = 0;
        for (int i = 0; i < count; i++)
        {
            if (readBit() == 1)
            {
                result |= (1 << i);
            }
        }
        return result;
    }
    
    
    /**
     * Reads untill 0x00 is read.
     */
    public String readString()
    {
        byte[] temp  = new byte[65536];
        byte   b;
        int    index = 0;
        while ((b = readByte()) != 0)
        {
            temp[index++] = b;
        }
        
        return new String(temp, StandardCharsets.UTF_8);
    }
    
    /**
     * Reads untill EOF
     */
    public String readAsString()
    {
        return new String(readRemaining(), StandardCharsets.UTF_8);
    }
    
    
    public byte[] readRemaining()
    {
        byte[] temp  = new byte[buffer.remaining()];
        int    index = 0;
        while (buffer.hasRemaining())
        {
            temp[index++] = buffer.get();
        }
        return temp;
    }
    
    public long readLong()
    {
        return buffer.getLong();
    }
    
    public int readInt()
    {
        return buffer.getInt();
    }
    
    public short readShort()
    {
        return buffer.getShort();
    }
    
    public List<Short> readShorts(int count)
    {
        List<Short> values = new ArrayList<>();
        for (int j = 0; j < count; j++)
        {
            values.add(readShort());
        }
        return values;
    }
    
    public byte readByte()
    {
        return buffer.get();
    }
    
    public byte[] readBytes(int length)
    {
        byte[] tempData = new byte[length];
        buffer.get(tempData, 0, length);
        return Arrays.copyOf(tempData, length);
    }
    
    public Path getPath()
    {
        return path;
    }
    
    public float readFloat()
    {
        return buffer.getFloat();
    }
    
    
    public boolean readBoolean()
    {
        return buffer.get() > 0;
    }
    
    /**
     * Reads untill 0x00 is read.
     */
    public String readFromOffset(int offset)
    {
        int pos = buffer.position();
        buffer.position(0);
        byte[] tempData = new byte[buffer.remaining()];
        buffer.get(tempData, 0, buffer.remaining());
        buffer.position(pos);
        
        byte[] temp  = new byte[65536];
        byte   b;
        int    index = offset;
        while ((b = tempData[index++]) != 0)
        {
            temp[index - offset] = b;
        }
        
        return new String(temp, StandardCharsets.UTF_8).trim();
    }
    
    public void printBuffer()
    {
        int pos = buffer.position();
        while (buffer.hasRemaining())
        {
            System.out.print(buffer.get() + ", ");
        }
        System.out.println();
        buffer.position(pos);
    }
    
    public Vector3f readVec3F()
    {
        Vector3f vector = new Vector3f();
        vector.x = buffer.getFloat();
        vector.y = buffer.getFloat();
        vector.z = buffer.getFloat();
        return vector;
    }
    
    public Vector3i readVec3I()
    {
        Vector3i vector = new Vector3i();
        vector.x = (buffer.getInt());
        vector.y = (buffer.getInt());
        vector.z = (buffer.getInt());
        return vector;
    }
    
    public Vector3s readVec3S()
    {
        Vector3s vector = new Vector3s();
        vector.setX((buffer.getShort()));
        vector.setY((buffer.getShort()));
        vector.setZ((buffer.getShort()));
        return vector;
    }
    
    public Vector3b readVec3B()
    {
        Vector3b vector = new Vector3b();
        vector.setX((buffer.get()));
        vector.setY((buffer.get()));
        vector.setZ((buffer.get()));
        return vector;
    }
    
    public Quaternionf readQuaternion()
    {
        Quaternionf vector = new Quaternionf();
        vector.x = (buffer.getFloat());
        vector.y = (buffer.getFloat());
        vector.z = (buffer.getFloat());
        vector.w = (buffer.getFloat());
        return vector;
    }
    
    public Vector2i readVec2I()
    {
        Vector2i vector = new Vector2i();
        vector.x = (buffer.getInt());
        vector.y = (buffer.getInt());
        return vector;
    }
    
    public Vector2f readVec2F()
    {
        Vector2f vector = new Vector2f();
        vector.x = (buffer.getFloat());
        vector.y = (buffer.getFloat());
        return vector;
    }
    
    public Vector4b readVec4B()
    {
        Vector4b vector = new Vector4b();
        vector.setX((buffer.get()));
        vector.setY((buffer.get()));
        vector.setZ((buffer.get()));
        vector.setW((buffer.get()));
        return vector;
    }
    
    public Vector4f readVec4F()
    {
        Vector4f vector = new Vector4f();
        vector.x = (buffer.getFloat());
        vector.y = (buffer.getFloat());
        vector.z = (buffer.getFloat());
        vector.w = (buffer.getFloat());
        return vector;
    }
    
    public Matrix4f readMatrix4x4()
    {
        Matrix4f vector = new Matrix4f();
        
        vector.m00 = (buffer.getFloat());
        vector.m01 = (buffer.getFloat());
        vector.m02 = (buffer.getFloat());
        vector.m03 = (buffer.getFloat());
        
        vector.m10 = (buffer.getFloat());
        vector.m11 = (buffer.getFloat());
        vector.m12 = (buffer.getFloat());
        vector.m13 = (buffer.getFloat());
        
        vector.m20 = (buffer.getFloat());
        vector.m21 = (buffer.getFloat());
        vector.m22 = (buffer.getFloat());
        vector.m23 = (buffer.getFloat());
        
        vector.m30 = (buffer.getFloat());
        vector.m31 = (buffer.getFloat());
        vector.m32 = (buffer.getFloat());
        vector.m33 = (buffer.getFloat());
        
        return vector;
    }
    
    public boolean isEOF()
    {
        return !buffer.hasRemaining();
    }
    
}
