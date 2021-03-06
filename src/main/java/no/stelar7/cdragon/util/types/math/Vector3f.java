package no.stelar7.cdragon.util.types.math;

public class Vector3f extends org.joml.Vector3f
{
    public Vector3f(float x, float y, float z)
    {
        super(x, y, z);
    }
    
    public Vector3f()
    {
        super();
    }
    
    public Vector3f(float scale)
    {
        super(scale);
    }
    
    @Override
    public String toString()
    {
        return String.format("{\"x\":%s, \"y\":%s, \"z\":%s}", x, y, z);
    }
}
