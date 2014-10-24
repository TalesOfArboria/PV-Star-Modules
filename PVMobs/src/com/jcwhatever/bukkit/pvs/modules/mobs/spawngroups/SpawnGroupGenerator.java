package com.jcwhatever.bukkit.pvs.modules.mobs.spawngroups;

import com.jcwhatever.bukkit.generic.pathing.GroundPathCheck;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.TextUtils;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.bukkit.pvs.modules.mobs.MobArenaExtension;
import com.jcwhatever.bukkit.pvs.modules.mobs.paths.PathCache;
import com.jcwhatever.bukkit.pvs.modules.mobs.utils.DistanceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpawnGroupGenerator {

    private final Map<String, Spawnpoint> _mobSpawns;
    private final MobArenaExtension _manager;
    private final IDataNode _groupsNode;
    private final IDataNode _dataNode;
    private PathCache _pathCache;

    private List<Spawnpoint> _spawnGroups;
    private boolean _groupsLoaded;


    public SpawnGroupGenerator(MobArenaExtension manager, Collection<Spawnpoint> spawnpoints) {
        PreCon.notNull(manager);
        PreCon.notNull(spawnpoints);

        _manager = manager;
        _mobSpawns = new HashMap<>(spawnpoints.size());
        _dataNode = manager.getDataNode().getNode("spawn-groups");
        _groupsNode = manager.getDataNode().getNode("spawn-groups.groups");

        for (Spawnpoint spawn : spawnpoints) {
            _mobSpawns.put(spawn.getSearchName(), spawn);
        }

        loadSpawnGroups();
    }

    public PathCache getPathCache() {
        return _pathCache;
    }

    public List<Spawnpoint> getSpawnGroups() {
        if (_spawnGroups == null) {

            if (!_groupsLoaded && !loadSpawnGroups()) {
                new CreateSpawnGroups().run();
            }
            else {
                return new ArrayList<>(_mobSpawns.values());
            }
        }

        return new ArrayList<>(_spawnGroups);
    }

    public void clearGroupCache() {
        _groupsNode.clear();
        _groupsNode.saveAsync(null);
    }

    public void clearGroups() {
        _spawnGroups = null;
        clearGroupCache();
    }

    public void generateGroups() {
        new CreateSpawnGroups().run();
    }


    public boolean isSpawnsChanged() {

        Set<String> currentSpawnList = new HashSet<>(_mobSpawns.keySet());
        List<String> cachedSpawnList = _dataNode.getStringList("spawns", null);
        if (cachedSpawnList == null)
            return true;

        if (currentSpawnList.size() != cachedSpawnList.size())
            return true;

        for (String spawnName : cachedSpawnList) {
            if (!currentSpawnList.contains(spawnName))
                return true;
        }

        return false;
    }


    private boolean loadSpawnGroups() {

        if (!isSpawnsChanged())
            return false;

        Set<String> groupNames = _groupsNode.getSubNodeNames();
        if (groupNames == null || groupNames.isEmpty()) {
            _pathCache = new PathCache(_manager, new ArrayList<Spawnpoint>(0));
            return false;
        }

        List<Spawnpoint> groups = new ArrayList<>(groupNames.size());

        for (String groupName : groupNames) {

            String rawNames = _groupsNode.getString(groupName);
            if (rawNames == null)
                continue;

            String[] spawnNames = TextUtils.PATTERN_COMMA.split(rawNames.toLowerCase());

            SpawnGroup group = null;

            List<Spawnpoint> groupSpawns = new ArrayList<>(spawnNames.length);

            for (String spawnName : spawnNames) {

                Spawnpoint spawn = _mobSpawns.get(spawnName);
                if (spawn == null)
                    continue;

                if (spawnName.equalsIgnoreCase(groupName)) {
                    group = new SpawnGroup(_manager, spawn);
                }

                groupSpawns.add(spawn);
            }

            if (group != null) {
                group.addSpawns(groupSpawns);
                groups.add(group);
            }
        }

        _spawnGroups = groups;
        _groupsLoaded = true;

        _pathCache = new PathCache(_manager, groups);

        return true;
    }


    private void saveSpawnGroups() {

        _groupsNode.clear();

        List<Spawnpoint> groups = new ArrayList<>(_spawnGroups);

        for (Spawnpoint group : groups) {

            List<Spawnpoint> spawns = ((SpawnGroup)group).getSpawns();
            if (spawns.isEmpty())
                continue;

            List<String> spawnNames = new ArrayList<String>(spawns.size());

            for (Spawnpoint spawn : spawns) {
                spawnNames.add(spawn.getName());
            }

            _groupsNode.set(group.getName(), TextUtils.concat(spawnNames, ","));
        }

        // add spawn list to data node so changes to spawns can be detected
        List<String> currentSpawnList = new ArrayList<>(_mobSpawns.keySet());
        _pathCache = new PathCache(_manager, groups);
        _dataNode.set("spawns", currentSpawnList);
        _dataNode.saveAsync(null);
    }

    /**
     * Generate spawn groups
     */
    private class CreateSpawnGroups implements Runnable {

        @Override
        public void run() {

            List<Spawnpoint> groups = new ArrayList<>(_mobSpawns.size());

            GroundPathCheck pathChecker = new GroundPathCheck();
            pathChecker.setMaxDropHeight(DistanceUtils.MAX_DROP_HEIGHT);
            pathChecker.setMaxIterations(DistanceUtils.MAX_ITERATIONS);
            pathChecker.setMaxRange(DistanceUtils.SEARCH_RADIUS);

            LinkedList<Spawnpoint> spawnPool = new LinkedList<>(_mobSpawns.values());
            int searchRadiusSquared = DistanceUtils.SEARCH_RADIUS * DistanceUtils.SEARCH_RADIUS;

            while (!spawnPool.isEmpty()) {

                // get a spawn and make it the primary of a new spawn group
                Spawnpoint primary = spawnPool.remove();
                SpawnGroup group = new SpawnGroup(_manager, primary);

                // find candidates to add to the group
                LinkedList<Spawnpoint> groupCandidates = new LinkedList<>(spawnPool);

                while (!groupCandidates.isEmpty()) {

                    Spawnpoint candidate = groupCandidates.remove();

                    double distance = primary.distanceSquared(candidate);

                    if (distance <= searchRadiusSquared) {
                        int pathDistance = pathChecker.searchDistance(primary, candidate);

                        if (pathDistance > -1 && pathDistance <= DistanceUtils.SEARCH_RADIUS) {
                            group.addSpawn(candidate);

                            // remove candidate from spawn pool
                            spawnPool.remove(candidate);
                        }
                    }
                }

                groups.add(group);
            }


            _spawnGroups = groups;

            try {
                _pathCache.clearCachePaths();
            } catch (IOException e) {
                e.printStackTrace();
            }
            _pathCache.cachePaths(16, 18);

            saveSpawnGroups();
        }
    }

}