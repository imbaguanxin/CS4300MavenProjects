package sgraph;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.Stack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import util.Light;
import util.TextureImage;

public class LightScenegraphRenderer extends GL3ScenegraphRenderer {

  protected int lightNum;

  public LightScenegraphRenderer() {
    super();
    lightNum = 0;
  }

  @Override
  public void draw(INode root, Stack<Matrix4f> modelView, Map<Light, Matrix4f> passedInLights) {
    root.draw(this, modelView, passedInLights);
  }

  @Override
  public void drawLight(Map<Light, Matrix4f> passedInLights) {
    //System.out.println("draw light");
    GL3 gl = glContext.getGL().getGL3();
    System.out.println(passedInLights.toString());
    FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
    FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);

    for (Map.Entry<Light, Matrix4f> entry : passedInLights.entrySet()) {
//      System.out.println("draw single light");
      Light light = entry.getKey();
      Matrix4f lightTransForm = entry.getValue();
//      System.out.println("position:" + light.getPosition());
//      System.out.println("Direction:" + light.getSpotDirection());
//      System.out.println(lightTransForm);
      Vector4f lightPosition = lightTransForm.transform(new Vector4f(light.getPosition()));
      Vector4f lightDirection = lightTransForm.transform(new Vector4f(light.getSpotDirection()));
//      System.out.println("position:" + lightPosition);
//      System.out.println("Direction:" + lightDirection);
      LightLocation lightLoc = new LightLocation();
      String lightName = "light[" + lightNum + "]";
      System.out.println("lightNum: " + lightNum);
      System.out.println(lightTransForm);
      lightNum++;
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
      //System.out.println(light.getSpotCutoff());
    }
    gl.glUniform1i(shaderLocations.getLocation("numLights"), 1);
    lightNum = 0;
  }

  @Override
  public void drawMesh(String name, util.Material material, String textureName,
      final Matrix4f transformation) {
    if (meshRenderers.containsKey(name)) {
      GL3 gl = glContext.getGL().getGL3();
      FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
      FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);

      if (textures != null && textures.containsKey(textureName)) {
//        System.out.println(textureName);
//        System.out.println(textures.get(textureName));
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glActiveTexture(GL.GL_TEXTURE1);
        gl.glUniform1i(shaderLocations.getLocation("texture"), 1);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        //gl.glActiveTexture(GL.GL_TEXTURE2);
        gl.glUniform1i(shaderLocations.getLocation("texture"), 0);
        Texture texture = textures.get(textureName).getTexture();
//        System.out.println("texture: " + texture.toString());

        texture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        texture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        texture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        texture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
//        System.out.println(gl.glGetError());
//        System.out.println("after setTexparameteri");

        Matrix4f textureTrans = new Matrix4f();
        if (texture.getMustFlipVertically())
        //for
        // flipping the
        // image vertically
        {
          textureTrans = new Matrix4f().translate(0, 1, 0).scale(1, -1, 1);
        } else {
          textureTrans = new Matrix4f();
        }
        gl.glUniformMatrix4fv(shaderLocations.getLocation("texturematrix"), 1, false,
            textureTrans.get(fb16));
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
  public void setLightNum(int num) {
    this.lightNum = num;
  }
}
