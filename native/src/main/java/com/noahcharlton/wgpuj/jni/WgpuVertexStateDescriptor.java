package com.noahcharlton.wgpuj.jni;

import com.noahcharlton.wgpuj.WgpuJava;
import com.noahcharlton.wgpuj.util.WgpuJavaStruct;
import com.noahcharlton.wgpuj.util.CStrPointer;
import com.noahcharlton.wgpuj.util.RustCString;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** NOTE: THIS FILE WAS PRE-GENERATED BY JNR_GEN! */
public class WgpuVertexStateDescriptor extends WgpuJavaStruct {

    private final Struct.Enum<WgpuIndexFormat> indexFormat = new Struct.Enum<>(WgpuIndexFormat.class);
    private final Struct.StructRef<WgpuVertexBufferLayoutDescriptor> vertexBuffers = new Struct.StructRef<>(WgpuVertexBufferLayoutDescriptor.class);
    private final Struct.Unsigned64 vertexBuffersLength = new Struct.Unsigned64();

    private WgpuVertexStateDescriptor(){}

    @Deprecated
    public WgpuVertexStateDescriptor(Runtime runtime){
        super(runtime);
    }

    public static WgpuVertexStateDescriptor createHeap(){
        return new WgpuVertexStateDescriptor();
    }

    public static WgpuVertexStateDescriptor createDirect(){
        var struct = new WgpuVertexStateDescriptor();
        struct.useDirectMemory();
        return struct;
    }


    public WgpuIndexFormat getIndexFormat(){
        return indexFormat.get();
    }

    public void setIndexFormat(WgpuIndexFormat x){
        this.indexFormat.set(x);
    }

    public Struct.StructRef<WgpuVertexBufferLayoutDescriptor> getVertexBuffers(){
        return vertexBuffers;
    }

    public void setVertexBuffers(WgpuVertexBufferLayoutDescriptor... x){
        if(x.length == 0 || x[0] == null){
            this.vertexBuffers.set(WgpuJava.createNullPointer());
        } else {
            this.vertexBuffers.set(x);
        }
    }

    public long getVertexBuffersLength(){
        return vertexBuffersLength.get();
    }

    public void setVertexBuffersLength(long x){
        this.vertexBuffersLength.set(x);
    }

}