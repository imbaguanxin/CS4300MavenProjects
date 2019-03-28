# CS4300 HW6

## Set Config Files to Run the Code

  All the config files are under `src/main/resources/configs`

  For example, just copy `src/main/resources/configs/old_hall_drone.config` to configuration and the code is going to run.

  `old_hall_drone.config` is displaying the drone. The main window is showing the global view and the small top right window is showing the drone camera's view.

  Similarly, other 3 files work as well, including `camera.config`, `tower_global.config`, `tower_drone.config` and `YMCA_global.config`.

### Configuration file

  A template for a valid config file should looks like this:

```
path [path to the xml scene graph file]
mode [GLOBAL/MOVING]
fix-position [x] [y] [z]
fix-center [x] [y] [z]
move-position [x] [y] [z]
move-center [x] [y] [z]
```
  The fix/move-position specifies the position of the camera in global/drone view; and the fix/move-center specifies the point the camera is facing in global/drone view. The up direction of the camera is set to the positive-y direction by default.

## Camera Operations

  There are two different mode to observe in this virtual world. You can switch between the two using `Space` key on your keyboard.

  1. Global View

  In the global view, you can observe this virtual world from a constant distance, which is defined by the config file you passed in. In this view you can use your mouse to drag the world like a trackball.

  2. Drone View

  In the drone view, you can view this virtual world through a camera mounted   on an unmanned drone flying around. Press `up`, `down`, `left`, `right` to move the drone. Press `w`, `a`, `s`, `d` to turn the direction of the camera. You can also use `f`, `c` to make the camera to slope to the left or right. Press `+`, `-` to zoom in or out, but notice there is a limitation on zooming too far or too near.

## New Features

  1. Lighting

  Lighting in scene graph can be specified as either a spotlight or a parallel light.

  Spotlight should be specified with a direction and an angle of spot. If a direction of spot is provided but not the angle of spot, this light will shine to all directions. If an angle of spot is provided but not the direction of spot, this light will have undefined behavior.

  Parallel light, or say, directional light, should be specified with a direction but with no position nor an angle of spot. Providing an angle of spot to a directional light would cause undefined behavior.

  2. Daylight in the scene

  Besides all the light comes with the Scene graph, our scene have a white daylight shining from the above, i.e. shining to the negative-y direction.

  3. Light mounted with camera

  On the camera mounted a spot light, which will rotate and move as the camera rotate and move. I.e. this light is static in the view coordinate system.

  4. Texture

  Each object in the scene graph can have a single texture attribute, but not multiple. The texture will be mapped to the object according to the texture mapping coordinate specified by the object file. The default texture is a picture filled with white pixels, if no specific texture is specified.

  Two buildings are updated now to have textures and each have some lights on it.

## Design of the code

  1. Light

  Lights are stored in the list kept by the node they are attached to. When drawing the light, class `View` calls the renderer to draw the day light of the scene and then calls class `Scenegraph` to draw all lights specifically. If you are to turn off the day light, you can just comment out that call, `renderer.drawSceneLight()`. Class `Scenegraph` will then call renderer to collect all lights from nodes, each light is combined with a transformation that is to be applied to the position and the direction of this light; and then draw them. All OpenGl related work is done in the renderer.

  After all lights are drawn, `View` then calls class `Scenegraph` to draw all the meshes. Drawing a mesh before drawing light will cause that mesh to not be lighten by lights drawn after it.

  Since camera and the drone is not in the same scene graph as other object in the scene. For drawing the flying drone correctly, lights on the drone must be switched on before any mesh in the scene is drawn. This might be a major draw-back of our code.

  Light in this model can shine through everything. There is no shades in the scene, unfortunately.

  2. Texture

  When drawing an object, a material and a texture name will be passed with the object name to the renderer. Renderer will then send all information in material down to the shader and bind the matching texture to texture id 0 after setting needed parameters so that the shader will be using the bound texture image.

## Scene

  1. tower.xml

  There are some textures tied to the scenegraph as well. The pillar of the first floor has a texture of a checkerboard. Glasses are broken. The big ball at the top of the building has a texture of moon surface. Also, all the walls share a texture of metal.

  Due to materials defined in this scene graph, there may be some angle of view that will make part of the building totally black. This phenomenon is expected.

  2. old-hall.xml

  All walls are mapped with a texture of bricks. The roof is mapped with a texture of tiles. Pillars and sills are not specifically textured because they are designed to have a texture of white. Window-grids are also not mapped to any textured since the structure is too tiny.

  Materials of object in this scene are not changed, still defining materials with `color` tag. Thus, this building is backward compatible. The process parsing `color` tag in xml file is also modified to support this. When a `color` tag is read, three values in `color` tag will be scalar factored and stored in ambient, diffuse, and specular.

  3. town-hall.xml

  This scene consist two buildings together, where the tower is set at the back of the old hall. Beside a spot light that is tied to the camera, four extra lighting is added, two for each building.

  On the tower, the first one is a spot light at the back of the building. It is a reddish light focus on the second floor. The Other one is a white parallel light shinning towards (-1,-1,0) which simulates the sun.

  On the old hall, One is a white spot light in front of the tower at the roof, facing downwards. Another one is also a white spot light but above the right part of the roof, shining downwards, which can be clearly visible if looking from the back side of the hall.

## Citation

  1. tile.jpg, 3D texture library,  http://www.3dczk.com/map1/wa/2016/0620/3491.html

  2. AmitShesh.jpg, Northeastern University, https://www.khoury.northeastern.edu/people/amit-shesh/

  3. bricks.jpg, Pinterest, https://www.pinterest.com/pin/431078995573218874/

  4. glass.jpg, Storyblocks, https://br.storyblocks.com/stock-image/broken-glass-texture-close-up-isolated-on-white-ru12asn4qjk80pkrx

  5. moonmap.jpg, cgtrader, https://www.cgtrader.com/3d-models/space/planet/92k-moon-displacement-map

  6. brushedMetal.jpg, textures4photoshop, http://www.textures4photoshop.com/tex/metal/brushed-stainless-steel-metal-texture-free.aspx
