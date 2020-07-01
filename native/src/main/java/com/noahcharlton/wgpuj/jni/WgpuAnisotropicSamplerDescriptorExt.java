package com.noahcharlton.wgpuj.jni;

import com.noahcharlton.wgpuj.WgpuJava;
import com.noahcharlton.wgpuj.util.WgpuJavaStruct;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** NOTE: THIS FILE WAS PRE-GENERATED BY JNR_GEN! */
public class WgpuAnisotropicSamplerDescriptorExt extends WgpuJavaStruct {

    private final Struct.StructRef<WgpuChainedStruct> nextInChain = new Struct.StructRef<>(WgpuChainedStruct.class);
    private final Struct.Enum<WgpuSType> sType = new Struct.Enum<>(WgpuSType.class);
    private final Struct.Unsigned8 anisotropicClamp = new Struct.Unsigned8();

    private WgpuAnisotropicSamplerDescriptorExt(){}

    @Deprecated
    public WgpuAnisotropicSamplerDescriptorExt(Runtime runtime){
        super(runtime);
    }

    public static WgpuAnisotropicSamplerDescriptorExt createHeap(){
        return new WgpuAnisotropicSamplerDescriptorExt();
    }

    public static WgpuAnisotropicSamplerDescriptorExt createDirect(){
        var struct = new WgpuAnisotropicSamplerDescriptorExt();
        struct.useDirectMemory();
        return struct;
    }


    public Struct.StructRef<WgpuChainedStruct> getNextInChain(){
        return nextInChain;
    }

    public void setNextInChain(WgpuChainedStruct... x){
        if(x.length == 0 || x[0] == null){
            this.nextInChain.set(WgpuJava.createNullPointer());
        } else {
            this.nextInChain.set(x);
        }
    }

    public WgpuSType getSType(){
        return sType.get();
    }

    public void setSType(WgpuSType x){
        this.sType.set(x);
    }

    public short getAnisotropicClamp(){
        return anisotropicClamp.get();
    }

    public void setAnisotropicClamp(short x){
        this.anisotropicClamp.set(x);
    }

}