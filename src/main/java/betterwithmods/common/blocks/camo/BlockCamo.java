package betterwithmods.common.blocks.camo;

import betterwithmods.client.BWCreativeTabs;
import betterwithmods.client.baking.UnlistedPropertyGeneric;
import betterwithmods.common.blocks.BWMBlock;
import betterwithmods.module.gameplay.miniblocks.MiniBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public abstract class BlockCamo extends BWMBlock {

    public static final IUnlistedProperty<CamoInfo> CAMO_INFO = new UnlistedPropertyGeneric<>("camo", CamoInfo.class);
    private final Function<Material, Collection<IBlockState>> subtypes;

    public BlockCamo(Material material, Function<Material, Collection<IBlockState>> subtypes) {
        super(material);
        this.subtypes = subtypes;
        setCreativeTab(BWCreativeTabs.MINI_BLOCKS);
    }

    public Optional<? extends TileCamo> getTile(IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileCamo)
            return Optional.of((TileCamo) tile);
        return Optional.empty();
    }


    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        return getTile(worldIn, pos).map(t -> t.getState().getBlockHardness(worldIn, pos)).orElse(super.getBlockHardness(blockState, worldIn, pos));
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return getTile(world, pos).map(t -> t.getState().getBlock().getExplosionResistance(world, pos, exploder, explosion)).orElse(super.getExplosionResistance(world, pos, exploder, explosion));
    }


    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (IBlockState state : subtypes.apply(blockMaterial)) {
            items.add(MiniBlocks.fromParent(this, state));
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{CAMO_INFO});
    }

    @Nullable
    @Override
    public abstract TileEntity createTileEntity(World world, IBlockState state);

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extendedBS = (IExtendedBlockState) super.getExtendedState(state, world, pos);
        return getTile(world, pos).map(t -> fromTile(extendedBS, t)).orElse(extendedBS);
    }

    public IBlockState fromTile(IExtendedBlockState state, TileCamo tile) {
        return state.withProperty(CAMO_INFO, new CamoInfo(tile));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileCamo) {
            TileCamo mini = (TileCamo) tile;
            return mini.getPickBlock(null, null, state);
        }
        return new ItemStack(this);
    }


    @Override
    public final void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        getDrops(drops, world, pos, state, null, fortune, false);
    }


    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F);
        int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
        boolean silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;

        NonNullList<ItemStack> items = NonNullList.create();
        getDrops(items, worldIn, pos, state, te, 0, false);
        float chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, fortuneLevel, 1.0f, silkTouch, player);

        harvesters.set(player);

        if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) {
            for (ItemStack item : items)
                if (chance >= 1.0f || worldIn.rand.nextFloat() <= chance)
                    spawnAsEntity(worldIn, pos, item);
        }

        harvesters.set(null);
    }

    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, @Nullable TileEntity te, int fortune, boolean silkTouch) {
        if (te instanceof TileCamo) {
            drops.add(((TileCamo) te).getPickBlock(null, null, state));
        } else {
            super.getDrops(drops, world, pos, state, fortune);
        }
    }


    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) {
            NonNullList<ItemStack> drops = NonNullList.create();
            getDrops(drops, worldIn, pos, state, worldIn.getTileEntity(pos), fortune, false);
            chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, worldIn, pos, state, fortune, chance, false, harvesters.get());

            for (ItemStack drop : drops) {
                if (worldIn.rand.nextFloat() <= chance) {
                    spawnAsEntity(worldIn, pos, drop);
                }
            }
        }
    }


    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return getTile(world, pos).map(t -> t.getPickBlock(player, target, state)).orElse(new ItemStack(this));
    }


    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return getTile(world, pos).map(t -> t.getState().getBlock().getFireSpreadSpeed(world, pos, face)).orElse(5);
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return getTile(world, pos).map(t -> t.getState().getBlock().getFlammability(world, pos, face)).orElse(10);
    }

}