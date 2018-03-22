package betterwithmods.common.blocks;

import betterwithmods.client.BWCreativeTabs;
import betterwithmods.common.blocks.mini.BlockMini;
import betterwithmods.common.blocks.tile.TileBasic;
import betterwithmods.util.InvUtils;
import betterwithmods.util.item.ToolsManager;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public abstract class BWMBlock extends Block {

    private Class<? extends TileEntity> tileClass;

    public BWMBlock(Material material) {
        super(material);
        setCreativeTab(BWCreativeTabs.BWTAB);
        if (material == Material.WOOD || material == BlockMini.MINI) {
            ToolsManager.setAxesAsEffectiveAgainst(this);
            this.setSoundType(SoundType.WOOD);
            this.setHarvestLevel("axe", 0);
        } else if (material == Material.ROCK) {
            this.setSoundType(SoundType.STONE);
            setHarvestLevel("pickaxe", 1);
            ToolsManager.setPickaxesAsEffectiveAgainst(this);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote && worldIn.getTileEntity(pos) instanceof TileBasic) {
            ((TileBasic) worldIn.getTileEntity(pos)).onBreak();
            worldIn.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        onBlockPlacedBy(world, pos, state, placer, stack, null, 0.5f, 0.5f, 0.5f);
    }

    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, @Nullable EnumFacing face, float hitX, float hitY, float hitZ) {
        if (hasTileEntity(state)) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileBasic) {
                ((TileBasic) tile).onPlacedBy(placer, face, stack, hitX, hitY, hitZ);
            }
        }
    }

    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        if (hasTileEntity(blockState)) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)) {
                if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)) {
                    return InvUtils.calculateComparatorLevel(tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP));
                }
            }
        }
        return 0;
    }

    public String getVariant() {
        return "inventory";
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return tileClass != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        try {
            return tileClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BWMBlock setTileClass(Class<? extends TileEntity> tileClass) {
        this.tileClass = tileClass;
        return this;
    }


}
