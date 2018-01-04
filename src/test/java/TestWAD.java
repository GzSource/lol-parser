import no.stelar7.cdragon.util.UtilHandler;
import no.stelar7.cdragon.types.wad.WADParser;
import no.stelar7.cdragon.types.wad.data.WADFile;
import org.junit.Test;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class TestWAD
{
    @Test
    public void testWAD() throws Exception
    {
        WADParser parser = new WADParser();
        
        String pluginName  = "rcp-be-lol-game-data";
        Path   extractPath = Paths.get(System.getProperty("user.home"), "Downloads");
        
        WADFile parsed = parser.parseLatest(pluginName, extractPath);
        parsed.extractFiles(pluginName, null, extractPath);
    }
    
    @Test
    public void testWADAll()
    {
        WADParser parser = new WADParser();
        
        String pluginName  = "rcp-be-lol-game-data";
        Path   extractPath = Paths.get(System.getProperty("user.home"), "Downloads");
        
        for (int i = UtilHandler.getLongFromIP("0.0.0.25"); i > 0; i--)
        {
            try
            {
                WADFile parsed   = parser.parseVersion(pluginName, i, extractPath);
                String  realName = pluginName + "_" + UtilHandler.getIPFromLong(i);
                
                if (parsed != null)
                {
                    parsed.extractFiles(realName, null, extractPath);
                } else
                {
                    System.out.println("File not found; " + realName);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    @Test
    public void testClientWAD() throws Exception
    {
        WADParser parser = new WADParser();
        
        Path extractPath = Paths.get(System.getProperty("user.home"), "Downloads", "temp");
        Path rito        = Paths.get("C:\\Riot Games");
        
        Files.walkFileTree(rito, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            {
                if (file.getFileName().toString().endsWith(".wad") || file.getFileName().toString().endsWith(".wad.client"))
                {
                    WADFile parsed = parser.parse(file);
                    parsed.extractFiles(file.getParent().getFileName().toString(), file.getFileName().toString(), extractPath.resolve(file.getParent().getFileName()));
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}