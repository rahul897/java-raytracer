package com.raytrace;

public class Sphere {
    Vector3D center;
    Material material;
    int radius;

    public Sphere(Vector3D center, int radius) {
        this.center = center;
        this.radius = radius;
    }

    public Sphere(Vector3D center, int radius, Material material) {
        this.center = center;
        this.material = material;
        this.radius = radius;
    }

    public IntersectResult rayIntersect(Vector3D orig, Vector3D dir) {
        Vector3D L = center.subtract(orig);
        double tca = L.dot(dir);
        double d2 = L.dot(L) - tca*tca;
        if (d2 > radius*radius) return new IntersectResult(false);
        double thc = Math.sqrt(radius*radius - d2);
        double t0 = tca - thc;
        double t1 = tca + thc;
        if (t0 < 0) t0 = t1;
        if (t0 < 0) return new IntersectResult(false);
        return new IntersectResult(t0, true);
    }
}
