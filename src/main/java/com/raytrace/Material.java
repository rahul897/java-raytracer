package com.raytrace;

public class Material {
    Vector3D color;
    Vector4D albedo;
    double specularExponent;
    double refractiveIndex;

    public Material() {
        this.albedo = new Vector4D(1,0,0,0);
    }

    public Material(double refractiveIndex, Vector4D albedo, Vector3D color, double specularExponent) {
        this.color = color;
        this.albedo = albedo;
        this.specularExponent = specularExponent;
        this.refractiveIndex = refractiveIndex;
    }
}
