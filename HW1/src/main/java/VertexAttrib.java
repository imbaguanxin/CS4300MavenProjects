import org.joml.Vector4f;


/**
 * This class represents the attributes of a single vertex. It is useful in building PolygonMesh
 * objects for many examples.
 * <p>
 * It implements the IVertexData interface so that it can be converted into an array of floats, to
 * work with OpenGL buffers
 */
public class VertexAttrib implements util.IVertexData {

  private Vector4f position;

  public VertexAttrib() {
    position = new Vector4f(0, 0, 0, 1);
  }

  @Override
  public boolean hasData(String attribName) {
    switch (attribName) {
      case "position":
        return true;
      default:
        return false;
    }
  }

  @Override
  public float[] getData(String attribName) throws IllegalArgumentException {
    float[] result;
    switch (attribName) {
      case "position":
        result = new float[4];
        result[0] = position.x;
        result[1] = position.y;
        result[2] = position.z;
        result[3] = position.w;
        break;
      default:
        throw new IllegalArgumentException("No attribute: " + attribName + " found!");

    }
    return result;
  }

  @Override
  public void setData(String attribName, float[] data) throws IllegalArgumentException {
    switch (attribName) {
      case "position":
        position = new Vector4f(0, 0, 0, 1);
        switch (data.length) {
          case 4:
            position.w = data[3];
          case 3:
            position.z = data[2];
          case 2:
            position.y = data[1];
          case 1:
            position.x = data[0];
            break;
          default:
            throw new IllegalArgumentException("Too much data for attribute: " + attribName);
        }
        break;
      default:
        throw new IllegalArgumentException("Attribute: " + attribName + " unsupported!");
    }
  }

  @Override
  public String[] getAllAttributes() {
    return new String[]{"position"};
  }
}
