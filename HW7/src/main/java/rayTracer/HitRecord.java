package rayTracer;

import org.joml.Vector2d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import util.Material;
import util.TextureImage;

public class HitRecord {

  private float t;
  private Vector4f intersectionInView;
  private Vector3f normal;
  private Material material;
  private TextureImage textureImage;
  private Vector2d textureCoordinate;

  public HitRecord() {
    intersectionInView = null;
    normal = null;
    material = null;
    textureImage = null;
    t = -1;
    textureCoordinate = null;
  }

  public float getT() {
    return t;
  }

  public void setT(float t) {
    this.t = t;
  }

  public Vector4f getIntersection() {
    return intersectionInView;
  }

  public void setIntersection(Vector4f intersectionInView) {
    this.intersectionInView = new Vector4f(intersectionInView);
  }

  public void setIntersection(float x, float y, float z) {
    this.intersectionInView = new Vector4f(x, y, z, 1);
  }

  public Vector3f getNormal() {
    return normal;
  }

  public void setNormal(Vector3f normal) {
    this.normal = new Vector3f(normal);
  }

  public void setNormal(float x, float y, float z) {
    this.normal = new Vector3f(x, y, z);
  }

  public Material getMaterial() {
    return material;
  }

  public void setMaterial(Material material) {
    this.material = material;
  }

  public TextureImage getTexture() {
    return textureImage;
  }

  public void setTextureImage(TextureImage textureImage) {
    this.textureImage = textureImage;
  }

  public Vector2d getTextureCoordinate() {
    return textureCoordinate;
  }

  public void setTextureCoordinate(Vector2d textureCoordinate) {
    this.textureCoordinate = new Vector2d(textureCoordinate);
  }

  public void setTextureCoordinate(float x, float y) {
    this.textureCoordinate = new Vector2d(x, y);
  }
}
