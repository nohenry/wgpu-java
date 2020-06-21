package com.noahcharlton.wgpuj.jni;

import com.noahcharlton.wgpuj.WgpuJava;
import com.noahcharlton.wgpuj.util.WgpuJavaStruct;
import com.noahcharlton.wgpuj.util.CStrPointer;
import com.noahcharlton.wgpuj.util.RustCString;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** NOTE: THIS FILE WAS PRE-GENERATED BY JNR_GEN! */
public class WgpuVertexAttributeDescriptor extends WgpuJavaStruct {

    private final Struct.Unsigned64 offset = new Struct.Unsigned64();
    private final Struct.Enum<WgpuVertexFormat> format = new Struct.Enum<>(WgpuVertexFormat.class);
    private final Struct.Unsigned32 shaderLocation = new Struct.Unsigned32();

    private WgpuVertexAttributeDescriptor(){}

    @Deprecated
    public WgpuVertexAttributeDescriptor(Runtime runtime){
        super(runtime);
    }

    public static WgpuVertexAttributeDescriptor createHeap(){
        return new WgpuVertexAttributeDescriptor();
    }

    public static WgpuVertexAttributeDescriptor createDirect(){
        var struct = new WgpuVertexAttributeDescriptor();
        struct.useDirectMemory();
        return struct;
    }


    public long getOffset(){
        return offset.get();
    }

    public void setOffset(long x){
        this.offset.set(x);
    }

    public WgpuVertexFormat getFormat(){
        return format.get();
    }

    public void setFormat(WgpuVertexFormat x){
        this.format.set(x);
    }

    public long getShaderLocation(){
        return shaderLocation.get();
    }

    public void setShaderLocation(long x){
        this.shaderLocation.set(x);
    }

}