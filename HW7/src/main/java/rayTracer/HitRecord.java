package rayTracer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import util.Material;
import util.TextureImage;

/**
 * This class records all necessary information about a hit.
 */
public class HitRecord implements Comparable<HitRecord> {

  private float t;
  private float fromRefraction;
  private float toRefraction;
  private Vector4f intersectionInView;
  private Vector4f normal;
  private Material material;
  private TextureImage textureImage;
  private Vector2f textureCoordinate;


  private boolean flipNormal;

  /**
   * Construct a hit record with default values.
   */
  public HitRecord() {
    intersectionInView = null;
    normal = null;
    material = null;
    textureImage = null;
    t = -1;
    textureCoordinate = null;
    fromRefraction = 1;
    toRefraction = 1;
  }

  public float getT() {
    return t;
  }

  public void setT(float t) {
    this.t = t;
  }

  public Vector4f getIntersection() {
    return new Vector4f(intersectionInView);
  }

  /**
   * Set the intersection in view.
   */
  public void setIntersection(Vector4f intersectionInView) {
    this.intersectionInView = new Vector4f(intersectionInView);
  }

  public void setIntersection(float x, float y, float z) {
    this.intersectionInView = new Vector4f(x, y, z, 1);
  }

  public Vector4f getNormal() {
    return new Vector4f(normal);
  }

  public void setNormal(Vector3f normal) {
    this.normal = new Vector4f(normal.x, normal.y, normal.z, 0).normalize();
  }

  public void setNormal(Vector4f normal) {
    this.normal = new Vector4f(normal).normalize();
  }

  public void setNormal(float x, float y, float z) {
    this.normal = new Vector4f(x, y, z, 0).normalize();
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

  public Vector2f getTextureCoordinate() {
    return new Vector2f(textureCoordinate);
  }

  public void setTextureCoordinate(Vector2f textureCoordinate) {
    this.textureCoordinate = new Vector2f(textureCoordinate);
  }

  public void setTextureCoordinate(float x, float y) {
    this.textureCoordinate = new Vector2f(x, y);
  }

  public float getFromRefraction() {
    return fromRefraction;
  }

  public float getToRefraction() {
    return toRefraction;
  }

  public void setFromRefraction(float fromRefraction) {
    this.fromRefraction = fromRefraction;
  }

  public void setToRefraction(float toRefraction) {
    this.toRefraction = toRefraction;
  }

  public boolean getFlipNormal() {
    return flipNormal;
  }

  public void setFlipNormal(boolean flipNormal) {
    this.flipNormal = flipNormal;
  }


  /**
   * Comparison between hit records are based on t.
   *
   * @param o the other hit record
   * @return 1 if this t is greater than the other t; 0 if equal; -1 if this t is less than the
   * other t.
   */
  @Override
  public int compareTo(HitRecord o) {
    float dif = this.getT() - o.getT();
    if (dif > 0) {
      return 1;
    } else if (dif == 0) {
      return 0;
    } else {
      return -1;
    }
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append(String.format("\n    t: %f\n", this.getT()))
        .append("    intersection in view: " + this.getIntersection().toString() + "\n")
        .append("    normal in view: " + this.getNormal().toString() + "\n")
        .append("    from refraction in view: " + this.getFromRefraction() + "\n")
        .append("    to refraction in view: " + this.getToRefraction() + "\n")
        .append("    flip normal: " + this.getFlipNormal() + "\n");
    return str.toString();
  }
}
