package com.noahcharlton.wgpuj.examples;

import com.noahcharlton.wgpuj.WgpuJava;
import com.noahcharlton.wgpuj.core.Device;
import com.noahcharlton.wgpuj.core.ShaderData;
import com.noahcharlton.wgpuj.core.WgpuCore;
import com.noahcharlton.wgpuj.core.WgpuGraphicApplication;
import com.noahcharlton.wgpuj.core.graphics.BlendDescriptor;
import com.noahcharlton.wgpuj.core.graphics.ColorState;
import com.noahcharlton.wgpuj.core.graphics.GraphicApplicationSettings;
import com.noahcharlton.wgpuj.core.graphics.RenderPipelineSettings;
import com.noahcharlton.wgpuj.core.math.MathUtils;
import com.noahcharlton.wgpuj.core.math.MatrixUtils;
import com.noahcharlton.wgpuj.core.util.Buffer;
import com.noahcharlton.wgpuj.core.util.BufferUsage;
import com.noahcharlton.wgpuj.core.util.Color;
import com.noahcharlton.wgpuj.core.util.Dimension;
import com.noahcharlton.wgpuj.jni.WgpuBindGroupEntry;
import com.noahcharlton.wgpuj.jni.WgpuBindGroupLayoutEntry;
import com.noahcharlton.wgpuj.jni.WgpuBindingType;
import com.noahcharlton.wgpuj.jni.WgpuBlendFactor;
import com.noahcharlton.wgpuj.jni.WgpuBlendOperation;
import com.noahcharlton.wgpuj.jni.WgpuColorStateDescriptor;
import com.noahcharlton.wgpuj.jni.WgpuCullMode;
import com.noahcharlton.wgpuj.jni.WgpuFrontFace;
import com.noahcharlton.wgpuj.jni.WgpuIndexFormat;
import com.noahcharlton.wgpuj.jni.WgpuInputStepMode;
import com.noahcharlton.wgpuj.jni.WgpuPrimitiveTopology;
import com.noahcharlton.wgpuj.jni.WgpuRasterizationStateDescriptor;
import com.noahcharlton.wgpuj.jni.WgpuTextureFormat;
import com.noahcharlton.wgpuj.jni.WgpuVertexBufferAttributeDescriptor;
import com.noahcharlton.wgpuj.jni.WgpuVertexBufferLayoutDescriptor;
import com.noahcharlton.wgpuj.jni.WgpuVertexFormat;
import jnr.ffi.Pointer;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HexWorldExample implements AutoCloseable {

    private static final int DEFAULT_WINDOW_WIDTH = 720;
    private static final int DEFAULT_WINDOW_HEIGHT = 550;

    private static final float RADIUS = 50f;
    private static final float HEX_X = (float) (RADIUS * Math.cos(MathUtils.toRadians(60)));
    private static final float HEX_Y = (float) (RADIUS * Math.sin(MathUtils.toRadians(60)));

    private static final float[] VERTICES = new float[]{
            0f, 0f,
            RADIUS, 0f,
            HEX_X, HEX_Y,
            -HEX_X, HEX_Y,
            -RADIUS, 0f,
            -HEX_X, -HEX_Y,
            HEX_X, -HEX_Y
    };

    private static final int FLOATS_PER_VERTEX = 2;

    private static final short[] INDICES = new short[]{
            0, 1, 2,
            0, 2, 3,
            0, 3, 4,
            0, 4, 5,
            0, 5, 6,
            0, 6, 1
    };

    private static final Matrix4f[] INSTANCES = generateTiles();

    private final WgpuGraphicApplication application;
    private final Device device;

    private Buffer vertexBuffer;
    private Buffer indexBuffer;
    private Buffer transMatrixBuffer;
    private Matrix4f viewMatrix;
    private long bindGroup;

    public HexWorldExample(GraphicApplicationSettings settings) {
        application = WgpuGraphicApplication.create(settings);
        device = application.getDevice();
        viewMatrix = new Matrix4f();
    }

    private void init() {
        float[] instanceModels = new float[16 * INSTANCES.length];
        float[] instanceColors = new float[4 * INSTANCES.length];
        Random random = new Random();

        for(int i = 0; i < INSTANCES.length; i++){
            var instance = MatrixUtils.toFloats(INSTANCES[i]);
            System.arraycopy(instance, 0, instanceModels, i * 16, 16);

            instanceColors[i * 4] = random.nextFloat() / 5f + .3f;
            instanceColors[i * 4 + 1] = random.nextFloat() / 10f + .2f;
            instanceColors[i * 4 + 2] = 0f;
            instanceColors[i * 4 + 3] = 1f;
        }

        var modelsBuffer = device.createFloatBuffer("Instance Models", instanceModels, BufferUsage.STORAGE);
        var colorsBuffer = device.createFloatBuffer("Instance Color", instanceColors, BufferUsage.STORAGE);
        var transformationMatrix = createTransformationMatrix();
        transMatrixBuffer = device.createFloatBuffer("Matrix", MatrixUtils.toFloats(transformationMatrix),
                BufferUsage.UNIFORM, BufferUsage.COPY_DST);

        var bindGroupLayout = device.createBindGroupLayout("matrix group layout",
                new WgpuBindGroupLayoutEntry().setPartial(0, WgpuBindGroupLayoutEntry.SHADER_STAGE_VERTEX,
                        WgpuBindingType.UNIFORM_BUFFER),
                new WgpuBindGroupLayoutEntry().setPartial(1, WgpuBindGroupLayoutEntry.SHADER_STAGE_VERTEX,
                        WgpuBindingType.STORAGE_BUFFER),
                new WgpuBindGroupLayoutEntry().setPartial(2, WgpuBindGroupLayoutEntry.SHADER_STAGE_VERTEX,
                        WgpuBindingType.STORAGE_BUFFER));

        bindGroup = device.createBindGroup("matrix bind group", bindGroupLayout,
                new WgpuBindGroupEntry().setBuffer(0, transMatrixBuffer.getId(), transMatrixBuffer.getSize()),
                new WgpuBindGroupEntry().setBuffer(1, modelsBuffer.getId(), modelsBuffer.getSize()),
                new WgpuBindGroupEntry().setBuffer(2, colorsBuffer.getId(), colorsBuffer.getSize()));

        var pipelineSettings = createRenderPipelineSettings();
        pipelineSettings.setBindGroupLayouts(bindGroupLayout);

        application.init(pipelineSettings);

        vertexBuffer = device.createVertexBuffer("Vertices", VERTICES);
        indexBuffer = device.createIndexBuffer("Indices", INDICES);
    }

    private RenderPipelineSettings createRenderPipelineSettings() {
        ShaderData vertex = ShaderData.fromRawClasspathFile("/hex_world.vert", "main");
        ShaderData fragment = ShaderData.fromRawClasspathFile("/hex_world.frag", "main");

        return new RenderPipelineSettings()
                .setVertexStage(vertex)
                .setFragmentStage(fragment)
                .setRasterizationState(new WgpuRasterizationStateDescriptor(
                        WgpuFrontFace.CCW,
                        WgpuCullMode.NONE,
                        0,
                        0.0f,
                        0.0f).getPointerTo())
                .setPrimitiveTopology(WgpuPrimitiveTopology.TRIANGLE_LIST)
                .setColorStates(new ColorState(
                        WgpuTextureFormat.BGRA8_UNORM,
                        new BlendDescriptor(WgpuBlendFactor.ONE, WgpuBlendFactor.ZERO, WgpuBlendOperation.ADD),
                        new BlendDescriptor(WgpuBlendFactor.ONE, WgpuBlendFactor.ZERO, WgpuBlendOperation.ADD),
                        WgpuColorStateDescriptor.ALL).build())
                .setDepthStencilState(WgpuJava.createNullPointer())
                .setVertexIndexFormat(WgpuIndexFormat.UINT16)
                .setBufferLayouts(new WgpuVertexBufferLayoutDescriptor(
                        Float.BYTES * FLOATS_PER_VERTEX,
                        WgpuInputStepMode.VERTEX,
                        new WgpuVertexBufferAttributeDescriptor(0, WgpuVertexFormat.FLOAT2, 0)))
                .setSampleCount(1)
                .setSampleMask(0)
                .setAlphaToCoverage(false)
                .setBindGroupLayouts()
                .setClearColor(Color.BLACK);
    }

    private void run() {
        while(!application.getWindow().isCloseRequested()) {
            updateTransMatrixBuffer();

            var renderPass = application.renderStart();

            renderPass.setBindGroup(0, bindGroup);
            renderPass.setVertexBuffer(vertexBuffer, 0);
            renderPass.setIndexBuffer(indexBuffer);
            renderPass.drawIndexed(INDICES.length, INSTANCES.length, 0);

            application.renderEnd();
        }
    }

    private void updateTransMatrixBuffer() {
        Pointer pointer = WgpuJava.createDirectPointer(16 * Float.BYTES);
        pointer.put(0, MatrixUtils.toFloats(createTransformationMatrix()), 0, 16);

        long queue = device.getDefaultQueue();
        long bufferId = transMatrixBuffer.getId();

        WgpuJava.wgpuNative.wgpu_queue_write_buffer(queue, bufferId, 0, pointer, 16 * Float.BYTES);
    }

    public static void main(String[] args) {
        WgpuCore.loadWgpuNative();

        var settings = new GraphicApplicationSettings("HexWorld", DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);

        try(var hexWorld = new HexWorldExample(settings)) {
            hexWorld.init();
            hexWorld.run();
        }
    }

    private Matrix4f createTransformationMatrix() {
        return MatrixUtils.generateMatrix(createProjectionMatrix(), viewMatrix);
    }

    private Matrix4f createProjectionMatrix() {
        Dimension dimension = application.getWindow().getWindowDimension();
        var projectionWidth = dimension.getWidth() / 2f;
        var projectionHeight = dimension.getHeight() / 2f;

        return new Matrix4f().ortho2D(-projectionWidth, projectionWidth,
                -projectionHeight, projectionHeight);
    }

    private static Matrix4f[] generateTiles() {
        List<Matrix4f> matrices = new ArrayList<>();
        int rows = 7;
        int columns = 7;

        float startX = -rows * RADIUS / 2f;
        float startY = -columns * HEX_Y / 2f;

        for(int x = 0; x < rows; x++){
            for(int y = 0; y < columns; y++){
                Matrix4f matrix = new Matrix4f();
                float xPos = startX + 1.5f * x * RADIUS;
                float yPos = startY + 2 * y * HEX_Y;

                if(x % 2 == 1){
                    yPos -= HEX_Y;
                }

                matrix.translate(xPos, yPos, 0f);

                matrices.add(matrix);
            }
        }

        return matrices.toArray(new Matrix4f[0]);
    }

    @Override
    public void close() {
        application.close();
    }
}
