/* This file is part of PV-Star Modules: PVParty for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.jcwhatever.bukkit.pvs.modules.party.commands;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.modules.party.Party;
import com.jcwhatever.bukkit.pvs.modules.party.PartyManager;
import com.jcwhatever.bukkit.pvs.modules.party.PartyModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

@ICommandInfo(
		parent="party", 
		command="new",
		usage="/pv party new", 
		description="Create a new party.",
		permissionDefault=PermissionDefault.TRUE)

public class NewSubCommand extends AbstractCommand {
	
	@Override
    public void execute(CommandSender sender, CommandArguments args)
            throws InvalidValueException, InvalidCommandSenderException {
        
	    InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER);
		
		Player p = (Player)sender;

        PartyManager manager = PartyModule.getModule().getManager();
		
		if (manager.isInParty(p)) {
			Party current = manager.getParty(p);
			tellError(p, "You can't create a new party until you leave the one you're in. You're currently in {0}.", current.getPartyName());
			return; // finish
		}
		
		Party party = manager.getParty(p); // creates new party with player as leader
		
		tellSuccess(p, "{0} created. Type '/pv party invite ?' to find out how to invite players to your party.", party.getPartyName());
    }

    
}



