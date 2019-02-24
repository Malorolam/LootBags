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
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;

import mal.core.util.BagConfigException;
import mal.lootbags.Bag;
import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import mal.lootbags.handler.BagHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.common.config.Configuration.UnicodeInputStreamReader;

/*
 * Handles the bag config file, loading and parsing it 
 */
@SuppressWarnings("FieldCanBeLocal")
public class BagConfigHandler {

	private enum ConfigText
	{
		TAB("    "), ERROR("Lootbags !!CONFIG ERROR!!    "), INFO("Lootbags Config Information:    ");
		
		private String text;
		
		ConfigText(String text)
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
    public static final CharMatcher allowedProperties = CharMatcher.javaLetterOrDigit().or(CharMatcher.anyOf(ALLOWED_CHARS));

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
            LootbagsUtil.LogError("An exception occurred while loading config file" + file.getName() + ". This file will be renamed to " + fileBak.getName() +
            		"and a new config file will be generated.");
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
        		} catch (IOException ignored){}
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
		boolean commentEscape = false;
		
		for(int linenum = 0; linenum < fileList.size(); linenum++)
		{
			
			String line = fileList.get(linenum);
			String trim = line.trim();
			
			//regex to separate the words
			String[] words = trim.split("(?<![$]):");
			if(trim.startsWith("//"))
			{
				//do nothing
				int l = linenum+1;
				LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Comment at line " + l);
			}
			else if(trim.startsWith("/*"))
			{
				commentEscape = true;
				int l = linenum+1;
				LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Comment start at line " + l);
			}
			else if(trim.endsWith("*/"))
			{
				commentEscape = false;
				int l = linenum+1;
				LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Comment end at line " + l);
			}
			else if(words[0].startsWith("$") && !commentEscape)//command and not in a comment block
			{
				//look in the words for comment information and trim out words after it
				int excludeIndex;
				for(int i = 0; i < words.length; i++)
				{
					String wd = words[i];
					if(wd.contains("//"))
					{
						excludeIndex = (i+1<=words.length)?(i+1):(i);//avoid array out of bounds exceptions hopefully
						boolean notched = false;
						if(wd.startsWith("//")) {
							excludeIndex -= 1;//notch the words down if it's the entire thing
							notched = true;
						}
						String[] wwd = new String[excludeIndex];
						for(int j = 0; j < wwd.length; j++)
							wwd[j] = words[j];
						words = wwd;

						//Remove characters from this word after the excape characters
						if(words.length > i) {//no need to work with this word if it's been removed
							String[] wds = wd.split("/(?=/)");
							words[i] = wds[0];
						}
						break;
					}
				}
				switch(words[0].toUpperCase())
				{
				case "$CONFIGVERSION":
					checkVersion(words, linenum+1);
					break;
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
/*				case "$CRAFTEDFROM"://the bag name and number of bags needed to craft this bag, expects two words
					addBagCrafting(words, linenum+1, currentBag);
					break;*///No longer functional
				case "$BAGVALUE"://the value of the bag for storage and conversion
					addBagValue(words, linenum+1, currentBag);
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
				case "$BLACKLISTRECYCLER"://if the items are blacklisted in the recycler, expects two words
					addRecyclerBlacklist(words, linenum+1, currentBag);
					break;
				case "$ENDBAG"://end the bag properly
					endNewBag(words,linenum,currentBag);
					currentBag=null;
					break;
				}
					
			}
			else if (!commentEscape)
			{
				int l = linenum+1;
				LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Text at line: " + l + " is not a command or in a list.  Please only have commands, list components, or whitespace in the config.", command);
				LootbagsUtil.LogError(ConfigText.INFO.getText()+"Text for reference: " + trim, command);
			}
		}
		LootbagsUtil.LogInfo("Bag Config Completed.");
	}

	private void checkVersion(String[] words, int linenum)
	{
		if(words.length == 2 && (words[1].equals(LootBags.CONFIGVERSION) || words[1].equals("BYPASS")))
			LootBags.configMismatch = false;
	}

	private Bag startNewBag(String[] words, int linenum, Bag currentBag)
	{
		if(words.length<3)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag start command at line " + linenum + " has error: Too few words, it needs the command, the bag name, and the bag id to properly initialize a bag.");
		}
		if(words.length>3)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag start command at line " + linenum + " has error: Too many words, it needs the command, the bag name, and the bag id only.");
		}
		if(currentBag != null)
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag start command at line " + linenum + " has error: There is a bag already open with name " + currentBag.getBagName() + ".");
		}
		
		int bagID=-1;
		try {
			bagID = Integer.parseInt(words[2]);
		}
		catch (Exception e)
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag start command at line " + linenum + " has error: Third word is not a number.");
		}
		if(!BagHandler.isIDFree(bagID))
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag start command at line " + linenum + " has error: Specified Bag ID not free.");
		}
		LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Started defining properties for bag named: " + words[1] + ".");
		return new Bag(words[1], bagID);
	}
	
	private void endNewBag(String[] words, int linenum, Bag currentBag)
	{
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag end command at line " + linenum + " has error: Too few words, it needs the command and the bag name.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag end command at line " + linenum + " has error: Too many words, it only needs the command and the bag name.");
		}
		if(words[1].equals(currentBag.getBagName()))
		{
			LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Successfully closed bag with name: "+words[1]+".");
			
			BagHandler.addBag(currentBag);
			return;
		}
		else
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag end command at line " + linenum + " is not closing the currently open bag.  Bag will be not saved.");
		}
	}
	
	private void addBagColor(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<3)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag color command at line " + linenum + " has error: Too few words, it needs the command and two colors in RGB format.");
		}
		if(words.length>3)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag image command at line " + linenum + " has error: Too many words, it needs only the command and two colors in RGB format.");
		}
		currentBag.setBagColor(parseBagColor(words[1]), parseBagColor(words[2]));
		
		LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Added color to bag: " + currentBag.getBagName() + ".");
	}
	
	private void addSecretState(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag secret command at line " + linenum + " has error: Too few words, it needs the command and a boolean state (true/false) to to set the secret state.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag secret command at line " + linenum + " has error: Too many words, it needs only the command and the boolean state.");
		}
		if(words[1].equalsIgnoreCase("true") || words[1].equalsIgnoreCase("t"))
			currentBag.setSecret(true);
		else if(words[1].equalsIgnoreCase("false") || words[1].equalsIgnoreCase("f"))
			currentBag.setSecret(false);
		else
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag secret command at line " + linenum + " has error: text is not true, t, false, or f.  Please use one of those four options.");
		}
		LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Set bag secret state for bag: " + currentBag.getBagName() + ".");
	}
	
	private void addBagNameColor(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag name color command at line " + linenum + " has error: Too few words, it needs the command and either a color string or a color command.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag name color at line " + linenum + " has error: Too many words, it needs only the command and a color string or command.");
		}
		
		String code = parseColorText(words[1]);
		currentBag.setBagNameColor(code);
	}
	
	private void addBagTextUnopened(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag unopened text command at line " + linenum + " has error: Too few words, it needs the command and at minimum the text to add.");
		}
		if(words.length>3)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag unopened text at line " + linenum + " has error: Too many words, it needs only the command, the color (either the / code or the $ command), and the text to add.");
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
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag opened text command at line " + linenum + " has error: Too few words, it needs the command and at minimum the text to add.");
		}
		if(words.length>3)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag opened text at line " + linenum + " has error: Too many words, it needs only the command, the color (either the / code or the $ command), and the text to add.");
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
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag shift text command at line " + linenum + " has error: Too few words, it needs the command and at minimum the text to add.");
		}
		if(words.length>3)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag shift text at line " + linenum + " has error: Too many words, it needs only the command, the color (either the / code or the $ command), and the text to add.");
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
	
/*	private void addBagWeight(String[] words, int linenum, Bag currentBag)
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
	}*/
	private void addBagValue(String[] words, int linenum, Bag currentBag)
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
		if(words.length>4)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag weight at line " + linenum + " has error: Too many words, it needs only the command and the weight.", command);
			return;
		}
		
		try
		{
			int lowerweight = Integer.parseInt(words[1]);
			int upperweight = lowerweight;
			if(words.length>2)
				upperweight = Integer.parseInt(words[2]);
			currentBag.setBagValue(lowerweight, upperweight);
		}
		catch(Exception e)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag weight command at line " + linenum + " has error: Second or third word is not a number.", command);
			return;
		}
	}
	
/*	private void addBagCrafting(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<3)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag crafting command at line " + linenum + " has error: Too few words, it needs the command, the source bag name, and the number of bags needed.");
		}
		if(words.length>3)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag crafting at line " + linenum + " has error: Too many words, it needs only the command, the source bag name, and the number of bags needed.");
		}
		
		String name = words[1];
		int count = Integer.parseInt(words[2]);
		
		if(!name.equalsIgnoreCase("$NULL"))
			currentBag.setCraftingSource(name, count);
	}*/
	
	private void addPassiveSpawnWeight(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag spawn weight command at line " + linenum + " has error: Too few words, it needs the command and the weight.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag spawn weight at line " + linenum + " has error: Too many words, it needs only the command and the weight.");
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
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag spawn weight command at line " + linenum + " has error: Too few words, it needs the command and the weight.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag spawn weight at line " + linenum + " has error: Too many words, it needs only the command and the weight.");
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
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag spawn weight command at line " + linenum + " has error: Too few words, it needs the command and the weight.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag spawn weight at line " + linenum + " has error: Too many words, it needs only the command and the weight.");
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
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag spawn weight command at line " + linenum + " has error: Too few words, it needs the command and the weight.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag spawn weight at line " + linenum + " has error: Too many words, it needs only the command and the weight.");
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
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag general source command at line " + linenum + " has error: Too few words, it needs the command and a boolean value.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag general source command at line " + linenum + " has error: Too many words, it needs only the command and a boolean value.");
		}
		
		if(words[1].equalsIgnoreCase("true") || words[1].equalsIgnoreCase("t") || words[1].equalsIgnoreCase("1"))
			currentBag.setGeneralSources(true);
		else if(words[1].equalsIgnoreCase("false") || words[1].equalsIgnoreCase("f") || words[1].equalsIgnoreCase("0"))
			currentBag.setGeneralSources(false);
		else
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag general source command at line " + linenum + " has error: boolean value not recognized as boolean.");
	}
	
	private void addMaximumItemCount(String[] words, int linenum, Bag currentBag) {
		if(currentBag==null)
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag maximum item count command at line " + linenum + " has error: Too few words, it needs the command and the number of items.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag maximum item count at line " + linenum + " has error: Too many words, it needs only the command and the number of items.");
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
		
		if(weight > BagHandler.HARDMAX)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag maximum item count at line " + linenum + " has error: count is greater than " + BagHandler.HARDMAX + ", setting to " + BagHandler.HARDMAX + ".", command);
			weight = BagHandler.HARDMAX;
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
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag minimum item count command at line " + linenum + " has error: Too few words, it needs the command and the number of items.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag minimum item count at line " + linenum + " has error: Too many words, it needs only the command and the number of items.");
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
		
		if(weight > BagHandler.HARDMAX)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag minimum item count at line " + linenum + " has error: count is greater than " + BagHandler.HARDMAX + ", setting to " + BagHandler.HARDMAX + ".", command);
			weight = BagHandler.HARDMAX;
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
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag maximum general weight command at line " + linenum + " has error: Too few words, it needs the command and weight.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag maximum general weight command at line " + linenum + " has error: Too many words, it needs only the command and weight.");
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
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag minimum general weight command at line " + linenum + " has error: Too few words, it needs the command and weight.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag minimum general weight command at line " + linenum + " has error: Too many words, it needs only the command and weight.");
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
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag item repeat command at line " + linenum + " has error: Too few words, it needs the command and a boolean value.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag item repeat command at line " + linenum + " has error: Too many words, it needs only the command and a boolean value.");
		}
		
		if(words[1].equalsIgnoreCase("none"))
			currentBag.setItemRepeats(0);
		else if(words[1].equalsIgnoreCase("damage"))
			currentBag.setItemRepeats(1);
		else if(words[1].equalsIgnoreCase("item"))
			currentBag.setItemRepeats(2);
		else if(words[1].equalsIgnoreCase("fixed"))
			currentBag.setItemRepeats(3);
		else
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag item repeat command at line " + linenum + " has error: text not recognized as 'none', 'damage', or 'item'.");
	}
	
	private void addExcludeEntities(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.");
		}
		if(words.length<2)//insufficient words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag entitity exclusion command at line " + linenum + " has error: Too few words, it needs the command and a boolean value.");
		}
		if(words.length>2)//excessive words error
		{
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag entity exlusion command at line " + linenum + " has error: Too many words, it needs only the command and a boolean value.");
		}
		
		if(words[1].equalsIgnoreCase("true") || words[1].equalsIgnoreCase("t") || words[1].equalsIgnoreCase("1"))
			currentBag.setEntityExclusion(true);
		else if(words[1].equalsIgnoreCase("false") || words[1].equalsIgnoreCase("f") || words[1].equalsIgnoreCase("0"))
			currentBag.setEntityExclusion(false);
		else
			throw new BagConfigException(ConfigText.ERROR.getText()+"Bag entity exclusion command at line " + linenum + " has error: boolean value not recognized as boolean.");
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
			else if(tempwords.length<2)
				LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Unknown text found at line " + templine + ": skipping line.", command);
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
			else if (tempwords.length == 2)//length for a loot category
			{
				try {
					String modid = tempwords[0];
					String loc = tempwords[1];
					String tt = modid + ":" + loc;
					ResourceLocation catloc = new ResourceLocation(tt);
					currentBag.addWhitelistCategory(catloc);
				} catch (Exception e)
				{
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
	
	private void addRecyclerBlacklist(String[] words, int linenum, Bag currentBag)
	{
		if(currentBag==null)
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"No active bag, ensure that a bag is correctly started before trying to change information on it.", command);
			return;
		}
		if(words.length<2)//insufficient words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Recycler Blacklist command at line " + linenum + " has error: Too few words, it needs the command and a boolean value.", command);
			return;
		}
		if(words.length>2)//excessive words error
		{
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Recycler Blacklist command at line " + linenum + " has error: Too many words, it needs only the command and a boolean value.", command);
			return;
		}
		
		if(words[1].equalsIgnoreCase("true") || words[1].equalsIgnoreCase("t") || words[1].equalsIgnoreCase("1"))
			currentBag.setRecyclerBlacklist(true);
		else if(words[1].equalsIgnoreCase("false") || words[1].equalsIgnoreCase("f") || words[1].equalsIgnoreCase("0"))
			currentBag.setRecyclerBlacklist(false);
		else
			LootbagsUtil.LogError(ConfigText.ERROR.getText()+"Bag entity exclusion command at line " + linenum + " has error: boolean value not recognized as boolean.", command);
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
					code = TextFormatting.WHITE.toString();
					break;
				case "BLACK":
					code = TextFormatting.BLACK.toString();
					break;
				case "DARK_BLUE":
					code = TextFormatting.DARK_BLUE.toString();
					break;
				case "DARK_GREEN":
					code = TextFormatting.DARK_GREEN.toString();
					break;
				case "DARK_AQUA":
					code = TextFormatting.DARK_AQUA.toString();
					break;
				case "DARK_RED":
					code = TextFormatting.DARK_RED.toString();
					break;
				case "DARK_PURPLE":
					code = TextFormatting.DARK_PURPLE.toString();
					break;
				case "GOLD":
					code = TextFormatting.GOLD.toString();
					break;
				case "GRAY":
					code = TextFormatting.GRAY.toString();
					break;
				case "DARK_GRAY":
					code = TextFormatting.DARK_GRAY.toString();
					break;
				case "BLUE":
					code = TextFormatting.BLUE.toString();
					break;
				case "GREEN":
					code = TextFormatting.GREEN.toString();
					break;
				case "AQUA":
					code = TextFormatting.AQUA.toString();
					break;
				case "RED":
					code = TextFormatting.RED.toString();
					break;
				case "LIGHT_PURPLE":
					code = TextFormatting.LIGHT_PURPLE.toString();
					break;
				case "YELLOW":
					code = TextFormatting.YELLOW.toString();
					break;
				case "OBFUSCATED":
					code = TextFormatting.OBFUSCATED.toString();
					break;
				case "BOLD":
					code = TextFormatting.BOLD.toString();
					break;
				case "STRIKETHROUGH":
					code = TextFormatting.STRIKETHROUGH.toString();
					break;
				case "UNDERLINE":
					code = TextFormatting.UNDERLINE.toString();
					break;
				case "ITALIC":
					code = TextFormatting.ITALIC.toString();
					break;
				default:
					code = null;
			}
			if(code!=null)
				LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Parsed text to TextFormatting option: " + code +".");
			else
			{
				LootbagsUtil.LogInfo(ConfigText.INFO.getText()+"Could not parse text command as TextFormatting.");
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
		//version info
		list.add("$CONFIGVERSION:"+ LootBags.CONFIGVERSION);
		//common bag
		list.add("$STARTBAG:lootbag_Common:0");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:false");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$WHITE");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:What's inside is not as interesting as not knowing.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DROPCHANCES");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:1:1");
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
		list.add("$ENDBAG:lootbag_Common");
		
		//uncommon bag
		list.add("$STARTBAG:lootbag_Uncommon:1");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:false");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$GREEN");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:What's inside is not as interesting as not knowing.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DROPCHANCES");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:4:4");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:50");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:50");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:100");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:true");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:5");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMGENERALLOOTWEIGHT:40");
		list.add(ConfigText.TAB.getText()+"$MINIMUMGENERALLOOTWEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:none");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:false");
		list.add(ConfigText.TAB.getText()+"$STARTBLACKLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"lootbags:itemlootbag:0");
		list.add(ConfigText.TAB.getText()+"$ENDBLACKLIST");
		list.add("$ENDBAG:lootbag_Uncommon");
		
		//rare bag
		list.add("$STARTBAG:lootbag_Rare:2");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:false");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$BLUE");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:What's inside is not as interesting as not knowing.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DROPCHANCES");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:16:16");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:15");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:15");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:50");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:true");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:5");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMGENERALLOOTWEIGHT:30");
		list.add(ConfigText.TAB.getText()+"$MINIMUMGENERALLOOTWEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:none");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:false");
		list.add(ConfigText.TAB.getText()+"$STARTBLACKLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"lootbags:itemlootbag:0");
		list.add(ConfigText.TAB.getText()+"$ENDBLACKLIST");
		list.add("$ENDBAG:lootbag_Rare");
		
		//epic bag
		list.add("$STARTBAG:lootbag_Epic:3");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:false");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$LIGHT_PURPLE");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:What's inside is not as interesting as not knowing.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DROPCHANCES");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:64:64");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:10");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:10");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:15");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:25");
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
		list.add("$ENDBAG:lootbag_Epic");
		
		//legendary bag
		list.add("$STARTBAG:lootbag_Legendary:4");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:false");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$GOLD");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:What's inside is not as interesting as not knowing.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DROPCHANCES");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:256:256");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:5");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:5");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:10");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:15");
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
		list.add("$ENDBAG:lootbag_Legendary");

		//Bacon Bag
		list.add("$STARTBAG:lootbag_Bacon:5");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:\u00A7d");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:\u00A7d:Turns out there is bacon inside...");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:\u00A77:Three out of every four bacons agree that they don't have enough bacon. The fourth has a bag full of bacon.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:\u00A7b:(It still isn't enough bacon.)");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:-1:-1");
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
		list.add("$ENDBAG:lootbag_Bacon");
		
		//Worn Out Bag
		list.add("$STARTBAG:lootbag_Worn_Out:6");
		list.add(ConfigText.TAB.getText()+"$BAGCOLOR:97|28|161:16|145|14");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$GRAY");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:The Fluffiest of Truth.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:My bags are not configured to drop beds in this pack. I am 100% certain about this.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DARK_PURPLE:~Malorolam");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:-1:-1");
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
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"$VISIBLENAME:Malorolam");
		list.add(ConfigText.TAB.getText()+"$ENDENTITYLIST");
		list.add(ConfigText.TAB.getText()+"$STARTWHITELIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:bed:0:1:1:20:31|-117|8|0|0|0|0|0|0|0|-29|98|96|96|102|-32|10|74|45|72|-52|44|114|-50|47|46|97|0|2|46|6|-10|-108|-52|-30|-126|-100|-60|74|14|6|22|-65|-60|-36|84|6|78|-65|-4|18|5|71|5|-89|-44|20|6|6|0|122|-45|0|-36|50|0|0|0");
		list.add(ConfigText.TAB.getText()+"$ENDWHITELIST");
		list.add("$ENDBAG:lootbag_Worn_Out");
		
		//Soaryn Bag
		list.add("$STARTBAG:lootbag_Soaryn:7");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$BLUE");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:Everytime a random chest is placed, a Soaryn gets more Chick Fil A.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:One out of every four chests is a Soaryn chest. Only you can prevent inventory clutter by creating more.");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:-1:-1");
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
		list.add("$ENDBAG:lootbag_Soaryn");
		
		//Wyld Bag
		list.add("$STARTBAG:lootbag_Wyld:8");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$RED");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Ooh, what could be inside?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:Raise your Cluckingtons!");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:Cluck Cluck...");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:-1:-1");
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
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:spawn_egg:0:1:1:20:31|-117|8|0|0|0|0|0|0|0|-29|98|96|-32|98|-32|116|-51|43|-55|44|-87|12|73|76|-25|96|96|-54|76|97|96|119|-50|-56|76|-50|78|-51|99|96|0|0|104|-97|-118|-19|31|0|0|0");
		list.add(ConfigText.TAB.getText()+"$ENDWHITELIST");
		list.add("$ENDBAG:lootbag_Wyld");
		
		//Bat Bag
		list.add("$STARTBAG:lootbag_Bat:9");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$DARK_GRAY");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$DARK_GREEN:A hero with no praise or glory.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:$GREEN:Paging Doctor Bat, paging Doctor Bat! Is there a Doctor Bat in the room?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DARK_GRAY:Stop touching me! I am the night! I am the night!");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:-1:-1");
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
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:spawn_egg:0:1:1:20:31|-117|8|0|0|0|0|0|0|0|-29|98|96|-32|98|-32|116|-51|43|-55|44|-87|12|73|76|-25|96|96|-54|76|97|96|118|74|44|97|96|0|0|110|124|-90|-64|27|0|0|0");
		list.add(ConfigText.TAB.getText()+"$ENDWHITELIST");
		list.add("$ENDBAG:lootbag_Bat");
		
		//Darkosto Bag
		list.add("$STARTBAG:lootbag_Darkosto:10");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$GRAY");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$DARK_RED:The most fitting of loot.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:$YELLOW:Happy Birthday Darkosto ~ Wyld");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DARK_RED:Only drops on a certain special day.");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:-1:-1");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:false");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:5");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:3");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:none");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:true");
		list.add(ConfigText.TAB.getText()+"$STARTENTITYLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"$VISIBLENAME:Darkosto");
		list.add(ConfigText.TAB.getText()+"$ENDENTITYLIST");
		list.add(ConfigText.TAB.getText()+"$STARTWHITELIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:cake:0:1:1:20");
		list.add(ConfigText.TAB.getText()+"$ENDWHITELIST");
		list.add("$ENDBAG:lootbag_Darkosto");

		//artifact bag
		list.add("$STARTBAG:lootbag_Artifact:11");
		list.add(ConfigText.TAB.getText()+"$BAGCOLOR:244|167|66:89|79|53");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:false");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$YELLOW");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:One of a kind?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:No, not really.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$WHITE:Hopefully not a supernova on a stick.");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:1024:1024");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:1");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:1");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:1");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:1");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:true");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:3");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMGENERALLOOTWEIGHT:6");
		list.add(ConfigText.TAB.getText()+"$MINIMUMGENERALLOOTWEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:none");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:false");
		list.add(ConfigText.TAB.getText()+"$STARTWHITELIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"lootbags:itemlootbag:12:1:1:1");
		list.add(ConfigText.TAB.getText()+"$ENDWHITELIST");
		list.add("$ENDBAG:lootbag_Artifact");

		//Old Blue Bag
		list.add("$STARTBAG:lootbag_Old_Blue:12");
		list.add(ConfigText.TAB.getText()+"$BAGCOLOR:84|89|142:17|23|84");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$BLUE");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$BLUE:A mysterious blue bag that");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$BLUE:seems to hold something inside.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:$BLUE:I don't know what I expected.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$BLUE:You'll have to open it to find out.");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:0:64");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:0");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:0");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:0");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:0");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:true");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMGENERALLOOTWEIGHT:-1");
		list.add(ConfigText.TAB.getText()+"$MINIMUMGENERALLOOTWEIGHT:20");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:none");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:false");
		list.add(ConfigText.TAB.getText()+"$STARTWHITELIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"lootbags:itemlootbag:12:1:1:20");
		list.add(ConfigText.TAB.getText()+"$ENDWHITELIST");
		list.add("$ENDBAG:lootbag_Old_Blue");

		//Patient Bag
		list.add("$STARTBAG:lootbag_Patient:13");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGCOLOR:71|0|0:0|0|0");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$GRAY");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$DARK_RED:Eventually it may be valuable.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:$DARK_RED:Were you patient enough?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$DARK_RED:Waiting is the best game of all.");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:-1:-1");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:5");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:5");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:5");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:5");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:false");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:none");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:true");
		list.add(ConfigText.TAB.getText()+"$BLACKLISTRECYCLER:true");
		list.add(ConfigText.TAB.getText()+"$STARTENTITYLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"$INTERNALNAME:Zombie");
		list.add(ConfigText.TAB.getText()+"$ENDENTITYLIST");
		list.add(ConfigText.TAB.getText()+"$STARTWHITELIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:nether_star:0:1:1:1");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"lootbags:itemlootbag:13:1:1:9999");
		list.add(ConfigText.TAB.getText()+"$ENDWHITELIST");
		list.add("$ENDBAG:lootbag_Patient");

		//Artificial Bag
		list.add("$STARTBAG:lootbag_Artificial:14");
		list.add(ConfigText.TAB.getText()+"$BAGCOLOR:244|167|66:89|79|53");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$YELLOW");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:One of many?");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:No, not really.");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$WHITE:Hopefully not a dire error.");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:2:-1");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:1");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:1");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:1");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:1");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:true");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:3");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:1");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMGENERALLOOTWEIGHT:40");
		list.add(ConfigText.TAB.getText()+"$MINIMUMGENERALLOOTWEIGHT:20");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:none");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:false");
		list.add("$ENDBAG:lootbag_Artificial");

		//Direwolf20 Bag
		list.add("$STARTBAG:lootbag_Direwolf:20");
		list.add(ConfigText.TAB.getText()+"$BAGCOLOR:93|181|204:70|71|135");
		list.add(ConfigText.TAB.getText()+"$ISSECRET:true");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTCOLOR:$AQUA");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTUNOPENED:$AQUA:Hello Everyone!");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTOPENED:$AQUA:Take it easy!");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:Enclosed is everything one needs to");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:make their very own Direwolf20 9x9!");
		list.add(ConfigText.TAB.getText()+"$BAGTEXTSHIFT:$AQUA:(Door and lighting sold separately)");
		list.add(ConfigText.TAB.getText()+"$BAGVALUE:-1:-1");
		list.add(ConfigText.TAB.getText()+"$PASSIVESPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$PLAYERSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$MOBSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$BOSSSPAWNWEIGHT:25");
		list.add(ConfigText.TAB.getText()+"$USEGENERALLOOTSOURCES:false");
		list.add(ConfigText.TAB.getText()+"$MAXIMUMITEMS:5");
		list.add(ConfigText.TAB.getText()+"$MINIMUMITEMS:5");
		list.add(ConfigText.TAB.getText()+"$PREVENTITEMREPEATS:fixed");
		list.add(ConfigText.TAB.getText()+"$EXCLUDEENTITIES:true");
		list.add(ConfigText.TAB.getText()+"$BLACKLISTRECYCLER:true");
		list.add(ConfigText.TAB.getText()+"$STARTENTITYLIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"$VISIBLENAME:Direwolf20");
		list.add(ConfigText.TAB.getText()+"$ENDENTITYLIST");
		list.add(ConfigText.TAB.getText()+"$STARTWHITELIST");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:cobblestone:0:64:64:20");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:cobblestone:0:64:64:20");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:cobblestone:0:64:64:20");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:cobblestone:0:62:62:20");
		list.add(ConfigText.TAB.getText()+ConfigText.TAB.getText()+"minecraft:glass:0:36:36:20");
		list.add(ConfigText.TAB.getText()+"$ENDWHITELIST");
		list.add("$ENDBAG:lootbag_Direwolf");
		return list;
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/