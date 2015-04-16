package no.myke.parser;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class ModelLoaderTest {

    private File file = new File("src/test/resources/frigate.3ds");
    
    @Test
    public void testLoad() throws Exception {
        Model model = ModelLoader.load3dModel(file);
        assertNotNull(model);
    }

}
