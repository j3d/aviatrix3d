/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.loader.discreet;

// External imports
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Point3d;
import org.j3d.maths.vector.Vector3d;
import org.j3d.util.I18nManager;
import org.j3d.util.MatrixUtils;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.loaders.discreet.*;

import org.j3d.renderer.aviatrix3d.loader.AVLoader;
import org.j3d.renderer.aviatrix3d.loader.AVModel;

/**
 * A loader implementation capable of loading .3ds files for all revisions.
 * <p>
 *
 * This loader is designed for reusing multiple times to read multiple files.
 * Several internal objects are reused wherever possible to aid in efficient
 * state sorting by the pipeline.
 * <p>
 *
 * <b>Capabilities</b>
 * <p>
 *
 * <ul>
 * <li>Meshes of all types, including multimaterial forms</li>
 * <li>Texturing, though somewhat limited by what multitexturing can do</li>
 * <li>Lights</li>
 * <li>Cameras</li>
 * <li>Background - solid colour only</li>
 * <li>Fog - linear only</li>
 * <li>Layers: 3DS files do not contain rendering layers</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class MaxLoader implements AVLoader
{
	/** Message when we get back a null mesh representation from the loader */
	private static final String UNKNOWN_ERROR_PROP =
		"org.j3d.renderer.aviatrix3d.loader.discreet.MaxLoader.nullMeshMsg";

    /** COnstant for texture coordinate type. Always a single 2D array */
    private static final int[] TEX_COORD_TYPES =
        { VertexGeometry.TEXTURE_COORDINATE_2 };

    /** The currently set load flags */
    private int loadFlags;

    /** Flag defining whether we should keep the internal model too. */
    private boolean keepModel;

    /** The parser used to handle the input stream */
    private MaxParser parser;

    /** Common poly attributes for single-sided lighting */
    private PolygonAttributes oneSidePolyAttrs;

    /** Common poly attributes for two-sided lighting */
    private PolygonAttributes twoSidePolyAttrs;

    /** Common matrix used for making calcs */
    private Matrix4d matrix;

    /** Common vector for calculating the eye point for camera calcs */
    private Point3d eyePosition;

    /** Common vector for calculating the target for camera calcs */
    private Point3d center;

    /** Common vector for calculating local Y Up for camera calcs */
    private Vector3d upVector;

    /** Utility class for generating normals */
    private MaxUtils maxUtils;

    /** Utility class for handling camera matrix generation */
    private MatrixUtils matrixUtils;

    /**
     * List of face flags that have had a material assigned to it. Used only
     * when the geometry has more than one material or a single material that
     * does not cover all the faces.
     */
    private boolean[] facesProcessed;

    /**
     * Create a new instance of the loader.
     */
    public MaxLoader()
    {
        loadFlags = LOAD_ALL;
        keepModel = false;

        matrix = new Matrix4d();
        maxUtils = new MaxUtils();
    }

    //---------------------------------------------------------------
    // Methods defined by AVLoader
    //---------------------------------------------------------------

    /**
     * Load a model from the given URL.
     *
     * @param url The url to load the model from
     * @return A representation of the model at the URL
     * @throws IOException something went wrong while reading the file
     */
    @Override
    public AVModel load(URL url) throws IOException
    {
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();

        ObjectMesh mesh = parse(is);

        if(mesh == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(UNKNOWN_ERROR_PROP);

            throw new IOException(msg);
        }

        return convertMeshToModel(mesh);
    }

    /**
     * Load a model from the given input stream. If the file format would
     * prefer to use a {@link java.io.Reader} interface, then use the
     * {@link java.io.InputStreamReader} to convert this stream to the desired
     * type. The caller will be responsible for closing down the stream at the
     * end of this process.
     *
     * @param stream The stream to load the model from
     * @return A representation of the model from the stream contents
     * @throws IOException something went wrong while reading the file
     */
    @Override
    public AVModel load(InputStream stream) throws IOException
    {
        ObjectMesh mesh = parse(stream);

        if(mesh == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(UNKNOWN_ERROR_PROP);

            throw new IOException(msg);
        }

        return convertMeshToModel(mesh);
    }

    /**
     * Load a model from the given file.
     *
     * @param file The file instance to load the model from
     * @return A representation of the model in the file
     * @throws IOException something went wrong while reading the file
     */
    @Override
    public AVModel load(File file) throws IOException
    {
        FileInputStream fis = new FileInputStream(file);
        ObjectMesh mesh = parse(fis);

        if(mesh == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(UNKNOWN_ERROR_PROP);

            throw new IOException(msg);
        }

        return convertMeshToModel(mesh);
    }

    /**
     * Set the flags for which parts of the file that should be loaded.
     * The flags are bit-fields, so can be bitwise OR'd together.
     *
     * @param flags The collection of flags to use
     */
    @Override
    public void setLoadFlags(int flags)
    {
        loadFlags = flags;
    }

    /**
     * Get the current set collection of load flags.
     *
     * @return A bitmask of flags that are currently set
     */
    @Override
    public int getLoadFlags()
    {
        return loadFlags;
    }

    /**
     * Define whether this loader should also keep around it's internal
     * representation of the file format, if it has one. If kept, this can be
     * retrieved through the {@link AVModel#getRawModel()} method and cast to
     * the appropriate class type.
     *
     * @param enable true to enable keeping the raw model, false otherwise
     */
    @Override
    public void keepInternalModel(boolean enable)
    {
        keepModel = enable;
    }

    /**
     * Check to see whether the loader should be currently keeping the internal
     * model.
     *
     * @return true when the internal model should be kept
     */
    @Override
    public boolean isInternalModelKept()
    {
        return keepModel;
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Internal method to start the load process.
     *
     * @param is The stream ready to roll for the parsing
     * @throws IOException something went wrong while reading the file
     */
    private ObjectMesh parse(InputStream is)
        throws IOException
    {
        if(parser == null)
            parser = new MaxParser(is);
        else
            parser.reset(is);

        return parser.parse();
    }

    /**
     * Convert the normal mesh object to a model suitable for returning as
     * part of the loader.
     *
     * @param mesh The mesh that needs to be converted
     * @return A model instance that represents the mesh
     */
    private AVModel convertMeshToModel(ObjectMesh mesh)
    {
        maxUtils.calcAllNormals(mesh);

        TransformGroup root_node = new TransformGroup();

        if(mesh.masterScale != 1)
        {
            matrix.setIdentity();
            matrix.set(mesh.masterScale);
            root_node.setTransform(matrix);
        }

        MaxModel model = new MaxModel(root_node, keepModel ? mesh : null);

        // Go through the material library section. If anything has a name,
        // create a material object for it and register it with the model. Do
        // this first so that any geometry that uses the named material will get
        // to use the same instance. Good for state sorting optimisation.
        for(int i = 0; i < mesh.numMaterials; i++)
            processMaterialBlock(model, mesh.materials[i]);

        // Now process the geometry section
        for(int i = 0; i < mesh.numBlocks; i++)
            processObjectBlock(model, mesh.blocks[i]);

        if((loadFlags & RUNTIMES) != 0)
        {
            for(int i = 0; i < mesh.numKeyframes; i++)
                processKeyframeBlock(model, mesh.keyframes[i]);
        }

        return model;
    }

    /**
     * Process a single material block instance and register it with the
     * model. If the material has a name (they all should being part of the
     * library) then create an object instance and register it as a named
     * object.
     *
     * @param model The model instance to register everything with
     * @param material The material block instance to process
     */
    private void processMaterialBlock(MaxModel model, MaterialBlock material)
    {
        if((material.name == null) || (material.name.length() == 0))
            return;

        Appearance app = new Appearance();

        model.addNamedObject(material.name, app);

        Material mat = new Material();
        app.setMaterial(mat);

        // TODO:
        // May want to check on how this actually maps to real shininess.
        mat.setShininess(material.shininessStrength);
        mat.setTransparency(1 - material.transparency);

        if(material.ambientColor != null)
            mat.setAmbientColor(material.ambientColor);

        if(material.diffuseColor != null)
            mat.setDiffuseColor(material.diffuseColor);

        if(material.specularColor != null)
            mat.setSpecularColor(material.specularColor);

        // TODO:
        // Wireframe rendering mode not handled for now.
        // Shading type not handled for now

        if(material.twoSidedLighting)
        {
            if(twoSidePolyAttrs == null)
            {
                twoSidePolyAttrs = new PolygonAttributes();
                twoSidePolyAttrs.setTwoSidedLighting(true);
                twoSidePolyAttrs.setSeparateSpecular(true);
            }

            app.setPolygonAttributes(twoSidePolyAttrs);
        }
        else
        {
            if(oneSidePolyAttrs == null)
            {
                oneSidePolyAttrs = new PolygonAttributes();
                oneSidePolyAttrs.setSeparateSpecular(true);
            }

            app.setPolygonAttributes(oneSidePolyAttrs);
        }

        TextureUnit[] tex_units = new TextureUnit[5];
        int num_tex_units = 0;

        if((material.bumpMap != null) && (material.bumpMap.filename != null))
        {
            TextureBlock tb = material.bumpMap;

            TextureUnit tu = new TextureUnit();

            // if any of these are non-default, setup a transform matrix,
            // otherwise don't bother.
            if((tb.uOffset != 0) || (tb.vOffset != 0) ||
               (tb.uScale != 1) || (tb.vScale != 1) || (tb.angle != 0))
            {
                // TODO: Does not handle the rotation currently
                // Need to set up two matrices that rotate then translate.

                matrix.setIdentity();

                // matrix.rotZ(angle);

                if(tb.uScale != 0)
                    matrix.m00 = tb.uScale;

                if(tb.vScale != 0)
                    matrix.m11 = tb.vScale;

                matrix.m03 = tb.uOffset;
                matrix.m13 = tb.vOffset;

                tu.setTextureTransform(matrix);
            }

            Texture2D tex = new Texture2D();
            tex.setUserData("BUMP_MAP");
            tu.setTexture(tex);

            // If bit 4 set, deactive tiling
            if((tb.tiling & 0x10) != 0)
            {
                tex.setBoundaryModeS(Texture.BM_CLAMP_TO_EDGE);
                tex.setBoundaryModeT(Texture.BM_CLAMP_TO_EDGE);
            }
            else
            {
                tex.setBoundaryModeS(Texture.BM_WRAP);
                tex.setBoundaryModeT(Texture.BM_WRAP);
            }

            model.addExternalObject(tex, tb.filename);

            // Since this is a bump map, set up DOT3 handling.
            TextureAttributes t_attr = new TextureAttributes();

            t_attr.setTextureMode(TextureAttributes.MODE_COMBINE);
            t_attr.setCombineMode(false, TextureAttributes.COMBINE_DOT3_RGB);
            t_attr.setCombineMode(true, TextureAttributes.COMBINE_REPLACE);
            t_attr.setCombineSource(false, 0, TextureAttributes.SOURCE_CURRENT_TEXTURE);
            t_attr.setCombineSource(true, 0, TextureAttributes.SOURCE_CONSTANT_COLOR);

            if(tb.blendColor1 != null)
                t_attr.setBlendColor(tb.blendColor1[0],
                                     tb.blendColor2[1],
                                     tb.blendColor2[2],
                                     1);

            // TODO:
            // See if we can map the tinting and alpha bits from tb.tiling.
            tu.setTextureAttributes(t_attr);

            tex_units[num_tex_units++] = tu;
        }

        if((material.opacityMap != null) && (material.opacityMap.filename != null))
        {
            TextureBlock tb = material.opacityMap;

            TextureUnit tu = new TextureUnit();

            // if any of these are non-default, setup a transform matrix,
            // otherwise don't bother.
            if((tb.uOffset != 0) || (tb.vOffset != 0) ||
               (tb.uScale != 1) || (tb.vScale != 1) || (tb.angle != 0))
            {
                // TODO: Does not handle the rotation currently
                // Need to set up two matrices that rotate then translate.

                matrix.setIdentity();

                // matrix.rotZ(angle);

                if(tb.uScale != 0)
                    matrix.m00 = tb.uScale;

                if(tb.vScale != 0)
                    matrix.m11 = tb.vScale;

                matrix.m03 = tb.uOffset;
                matrix.m13 = tb.vOffset;

                tu.setTextureTransform(matrix);
            }

            Texture2D tex = new Texture2D();
            tex.setUserData("OPACITY_MAP");
            tu.setTexture(tex);

            // If bit 4 set, deactive tiling
            if((tb.tiling & 0x10) != 0)
            {
                tex.setBoundaryModeS(Texture.BM_CLAMP_TO_EDGE);
                tex.setBoundaryModeT(Texture.BM_CLAMP_TO_EDGE);
            }
            else
            {
                tex.setBoundaryModeS(Texture.BM_WRAP);
                tex.setBoundaryModeT(Texture.BM_WRAP);
            }

            model.addExternalObject(tex, tb.filename);

            // Since this is opacity, map, set combine and pass through any RGB
            // values using a replace mode, but replace the current alpha with those
            // from the given texture map.
            TextureAttributes t_attr = new TextureAttributes();

            t_attr.setTextureMode(TextureAttributes.MODE_COMBINE);
            t_attr.setCombineMode(false, TextureAttributes.COMBINE_REPLACE);
            t_attr.setCombineMode(true, TextureAttributes.COMBINE_REPLACE);
            t_attr.setCombineSource(false, 0, TextureAttributes.SOURCE_PREVIOUS_UNIT);
            t_attr.setCombineSource(true, 0, TextureAttributes.SOURCE_CURRENT_TEXTURE);

            // TODO:
            // See if we can map the tinting and alpha bits from tb.tiling.
            tu.setTextureAttributes(t_attr);

            tex_units[num_tex_units++] = tu;
        }

        if((material.textureMap1 != null) && (material.textureMap1.filename != null))
        {
            TextureBlock tb = material.textureMap1;

            TextureUnit tu = new TextureUnit();

            // if any of these are non-default, setup a transform matrix,
            // otherwise don't bother.
            if((tb.uOffset != 0) || (tb.vOffset != 0) ||
               (tb.uScale != 1) || (tb.vScale != 1) || (tb.angle != 0))
            {
                // TODO: Does not handle the rotation currently
                // Need to set up two matrices that rotate then translate.

                matrix.setIdentity();

                // matrix.rotZ(angle);

                if(tb.uScale != 0)
                    matrix.m00 = tb.uScale;

                if(tb.vScale != 0)
                    matrix.m11 = tb.vScale;

                matrix.m03 = tb.uOffset;
                matrix.m13 = tb.vOffset;

                tu.setTextureTransform(matrix);
            }

            Texture2D tex = new Texture2D();
            tex.setUserData("BASE_MAP1");
            tu.setTexture(tex);

            // If bit 4 set, deactive tiling
            if((tb.tiling & 0x10) != 0)
            {
                tex.setBoundaryModeS(Texture.BM_CLAMP_TO_EDGE);
                tex.setBoundaryModeT(Texture.BM_CLAMP_TO_EDGE);
            }
            else
            {
                tex.setBoundaryModeS(Texture.BM_WRAP);
                tex.setBoundaryModeT(Texture.BM_WRAP);
            }

            model.addExternalObject(tex, tb.filename);

            // Since this is a standard texture map modulate this value wit
            // everything we have from beforehand, unless the tiling bit 0x01
            // is set (meaning use replace mode, which is the default).


            // TODO:
            // See if we can map the tinting and alpha bits from tb.tiling.
            TextureAttributes t_attr = new TextureAttributes();
            if((tb.tiling & 0x01) != 0)
                t_attr.setTextureMode(TextureAttributes.MODE_MODULATE);
            else
                t_attr.setTextureMode(TextureAttributes.MODE_REPLACE);

            tu.setTextureAttributes(t_attr);

            tex_units[num_tex_units++] = tu;
        }

        // TOD0: Handle reflection maps. Are they sphere maps or cubic env?

        if((material.specularMap != null) && (material.specularMap.filename != null))
        {
            TextureBlock tb = material.specularMap;

            TextureUnit tu = new TextureUnit();

            // if any of these are non-default, setup a transform matrix,
            // otherwise don't bother.
            if((tb.uOffset != 0) || (tb.vOffset != 0) ||
               (tb.uScale != 1) || (tb.vScale != 1) || (tb.angle != 0))
            {
                // TODO: Does not handle the rotation currently
                // Need to set up two matrices that rotate then translate.

                matrix.setIdentity();

                // matrix.rotZ(angle);

                if(tb.uScale != 0)
                    matrix.m00 = tb.uScale;

                if(tb.vScale != 0)
                    matrix.m11 = tb.vScale;

                matrix.m03 = tb.uOffset;
                matrix.m13 = tb.vOffset;

                tu.setTextureTransform(matrix);
            }

            Texture2D tex = new Texture2D();
            tex.setUserData("SPECULAR_MAP");
            tu.setTexture(tex);

            // If bit 4 set, deactive tiling
            if((tb.tiling & 0x10) != 0)
            {
                tex.setBoundaryModeS(Texture.BM_CLAMP_TO_EDGE);
                tex.setBoundaryModeT(Texture.BM_CLAMP_TO_EDGE);
            }
            else
            {
                tex.setBoundaryModeS(Texture.BM_WRAP);
                tex.setBoundaryModeT(Texture.BM_WRAP);
            }

            model.addExternalObject(tex, tb.filename);

            // Specular maps are treated as an additive source to add highlights
            // the exist object.
            TextureAttributes t_attr = new TextureAttributes();
            t_attr.setTextureMode(TextureAttributes.MODE_ADD);

            // TODO:
            // See if we can map the tinting and alpha bits from tb.tiling.
            tu.setTextureAttributes(t_attr);

            tex_units[num_tex_units++] = tu;
        }

        app.setTextureUnits(tex_units, num_tex_units);
    }

    /**
     * Process a single object block instance according to the various flags
     * provided.
     *
     * @param model The model instance to register everything with
     * @param object The object block instance to process for geometry
     */
    private void processObjectBlock(MaxModel model, ObjectBlock object)
    {
        // process mesh, then lights, then cameras
        Group mesh_group = processObjectMesh(model, object);

        if((loadFlags & LIGHTS) != 0)
            processObjectLights(model, object, mesh_group);

        if((loadFlags & VIEWPOINTS) != 0)
            processObjectCameras(model, object, mesh_group);
    }

    /**
     * Process the mesh part of an object block instance according to the
     * various flags provided.
     *
     * @param model The model instance to register everything with
     * @param object The object block instance to process for geometry
     * @return The grouping node that the mesh was added to, so that lights
     *    and cameras may be placed in the same area
     */
    private Group processObjectMesh(MaxModel model, ObjectBlock object)
    {
        Group root_group = model.getModelRoot();
        Map named_objs = model.getNamedObjects();

        // Grouping node that everything is held.
        Group object_group = new Group();
        root_group.addChild(object_group);

        for(int j = 0; j < object.numMeshes; j++)
        {
            // If there is more than one material declared for this mesh or
            // there is a single material, but it does not cover every face,
            // we need to split it up into the sets of faces that share the
            // material into separate Shape3D instances.

            TransformGroup group = new TransformGroup();
            object_group.addChild(group);
            model.addNamedObject(object.name + '-' + j, group);

            TriangleMesh mesh = object.meshes[j];

/*
            // Set the matrix from the local coords. The values are
            // presented as x-Axis, Y-axis, Z-axis and then origin offset.
            matrix.m00 = mesh.localCoords[0];
            matrix.m01 = mesh.localCoords[1];
            matrix.m02 = mesh.localCoords[2];
            matrix.m03 = mesh.localCoords[9];

            matrix.m10 = mesh.localCoords[3];
            matrix.m11 = mesh.localCoords[4];
            matrix.m12 = mesh.localCoords[5];
            matrix.m13 = mesh.localCoords[10];

            matrix.m20 = -mesh.localCoords[6];
            matrix.m21 = -mesh.localCoords[7];
            matrix.m22 = -mesh.localCoords[8];
            matrix.m23 = mesh.localCoords[11];

            matrix.m30 = 0;
            matrix.m31 = 0;
            matrix.m32 = 0;
            matrix.m33 = 1;

            group.setTransform(matrix);
*/

            if((mesh.numMaterials > 1) ||
               ((mesh.numMaterials == 1) && (mesh.materials[0].numFaces != 0) &&
                (mesh.materials[0].numFaces != mesh.numFaces)))
            {
                if(facesProcessed == null || facesProcessed.length < mesh.numFaces)
                    facesProcessed = new boolean[mesh.numFaces];

                // reset it all to false again
                for(int i = 0; i < mesh.numFaces; i++)
                    facesProcessed[i] = false;

                int processed_count = 0;

                for(int i = 0; i < mesh.numMaterials; i++)
                {
                    MaterialData md = mesh.materials[i];
                    Shape3D shape = new Shape3D();
                    Appearance app =
                        (Appearance)named_objs.get(md.materialName);
                    shape.setAppearance(app);
                    group.addChild(shape);

                    // Generate a new local index list first.
                    int[] local_indexes = new int[md.numFaces * 3];
                    int cnt = 0;
                    for(int k = 0; k < md.numFaces; k++)
                    {
//System.out.println("face " + k + " facelist " + md.faceList[k] +
//                    " m_face " + mesh.faces[md.faceList[k] * 3] +
//                    " " + mesh.faces[md.faceList[k] * 3 + 1] +
//                    " " + mesh.faces[md.faceList[k] * 3 + 2]);

                        local_indexes[cnt] = mesh.faces[md.faceList[k] * 3];
                        local_indexes[cnt + 1] = mesh.faces[md.faceList[k] * 3 + 1];
                        local_indexes[cnt + 2] = mesh.faces[md.faceList[k] * 3 + 2];
                        cnt += 3;
                    }

                    float[] tmp = new float[md.numFaces * 3 * 3];
                    cnt = 0;
                    for(int k = 0; k < md.numFaces; k++)
                    {
                        facesProcessed[local_indexes[k]] = true;
                        processed_count++;

                        tmp[cnt] = mesh.vertices[local_indexes[k * 3] * 3];
                        tmp[cnt + 1] = mesh.vertices[local_indexes[k * 3] * 3 + 1];
                        tmp[cnt + 2] = mesh.vertices[local_indexes[k * 3] * 3 + 2];

                        tmp[cnt + 3] = mesh.vertices[local_indexes[k * 3 + 1] * 3];
                        tmp[cnt + 4] = mesh.vertices[local_indexes[k * 3 + 1] * 3 + 1];
                        tmp[cnt + 5] = mesh.vertices[local_indexes[k * 3 + 1] * 3 + 2];

                        tmp[cnt + 6] = mesh.vertices[local_indexes[k * 3 + 2] * 3];
                        tmp[cnt + 7] = mesh.vertices[local_indexes[k * 3 + 2] * 3 + 1];
                        tmp[cnt + 8] = mesh.vertices[local_indexes[k * 3 + 2] * 3 + 2];

                        cnt += 9;
                    }

                    TriangleArray geom = new TriangleArray();
                    shape.setGeometry(geom);
                    geom.setVertices(TriangleArray.COORDINATE_3,
                                     tmp,
                                     md.numFaces * 3);

                    if(mesh.normals != null)
                    {
                        cnt = 0;
                        tmp = new float[md.numFaces * 3 * 3];

                        for(int k = 0; k < md.numFaces; k++)
                        {
                            tmp[cnt] = mesh.normals[local_indexes[k] * 3];
                            tmp[cnt + 1] = mesh.normals[local_indexes[k] * 3 + 1];
                            tmp[cnt + 2] = mesh.normals[local_indexes[k] * 3 + 2];

                            tmp[cnt + 3] = mesh.normals[local_indexes[k] * 3];
                            tmp[cnt + 4] = mesh.normals[local_indexes[k] * 3 + 1];
                            tmp[cnt + 5] = mesh.normals[local_indexes[k] * 3 + 2];

                            tmp[cnt + 6] = mesh.normals[local_indexes[k] * 3];
                            tmp[cnt + 7] = mesh.normals[local_indexes[k] * 3 + 1];
                            tmp[cnt + 8] = mesh.normals[local_indexes[k] * 3 + 2];

                            cnt += 9;
                        }

                        geom.setNormals(tmp);
                    }

                    if(mesh.numTexCoords != 0)
                    {
                        cnt = 0;
                        float tmp_t[][] = new float[1][md.numFaces * 3 * 2];
                        for(int k = 0; k < md.numFaces * 3; k++)
                        {
                            tmp_t[0][cnt] = mesh.texCoords[local_indexes[k] * 2];
                            tmp_t[0][cnt + 1] = mesh.texCoords[local_indexes[k] * 2 + 1];

                            cnt += 2;
                        }

                        geom.setTextureCoordinates(TEX_COORD_TYPES, tmp_t, 1);

                        // Set the tex coord mapping based on the number of
                        // textures supplied by the given material.
                        // The create a new tex map array. All values are
                        // pointing to map 0, which is the default anyway, so
                        // no need to initialise any values.
                        int num_tex_maps = app.numTextureUnits();
                        int[] tex_maps = new int[num_tex_maps];

                        geom.setTextureSetMap(tex_maps);
                    }
                }

                // Should we make another geometry from the faces that haven't been
                // used? So what do we make the material colour
            }
            else
            {
                Shape3D shape = new Shape3D();

                // Single mesh with or without single material
                IndexedTriangleArray geom = new IndexedTriangleArray();
                geom.setVertices(TriangleArray.COORDINATE_3,
                                 mesh.vertices,
                                 mesh.numVertices);

                geom.setIndices(mesh.faces, mesh.numFaces * 3);

                // If we have normals, we need to change them from per-face to
                // per vertex.
                if(mesh.normals != null)
                {
                    geom.setNormals(mesh.normals);
                }

                shape.setGeometry(geom);

                if(mesh.numMaterials == 1)
                {
                    MaterialData md = mesh.materials[0];
                    Appearance app =
                        (Appearance)named_objs.get(md.materialName);
                    shape.setAppearance(app);
                }

                if(mesh.numTexCoords != 0)
                {
                    float[][] tex_coords = { mesh.texCoords };
                    geom.setTextureCoordinates(TEX_COORD_TYPES, tex_coords, 1);

                    // Set the tex coord mapping based on the number of
                    // textures supplied by the given material.
                    int num_tex_maps = 1;

                    if(mesh.numMaterials == 1)
                    {
                        MaterialData md = mesh.materials[0];
                        Appearance app =
                            (Appearance)named_objs.get(md.materialName);

                        // The create a new tex map array. All values are
                        // pointing to map 0, which is the default anyway, so
                        // no need to initialise any values.
                        num_tex_maps = app.numTextureUnits();
                    }

                    int[] tex_maps = new int[num_tex_maps];
                    geom.setTextureSetMap(tex_maps);
                }

                group.addChild(shape);
            }
        }

        return object_group;
    }

    /**
     * Process the light part of an object block instance. Assumes that the
     * flags have said it is OK to do so before calling this method.
     *
     * @param model The model instance to register everything with
     * @param object The object block instance to process for geometry
     * @param parent The grouping node that the mesh was added to, so that lights
     *    and cameras may be placed in the same area
     */
    private void processObjectLights(MaxModel model, ObjectBlock object, Group parent)
    {
        for(int j = 0; j < object.numLights; j++)
        {
            LightBlock l_block = object.lights[j];

            switch(l_block.type)
            {
                case LightBlock.DIRECTIONAL_LIGHT:
                    DirectionalLight dl = new DirectionalLight();
                    dl.setDirection(l_block.direction);
                    dl.setDiffuseColor(l_block.color);
                    dl.setEnabled(l_block.enabled);
                    parent.addChild(dl);
                    model.addLight(dl);
                    break;

                case LightBlock.SPOT_LIGHT:
                    SpotLight sl = new SpotLight();
                    sl.setPosition(l_block.direction);
                    sl.setDiffuseColor(l_block.color);
                    sl.setEnabled(l_block.enabled);

                    // Calc direction from the target values
                    sl.setDirection(l_block.direction[0] - l_block.target[0],
                                    l_block.direction[1] - l_block.target[1],
                                    l_block.direction[2] - l_block.target[2]);

                    sl.setCutOffAngle(l_block.hotspotAngle);
                    sl.setAttenuation(l_block.attenuation, 0, 0);
                    parent.addChild(sl);
                    model.addLight(sl);
                    break;
            }
        }
    }

    /**
     * Process the camera part of an object block instance. Assumes that the
     * flags have said it is OK to do so before calling this method.
     *
     * @param model The model instance to register everything with
     * @param object The object block instance to process for geometry
     * @param parent The grouping node that the mesh was added to, so that lights
     *    and cameras may be placed in the same area
     */
    private void processObjectCameras(MaxModel model, ObjectBlock object, Group parent)
    {
        if(object.numCameras == 0)
            return;

        if(matrixUtils == null)
        {
            matrixUtils = new MatrixUtils();
            eyePosition = new Point3d();
            center = new Point3d();
            upVector = new Vector3d();
        }

        if(matrix == null)
            matrix = new Matrix4d();

        for(int j = 0; j < object.numCameras; j++)
        {
            CameraBlock c_block = object.cameras[j];

            eyePosition.set(c_block.location[0], c_block.location[1], c_block.location[2]);
            center.set(c_block.target[0], c_block.target[1], c_block.target[2]);

            double angle = Math.toRadians(c_block.bankAngle);
            upVector.set((float)Math.sin(angle), (float)Math.cos(angle), 0);

            matrixUtils.lookAt(eyePosition, center, upVector, matrix);
            matrixUtils.inverse(matrix, matrix);

            TransformGroup tg = new TransformGroup();
            tg.setTransform(matrix);

            Viewpoint vp = new Viewpoint();
            tg.addChild(vp);

            parent.addChild(tg);
            model.addViewpoint(vp);
        }
    }

    /**
     * Process the background information for the mesh and create the geometry
     * for it.
     *
     * @param model The model instance to register everything with
     * @param mesh The object mesh instance to process for the background
     */
    private void processBackground(MaxModel model, ObjectMesh mesh)
    {
        switch(mesh.selectedBackground)
        {
            case ObjectMesh.USE_BITMAP:
                break;

            case ObjectMesh.USE_GRADIENT:
                break;

            case ObjectMesh.USE_SOLID_BG:
                ColorBackground sbg = new ColorBackground();
                sbg.setColor(mesh.solidBackgroundColor);

                Group root_group = model.getModelRoot();
                root_group.addChild(sbg);
                model.addBackground(sbg);
                break;
        }
    }

    /**
     * Process the fog information for the mesh and create the node for it.
     *
     * @param model The model instance to register everything with
     * @param mesh The object mesh instance to process for the fog
     */
    private void processFog(MaxModel model, ObjectMesh mesh)
    {
        switch(mesh.selectedFog)
        {
            case ObjectMesh.USE_DISTANCE_FOG:
                break;

            case ObjectMesh.USE_LAYER_FOG:
                break;

            case ObjectMesh.USE_LINEAR_FOG:
                Fog lf = new Fog(Fog.LINEAR);
                lf.setColor(mesh.fogColor);
                lf.setLinearDistance(mesh.linearFogDetails[0],
                                     mesh.linearFogDetails[2]);

                Group root_group = model.getModelRoot();
                root_group.addChild(lf);
                model.addFog(lf);
                break;
        }
    }

    /**
     * Process a single keyframe block instance. Each keyframe block will have
     * it's own instance of a runtime object so that each animation can be
     * run separately.
     *
     * @param model The model instance to register everything with
     * @param keyframe The keyframe data instance to process for animations
     */
    private void processKeyframeBlock(MaxModel model, KeyframeBlock keyframe)
    {
    }
}
