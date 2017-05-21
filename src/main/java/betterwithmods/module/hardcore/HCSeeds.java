package betterwithmods.module.hardcore;

import betterwithmods.common.BWMBlocks;
import betterwithmods.module.Feature;
import betterwithmods.util.InvUtils;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

/**
 * Created by tyler on 5/21/17.
 */
public class HCSeeds extends Feature {

    @Override
    public String getFeatureDescription() {
        return "Requires Tilling the ground with a hoe to get seeds.";
    }

    @SubscribeEvent
    public void getDrops(BlockEvent.HarvestDropsEvent event) {
        World world = event.getWorld();
        IBlockState state = world.getBlockState(event.getPos());
        //todo need to check is shearing?
        if (state.getBlock() instanceof BlockTallGrass) {
            event.getDrops().clear();
        }
    }

    private static final Random RANDOM = new Random();

    public NonNullList<ItemStack> getDrops(int fortune) {
        if (RANDOM.nextInt(8) != 0) return NonNullList.create();
        ItemStack seed = net.minecraftforge.common.ForgeHooks.getGrassSeed(RANDOM, 0);
        if(seed.isItemEqual(new ItemStack(Items.WHEAT_SEEDS)) || seed.isEmpty()) {
            return NonNullList.create();
        } else {
            return NonNullList.withSize(1, seed);
        }
    }

    @SubscribeEvent
    public void onHoe(UseHoeEvent e) {
        World world = e.getWorld();
        if (!world.isRemote) {
            BlockPos pos = e.getPos();
            if (world.isAirBlock(pos.up())) {
                IBlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof BlockGrass) {
                    InvUtils.ejectStackWithOffset(world, pos.up(), getDrops(0));
                }
            }
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        MinecraftForge.addGrassSeed(new ItemStack(BWMBlocks.HEMP, 1), 5);
    }

    @Override
    public void disabledInit(FMLInitializationEvent event) {
        MinecraftForge.addGrassSeed(new ItemStack(BWMBlocks.HEMP, 1), 5);
    }

    @Override
    public boolean hasSubscriptions() {
        return true;
    }
}