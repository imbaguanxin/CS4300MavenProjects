package rayTracer;

import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * This class represents a ray in 3d space.
 */
public class ThreeDRay {

  private Vector3f startingPoint;
  private Vector3f direction;

  /**
   * Construct a ray with all variables in float.
   *
   * @param sx x component of the starting point
   * @param sy y component of the starting point
   * @param sz z component of the starting point
   * @param vx x component of the direction
   * @param vy y component of the direction
   * @param vz z component of the direction
   */
  public ThreeDRay(float sx, float sy, float sz, float vx, float vy, float vz) {
    this.startingPoint = new Vector3f(sx, sy, sz);
    this.direction = new Vector3f(vx, vy, vz);
  }

  /**
   * Construct a ray with all variables in Vector3f.
   *
   * @param startingPoint the starting point of ray
   * @param direction the direction of ray
   */
  public ThreeDRay(Vector3f startingPoint, Vector3f direction) {
    this.startingPoint = new Vector3f(startingPoint.x, startingPoint.y, startingPoint.z);
    this.direction = new Vector3f(direction.x, direction.y, direction.z);
  }

  public Vector4f getStartingPoint() {
    return new Vector4f(startingPoint.x, startingPoint.y, startingPoint.z, 1);
  }

  public void setStartingPoint(float sx, float sy, float sz) {
    this.startingPoint = new Vector3f(sx, sy, sz);
  }

  public Vector4f getDirection() {
    return new Vector4f(direction.x, direction.y, direction.z, 0);
  }

  public void setDirection(Vector4f direction) {
    if (direction.w != 0) {
      System.out.println("Given Direction is not a direction!, use x, y, z as direction");
    }
    this.direction = new Vector3f(direction.x, direction.y, direction.z);
  }

  public void setDirection(float vx, float vy, float vz) {
    this.direction = new Vector3f(vx, vy, vz);
  }

  public String toString() {
    return "start:" + this.getStartingPoint().toString() + " dir: " + this.getDirection()
        .toString();
  }
}
