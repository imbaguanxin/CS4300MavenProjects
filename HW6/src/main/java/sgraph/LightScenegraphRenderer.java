package sgraph;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import util.Light;
import util.TextureImage;

public class LightScenegraphRenderer extends GL3ScenegraphRenderer {

  private int numLight;

  public LightScenegraphRenderer() {
    super();
    numLight = 0;
  }

  /**
   * Draw the scene graph rooted at supplied node using the supplied modelview stack. This is
   * usually called by the scene graph
   */
  @Override
  public void draw(INode root, Stack<Matrix4f> modelView) {
    root.draw(this, modelView);
  }


  /**
   * This is a method should be called before draw to enable the light in the scenegraph
   *
   * @param root The passed in root directs the renderer to find all lights
   * @param modelView Similar to draw, this is a stack of modelView.
   */
  @Override
  public void lightOn(INode root, Stack<Matrix4f> modelView) {
    Stack<Matrix4f> mvCopy = new Stack<>();
    for (Matrix4f mv : modelView) {
      mvCopy.push(new Matrix4f(mv));
    }
    Map<Light, Matrix4f> lights = root.getLights(mvCopy);
    this.lightOn(lights);
  }


  /**
   * Draw extra passed in lights not from xml. This was originally used to debug.
   *
   * @param mv The world to view modelView.
   * @param lights The passed in extra lights from View.
   */
  @Override
  public void drawSceneLight(Matrix4f mv, List<Light> lights) {
    Map<Light, Matrix4f> lightMap = new HashMap<>();
    for (Light light : lights) {
      lightMap.put(light, new Matrix4f(mv));
    }
    this.lightOn(lightMap);
  }

  /**
   * This is the actual method that renders all the lights.
   *
   * @param lights All the lights needed to be drew
   */
  private void lightOn(Map<Light, Matrix4f> lights) {
    GL3 gl = glContext.getGL().getGL3();
    FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);
    LightLocation lightLoc = new LightLocation();
    for (Entry<Light, Matrix4f> entry : lights.entrySet()) {
      Light light = entry.getKey();
      Matrix4f lightTransForm = entry.getValue();
      Vector4f lightPosition = lightTransForm.transform(new Vector4f(light.getPosition()));
      Vector4f lightDirection = lightTransForm.transform(new Vector4f(light.getSpotDirection()));
      String lightName = "light[" + numLight + "]";
      lightLoc.position = shaderLocations.getLocation(lightName + ".position");
      lightLoc.ambient = shaderLocations.getLocation(lightName + ".ambient");
      lightLoc.diffuse = shaderLocations.getLocation(lightName + ".diffuse");
      lightLoc.specular = shaderLocations.getLocation(lightName + ".specular");
      lightLoc.direction = shaderLocations.getLocation(lightName + ".direction");
      lightLoc.cutOff = shaderLocations.getLocation(lightName + ".cutOff");

      gl.glUniform4fv(lightLoc.position, 1, lightPosition.get(fb4));
      gl.glUniform3fv(lightLoc.ambient, 1, light.getAmbient().get(fb4));
      gl.glUniform3fv(lightLoc.diffuse, 1, light.getDiffuse().get(fb4));
      gl.glUniform3fv(lightLoc.specular, 1, light.getSpecular().get(fb4));
      gl.glUniform4fv(lightLoc.direction, 1, lightDirection.get(fb4));
      gl.glUniform1f(lightLoc.cutOff, light.getSpotCutoff());
      numLight++;
    }
    gl.glUniform1i(shaderLocations.getLocation("numLights"), numLight);
  }

  /**
   * This is a overrided version of mesh drawer. Passed in all material information to shader
   */
  @Override
  public void drawMesh(String name, util.Material material, String textureName,
      final Matrix4f transformation) {
    if (meshRenderers.containsKey(name)) {
      GL3 gl = glContext.getGL().getGL3();
      FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
      FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);

      if (textures != null && textures.containsKey(textureName)) {
        gl.glEnable(gl.GL_TEXTURE_2D);
        gl.glActiveTexture(gl.GL_TEXTURE0);
        gl.glUniform1i(shaderLocations.getLocation("image"), 0);

        Texture texture = textures.get(textureName).getTexture();

        texture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        texture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        texture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        texture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

        texture.setMustFlipVertically(true);
        Matrix4f textureTrans = new Matrix4f();
        if (texture.getMustFlipVertically()) { //for flipping the image vertically
          textureTrans = new Matrix4f().translate(0, 1, 0).scale(1, -1, 1);
        }
        gl.glUniformMatrix4fv(shaderLocations.getLocation("texturematrix"), 1, false,
            textureTrans.get(fb16));
        gl.glDisable(gl.GL_TEXTURE_2D);
        texture.bind(gl);
      }

      //get the color

      //set the color for all vertices to be drawn for this object

      int loc = shaderLocations.getLocation("material.ambient");
      if (loc < 0) {
        throw new IllegalArgumentException("No shader variable for \" material.ambient \"");
      }
      gl.glUniform3fv(loc, 1,
          material.getAmbient().get(fb4));

      loc = shaderLocations.getLocation("material.diffuse");
      if (loc < 0) {
        throw new IllegalArgumentException("No shader variable for \" material.diffuse \"");
      }
      gl.glUniform3fv(loc, 1,
          material.getDiffuse().get(fb4));

      loc = shaderLocations.getLocation("material.specular");
      if (loc < 0) {
        throw new IllegalArgumentException("No shader variable for \" material.specular \"");
      }
      gl.glUniform3fv(loc, 1,
          material.getSpecular().get(fb4));

      loc = shaderLocations.getLocation("material.shininess");
      if (loc < 0) {
        throw new IllegalArgumentException("No shader variable for \" material.shininess \"");
      }
      gl.glUniform1f(loc, material.getShininess());

      loc = shaderLocations.getLocation("modelview");
      if (loc < 0) {
        throw new IllegalArgumentException("No shader variable for \" modelview \"");
      }
      gl.glUniformMatrix4fv(loc, 1, false, transformation.get(fb16));

      loc = shaderLocations.getLocation("normalmatrix");
      if (loc < 0) {
        throw new IllegalArgumentException("No shader variable for \" normalmatrix \"");
      }
      Matrix4f normalmatrix = new Matrix4f(transformation);
      normalmatrix = normalmatrix.invert().transpose();
      gl.glUniformMatrix4fv(shaderLocations.getLocation("normalmatrix"), 1,
          false, normalmatrix.get(fb16));

      meshRenderers.get(name).draw(glContext);
    }
  }

  @Override
  public void zeroNumLight() {
    this.numLight = 0;
  }
}
