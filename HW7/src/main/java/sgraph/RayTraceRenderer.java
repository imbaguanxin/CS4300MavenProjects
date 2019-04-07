package sgraph;

import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import rayTracer.HitRecord;
import rayTracer.ThreeDRay;
import util.Material;
import util.TextureImage;

public class RayTraceRenderer extends LightScenegraphRenderer {

  public RayTraceRenderer() {
    super();
  }

  @Override
  public List<HitRecord> checkHit(String objectName, ThreeDRay ray, Matrix4f modelView,
      Material mat, String textureName) {
    List<HitRecord> result = new ArrayList<>();
    switch (objectName) {
      case "sphere":
        result.addAll(
            checkHitSphere(
                ray.getStartingPoint(),
                ray.getDirection(),
                new Matrix4f(modelView),
                mat,
                textureName));
        break;
      case "box":
        result.addAll(
            checkHitBox(
                ray.getStartingPoint(),
                ray.getDirection(),
                new Matrix4f(modelView),
                mat,
                textureName));
        break;
      case "cylinder":
        result.addAll(
            checkHitCylinder(
                ray.getStartingPoint(),
                ray.getDirection(),
                new Matrix4f(modelView),
                mat,
                textureName));
        break;
      default:
        System.out.println("Not supported shape: " + objectName);
    }
    return result;
  }

  private List<HitRecord> checkHitCylinder(Vector4f start, Vector4f vector, Matrix4f modelView,
      Material mat, String textureName) {
    List<HitRecord> result = new ArrayList<>();
    Matrix4f invertedMV = new Matrix4f();
    modelView.invert(invertedMV);
    Vector4f s = new Vector4f();
    Vector4f v = new Vector4f();
    invertedMV.transform(start, s);
    invertedMV.transform(vector, v);

    // intersect with the top surface
    float tTop = (1 - s.y) / v.y;
    float topInterX = s.x + tTop * v.x;
    float topInterZ = s.z + tTop * v.z;
    float topDisSq = topInterX * topInterX + topInterZ * topInterZ;
    if (topDisSq <= 1) {
      HitRecord topHit = new HitRecord();
      topHit.setT(tTop);
      // set material
      topHit.setMaterial(mat);

      // intersection in View
      Vector4f intersectionInView = new Vector4f(start).add(new Vector4f(vector).mul(tTop));
      topHit.setIntersection(intersectionInView);

      Vector4f normal = new Vector4f(0, 1, 0, 0);
      Matrix4f invTranspose = new Matrix4f(modelView).transpose().invert();
      invTranspose.transform(normal);
      topHit.setNormal(normal.x, normal.y, normal.z);

      TextureImage image = this.textures.get(textureName);
      if (!textureName.equals("") && image != null) {
        topHit.setTextureImage(image);
        topHit.setTextureCoordinate(0, 0);
      }

      result.add(topHit);
    }

    // intersect with the bottom surface
    float tBot = -s.y / v.y;
    float botInterX = s.x + tBot * v.x;
    float botInterZ = s.z + tBot * v.z;
    float botDisSq = botInterX * botInterX + botInterZ * botInterZ;
    if (botDisSq <= 1) {
      HitRecord botHit = new HitRecord();
      botHit.setT(tBot);
      // set material
      botHit.setMaterial(mat);

      // intersection in View
      Vector4f intersectionInView = new Vector4f(start).add(new Vector4f(vector).mul(tBot));
      botHit.setIntersection(intersectionInView);

      Vector4f normal = new Vector4f(0, -1, 0, 0);
      Matrix4f invTranspose = new Matrix4f(modelView).transpose().invert();
      invTranspose.transform(normal);
      botHit.setNormal(normal.x, normal.y, normal.z);

      TextureImage image = this.textures.get(textureName);
      if (!textureName.equals("") && image != null) {
        botHit.setTextureImage(image);
        botHit.setTextureCoordinate(0, 0);
      }

      result.add(botHit);
    }

    float a = v.x * v.x + v.z * v.z;
    float b = 2 * (v.x * s.x + v.z * s.z);
    float c = s.x * s.x + s.z * s.z - 1;
    float delta = b * b - 4 * a * c;
    if (delta >= 0) {
      float tsmall = (float) (-b - Math.sqrt(delta)) / (2 * a);
      float tBig = (float) (-b - Math.sqrt(delta)) / (2 * a);
      float t;
      if (tsmall >= 0) {
        t = tsmall;
      } else {
        t = tBig;
      }

      float y = s.y + t * v.y;
      if (y <= 1 && y >= 0) {
        HitRecord hit = new HitRecord();
        hit.setT(t);
        // set material
        hit.setMaterial(mat);

        // intersection in View
        Vector4f intersectionInView = new Vector4f(start).add(new Vector4f(vector).mul(t));
        hit.setIntersection(intersectionInView);

        // set normal
        Vector4f intersection = new Vector4f(s).add(new Vector4f(v).mul(t));
        Vector4f normal = new Vector4f(intersection.x, 0, intersection.z,0);
        Matrix4f invTranspose = new Matrix4f(modelView).transpose().invert();
        invTranspose.transform(normal);
        hit.setNormal(normal.x, normal.y, normal.z);

        TextureImage image = this.textures.get(textureName);
        if (!textureName.equals("") && image != null) {
          hit.setTextureImage(image);
          hit.setTextureCoordinate(0, 0);
        }

        result.add(hit);
      }


    }

    return result;
  }

  private List<HitRecord> checkHitBox(Vector4f start, Vector4f vector, Matrix4f modelView,
      Material mat, String textureName) {

    List<HitRecord> result = new ArrayList<>();
    Matrix4f invertedMV = new Matrix4f();
    modelView.invert(invertedMV);
    Vector4f s = new Vector4f();
    Vector4f v = new Vector4f();
    invertedMV.transform(start, s);
    invertedMV.transform(vector, v);

    float txMin = Math.min((-0.5f - s.x) / v.x, (0.5f - s.x) / v.x);
    float txMax = Math.max((-0.5f - s.x) / v.x, (0.5f - s.x) / v.x);
    float tyMin = Math.min((-0.5f - s.y) / v.y, (0.5f - s.y) / v.y);
    float tyMax = Math.max((-0.5f - s.y) / v.y, (0.5f - s.y) / v.y);
    float tzMin = Math.min((-0.5f - s.z) / v.z, (0.5f - s.z) / v.z);
    float tzMax = Math.max((-0.5f - s.z) / v.z, (0.5f - s.z) / v.z);

    float tMin = Math.max(Math.max(txMin, tyMin), tzMin);
    float tMax = Math.min(Math.min(txMax, tyMax), tzMax);
    if (tMin <= tMax) {
      // hit point goes in the polygon
      HitRecord hIn = new HitRecord();
      hIn.setT(tMin);

      // set material
      hIn.setMaterial(mat);

      // intersection in View
      Vector4f intersectionInView = new Vector4f(start).add(new Vector4f(vector).mul(tMin));
      hIn.setIntersection(intersectionInView);

      // calculate normal vector
      Vector4f normal = new Vector4f(0, 0, 0, 0);
      // find intersection in obj coordinate system
      Vector4f intersection = new Vector4f(s).add(new Vector4f(v).mul(tMin));
      // find intersection in obj coordinate system
      if (intersection.x == .5f) {
        normal.x = 1f;
      } else if (intersection.x == -.5f) {
        normal.x = -1f;
      }
      if (intersection.y == .5f) {
        normal.y = 1f;
      } else if (intersection.y == -.5f) {
        normal.y = -1f;
      }
      if (intersection.z == .5f) {
        normal.z = 1f;
      } else if (intersection.z == -.5f) {
        normal.z = -1f;
      }
      Matrix4f invTranspose = new Matrix4f(modelView).transpose().invert();
      invTranspose.transform(normal);
      hIn.setNormal(normal.x, normal.y, normal.z);

      // set texture
      TextureImage image = this.textures.get(textureName);
      if (!textureName.equals("") && image != null) {
        hIn.setTextureImage(image);
        float textureX = 0, textureY = 0;
        if (intersection.x == .5f) {
          textureX = .5f + .25f * (intersection.z + .5f);
          textureY = .25f + .25f * (intersection.y + .5f);
        } else if (intersection.x == -.5f) {
          textureX = .25f - .25f * (intersection.z + .5f);
          textureY = .25f + .25f * (intersection.y + .5f);
        } else if (intersection.y == .5f) {
          textureX = .25f + .25f * (intersection.x + .5f);
          textureY = .5f + .25f * (intersection.z + .5f);
        } else if (intersection.y == -.5f) {
          textureX = .25f + .25f * (intersection.x + .5f);
          textureY = .25f - .25f * (intersection.z + .5f);
        } else if (intersection.z == .5f) {
          textureX = 1f - .25f * (intersection.x + .5f);
          textureY = .25f + .25f * (intersection.y + .5f);
        } else if (intersection.z == -.5f) {
          textureX = .25f + .25f * (intersection.x + .5f);
          textureY = .25f + .25f * (intersection.y + .5f);
        }
        hIn.setTextureCoordinate(textureX, -(textureY - 1));
      }
      result.add(hIn);

    }
    return result;
  }

  private List<HitRecord> checkHitSphere(Vector4f start, Vector4f vector, Matrix4f modelView,
      Material mat, String textureName) {
    List<HitRecord> result = new ArrayList<>();
    Matrix4f invertedMV = new Matrix4f();
    modelView.invert(invertedMV);
    Vector4f s = new Vector4f();
    Vector4f v = new Vector4f();
    invertedMV.transform(start, s);
    invertedMV.transform(vector, v);

    float a = v.x * v.x + v.y * v.y + v.z * v.z;
    float b = 2f * (s.x * v.x + s.y * v.y + s.z * v.z);
    float c = s.x * s.x + s.y * s.y + s.z * s.z - 1;

    float delta = b * b - 4 * a * c;
    if (delta >= 0) {
      float t1 = (-b + (float) Math.sqrt(delta)) / (2 * a);
      float t2 = (-b - (float) Math.sqrt(delta)) / (2 * a);
      float t;
      if (t2 >= 0) {
        t = t2;
      } else {
        t = t1;
      }
      // hit point
      HitRecord hIn = new HitRecord();
      hIn.setT(t);

      // set material
      hIn.setMaterial(mat);

      // set intersection in View
      Vector4f intersectionInView = new Vector4f(start).add(new Vector4f(vector).mul(t));
      hIn.setIntersection(intersectionInView);

      // compute normal vector in view coordinate
      // intersection in obj coordinate system
      Vector4f intersection = new Vector4f(s).add(new Vector4f(v).mul(t));
      // normal vector in obj coordinate system
      Vector4f normal = new Vector4f(intersection);
      Matrix4f invTranspose = new Matrix4f(modelView).transpose().invert();
      invTranspose.transform(normal);
      hIn.setNormal(normal.x, normal.y, normal.z);

      // set texture
      TextureImage image = this.textures.get(textureName);
      if (!textureName.equals("") && image != null) {
        hIn.setTextureImage(image);
        float phi = (float) Math.asin(-intersection.y);
        float theta = (float) Math.atan2(intersection.z, -intersection.x);
        float imageT = phi / (float) Math.PI + .5f;
        float imageS = theta / (2 * (float) Math.PI) + .5f;
//        System.out.println("t: " + imageT + "s: " + imageS);
        hIn.setTextureCoordinate(imageS, imageT);
      }
//      Vector4f getNorm = hIn.getNormal();
//      if (Math.abs(getNorm.x) < 0.1 && Math.abs(getNorm.y) < 0.1) {
//        System.out.println("sphere");
//        System.out.println("point:" + hIn.getIntersection());
//        System.out.println("normal: " + hIn.getNormal());
//      }

      // add to list
      result.add(hIn);
    }
    return result;
  }

}
