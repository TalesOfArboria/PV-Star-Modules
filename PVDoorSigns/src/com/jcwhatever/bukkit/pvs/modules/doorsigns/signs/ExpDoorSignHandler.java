package com.jcwhatever.bukkit.pvs.modules.doorsigns.signs;

import com.jcwhatever.bukkit.generic.signs.SignContainer;
import com.jcwhatever.bukkit.generic.utils.TextUtils;
import com.jcwhatever.bukkit.generic.utils.TextUtils.TextColor;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.plugin.Plugin;

import java.util.regex.Matcher;

public class ExpDoorSignHandler extends AbstractNumberSignHandler {

    @Override
    public Plugin getPlugin() {
        return PVStarAPI.getPlugin();
    }

    @Override
    public String getName() {
        return "Exp_Door";
    }

    @Override
    public String getDescription() {
        return "Opens doors using player Exp as currency.";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "Exp Door",
                "--anything--",
                "*<cost>*",
                "--anything--"
        };
    }

    @Override
    public String getHeaderPrefix() {
        return TextColor.DARK_BLUE.toString();
    }

    @Override
    protected double getCost(SignContainer sign) {
        int cost;

        Matcher matcher = TextUtils.PATTERN_NUMBERS.matcher(sign.getRawLine(2));

        if (!matcher.find()) {
            Msg.warning("No cost could be found on line 3 of Exp Door sign.");
            return -1;
        }

        String rawNumber = matcher.group();

        try {
            cost = Integer.parseInt(rawNumber);
        }
        catch (NumberFormatException exc) {
            Msg.warning("Failed to parse cost from Exp Door sign.");
            return -1;
        }

        return cost;
    }

    @Override
    protected double getPlayerBalance(ArenaPlayer player) {
        return player.getHandle().getLevel();
    }

    @Override
    protected void incrementPlayerBalance(ArenaPlayer player, double amount) {

        int level = player.getHandle().getLevel();

        player.getHandle().setLevel(0);
        player.getHandle().setLevel(level + (int)amount);
    }

    @Override
    protected String getCurrencyName() {
        return "Exp Levels";
    }

    @Override
    protected String getCurrencyNamePlural() {
        return "Exp Levels";
    }


}