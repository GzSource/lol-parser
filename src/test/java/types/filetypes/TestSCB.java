package types.filetypes;

import no.stelar7.cdragon.types.scb.SCBParser;
import no.stelar7.cdragon.types.scb.data.SCBFile;
import no.stelar7.cdragon.util.handlers.UtilHandler;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class TestSCB
{
    @Test
    public void testSCB() {
        SCBParser parser = new SCBParser();
    
        Path file = UtilHandler.CDRAGON_FOLDER.resolve("parser_test\\00a356da16b6715f.scb");
        System.out.println("Parsing: " + file.toString());
    
        SCBFile data = parser.parse(file);
        System.out.println();
    }
    
}
