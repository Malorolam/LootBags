package mal.lootbags.jei;

import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;

public class LootWrapperFactory implements IRecipeWrapperFactory<LootEntry> {

	@Override
	public IRecipeWrapper getRecipeWrapper(LootEntry recipe) {
		return new LootWrapper(recipe);
	}

}
