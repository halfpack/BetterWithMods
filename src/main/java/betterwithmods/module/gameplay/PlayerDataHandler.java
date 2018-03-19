package betterwithmods.module.gameplay;

import betterwithmods.BWMod;
import betterwithmods.module.Feature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerDataHandler extends Feature {
    public PlayerDataHandler() {
        canDisable = false;
    }

    @Override
    public boolean hasSubscriptions() {
        return true;
    }

    private static final ResourceLocation PLAYER_INFO = new ResourceLocation(BWMod.MODID, "player_info");

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer && !event.getCapabilities().containsKey(PLAYER_INFO)) {
            event.addCapability(PLAYER_INFO, new PlayerInfo());
        }
    }

    public static PlayerInfo getPlayerInfo(EntityPlayer player) {
        if (player.hasCapability(CAP_PLAYER_INFO, null)) {
            return player.getCapability(CAP_PLAYER_INFO, null);
        }
        return null;
    }

    @SuppressWarnings("CanBeFinal")
    @CapabilityInject(PlayerInfo.class)
    public static Capability<PlayerInfo> CAP_PLAYER_INFO = null;

    public static class CapabilityPlayerInfo implements Capability.IStorage<PlayerInfo> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<PlayerInfo> capability, PlayerInfo instance, EnumFacing side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<PlayerInfo> capability, PlayerInfo instance, EnumFacing side, NBTBase nbt) {
            instance.deserializeNBT((NBTTagCompound) nbt);
        }
    }

    public static class PlayerInfo implements ICapabilitySerializable<NBTTagCompound> {
        public boolean givenManual;

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CAP_PLAYER_INFO;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == CAP_PLAYER_INFO)
                return CAP_PLAYER_INFO.cast(this);
            return null;

        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("givenManual", givenManual);
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            givenManual = nbt.getBoolean("givenManual");
        }
    }
}
