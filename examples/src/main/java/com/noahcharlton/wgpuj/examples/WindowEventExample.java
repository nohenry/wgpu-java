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
import com.noahcharlton.wgpuj.core.graphics.Window;
import com.noahcharlton.wgpuj.core.graphics.WindowEventHandler;
import com.noahcharlton.wgpuj.core.input.Key;
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
import org.joml.Vector3f;

import java.util.EnumSet;

public class WindowEventExample {

    private static final float SIZE = 50;

    private static final float[] VERTICES = new float[]{
            -SIZE, -SIZE, 1f, 0f, 0f,
            -SIZE, SIZE, 0f, 1f, 0f,
            SIZE, SIZE, 0f, 0f, 1f,
            SIZE, -SIZE, 1f, 1f, 1f,
    };

    private static final short[] INDICES = new short[]{
            0, 1, 2,
            0, 2, 3
    };

    private static final int FLOATS_PER_VERTEX = 5;

    private final Matrix4f viewMatrix = new Matrix4f().lookAt(
            new Vector3f(0, 0, 0),
            new Vector3f(0, 0, -1),
            MathUtils.UNIT_Y
    );

    private final Window window;
    private final Buffer vertexBuffer;
    private final Buffer indexBuffer;
    private final Buffer matrixBuffer;
    private final long bindGroup;
    private final long queue;
    private final EnumSet<Key> keysPressed = EnumSet.noneOf(Key.class);

    public WindowEventExample() {
        RenderPipelineSettings renderPipelineSettings = createPipelineSettings();
        GraphicApplicationSettings appSettings = new GraphicApplicationSettings("Wgpu-Java event example", 640, 480);

        try(var application = WgpuGraphicApplication.create(appSettings)) {
            Device device = application.getDevice();
            window = application.getWindow();
            window.setEventHandler(new WindowHandler(this));
            queue = application.getQueue();

            vertexBuffer = device.createVertexBuffer("Vertex buffer", VERTICES);
            indexBuffer = device.createIndexBuffer("Index buffer", INDICES);

            Matrix4f projection = createProjectionMatrix();
            Matrix4f transformationMatrix = MatrixUtils.generateMatrix(projection, viewMatrix);

            matrixBuffer = device.createFloatBuffer("Matrix", MatrixUtils.toFloats(transformationMatrix),
                    BufferUsage.UNIFORM, BufferUsage.COPY_DST);

            var bindGroupLayout = device.createBindGroupLayout("matrix group layout",
                    new WgpuBindGroupLayoutEntry().setPartial(0, WgpuBindGroupLayoutEntry.SHADER_STAGE_VERTEX,
                            WgpuBindingType.UNIFORM_BUFFER));

            bindGroup = device.createBindGroup("matrix bind group", bindGroupLayout,
                            new WgpuBindGroupEntry().setBuffer(0, matrixBuffer.getId(), matrixBuffer.getSize()));

            renderPipelineSettings.setBindGroupLayouts(bindGroupLayout);
            application.init(renderPipelineSettings);

            runMainLoop(application);
        }
    }

    private Matrix4f createProjectionMatrix() {
        Dimension dimension = window.getWindowDimension();
        var projectionWidth = dimension.getWidth() / 2f;
        var projectionHeight = dimension.getHeight() / 2f;

        return new Matrix4f().ortho2D(-projectionWidth, projectionWidth,
                -projectionHeight, projectionHeight);
    }

    private void updateMatrix() {
        Matrix4f projection = createProjectionMatrix();
        Matrix4f transformationMatrix = MatrixUtils.generateMatrix(projection, viewMatrix);

        Pointer pointer = WgpuJava.createDirectPointer(16 * Float.BYTES);
        pointer.put(0, MatrixUtils.toFloats(transformationMatrix), 0, 16);

        WgpuJava.wgpuNative.wgpu_queue_write_buffer(queue, matrixBuffer.getId(), 0, pointer, 16 * Float.BYTES);
    }

    private void runMainLoop(WgpuGraphicApplication application) {
        while(!application.getWindow().isCloseRequested()) {
            updateInput();

            var renderPass = application.renderStart();
            renderPass.setBindGroup(0, bindGroup);
            renderPass.setIndexBuffer(indexBuffer);
            renderPass.setVertexBuffer(vertexBuffer, 0);

            renderPass.drawIndexed(INDICES.length, 1, 0);

            application.renderEnd();
        }
    }

    private void updateInput() {
        boolean dirty = false;
        float speed = 4f;
        float rotateSpeed = .1f;

        if(keysPressed.contains(Key.W)) {
            viewMatrix.translate(0f, speed, 0f);
            dirty = true;
        }

        if(keysPressed.contains(Key.A)) {
            viewMatrix.translate(-speed, 0f, 0f);
            dirty = true;
        }

        if(keysPressed.contains(Key.S)) {
            viewMatrix.translate(0f, -speed, 0f);
            dirty = true;
        }

        if(keysPressed.contains(Key.D)) {
            viewMatrix.translate(speed, 0f, 0f);
            dirty = true;
        }

        if(keysPressed.contains(Key.Q)) {
            viewMatrix.rotate(rotateSpeed, MathUtils.UNIT_Z);
            dirty = true;
        }

        if(keysPressed.contains(Key.E)) {
            viewMatrix.rotate(-rotateSpeed, MathUtils.UNIT_Z);
            dirty = true;
        }

        if(dirty)
            updateMatrix();
    }

    private RenderPipelineSettings createPipelineSettings() {
        ShaderData vertex = ShaderData.fromRawClasspathFile("/window.vert", "main");
        ShaderData fragment = ShaderData.fromRawClasspathFile("/window.frag", "main");

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
                        new WgpuVertexBufferAttributeDescriptor(0, WgpuVertexFormat.FLOAT2, 0),
                        new WgpuVertexBufferAttributeDescriptor(Float.BYTES * 2, WgpuVertexFormat.FLOAT3, 1)))
                .setSampleCount(1)
                .setSampleMask(0)
                .setAlphaToCoverage(false)
                .setBindGroupLayouts()
                .setClearColor(Color.BLACK);
    }

    public static void main(String[] args) {
        WgpuCore.loadWgpuNative();

        new WindowEventExample();
    }

    static class WindowHandler implements WindowEventHandler {

        private final WindowEventExample app;

        public WindowHandler(WindowEventExample app) {
            this.app = app;
        }

        @Override
        public void onResize() {
            app.updateMatrix();
        }

        @Override
        public void onKeyPressed(Key key) {
            app.keysPressed.add(key);
        }

        @Override
        public void onKeyReleased(Key key) {
            app.keysPressed.remove(key);
        }
    }
}
