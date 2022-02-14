package com.raytrace;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.*;

public class MainRenderer {
    static BufferedImage bg = null;
    static int envmapHeight;
    static int envmapWidth;

    static Vector3D reflect(Vector3D I, Vector3D N) {
        return I.subtract(N.multiply(2.f*(I.dot(N))));
    }

    static Vector3D refract(Vector3D I, Vector3D N, double refractiveIndex) { // Snell's law
        double cosi = - Math.max(-1.f, Math.min(1.f, I.dot(N)));
        double etai = 1, etat = refractiveIndex, tmp;
        Vector3D n = N;
        if (cosi < 0) {
            cosi = -cosi;
            tmp = etai;
            etai = etat;
            etat = tmp;
            n = N.multiply(-1);
        }
        double eta = etai / etat;
        double k = 1 - eta*eta*(1 - cosi*cosi);
        return k < 0 ? new Vector3D(0,0,0) : I.multiply(eta).add(n.multiply(eta * cosi - sqrt(k)));
    }

    static Vector3D castRay(Vector3D orig, Vector3D dir, List<Sphere> spheres, List<Light> lights, int depth) {
        IntersectResult ir = sceneIntersect(orig, dir, spheres);
        if (depth>4 || !ir.result) {
            // background (spherical coord)
            int x_raw = ((int) ((atan2(dir.z, dir.x) / (2 * PI) + 0.5) * envmapWidth) + (int) ( envmapWidth /  PI)) % envmapWidth;
            int y_raw = (int) (acos(dir.y) / PI * envmapHeight);
            int x = Math.max(0, Math.min(x_raw, envmapWidth -1));
            int y = Math.max(0, Math.min(y_raw, envmapHeight -1));
            Color bgColor = new Color(bg.getRGB(x, y));
            return new Vector3D(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue()).multiply(1/255.); // background color
//            return new Vector3D(0.2, 0.7, 0.8); // background color
        }

        Vector3D reflectDir = reflect(dir, ir.normal).normalize();
        Vector3D refractDir = refract(dir, ir.normal, ir.material.refractiveIndex).normalize();
        Vector3D reflectOrig = reflectDir.dot(ir.normal) < 0 ? ir.hit.subtract(ir.normal.multiply(1e-3)) : ir.hit.add(ir.normal.multiply(1e-3)); // offset the original point to avoid occlusion by the object itself
        Vector3D refractOrig = refractDir.dot(ir.normal) < 0 ? ir.hit.subtract(ir.normal.multiply(1e-3)) : ir.hit.add(ir.normal.multiply(1e-3)); // offset the original point to avoid occlusion by the object itself

        Vector3D reflectColor = castRay(reflectOrig, reflectDir, spheres, lights, depth + 1);
        Vector3D refractColor = castRay(refractOrig, refractDir, spheres, lights, depth + 1);


        float diffuseLightIntensity = 0, specularLightintensity = 0;
        for (int i=0; i<lights.size(); i++) {
            Vector3D lightDir      = (lights.get(i).position.subtract(ir.hit)).normalize();
            double lightDistance = (lights.get(i).position.subtract(ir.hit)).norm();

            Vector3D shadow_orig = lightDir.dot(ir.normal) < 0 ? ir.hit.subtract(ir.normal.multiply(1e-3)) : ir.hit.add(ir.normal.multiply(1e-3)) ; // checking if the point lies in the shadow of the lights.get(i
            IntersectResult shadowIr = sceneIntersect(shadow_orig, lightDir, spheres);
            if (shadowIr.result && (shadowIr.hit.subtract(shadow_orig)).norm() < lightDistance)
                continue;

            diffuseLightIntensity  += lights.get(i).intensity * Math.max(0.f, lightDir.dot(ir.normal));
            specularLightintensity += Math.pow(Math.max(0.f, -reflect(lightDir.multiply(-1), ir.normal).dot(dir)), ir.material.specularExponent)*lights.get(i).intensity;

        }
        return ir.material.color.multiply(diffuseLightIntensity*ir.material.albedo.x).add(
                new Vector3D(1., 1., 1.).multiply(specularLightintensity * ir.material.albedo.y)).add(
                        reflectColor.multiply(ir.material.albedo.z)).add(
                                refractColor.multiply(ir.material.albedo.w)
        );
    }

    static IntersectResult sceneIntersect(Vector3D orig, Vector3D dir, List<Sphere> spheres) {
        double spheres_dist = Double.MAX_VALUE;
        Vector3D hit = null,N = null;
        Material material = new Material();
        for (int i=0; i < spheres.size(); i++) {
            IntersectResult ir = spheres.get(i).rayIntersect(orig, dir);
            if (ir.result && ir.distance < spheres_dist) {
                spheres_dist = ir.distance;
                hit = orig.add(dir.multiply(ir.distance));
                N = (hit.subtract(spheres.get(i).center)).normalize();
                material = spheres.get(i).material;
            }
        }

        double checkerboardDist = Double.MAX_VALUE;
        if (Math.abs(dir.y)>1e-3) {
            double d = -(orig.y + 4) / dir.y; // the checkerboard plane has equation y = -4
            Vector3D pt = orig.add(dir.multiply(d));
            if (d > 0 && Math.abs(pt.x) < 10 && pt.z < -10 && pt.z > -30 && d < spheres_dist) {
                checkerboardDist = d;
                hit = pt;
                N = new Vector3D(0, 1, 0);
                material.color = (int) ((int)(.5 * hit.x + 1000) + (int)hit.z*0.5) %2==1 ? new Vector3D(.3,.3,.3) : new Vector3D(.3, .2, .1);
            }
        }

        if(min(spheres_dist,checkerboardDist)<1000)
            return new IntersectResult(hit,N,material,true);
        return new IntersectResult(false);
    }

    public static void render(List<Sphere> spheres, List<Light> lights) throws IOException {
        int width = 1080;
        int height = 720;
        int fov      = (int) (Math.PI/3.);

        Vector3D[] frameBuffer = new Vector3D[width*height];

        for (int j = 0; j<height; j++) {
            for (int i = 0; i<width; i++) {
                frameBuffer[i+j*width] = new Vector3D((double)j/height,(double)i/width, 0);
            }
        }
//        BufferedWriter writer = new BufferedWriter(new FileWriter("out.ppm"));
//        writer.write("P3\n" + width + " " + height + "\n255\n");
//        for (int j = 0; j<height; j++) {
//            for (int i = 0; i<width; i++) {
//                writer.write( ""+(int)(255 * Math.max(0.d, Math.min(1.d, frameBuffer[i+j*width].x)))+" ");
//                writer.write( ""+(int)(255 * Math.max(0.d, Math.min(1.d, frameBuffer[i+j*width].y)))+" ");
//                writer.write( ""+(int)(255 * Math.max(0.d, Math.min(1.d, frameBuffer[i+j*width].z)))+" ");
//            }
//            writer.write("\n");
//        }
//        writer.close() ;
        BufferedImage img = new BufferedImage(width, height, TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++){

                double x = (2 * (i + 0.5) / width - 1) * tan(fov / 2.) * width / height;
                double y = -(2 * (j + 0.5) / height - 1) * tan(fov / 2.);

                Vector3D dir = new Vector3D(x, y, -1).normalize();
                frameBuffer[i + j * width] = castRay(new Vector3D(0, 0, 0), dir, spheres, lights,0);
                frameBuffer[i + j * width].colorNormalize();
                img.setRGB(i, j, new Color((int) (255 * frameBuffer[i + j * width].x),
                        (int) (255 * frameBuffer[i + j * width].y),
                        (int) (255 * frameBuffer[i + j * width].z)).getRGB());
            }
        }

        ImageIcon icon=new ImageIcon(img);
        JFrame frame=new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(width,height);
        JLabel lbl=new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) throws IOException {
        bg = ImageIO.read(new File("C:\\Users\\rahul\\Downloads\\raytrace\\envmap.jpg"));
        envmapHeight = bg.getHeight();
        envmapWidth = bg.getWidth();
        Material      ivory = new Material(1.0, new Vector4D(0.6,  0.3, 0.1, 0.0), new Vector3D(0.4, 0.4, 0.3),   50.);
        Material      glass = new Material(1.5, new Vector4D(0.0,  0.5, 0.1, 0.8), new Vector3D(0.6, 0.7, 0.8),  125.);
        Material red_rubber = new Material(1.0, new Vector4D(0.9,  0.1, 0.0, 0.0), new Vector3D(0.3, 0.1, 0.1),   10.);
        Material     mirror = new Material(1.0, new Vector4D(0.0, 10.0, 0.8, 0.0), new Vector3D(1.0, 1.0, 1.0), 1425.);

        List<Sphere> spheres = new ArrayList<>();
        spheres.add(new Sphere(new Vector3D(-3,    0,   -16), 2,      ivory));
        spheres.add(new Sphere(new Vector3D(-1.0, -1.5, -12), 2, glass));
        spheres.add(new Sphere(new Vector3D( 1.5, -0.5, -18), 3, red_rubber));
        spheres.add(new Sphere(new Vector3D( 7,    5,   -18), 4,      mirror));

        List<Light>  lights = new ArrayList<>();
        lights.add(new Light(new Vector3D(-20, 20,  20), 1.5));
        lights.add(new Light(new Vector3D( 30, 50, -25), 1.8));
        lights.add(new Light(new Vector3D( 30, 20,  30), 1.7));

        render(spheres, lights);
    }
}
