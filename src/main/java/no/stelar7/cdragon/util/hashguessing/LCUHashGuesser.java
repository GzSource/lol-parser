package no.stelar7.cdragon.util.hashguessing;

import com.google.gson.*;
import no.stelar7.cdragon.types.wad.data.WADFile;
import no.stelar7.cdragon.util.handlers.*;
import no.stelar7.cdragon.util.types.*;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class LCUHashGuesser extends HashGuesser
{
    
    public LCUHashGuesser(Set<String> hashes)
    {
        super(HashGuesser.hashFileLCU, hashes);
    }
    
    public Set<String> buildWordlist()
    {
        Pattern     reFilterPath = Pattern.compile("(?:^plugins/rcp-be-lol-game-data/global/default/data/characters/|/[0-9a-f]{32}\\.)");
        Set<String> paths        = new HashSet<>();
        for (String p : this.known.values())
        {
            if (!reFilterPath.matcher(p).find())
            {
                paths.add(p);
            }
        }
        
        return buildWordlist(paths);
    }
    
    public void substituteRegionLang()
    {
        Pattern     regex      = Pattern.compile("^plugins/([^/]+)/[^/]+/[^/]+/");
        Set<String> regionLang = UtilHandler.product("/", this.REGIONS, this.LANGUAGES);
        
        System.out.format("Substitute region and lang: %s regions %s languages%n", REGIONS.size(), LANGUAGES.size());
        
        for (String s : regionLang)
        {
            String replacement = "plugins/$1/" + s + "/";
            for (String value : this.known.values())
            {
                String toCheck = regex.matcher(value).replaceFirst(replacement);
                check(toCheck);
            }
        }
    }
    
    public void substituteBasenameWords(String plugin, String ext, Set<String> words, int amount)
    {
        if (words == null)
        {
            words = this.buildWordlist();
        }
        
        Set<String> paths = new HashSet<>(this.known.values());
        if (plugin != null)
        {
            for (String path : new ArrayList<>(paths))
            {
                if (!path.startsWith("plugins/" + plugin))
                {
                    paths.remove(path);
                }
            }
        }
        
        if (ext != null)
        {
            for (String path : new ArrayList<>(paths))
            {
                if (!path.endsWith(ext))
                {
                    paths.remove(path);
                }
            }
        }
        
        super.substituteBasenameWords(paths, words, amount);
    }
    
    public void addBasenameWord()
    {
        super.addBasenameWord(new HashSet<>(this.known.values()), this.buildWordlist());
    }
    
    public void doubleSubstitution(String plugin, String ext, Set<String> words)
    {
        Set<String> paths = new HashSet<>(this.known.values());
        if (plugin != null)
        {
            for (String path : new ArrayList<>(paths))
            {
                if (!path.startsWith("plugins/" + plugin))
                {
                    paths.remove(path);
                }
            }
        }
        
        if (ext != null)
        {
            for (String path : new ArrayList<>(paths))
            {
                if (!path.endsWith(ext))
                {
                    paths.remove(path);
                }
            }
        }
        
        super.doubleSubstitution(paths, words);
    }
    
    public void substituteNumbers(int max, Optional<Integer> numbers)
    {
        Pattern filter = Pattern.compile("(?:\n" +
                                         "^(?:plugins/rcp-be-lol-game-data/[^/]+/[^/]+/v1/champion-\n" +
                                         "| plugins/rcp-be-lol-game-data/global/default/(?:data|assets)/characters/\n" +
                                         "| plugins/rcp-be-lol-game-data/global/default/data/items/icons2d/\\d+_\n" +
                                         "| plugins/rcp-be-lol-game-data/[^/]+/[^/]+/v1/champions/-1.json\n" +
                                         ")\n" +
                                         "| /[0-9a-f]{32}\\.\n" +
                                         ")");
        
        Set<String> paths = new HashSet<>();
        for (String value : this.known.values())
        {
            if (!filter.matcher(value).find())
            {
                paths.add(value);
            }
        }
        
        super.substituteNumbers(paths, max, numbers);
    }
    
    public void substitutePlugin()
    {
        Set<String> all = new HashSet<>(this.known.values());
        for (String s : new ArrayList<>(all))
        {
            if (!s.startsWith("plugins/"))
            {
                all.remove(s);
            }
        }
        
        Set<String> plugins = new HashSet<>();
        for (String s : all)
        {
            plugins.add(s.split("/", 3)[1]);
        }
        
        Set<String> formats = new HashSet<>();
        for (String s : all)
        {
            formats.add(s.replaceFirst("^plugins/([^/]+)/", "plugins/%s/"));
        }
        
        System.out.format("substitute plugin: %s formats %s plugins%n", formats.size(), plugins.size());
        for (String format : formats)
        {
            for (String plugin : plugins)
            {
                String toCheck = String.format(format, plugin);
                check(toCheck);
            }
        }
    }
    
    public void grepWad(WADFile file)
    {
        System.out.println("Loading from wad!");
        
        Set<String> relative = new HashSet<>();
        
        for (Triplet<String, String, String> pair : this.getFilesAsText(file))
        {
            String      unhashed = pair.getA();
            String      ext      = pair.getB();
            String      content  = pair.getC();
            JsonElement jsonData = null;
            
            if (ext.equalsIgnoreCase("json"))
            {
                jsonData = UtilHandler.getJsonParser().parse(content);
            }
            
            if (jsonData != null)
            {
                if (unhashed.equalsIgnoreCase("plugins/rcp-fe-lol-loot/global/default/trans.json"))
                {
                    for (String key : jsonData.getAsJsonObject().keySet())
                    {
                        String toCheck = "plugins/rcp-be-lol-game-data/global/default/v1/hextech-images/" + key + ".png";
                        check(toCheck);
                    }
                } else if (jsonData.isJsonObject() && jsonData.getAsJsonObject().has("pluginDependencies") && jsonData.getAsJsonObject().has("name"))
                {
                    String      name     = jsonData.getAsJsonObject().get("name").getAsString();
                    Set<String> subPaths = new HashSet<>(Arrays.asList("index.html", "init.js", "init.js.map", "bundle.js", "trans.json", "css/main.css", "license.json"));
                    for (String sub : subPaths)
                    {
                        String toCheck = "plugins/" + name + "/global/default/" + sub;
                        check(toCheck);
                    }
                } else if (jsonData.isJsonObject() && jsonData.getAsJsonObject().has("musicVolume") && jsonData.getAsJsonObject().has("files"))
                {
                    Set<String> names = new HashSet<>();
                    for (JsonElement path : jsonData.getAsJsonObject().get("files").getAsJsonArray())
                    {
                        Pattern p = Pattern.compile("-splash-([^.]+)");
                        Matcher m = p.matcher(path.getAsString());
                        if (m.find())
                        {
                            names.add(m.group(1));
                        }
                    }
                    
                    for (String name : names)
                    {
                        String toCheck = "plugins/rcp-fe-lol-splash/global/default/splash-assets/" + name + "/config.json";
                        check(toCheck);
                        
                        for (JsonElement path : jsonData.getAsJsonObject().get("files").getAsJsonArray())
                        {
                            toCheck = "plugins/rcp-fe-lol-splash/global/default/splash-assets/" + name + "/" + path;
                            check(toCheck);
                        }
                    }
                    
                } else if (unhashed.equalsIgnoreCase("plugins/rcp-be-lol-game-data/global/default/v1/champion-summary.json"))
                {
                    Set<Integer> ids = new HashSet<>();
                    for (JsonElement ch : jsonData.getAsJsonArray())
                    {
                        ids.add(ch.getAsJsonObject().get("id").getAsInt());
                    }
                    
                    for (Integer id : ids)
                    {
                        String toCheck = "plugins/rcp-be-lol-game-data/global/default/v1/champions/" + id + ".json";
                        check(toCheck);
                        
                        toCheck = "plugins/rcp-be-lol-game-data/global/default/v1/champion-splashes/" + id + "/metadata.json";
                        check(toCheck);
                    }
                } else if (jsonData.isJsonObject() && jsonData.getAsJsonObject().has("recommendedItemDefaults"))
                {
                    for (JsonElement rec : jsonData.getAsJsonObject().getAsJsonArray("recommendedItemDefaults"))
                    {
                        String toCheck = "plugins/rcp-be-lol-game-data/global/default" + rec;
                        check(toCheck);
                    }
                }
            }
            
            
            Pattern p = Pattern.compile("/fe/([^/]+)/([a-zA-Z0-9/_.@-]+)");
            Matcher m = p.matcher(content);
            while (m.find())
            {
                String toCheck = "plugins/rcp-fe-" + m.group(1) + "/global/default/" + m.group(2);
                check(toCheck);
            }
            
            p = Pattern.compile("/DATA/([a-zA-Z0-9/_.@-]+)");
            m = p.matcher(content);
            while (m.find())
            {
                String toCheck = "plugins/rcp-be-lol-game-data/global/default/data/" + m.group(1);
                check(toCheck);
            }
            
            p = Pattern.compile("[/\"]lol-game-data/assets/([a-zA-Z0-9/_.@-]+)");
            m = p.matcher(content);
            while (m.find())
            {
                String toCheck = "plugins/rcp-be-lol-game-data/global/default/" + m.group(1);
                check(toCheck);
            }
            
            
            p = Pattern.compile("[^a-zA-Z0-9/_.\\\\-]((?:\\.|\\.\\.)/[a-zA-Z0-9/_.-]+)");
            m = p.matcher(content);
            while (m.find())
            {
                relative.add(m.group(1));
            }
            
            p = Pattern.compile("[\"']([a-zA-Z0-9][a-zA-Z0-9/_.@-]*\\.(?:js|json|webm|html|[a-z]{3}))[\"']");
            m = p.matcher(content);
            while (m.find())
            {
                relative.add(m.group(1));
            }
            
            p = Pattern.compile("<template id=\"[^\"]*-template-([^\"]+)\"");
            m = p.matcher(content);
            while (m.find())
            {
                relative.add(m.group(1) + "/template.html");
            }
            
            p = Pattern.compile("sourceMappingURL=(.*?\\.js)\\.map");
            m = p.matcher(content);
            while (m.find())
            {
                relative.add(m.group(1));
            }
        }
        
        checkBasenames(relative);
    }
    
    public void guessFromGameHashes()
    {
        String base = "plugins/rcp-be-lol-game-data/global/default";
        for (String path : HashGuesser.hashFileGAME.load(true).values())
        {
            String prefix = UtilHandler.getFilename(path);
            prefix = prefix.substring(0, prefix.lastIndexOf("."));
            String ext = UtilHandler.getEnding(path);
            
            if (ext.equals("dds"))
            {
                check(base + "/" + prefix + ".jpg");
                check(base + "/" + prefix + ".png");
            } else if (ext.equals("json"))
            {
                check(base + "/" + path);
            }
        }
    }
    
    public void guessPatterns()
    {
        Set<Integer> primary = IntStream.iterate(8000, i -> i < 8700, i -> i + 100).boxed().collect(Collectors.toSet());
        primary.add(0);
        
        primary.forEach(i -> {
            Set<Integer> secondary = IntStream.rangeClosed(i, i + 100).boxed().collect(Collectors.toSet());
            secondary.add(0);
            
            for (Integer j : primary)
            {
                for (Integer k : secondary)
                {
                    String toCheck = "plugins/rcp-fe-lol-perks/global/default/images/inventory-card/" + i + "/p" + i + "_s" + j + "_k" + k + ".jpg";
                    check(toCheck);
                }
            }
            
            Set<String> paths = new HashSet<>(Arrays.asList("environment.jpg", "construct.png"));
            for (Integer j : secondary)
            {
                paths.add("keystones/" + j + ".png");
            }
            
            for (Integer j : primary)
            {
                paths.add("f'second/" + j + ".png");
            }
            
            for (String path : paths)
            {
                String toCheck = "plugins/rcp-fe-lol-perks/global/default/images/construct/" + i + "/" + path;
                check(toCheck);
            }
        });
        
        Set<String> paths = new HashSet<>();
        IntStream.range(0, 5).forEach(i -> {
            Set<String> actions = new HashSet<>(Arrays.asList("filter", "unfilter"));
            for (String action : actions)
            {
                paths.add(i + "." + action + ".csv");
                
                for (String lang : this.LANGUAGES)
                {
                    paths.add(i + "." + action + ".language." + lang.split("_")[0] + ".csv");
                    paths.add(i + "." + action + ".country." + lang.split("_")[1] + ".csv");
                    paths.add(i + "." + action + ".locale." + lang + ".csv");
                }
                
                for (String region : this.REGIONS)
                {
                    paths.add(i + "." + action + ".region." + region + "c.csv");
                }
            }
        });
        
        Set<String> divs = new HashSet<>(Arrays.asList("allowedchars", "breakingchars", "projectedchars", "projectedchars1337", "punctuationchars", "variantaliases"));
        for (String p : divs)
        {
            for (String lang : this.LANGUAGES)
            {
                paths.add(p + ".locale." + lang + ".txt");
                paths.add(p + ".language." + lang.split("_")[0] + ".txt");
            }
        }
        
        for (String path : paths)
        {
            String toCheck = "plugins/rcp-be-sanitizer/global/default/" + path;
            check(toCheck);
        }
        
        
        for (String p : this.known.values())
        {
            if (p.startsWith("plugins/rcp-fe-lol-loot/global/default/assets/loot_item_icons/"))
            {
                String toCheck = p.replace(".png", "_splash.png");
                check(toCheck);
            }
        }
    }
}