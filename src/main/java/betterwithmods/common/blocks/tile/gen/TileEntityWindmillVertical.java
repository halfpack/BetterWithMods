package betterwithmods.common.blocks.tile.gen;

import betterwithmods.common.BWMBlocks;
import betterwithmods.common.BWMItems;
import betterwithmods.common.blocks.mechanical.BlockAxle;
import betterwithmods.common.blocks.mechanical.BlockWindmill;
import betterwithmods.util.InvUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//TODO almost need to completely rewrite these in terms of capabilities.
public class TileEntityWindmillVertical extends TileAxleGenerator implements IColor {
    public int[] bladeMeta = {0, 0, 0, 0, 0, 0, 0, 0};

    public int getBladeColor(int blade) {
        return bladeMeta[blade];
    }

    @Override
    public int getMinimumInput(EnumFacing facing) {
        return 0;
    }

    @Override
    public int getColorFromBlade(int blade) {
        return bladeMeta[blade];
    }

    @Override
    public boolean dyeBlade(int dyeColor) {
        boolean dyed = false;
        if (bladeMeta[dyeIndex] != dyeColor) {
            bladeMeta[dyeIndex] = dyeColor;
            dyed = true;
            IBlockState state = getWorld().getBlockState(this.pos);
            this.getWorld().notifyBlockUpdate(this.pos, state, state, 3);
            this.markDirty();
        }
        dyeIndex++;
        if (dyeIndex > 7)
            dyeIndex = 0;
        return dyed;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        for (int i = 0; i < 8; i++) {
            if (tag.hasKey("Color_" + i))
                bladeMeta[i] = tag.getInteger("Color_" + i);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagCompound t = super.writeToNBT(tag);
        for (int i = 0; i < 8; i++) {
            t.setInteger("Color_" + i, bladeMeta[i]);
        }
        t.setByte("DyeIndex", this.dyeIndex);
        return t;
    }

    public boolean isSlaveValid(int offset) {
        boolean notBlocked = true;
        int airCounter = 0;
        for (int x = -4; x < 5; x++) {
            for (int z = -4; z < 5; z++) {
                BlockPos offPos = pos.add(x, offset, z);
                if (x == 0 && z == 0) {
                }
                if (getWorld().provider.getDimensionType() == DimensionType.NETHER)
                    notBlocked = this.getWorld().isAirBlock(offPos);
                else if (getWorld().provider.getDimensionType() != DimensionType.NETHER) {
                    notBlocked = this.getWorld().isAirBlock(offPos);
                    if (getWorld().canBlockSeeSky(offPos))
                        airCounter++;
                }
                if (!notBlocked)
                    break;
            }
            if (!notBlocked)
                break;
        }
        if (getWorld().provider.getDimensionType() != DimensionType.NETHER)
            return notBlocked && airCounter > 25;
        return notBlocked;
    }

    @Override
    public void verifyIntegrity() {
        //check master's validity
        boolean valid = true;
        if (getWorld().getBlockState(pos).getBlock() != null && getWorld().getBlockState(pos).getBlock() == BWMBlocks.WINDMILL) {
            for (int i = -3; i < 4; i++) {
                if (i == 0)
                    continue;
                valid = isSlaveValid(i);
                if (!valid)
                    break;
            }
        }
        isValid = valid;
    }

    private void invalidateWindmill() {
        this.getWorld().setBlockState(this.pos, BWMBlocks.WINDMILL.getDefaultState());
        for (int i = -3; i < 4; i++) {
            BlockPos pos = this.pos.add(0, i, 0);
            if (getWorld().getBlockState(pos).getBlock() instanceof BlockAxle)
                this.getWorld().setBlockState(pos, BWMBlocks.WOODEN_AXLE.getDefaultState());
        }
        if (!this.getWorld().isRemote)
            InvUtils.ejectStackWithOffset(getWorld(), pos, new ItemStack(BWMItems.AXLE_GENERATOR, 1, 2));
        this.getWorld().setBlockState(this.pos, BWMBlocks.WOODEN_AXLE.getDefaultState());
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager mgr, SPacketUpdateTileEntity pkt) {
        NBTTagCompound tag = pkt.getNbtCompound();
        this.readFromNBT(tag);
        IBlockState state = getWorld().getBlockState(this.pos);
        this.getWorld().notifyBlockUpdate(this.pos, state, state, 3);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void calculatePower() {
//        byte speed = 0;
//        if (this.isValid() && !isGalacticraftDimension() && isNotOtherDimension()) {
//            if ((this.getWorld().isRaining() || this.getWorld().isThundering()) && this.getWorld().provider.getDimensionType() != DimensionType.NETHER)
//                speed = 2;
//            else
//                speed = 1;
//        }
//        if (speed != this.runningState && getWorld().getBlockState(pos).getBlock() instanceof BlockWindmill) {
//            this.setRunningState(speed);
//            this.getWorld().setBlockState(pos, this.getWorld().getBlockState(pos));
//            getWorld().scheduleBlockUpdate(pos, this.getBlockType(), this.getBlockType().tickRate(getWorld()), 5);//this.getWorld().markBlockForUpdate(pos);
//        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (getWorld().getBlockState(pos).getBlock() != null && getWorld().getBlockState(pos).getBlock() instanceof BlockWindmill)
            return new AxisAlignedBB(x - 4, y - 4, z - 4, x + 4, y + 4, z + 4);
        else
            return super.getRenderBoundingBox();
    }
}
