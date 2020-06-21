package com.noahcharlton.wgpuj.jni;

import com.noahcharlton.wgpuj.WgpuJava;
import com.noahcharlton.wgpuj.util.WgpuJavaStruct;
import com.noahcharlton.wgpuj.util.CStrPointer;
import com.noahcharlton.wgpuj.util.RustCString;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** NOTE: THIS FILE WAS PRE-GENERATED BY JNR_GEN! */
public class WgpuProgrammableStageDescriptor extends WgpuJavaStruct {

    private final Struct.Unsigned64 module = new Struct.Unsigned64();
    private final @CStrPointer Struct.Pointer entryPoint = new Struct.Pointer();

    private WgpuProgrammableStageDescriptor(){}

    @Deprecated
    public WgpuProgrammableStageDescriptor(Runtime runtime){
        super(runtime);
    }

    public static WgpuProgrammableStageDescriptor createHeap(){
        return new WgpuProgrammableStageDescriptor();
    }

    public static WgpuProgrammableStageDescriptor createDirect(){
        var struct = new WgpuProgrammableStageDescriptor();
        struct.useDirectMemory();
        return struct;
    }


    public long getModule(){
        return module.get();
    }

    public void setModule(long x){
        this.module.set(x);
    }

    public java.lang.String getEntryPoint(){
        return RustCString.fromPointer(entryPoint.get());
    }

    public void setEntryPoint(java.lang.String x){
        this.entryPoint.set(RustCString.toPointer(x));
    }

}