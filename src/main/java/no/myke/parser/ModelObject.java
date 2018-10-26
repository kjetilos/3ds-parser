package no.myke.parser;

/**
 *
 */
public class ModelObject {
    private final String name;
    public Vector[] vectors;
    public short[] polygons;
    public java.util.HashMap<Short,String> materialType = new java.util.HashMap(); //Key is the face number as a short, value is the face material as a string
    public float[] textureCoordinates;

    public ModelObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
