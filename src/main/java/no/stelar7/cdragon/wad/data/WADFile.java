package no.stelar7.cdragon.wad.data;

import lombok.*;
import no.stelar7.cdragon.util.*;
import no.stelar7.cdragon.wad.data.content.*;
import no.stelar7.cdragon.wad.data.header.WADHeaderBase;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

@Data
public class WADFile
{
    @Setter(value = AccessLevel.NONE)
    @Getter(value = AccessLevel.NONE)
    private final RAFReader fileReader;
    
    private WADHeaderBase header;
    private List<WADContentHeaderV1> contentHeaders = new ArrayList<>();
    
    public WADFile(RAFReader raf)
    {
        this.fileReader = raf;
    }
    
    public void extractFiles(Path outputPath)
    {
        System.out.println("Extracting files");
        
        // set this to 1 to reduce memory usage
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final int       interval = (int) Math.ceil(getContentHeaders().size() / 20f);
        
        for (int index = 0; index < getContentHeaders().size(); index++)
        {
            final int selfIndex = index;
            executor.submit(() ->
                            {
                                WADContentHeaderV1 fileHeader = getContentHeaders().get(selfIndex);
                
                                if (getHeader().getMajor() > 1 && ((WADContentHeaderV2) fileHeader).isDuplicate())
                                {
                                    return;
                                }
                
                                saveFile(fileHeader, outputPath);
                
                                if (selfIndex % interval == 0)
                                {
                                    System.out.println(selfIndex + "/" + getContentHeaders().size());
                                }
                
                            });
        }
        
        try
        {
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            fileReader.close();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    private void saveFile(WADContentHeaderV1 header, Path savePath)
    {
        try
        {
            String hash     = String.format("%016X", header.getPathHash()).toLowerCase(Locale.ENGLISH);
            String filename = UtilHandler.getKnownFileHashes().getOrDefault(hash, "\\unknown\\" + hash);
            Path   self     = Paths.get(savePath.toString(), filename);
            
            self.getParent().toFile().mkdirs();
            String parentName = self.getParent().getFileName().toString();
            
            
            byte[] data = readContentFromHeaderData(header);
            Files.write(self, data);
            
            if ("unknown".equals(parentName))
            {
                findFileTypeAndRename(self, data, filename, savePath);
            }
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private synchronized byte[] readContentFromHeaderData(WADContentHeaderV1 header) throws IOException
    {
        fileReader.seek(header.getOffset());
        if (header.isCompressed())
        {
            byte[] fileBytes = fileReader.readBytes(header.getCompressedFileSize());
            if (UtilHandler.isProbableGZIP(fileBytes))
            {
                return CompressionHandler.uncompressGZIP(fileBytes);
            }
            
            if (UtilHandler.isProbableZSTD(fileBytes))
            {
                return CompressionHandler.uncompressZSTD(fileBytes, header.getFileSize());
            }
            
            Files.write(Paths.get("unknown.file"), fileBytes);
            System.out.println("");
            return fileBytes;
        } else
        {
            return fileReader.readBytes(header.getFileSize());
        }
    }
    
    private String findFileType(Path self, byte[] data)
    {
        ByteArrayWrapper magic  = new ByteArrayWrapper(Arrays.copyOf(data, 4));
        String           result = UtilHandler.getMagicNumbers().get(magic);
        
        if (result != null)
        {
            return result;
        }
        
        if (UtilHandler.isProbableJSON(magic.getData()))
        {
            return "json";
        }
        
        System.out.print("Unknown filetype: ");
        System.out.print(self.toString());
        System.out.println(magic.toString());
        return "txt";
    }
    
    private void findFileTypeAndRename(Path self, byte[] data, String filename, Path parent)
    {
        try
        {
            
            // TODO:
            // somehow find the real filename
            //  String hashName     = filename.substring(filename.lastIndexOf('\\') + 1);
            //  String realFileName = getHash("plugins/rcp-fe-l10n/rcp-fe-l10n/global/licenses.json");
            // TODOEND
            
            String        fileType = findFileType(self, data);
            StringBuilder sb       = new StringBuilder(filename).append(".").append(fileType);
            Path          other    = Paths.get(parent.toString(), sb.toString());
            
            Files.move(self, other, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    
    /*
    private String getHash(String text) throws IOException
    {
        XXHashFactory        factory = XXHashFactory.fastestInstance();
        byte[]               data    = text.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in      = new ByteArrayInputStream(data);
        
        StreamingXXHash64 hash64 = factory.newStreamingHash64(0);
        byte[]            buf    = new byte[8];
        for (; ; )
        {
            int read = in.read(buf);
            if (read == -1)
            {
                break;
            }
            hash64.update(buf, 0, read);
        }
        return String.format("%016X", hash64.getValue()).toLowerCase(Locale.ENGLISH);
    }
    */
}
