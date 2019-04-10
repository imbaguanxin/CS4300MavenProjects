# Assignment 7

## Set Config Files to Run the Code

  All the config files are under `src/main/resources/configs`

  For example, just copy `src/main/resources/configs/camera.config` to configuration and the code is going to run.

  `camera.config` is displaying the final rendering. Camera position: (0,0,30) looking at (0,0,0).

  We are rendering a die and an earth.

  Similarly, other 3 files work as well, including `camera.config`, `box.config`, `sphere.config` and `light.config`.

## Final rendering
  This is a render from ray tracer
  ![final_rendering](final_rendering_rayTracer.png)
  This is a render from OpenGL
  ![final_rendering_opengl](final_rendering_OpenGL.png)

## box rendering
  There are two box in this scene. One has texture, the other doesn't.
  This is a render from ray tracer
  ![box](box_rayTracer.png)
  This is a render from OpenGL
  ![box_openGL](box_OpenGL.png)

## sphere rendering

  There are three spheres in this scene.
  This is a render from ray tracer
  ![sphere](sphere_rayTracer.png)
  This is a render from OpenGL
  ![sphere_openGL](sphere_OpenGL.png)

## light works
  There is a spot light and a directional light
  This is a render from ray tracer
  ![light](light_works_rayTracer.png)

## Texture works
  This is a render from ray tracer
  ![texture](texture_rayTracer.png)

**You may find that the map is inversed in our renderings. We have asked this problem in Piazza and we were told to fit our image with OpenGL even though OpenGL is rendering the wrong one. We were originally rendering the "correct" image and inverted our rendering after reading the Piazza post.**

Textures coordinates in obj files and corresponding texture images are using upper left corner of image as the origin, while OpenGL is setting the origin at the lower left. In order to have boxes to have texture on all sides, we can just simply flip the image upside down. However, this operation will also be applied spheres and thus cause inverted image. According to the [piazza post](https://piazza.com/class/jqmk9wn2e48c4?cid=196), our ray tracer now simulates OpenGL to have its origin at the lower left corner.
