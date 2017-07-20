package betterwithmods.module.industry.pollution;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

public class CommandCheckPollution extends CommandBase {
    @Override
    public String getName() {
        return "checkPollution";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "bwm.checkPollution.usage";
    }

    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        World world = server.getEntityWorld();
        ChunkPos pos = new ChunkPos(sender.getPosition());
        if (world.getChunkFromChunkCoords(pos.x, pos.z).isLoaded()) {
            List<String> information = new ArrayList<>();
            providePollutionInformation(information, pos);
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    if (x * x != z * z) {
                        Chunk chunk = world.getChunkFromChunkCoords(pos.x + x, pos.z + z);
                        if (chunk.isLoaded()) {
                            providePollutionInformation(information, chunk.getPos());
                        }
                    }
                }
            }
            if (!information.isEmpty())
                information.forEach(s -> sender.sendMessage(new TextComponentString(s)));
            else sender.sendMessage(new TextComponentString("Pollution data for chunk [" + pos.x + ", " + pos.z + "] somehow empty."));
        }
    }

    private void providePollutionInformation(List<String> addTo, ChunkPos from) {
        float pollution = Pollution.handler.getPollutionStat(from);
        if (pollution > -1) {
            String info = "Chunk [" + from.x + ", " + from.z + "] pollution: " + String.format("%.2f", pollution);
            addTo.add(info);
        }
    }
}
