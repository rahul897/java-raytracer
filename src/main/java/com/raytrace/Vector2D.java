package com.raytrace;

public class Vector2D {
    public double x,y;

    public Vector2D(){
        this.x = 0;this.y =0;
    }

    public Vector2D(double x, double y){
        this.x = x;this.y =y;
    }

    public Vector2D add(Vector2D v){
        return new Vector2D(this.x+v.x, this.y+v.y);
    }

    public Vector2D subtract(Vector2D v){
        return new Vector2D(this.x-v.x, this.y-v.y);
    }

    public Vector2D negate(Vector2D v){
        return new Vector2D(-this.x, -this.y);
    }

    public Vector2D multiply(Vector2D v){
        return new Vector2D(this.x*v.x, this.y*v.y);
    }

    public Vector2D add(double v){
        return new Vector2D(this.x+v, this.y+v);
    }

    public Vector2D subtract(double v){
        return new Vector2D(this.x-v, this.y-v);
    }

    public Vector2D multiply(double v){
        return new Vector2D(this.x*v, this.y*v);
    }

    public double dot(Vector2D v){
        return this.x*v.x+ this.y*v.y;
    }

    public double sqDistance(){
        return this.x*this.x+this.y*this.y;
    }

    public double norm(){
        return Math.sqrt(sqDistance());
    }

    @Override
    public String toString() {
        return x +" " + y +" " ;
    }

}
