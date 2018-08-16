/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.rendering.assets.vampShader;

import com.google.api.client.util.Charsets;
import com.google.common.io.CharStreams;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.exceptions.InvalidAssetFilenameException;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.format.AssetFileFormat;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.naming.Name;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.PathMatcher;
import java.util.List;

@RegisterAssetFileFormat
public class VampGLSLShaderFormat implements AssetFileFormat<VampShaderData> {
    private static final String FRAGMENT_SUFFIX = "_frag.glsl";
    private static final String VERTEX_SUFFIX = "_vert.glsl";

    @Override
    public PathMatcher getFileMatcher() {
        return path -> {
            String name = path.getFileName().toString();
            return name.endsWith(FRAGMENT_SUFFIX) || name.endsWith(VERTEX_SUFFIX);
        };
    }

    @Override
    public Name getAssetName(String filename) throws InvalidAssetFilenameException {
        // This only works because FRAGMENT_SUFFIX.length() == VERTEX_SUFFIX.length()
        return new Name(filename.substring(0, filename.length() - FRAGMENT_SUFFIX.length()));
    }

    @Override
    public VampShaderData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        String vertProgram = null;
        String fragProgram = null;

        for (AssetDataFile input : inputs) {
            if (input.getFilename().endsWith(VERTEX_SUFFIX)) {
                vertProgram = readInput(input);
            } else if (input.getFilename().endsWith(FRAGMENT_SUFFIX)) {
                fragProgram = readInput(input);
            }
        }

        if (vertProgram != null && fragProgram != null) {
            return new VampShaderData(vertProgram, fragProgram);
        }

        throw new IOException("Failed to load shader '" + urn + "' - missing vertex or fragment program");
    }

    private String readInput(AssetDataFile input) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(input.openStream(), Charsets.UTF_8)) {
            return CharStreams.toString(reader);
        }
    }
}