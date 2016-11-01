package mal.lootbags.jei;

import java.lang.ref.Reference;

import javax.annotation.Nonnull;

import mal.lootbags.LootBags;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
public class JEILoot extends BlankModPlugin{

	public static final String LOOT = LootBags.MODID+".loot";
	
	private static IJeiHelpers jeiHelpers;
	
	@Override
	public void register(@Nonnull IModRegistry registry)
	{
		JEILoot.jeiHelpers = registry.getJeiHelpers();
		registry.addRecipeHandlers(new LootHandler());
		registry.addRecipeCategories(new LootCategory(JEILoot.jeiHelpers.getGuiHelper()));
		
		registry.addRecipes(LootRegistry.getInstance().getLoot());
	}
}
