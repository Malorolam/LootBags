package mal.lootbags.jei;

import javax.annotation.Nonnull;

import mal.lootbags.LootBags;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

@JEIPlugin
public class JEILoot implements IModPlugin{

	public static final String LOOT = LootBags.MODID+".loot";
	//public static final String RECYCLER = LootBags.MODID+".recycler";

	public void register(@Nonnull IModRegistry registry)
	{	
		registry.handleRecipes(LootEntry.class, LootWrapper::new, LOOT);
		//registry.handleRecipes(RecyclerEntry.class, RecyclerWrapper::new, RECYCLER);
		
		registry.addRecipes(LootRegistry.getInstance().getLoot(), LOOT);
		//registry.addRecipes(RecyclerRegistry.getInstance().getItem(), RECYCLER);
	}
	
	@Override
	public void registerCategories(IRecipeCategoryRegistration registry)
	{
		final IJeiHelpers jeiHelpers = registry.getJeiHelpers();
		final IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		registry.addRecipeCategories(new LootCategory(guiHelper));
		//registry.addRecipeCategories(new RecyclerCategory(guiHelper));
	}
}
