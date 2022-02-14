package com.raytrace;

public class Vector3D {
    public double x,y,z;

    public Vector3D(){
        this.x = 0;this.y =0;this.z = 0;
    }

    public Vector3D(double x, double y,double z){
        this.x = x;this.y =y;this.z = z;
    }

    public Vector3D add(Vector3D v){
        return new Vector3D(this.x+v.x, this.y+v.y, this.z+v.z);
    }

    public Vector3D subtract(Vector3D v){
        return new Vector3D(this.x-v.x, this.y-v.y, this.z-v.z);
    }

    public Vector3D negate(Vector3D v){
        return new Vector3D(-this.x, -this.y, -this.z);
    }

    public Vector3D multiply(Vector3D v){
        return new Vector3D(this.x*v.x, this.y*v.y, this.z*v.z);
    }

    public Vector3D add(double v){
        return new Vector3D(this.x+v, this.y+v, this.z+v);
    }

    public Vector3D subtract(double v){
        return new Vector3D(this.x-v, this.y-v,this.z-v);
    }

    public Vector3D multiply(double v){
        return new Vector3D(this.x*v, this.y*v, this.z*v);
    }

    public double dot(Vector3D v){
        return this.x*v.x+ this.y*v.y+this.z*v.z;
    }

    public double sqDistance(){
        return this.x*this.x+this.y*this.y+this.z*this.z;
    }

    public double norm(){
        return Math.sqrt(sqDistance());
    }

    public Vector3D cross(Vector3D v){
        return new Vector3D(this.y*v.z - this.z*v.y, this.z*v.x - this.x*v.z, this.x*v.y - this.y*v.x );
    }

    public Vector3D normalize(){
        return this.multiply(1/norm());
    }

    @Override
    public String toString() {
        return x +" " + y +" " + z+ " ";
    }

    public void colorNormalize() {
        double mxyz = Math.max(x,Math.max(y,z));
        if(mxyz>1){
            this.x*=1/mxyz;
            this.y*=1/mxyz;
            this.z*=1/mxyz;
        }
    }
}
