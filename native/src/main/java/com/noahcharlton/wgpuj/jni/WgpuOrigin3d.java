package com.noahcharlton.wgpuj.jni;

import com.noahcharlton.wgpuj.WgpuJava;
import com.noahcharlton.wgpuj.util.WgpuJavaStruct;
import com.noahcharlton.wgpuj.util.CStrPointer;
import com.noahcharlton.wgpuj.util.RustCString;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/** NOTE: THIS FILE WAS PRE-GENERATED BY JNR_GEN! */
public class WgpuOrigin3d extends WgpuJavaStruct {

    private final Struct.Unsigned32 x = new Struct.Unsigned32();
    private final Struct.Unsigned32 y = new Struct.Unsigned32();
    private final Struct.Unsigned32 z = new Struct.Unsigned32();

    private WgpuOrigin3d(){}

    @Deprecated
    public WgpuOrigin3d(Runtime runtime){
        super(runtime);
    }

    public static WgpuOrigin3d createHeap(){
        return new WgpuOrigin3d();
    }

    public static WgpuOrigin3d createDirect(){
        var struct = new WgpuOrigin3d();
        struct.useDirectMemory();
        return struct;
    }


    public long getX(){
        return x.get();
    }

    public void setX(long x){
        this.x.set(x);
    }

    public long getY(){
        return y.get();
    }

    public void setY(long x){
        this.y.set(x);
    }

    public long getZ(){
        return z.get();
    }

    public void setZ(long x){
        this.z.set(x);
    }

}