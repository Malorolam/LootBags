package mal.lootbags.jei;

import javax.annotation.Nonnull;

import mal.lootbags.LootBags;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

@JEIPlugin
public class JEILoot extends BlankModPlugin{

	public static final String LOOT = LootBags.MODID+".loot";
	
	private static IJeiHelpers jeiHelpers;
	private static IJeiRuntime jeiRuntime;
	
	@Override
	public void register(@Nonnull IModRegistry registry)
	{	
		registry.handleRecipes(LootEntry.class, LootWrapper::new, LOOT);
		
		registry.addRecipes(LootRegistry.getInstance().getLoot(), LOOT);
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime)
	{
		JEILoot.jeiRuntime = runtime;
	}
	
	@Override
	public void registerCategories(IRecipeCategoryRegistration registry)
	{
		JEILoot.jeiHelpers = registry.getJeiHelpers();
		registry.addRecipeCategories(new LootCategory());
	}
	
	public static IJeiHelpers getJEIHelpers()
	{
		return jeiHelpers;
	}
}
