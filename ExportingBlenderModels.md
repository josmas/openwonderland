# Exporting Blender Models #

By Matthew Schmidt

Here are instructions for exporting static models (not for animated ones) from Blender in order to import them into Wonderland.

1. From the File menu, select Export-->Collada 1.4 (dae). You are presented with an export dialog.

2. Choose a directory to export to in the "Export File:" field

3. Select "Triangles" (not Polygons)

4. Do not select "Only Export Selection" (we're exporting the entire model), "Bake Matrices", "Sample Animation", "Disable Physics", or "Only Current Scene".

5. Select "Use Relative Paths"

6. Select "Use UV Image Mats"

7. Finally, select "Export"

I don't recall specifically, but you may need to manually edit the exported .DAE in order to point it to your textures.