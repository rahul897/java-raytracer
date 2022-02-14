package com.raytrace;

public class Light {
    Vector3D position;
    double intensity;

    public Light(Vector3D position, double intensity) {
        this.position = position;
        this.intensity = intensity;
    }
}
