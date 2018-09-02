package no.stelar7.cdragon.types.bin.data;


import java.util.*;

public class BINStruct
{
    private String         hash;
    private int            size;
    private short          count;
    private List<BINValue> data = new ArrayList<>();
    
    public String getHash()
    {
        return hash;
    }
    
    public void setHash(String hash)
    {
        this.hash = hash;
    }
    
    public int getSize()
    {
        return size;
    }
    
    public void setSize(int size)
    {
        this.size = size;
    }
    
    public short getCount()
    {
        return count;
    }
    
    public void setCount(short count)
    {
        this.count = count;
    }
    
    public List<BINValue> getData()
    {
        return data;
    }
    
    public void setData(List<BINValue> data)
    {
        this.data = data;
    }
}
