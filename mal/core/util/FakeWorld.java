package mal.core.util;

import java.io.File;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

//A fake client to fool the stupid stupid json loot tables; shamelessly based off of how way2muchnoise does it for JER
public class FakeWorld extends World{

	public static final WorldSettings settings = new WorldSettings(0, GameType.SURVIVAL, true, false, WorldType.DEFAULT);
	public static final WorldInfo info = new WorldInfo(settings, "lootbags_fake_info");
	public static final FakeSave saves = new FakeSave();
	public static final WorldProvider provider = new WorldProvider() {
		@Override
		public DimensionType getDimensionType()
		{
			return DimensionType.OVERWORLD;
		}
	};
	
	public FakeWorld()
	{
		super(saves, info, provider, new Profiler(), true);
	}
	
	@Override
	protected IChunkProvider createChunkProvider()
	{
		return new IChunkProvider() {

			@Override
			public Chunk getLoadedChunk(int x, int z) {
				return null;
			}

			@Override
			public Chunk provideChunk(int x, int z) {
				return null;
			}

			@Override
			public String makeString() {
				return null;
			}

			@Override
			public boolean tick() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isChunkGeneratedAt(int p_191062_1_, int p_191062_2_) {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}
	
	@Override
	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty)
	{
		return false;
	}
	
	private static class FakeSave implements ISaveHandler
	{

		@Override
		public WorldInfo loadWorldInfo() {
			return info;
		}

		@Override
		public void checkSessionLock() throws MinecraftException {		}

		@Override
		public IChunkLoader getChunkLoader(WorldProvider provider) {
			return new IChunkLoader() {

				@Override
				public Chunk loadChunk(World worldIn, int x, int z) throws IOException {
					return null;
				}

				@Override
				public void saveChunk(World worldIn, Chunk chunkIn) throws MinecraftException, IOException {
				}

				@Override
				public void saveExtraChunkData(World worldIn, Chunk chunkIn) throws IOException {
				}

				@Override
				public void chunkTick() {
				}

				@Override
				public boolean isChunkGeneratedAt(int p_191063_1_, int p_191063_2_) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void flush() {
					// TODO Auto-generated method stub
					
				}
			};
		}

		@Override
		public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {
		}

		@Override
		public void saveWorldInfo(WorldInfo worldInformation) {
		}

		@Override
		public IPlayerFileData getPlayerNBTManager() {
			return new IPlayerFileData() {

				@Override
				public void writePlayerData(EntityPlayer player) {
				}

				@Override
				public NBTTagCompound readPlayerData(EntityPlayer player) {
					return new NBTTagCompound();
				}

				@Override
				public String[] getAvailablePlayerDat() {
					return new String[0];
				}
				
			};
		}

		@Override
		public void flush() {
		}

		@Override
		public File getWorldDirectory() {
			return null;
		}

		@Override
		public File getMapFileFromName(String mapName) {
			return null;
		}

		@Override
		public TemplateManager getStructureTemplateManager() {
			return null;
		}
		
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/