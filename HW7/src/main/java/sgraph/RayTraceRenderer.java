package sgraph;

import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import rayTracer.HitRecord;
import rayTracer.ThreeDRay;

public class RayTraceRenderer extends LightScenegraphRenderer {

  public RayTraceRenderer() {
    super();
  }

  @Override
  public List<HitRecord> checkHit(String objectName, ThreeDRay ray, Matrix4f modelView) {
    List<HitRecord> result = new ArrayList<>();

    return result;
  }

}
