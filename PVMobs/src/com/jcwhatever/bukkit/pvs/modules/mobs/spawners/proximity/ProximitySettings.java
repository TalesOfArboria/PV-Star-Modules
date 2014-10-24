package com.jcwhatever.bukkit.pvs.modules.mobs.spawners.proximity;

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.storage.settings.ISettingsManager;
import com.jcwhatever.bukkit.generic.storage.settings.SettingDefinitions;
import com.jcwhatever.bukkit.generic.storage.settings.SettingsManager;
import com.jcwhatever.bukkit.generic.storage.settings.ValueType;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.modules.mobs.spawners.ISpawnerSettings;
import com.jcwhatever.bukkit.pvs.modules.mobs.MobArenaExtension;

public class ProximitySettings implements ISpawnerSettings {

    private static SettingDefinitions _settings;

    static {
        _settings = new SettingDefinitions();

        _settings
                .set("max-per-spawn", 2, ValueType.INTEGER, "Maximum mobs per spawnpoint per spawn cycle.")
                .set("max-mobs-per-player", 4, ValueType.INTEGER, "Maximum mobs spawned per player.")
                .set("max-path-distance", 18, ValueType.INTEGER, "Maximum mob path distance when detecting proximity.")
                .set("max-distance", 24, ValueType.INTEGER, "Maximum distance when detecting proximity.")
        ;
    }

    private int _maxMobsPerSpawn;
    private int _maxMobsPerPlayer;
    private int _maxMobPathDistance; // max distance of a valid mob path
    private int _maxMobDistance;
    private int _maxMobDistanceSquared; // max distance when getting closest mob (squared)

    private final MobArenaExtension _manager;
    private final ProximitySpawner _spawner;
    private final Arena _arena;
    private final IDataNode _dataNode;
    private final SettingsManager _settingsManager;


    ProximitySettings(ProximitySpawner spawner) {
        _spawner = spawner;
        _manager = spawner.getManager();
        _arena = spawner.getManager().getArena();
        _dataNode = spawner.getManager().getDataNode().getNode("spawners.proximity");
        _settingsManager = new SettingsManager(_dataNode, _settings);

        _settingsManager.addOnSettingsChanged(new Runnable() {
            @Override
            public void run() {
                _maxMobsPerSpawn = _settingsManager.get("max-per-spawn");
                _maxMobsPerPlayer = _settingsManager.get("max-mobs-per-player");
                _maxMobPathDistance = _settingsManager.get("max-path-distance");
                _maxMobDistance = _settingsManager.get("max-distance");
                _maxMobDistanceSquared = _maxMobDistance * _maxMobDistance;
            }
        }, true);
    }


    @Override
    public SettingDefinitions getDefinitions() {
        return _settings;
    }

    @Override
    public ISettingsManager getManager() {
        return _settingsManager;
    }

    public int getMaxMobsPerSpawn() {
        return _maxMobsPerSpawn;
    }


    public void setMaxMobsPerSpawn(int value) {
        _settingsManager.set("max-per-spawn", value);
    }


    public int getMaxMobsPerPlayer() {
        return _maxMobsPerPlayer;
    }


    public void setMaxMobsPerPlayer(int value) {
        _settingsManager.set("max-mobs-per-player", value);
    }

    public int getMaxPathDistance() {
        return _maxMobPathDistance;
    }

    public void setMaxMobPathDistance(int value) {
        _settingsManager.set("max-path-distance", value);
    }

    public int getMaxDistance() {
        return _maxMobDistance;
    }

    public void setMaxDistance(int value) {
        _settingsManager.set("max-distance", value);
    }

    public int getMaxMobDistanceSquared() {
        return _maxMobDistanceSquared;
    }
}