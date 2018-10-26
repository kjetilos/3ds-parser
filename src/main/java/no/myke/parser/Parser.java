package no.myke.parser;

import java.io.IOException;

/**
 * This parser understands the layout of the 3ds file and is
 * able to construct a Model from a reader.
 *
 * @author Kjetil Østerås
 */
public class Parser {

    private Model model;
    private TypeReader reader;
    private Logger logger;
    private ModelObject currentObject;

    public Parser(TypeReader reader) {
        this.reader = reader;
    }

    public Model parseFile() throws ParserException {
        try {
            int limit = readChunk();
            while (reader.position() < limit) {
                readChunk();
            }
        } catch (IOException e) {
            throw new ParserException(e);
        }
        return model;
    }

    private int readChunk() throws IOException {
        short type = reader.getShort();
        int size = reader.getInt(); // this is probably unsigned but for convenience we use signed int
        log("Chunk 0x%04x (%d)", type, size);
        parseChunk(type, size);
        return size;
    }

    private void parseChunk(short type, int size) throws IOException {
        switch (type) {
        case 0x0002: //M3D Version
            parseVersionChunk();
            break;
        case 0x3d3d: //Start of object mesh data.
            break;
        case 0x4000: //first item of Subchunk 4000 is an ASCIIZ string of the objects name. Remember an object can be a mesh, a light or a camera. 
            parseObjectChunk();
            break;
        case 0x4100: //Header for Triangular Polygon Object (Just an identifier, no actual information)
            parseTriangularMesh();
            break;
        case 0x4110: //Count of verticus, followed by X,Y,Z co-ords
            parseVerticesList();
            break;
        case 0x4120: //Faces Description / Point List
            parseFacesDescription();
            break;
        case 0x4130: //Face Material
            parseFaceMaterial();
            break;
        case 0x4140: //Mapping Coordinates List
            parseMappingCoordinates();
            break;
        case 0x4160: //Local Coordinates System / Translation Matrix
            parseLocalCoordinateSystem();
            break;
        case 0x4d4d: //A 3ds file has the Primary chunk ID 0x4D4D. This is always the first chunk of the file. With in the primary chunk are the main chunks.
            parseMainChunk();
            break;
        case (short)0xafff: // Material block
            break;
        case (short)0xa000: // Material name
            parseMaterialName();
            break;
        case (short)0xa200: // Texture map 1
            break;
        case (short)0xa300: // Mapping filename
            parseMappingFilename();
            break;
        default:
            skipChunk(type, size);
        }
    }

    private void skipChunk(int type, int size) throws IOException {
        log("skipping chunk");
        move(size - 6); // size includes headers. header is 6 bytes
    }

    private void move(int i) throws IOException {
        reader.skip(i);
    }

    private void parseMainChunk() {
        model = new Model();
    }

    private void parseVersionChunk() throws IOException {
        int version = reader.getInt();
        log("3ds version %d", version);
    }

    private void parseObjectChunk() throws IOException {
        String name = reader.readString();
        log("Found Object %s", name);
        currentObject = model.newModelObject(name);
    }

    private void parseTriangularMesh() throws IOException {
        log("Found Mesh Header");
    }

    private void parseVerticesList() throws IOException {
        //byte range: 0-1
        //Size: 2 	
        //Type: short int 	
        //Description: Total vertices in object
        short numVertices = reader.getShort();
        //vertices always come in multiples of 3 (X,Y,Z) co-ordinates
        float[] vertices = new float[numVertices * 3];
        
        //byte range: 2-5, 6-9, 10-13
        //Size: 4 (each)
        //Type: float (each)
        //Description: X, Y and Z values as a float
        //These 12 bytes are repeated for however many vertices are in the object, with the next 3 (X,Y,Z) being in the byte range 14-25.
        for (int i=0; i<vertices.length; i++) {
            vertices[i] = reader.getFloat();
        }

        //Create vectors from groups of 3 vertices (X, Y, Z).
        //Use the values from 0x4120 (Faces Description / Point List) to find faces.
        Vector[] vectors = new Vector[numVertices];
        for (int i = 0; i < numVertices; i++)
        {
            //Create vector with 3 points
            vectors[i] = new Vector(vertices[i*3], vertices[i*3 + 1], vertices[i*3 + 2]);
        }
        currentObject.vectors = vectors;
        log("Found %d vertices", numVertices);
    }

    private void parseFacesDescription() throws IOException {
	    //byte range: 0-1
	    //Size: 2 	
	    //Type: short int 	
	    //Description: Total polygons in object - numFaces
        short numFaces = reader.getShort();
        short[] faces = new short[numFaces * 3];

        //byte range: 2-3, 4-5, 6-7
        //Size: 2 (each)
        //Type: short int (each)
        //Description: point1, point2 and point3 values as a short.
        //These points refer to the corresponding vertex of the triangular polygon from the vertex list.
        //Points are organized in a clock-wise order.
        //Repeats 'numFaces' times for each polygon.
        for (int i=0; i<numFaces; i++) {
            faces[i*3] = reader.getShort();
            faces[i*3 + 1] = reader.getShort();
            faces[i*3 + 2] = reader.getShort();
            reader.getShort(); // Discard face flag
        }
        log("Found %d faces", numFaces);
        currentObject.polygons = faces;
    }

    private void parseFaceMaterial() throws IOException {
        String name = reader.readString();
        short n_faces = reader.getShort();
        log("%d faces with material %s", n_faces, name);
        for(short i = 0 ; i < n_faces ; i++)
        {
            currentObject.materialType.put(reader.getShort(), name);
        }
    }

    private void parseLocalCoordinateSystem() throws IOException {
        float[] x1 = new float[3];
        float[] x2 = new float[3];
        float[] x3 = new float[3];
        float[] origin = new float[3];
        readVector(x1);
        readVector(x2);
        readVector(x3);
        readVector(origin);
    }

    private void parseMappingCoordinates() throws IOException {
        short numVertices = reader.getShort();
        float[] uv = new float[numVertices * 2];
        for (int i=0; i<numVertices; i++) {
            uv[i*2] = reader.getFloat();
            uv[i*2+1] = reader.getFloat();
        }
        currentObject.textureCoordinates = uv;
        log("Found %d mapping coordinates", numVertices);
    }

    private void parseMaterialName() throws IOException {
        String materialName = reader.readString();
        log("Material name %s", materialName);
    }

    private void parseMappingFilename() throws IOException {
        String mappingFile = reader.readString();
        log("Mapping file %s", mappingFile);
    }

    private void readVector(float[] v) throws IOException {
        v[0] = reader.getFloat();
        v[1] = reader.getFloat();
        v[2] = reader.getFloat();
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    private void log(String format, Object... args) {
        if (logger != null) {
            logger.log(String.format(format, args));
        }
    }

}
