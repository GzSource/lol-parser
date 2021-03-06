package no.stelar7.cdragon.util.hashguessing;

import com.google.gson.*;
import no.stelar7.cdragon.types.bin.BINParser;
import no.stelar7.cdragon.types.filemanifest.*;
import no.stelar7.cdragon.util.handlers.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.Stream;

public class GameHashGuesser extends HashGuesser
{
    
    public GameHashGuesser(Set<String> hashes)
    {
        super(HashGuesser.hashFileGAME, hashes);
        System.out.println("Started guessing GAME hashes");
    }
    
    public void guessBinByLinkedFiles(Path pbe)
    {
        System.out.println("Guessing bin files by linked file names");
        try
        {
            System.out.println("Parsing bin files...");
            BINParser parser = new BINParser();
            Files.walk(pbe)
                 .filter(UtilHandler.IS_BIN_PREDICATE)
                 .map(parser::parse)
                 .filter(Objects::nonNull)
                 .flatMap(b -> b.getLinkedFiles().stream())
                 .forEach(this::check);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    
    public void guessAssetsBySearch(Path pbe)
    {
        System.out.println("Guessing assets by searching strings");
        List<Path> readMe = UtilHandler.getFilesMatchingPredicate(pbe, UtilHandler.IS_JSON_PREDICATE);
        
        // need a better regex for this :thinking:
        Pattern p = Pattern.compile("(/.*?/).*?\\..*\"");
        
        readMe.stream()
              .map(UtilHandler::readAsString)
              .forEach(e -> {
                  Matcher m = p.matcher(e);
                  while (m.find())
                  {
                      int lastStart = 0;
                      for (int i = 0; i <= m.groupCount(); i++)
                      {
                          int start = m.start(i);
                          int end   = m.end(i) - 1;
                    
                          if (start == lastStart)
                          {
                              continue;
                          }
                    
                          lastStart = start;
                          while (e.charAt(start - 1) != '"')
                          {
                              start--;
                          }
                    
                          while (e.charAt(end) != '"')
                          {
                              end++;
                          }
                    
                          String toCheck = e.substring(start, end).toLowerCase();
                          this.check(toCheck);
                      }
                  }
              });
    }
    
    private final Predicate<String> isGameHash = s -> !s.startsWith("plugins/");
    
    public void pullCDTB()
    {
        System.out.println("Feching hashlists from CDTB");
        String hashA = "https://github.com/CommunityDragon/CDTB/raw/master/cdragontoolbox/hashes.game.txt";
        String hashB = "https://github.com/Morilli/CDTB/raw/new-hashes/cdragontoolbox/hashes.game.txt";
        
        Stream.of(WebHandler.readWeb(hashA).stream(), WebHandler.readWeb(hashB).stream())
              .flatMap(a -> a)
              .map(line -> line.substring(line.indexOf(' ') + 1))
              .filter(isGameHash)
              .forEach(this::check);
    }
    
    @Override
    public String generateHash(String val)
    {
        Long hashNum = HashHandler.computeXXHash64AsLong(val);
        return HashHandler.toHex(hashNum, 16);
    }
    
    public void guessShaderFiles(Path dataPath)
    {
        System.out.println("Guessing shaders by manifest");
        
        List<String> prefixes = Arrays.asList("data/shaders/hlsl/", "assets/shaders/generated/shaders/");
        List<String> suffixes = new ArrayList<>(Arrays.asList("", ".dx9", ".glsl"));
        for (String s : Arrays.asList(".dx9_", ".glsl_"))
        {
            for (int i = 0; i < 100000; i += 100)
            {
                suffixes.add(s + i);
            }
        }
        
        Collections.sort(suffixes);
        
        try
        {
            Path       manifest = Files.find(dataPath, 100, (path, attr) -> path.toString().contains("76EBE65321C56DD9.json")).findFirst().get();
            JsonObject shaders  = UtilHandler.getJsonParser().parse(Files.readString(manifest)).getAsJsonObject().getAsJsonObject("shaders");
            JsonArray  sections = shaders.getAsJsonArray("sections");
            
            for (JsonElement sectionEle : sections)
            {
                JsonObject section = (JsonObject) sectionEle;
                JsonArray  files   = section.getAsJsonArray("files");
                
                for (JsonElement fileEle : files)
                {
                    for (String suffix : suffixes)
                    {
                        for (String prefix : prefixes)
                        {
                            String toCheck = prefix + fileEle.getAsString() + suffix;
                            check(toCheck);
                        }
                    }
                }
            }
            
            Function<JsonElement, String>      getFirstChildKey     = obj -> obj.getAsJsonObject().keySet().toArray(String[]::new)[0];
            Function<JsonElement, JsonElement> getFirstChildElement = obj -> obj.getAsJsonObject().get(getFirstChildKey.apply(obj));
            Function<JsonElement, JsonObject>  getFirstChildObject  = obj -> getFirstChildElement.apply(obj).getAsJsonObject();
            
            prefixes = Arrays.asList("data/shaders/hlsl/", "assets/shaders/generated/");
            suffixes = new ArrayList<>(Arrays.asList(".ps_2_0", ".vs_2_0", ".ps_2_0.dx9", ".vs_2_0.dx9", ".ps_2_0.glsl", ".vs_2_0.glsl"));
            for (String s : Arrays.asList(".ps_2_0.dx9_", ".vs_2_0.dx9_", ".ps_2_0.glsl_", ".vs_2_0.glsl_"))
            {
                for (int i = 0; i < 100000; i += 100)
                {
                    suffixes.add(s + i);
                }
            }
            
            Path      shaderJson = Paths.get("D:\\pbe\\data\\shaders\\shaders.json");
            JsonArray shaderObj  = UtilHandler.getJsonParser().parse(Files.readString(shaderJson)).getAsJsonObject().getAsJsonArray("CustomShaderDef");
            for (JsonElement elem : shaderObj)
            {
                JsonObject obj     = (JsonObject) elem;
                JsonObject realObj = getFirstChildObject.apply(obj);
                String     name    = realObj.get("objectPath").getAsString();
                
                for (String suffix : suffixes)
                {
                    for (String prefix : prefixes)
                    {
                        String toCheck = prefix + name + suffix;
                        check(toCheck);
                    }
                }
                
            }
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void guessStringTableFiles()
    {
        for (String language : LANGUAGES)
        {
            String hash = String.format("DATA/Menu/bootstrap_%s.stringtable", language);
            check(hash);
        }
    }
    
    public void guessHardcoded()
    {
        check("UX/RenderUI/Overrides/Default/PerkSummonerSpecialist.bin");
        check("UX/RenderUI/Overrides/Default/SB_LtoR_NoNames.bin");
        check("UX/RenderUI/Overrides/Default/SB_MirroredCenter_Names.bin");
        check("UX/RenderUI/Overrides/Default/SB_MirroredCenter_NoNames.bin");
    }
    
    public void guessScripts(Path dataPath)
    {
        System.out.println("Guessing scripts by manifest");
        
        Path                  luaManifest = dataPath.resolve("data/all_lua_files.manifest");
        ManifestContentParser parser      = new ManifestContentParser();
        ManifestContentFileV1 v1          = parser.parseV1(luaManifest);
        v1.getItems().forEach(this::check);
    }
}
