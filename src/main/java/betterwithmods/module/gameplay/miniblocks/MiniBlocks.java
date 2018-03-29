package betterwithmods.module.gameplay.miniblocks;

import betterwithmods.BWMod;
import betterwithmods.client.model.render.RenderUtils;
import betterwithmods.common.BWMBlocks;
import betterwithmods.module.Feature;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

public class MiniBlocks extends Feature {

    public static final Set<ItemStack> MATERIALS = Sets.newHashSet();
    public static Block SIDING = new BlockSiding().setRegistryName("siding");

    public MiniBlocks() {
        enabledByDefault = false;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        BWMBlocks.registerBlock(SIDING, new ItemMini(SIDING));
        GameRegistry.registerTileEntity(TileSiding.class, "bwm.siding");
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        MATERIALS.addAll(Lists.newArrayList(new ItemStack(Blocks.PLANKS), new ItemStack(Blocks.PLANKS, 1, 1), new ItemStack(Blocks.PLANKS, 1, 2), new ItemStack(Blocks.PLANKS, 1, 3)));
    }

    @Override
    public boolean hasSubscriptions() {
        return true;
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPostBake(ModelBakeEvent event) {
        MiniModel.SIDING = new MiniModel(RenderUtils.getModel(new ResourceLocation(BWMod.MODID, "block/mini/siding")));
        event.getModelRegistry().putObject(new ModelResourceLocation(BWMod.MODID + ":siding", "normal"), MiniModel.SIDING);
        event.getModelRegistry().putObject(new ModelResourceLocation(BWMod.MODID + ":siding", "inventory"), MiniModel.SIDING);
    }
}
