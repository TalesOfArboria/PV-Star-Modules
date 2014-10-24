package com.jcwhatever.bukkit.pvs.modules.notesigns.commands;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.signs.SignContainer;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

@ICommandInfo(
        parent="notes",
        command="clear",
        usage="/{plugin-command} {command} clear",
        description="Remove all notes in the selected arena.")

public class ClearSubCommand extends AbstractPVCommand {

    @Localizable static final String _SUCCESS = "{0} signs removed in arena '{1}'.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return; // finish

        int hideCount = 0;
        List<SignContainer> signs = PVStarAPI.getSignManager().getSigns("Note");
        if (signs != null && !signs.isEmpty()) {

            for (SignContainer sign : signs) {

                IDataNode signNode = sign.getDataNode();
                if (signNode == null)
                    continue;

                UUID arenaId = signNode.getUUID("arena-id");
                if (!arena.getId().equals(arenaId))
                    continue;

                if (sign.getSign() != null)
                    sign.getSign().getBlock().setType(Material.AIR);

                if (sign.getDataNode() != null) {
                    sign.getDataNode().remove();
                }

                hideCount++;
            }
        }

        PVStarAPI.getSignManager().getDataNode().saveAsync(null);

        tellSuccess(sender, Lang.get(_SUCCESS, hideCount, arena.getName()));
    }
}