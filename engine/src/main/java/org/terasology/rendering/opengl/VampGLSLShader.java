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
package org.terasology.rendering.opengl;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.GameThread;
import org.terasology.rendering.assets.vampShader.VampShader;
import org.terasology.rendering.assets.vampShader.VampShaderData;

/**
 * GLSL Shader Program Instance class.
 * <p>
 * Provides actual shader compilation and manipulation support.
 * </p>
 */
public class VampGLSLShader extends VampShader {
    private static final Logger logger = LoggerFactory.getLogger(GLSLShader.class);

//    private static String includedFunctionsVertex = "";
//    private static String includedFunctionsFragment = "";
//    private static String includedDefines = "";
//    private static String includedUniforms = "";
//
//    static {
//        try (
//                InputStream vertStream = GLSLShader.class.getClassLoader().getResourceAsStream("org/terasology/include/globalFunctionsVertIncl.glsl");
//                InputStream fragStream = GLSLShader.class.getClassLoader().getResourceAsStream("org/terasology/include/globalFunctionsFragIncl.glsl");
//                InputStream uniformsStream = GLSLShader.class.getClassLoader().getResourceAsStream("org/terasology/include/globalUniformsIncl.glsl");
//                InputStream definesStream = GLSLShader.class.getClassLoader().getResourceAsStream("org/terasology/include/globalDefinesIncl.glsl")
//        ) {
//            includedFunctionsVertex = CharStreams.toString(new InputStreamReader(vertStream, Charsets.UTF_8));
//            includedFunctionsFragment = CharStreams.toString(new InputStreamReader(fragStream, Charsets.UTF_8));
//            includedDefines = CharStreams.toString(new InputStreamReader(definesStream, Charsets.UTF_8));
//            includedUniforms = CharStreams.toString(new InputStreamReader(uniformsStream, Charsets.UTF_8));
//        } catch (IOException e) {
//            logger.error("Failed to load Include shader resources");
//        }
//    }

    private VampShaderData shaderData;
    private DisposalAction disposalAction;

    public VampGLSLShader(ResourceUrn urn, AssetType<?, VampShaderData> assetType, VampShaderData data) {
        super(urn, assetType);
        disposalAction = new DisposalAction(urn);
        getDisposalHook().setDisposeAction(disposalAction);
        reload(data);
    }

    int linkShaderProgram(int featureHash) {
        int shaderProgram = GL20.glCreateProgram();

        GL20.glAttachShader(shaderProgram, disposalAction.fragmentPrograms.get(featureHash));
        GL20.glAttachShader(shaderProgram, disposalAction.vertexPrograms.get(featureHash));
        GL20.glLinkProgram(shaderProgram);
        GL20.glValidateProgram(shaderProgram);

        return shaderProgram;
    }

    @Override
    public void recompile() {
        // TODO: Complete.
    }

    private int compileShader(int type) {
        int shaderId = GL20.glCreateShader(type);

        String shader = assembleShader(type, features);

        if (config.getRendering().isDumpShaders()) {
            dumpCode(type, features, shader);
        }

        GL20.glShaderSource(shaderId, shader);
        GL20.glCompileShader(shaderId);

        return shaderId;
    }

//    private StringBuilder createShaderBuilder() {
//        String preProcessorPreamble = "#version 120\n";
//
//        // TODO: Implement a system for this - this has gotten way out of hand.
//        WorldAtlas worldAtlas = CoreRegistry.get(WorldAtlas.class);
//        if (worldAtlas != null) {
//            preProcessorPreamble += "#define TEXTURE_OFFSET " + worldAtlas.getRelativeTileSize() + "\n";
//        } else {
//            preProcessorPreamble += "#define TEXTURE_OFFSET 0.06125\n";
//        }
//        RenderingConfig renderConfig = config.getRendering();
//
//        preProcessorPreamble += "#define BLOCK_LIGHT_POW " + WorldRenderer.BLOCK_LIGHT_POW + "\n";
//        preProcessorPreamble += "#define BLOCK_LIGHT_SUN_POW " + WorldRenderer.BLOCK_LIGHT_SUN_POW + "\n";
//        preProcessorPreamble += "#define BLOCK_INTENSITY_FACTOR " + WorldRenderer.BLOCK_INTENSITY_FACTOR + "\n";
//        preProcessorPreamble += "#define SHADOW_MAP_RESOLUTION " + (float) renderConfig.getShadowMapResolution() + "\n";
//        preProcessorPreamble += "#define SSAO_KERNEL_ELEMENTS " + SSAO_KERNEL_ELEMENTS + "\n";
//        preProcessorPreamble += "#define SSAO_NOISE_SIZE " + SSAO_NOISE_SIZE + "\n";
//        // TODO: This shouldn't be hardcoded
//        preProcessorPreamble += "#define TEXTURE_OFFSET_EFFECTS " + 0.0625f + "\n";
//
//        StringBuilder builder = new StringBuilder().append(preProcessorPreamble);
//        if (renderConfig.isVolumetricFog()) {
//            builder.append("#define VOLUMETRIC_FOG");
//        }
//
//        if (renderConfig.isAnimateGrass()) {
//            builder.append("#define ANIMATED_GRASS \n");
//        }
//        if (renderConfig.isAnimateWater()) {
//            builder.append("#define ANIMATED_WATER \n");
//        }
//        if (renderConfig.getBlurIntensity() == 0) {
//            builder.append("#define NO_BLUR \n");
//        }
//        if (renderConfig.isFlickeringLight()) {
//            builder.append("#define FLICKERING_LIGHT \n");
//        }
//        if (renderConfig.isVignette()) {
//            builder.append("#define VIGNETTE \n");
//        }
//        if (renderConfig.isBloom()) {
//            builder.append("#define BLOOM \n");
//        }
//        if (renderConfig.isMotionBlur()) {
//            builder.append("#define MOTION_BLUR \n");
//        }
//        if (renderConfig.isSsao()) {
//            builder.append("#define SSAO \n");
//        }
//        if (renderConfig.isFilmGrain()) {
//            builder.append("#define FILM_GRAIN \n");
//        }
//        if (renderConfig.isOutline()) {
//            builder.append("#define OUTLINE \n");
//        }
//        if (renderConfig.isLightShafts()) {
//            builder.append("#define LIGHT_SHAFTS \n");
//        }
//        if (renderConfig.isDynamicShadows()) {
//            builder.append("#define DYNAMIC_SHADOWS \n");
//        }
//        if (renderConfig.isNormalMapping()) {
//            builder.append("#define NORMAL_MAPPING \n");
//        }
//        if (renderConfig.isParallaxMapping()) {
//            builder.append("#define PARALLAX_MAPPING \n");
//        }
//        if (renderConfig.isDynamicShadowsPcfFiltering()) {
//            builder.append("#define DYNAMIC_SHADOWS_PCF \n");
//        }
//        if (renderConfig.isCloudShadows()) {
//            builder.append("#define CLOUD_SHADOWS \n");
//        }
//        if (renderConfig.isLocalReflections()) {
//            builder.append("#define LOCAL_REFLECTIONS \n");
//        }
//        if (renderConfig.isInscattering()) {
//            builder.append("#define INSCATTERING \n");
//        }
//        // TODO A 3D wizard should take a look at this. Configurable for the moment to make better comparisons possible.
//        if (renderConfig.isClampLighting()) {
//            builder.append("#define CLAMP_LIGHTING \n");
//        }
//
//        for (ChunkVertexFlag vertexFlag : ChunkVertexFlag.values()) {
//            builder.append("#define ").append(vertexFlag.getDefineName()).append(" int(").append(vertexFlag.getValue()).append(") \n");
//        }
//
//        return builder;
//    }

    private String assembleShader(int type) {
        StringBuilder shader = createShaderBuilder();

        shader.append(includedDefines);
        shader.append(includedUniforms);

        if (type == GL20.GL_FRAGMENT_SHADER) {
            shader.append(includedFunctionsFragment);
            shader.append("\n");
            shader.append(shaderData.getFragmentProgram());
        } else {
            shader.append(includedFunctionsVertex);
            shader.append("\n");
            shader.append(shaderData.getVertexProgram());
        }

        return shader.toString();
    }

    private void dumpCode(int type, String sourceCode) {
        String debugShaderType = "UNKNOWN";
        if (type == GL20.GL_FRAGMENT_SHADER) {
            debugShaderType = "FRAGMENT";
        } else if (type == GL20.GL_VERTEX_SHADER) {
            debugShaderType = "VERTEX";
        }

        // Dump all final shader sources to the log directory
        final String strippedTitle = getUrn().toString().replace(":", "-");

        // example: fragment_shader-engine-font_0.glsl
        String fname = debugShaderType.toLowerCase() + "_" + strippedTitle + "_" + featureHash + ".glsl";
        Path path = PathManager.getInstance().getShaderLogPath().resolve(fname);
        try (BufferedWriter writer = Files.newBufferedWriter(path, TerasologyConstants.CHARSET)) {
            writer.write(sourceCode);
        } catch (IOException e) {
            logger.error("Failed to dump shader source.");
        }
    }

    private String getLogInfo(int shaderId) {
        int length = ARBShaderObjects.glGetObjectParameteriARB(shaderId, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB);

        if (length > 0) {
            return ARBShaderObjects.glGetInfoLogARB(shaderId, length);
        }

        return "No Info";
    }

    private boolean compileSuccess(int shaderId) {
        int compileStatus = ARBShaderObjects.glGetObjectParameteriARB(shaderId, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB);
        //int linkStatus = ARBShaderObjects.glGetObjectParameteriARB(shaderId, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB);
        //int validateStatus = ARBShaderObjects.glGetObjectParameteriARB(shaderId, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB);


        if (compileStatus == 0 /*|| linkStatus == 0 || validateStatus == 0*/) {
            return false;
        }

        //logger.info("Shader '{}' successfully compiled.", getURI());
        return true;
    }

    @Override
    protected void doReload(ShaderData data) {
        try {
            GameThread.synch(() -> {
                logger.debug("Recompiling shader {}.", getUrn());

                disposalAction.disposeData();
                shaderData = data;
                parameters.clear();
                for (ShaderParameterMetadata metadata : shaderData.getParameterMetadata()) {
                    parameters.put(metadata.getName(), metadata);
                }
                updateAvailableFeatures();
                try {
                    recompile();
                } catch (RuntimeException e) {
                    logger.warn(e.getMessage());
                }
            });
        } catch (InterruptedException e) {
            logger.error("Failed to reload {}", getUrn(), e);
        }
    }

    private static class DisposalAction implements Runnable {

        private final ResourceUrn urn;

        private TIntIntMap fragmentPrograms = new TIntIntHashMap();
        private TIntIntMap vertexPrograms = new TIntIntHashMap();

        // made package-private after CheckStyle's suggestion
        DisposalAction(ResourceUrn urn) {
            this.urn = urn;
        }

        @Override
        public void run() {
            logger.debug("Disposing shader {}.", urn);
            try {
                GameThread.synch(this::disposeData);
            } catch (InterruptedException e) {
                logger.error("Failed to dispose {}", urn, e);
            }
        }

        private void disposeData() {
            TIntIntIterator it = fragmentPrograms.iterator();
            while (it.hasNext()) {
                it.advance();
                GL20.glDeleteShader(it.value());
            }
            fragmentPrograms.clear();

            it = vertexPrograms.iterator();
            while (it.hasNext()) {
                it.advance();
                GL20.glDeleteShader(it.value());
            }
            vertexPrograms.clear();
        }
    }
}
