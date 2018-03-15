package betterwithmods.module.gameplay;

import betterwithmods.common.BWOreDictionary;
import betterwithmods.common.BWRegistry;
import betterwithmods.module.Feature;
import betterwithmods.util.StackIngredient;
import com.google.common.collect.Sets;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import java.util.Set;

/**
 * Created by primetoxinz on 4/21/17.
 */
public class NuggetCompression extends Feature {

    @Override
    public String getFeatureDescription() {
        return "Adds recipes to the Crucible to compact 9 Nuggets into it's corresponding Ingot.";
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        Set<String> exclude = Sets.newHashSet("diamond", "soulforgedsteel");
        for (BWOreDictionary.Ore ingot : BWOreDictionary.ingotNames) {
            String suffix = ingot.getSuffix();
            if (!ingot.getOres().isEmpty() && suffix != null && !exclude.contains(suffix.toLowerCase())) {
                StackIngredient nugget = StackIngredient.fromOre(9, "nugget" + suffix);
                BWRegistry.CRUCIBLE.addStokedRecipe(nugget, ingot.getOres().get(0));
            }

        }
    }
}
