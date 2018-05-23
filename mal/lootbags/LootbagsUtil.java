package mal.lootbags;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.loot.LootEntryItemAccess;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import org.apache.logging.log4j.Level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mal.core.util.FakeWorld;
import mal.lootbags.loot.LootItem;

/**
 * General utility class for random stuff needed multiple places
 * @author Mal
 *
 */
public class LootbagsUtil {

	public static void LogError(String message)
	{
		LootBags.LOOTLOG.log(Level.ERROR, message);
	}
	
	public static void LogError(String message, ICommandSender icommand)
	{
		LogError(message);
		if(icommand != null)
			icommand.sendMessage(new TextComponentString(message));
	}
	
	public static void LogInfo(String message)
	{
		if(LootBags.VERBOSEMODE)
			LootBags.LOOTLOG.log(Level.INFO, message);
	}
	
	public static void LogDebug(String message)
	{
		if(LootBags.DEBUGMODE)
			LootBags.LOOTLOG.log(Level.INFO, message);
	}
	
	public static ArrayList<Integer> constructDamageRange(String word)
	{
		//construct the damage values from the syntax, & between two numbers indicates inclusive and # indicates
		//to skip number inbetween the border numbers
		//example: 0&5#8&10 will use damage values 0, 1, 2, 3, 4, 5, 8, 9, 10
		
		ArrayList<Integer> damages = new ArrayList<Integer>();
		
		String[] sec = word.split("[#&]");
		//should be a bunch of numbers now
		
		if(sec.length==1)
		{
			try {
				damages.add(Integer.parseInt(sec[0]));
			}
			catch(Exception e)
			{
				LogError("Cannot construct a damage range that doesn't follow the correct syntax.");
				return null;
			}
			return damages;
		}
		//figure out if between two numbers is inclusive or exclusive
		int wordindex = 0;
		for(int i = 0; i < sec.length-1; i++)
		{
			wordindex += sec[i].length();
			try {
				int num1 = Integer.parseInt(sec[i]);
				int num2 = Integer.parseInt(sec[i+1]);
				if(word.substring(wordindex, wordindex+1).equalsIgnoreCase("#"))//exclusive
				{
					damages.add(num1);
					damages.add(num2);//don't worry about duplicates, we'll fix it later
				}
				else if(word.substring(wordindex, wordindex+1).equalsIgnoreCase("&"))
				{
					if(num2<num1)
					{
						int t = num1;
						num1 = num2;
						num2 = t;
					}
					
					for(int j = num1; j <= num2; j++)
					{
						damages.add(j);
					}
				}
				wordindex += 1;//add in the index for the extra character
			}
			catch(Exception e)
			{
				LogError("Cannot construct a damage range that doesn't follow the correct syntax.");
				return null;
			}
		}
		
		//sort the list and remove all duplicates
				for(int i = 1; i < damages.size(); i++)
				{
					for(int j = i; j>1 && damages.get(j) < damages.get(j-1); j--)
					{
						int temp = damages.get(j);
						damages.set(j, damages.get(j-1));
						damages.set(j-1, temp);
					}
				}
				
				ArrayList<Integer> ret = new ArrayList<Integer>();
				for(int i = 0; i < damages.size(); i++)
				{
					if(!ret.contains(damages.get(i)))
						ret.add(damages.get(i));
				}
				
				return ret;
	}
	
	public static byte[] parseNBTArray(String array)
	{
		String[] sec = array.split("\\|");
		//System.out.println(array);
		byte[] nbt = new byte[sec.length];
		for(int i = 0; i < nbt.length; i++)
			nbt[i]=Byte.parseByte(sec[i]);
		
		return nbt;
	}
	
	/**
	 * Takes an input string and an optional pre-line text and splits the input into multiple lines maximum of MAXLENGTH character
	 * and adds the pretext to the start of each line
	 */
	public static ArrayList<String> addLineBreaks(String input)
	{
		return addLineBreaks(input, "");
	}
	public static ArrayList<String> addLineBreaks(String input, String pretext)
	{
		int MAXLENGTH = 40;//maximum length of a single line in characters
		String APPEND = "|";
		String SPACE = " ";
		
		String[] tokens = input.split("\\s+");
		StringBuilder output = new StringBuilder(input.length());
		
		int len = 0;
		for(int i = 0; i < tokens.length; i++)
		{
			String word = tokens[i];
			
			if(len + (SPACE+word).length() > MAXLENGTH)
			{
				if(i>0)
					output.append(APPEND);
				len = 0;
			}
			if(i < tokens.length -1 && (len+(word+SPACE).length() + tokens[i+1].length() <= MAXLENGTH))
			{
				word += SPACE;
			}
			
			output.append(word);
			len += word.length();
		}
		
		String[] list = output.toString().split("\\|");
		ArrayList<String> ret = new ArrayList<String>();
		for(int i = 0; i < list.length; i++)
			ret.add(pretext+list[i]);
		
		return ret;
	}
	
	public static boolean listContainsItem(ArrayList<LootItem> list, LootItem item)
	{
		for(LootItem loot:list)
		{
			if(LootBags.areItemStacksEqualItem(loot.getContentItem(), item.getContentItem(), true, false))
				return true;
		}
		
		return false;
	}
	
    public static byte[] compress(NBTTagCompound p_74798_0_) throws IOException
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        DataOutputStream dataoutputstream = new DataOutputStream(new GZIPOutputStream(bytearrayoutputstream));

        try
        {
            CompressedStreamTools.write(p_74798_0_, dataoutputstream);
        }
        finally
        {
            dataoutputstream.close();
        }

        return bytearrayoutputstream.toByteArray();
    }
    
    public static ItemStack[] generateStacks(Random rand, LootItem lootitem, int min, int max)
    {
        int count = min + (rand.nextInt(max - min + 1));
        ItemStack source = lootitem.getContentItem().copy();
        source = LootEntryItemAccess.applyFunctions(lootitem.getLootItem(), source, LootBags.LOOTMAP.getContext());

        ItemStack[] ret;
        if (source.getItem() == null)
        {
            ret = new ItemStack[0];
        }
        else if (count > source.getMaxStackSize())
        {
            ret = new ItemStack[count];
            for (int x = 0; x < count; x++)
            {
                ret[x] = source.copy();
                ret[x].setCount(1);
            }
        }
        else
        {
            ret = new ItemStack[1];
            ret[0] = source;
            ret[0].setCount(count);
        }
        return ret;
    }
    
    public static String translateToLocal(String text)
    {
    	return new TextComponentTranslation(text).getFormattedText();
    }
    
    private static LootTableManager manager;
    public static LootTableManager getLootManager(@Nullable World world)
    {
    	if(world == null || world.getLootTableManager() == null)
    	{
    		if(manager == null)
    		{
    			ISaveHandler saveHandler = FakeWorld.saves;
    			manager = new LootTableManager(new File(new File(saveHandler.getWorldDirectory(), "data"), "loot_tables"));
    		}
    		return manager;
    	}
    	return world.getLootTableManager();
    }
    
    public static LootItem getRandomItem(List<LootItem> list, int totalWeight)
    {
    	if(totalWeight <= 0)
    	{
    		LootbagsUtil.LogError("Total Weight less than or equal to zero.");
    		return null;
    	}
    	
    	int weight = LootBags.getRandom().nextInt(totalWeight);
    	for(int i=0; i < list.size(); i++)
    	{
    		LootItem item = list.get(i);
    		weight -= item.itemWeight;
    		
    		if(weight < 0)
    			return item;
    	}
    	
    	return null;
    }
    
    public static NonNullList<Ingredient> parseShapeless(final JsonContext context, final JsonObject json)
    {
    	final NonNullList<Ingredient> ingredients = NonNullList.create();
    	for(final JsonElement element: JsonUtils.getJsonArray(json, "ingredients"))
    		ingredients.add(CraftingHelper.getIngredient(element, context));
    	
    	if(ingredients.isEmpty())
    		throw new JsonParseException("No Ingredients.");
    	
    	return ingredients;
    }
    
    public static String formatSciNot(int value)
    {
    	NumberFormat formatter = new DecimalFormat("0.##E0");
    	if(value > 99999)
    		return formatter.format(value);
    	else
    		return Integer.toString(value);
    }
    
	/*
	 * Helper method for determining if a point is in a region of a gui
	 */
	public static boolean isPointInRegion(int left, int top, int width, int height, int pointx,
			int pointy, int guiLeft, int guiTop) {
        pointx -= guiLeft;
        pointy -= guiTop;
        return pointx >= left - 1 && pointx < left + width + 1 && pointy >= top - 1 && pointy < top + height + 1;
	}

    public static ItemStack[] getItemStackArrayEmpty(int size)
    {
    	ItemStack[] out = new ItemStack[size];
    	for(int i = 0; i < size; i++)
    		out[i] = ItemStack.EMPTY;
    	return out;
    }
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/