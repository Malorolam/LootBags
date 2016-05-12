package mal.lootbags.config;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;

import com.google.common.base.CharMatcher;

import mal.lootbags.Bag;
import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import mal.lootbags.handler.BagHandler;
import mal.lootbags.loot.LootItem;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Configuration.UnicodeInputStreamReader;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.FMLInjectionData;

/*
 * Handles the bag config file, loading and parsing it 
 */
public class BagConfigHandler {

	private enum ConfigText
	{
		TAB("    "), ERROR("Lootbags !!CONFIG ERROR!!    "), INFO("Lootbags Config Information:    ");
		
		private String text;
		
		private ConfigText(String text)
		{
			this.text = text;
		}
		
		public String getText()
		{
			return text;
		}
	}
	
	private File file;
	private String fileName = null;
    private String defaultEncoding = Configuration.DEFAULT_ENCODING;
    public static final String ALLOWED_CHARS = "._-";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String CATEGORY_SPLITTER = ".";
    public static final String NEW_LINE = System.getProperty("line.separator");
    public static final String COMMENT_SEPARATOR = "##########################################################################################################";
    private static final String CONFIG_VERSION_MARKER = "~CONFIG_VERSION";
    private static final Pattern CONFIG_START = Pattern.compile("START: \"([^\\\"]+)\"");
    private static final Pattern CONFIG_END = Pattern.compile("END: \"([^\\\"]+)\"");
    public static final CharMatcher allowedProperties = CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.anyOf(ALLOWED_CHARS));
    
    public static ICommandSender command = null;
    
    private ArrayList<String> fileList;
    private FMLPreInitializationEvent FMLPreEvent;
    
    public BagConfigHandler(FMLPreInitializationEvent event)
    {
    	FMLPreEvent = event;
    }
	
	public void initBagConfig()
	{
		file = new File(FMLPreEvent.getModConfigurationDirectory(), "Lootbags_BagConfig.cfg");
		String basePath = ((File)(FMLInjectionData.data()[6])).getAbsolutePath().replace(File.separatorChar, '/').replace("/.", "");
        String path = file.getAbsolutePath().replace(File.separatorChar, '/').replace("/./", "/").replace(basePath, "");
        
        fileName = path;
        reloadBagConfig(null);
	}
	
	public void reloadBagConfig(ICommandSender icommand)
	{
		fileList = new ArrayList<String>();
		command = icommand;
		try
        {
            load();
        }
        catch (Throwable e)
        {
            File fileBak = new File(file.getAbsolutePath() + "_" + 
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".errored");
            FMLLog.severe("An exception occurred while loading config file %s. This file will be renamed to %s " +
            		"and a new config file will be generated.", file.getName(), fileBak.getName());
            e.printStackTrace();
            
            file.renameTo(fileBak);
            load();
        }
        parseConfigText();
        save();
        command = null;
	}
	
	private void load()
	{
		BufferedReader buffer = null;
        UnicodeInputStreamReader input = null;
        try
        {
            if (file.getParentFile() != null)
            {
                file.getParentFile().mkdirs();
            }

            if (!file.exists())
            {
                // Either a previous load attempt failed or the file is new; clear maps
                if (!file.createNewFile())
                    return;
                fileList = populateDefaultFile();
            }
            else if (file.canRead())
            {
                input = new UnicodeInputStreamReader(new FileInputStream(file), defaultEncoding);
                defaultEncoding = input.getEncoding();
                buffer = new BufferedReader(input);

                String line;
                ConfigCategory currentCat = null;
                Property.Type type = null;
                int lineNum = 0;
                String name = null;
                
                lineNum++;
            	line = buffer.readLine();
                while(line != null)
                {
                	fileList.add(line);
                	
                	lineNum++;
                	line = buffer.readLine();
                	
                }
            }
        }
        catch (IOException e)
        {
        	e.printStackTrace();
        }
        finally
        {
        	if (buffer != null)
        	{
        		try
        		{
        			buffer.close();
        		} catch (IOException e){}
        	}
        	if (input != null)
        	{
        		try
        		{
        			input.close();
        		} catch (IOException e){}
        	}
        }
	}
	
	public void save()
	{
		try
        {
            if (file.getParentFile() != null)
            {
                file.getParentFile().mkdirs();
            }

            if (!file.exists() && !file.createNewFile())
            {
                return;
            }

            if (file.canWrite())
            {
                FileOutputStream fos = new FileOutputStream(file);
                BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos, defaultEncoding));

                for(String s: fileList)
                {
                	buffer.write(s+NEW_LINE);
                }

                buffer.close();
                fos.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
	}
	
	public void parseConfigText()
	{
		String activeBagName=null;
		Bag currentBag = null;
		
		for(int linenum = 0; linenum < fileList.size(); linenum++)
		{
			
			String line = fileList.get(linenum);
			String trim = line.trim();
			
			//regex to separate the words
			String[] words = trim.split("(?<!$):");
			
			if(words[0].startsWith("$"))//command
			{
				switch(words[0].toUpperCase())
				{
				case "$STARTBAG"://start bag command, it expects two other words
					currentBag = startNewBag(words, linenum+1, currentBag);
					break;
				case "$BAGCOLOR"://bag color command, expects two other words that are commands or ints
					addBagColor(words, linenum+1, currentBag);
					break;
				case "$ISSECRET"://secret command, it expects one other word
					addSecretState(words, linenum+1, currentBag);
					break;
				case "$BAGTEXTCOLOR"://bag name command, it expects one other word
					addBagNameColor(words, linenum+1, currentBag);
					break;
				case "$BAGTEXTUNOPENED"://unopened bag text, can show up multiple times, expects either one or two "words"
					addBagTextUnopened(words, linenum+1, currentBag);
					break;
				case "$BAGTEXTOPENED"://opened bag text, can show up multiple times, expects one or two words
					addBagTextOpened(words, linenum+1, currentBag);
					break;
				case "$BAGTEXTSHIFT"://shift text, can show up multiple times, expects one or two words
					addBagTextShift(words, linenum+1, currentBag);
					break;
				case "$WEIGHT"://the weight of the bag for use in recycling and crafting, expects two words
					addBagWeight(words, linenum+1, currentBag);
					break;
				case "$CRAFTEDFROM"://the bag name and number of bags needed to craft this bag, expects two words
					addBagCrafting(words, linenum+1, currentBag);
					break;
				case "$PASSIVESPAWNWEIGHT"://the spawn weight for passive mobs, expects two words
					addPassiveSpawnWeight(words, linenum+1, currentBag);
					break;
				case "$PLAYERSPAWNWEIGHT"://spawn weight for players, expects two words
					addPlayerSpawnWeight(words, linenum+1, currentBag);
					break;
				case "$MOBSPAWNWEIGHT"://spawn weight for monsters, expects two words
					addMonsterSpawnWeight(words, linenum+1, currentBag);
					break;
				case "$BOSSSPAWNWEIGHT"://spawn weight for bosses, expects two words
					addBossSpawnWeight(words, linenum+1, currentBag);
					break;
				case "$USEGENERALLOOTSOURCES"://if the general table is added to the bag or not, expects two words
					addGeneralLootSource(words, linenum+1, currentBag);
					break;
				case "$MAXIMUMITEMS"://maximum number of items that can drop, expects two words
					addMaximumItemCount(words, linenum+1, currentBag);
					break;
				case "$MINIMUMITEMS"://minimum number of items that can drop, expects two words
					addMinimumItemCount(words, linenum+1, currentBag);
					break;
				case "$MAXIMUMGENERALLOOTWEIGHT"://maximum weight of the general loot that shows up, expects two words
					addMaximumGeneralWeight(words, linenum+1, currentBag);
					break;
				case "$MINIMUMGENERALLOOTWEIGHT"://minimum weight of the general loot that shows up, expects two words
					addMinimumGeneralWeight(words, linenum+1, currentBag);
					break;
				case "$PREVENTITEMREPEATS"://prevents a particular table entry from being added to the bag twice, expects two words
					addPreventItemRepeats(words, linenum+1, currentBag);
					break;
				case "$EXCLUDEENTITIES"://toggle if the mob blacklist blocks or allows mobs
					addExcludeEntities(words, linenum+1, currentBag);
					break;
				case "$STARTENTITYLIST"://start the entity list checking, it'll be closed in the method, expects only one word
					addEntityList(words,linenum+1,currentBag);
					break;
				case "$STARTWHITELIST"://start the whitelist checking, closed in the method
					linenum = addWhiteList(words,linenum,currentBag);
					break;
				case "$STARTBLACKLIST"://start the blacklist checking, closed in the method
					linenum = addBlackList(words,linenum,currentBag);
					break;
				case "$ENDBAG"://end the bag properly
					endNewBag(words,linenum,currentBag);
					currentBag=null;
					break;
				}
					
			}
			else
			{
				int l = linenum+1;
				LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Text at line: " + l + " is not a command or in a list.  Please only have commands, list components, or whitespace in the config.", command);
				LootbagsUtil.LogError(ConfigText.INFO.getText()+"Text for reference: " + trim, command);
			}
		}
		LootbagsUtil.LogInfo("Bag Config Completed.");
	}

	private Bag startNewBag(String[] words, int linenum, Bag currentBag)
	{
		if(words.length<3)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag start command at line " + linenum + " has error: Too few words, it needs the command, the bag name, and the bag id to properly initialize a bag.", command);
			return null;
		}
		if(words.length>3)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag start command at line " + linenum + " has error: Too many words, it needs the command, the bag name, and the bag id only.", command);
			return null;
		}
		if(currentBag != null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag start command at line " + linenum + " has error: There is a bag already open with name " + currentBag.getBagName() + ".", command);
		}
		
		int bagID=-1;
		try {
			bagID = Integer.parseInt(words[2]);
		}
		catch (Exception e)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag start command at line " + linenum + " has error: Third word is not a number.", command);
			return null;
		}
		if(!BagHandler.isIDFree(bagID))
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag start command at line " + linenum + " has error: Specified Bag ID not free.", command);
			return null;
		}
		LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Started defining properties for bag named: " + words[1] + ".");
		return new Bag(words[1], bagID);
	}
	
	private void endNewBag(String[] words, int linenum, Bag currentBag)
	{
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag end command at line " + linenum + " has error: Too few words, it needs the command and the bag name.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag end command at line " + linenum + " has error: Too many words, it only needs the command and the bag name.", command);
			return;
		}
		if(words[1].equals(currentBag.getBagName()))
		{
			LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Successfully closed bag with name: "+words[1]+".");
			BagHandler.addBag(currentBag);
			return;
		}
		else
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag end command at line " + linenum + " is not closing the currently open bag.  Bag will be not saved.", command);
			return;
		}
	}
	
	private void addBagColor(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<3)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag color command at line " + linenum + " has error: Too few words, it needs the command and two colors in RGB format.", command);
			return;
		}
		if(words.length>3)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag image command at line " + linenum + " has error: Too many words, it needs only the command and two colors in RGB format.", command);
			return;
		}
		currentBag.setBagColor(parseBagColor(words[1]), parseBagColor(words[2]));
		
		LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Added color to bag: " + currentBag.getBagName() + ".");
	}
	
	private void addSecretState(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag secret command at line " + linenum + " has error: Too few words, it needs the command and a boolean state (true/false) to to set the secret state.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag secret command at line " + linenum + " has error: Too many words, it needs only the command and the boolean state.", command);
			return;
		}
		if(words[1].equalsIgnoreCase("true") || words[1].equalsIgnoreCase("t"))
			currentBag.setSecret(true);
		else if(words[1].equalsIgnoreCase("false") || words[1].equalsIgnoreCase("f"))
			currentBag.setSecret(false);
		else
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag secret command at line " + linenum + " has error: text is not true, t, false, or f.  Please use one of those four options.", command);
			return;
		}
		LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Set bag secret state for bag: " + currentBag.getBagName() + ".");
	}
	
	private void addBagNameColor(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag name color command at line " + linenum + " has error: Too few words, it needs the command and either a color string or a color command.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag name color at line " + linenum + " has error: Too many words, it needs only the command and a color string or command.", command);
			return;
		}
		
		String code = parseColorText(words[1]);
		currentBag.setBagNameColor(code);
	}
	
	private void addBagTextUnopened(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag unopened text command at line " + linenum + " has error: Too few words, it needs the command and at minimum the text to add.", command);
			return;
		}
		if(words.length>3)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag unopened text at line " + linenum + " has error: Too many words, it needs only the command, the color (either the / code or the $ command), and the text to add.", command);
			return;
		}
		
		if(words.length==2)//only the text is included so use default color
		{
			String text = words[1];
			currentBag.addUnopenedText(text);
		}
		else if(words.length==3)//just the color command/code included, letting the color parser handle someone doing $color
		{
			String code = parseColorText(words[1]);
			String text = words[2];
			currentBag.addUnopenedText(code+text);
		}
	}
	
	private void addBagTextOpened(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag opened text command at line " + linenum + " has error: Too few words, it needs the command and at minimum the text to add.", command);
			return;
		}
		if(words.length>3)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag opened text at line " + linenum + " has error: Too many words, it needs only the command, the color (either the / code or the $ command), and the text to add.", command);
			return;
		}
		
		if(words.length==2)//only the text is included so use default color
		{
			String text = words[1];
			currentBag.addOpenedText(text);
		}
		else if(words.length==3)//just the color command/code included, letting the color parser handle someone doing $color
		{
			String code = parseColorText(words[1]);
			String text = words[2];
			currentBag.addOpenedText(code+text);
		}
	}
	
	private void addBagTextShift(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag shift text command at line " + linenum + " has error: Too few words, it needs the command and at minimum the text to add.", command);
			return;
		}
		if(words.length>3)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag shift text at line " + linenum + " has error: Too many words, it needs only the command, the color (either the / code or the $ command), and the text to add.", command);
			return;
		}
		
		if(words.length==2)//only the text is included so use default color
		{
			String text = words[1];
			currentBag.addShiftText(text);
		}
		else if(words.length==3)//just the color command/code included, letting the color parser handle someone doing $color
		{
			String code = parseColorText(words[1]);
			String text = words[2];
			currentBag.addShiftText(code+text);
		}
	}
	
	private void addBagWeight(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag weight command at line " + linenum + " has error: Too few words, it needs the command and the weight.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag weight at line " + linenum + " has error: Too many words, it needs only the command and the weight.", command);
			return;
		}
		
		try
		{
			int weight = Integer.parseInt(words[1]);
			currentBag.setWeight(weight);
		}
		catch(Exception e)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag weight command at line " + linenum + " has error: Second word is not a number.", command);
			return;
		}
	}
	
	private void addBagCrafting(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag crafting command at line " + linenum + " has error: Too few words, it needs the command and the source bag name.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag crafting at line " + linenum + " has error: Too many words, it needs only the command and the source bag name.", command);
			return;
		}
		
		String name = words[1];
		
		if(!name.equalsIgnoreCase("$NULL"))
			currentBag.setCraftingSource(name);
	}
	
	private void addPassiveSpawnWeight(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag spawn weight command at line " + linenum + " has error: Too few words, it needs the command and the weight.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag spawn weight at line " + linenum + " has error: Too many words, it needs only the command and the weight.", command);
			return;
		}

		int weight = 0;
		try
		{
			weight = Integer.parseInt(words[1]);
		}
		catch(Exception e)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag spawn weight at line " + linenum + "  has error: second word is not a number.  Using default value.", command);
			weight = LootBags.getDefaultDropWeight();
		}
		
		currentBag.setSpawnChancePassive(weight);
	}
	
	private void addPlayerSpawnWeight(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag spawn weight command at line " + linenum + " has error: Too few words, it needs the command and the weight.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag spawn weight at line " + linenum + " has error: Too many words, it needs only the command and the weight.", command);
			return;
		}

		int weight = 0;
		try
		{
			weight = Integer.parseInt(words[1]);
		}
		catch(Exception e)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag spawn weight at line " + linenum + "  has error: second word is not a number.  Using default value.", command);
			weight = LootBags.getDefaultDropWeight();
		}
		
		currentBag.setSpawnChancePlayer(weight);
	}
	
	private void addMonsterSpawnWeight(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag spawn weight command at line " + linenum + " has error: Too few words, it needs the command and the weight.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag spawn weight at line " + linenum + " has error: Too many words, it needs only the command and the weight.", command);
			return;
		}

		int weight = 0;
		try
		{
			weight = Integer.parseInt(words[1]);
		}
		catch(Exception e)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag spawn weight at line " + linenum + "  has error: second word is not a number.  Using default value.", command);
			weight = LootBags.getDefaultDropWeight();
		}
		
		currentBag.setSpawnChanceMonster(weight);
	}
	
	private void addBossSpawnWeight(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag spawn weight command at line " + linenum + " has error: Too few words, it needs the command and the weight.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag spawn weight at line " + linenum + " has error: Too many words, it needs only the command and the weight.", command);
			return;
		}

		int weight = 0;
		try
		{
			weight = Integer.parseInt(words[1]);
		}
		catch(Exception e)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag spawn weight at line " + linenum + "  has error: second word is not a number.  Using default value.", command);
			weight = LootBags.getDefaultDropWeight();
		}
		
		currentBag.setSpawnChanceBoss(weight);
	}
	
	private void addGeneralLootSource(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag general source command at line " + linenum + " has error: Too few words, it needs the command and a boolean value.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag general source command at line " + linenum + " has error: Too many words, it needs only the command and a boolean value.", command);
			return;
		}
		
		if(words[1].equalsIgnoreCase("true") || words[1].equalsIgnoreCase("t") || words[1].equalsIgnoreCase("1"))
			currentBag.setGeneralSources(true);
		else if(words[1].equalsIgnoreCase("false") || words[1].equalsIgnoreCase("f") || words[1].equalsIgnoreCase("0"))
			currentBag.setGeneralSources(false);
		else
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag general source command at line " + linenum + " has error: boolean value not recognized as boolean.", command);
	}
	
	private void addMaximumItemCount(String[] words, int linenum, Bag currentBag) {
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag maximum item count command at line " + linenum + " has error: Too few words, it needs the command and the number of items.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag maximum item count at line " + linenum + " has error: Too many words, it needs only the command and the number of items.", command);
			return;
		}

		int weight = 0;
		try
		{
			weight = Integer.parseInt(words[1]);
		}
		catch(Exception e)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag maximum item count at line " + linenum + "  has error: second word is not a number.  Using default value.", command);
			weight = 5;
		}
		
		if(weight > 5)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag maximum item count at line " + linenum + " has error: count is greater than 5, setting to 5.", command);
			weight = 5;
		}
		if(weight < 1)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag maximum item count at line " + linenum + " has error: count is less than 1, setting to 1.", command);
			weight = 1;
		}
		currentBag.setMaximumItemsDropped(weight);
	}
	
	private void addMinimumItemCount(String[] words, int linenum, Bag currentBag) {
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag minimum item count command at line " + linenum + " has error: Too few words, it needs the command and the number of items.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag minimum item count at line " + linenum + " has error: Too many words, it needs only the command and the number of items.", command);
			return;
		}

		int weight = 0;
		try
		{
			weight = Integer.parseInt(words[1]);
		}
		catch(Exception e)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag minimum item count at line " + linenum + "  has error: second word is not a number.  Using default value.", command);
			weight = 1;
		}
		
		if(weight > 5)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag minimum item count at line " + linenum + " has error: count is greater than 5, setting to 5.", command);
			weight = 5;
		}
		if(weight < 1)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag minimum item count at line " + linenum + " has error: count is less than 1, setting to 1.", command);
			weight = 1;
		}
		currentBag.setMinimumItemsDropped(weight);
	}
	
	private void addMaximumGeneralWeight(String[] words, int linenum, Bag currentBag) {
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag maximum general weight command at line " + linenum + " has error: Too few words, it needs the command and weight.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag maximum general weight command at line " + linenum + " has error: Too many words, it needs only the command and weight.", command);
			return;
		}

		int weight = 0;
		try
		{
			weight = Integer.parseInt(words[1]);
		}
		catch(Exception e)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag maximum general weight at line " + linenum + "  has error: second word is not a number.  Using default value.", command);
			weight = -1;
		}
		
		currentBag.setMaximumGeneralWeight(weight);
	}
	
	private void addMinimumGeneralWeight(String[] words, int linenum, Bag currentBag) {
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag minimum general weight command at line " + linenum + " has error: Too few words, it needs the command and weight.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag minimum general weight command at line " + linenum + " has error: Too many words, it needs only the command and weight.", command);
			return;
		}

		int weight = 0;
		try
		{
			weight = Integer.parseInt(words[1]);
		}
		catch(Exception e)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag minimum general weight at line " + linenum + "  has error: second word is not a number.  Using default value.", command);
			weight = -1;
		}
		
		currentBag.setMinimumGeneralWeight(weight);
	}
	
	private void addPreventItemRepeats(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag item repeat command at line " + linenum + " has error: Too few words, it needs the command and a boolean value.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag item repeat command at line " + linenum + " has error: Too many words, it needs only the command and a boolean value.", command);
			return;
		}
		
		if(words[1].equalsIgnoreCase("none"))
			currentBag.setItemRepeats(0);
		else if(words[1].equalsIgnoreCase("damage"))
			currentBag.setItemRepeats(1);
		else if(words[1].equalsIgnoreCase("item"))
			currentBag.setItemRepeats(2);
		else
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag item repeat command at line " + linenum + " has error: text not recognized as 'none', 'damage', or 'item'.", command);
	}
	
	private void addExcludeEntities(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag entitity exclusion command at line " + linenum + " has error: Too few words, it needs the command and a boolean value.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag entity exlusion command at line " + linenum + " has error: Too many words, it needs only the command and a boolean value.", command);
			return;
		}
		
		if(words[1].equalsIgnoreCase("true") || words[1].equalsIgnoreCase("t") || words[1].equalsIgnoreCase("1"))
			currentBag.setEntityExclusion(true);
		else if(words[1].equalsIgnoreCase("false") || words[1].equalsIgnoreCase("f") || words[1].equalsIgnoreCase("0"))
			currentBag.setEntityExclusion(false);
		else
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag entity exclusion command at line " + linenum + " has error: boolean value not recognized as boolean.", command);
	}
	
	private void addEntityList(String[] words, int linenum, Bag currentBag)
	{
		int templine = linenum;
		boolean exitflag = false;
		
		while(!exitflag)
		{
			String line = fileList.get(templine);
			String trim = line.trim();
			
			//regex to separate the words
			String[] tempwords = trim.split("(?<!$):");
			
			if(tempwords[0].equalsIgnoreCase("$ENDENTITYLIST"))
				exitflag = true;
			else if(tempwords[0].equalsIgnoreCase("$VISIBLENAME"))
				currentBag.addEntityToList(tempwords[1], true);
			else if(tempwords[0].equalsIgnoreCase("$INTERNALNAME"))
				currentBag.addEntityToList(tempwords[1], false);
			else if(tempwords[0].startsWith("$"))
			{
				LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Unknown command found at line " + templine + ": exiting entity list subroutine.", command);
				exitflag = true;
			}
			else
				LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Unknown text found at line " + templine + ": skipping line.", command);
			templine++;
		}
		
		linenum = templine;//skip the lines read here
	}
	
	private int addWhiteList(String[] words, int linenum, Bag currentBag)
	{
		int templine = linenum;
		boolean exitflag = false;
		
		while(!exitflag)
		{
			templine++;
			String line = fileList.get(templine);
			String trim = line.trim();
			
			//regex to separate the words
			String[] tempwords = trim.split("(?<!$):");
			
			if(tempwords[0].equalsIgnoreCase("$ENDWHITELIST"))
				exitflag = true;
			else if(tempwords.length==6)//correct length for a standard whitelist item
			{
				try {
				String modid = tempwords[0];
				String itemname = tempwords[1];
				ArrayList<Integer> itemdamage = LootbagsUtil.constructDamageRange(tempwords[2]);
				int minstack = Integer.parseInt(tempwords[3]);
				int maxstack = Integer.parseInt(tempwords[4]);
				int weight = Integer.parseInt(tempwords[5]);
				
				currentBag.addWhitelistItem(modid, itemname, itemdamage, minstack, maxstack, weight);
				} catch(Exception e) {
					int l = templine+1;
					LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Parsing error at line " + l + ": " + line + ": skipping line", command);
					e.printStackTrace();
				}
			}
			else if(tempwords.length==7)//length including NBT data
			{
				try {
				String modid = tempwords[0];
				String itemname = tempwords[1];
				ArrayList<Integer> itemdamage = LootbagsUtil.constructDamageRange(tempwords[2]);
				int minstack = Integer.parseInt(tempwords[3]);
				int maxstack = Integer.parseInt(tempwords[4]);
				int weight = Integer.parseInt(tempwords[5]);
				byte[] nbt = LootbagsUtil.parseNBTArray(tempwords[6]);
				
				currentBag.addWhitelistItem(modid, itemname, itemdamage, minstack, maxstack, weight, nbt);
				} catch(Exception e) {
					int l = templine+1;
					LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Parsing error at line " + l + ": " + line + ": skipping line", command);
					e.printStackTrace();
				}
			}
			else if(tempwords[0].startsWith("$"))
			{
				int l = templine+1;
				LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Unknown command found at line " + l + ": " + line + ": exiting whitelist subroutine.", command);
				exitflag = true;
			}
			else
			{
				int l = templine+1;
				LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Unknown text found at line " + l + ": " + line + ": skipping line.", command);
			}
		}
		
		return templine;//skip the lines read here
	}
	
	private int addBlackList(String[] words, int linenum, Bag currentBag)
	{
		int templine = linenum;
		boolean exitflag = false;
		
		while(!exitflag)
		{
			templine++;
			String line = fileList.get(templine);
			String trim = line.trim();
			
			//regex to separate the words
			String[] tempwords = trim.split("(?<!$):");
			
			if(tempwords[0].equalsIgnoreCase("$ENDBLACKLIST"))
				exitflag = true;
			else if(tempwords.length==1)//correct length for a mod blacklist
			{
				try {
				String modid = tempwords[0];
				
				currentBag.addBlacklistItem(modid);
				} catch(Exception e) {
					int l = templine+1;
					LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Parsing error at line " + l + ": " + line + ": skipping line", command);
					e.printStackTrace();
				}
			}
			else if(tempwords.length==3)//correct length for a standard blacklist item
			{
				try {
				String modid = tempwords[0];
				String itemname = tempwords[1];
				ArrayList<Integer> itemdamage = LootbagsUtil.constructDamageRange(tempwords[2]);
				
				currentBag.addBlacklistItem(modid, itemname, itemdamage);
				} catch(Exception e) {
					int l = templine+1;
					LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Parsing error at line " + l + ": " + line + ": skipping line", command);
					e.printStackTrace();
				}
			}
			else if(tempwords[0].startsWith("$"))
			{
				int l = templine+1;
				LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Unknown command found at line " + l + ": " + line + ": exiting blacklist subroutine.", command);
				exitflag = true;
			}
			else
			{
				int l = templine+1;
				LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Unknown text found at line " + l + ": " + line + ": skipping line.", command);
			}
		}
		
		return templine;//skip the lines read here
	}
	
	private int parseBagColor(String text)
	{
		String[] rgb = text.split("\\|");
		try {
			if(rgb.length != 3)
				throw new Exception();
			int r = Integer.parseInt(rgb[0]);
			int g = Integer.parseInt(rgb[1]);
			int b = Integer.parseInt(rgb[2]);
			Color c = new Color(r,g,b);
			
			return c.getRGB();
		} catch (Exception e) {
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag Color Parse Error: Color code not three integers separated by vertical bars.", command);
		}
		return 16777215;
	}
	
	private String parseColorText(String text)
	{
		if(text.startsWith("$"))//color command
		{
			String trim = text.substring(1).toUpperCase();
			String code;
			switch(trim)
			{
				case "WHITE":
					code = EnumChatFormatting.WHITE.toString();
					break;
				case "BLACK":
					code = EnumChatFormatting.BLACK.toString();
					break;
				case "DARK_BLUE":
					code = EnumChatFormatting.DARK_BLUE.toString();
					break;
				case "DARK_GREEN":
					code = EnumChatFormatting.DARK_GREEN.toString();
					break;
				case "DARK_AQUA":
					code = EnumChatFormatting.DARK_AQUA.toString();
					break;
				case "DARK_RED":
					code = EnumChatFormatting.DARK_RED.toString();
					break;
				case "DARK_PURPLE":
					code = EnumChatFormatting.DARK_PURPLE.toString();
					break;
				case "GOLD":
					code = EnumChatFormatting.GOLD.toString();
					break;
				case "GRAY":
					code = EnumChatFormatting.GRAY.toString();
					break;
				case "DARK_GRAY":
					code = EnumChatFormatting.DARK_GRAY.toString();
					break;
				case "BLUE":
					code = EnumChatFormatting.BLUE.toString();
					break;
				case "GREEN":
					code = EnumChatFormatting.GREEN.toString();
					break;
				case "AQUA":
					code = EnumChatFormatting.AQUA.toString();
					break;
				case "RED":
					code = EnumChatFormatting.RED.toString();
					break;
				case "LIGHT_PURPLE":
					code = EnumChatFormatting.LIGHT_PURPLE.toString();
					break;
				case "YELLOW":
					code = EnumChatFormatting.YELLOW.toString();
					break;
				case "OBFUSCATED":
					code = EnumChatFormatting.OBFUSCATED.toString();
					break;
				case "BOLD":
					code = EnumChatFormatting.BOLD.toString();
					break;
				case "STRIKETHROUGH":
					code = EnumChatFormatting.STRIKETHROUGH.toString();
					break;
				case "UNDERLINE":
					code = EnumChatFormatting.UNDERLINE.toString();
					break;
				case "ITALIC":
					code = EnumChatFormatting.ITALIC.toString();
					break;
				default:
					code = null;
			}
			if(code!=null)
				LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Parsed text to EnumChatFormatting option: " + code +".");
			else
			{
				LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Could not parse text command as EnumChatFormatting.");
				code="";
			}
			return code;
		}
		else
		{
			LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Parsed text to back slash color code " + text.substring(1) + ".");
			return text;
		}
	}
	
	/**
	 * Populates the default file with the default settings and bags
	 */
	private ArrayList<String> populateDefaultFile()
	{
		ArrayList<String> list = new ArrayList<String>();
		
		/**
		 * Standard Bags
		 */
		//common bag
		list.add("$STARTBAG:lootbagCommon:0");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:false");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$WHITE");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:What's inside is not as interesting as not knowing.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DROPCHANCES");
		list.add(ConfigText.TAB.getText()+"$WEIGHT:1000");
		list.add(ConfigText.TAB.getText()+"$CRAFTEDFROM:$NULL");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:50");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:100");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:100");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:200");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:true");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:5");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMGENERALLOOTWEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$MINIMUMGENERALLOOTWEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:none");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:false");
		list.add(ConfigText.TAB.getText()+"$STARTBLACKLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"lootbags:itemlootbag:0");
		list.add(ConfigText.TAB.getText()+"$ENDBLACKLIST");
		list.add("$ENDBAG:lootbagCommon");
		
		//uncommon bag
		list.add("$STARTBAG:lootbagUncommon:1");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:false");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$GREEN");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:What's inside is not as interesting as not knowing.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DROPCHANCES");
		list.add(ConfigText.TAB.getText()+"$WEIGHT:4000");
		list.add(ConfigText.TAB.getText()+"$CRAFTEDFROM:lootbagCommon");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:50");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:50");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:100");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:true");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:5");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMGENERALLOOTWEIGHT:20");
		list.add(ConfigText.TAB.getText()+"$MINIMUMGENERALLOOTWEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:none");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:false");
		list.add(ConfigText.TAB.getText()+"$STARTBLACKLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"lootbags:itemlootbag:0");
		list.add(ConfigText.TAB.getText()+"$ENDBLACKLIST");
		list.add("$ENDBAG:lootbagUncommon");
		
		//rare bag
		list.add("$STARTBAG:lootbagRare:2");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:false");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$BLUE");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:What's inside is not as interesting as not knowing.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DROPCHANCES");
		list.add(ConfigText.TAB.getText()+"$WEIGHT:16000");
		list.add(ConfigText.TAB.getText()+"$CRAFTEDFROM:lootbagUncommon");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:15");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:15");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:50");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:true");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:5");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMGENERALLOOTWEIGHT:15");
		list.add(ConfigText.TAB.getText()+"$MINIMUMGENERALLOOTWEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:none");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:false");
		list.add(ConfigText.TAB.getText()+"$STARTBLACKLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"lootbags:itemlootbag:0");
		list.add(ConfigText.TAB.getText()+"$ENDBLACKLIST");
		list.add("$ENDBAG:lootbagRare");
		
		//epic bag
		list.add("$STARTBAG:lootbagEpic:3");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:false");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$LIGHT_PURPLE");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:What's inside is not as interesting as not knowing.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DROPCHANCES");
		list.add(ConfigText.TAB.getText()+"$WEIGHT:64000");
		list.add(ConfigText.TAB.getText()+"$CRAFTEDFROM:lootbagRare");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:10");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:10");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:15");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:true");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:5");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMGENERALLOOTWEIGHT:10");
		list.add(ConfigText.TAB.getText()+"$MINIMUMGENERALLOOTWEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:none");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:false");
		list.add(ConfigText.TAB.getText()+"$STARTBLACKLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"lootbags:itemlootbag:0");
		list.add(ConfigText.TAB.getText()+"$ENDBLACKLIST");
		list.add("$ENDBAG:lootbagEpic");
		
		//legendary bag
		list.add("$STARTBAG:lootbagLegendary:4");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:false");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$GOLD");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:What's inside is not as interesting as not knowing.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DROPCHANCES");
		list.add(ConfigText.TAB.getText()+"$WEIGHT:256000");
		list.add(ConfigText.TAB.getText()+"$CRAFTEDFROM:lootbagEpic");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:5");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:5");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:10");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:15");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:true");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:5");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMGENERALLOOTWEIGHT:5");
		list.add(ConfigText.TAB.getText()+"$MINIMUMGENERALLOOTWEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:none");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:false");
		list.add(ConfigText.TAB.getText()+"$STARTBLACKLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"lootbags:itemlootbag:0");
		list.add(ConfigText.TAB.getText()+"$ENDBLACKLIST");
		list.add("$ENDBAG:lootbagLegendary");
		
		/**
		 * Secret Bags
		 */
		//Bacon Bag
		list.add("$STARTBAG:lootbagBacon:5");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:\u00A7d");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:\u00A7d:Turns out there is bacon inside...");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:\u00A77:Three out of every four bacons agree that they don't have enough bacon. The fourth has a bag full of bacon.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:\u00A7b:(It still isn't enough bacon.)");
		list.add(ConfigText.TAB.getText()+"$WEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$CRAFTEDFROM:$NULL");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:false");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:3");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:none");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:true");
		list.add(ConfigText.TAB.getText()+"$STARTENTITYLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"$VISIBLENAME:bacon_donut");
		list.add(ConfigText.TAB.getText()+"$ENDENTITYLIST");
		list.add(ConfigText.TAB.getText()+"$STARTWHITELIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:porkchop:0:1:8:20");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:cooked_porkchop:0:1:8:20");
		list.add(ConfigText.TAB.getText()+"$ENDWHITELIST");
		list.add("$ENDBAG:lootbagBacon");
		
		//Worn Out Bag
		list.add("$STARTBAG:lootbagWornOut:6");
//		list.add(ConfigText.TAB.getText()+"$BAGCOLOR:204|0|204:51|255|51");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$GRAY");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:I told you my bags don't drop beds!");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:My bags are not configured to drop beds in this pack. I am 100% certain about this.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DARK_PURPLE:~Malorolam");
		list.add(ConfigText.TAB.getText()+"$WEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$CRAFTEDFROM:$NULL");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:1");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:1");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:1");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:1");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:false");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:item");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:false");
		list.add(ConfigText.TAB.getText()+"$STARTWHITELIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:cake:0:1:1:20");
		list.add(ConfigText.TAB.getText()+"$ENDWHITELIST");
		list.add("$ENDBAG:lootbagWornOut");
		
		//Soaryn Bag
		list.add("$STARTBAG:lootbagSoaryn:7");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$BLUE");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:Everytime a random chest is placed, a Soaryn gets more Chick Fil A.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:One out of every four chests is a Soaryn chest. Only you can prevent inventory clutter by creating more.");
		list.add(ConfigText.TAB.getText()+"$WEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$CRAFTEDFROM:$NULL");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:false");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:3");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:3");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:item");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:true");
		list.add(ConfigText.TAB.getText()+"$STARTENTITYLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"$VISIBLENAME:Soaryn");
		list.add(ConfigText.TAB.getText()+"$ENDENTITYLIST");
		list.add(ConfigText.TAB.getText()+"$STARTWHITELIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:chest:0:1:2:20");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:stick:0:1:1:20");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:quartz:0:4:4:20");
		list.add(ConfigText.TAB.getText()+"$ENDWHITELIST");
		list.add("$ENDBAG:lootbagSoaryn");
		
		//Wyld Bag
		list.add("$STARTBAG:lootbagWyld:8");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$RED");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:Raise your Cluckingtons!");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:Cluck Cluck...");
		list.add(ConfigText.TAB.getText()+"$WEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$CRAFTEDFROM:$NULL");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:false");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:item");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:true");
		list.add(ConfigText.TAB.getText()+"$STARTENTITYLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"$VISIBLENAME:Wyld");
		list.add(ConfigText.TAB.getText()+"$ENDENTITYLIST");
		list.add(ConfigText.TAB.getText()+"$STARTWHITELIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:spawn_egg:93:1:1:20");
		list.add(ConfigText.TAB.getText()+"$ENDWHITELIST");
		list.add("$ENDBAG:lootbagWyld");
		
		//Bat Bag
		list.add("$STARTBAG:lootbagBat:9");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$DARK_GRAY");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$DARK_GREEN:A hero with no praise or glory.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:$GREEN:Paging Doctor Bat, paging Doctor Bat! Is there a Doctor Bat in the room?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$GREEN:Where oh where has my little Bat gone? Oh where, oh where can he be? His cowl, his scowl, his temper so foul. I do hope he's coming for me.");
		list.add(ConfigText.TAB.getText()+"$WEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$CRAFTEDFROM:$NULL");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:false");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:item");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:true");
		list.add(ConfigText.TAB.getText()+"$STARTENTITYLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"$VISIBLENAME:Batman");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"$INTERNALNAME:Bat");
		list.add(ConfigText.TAB.getText()+"$ENDENTITYLIST");
		list.add(ConfigText.TAB.getText()+"$STARTWHITELIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:spawn_egg:65:1:1:20");
		list.add(ConfigText.TAB.getText()+"$ENDWHITELIST");
		list.add("$ENDBAG:lootbagBat");
		
		return list;
	}
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/