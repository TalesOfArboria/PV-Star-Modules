package com.jcwhatever.bukkit.pvs.modules.party.commands;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.player.PlayerHelper;
import com.jcwhatever.bukkit.generic.utils.TextUtils;
import com.jcwhatever.bukkit.pvs.modules.party.Party;
import com.jcwhatever.bukkit.pvs.modules.party.PartyManager;
import com.jcwhatever.bukkit.pvs.modules.party.PartyModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

@ICommandInfo(
		parent="party",
		command="join",
		staticParams={"playerName=$default"},
		usage="/pv party join <playerName>", 
		description="Join a party you've been invited to.",
		permissionDefault=PermissionDefault.TRUE)

public class JoinSubCommand extends AbstractCommand {

	@Override
    public void execute(CommandSender sender, CommandArguments args)
            throws InvalidValueException, InvalidCommandSenderException {
        
	    InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER);
		
		Player p = (Player)sender;

        PartyManager manager = PartyModule.getInstance().getManager();
		
		if (manager.isInParty(p)) {
			tellError(p, "You're already in a party. You must leave the party you're in before you can join another.");
			return; // finish
		}
		
		String playerName = args.getString("playerName");
		List<Party> invited = manager.getInvitedParties(p);
		
		if (invited.size() == 0) {
			tellError(p, "You haven't been invited to any parties.");
			return; // finish
		}
		
		if (playerName.equals("$default") && invited.size() > 1) {
			
			tellError(p, "You've been invited to multiple parties. Please specify which player party you want to join. Type '/pv party join ?' for help.");
			
			List<String> leaderNames = new ArrayList<String>(invited.size());
			for (Party party : invited) {
				leaderNames.add(party.getLeader().getName());
			}
			
			tell(p, "You've received invitations from: {0}", TextUtils.concat(leaderNames, ", "));			
			
			return; // finish
		}
		
		if (playerName.equals("$default") && invited.size() == 1) {
			Party party = invited.get(0);
			if (!manager.addPlayer(p, party)) {
				tellError(p, "Failed to join party.");
				return; // finish
			}
			party.tell("{0} has joined the party.", p.getName());
			return; // finish
		}
		
		
		
		
		Player leader = PlayerHelper.getPlayer(playerName);
		if (leader == null) {
			tellError(p, "Player '{0}' was not found.", playerName);
			return; // finish
		}
		
		if (!manager.isInParty(leader)) {
			tellError(p, "{0} is not the leader of a party.", leader.getName());
			return; // finish
		}
		
		Party party = manager.getParty(leader);
		
		if (!party.getLeader().equals(leader)) {
			tellError(p, "{0} is not the leader of {1}.", leader.getName(), party.getPartyName());
			return; // finish
		}
		
		if (!party.isInvited(p)) {
			tellError(p, "You're not invited to {0} or you're invitation has expired.", party.getPartyName());
			return; // finish
		}
				
		if (!manager.addPlayer(p, party)) {
			tellError(p, "Failed to join party.");
			return; // finish
		}
		
		party.tell("{0} has joined the party.", p.getName());
    }
}

