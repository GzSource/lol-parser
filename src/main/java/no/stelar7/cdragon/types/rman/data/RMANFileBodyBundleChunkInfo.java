package no.stelar7.cdragon.types.rman.data;

import java.util.Objects;

public class RMANFileBodyBundleChunkInfo
{
    private String bundleId;
    private String chunkId;
    private int    offsetToChunk;
    private int    compressedSize;
    
    public RMANFileBodyBundleChunkInfo(String bundleId, String chunkId, int offsetToChunk, int compressedSize)
    {
        this.bundleId = bundleId;
        this.chunkId = chunkId;
        this.offsetToChunk = offsetToChunk;
        this.compressedSize = compressedSize;
    }
    
    public String getBundleId()
    {
        return bundleId;
    }
    
    public void setBundleId(String bundleId)
    {
        this.bundleId = bundleId;
    }
    
    public String getChunkId()
    {
        return chunkId;
    }
    
    public void setChunkId(String chunkId)
    {
        this.chunkId = chunkId;
    }
    
    public int getOffsetToChunk()
    {
        return offsetToChunk;
    }
    
    public void setOffsetToChunk(int offsetToChunk)
    {
        this.offsetToChunk = offsetToChunk;
    }
    
    public int getCompressedSize()
    {
        return compressedSize;
    }
    
    public void setCompressedSize(int compressedSize)
    {
        this.compressedSize = compressedSize;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        RMANFileBodyBundleChunkInfo that = (RMANFileBodyBundleChunkInfo) o;
        return offsetToChunk == that.offsetToChunk &&
               compressedSize == that.compressedSize &&
               Objects.equals(bundleId, that.bundleId) &&
               Objects.equals(chunkId, that.chunkId);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(bundleId, chunkId, offsetToChunk, compressedSize);
    }
    
    @Override
    public String toString()
    {
        return "RMANFileBodyBundleChunkInfo{" +
               "bundleId='" + bundleId + '\'' +
               ", chunkId='" + chunkId + '\'' +
               ", offsetToChunk=" + offsetToChunk +
               ", compressedSize=" + compressedSize +
               '}';
    }
}
