package mal.lootbags;

import java.util.ArrayList;
import java.util.StringTokenizer;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;

/**
 * General utility class for random stuff needed multiple places
 * @author Mal
 *
 */
public class LootbagsUtil {

	public static void LogError(String message)
	{
		FMLLog.log(Level.ERROR, message);
	}
	
	public static void LogError(String message, ICommandSender icommand)
	{
		LogError(message);
		if(icommand != null)
			icommand.addChatMessage(new ChatComponentText(message));
	}
	
	public static void LogInfo(String message)
	{
		FMLLog.log(Level.INFO, message);
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
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/