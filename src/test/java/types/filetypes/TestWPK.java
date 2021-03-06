package types.filetypes;

import no.stelar7.cdragon.types.ogg.OGGParser;
import no.stelar7.cdragon.types.ogg.data.OGGStream;
import no.stelar7.cdragon.types.wem.data.WEMFile;
import no.stelar7.cdragon.types.wpk.WPKParser;
import no.stelar7.cdragon.types.wpk.data.WPKFile;
import no.stelar7.cdragon.util.handlers.UtilHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

public class TestWPK
{
    @Test
    public void testWPK() throws IOException
    {
        WPKParser parser = new WPKParser();
        
        String filename = "AEF3226985BC3681.wpk";
        Path   file     = UtilHandler.CDRAGON_FOLDER.resolve("cdragon\\" + filename);
        System.out.println("Parsing: " + file.toString());
        
        WPKFile   data = parser.parse(file);
        OGGParser ogg  = new OGGParser();
        
        for (WEMFile wemFile : data.getWEMFiles())
        {
            System.out.println(wemFile.getFilename() + (wemFile.getData() == null ? " - HAS JUNK CHUNK!" : ""));
            
            OGGStream odata        = ogg.parse(wemFile.getData());
            Path      outputFolder = file.resolveSibling(filename.substring(0, filename.indexOf('.')));
            Files.createDirectories(outputFolder);
            Files.write(outputFolder.resolve(wemFile.getFilename() + ".ogg"), odata.getData().toByteArray());
        }
    }
    
    @Test
    public void getFilenames() throws IOException
    {
        WPKParser    parser = new WPKParser();
        Path         from   = UtilHandler.CDRAGON_FOLDER.resolve("pbe");
        List<String> names  = new ArrayList<>();
        Files.walkFileTree(from, new SimpleFileVisitor<>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            {
                if (file.toString().endsWith(".wpk"))
                {
                    WPKFile data = parser.parse(file);
                    names.addAll(data.getWEMFiles().stream().map(WEMFile::getFilename).collect(Collectors.toList()));
                    
                }
                return FileVisitResult.CONTINUE;
            }
        });
        Files.write(from.resolveSibling("wemfilenames"), UtilHandler.getGson().toJson(names).getBytes(StandardCharsets.UTF_8));
    }
}
