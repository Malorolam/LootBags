package mal.lootbags.jei;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class LootHandler implements IRecipeHandler<LootEntry>{

	@Override
	public String getRecipeCategoryUid() {
		return JEILoot.LOOT;
	}

	@Override
	public String getRecipeCategoryUid(LootEntry arg0) {
		return JEILoot.LOOT;
	}

	@Override
	public Class<LootEntry> getRecipeClass() {
		return LootEntry.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(LootEntry recipe) {
		return new LootWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(LootEntry recipe) {
		return true;
	}

}
