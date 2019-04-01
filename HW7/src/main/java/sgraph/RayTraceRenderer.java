package sgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import rayTracer.HitRecord;
import rayTracer.ThreeDRay;
import util.ObjectInstance;

public class RayTraceRenderer extends LightScenegraphRenderer {

  public RayTraceRenderer() {
    super();
  }

  @Override
  public List<HitRecord> checkHit(String objectName, ThreeDRay ray, Matrix4f modelView) {
    List<HitRecord> result = new ArrayList<>();
    ObjectInstance obj = super.meshRenderers.get(objectName);

    return result;
  }

  private List<HitRecord> checkHitSpecial(Matrix4f modelView, ThreeDRay ray, String type) {
    List<HitRecord> result = new ArrayList<>();
    Matrix4f invertedMV = modelView.invert();
    Vector4f s = invertedMV.transform(ray.getStartingPoint());
    Vector4f v = invertedMV.transform(ray.getDirection());
    if (type.equals("polygon")) {
      result.addAll(checkHitPolygon(s, v));
    } else if (type.equals("sphere")) {
      result.addAll(checkHitSphere(s, v));
    }
    return result;
  }

  private List<HitRecord> checkHitPolygon(Vector4f s, Vector4f v) {
    List<HitRecord> result = new ArrayList<>();
    float txMin = (-.5f - s.x) / v.x;
    float txMax = (.5f - s.x) / v.x;
    float tyMin = (-.5f - s.y) / v.y;
    float tyMax = (.5f - s.y) / v.y;
    float tzMin = (-.5f - s.z) / v.z;
    float tzMax = (.5f - s.z) / v.z;
    float tMin = Math.max(Math.max(txMin, tyMin), tzMin);
    float tMax = Math.min(Math.min(txMax, tyMax), tzMax);
    if (tMin <= tMax) {
      // hit point goes in the polygon
      HitRecord hIn = new HitRecord();
      hIn.setT(tMin);
      // hit point goes out the polygon
      HitRecord hOut = new HitRecord();
      hOut.setT(tMax);
      // add to list
      result.add(hIn);
      result.add(hOut);
    }
    return result;
  }

  private List<HitRecord> checkHitSphere(Vector4f s, Vector4f v) {
    List<HitRecord> result = new ArrayList<>();

    float a = (float) (Math.pow(v.x, 2) + Math.pow(v.y, 2) + Math.pow(v.z, 2));
    float b = 2f * ((s.x * v.x) + s.y * v.y + s.z * v.z);
    float c = s.x * s.x + s.y * s.y + s.z * s.z - 1;

    float delta = b * b - 4 * a * c;
    if (delta >= 0) {
      float t1 = (-b + (float) Math.sqrt(delta)) / (2 * a);
      float t2 = (-b - (float) Math.sqrt(delta)) / (2 * a);
      // hit point 1
      HitRecord hIn = new HitRecord();
      hIn.setT(t1);
      // hit point 2
      HitRecord hOut = new HitRecord();
      hOut.setT(t2);
      // add to list
      result.add(hIn);
      result.add(hOut);
    }
    return result;
  }

}
