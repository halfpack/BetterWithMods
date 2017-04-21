package betterwithmods.common.registry;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class TurntableInteraction extends BlockMetaHandler {
    public static TurntableInteraction INSTANCE = new TurntableInteraction();

    private TurntableInteraction() {
        super("turntable");
    }

    public void addTurntableRecipe(ItemStack inputBlock, ItemStack outputBlock, ItemStack... scraps) {
        if (inputBlock != ItemStack.EMPTY && inputBlock.getItem() instanceof ItemBlock) {
            Block iBlock = ((ItemBlock) inputBlock.getItem()).getBlock();
            int iMeta = inputBlock.getMetadata();
            if (outputBlock == ItemStack.EMPTY)
                addTurntableRecipe(iBlock, iMeta, null, 0, scraps);
            else if (outputBlock.getItem() instanceof ItemBlock) {
                Block oBlock = ((ItemBlock) outputBlock.getItem()).getBlock();
                int oMeta = outputBlock.getMetadata();
                addTurntableRecipe(iBlock, iMeta, oBlock, oMeta, scraps);
            }
        }
    }

    public void addTurntableRecipe(Block block, int meta, Block result, int resultMeta, ItemStack... scraps) {
        addRecipe(new TurntableRecipe(block, meta, result, resultMeta, scraps == null ? Lists.newArrayList() : Arrays.asList(scraps)));
    }


}
