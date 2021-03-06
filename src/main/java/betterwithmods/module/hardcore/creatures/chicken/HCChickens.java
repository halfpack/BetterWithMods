package betterwithmods.module.hardcore.creatures.chicken;

import betterwithmods.BWMod;
import betterwithmods.common.BWMBlocks;
import betterwithmods.common.entity.ai.AIFoodEggLayer;
import betterwithmods.module.Feature;
import betterwithmods.util.InvUtils;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Set;

import static betterwithmods.module.hardcore.creatures.chicken.EggLayer.EGG_LAYER_CAP;

/**
 * Created by primetoxinz on 5/13/17.
 */
public class HCChickens extends Feature {

    public static final ResourceLocation EGG_LAYER = new ResourceLocation(BWMod.MODID, "egglayer");
    public static Set<ItemStack> SEEDS;

    @Override
    public void setupConfig() {
    }

    @Override
    public String getFeatureDescription() {
        return "Rework chicken breeding. Chickens don't breed in pairs. You feed a single chicken 1 seed, and it craps out an egg that can be thrown. The egg either makes a chicken, or drops raw egg.";
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        CapabilityManager.INSTANCE.register(EggLayer.class, new EggLayer.CapabilityEggLayer(), EggLayer::new);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        SEEDS = Sets.newHashSet(new ItemStack(BWMBlocks.HEMP), new ItemStack(Items.WHEAT_SEEDS), new ItemStack(Items.MELON_SEEDS), new ItemStack(Items.PUMPKIN_SEEDS), new ItemStack(Items.BEETROOT_SEEDS));
    }

    @SubscribeEvent
    public void onAttachCap(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityChicken) {
            event.addCapability(EGG_LAYER, new EggLayer(new ItemStack(Items.EGG), SEEDS));
        }
    }

    @SubscribeEvent
    public void addEntityAI(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityAnimal && event.getEntity().hasCapability(EGG_LAYER_CAP, EnumFacing.DOWN)) {
            EntityAnimal animal = (EntityAnimal) event.getEntity();
            animal.tasks.addTask(3, new AIFoodEggLayer(animal));
        }
    }

    @SubscribeEvent
    public void onEntityTick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entityLiving = event.getEntityLiving();
        if (entityLiving.world.isRemote)
            return;
        if (entityLiving instanceof EntityChicken) {
            EntityChicken chicken = (EntityChicken) entityLiving;
            //Stops vanilla egg dropping mechanic
            chicken.timeUntilNextEgg = 6000000;
        }
        if (entityLiving.hasCapability(EGG_LAYER_CAP, EnumFacing.DOWN)) {
            EggLayer layer = entityLiving.getCapability(EGG_LAYER_CAP, EnumFacing.DOWN);
            if (layer != null) {
                if(layer.isFeed()) {
                    layer.setTicks(layer.getTicks() - 1);
                    if(layer.canLayEgg()) {
                        layer.lay(entityLiving);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (SEEDS.stream().anyMatch(s -> InvUtils.matches(s, event.getItemStack())) && event.getTarget() instanceof EntityLiving && event.getTarget().hasCapability(EGG_LAYER_CAP, EnumFacing.DOWN)) {
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);
            EggLayer layer = event.getTarget().getCapability(EGG_LAYER_CAP, EnumFacing.DOWN);
            if (layer != null) {
                layer.feed((EntityLiving) event.getTarget(), event.getItemStack());
            }
        }
    }

    @Override
    public boolean hasSubscriptions() {
        return true;
    }

}
