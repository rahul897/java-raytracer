package com.raytrace;

public class IntersectResult {
    boolean result;
    double distance;
    Vector3D hit;
    Vector3D normal;
    Material material;

    public IntersectResult(boolean b) {
        result = b;
        distance = Double.MAX_VALUE;
    }

    public IntersectResult(double t0, boolean b) {
        result = b;
        distance = t0;
    }

    public IntersectResult(Vector3D hit, Vector3D normal, Material material, boolean result) {
        this.result = result;
        this.hit = hit;
        this.normal = normal;
        this.material = material;
    }
}
