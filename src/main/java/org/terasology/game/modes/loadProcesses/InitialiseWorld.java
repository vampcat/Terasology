/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.game.modes.loadProcesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.controllers.LocalPlayerSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.PathManager;
import org.terasology.physics.BulletPhysics;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldBiomeProviderImpl;
import org.terasology.world.WorldInfo;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkStore;
import org.terasology.world.chunks.localChunkProvider.LocalChunkProvider;
import org.terasology.world.chunks.store.ChunkStoreGZip;
import org.terasology.world.generator.core.ChunkGeneratorManager;
import org.terasology.world.generator.core.ChunkGeneratorManagerImpl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Immortius
 */
public class InitialiseWorld implements LoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(InitialiseWorld.class);

    private WorldInfo worldInfo;

    public InitialiseWorld(WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
    }

    @Override
    public String getMessage() {
        return "Initializing world...";
    }

    @Override
    public boolean step() {
        if (worldInfo.getSeed() == null || worldInfo.getSeed().isEmpty()) {
            FastRandom random = new FastRandom();
            worldInfo.setSeed(random.randomCharacterString(16));
        }

        logger.info("World seed: \"{}\"", worldInfo.getSeed());

        // TODO: Separate WorldRenderer from world handling in general
        // Init ChunkGeneratorManager
        ChunkGeneratorManager chunkGeneratorManager = ChunkGeneratorManagerImpl.buildChunkGenerator(Arrays.asList(worldInfo.getChunkGenerators()));
        chunkGeneratorManager.setWorldSeed(worldInfo.getSeed());
        chunkGeneratorManager.setWorldBiomeProvider(new WorldBiomeProviderImpl(worldInfo.getSeed()));

        ChunkStore chunkStore = null;
        File f = new File(PathManager.getInstance().getWorldSavePath(worldInfo.getTitle()), worldInfo.getTitle() + ".dat");
        if (f.exists()) {
            try {
                chunkStore = ChunkStoreGZip.load(f);
            } catch (IOException e) {
                /* TODO: We really should expose this error via UI so player knows that there is an issue with their world
                   (don't have the game continue or we risk overwriting their game)
                 */
                logger.error("Failed to load chunk store", e);
            }
        }
        if (chunkStore == null) {
            chunkStore = new ChunkStoreGZip();
        }

        // Init. a new world
        WorldRenderer worldRenderer = new WorldRenderer(worldInfo, new LocalChunkProvider(chunkStore, chunkGeneratorManager), CoreRegistry.get(LocalPlayerSystem.class));
        CoreRegistry.put(WorldRenderer.class, worldRenderer);
        CoreRegistry.put(WorldProvider.class, worldRenderer.getWorldProvider());

        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        CoreRegistry.put(LocalPlayer.class, new LocalPlayer());
        CoreRegistry.put(Camera.class, worldRenderer.getActiveCamera());
        CoreRegistry.put(BulletPhysics.class, worldRenderer.getBulletRenderer());

        // TODO: This may be the wrong place, or we should change time handling so that it deals better with time not passing
        CoreRegistry.get(WorldProvider.class).setTime(worldInfo.getTime());
        return true;
    }

    @Override
    public int begin() {
        return 1;
    }


}