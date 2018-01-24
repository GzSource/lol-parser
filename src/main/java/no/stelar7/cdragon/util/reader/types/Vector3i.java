package no.stelar7.cdragon.util.reader.types;

public class Vector3i extends org.joml.Vector3i
{
    @Override
    public String toString()
    {
        return String.format("{\"x\":%d, \"y\":%d, \"z\":%d}", x, y, z);
    }
}
