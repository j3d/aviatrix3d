/*
 * **************************************************************************
 *                        Copyright j3d.org (c) 2000 - ${year}
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read docs/lgpl.txt for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * **************************************************************************
 */

package org.j3d.aviatrix3d.output.graphics;

import java.nio.*;

import javax.media.opengl.*;

import com.jogamp.common.nio.PointerBuffer;

/**
 * Pretend Mock of the GL2 interface. This exists because CGLIB cannot
 * generate a proper mock for the GL/GL2/GL3... interfaces due to too
 * many methods existing and then having more than 65k bytes in a method.
 *
 * @author justin
 */
public class MockGL2 implements GL2
{
    private int methodCallCount;

    public MockGL2()
    {
        methodCallCount = 0;
    }

    @Override
    public void glAccum(int op, float value)
    {
        methodCallCount++;
    }

    @Override
    public void glActiveStencilFaceEXT(int face)
    {
        methodCallCount++;
    }

    @Override
    public void glApplyTextureEXT(int mode)
    {
        methodCallCount++;
    }

    @Override
    public boolean glAreTexturesResident(int n, IntBuffer textures, ByteBuffer residences)
    {
        return false;
    }

    @Override
    public boolean glAreTexturesResident(int n, int[] textures, int textures_offset, byte[] residences, int residences_offset)
    {
        return false;
    }

    @Override
    public void glArrayElement(int i)
    {
        methodCallCount++;
    }

    @Override
    public void glAttachObjectARB(int containerObj, int obj)
    {
        methodCallCount++;
    }

    @Override
    public void glBegin(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glBeginConditionalRenderNVX(int id)
    {
        methodCallCount++;
    }

    @Override
    public void glBeginOcclusionQueryNV(int id)
    {
        methodCallCount++;
    }

    @Override
    public void glBeginPerfMonitorAMD(int monitor)
    {
        methodCallCount++;
    }

    @Override
    public void glBeginTransformFeedback(int primitiveMode)
    {
        methodCallCount++;
    }

    @Override
    public void glBeginVertexShaderEXT()
    {
        methodCallCount++;
    }

    @Override
    public void glBeginVideoCaptureNV(int video_capture_slot)
    {
        methodCallCount++;
    }

    @Override
    public void glBindBufferBase(int target, int index, int buffer)
    {
        methodCallCount++;
    }

    @Override
    public void glBindBufferRange(int target, int index, int buffer, long offset, long size)
    {
        methodCallCount++;
    }

    @Override
    public int glBindLightParameterEXT(int light, int value)
    {
        return 0;
    }

    @Override
    public int glBindMaterialParameterEXT(int face, int value)
    {
        return 0;
    }

    @Override
    public void glBindMultiTextureEXT(int texunit, int target, int texture)
    {
        methodCallCount++;
    }

    @Override
    public int glBindParameterEXT(int value)
    {
        return 0;
    }

    @Override
    public void glBindProgramARB(int target, int program)
    {
        methodCallCount++;
    }

    @Override
    public int glBindTexGenParameterEXT(int unit, int coord, int value)
    {
        return 0;
    }

    @Override
    public int glBindTextureUnitParameterEXT(int unit, int value)
    {
        return 0;
    }

    @Override
    public void glBindTransformFeedbackNV(int target, int id)
    {
        methodCallCount++;
    }

    @Override
    public void glBindVertexArray(int array)
    {
        methodCallCount++;
    }

    @Override
    public void glBindVertexShaderEXT(int id)
    {
        methodCallCount++;
    }

    @Override
    public void glBindVideoCaptureStreamBufferNV(int video_capture_slot, int stream, int frame_region, long offset)
    {
        methodCallCount++;
    }

    @Override
    public void glBindVideoCaptureStreamTextureNV(int video_capture_slot, int stream, int frame_region, int target, int texture)
    {
        methodCallCount++;
    }

    @Override
    public void glBitmap(int width, int height, float xorig, float yorig, float xmove, float ymove, ByteBuffer bitmap)
    {
        methodCallCount++;
    }

    @Override
    public void glBitmap(int width, int height, float xorig, float yorig, float xmove, float ymove, byte[] bitmap, int bitmap_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glBitmap(int width, int height, float xorig, float yorig, float xmove, float ymove, long bitmap_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glBlendEquationIndexedAMD(int buf, int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glBlendEquationSeparateIndexedAMD(int buf, int modeRGB, int modeAlpha)
    {
        methodCallCount++;
    }

    @Override
    public void glBlendFuncIndexedAMD(int buf, int src, int dst)
    {
        methodCallCount++;
    }

    @Override
    public void glBlendFuncSeparateINGR(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha)
    {
        methodCallCount++;
    }

    @Override
    public void glBlendFuncSeparateIndexedAMD(int buf, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha)
    {
        methodCallCount++;
    }

    @Override
    public void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter)
    {
        methodCallCount++;
    }

    @Override
    public void glBufferParameteri(int target, int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glCallList(int list)
    {
        methodCallCount++;
    }

    @Override
    public void glCallLists(int n, int type, Buffer lists)
    {
        methodCallCount++;
    }

    @Override
    public int glCheckNamedFramebufferStatusEXT(int framebuffer, int target)
    {
        return 0;
    }

    @Override
    public void glClearAccum(float red, float green, float blue, float alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil)
    {
        methodCallCount++;
    }

    @Override
    public void glClearBufferfv(int buffer, int drawbuffer, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glClearBufferfv(int buffer, int drawbuffer, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glClearBufferiv(int buffer, int drawbuffer, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glClearBufferiv(int buffer, int drawbuffer, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glClearBufferuiv(int buffer, int drawbuffer, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glClearBufferuiv(int buffer, int drawbuffer, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glClearColorIi(int red, int green, int blue, int alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glClearColorIui(int red, int green, int blue, int alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glClearIndex(float c)
    {
        methodCallCount++;
    }

    @Override
    public void glClientAttribDefaultEXT(int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glClipPlane(int plane, DoubleBuffer equation)
    {
        methodCallCount++;
    }

    @Override
    public void glClipPlane(int plane, double[] equation, int equation_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glClipPlanef(int plane, FloatBuffer equation)
    {
        methodCallCount++;
    }

    @Override
    public void glClipPlanef(int plane, float[] equation, int equation_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3b(byte red, byte green, byte blue)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3bv(ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3bv(byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3d(double red, double green, double blue)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3f(float red, float green, float blue)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3h(short red, short green, short blue)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3hv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3hv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3i(int red, int green, int blue)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3s(short red, short green, short blue)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3ub(byte red, byte green, byte blue)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3ubv(ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3ubv(byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3ui(int red, int green, int blue)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3uiv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3uiv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3us(short red, short green, short blue)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3usv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor3usv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4b(byte red, byte green, byte blue, byte alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4bv(ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4bv(byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4d(double red, double green, double blue, double alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4h(short red, short green, short blue, short alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4hv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4hv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4i(int red, int green, int blue, int alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4s(short red, short green, short blue, short alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4ubv(ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4ubv(byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4ui(int red, int green, int blue, int alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4uiv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4uiv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4us(short red, short green, short blue, short alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4usv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4usv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColorMaskIndexed(int index, boolean r, boolean g, boolean b, boolean a)
    {
        methodCallCount++;
    }

    @Override
    public void glColorMaterial(int face, int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glColorSubTable(int target, int start, int count, int format, int type, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glColorSubTable(int target, int start, int count, int format, int type, long data_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColorTable(int target, int internalformat, int width, int format, int type, Buffer table)
    {
        methodCallCount++;
    }

    @Override
    public void glColorTable(int target, int internalformat, int width, int format, int type, long table_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColorTableParameterfv(int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glColorTableParameterfv(int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColorTableParameteriv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glColorTableParameteriv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glCompileShaderARB(int shaderObj)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width, int border, int imageSize, Buffer bits)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer bits)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedMultiTexImage3DEXT(int texunit, int target, int level, int internalformat, int width, int height, int depth, int border, int imageSize, Buffer bits)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int width, int format, int imageSize, Buffer bits)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer bits)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, Buffer bits)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTextureImage1DEXT(int texture, int target, int level, int internalformat, int width, int border, int imageSize, Buffer bits)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTextureImage2DEXT(int texture, int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer bits)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTextureImage3DEXT(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int imageSize, Buffer bits)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format, int imageSize, Buffer bits)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer bits)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, Buffer bits)
    {
        methodCallCount++;
    }

    @Override
    public void glConvolutionFilter1D(int target, int internalformat, int width, int format, int type, Buffer image)
    {
        methodCallCount++;
    }

    @Override
    public void glConvolutionFilter1D(int target, int internalformat, int width, int format, int type, long image_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glConvolutionFilter2D(int target, int internalformat, int width, int height, int format, int type, Buffer image)
    {
        methodCallCount++;
    }

    @Override
    public void glConvolutionFilter2D(int target, int internalformat, int width, int height, int format, int type, long image_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glConvolutionParameterf(int target, int pname, float params)
    {
        methodCallCount++;
    }

    @Override
    public void glConvolutionParameterfv(int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glConvolutionParameterfv(int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glConvolutionParameteri(int target, int pname, int params)
    {
        methodCallCount++;
    }

    @Override
    public void glConvolutionParameteriv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glConvolutionParameteriv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyBufferSubData(int readTarget, int writeTarget, long readOffset, long writeOffset, long size)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyColorSubTable(int target, int start, int x, int y, int width)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyColorTable(int target, int internalformat, int x, int y, int width)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyConvolutionFilter1D(int target, int internalformat, int x, int y, int width)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyConvolutionFilter2D(int target, int internalformat, int x, int y, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyImageSubDataNV(int srcName, int srcTarget, int srcLevel, int srcX, int srcY, int srcZ, int dstName, int dstTarget, int dstLevel, int dstX, int dstY, int dstZ, int width, int height, int depth)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int x, int y, int width, int border)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int x, int y, int width, int height, int border)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int x, int y, int width)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset, int x, int y, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyPathNV(int resultPath, int srcPath)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyPixels(int x, int y, int width, int height, int type)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyTextureImage1DEXT(int texture, int target, int level, int internalformat, int x, int y, int width, int border)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyTextureImage2DEXT(int texture, int target, int level, int internalformat, int x, int y, int width, int height, int border)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int x, int y, int width)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int x, int y, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glCoverFillPathInstancedNV(int numPaths, int pathNameType, Buffer paths, int pathBase, int coverMode, int transformType, FloatBuffer transformValues)
    {
        methodCallCount++;
    }

    @Override
    public void glCoverFillPathInstancedNV(int numPaths, int pathNameType, Buffer paths, int pathBase, int coverMode, int transformType, float[] transformValues, int transformValues_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glCoverFillPathNV(int path, int coverMode)
    {
        methodCallCount++;
    }

    @Override
    public void glCoverStrokePathInstancedNV(int numPaths, int pathNameType, Buffer paths, int pathBase, int coverMode, int transformType, FloatBuffer transformValues)
    {
        methodCallCount++;
    }

    @Override
    public void glCoverStrokePathInstancedNV(int numPaths, int pathNameType, Buffer paths, int pathBase, int coverMode, int transformType, float[] transformValues, int transformValues_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glCoverStrokePathNV(int path, int coverMode)
    {
        methodCallCount++;
    }

    @Override
    public int glCreateProgramObjectARB()
    {
        return 0;
    }

    @Override
    public int glCreateShaderObjectARB(int shaderType)
    {
        return 0;
    }

    @Override
    public void glCullParameterdvEXT(int pname, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glCullParameterdvEXT(int pname, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glCullParameterfvEXT(int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glCullParameterfvEXT(int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glCurrentPaletteMatrix(int index)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteFencesAPPLE(int n, IntBuffer fences)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteFencesAPPLE(int n, int[] fences, int fences_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteFencesNV(int n, IntBuffer fences)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteFencesNV(int n, int[] fences, int fences_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteLists(int list, int range)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteNamesAMD(int identifier, int num, IntBuffer names)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteNamesAMD(int identifier, int num, int[] names, int names_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteObjectARB(int obj)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteOcclusionQueriesNV(int n, IntBuffer ids)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteOcclusionQueriesNV(int n, int[] ids, int ids_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDeletePathsNV(int path, int range)
    {
        methodCallCount++;
    }

    @Override
    public void glDeletePerfMonitorsAMD(int n, IntBuffer monitors)
    {
        methodCallCount++;
    }

    @Override
    public void glDeletePerfMonitorsAMD(int n, int[] monitors, int monitors_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteProgramsARB(int n, IntBuffer programs)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteProgramsARB(int n, int[] programs, int programs_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteTransformFeedbacksNV(int n, IntBuffer ids)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteTransformFeedbacksNV(int n, int[] ids, int ids_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteVertexArrays(int n, IntBuffer arrays)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteVertexArrays(int n, int[] arrays, int arrays_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteVertexShaderEXT(int id)
    {
        methodCallCount++;
    }

    @Override
    public void glDepthBoundsEXT(double zmin, double zmax)
    {
        methodCallCount++;
    }

    @Override
    public void glDetachObjectARB(int containerObj, int attachedObj)
    {
        methodCallCount++;
    }

    @Override
    public void glDisableClientStateIndexedEXT(int array, int index)
    {
        methodCallCount++;
    }

    @Override
    public void glDisableClientStateiEXT(int array, int index)
    {
        methodCallCount++;
    }

    @Override
    public void glDisableIndexed(int target, int index)
    {
        methodCallCount++;
    }

    @Override
    public void glDisableVariantClientStateEXT(int id)
    {
        methodCallCount++;
    }

    @Override
    public void glDisableVertexArrayAttribEXT(int vaobj, int index)
    {
        methodCallCount++;
    }

    @Override
    public void glDisableVertexArrayEXT(int vaobj, int array)
    {
        methodCallCount++;
    }

    @Override
    public void glDisableVertexAttribAPPLE(int index, int pname)
    {
        methodCallCount++;
    }

    @Override
    public void glDisableVertexAttribArrayARB(int index)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawArraysInstanced(int mode, int first, int count, int instancecount)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawBuffers(int n, IntBuffer bufs)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawBuffers(int n, int[] bufs, int bufs_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawBuffersATI(int n, IntBuffer bufs)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawBuffersATI(int n, int[] bufs, int bufs_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawElementsInstanced(int mode, int count, int type, Buffer indices, int instancecount)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawElementsInstanced(int mode, int count, int type, long indices_buffer_offset, int instancecount)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawPixels(int width, int height, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawPixels(int width, int height, int format, int type, long pixels_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawRangeElements(int mode, int start, int end, int count, int type, Buffer indices)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawRangeElements(int mode, int start, int end, int count, int type, long indices_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawTextureNV(int texture, int sampler, float x0, float y0, float x1, float y1, float z, float s0, float t0, float s1, float t1)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawTransformFeedbackNV(int mode, int id)
    {
        methodCallCount++;
    }

    @Override
    public void glEdgeFlag(boolean flag)
    {
        methodCallCount++;
    }

    @Override
    public void glEdgeFlagPointer(int stride, Buffer ptr)
    {
        methodCallCount++;
    }

    @Override
    public void glEdgeFlagPointer(int stride, long ptr_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glEdgeFlagv(ByteBuffer flag)
    {
        methodCallCount++;
    }

    @Override
    public void glEdgeFlagv(byte[] flag, int flag_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glEnableClientStateIndexedEXT(int array, int index)
    {
        methodCallCount++;
    }

    @Override
    public void glEnableClientStateiEXT(int array, int index)
    {
        methodCallCount++;
    }

    @Override
    public void glEnableIndexed(int target, int index)
    {
        methodCallCount++;
    }

    @Override
    public void glEnableVariantClientStateEXT(int id)
    {
        methodCallCount++;
    }

    @Override
    public void glEnableVertexArrayAttribEXT(int vaobj, int index)
    {
        methodCallCount++;
    }

    @Override
    public void glEnableVertexArrayEXT(int vaobj, int array)
    {
        methodCallCount++;
    }

    @Override
    public void glEnableVertexAttribAPPLE(int index, int pname)
    {
        methodCallCount++;
    }

    @Override
    public void glEnableVertexAttribArrayARB(int index)
    {
        methodCallCount++;
    }

    @Override
    public void glEnd()
    {
        methodCallCount++;
    }

    @Override
    public void glEndConditionalRenderNVX()
    {
        methodCallCount++;
    }

    @Override
    public void glEndList()
    {
        methodCallCount++;
    }

    @Override
    public void glEndOcclusionQueryNV()
    {
        methodCallCount++;
    }

    @Override
    public void glEndPerfMonitorAMD(int monitor)
    {
        methodCallCount++;
    }

    @Override
    public void glEndTransformFeedback()
    {
        methodCallCount++;
    }

    @Override
    public void glEndVertexShaderEXT()
    {
        methodCallCount++;
    }

    @Override
    public void glEndVideoCaptureNV(int video_capture_slot)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalCoord1d(double u)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalCoord1dv(DoubleBuffer u)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalCoord1dv(double[] u, int u_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalCoord1f(float u)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalCoord1fv(FloatBuffer u)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalCoord1fv(float[] u, int u_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalCoord2d(double u, double v)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalCoord2dv(DoubleBuffer u)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalCoord2dv(double[] u, int u_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalCoord2f(float u, float v)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalCoord2fv(FloatBuffer u)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalCoord2fv(float[] u, int u_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalMapsNV(int target, int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalMesh1(int mode, int i1, int i2)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalMesh2(int mode, int i1, int i2, int j1, int j2)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalPoint1(int i)
    {
        methodCallCount++;
    }

    @Override
    public void glEvalPoint2(int i, int j)
    {
        methodCallCount++;
    }

    @Override
    public void glExtractComponentEXT(int res, int src, int num)
    {
        methodCallCount++;
    }

    @Override
    public void glFeedbackBuffer(int size, int type, FloatBuffer buffer)
    {
        methodCallCount++;
    }

    @Override
    public void glFinishFenceAPPLE(int fence)
    {
        methodCallCount++;
    }

    @Override
    public void glFinishFenceNV(int fence)
    {
        methodCallCount++;
    }

    @Override
    public void glFinishObjectAPPLE(int object, int name)
    {
        methodCallCount++;
    }

    @Override
    public void glFinishTextureSUNX()
    {
        methodCallCount++;
    }

    @Override
    public void glFlushMappedNamedBufferRangeEXT(int buffer, long offset, long length)
    {
        methodCallCount++;
    }

    @Override
    public void glFlushPixelDataRangeNV(int target)
    {
        methodCallCount++;
    }

    @Override
    public void glFlushVertexArrayRangeAPPLE(int length, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glFlushVertexArrayRangeNV()
    {
        methodCallCount++;
    }

    @Override
    public void glFogCoordPointer(int type, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glFogCoordPointer(int type, int stride, long pointer_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glFogCoordd(double coord)
    {
        methodCallCount++;
    }

    @Override
    public void glFogCoorddv(DoubleBuffer coord)
    {
        methodCallCount++;
    }

    @Override
    public void glFogCoorddv(double[] coord, int coord_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glFogCoordf(float coord)
    {
        methodCallCount++;
    }

    @Override
    public void glFogCoordfv(FloatBuffer coord)
    {
        methodCallCount++;
    }

    @Override
    public void glFogCoordfv(float[] coord, int coord_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glFogCoordh(short fog)
    {
        methodCallCount++;
    }

    @Override
    public void glFogCoordhv(ShortBuffer fog)
    {
        methodCallCount++;
    }

    @Override
    public void glFogCoordhv(short[] fog, int fog_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glFogi(int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glFogiv(int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glFogiv(int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glFrameTerminatorGREMEDY()
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferDrawBufferEXT(int framebuffer, int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferDrawBuffersEXT(int framebuffer, int n, IntBuffer bufs)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferDrawBuffersEXT(int framebuffer, int n, int[] bufs, int bufs_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferReadBufferEXT(int framebuffer, int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferTextureEXT(int target, int attachment, int texture, int level)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferTextureFaceEXT(int target, int attachment, int texture, int level, int face)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferTextureLayerEXT(int target, int attachment, int texture, int level, int layer)
    {
        methodCallCount++;
    }

    @Override
    public void glGenFencesAPPLE(int n, IntBuffer fences)
    {
        methodCallCount++;
    }

    @Override
    public void glGenFencesAPPLE(int n, int[] fences, int fences_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGenFencesNV(int n, IntBuffer fences)
    {
        methodCallCount++;
    }

    @Override
    public void glGenFencesNV(int n, int[] fences, int fences_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glGenLists(int range)
    {
        return 0;
    }

    @Override
    public void glGenNamesAMD(int identifier, int num, IntBuffer names)
    {
        methodCallCount++;
    }

    @Override
    public void glGenNamesAMD(int identifier, int num, int[] names, int names_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGenOcclusionQueriesNV(int n, IntBuffer ids)
    {
        methodCallCount++;
    }

    @Override
    public void glGenOcclusionQueriesNV(int n, int[] ids, int ids_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glGenPathsNV(int range)
    {
        return 0;
    }

    @Override
    public void glGenPerfMonitorsAMD(int n, IntBuffer monitors)
    {
        methodCallCount++;
    }

    @Override
    public void glGenPerfMonitorsAMD(int n, int[] monitors, int monitors_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGenProgramsARB(int n, IntBuffer programs)
    {
        methodCallCount++;
    }

    @Override
    public void glGenProgramsARB(int n, int[] programs, int programs_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glGenSymbolsEXT(int datatype, int storagetype, int range, int components)
    {
        return 0;
    }

    @Override
    public void glGenTransformFeedbacksNV(int n, IntBuffer ids)
    {
        methodCallCount++;
    }

    @Override
    public void glGenTransformFeedbacksNV(int n, int[] ids, int ids_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGenVertexArrays(int n, IntBuffer arrays)
    {
        methodCallCount++;
    }

    @Override
    public void glGenVertexArrays(int n, int[] arrays, int arrays_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glGenVertexShadersEXT(int range)
    {
        return 0;
    }

    @Override
    public void glGenerateMultiTexMipmapEXT(int texunit, int target)
    {
        methodCallCount++;
    }

    @Override
    public void glGenerateTextureMipmapEXT(int texture, int target)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveUniformARB(int programObj, int index, int maxLength, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveUniformARB(int programObj, int index, int maxLength, int[] length, int length_offset, int[] size, int size_offset, int[] type, int type_offset, byte[] name, int name_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize, IntBuffer length, ByteBuffer uniformBlockName)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize, int[] length, int length_offset, byte[] uniformBlockName, int uniformBlockName_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveUniformsiv(int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int uniformIndices_offset, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetAttachedObjectsARB(int containerObj, int maxCount, IntBuffer count, IntBuffer obj)
    {
        methodCallCount++;
    }

    @Override
    public void glGetAttachedObjectsARB(int containerObj, int maxCount, int[] count, int count_offset, int[] obj, int obj_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetBooleanIndexedv(int target, int index, ByteBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetBooleanIndexedv(int target, int index, byte[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetClipPlane(int plane, DoubleBuffer equation)
    {
        methodCallCount++;
    }

    @Override
    public void glGetClipPlane(int plane, double[] equation, int equation_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetClipPlanef(int plane, FloatBuffer equation)
    {
        methodCallCount++;
    }

    @Override
    public void glGetClipPlanef(int plane, float[] equation, int equation_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetColorTable(int target, int format, int type, Buffer table)
    {
        methodCallCount++;
    }

    @Override
    public void glGetColorTable(int target, int format, int type, long table_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetColorTableParameterfv(int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetColorTableParameterfv(int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetColorTableParameteriv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetColorTableParameteriv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetCompressedMultiTexImageEXT(int texunit, int target, int lod, Buffer img)
    {
        methodCallCount++;
    }

    @Override
    public void glGetCompressedTextureImageEXT(int texture, int target, int lod, Buffer img)
    {
        methodCallCount++;
    }

    @Override
    public void glGetConvolutionFilter(int target, int format, int type, Buffer image)
    {
        methodCallCount++;
    }

    @Override
    public void glGetConvolutionFilter(int target, int format, int type, long image_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetConvolutionParameterfv(int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetConvolutionParameterfv(int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetConvolutionParameteriv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetConvolutionParameteriv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetDoubleIndexedvEXT(int target, int index, DoubleBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetDoubleIndexedvEXT(int target, int index, double[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetDoublei_vEXT(int pname, int index, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetDoublei_vEXT(int pname, int index, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetFenceivNV(int fence, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetFenceivNV(int fence, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetFloatIndexedvEXT(int target, int index, FloatBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetFloatIndexedvEXT(int target, int index, float[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetFloati_vEXT(int pname, int index, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetFloati_vEXT(int pname, int index, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glGetFragDataLocation(int program, String name)
    {
        return 0;
    }

    @Override
    public void glGetFramebufferParameterivEXT(int framebuffer, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetFramebufferParameterivEXT(int framebuffer, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glGetHandleARB(int pname)
    {
        return 0;
    }

    @Override
    public void glGetHistogram(int target, boolean reset, int format, int type, Buffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glGetHistogram(int target, boolean reset, int format, int type, long values_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetHistogramParameterfv(int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetHistogramParameterfv(int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetHistogramParameteriv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetHistogramParameteriv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public long glGetImageHandleNV(int texture, int level, boolean layered, int layer, int format)
    {
        return 0;
    }

    @Override
    public void glGetInfoLogARB(int obj, int maxLength, IntBuffer length, ByteBuffer infoLog)
    {
        methodCallCount++;
    }

    @Override
    public void glGetInfoLogARB(int obj, int maxLength, int[] length, int length_offset, byte[] infoLog, int infoLog_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetIntegerIndexedv(int target, int index, IntBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetIntegerIndexedv(int target, int index, int[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetIntegeri_v(int target, int index, IntBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetIntegeri_v(int target, int index, int[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetInvariantBooleanvEXT(int id, int value, ByteBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetInvariantBooleanvEXT(int id, int value, byte[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetInvariantFloatvEXT(int id, int value, FloatBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetInvariantFloatvEXT(int id, int value, float[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetInvariantIntegervEXT(int id, int value, IntBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetInvariantIntegervEXT(int id, int value, int[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetLightiv(int light, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetLightiv(int light, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetLocalConstantBooleanvEXT(int id, int value, ByteBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetLocalConstantBooleanvEXT(int id, int value, byte[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetLocalConstantFloatvEXT(int id, int value, FloatBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetLocalConstantFloatvEXT(int id, int value, float[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetLocalConstantIntegervEXT(int id, int value, IntBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetLocalConstantIntegervEXT(int id, int value, int[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapAttribParameterfvNV(int target, int index, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapAttribParameterfvNV(int target, int index, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapAttribParameterivNV(int target, int index, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapAttribParameterivNV(int target, int index, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapControlPointsNV(int target, int index, int type, int ustride, int vstride, boolean packed, Buffer points)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapParameterfvNV(int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapParameterfvNV(int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapParameterivNV(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapParameterivNV(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapdv(int target, int query, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapdv(int target, int query, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapfv(int target, int query, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapfv(int target, int query, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapiv(int target, int query, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMapiv(int target, int query, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMaterialiv(int face, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMaterialiv(int face, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMinmax(int target, boolean reset, int format, int type, Buffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMinmax(int target, boolean reset, int format, int type, long values_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMinmaxParameterfv(int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMinmaxParameterfv(int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMinmaxParameteriv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMinmaxParameteriv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexEnvfvEXT(int texunit, int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexEnvfvEXT(int texunit, int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexEnvivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexEnvivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexGendvEXT(int texunit, int coord, int pname, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexGendvEXT(int texunit, int coord, int pname, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexGenfvEXT(int texunit, int coord, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexGenfvEXT(int texunit, int coord, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexGenivEXT(int texunit, int coord, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexGenivEXT(int texunit, int coord, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexImageEXT(int texunit, int target, int level, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexLevelParameterfvEXT(int texunit, int target, int level, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexLevelParameterfvEXT(int texunit, int target, int level, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexLevelParameterivEXT(int texunit, int target, int level, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexLevelParameterivEXT(int texunit, int target, int level, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexParameterIivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexParameterIivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexParameterIuivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexParameterIuivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexParameterfvEXT(int texunit, int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexParameterfvEXT(int texunit, int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexParameterivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultiTexParameterivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultisamplefvNV(int pname, int index, FloatBuffer val)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMultisamplefvNV(int pname, int index, float[] val, int val_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedBufferParameterivEXT(int buffer, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedBufferParameterivEXT(int buffer, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedBufferSubDataEXT(int buffer, long offset, long size, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedFramebufferAttachmentParameterivEXT(int framebuffer, int attachment, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedFramebufferAttachmentParameterivEXT(int framebuffer, int attachment, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedProgramLocalParameterIivEXT(int program, int target, int index, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedProgramLocalParameterIivEXT(int program, int target, int index, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedProgramLocalParameterIuivEXT(int program, int target, int index, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedProgramLocalParameterIuivEXT(int program, int target, int index, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedProgramLocalParameterdvEXT(int program, int target, int index, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedProgramLocalParameterdvEXT(int program, int target, int index, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedProgramLocalParameterfvEXT(int program, int target, int index, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedProgramLocalParameterfvEXT(int program, int target, int index, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedProgramStringEXT(int program, int target, int pname, Buffer string)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedProgramivEXT(int program, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedProgramivEXT(int program, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedRenderbufferParameterivEXT(int renderbuffer, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedRenderbufferParameterivEXT(int renderbuffer, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetObjectParameterfvARB(int obj, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetObjectParameterfvARB(int obj, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetObjectParameterivAPPLE(int objectType, int name, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetObjectParameterivAPPLE(int objectType, int name, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetObjectParameterivARB(int obj, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetObjectParameterivARB(int obj, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetOcclusionQueryivNV(int id, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetOcclusionQueryivNV(int id, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetOcclusionQueryuivNV(int id, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetOcclusionQueryuivNV(int id, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathColorGenfvNV(int color, int pname, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathColorGenfvNV(int color, int pname, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathColorGenivNV(int color, int pname, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathColorGenivNV(int color, int pname, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathCommandsNV(int path, ByteBuffer commands)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathCommandsNV(int path, byte[] commands, int commands_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathCoordsNV(int path, FloatBuffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathCoordsNV(int path, float[] coords, int coords_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathDashArrayNV(int path, FloatBuffer dashArray)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathDashArrayNV(int path, float[] dashArray, int dashArray_offset)
    {
        methodCallCount++;
    }

    @Override
    public float glGetPathLengthNV(int path, int startSegment, int numSegments)
    {
        return 0;
    }

    @Override
    public void glGetPathMetricRangeNV(int metricQueryMask, int firstPathName, int numPaths, int stride, FloatBuffer metrics)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathMetricRangeNV(int metricQueryMask, int firstPathName, int numPaths, int stride, float[] metrics, int metrics_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathMetricsNV(int metricQueryMask, int numPaths, int pathNameType, Buffer paths, int pathBase, int stride, FloatBuffer metrics)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathMetricsNV(int metricQueryMask, int numPaths, int pathNameType, Buffer paths, int pathBase, int stride, float[] metrics, int metrics_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathParameterfvNV(int path, int pname, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathParameterfvNV(int path, int pname, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathParameterivNV(int path, int pname, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathParameterivNV(int path, int pname, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathSpacingNV(int pathListMode, int numPaths, int pathNameType, Buffer paths, int pathBase, float advanceScale, float kerningScale, int transformType, FloatBuffer returnedSpacing)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathSpacingNV(int pathListMode, int numPaths, int pathNameType, Buffer paths, int pathBase, float advanceScale, float kerningScale, int transformType, float[] returnedSpacing, int returnedSpacing_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathTexGenfvNV(int texCoordSet, int pname, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathTexGenfvNV(int texCoordSet, int pname, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathTexGenivNV(int texCoordSet, int pname, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPathTexGenivNV(int texCoordSet, int pname, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPerfMonitorCounterDataAMD(int monitor, int pname, int dataSize, IntBuffer data, IntBuffer bytesWritten)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPerfMonitorCounterDataAMD(int monitor, int pname, int dataSize, int[] data, int data_offset, int[] bytesWritten, int bytesWritten_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPerfMonitorCounterInfoAMD(int group, int counter, int pname, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPerfMonitorCounterStringAMD(int group, int counter, int bufSize, IntBuffer length, ByteBuffer counterString)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPerfMonitorCounterStringAMD(int group, int counter, int bufSize, int[] length, int length_offset, byte[] counterString, int counterString_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPerfMonitorCountersAMD(int group, IntBuffer numCounters, IntBuffer maxActiveCounters, int counterSize, IntBuffer counters)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPerfMonitorCountersAMD(int group, int[] numCounters, int numCounters_offset, int[] maxActiveCounters, int maxActiveCounters_offset, int counterSize, int[] counters, int counters_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPerfMonitorGroupStringAMD(int group, int bufSize, IntBuffer length, ByteBuffer groupString)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPerfMonitorGroupStringAMD(int group, int bufSize, int[] length, int length_offset, byte[] groupString, int groupString_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPerfMonitorGroupsAMD(IntBuffer numGroups, int groupsSize, IntBuffer groups)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPerfMonitorGroupsAMD(int[] numGroups, int numGroups_offset, int groupsSize, int[] groups, int groups_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPixelMapfv(int map, FloatBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPixelMapfv(int map, float[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPixelMapfv(int map, long values_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPixelMapuiv(int map, IntBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPixelMapuiv(int map, int[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPixelMapuiv(int map, long values_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPixelMapusv(int map, ShortBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPixelMapusv(int map, short[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPixelMapusv(int map, long values_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPixelTransformParameterfvEXT(int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPixelTransformParameterfvEXT(int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPixelTransformParameterivEXT(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPixelTransformParameterivEXT(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPointeri_vEXT(int pname, int index, PointerBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPolygonStipple(ByteBuffer mask)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPolygonStipple(byte[] mask, int mask_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetPolygonStipple(long mask_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramEnvParameterIivNV(int target, int index, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramEnvParameterIivNV(int target, int index, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramEnvParameterIuivNV(int target, int index, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramEnvParameterIuivNV(int target, int index, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramEnvParameterdvARB(int target, int index, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramEnvParameterdvARB(int target, int index, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramEnvParameterfvARB(int target, int index, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramEnvParameterfvARB(int target, int index, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramLocalParameterIivNV(int target, int index, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramLocalParameterIivNV(int target, int index, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramLocalParameterIuivNV(int target, int index, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramLocalParameterIuivNV(int target, int index, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramLocalParameterdvARB(int target, int index, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramLocalParameterdvARB(int target, int index, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramLocalParameterfvARB(int target, int index, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramLocalParameterfvARB(int target, int index, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramStringARB(int target, int pname, Buffer string)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramSubroutineParameteruivNV(int target, int index, IntBuffer param)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramSubroutineParameteruivNV(int target, int index, int[] param, int param_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramivARB(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramivARB(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetQueryObjecti64vEXT(int id, int pname, LongBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetQueryObjecti64vEXT(int id, int pname, long[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetQueryObjectui64vEXT(int id, int pname, LongBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetQueryObjectui64vEXT(int id, int pname, long[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetSeparableFilter(int target, int format, int type, Buffer row, Buffer column, Buffer span)
    {
        methodCallCount++;
    }

    @Override
    public void glGetSeparableFilter(int target, int format, int type, long row_buffer_offset, long column_buffer_offset, long span_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetShaderSourceARB(int obj, int maxLength, IntBuffer length, ByteBuffer source)
    {
        methodCallCount++;
    }

    @Override
    public void glGetShaderSourceARB(int obj, int maxLength, int[] length, int length_offset, byte[] source, int source_offset)
    {
        methodCallCount++;
    }

    @Override
    public String glGetStringi(int name, int index)
    {
        return null;
    }

    @Override
    public void glGetTexGendv(int coord, int pname, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexGendv(int coord, int pname, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public long glGetTextureHandleNV(int texture)
    {
        return 0;
    }

    @Override
    public void glGetTextureImageEXT(int texture, int target, int level, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTextureLevelParameterfvEXT(int texture, int target, int level, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTextureLevelParameterfvEXT(int texture, int target, int level, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTextureLevelParameterivEXT(int texture, int target, int level, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTextureLevelParameterivEXT(int texture, int target, int level, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTextureParameterIivEXT(int texture, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTextureParameterIivEXT(int texture, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTextureParameterIuivEXT(int texture, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTextureParameterIuivEXT(int texture, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTextureParameterfvEXT(int texture, int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTextureParameterfvEXT(int texture, int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTextureParameterivEXT(int texture, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTextureParameterivEXT(int texture, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public long glGetTextureSamplerHandleNV(int texture, int sampler)
    {
        return 0;
    }

    @Override
    public void glGetTransformFeedbackVarying(int program, int index, int bufSize, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTransformFeedbackVarying(int program, int index, int bufSize, int[] length, int length_offset, int[] size, int size_offset, int[] type, int type_offset, byte[] name, int name_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glGetUniformBlockIndex(int program, String uniformBlockName)
    {
        return 0;
    }

    @Override
    public int glGetUniformBufferSizeEXT(int program, int location)
    {
        return 0;
    }

    @Override
    public void glGetUniformIndices(int program, int uniformCount, String[] uniformNames, IntBuffer uniformIndices)
    {
        methodCallCount++;
    }

    @Override
    public void glGetUniformIndices(int program, int uniformCount, String[] uniformNames, int[] uniformIndices, int uniformIndices_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glGetUniformLocationARB(int programObj, String name)
    {
        return 0;
    }

    @Override
    public long glGetUniformOffsetEXT(int program, int location)
    {
        return 0;
    }

    @Override
    public void glGetUniformfvARB(int programObj, int location, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetUniformfvARB(int programObj, int location, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetUniformivARB(int programObj, int location, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetUniformivARB(int programObj, int location, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetUniformuiv(int program, int location, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetUniformuiv(int program, int location, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVariantBooleanvEXT(int id, int value, ByteBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVariantBooleanvEXT(int id, int value, byte[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVariantFloatvEXT(int id, int value, FloatBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVariantFloatvEXT(int id, int value, float[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVariantIntegervEXT(int id, int value, IntBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVariantIntegervEXT(int id, int value, int[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexArrayIntegeri_vEXT(int vaobj, int index, int pname, IntBuffer param)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexArrayIntegeri_vEXT(int vaobj, int index, int pname, int[] param, int param_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexArrayIntegervEXT(int vaobj, int pname, IntBuffer param)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexArrayIntegervEXT(int vaobj, int pname, int[] param, int param_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexArrayPointeri_vEXT(int vaobj, int index, int pname, PointerBuffer param)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexArrayPointervEXT(int vaobj, int pname, PointerBuffer param)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribIiv(int index, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribIiv(int index, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribIivEXT(int index, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribIivEXT(int index, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribIuiv(int index, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribIuiv(int index, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribIuivEXT(int index, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribIuivEXT(int index, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribLi64vNV(int index, int pname, LongBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribLi64vNV(int index, int pname, long[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribLui64vNV(int index, int pname, LongBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribLui64vNV(int index, int pname, long[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribdvARB(int index, int pname, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribdvARB(int index, int pname, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribfvARB(int index, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribfvARB(int index, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribivARB(int index, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribivARB(int index, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVideoCaptureStreamdvNV(int video_capture_slot, int stream, int pname, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVideoCaptureStreamdvNV(int video_capture_slot, int stream, int pname, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVideoCaptureStreamfvNV(int video_capture_slot, int stream, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVideoCaptureStreamfvNV(int video_capture_slot, int stream, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVideoCaptureStreamivNV(int video_capture_slot, int stream, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVideoCaptureStreamivNV(int video_capture_slot, int stream, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVideoCaptureivNV(int video_capture_slot, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVideoCaptureivNV(int video_capture_slot, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnColorTable(int target, int format, int type, int bufSize, Buffer table)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnConvolutionFilter(int target, int format, int type, int bufSize, Buffer image)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnHistogram(int target, boolean reset, int format, int type, int bufSize, Buffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnMapdv(int target, int query, int bufSize, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnMapdv(int target, int query, int bufSize, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnMapfv(int target, int query, int bufSize, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnMapfv(int target, int query, int bufSize, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnMapiv(int target, int query, int bufSize, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnMapiv(int target, int query, int bufSize, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnMinmax(int target, boolean reset, int format, int type, int bufSize, Buffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnPixelMapfv(int map, int bufSize, FloatBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnPixelMapfv(int map, int bufSize, float[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnPixelMapuiv(int map, int bufSize, IntBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnPixelMapuiv(int map, int bufSize, int[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnPixelMapusv(int map, int bufSize, ShortBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnPixelMapusv(int map, int bufSize, short[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnPolygonStipple(int bufSize, ByteBuffer pattern)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnPolygonStipple(int bufSize, byte[] pattern, int pattern_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnSeparableFilter(int target, int format, int type, int rowBufSize, Buffer row, int columnBufSize, Buffer column, Buffer span)
    {
        methodCallCount++;
    }

    @Override
    public void glHintPGI(int target, int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glHistogram(int target, int width, int internalformat, boolean sink)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexFuncEXT(int func, float ref)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexMask(int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexMaterialEXT(int face, int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexPointer(int type, int stride, Buffer ptr)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexd(double c)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexdv(DoubleBuffer c)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexdv(double[] c, int c_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexf(float c)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexfv(FloatBuffer c)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexfv(float[] c, int c_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexi(int c)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexiv(IntBuffer c)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexiv(int[] c, int c_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexs(short c)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexsv(ShortBuffer c)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexsv(short[] c, int c_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexub(byte c)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexubv(ByteBuffer c)
    {
        methodCallCount++;
    }

    @Override
    public void glIndexubv(byte[] c, int c_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glInitNames()
    {
        methodCallCount++;
    }

    @Override
    public void glInsertComponentEXT(int res, int src, int num)
    {
        methodCallCount++;
    }

    @Override
    public void glInterleavedArrays(int format, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glInterleavedArrays(int format, int stride, long pointer_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glInterpolatePathsNV(int resultPath, int pathA, int pathB, float weight)
    {
        methodCallCount++;
    }

    @Override
    public boolean glIsEnabledIndexed(int target, int index)
    {
        return false;
    }

    @Override
    public boolean glIsFenceAPPLE(int fence)
    {
        return false;
    }

    @Override
    public boolean glIsFenceNV(int fence)
    {
        return false;
    }

    @Override
    public boolean glIsImageHandleResidentNV(long handle)
    {
        return false;
    }

    @Override
    public boolean glIsList(int list)
    {
        return false;
    }

    @Override
    public boolean glIsNameAMD(int identifier, int name)
    {
        return false;
    }

    @Override
    public boolean glIsOcclusionQueryNV(int id)
    {
        return false;
    }

    @Override
    public boolean glIsPathNV(int path)
    {
        return false;
    }

    @Override
    public boolean glIsPointInFillPathNV(int path, int mask, float x, float y)
    {
        return false;
    }

    @Override
    public boolean glIsPointInStrokePathNV(int path, float x, float y)
    {
        return false;
    }

    @Override
    public boolean glIsProgramARB(int program)
    {
        return false;
    }

    @Override
    public boolean glIsTextureHandleResidentNV(long handle)
    {
        return false;
    }

    @Override
    public boolean glIsTransformFeedbackNV(int id)
    {
        return false;
    }

    @Override
    public boolean glIsVariantEnabledEXT(int id, int cap)
    {
        return false;
    }

    @Override
    public boolean glIsVertexArray(int array)
    {
        return false;
    }

    @Override
    public boolean glIsVertexAttribEnabledAPPLE(int index, int pname)
    {
        return false;
    }

    @Override
    public void glLightModeli(int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glLightModeliv(int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glLightModeliv(int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glLighti(int light, int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glLightiv(int light, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glLightiv(int light, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glLineStipple(int factor, short pattern)
    {
        methodCallCount++;
    }

    @Override
    public void glLinkProgramARB(int programObj)
    {
        methodCallCount++;
    }

    @Override
    public void glListBase(int base)
    {
        methodCallCount++;
    }

    @Override
    public void glLoadMatrixd(DoubleBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glLoadMatrixd(double[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glLoadName(int name)
    {
        methodCallCount++;
    }

    @Override
    public void glLoadTransposeMatrixd(DoubleBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glLoadTransposeMatrixd(double[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glLoadTransposeMatrixf(FloatBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glLoadTransposeMatrixf(float[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glLockArraysEXT(int first, int count)
    {
        methodCallCount++;
    }

    @Override
    public void glMakeImageHandleNonResidentNV(long handle)
    {
        methodCallCount++;
    }

    @Override
    public void glMakeImageHandleResidentNV(long handle, int access)
    {
        methodCallCount++;
    }

    @Override
    public void glMakeTextureHandleNonResidentNV(long handle)
    {
        methodCallCount++;
    }

    @Override
    public void glMakeTextureHandleResidentNV(long handle)
    {
        methodCallCount++;
    }

    @Override
    public void glMap1d(int target, double u1, double u2, int stride, int order, DoubleBuffer points)
    {
        methodCallCount++;
    }

    @Override
    public void glMap1d(int target, double u1, double u2, int stride, int order, double[] points, int points_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMap1f(int target, float u1, float u2, int stride, int order, FloatBuffer points)
    {
        methodCallCount++;
    }

    @Override
    public void glMap1f(int target, float u1, float u2, int stride, int order, float[] points, int points_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMap2d(int target, double u1, double u2, int ustride, int uorder, double v1, double v2, int vstride, int vorder, DoubleBuffer points)
    {
        methodCallCount++;
    }

    @Override
    public void glMap2d(int target, double u1, double u2, int ustride, int uorder, double v1, double v2, int vstride, int vorder, double[] points, int points_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMap2f(int target, float u1, float u2, int ustride, int uorder, float v1, float v2, int vstride, int vorder, FloatBuffer points)
    {
        methodCallCount++;
    }

    @Override
    public void glMap2f(int target, float u1, float u2, int ustride, int uorder, float v1, float v2, int vstride, int vorder, float[] points, int points_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMapControlPointsNV(int target, int index, int type, int ustride, int vstride, int uorder, int vorder, boolean packed, Buffer points)
    {
        methodCallCount++;
    }

    @Override
    public void glMapGrid1d(int un, double u1, double u2)
    {
        methodCallCount++;
    }

    @Override
    public void glMapGrid1f(int un, float u1, float u2)
    {
        methodCallCount++;
    }

    @Override
    public void glMapGrid2d(int un, double u1, double u2, int vn, double v1, double v2)
    {
        methodCallCount++;
    }

    @Override
    public void glMapGrid2f(int un, float u1, float u2, int vn, float v1, float v2)
    {
        methodCallCount++;
    }

    @Override
    public ByteBuffer glMapNamedBufferEXT(int buffer, int access)
    {
        return null;
    }

    @Override
    public ByteBuffer glMapNamedBufferRangeEXT(int buffer, long offset, long length, int access)
    {
        return null;
    }

    @Override
    public void glMapParameterfvNV(int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glMapParameterfvNV(int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMapParameterivNV(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glMapParameterivNV(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public ByteBuffer glMapTexture2DINTEL(int texture, int level, int access, IntBuffer stride, IntBuffer layout)
    {
        return null;
    }

    @Override
    public ByteBuffer glMapTexture2DINTEL(int texture, int level, int access, int[] stride, int stride_offset, int[] layout, int layout_offset)
    {
        return null;
    }

    @Override
    public void glMapVertexAttrib1dAPPLE(int index, int size, double u1, double u2, int stride, int order, DoubleBuffer points)
    {
        methodCallCount++;
    }

    @Override
    public void glMapVertexAttrib1dAPPLE(int index, int size, double u1, double u2, int stride, int order, double[] points, int points_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMapVertexAttrib1fAPPLE(int index, int size, float u1, float u2, int stride, int order, FloatBuffer points)
    {
        methodCallCount++;
    }

    @Override
    public void glMapVertexAttrib1fAPPLE(int index, int size, float u1, float u2, int stride, int order, float[] points, int points_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMapVertexAttrib2dAPPLE(int index, int size, double u1, double u2, int ustride, int uorder, double v1, double v2, int vstride, int vorder, DoubleBuffer points)
    {
        methodCallCount++;
    }

    @Override
    public void glMapVertexAttrib2dAPPLE(int index, int size, double u1, double u2, int ustride, int uorder, double v1, double v2, int vstride, int vorder, double[] points, int points_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMapVertexAttrib2fAPPLE(int index, int size, float u1, float u2, int ustride, int uorder, float v1, float v2, int vstride, int vorder, FloatBuffer points)
    {
        methodCallCount++;
    }

    @Override
    public void glMapVertexAttrib2fAPPLE(int index, int size, float u1, float u2, int ustride, int uorder, float v1, float v2, int vstride, int vorder, float[] points, int points_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMateriali(int face, int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glMaterialiv(int face, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glMaterialiv(int face, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixFrustumEXT(int mode, double left, double right, double bottom, double top, double zNear, double zFar)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixIndexPointer(int size, int type, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixIndexubvARB(int size, ByteBuffer indices)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixIndexubvARB(int size, byte[] indices, int indices_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixIndexuivARB(int size, IntBuffer indices)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixIndexuivARB(int size, int[] indices, int indices_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixIndexusvARB(int size, ShortBuffer indices)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixIndexusvARB(int size, short[] indices, int indices_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixLoadIdentityEXT(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixLoadTransposedEXT(int mode, DoubleBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixLoadTransposedEXT(int mode, double[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixLoadTransposefEXT(int mode, FloatBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixLoadTransposefEXT(int mode, float[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixLoaddEXT(int mode, DoubleBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixLoaddEXT(int mode, double[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixLoadfEXT(int mode, FloatBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixLoadfEXT(int mode, float[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixMultTransposedEXT(int mode, DoubleBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixMultTransposedEXT(int mode, double[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixMultTransposefEXT(int mode, FloatBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixMultTransposefEXT(int mode, float[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixMultdEXT(int mode, DoubleBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixMultdEXT(int mode, double[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixMultfEXT(int mode, FloatBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixMultfEXT(int mode, float[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixOrthoEXT(int mode, double left, double right, double bottom, double top, double zNear, double zFar)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixPopEXT(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixPushEXT(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixRotatedEXT(int mode, double angle, double x, double y, double z)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixRotatefEXT(int mode, float angle, float x, float y, float z)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixScaledEXT(int mode, double x, double y, double z)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixScalefEXT(int mode, float x, float y, float z)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixTranslatedEXT(int mode, double x, double y, double z)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixTranslatefEXT(int mode, float x, float y, float z)
    {
        methodCallCount++;
    }

    @Override
    public void glMinmax(int target, int internalformat, boolean sink)
    {
        methodCallCount++;
    }

    @Override
    public void glMultMatrixd(DoubleBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glMultMatrixd(double[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultTransposeMatrixd(DoubleBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glMultTransposeMatrixd(double[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultTransposeMatrixf(FloatBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glMultTransposeMatrixf(float[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexBufferEXT(int texunit, int target, int internalformat, int buffer)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1bOES(int texture, byte s)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1bvOES(int texture, ByteBuffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1bvOES(int texture, byte[] coords, int coords_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1d(int target, double s)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1dv(int target, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1dv(int target, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1f(int target, float s)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1fv(int target, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1fv(int target, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1h(int target, short s)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1hv(int target, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1hv(int target, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1i(int target, int s)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1iv(int target, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1iv(int target, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1s(int target, short s)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1sv(int target, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord1sv(int target, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2bOES(int texture, byte s, byte t)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2bvOES(int texture, ByteBuffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2bvOES(int texture, byte[] coords, int coords_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2d(int target, double s, double t)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2dv(int target, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2dv(int target, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2f(int target, float s, float t)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2fv(int target, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2fv(int target, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2h(int target, short s, short t)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2hv(int target, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2hv(int target, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2i(int target, int s, int t)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2iv(int target, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2iv(int target, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2s(int target, short s, short t)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2sv(int target, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord2sv(int target, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3bOES(int texture, byte s, byte t, byte r)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3bvOES(int texture, ByteBuffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3bvOES(int texture, byte[] coords, int coords_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3d(int target, double s, double t, double r)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3dv(int target, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3dv(int target, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3f(int target, float s, float t, float r)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3fv(int target, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3fv(int target, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3h(int target, short s, short t, short r)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3hv(int target, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3hv(int target, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3i(int target, int s, int t, int r)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3iv(int target, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3iv(int target, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3s(int target, short s, short t, short r)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3sv(int target, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord3sv(int target, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4bOES(int texture, byte s, byte t, byte r, byte q)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4bvOES(int texture, ByteBuffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4bvOES(int texture, byte[] coords, int coords_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4d(int target, double s, double t, double r, double q)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4dv(int target, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4dv(int target, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4fv(int target, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4fv(int target, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4h(int target, short s, short t, short r, short q)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4hv(int target, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4hv(int target, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4i(int target, int s, int t, int r, int q)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4iv(int target, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4iv(int target, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4s(int target, short s, short t, short r, short q)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4sv(int target, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4sv(int target, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoordPointerEXT(int texunit, int size, int type, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexEnvfEXT(int texunit, int target, int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexEnvfvEXT(int texunit, int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexEnvfvEXT(int texunit, int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexEnviEXT(int texunit, int target, int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexEnvivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexEnvivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexGendEXT(int texunit, int coord, int pname, double param)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexGendvEXT(int texunit, int coord, int pname, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexGendvEXT(int texunit, int coord, int pname, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexGenfEXT(int texunit, int coord, int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexGenfvEXT(int texunit, int coord, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexGenfvEXT(int texunit, int coord, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexGeniEXT(int texunit, int coord, int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexGenivEXT(int texunit, int coord, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexGenivEXT(int texunit, int coord, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width, int border, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexImage3DEXT(int texunit, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexParameterIivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexParameterIivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexParameterIuivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexParameterIuivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexParameterfEXT(int texunit, int target, int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexParameterfvEXT(int texunit, int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexParameterfvEXT(int texunit, int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexParameteriEXT(int texunit, int target, int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexParameterivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexParameterivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexRenderbufferEXT(int texunit, int target, int renderbuffer)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int width, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedBufferDataEXT(int buffer, long size, Buffer data, int usage)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedBufferSubDataEXT(int buffer, long offset, long size, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedCopyBufferSubDataEXT(int readBuffer, int writeBuffer, long readOffset, long writeOffset, long size)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedFramebufferRenderbufferEXT(int framebuffer, int attachment, int renderbuffertarget, int renderbuffer)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedFramebufferTexture1DEXT(int framebuffer, int attachment, int textarget, int texture, int level)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedFramebufferTexture2DEXT(int framebuffer, int attachment, int textarget, int texture, int level)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedFramebufferTexture3DEXT(int framebuffer, int attachment, int textarget, int texture, int level, int zoffset)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedFramebufferTextureEXT(int framebuffer, int attachment, int texture, int level)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedFramebufferTextureFaceEXT(int framebuffer, int attachment, int texture, int level, int face)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedFramebufferTextureLayerEXT(int framebuffer, int attachment, int texture, int level, int layer)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameter4dEXT(int program, int target, int index, double x, double y, double z, double w)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameter4dvEXT(int program, int target, int index, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameter4dvEXT(int program, int target, int index, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameter4fEXT(int program, int target, int index, float x, float y, float z, float w)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameter4fvEXT(int program, int target, int index, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameter4fvEXT(int program, int target, int index, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameterI4iEXT(int program, int target, int index, int x, int y, int z, int w)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameterI4ivEXT(int program, int target, int index, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameterI4ivEXT(int program, int target, int index, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameterI4uiEXT(int program, int target, int index, int x, int y, int z, int w)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameterI4uivEXT(int program, int target, int index, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameterI4uivEXT(int program, int target, int index, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameters4fvEXT(int program, int target, int index, int count, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParameters4fvEXT(int program, int target, int index, int count, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParametersI4ivEXT(int program, int target, int index, int count, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParametersI4ivEXT(int program, int target, int index, int count, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParametersI4uivEXT(int program, int target, int index, int count, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramLocalParametersI4uivEXT(int program, int target, int index, int count, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedProgramStringEXT(int program, int target, int format, int len, Buffer string)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedRenderbufferStorageEXT(int renderbuffer, int internalformat, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedRenderbufferStorageMultisampleCoverageEXT(int renderbuffer, int coverageSamples, int colorSamples, int internalformat, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedRenderbufferStorageMultisampleEXT(int renderbuffer, int samples, int internalformat, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glNewList(int list, int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3b(byte nx, byte ny, byte nz)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3bv(ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3bv(byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3d(double nx, double ny, double nz)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3h(short nx, short ny, short nz)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3hv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3hv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3i(int nx, int ny, int nz)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3s(short nx, short ny, short nz)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glObjectPurgeableAPPLE(int objectType, int name, int option)
    {
        return 0;
    }

    @Override
    public int glObjectUnpurgeableAPPLE(int objectType, int name, int option)
    {
        return 0;
    }

    @Override
    public void glPNTrianglesfATI(int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glPNTrianglesiATI(int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glPassThrough(float token)
    {
        methodCallCount++;
    }

    @Override
    public void glPathColorGenNV(int color, int genMode, int colorFormat, FloatBuffer coeffs)
    {
        methodCallCount++;
    }

    @Override
    public void glPathColorGenNV(int color, int genMode, int colorFormat, float[] coeffs, int coeffs_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPathCommandsNV(int path, int numCommands, ByteBuffer commands, int numCoords, int coordType, Buffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glPathCommandsNV(int path, int numCommands, byte[] commands, int commands_offset, int numCoords, int coordType, Buffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glPathCoordsNV(int path, int numCoords, int coordType, Buffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glPathCoverDepthFuncNV(int func)
    {
        methodCallCount++;
    }

    @Override
    public void glPathDashArrayNV(int path, int dashCount, FloatBuffer dashArray)
    {
        methodCallCount++;
    }

    @Override
    public void glPathDashArrayNV(int path, int dashCount, float[] dashArray, int dashArray_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPathFogGenNV(int genMode)
    {
        methodCallCount++;
    }

    @Override
    public void glPathGlyphRangeNV(int firstPathName, int fontTarget, Buffer fontName, int fontStyle, int firstGlyph, int numGlyphs, int handleMissingGlyphs, int pathParameterTemplate, float emScale)
    {
        methodCallCount++;
    }

    @Override
    public void glPathGlyphsNV(int firstPathName, int fontTarget, Buffer fontName, int fontStyle, int numGlyphs, int type, Buffer charcodes, int handleMissingGlyphs, int pathParameterTemplate, float emScale)
    {
        methodCallCount++;
    }

    @Override
    public void glPathParameterfNV(int path, int pname, float value)
    {
        methodCallCount++;
    }

    @Override
    public void glPathParameterfvNV(int path, int pname, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glPathParameterfvNV(int path, int pname, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPathParameteriNV(int path, int pname, int value)
    {
        methodCallCount++;
    }

    @Override
    public void glPathParameterivNV(int path, int pname, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glPathParameterivNV(int path, int pname, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPathStencilDepthOffsetNV(float factor, float units)
    {
        methodCallCount++;
    }

    @Override
    public void glPathStencilFuncNV(int func, int ref, int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glPathStringNV(int path, int format, int length, Buffer pathString)
    {
        methodCallCount++;
    }

    @Override
    public void glPathSubCommandsNV(int path, int commandStart, int commandsToDelete, int numCommands, ByteBuffer commands, int numCoords, int coordType, Buffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glPathSubCommandsNV(int path, int commandStart, int commandsToDelete, int numCommands, byte[] commands, int commands_offset, int numCoords, int coordType, Buffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glPathSubCoordsNV(int path, int coordStart, int numCoords, int coordType, Buffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glPathTexGenNV(int texCoordSet, int genMode, int components, FloatBuffer coeffs)
    {
        methodCallCount++;
    }

    @Override
    public void glPathTexGenNV(int texCoordSet, int genMode, int components, float[] coeffs, int coeffs_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPauseTransformFeedbackNV()
    {
        methodCallCount++;
    }

    @Override
    public void glPixelDataRangeNV(int target, int length, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelMapfv(int map, int mapsize, FloatBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelMapfv(int map, int mapsize, float[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelMapfv(int map, int mapsize, long values_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelMapuiv(int map, int mapsize, IntBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelMapuiv(int map, int mapsize, int[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelMapuiv(int map, int mapsize, long values_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelMapusv(int map, int mapsize, ShortBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelMapusv(int map, int mapsize, short[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelMapusv(int map, int mapsize, long values_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelTransferf(int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelTransferi(int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelTransformParameterfEXT(int target, int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelTransformParameterfvEXT(int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelTransformParameterfvEXT(int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelTransformParameteriEXT(int target, int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelTransformParameterivEXT(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelTransformParameterivEXT(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelZoom(float xfactor, float yfactor)
    {
        methodCallCount++;
    }

    @Override
    public boolean glPointAlongPathNV(int path, int startSegment, int numSegments, float distance, FloatBuffer x, FloatBuffer y, FloatBuffer tangentX, FloatBuffer tangentY)
    {
        return false;
    }

    @Override
    public boolean glPointAlongPathNV(int path, int startSegment, int numSegments, float distance, float[] x, int x_offset, float[] y, int y_offset, float[] tangentX, int tangentX_offset, float[] tangentY, int tangentY_offset)
    {
        return false;
    }

    @Override
    public void glPolygonStipple(ByteBuffer mask)
    {
        methodCallCount++;
    }

    @Override
    public void glPolygonStipple(byte[] mask, int mask_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPolygonStipple(long mask_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPopAttrib()
    {
        methodCallCount++;
    }

    @Override
    public void glPopClientAttrib()
    {
        methodCallCount++;
    }

    @Override
    public void glPopName()
    {
        methodCallCount++;
    }

    @Override
    public void glPrimitiveRestartIndexNV(int index)
    {
        methodCallCount++;
    }

    @Override
    public void glPrimitiveRestartNV()
    {
        methodCallCount++;
    }

    @Override
    public void glPrioritizeTextures(int n, IntBuffer textures, FloatBuffer priorities)
    {
        methodCallCount++;
    }

    @Override
    public void glPrioritizeTextures(int n, int[] textures, int textures_offset, float[] priorities, int priorities_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramBufferParametersIivNV(int target, int bindingIndex, int wordIndex, int count, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramBufferParametersIivNV(int target, int bindingIndex, int wordIndex, int count, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramBufferParametersIuivNV(int target, int bindingIndex, int wordIndex, int count, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramBufferParametersIuivNV(int target, int bindingIndex, int wordIndex, int count, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramBufferParametersfvNV(int target, int bindingIndex, int wordIndex, int count, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramBufferParametersfvNV(int target, int bindingIndex, int wordIndex, int count, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameter4dARB(int target, int index, double x, double y, double z, double w)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameter4dvARB(int target, int index, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameter4dvARB(int target, int index, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameter4fARB(int target, int index, float x, float y, float z, float w)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameter4fvARB(int target, int index, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameter4fvARB(int target, int index, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameterI4iNV(int target, int index, int x, int y, int z, int w)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameterI4ivNV(int target, int index, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameterI4ivNV(int target, int index, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameterI4uiNV(int target, int index, int x, int y, int z, int w)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameterI4uivNV(int target, int index, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameterI4uivNV(int target, int index, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameters4fvEXT(int target, int index, int count, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParameters4fvEXT(int target, int index, int count, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParametersI4ivNV(int target, int index, int count, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParametersI4ivNV(int target, int index, int count, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParametersI4uivNV(int target, int index, int count, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramEnvParametersI4uivNV(int target, int index, int count, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameter4dARB(int target, int index, double x, double y, double z, double w)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameter4dvARB(int target, int index, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameter4dvARB(int target, int index, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameter4fARB(int target, int index, float x, float y, float z, float w)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameter4fvARB(int target, int index, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameter4fvARB(int target, int index, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameterI4iNV(int target, int index, int x, int y, int z, int w)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameterI4ivNV(int target, int index, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameterI4ivNV(int target, int index, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameterI4uiNV(int target, int index, int x, int y, int z, int w)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameterI4uivNV(int target, int index, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameterI4uivNV(int target, int index, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameters4fvEXT(int target, int index, int count, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParameters4fvEXT(int target, int index, int count, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParametersI4ivNV(int target, int index, int count, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParametersI4ivNV(int target, int index, int count, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParametersI4uivNV(int target, int index, int count, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramLocalParametersI4uivNV(int target, int index, int count, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramStringARB(int target, int format, int len, String string)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramSubroutineParametersuivNV(int target, int count, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramSubroutineParametersuivNV(int target, int count, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniform1uiEXT(int program, int location, int v0)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniform1uivEXT(int program, int location, int count, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniform1uivEXT(int program, int location, int count, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniform2uiEXT(int program, int location, int v0, int v1)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniform2uivEXT(int program, int location, int count, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniform2uivEXT(int program, int location, int count, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniform3uiEXT(int program, int location, int v0, int v1, int v2)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniform3uivEXT(int program, int location, int count, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniform3uivEXT(int program, int location, int count, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniform4uiEXT(int program, int location, int v0, int v1, int v2, int v3)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniform4uivEXT(int program, int location, int count, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniform4uivEXT(int program, int location, int count, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformHandleui64NV(int program, int location, long value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformHandleui64vNV(int program, int location, int count, LongBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformHandleui64vNV(int program, int location, int count, long[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformMatrix2x3fvEXT(int program, int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformMatrix2x3fvEXT(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformMatrix2x4fvEXT(int program, int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformMatrix2x4fvEXT(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformMatrix3x2fvEXT(int program, int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformMatrix3x2fvEXT(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformMatrix3x4fvEXT(int program, int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformMatrix3x4fvEXT(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformMatrix4x2fvEXT(int program, int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformMatrix4x2fvEXT(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformMatrix4x3fvEXT(int program, int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformMatrix4x3fvEXT(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramVertexLimitNV(int target, int limit)
    {
        methodCallCount++;
    }

    @Override
    public void glProvokingVertexEXT(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glPushAttrib(int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glPushClientAttrib(int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glPushClientAttribDefaultEXT(int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glPushName(int name)
    {
        methodCallCount++;
    }

    @Override
    public int glQueryMatrixxOES(IntBuffer mantissa, IntBuffer exponent)
    {
        return 0;
    }

    @Override
    public int glQueryMatrixxOES(int[] mantissa, int mantissa_offset, int[] exponent, int exponent_offset)
    {
        return 0;
    }

    @Override
    public void glRasterPos2d(double x, double y)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos2dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos2dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos2f(float x, float y)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos2fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos2fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos2i(int x, int y)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos2iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos2iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos2s(short x, short y)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos2sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos2sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos3d(double x, double y, double z)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos3dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos3dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos3f(float x, float y, float z)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos3fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos3fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos3i(int x, int y, int z)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos3iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos3iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos3s(short x, short y, short z)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos3sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos3sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos4d(double x, double y, double z, double w)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos4dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos4dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos4f(float x, float y, float z, float w)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos4fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos4fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos4i(int x, int y, int z, int w)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos4iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos4iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos4s(short x, short y, short z, short w)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos4sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glRasterPos4sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glReadBuffer(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glRectd(double x1, double y1, double x2, double y2)
    {
        methodCallCount++;
    }

    @Override
    public void glRectdv(DoubleBuffer v1, DoubleBuffer v2)
    {
        methodCallCount++;
    }

    @Override
    public void glRectdv(double[] v1, int v1_offset, double[] v2, int v2_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRectf(float x1, float y1, float x2, float y2)
    {
        methodCallCount++;
    }

    @Override
    public void glRectfv(FloatBuffer v1, FloatBuffer v2)
    {
        methodCallCount++;
    }

    @Override
    public void glRectfv(float[] v1, int v1_offset, float[] v2, int v2_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRecti(int x1, int y1, int x2, int y2)
    {
        methodCallCount++;
    }

    @Override
    public void glRectiv(IntBuffer v1, IntBuffer v2)
    {
        methodCallCount++;
    }

    @Override
    public void glRectiv(int[] v1, int v1_offset, int[] v2, int v2_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glRects(short x1, short y1, short x2, short y2)
    {
        methodCallCount++;
    }

    @Override
    public void glRectsv(ShortBuffer v1, ShortBuffer v2)
    {
        methodCallCount++;
    }

    @Override
    public void glRectsv(short[] v1, int v1_offset, short[] v2, int v2_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glRenderMode(int mode)
    {
        return 0;
    }

    @Override
    public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glRenderbufferStorageMultisampleCoverageNV(int target, int coverageSamples, int colorSamples, int internalformat, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glResetHistogram(int target)
    {
        methodCallCount++;
    }

    @Override
    public void glResetMinmax(int target)
    {
        methodCallCount++;
    }

    @Override
    public void glResumeTransformFeedbackNV()
    {
        methodCallCount++;
    }

    @Override
    public void glRotated(double angle, double x, double y, double z)
    {
        methodCallCount++;
    }

    @Override
    public void glSampleMaskIndexedNV(int index, int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glScaled(double x, double y, double z)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3b(byte red, byte green, byte blue)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3bv(ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3bv(byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3d(double red, double green, double blue)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3f(float red, float green, float blue)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3h(short red, short green, short blue)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3hv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3hv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3i(int red, int green, int blue)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3s(short red, short green, short blue)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3ub(byte red, byte green, byte blue)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3ubv(ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3ubv(byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3ui(int red, int green, int blue)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3uiv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3uiv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3us(short red, short green, short blue)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3usv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColor3usv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColorPointer(int size, int type, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColorPointer(int size, int type, int stride, long pointer_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glSelectBuffer(int size, IntBuffer buffer)
    {
        methodCallCount++;
    }

    @Override
    public void glSelectPerfMonitorCountersAMD(int monitor, boolean enable, int group, int numCounters, IntBuffer counterList)
    {
        methodCallCount++;
    }

    @Override
    public void glSelectPerfMonitorCountersAMD(int monitor, boolean enable, int group, int numCounters, int[] counterList, int counterList_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type, Buffer row, Buffer column)
    {
        methodCallCount++;
    }

    @Override
    public void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type, long row_buffer_offset, long column_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glSetFenceAPPLE(int fence)
    {
        methodCallCount++;
    }

    @Override
    public void glSetFenceNV(int fence, int condition)
    {
        methodCallCount++;
    }

    @Override
    public void glSetInvariantEXT(int id, int type, Buffer addr)
    {
        methodCallCount++;
    }

    @Override
    public void glSetLocalConstantEXT(int id, int type, Buffer addr)
    {
        methodCallCount++;
    }

    @Override
    public void glShaderOp1EXT(int op, int res, int arg1)
    {
        methodCallCount++;
    }

    @Override
    public void glShaderOp2EXT(int op, int res, int arg1, int arg2)
    {
        methodCallCount++;
    }

    @Override
    public void glShaderOp3EXT(int op, int res, int arg1, int arg2, int arg3)
    {
        methodCallCount++;
    }

    @Override
    public void glShaderSourceARB(int shaderObj, int count, String[] string, IntBuffer length)
    {
        methodCallCount++;
    }

    @Override
    public void glShaderSourceARB(int shaderObj, int count, String[] string, int[] length, int length_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilClearTagEXT(int stencilTagBits, int stencilClearTag)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilFillPathInstancedNV(int numPaths, int pathNameType, Buffer paths, int pathBase, int fillMode, int mask, int transformType, FloatBuffer transformValues)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilFillPathInstancedNV(int numPaths, int pathNameType, Buffer paths, int pathBase, int fillMode, int mask, int transformType, float[] transformValues, int transformValues_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilFillPathNV(int path, int fillMode, int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilStrokePathInstancedNV(int numPaths, int pathNameType, Buffer paths, int pathBase, int reference, int mask, int transformType, FloatBuffer transformValues)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilStrokePathInstancedNV(int numPaths, int pathNameType, Buffer paths, int pathBase, int reference, int mask, int transformType, float[] transformValues, int transformValues_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilStrokePathNV(int path, int reference, int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glStringMarkerGREMEDY(int len, Buffer string)
    {
        methodCallCount++;
    }

    @Override
    public void glSwizzleEXT(int res, int in, int outX, int outY, int outZ, int outW)
    {
        methodCallCount++;
    }

    @Override
    public void glSyncTextureINTEL(int texture)
    {
        methodCallCount++;
    }

    @Override
    public boolean glTestFenceAPPLE(int fence)
    {
        return false;
    }

    @Override
    public boolean glTestFenceNV(int fence)
    {
        return false;
    }

    @Override
    public boolean glTestObjectAPPLE(int object, int name)
    {
        return false;
    }

    @Override
    public void glTexCoord1bOES(byte s)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1bvOES(ByteBuffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1bvOES(byte[] coords, int coords_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1d(double s)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1f(float s)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1h(short s)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1hv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1hv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1i(int s)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1s(short s)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord1sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2bOES(byte s, byte t)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2bvOES(ByteBuffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2bvOES(byte[] coords, int coords_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2d(double s, double t)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2f(float s, float t)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2h(short s, short t)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2hv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2hv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2i(int s, int t)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2s(short s, short t)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord2sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3bOES(byte s, byte t, byte r)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3bvOES(ByteBuffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3bvOES(byte[] coords, int coords_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3d(double s, double t, double r)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3f(float s, float t, float r)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3h(short s, short t, short r)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3hv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3hv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3i(int s, int t, int r)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3s(short s, short t, short r)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord3sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4bOES(byte s, byte t, byte r, byte q)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4bvOES(ByteBuffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4bvOES(byte[] coords, int coords_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4d(double s, double t, double r, double q)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4f(float s, float t, float r, float q)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4h(short s, short t, short r, short q)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4hv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4hv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4i(int s, int t, int r, int q)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4s(short s, short t, short r, short q)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoord4sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexGend(int coord, int pname, double param)
    {
        methodCallCount++;
    }

    @Override
    public void glTexGendv(int coord, int pname, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glTexGendv(int coord, int pname, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexRenderbufferNV(int target, int renderbuffer)
    {
        methodCallCount++;
    }

    @Override
    public void glTexStorageSparseAMD(int target, int internalFormat, int width, int height, int depth, int layers, int flags)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureBarrierNV()
    {
        methodCallCount++;
    }

    @Override
    public void glTextureBufferEXT(int texture, int target, int internalformat, int buffer)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width, int border, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureLightEXT(int pname)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureMaterialEXT(int face, int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureNormalEXT(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureParameterIivEXT(int texture, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureParameterIivEXT(int texture, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureParameterIuivEXT(int texture, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureParameterIuivEXT(int texture, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureParameterfEXT(int texture, int target, int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureParameterfvEXT(int texture, int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureParameterfvEXT(int texture, int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureParameteriEXT(int texture, int target, int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureParameterivEXT(int texture, int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureParameterivEXT(int texture, int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureRangeAPPLE(int target, int length, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureRenderbufferEXT(int texture, int target, int renderbuffer)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureStorageSparseAMD(int texture, int target, int internalFormat, int width, int height, int depth, int layers, int flags)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glTransformFeedbackVaryings(int program, int count, String[] varyings, int bufferMode)
    {
        methodCallCount++;
    }

    @Override
    public void glTransformPathNV(int resultPath, int srcPath, int transformType, FloatBuffer transformValues)
    {
        methodCallCount++;
    }

    @Override
    public void glTransformPathNV(int resultPath, int srcPath, int transformType, float[] transformValues, int transformValues_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTranslated(double x, double y, double z)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1fARB(int location, float v0)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1fvARB(int location, int count, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1fvARB(int location, int count, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1iARB(int location, int v0)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1ivARB(int location, int count, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1ivARB(int location, int count, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1ui(int location, int v0)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1uiv(int location, int count, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1uiv(int location, int count, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2fARB(int location, float v0, float v1)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2fvARB(int location, int count, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2fvARB(int location, int count, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2iARB(int location, int v0, int v1)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2ivARB(int location, int count, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2ivARB(int location, int count, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2ui(int location, int v0, int v1)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2uiv(int location, int count, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2uiv(int location, int count, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3fARB(int location, float v0, float v1, float v2)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3fvARB(int location, int count, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3fvARB(int location, int count, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3iARB(int location, int v0, int v1, int v2)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3ivARB(int location, int count, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3ivARB(int location, int count, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3ui(int location, int v0, int v1, int v2)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3uiv(int location, int count, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3uiv(int location, int count, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4fARB(int location, float v0, float v1, float v2, float v3)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4fvARB(int location, int count, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4fvARB(int location, int count, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4iARB(int location, int v0, int v1, int v2, int v3)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4ivARB(int location, int count, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4ivARB(int location, int count, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4ui(int location, int v0, int v1, int v2, int v3)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4uiv(int location, int count, IntBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4uiv(int location, int count, int[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformBufferEXT(int program, int location, int buffer)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformHandleui64NV(int location, long value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformHandleui64vNV(int location, int count, LongBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformHandleui64vNV(int location, int count, long[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix2fvARB(int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix2fvARB(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix2x3fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix2x3fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix2x4fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix2x4fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix3fvARB(int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix3fvARB(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix3x2fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix3x2fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix3x4fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix3x4fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix4fvARB(int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix4fvARB(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix4x2fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix4x2fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix4x3fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix4x3fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUnlockArraysEXT()
    {
        methodCallCount++;
    }

    @Override
    public boolean glUnmapNamedBufferEXT(int buffer)
    {
        return false;
    }

    @Override
    public void glUnmapTexture2DINTEL(int texture, int level)
    {
        methodCallCount++;
    }

    @Override
    public void glUseProgramObjectARB(int programObj)
    {
        methodCallCount++;
    }

    @Override
    public void glVDPAUFiniNV()
    {
        methodCallCount++;
    }

    @Override
    public void glVDPAUGetSurfaceivNV(long surface, int pname, int bufSize, IntBuffer length, IntBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glVDPAUGetSurfaceivNV(long surface, int pname, int bufSize, int[] length, int length_offset, int[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVDPAUInitNV(Buffer vdpDevice, Buffer getProcAddress)
    {
        methodCallCount++;
    }

    @Override
    public void glVDPAUIsSurfaceNV(long surface)
    {
        methodCallCount++;
    }

    @Override
    public void glVDPAUMapSurfacesNV(int numSurfaces, PointerBuffer surfaces)
    {
        methodCallCount++;
    }

    @Override
    public long glVDPAURegisterOutputSurfaceNV(Buffer vdpSurface, int target, int numTextureNames, IntBuffer textureNames)
    {
        return 0;
    }

    @Override
    public long glVDPAURegisterOutputSurfaceNV(Buffer vdpSurface, int target, int numTextureNames, int[] textureNames, int textureNames_offset)
    {
        return 0;
    }

    @Override
    public long glVDPAURegisterVideoSurfaceNV(Buffer vdpSurface, int target, int numTextureNames, IntBuffer textureNames)
    {
        return 0;
    }

    @Override
    public long glVDPAURegisterVideoSurfaceNV(Buffer vdpSurface, int target, int numTextureNames, int[] textureNames, int textureNames_offset)
    {
        return 0;
    }

    @Override
    public void glVDPAUSurfaceAccessNV(long surface, int access)
    {
        methodCallCount++;
    }

    @Override
    public void glVDPAUUnmapSurfacesNV(int numSurface, PointerBuffer surfaces)
    {
        methodCallCount++;
    }

    @Override
    public void glVDPAUUnregisterSurfaceNV(long surface)
    {
        methodCallCount++;
    }

    @Override
    public void glValidateProgramARB(int programObj)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantPointerEXT(int id, int type, int stride, Buffer addr)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantPointerEXT(int id, int type, int stride, long addr_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantbvEXT(int id, ByteBuffer addr)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantbvEXT(int id, byte[] addr, int addr_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantdvEXT(int id, DoubleBuffer addr)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantdvEXT(int id, double[] addr, int addr_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantfvEXT(int id, FloatBuffer addr)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantfvEXT(int id, float[] addr, int addr_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantivEXT(int id, IntBuffer addr)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantivEXT(int id, int[] addr, int addr_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantsvEXT(int id, ShortBuffer addr)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantsvEXT(int id, short[] addr, int addr_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantubvEXT(int id, ByteBuffer addr)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantubvEXT(int id, byte[] addr, int addr_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantuivEXT(int id, IntBuffer addr)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantuivEXT(int id, int[] addr, int addr_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantusvEXT(int id, ShortBuffer addr)
    {
        methodCallCount++;
    }

    @Override
    public void glVariantusvEXT(int id, short[] addr, int addr_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2bOES(byte x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2bvOES(ByteBuffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2bvOES(byte[] coords, int coords_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2d(double x, double y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2f(float x, float y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2h(short x, short y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2hv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2hv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2i(int x, int y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2s(short x, short y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex2sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3bOES(byte x, byte y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3bvOES(ByteBuffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3bvOES(byte[] coords, int coords_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3d(double x, double y, double z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3f(float x, float y, float z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3h(short x, short y, short z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3hv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3hv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3i(int x, int y, int z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3s(short x, short y, short z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex3sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4bOES(byte x, byte y, byte z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4bvOES(ByteBuffer coords)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4bvOES(byte[] coords, int coords_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4d(double x, double y, double z, double w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4f(float x, float y, float z, float w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4h(short x, short y, short z, short w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4hv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4hv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4i(int x, int y, int z, int w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4s(short x, short y, short z, short w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertex4sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayColorOffsetEXT(int vaobj, int buffer, int size, int type, int stride, long offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayEdgeFlagOffsetEXT(int vaobj, int buffer, int stride, long offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayFogCoordOffsetEXT(int vaobj, int buffer, int type, int stride, long offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayIndexOffsetEXT(int vaobj, int buffer, int type, int stride, long offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayMultiTexCoordOffsetEXT(int vaobj, int buffer, int texunit, int size, int type, int stride, long offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayNormalOffsetEXT(int vaobj, int buffer, int type, int stride, long offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayParameteriAPPLE(int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayRangeAPPLE(int length, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayRangeNV(int length, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArraySecondaryColorOffsetEXT(int vaobj, int buffer, int size, int type, int stride, long offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayTexCoordOffsetEXT(int vaobj, int buffer, int size, int type, int stride, long offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayVertexAttribIOffsetEXT(int vaobj, int buffer, int index, int size, int type, int stride, long offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayVertexAttribOffsetEXT(int vaobj, int buffer, int index, int size, int type, boolean normalized, int stride, long offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayVertexOffsetEXT(int vaobj, int buffer, int size, int type, int stride, long offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1dARB(int index, double x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1dvARB(int index, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1dvARB(int index, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1fARB(int index, float x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1fvARB(int index, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1fvARB(int index, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1h(int index, short x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1hv(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1hv(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1sARB(int index, short x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1svARB(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1svARB(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2dARB(int index, double x, double y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2dvARB(int index, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2dvARB(int index, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2fARB(int index, float x, float y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2fvARB(int index, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2fvARB(int index, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2h(int index, short x, short y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2hv(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2hv(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2sARB(int index, short x, short y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2svARB(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2svARB(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3dARB(int index, double x, double y, double z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3dvARB(int index, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3dvARB(int index, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3fARB(int index, float x, float y, float z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3fvARB(int index, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3fvARB(int index, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3h(int index, short x, short y, short z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3hv(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3hv(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3sARB(int index, short x, short y, short z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3svARB(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3svARB(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4NbvARB(int index, ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4NbvARB(int index, byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4NivARB(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4NivARB(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4NsvARB(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4NsvARB(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4NubARB(int index, byte x, byte y, byte z, byte w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4NubvARB(int index, ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4NubvARB(int index, byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4NuivARB(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4NuivARB(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4NusvARB(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4NusvARB(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4bvARB(int index, ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4bvARB(int index, byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4dARB(int index, double x, double y, double z, double w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4dvARB(int index, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4dvARB(int index, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4fARB(int index, float x, float y, float z, float w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4fvARB(int index, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4fvARB(int index, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4h(int index, short x, short y, short z, short w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4hv(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4hv(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4ivARB(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4ivARB(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4sARB(int index, short x, short y, short z, short w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4svARB(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4svARB(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4ubvARB(int index, ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4ubvARB(int index, byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4uivARB(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4uivARB(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4usvARB(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4usvARB(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI1iEXT(int index, int x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI1ivEXT(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI1ivEXT(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI1uiEXT(int index, int x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI1uivEXT(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI1uivEXT(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI2iEXT(int index, int x, int y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI2ivEXT(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI2ivEXT(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI2uiEXT(int index, int x, int y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI2uivEXT(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI2uivEXT(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI3iEXT(int index, int x, int y, int z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI3ivEXT(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI3ivEXT(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI3uiEXT(int index, int x, int y, int z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI3uivEXT(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI3uivEXT(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4bvEXT(int index, ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4bvEXT(int index, byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4i(int index, int x, int y, int z, int w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4iEXT(int index, int x, int y, int z, int w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4iv(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4iv(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4ivEXT(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4ivEXT(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4svEXT(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4svEXT(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4ubvEXT(int index, ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4ubvEXT(int index, byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4ui(int index, int x, int y, int z, int w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4uiEXT(int index, int x, int y, int z, int w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4uiv(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4uiv(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4uivEXT(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4uivEXT(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4usvEXT(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4usvEXT(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribIPointer(int index, int size, int type, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribIPointer(int index, int size, int type, int stride, long pointer_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public boolean glIsPBOPackEnabled()
    {
        return false;
    }

    @Override
    public boolean glIsPBOUnpackEnabled()
    {
        return false;
    }

    @Override
    public void glVertexAttribIPointerEXT(int index, int size, int type, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL1i64NV(int index, long x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL1i64vNV(int index, LongBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL1i64vNV(int index, long[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL1ui64NV(int index, long x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL1ui64vNV(int index, LongBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL1ui64vNV(int index, long[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL2i64NV(int index, long x, long y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL2i64vNV(int index, LongBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL2i64vNV(int index, long[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL2ui64NV(int index, long x, long y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL2ui64vNV(int index, LongBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL2ui64vNV(int index, long[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL3i64NV(int index, long x, long y, long z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL3i64vNV(int index, LongBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL3i64vNV(int index, long[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL3ui64NV(int index, long x, long y, long z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL3ui64vNV(int index, LongBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL3ui64vNV(int index, long[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL4i64NV(int index, long x, long y, long z, long w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL4i64vNV(int index, LongBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL4i64vNV(int index, long[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL4ui64NV(int index, long x, long y, long z, long w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL4ui64vNV(int index, LongBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribL4ui64vNV(int index, long[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribLFormatNV(int index, int size, int type, int stride)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribParameteriAMD(int index, int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribPointerARB(int index, int size, int type, boolean normalized, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribPointerARB(int index, int size, int type, boolean normalized, int stride, long pointer_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribs1hv(int index, int n, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribs1hv(int index, int n, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribs2hv(int index, int n, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribs2hv(int index, int n, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribs3hv(int index, int n, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribs3hv(int index, int n, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribs4hv(int index, int n, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribs4hv(int index, int n, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexBlendARB(int count)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexWeightPointerEXT(int size, int type, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexWeightPointerEXT(int size, int type, int stride, long pointer_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexWeightfEXT(float weight)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexWeightfvEXT(FloatBuffer weight)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexWeightfvEXT(float[] weight, int weight_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexWeighth(short weight)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexWeighthv(ShortBuffer weight)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexWeighthv(short[] weight, int weight_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glVideoCaptureNV(int video_capture_slot, IntBuffer sequence_num, LongBuffer capture_time)
    {
        return 0;
    }

    @Override
    public int glVideoCaptureNV(int video_capture_slot, int[] sequence_num, int sequence_num_offset, long[] capture_time, int capture_time_offset)
    {
        return 0;
    }

    @Override
    public void glVideoCaptureStreamParameterdvNV(int video_capture_slot, int stream, int pname, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glVideoCaptureStreamParameterdvNV(int video_capture_slot, int stream, int pname, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVideoCaptureStreamParameterfvNV(int video_capture_slot, int stream, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glVideoCaptureStreamParameterfvNV(int video_capture_slot, int stream, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVideoCaptureStreamParameterivNV(int video_capture_slot, int stream, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glVideoCaptureStreamParameterivNV(int video_capture_slot, int stream, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightPathsNV(int resultPath, int numPaths, IntBuffer paths, FloatBuffer weights)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightPathsNV(int resultPath, int numPaths, int[] paths, int paths_offset, float[] weights, int weights_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightPointer(int size, int type, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightbvARB(int size, ByteBuffer weights)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightbvARB(int size, byte[] weights, int weights_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightdvARB(int size, DoubleBuffer weights)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightdvARB(int size, double[] weights, int weights_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightfvARB(int size, FloatBuffer weights)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightfvARB(int size, float[] weights, int weights_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightivARB(int size, IntBuffer weights)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightivARB(int size, int[] weights, int weights_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightsvARB(int size, ShortBuffer weights)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightsvARB(int size, short[] weights, int weights_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightubvARB(int size, ByteBuffer weights)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightubvARB(int size, byte[] weights, int weights_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightuivARB(int size, IntBuffer weights)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightuivARB(int size, int[] weights, int weights_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightusvARB(int size, ShortBuffer weights)
    {
        methodCallCount++;
    }

    @Override
    public void glWeightusvARB(int size, short[] weights, int weights_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos2d(double x, double y)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos2dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos2dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos2f(float x, float y)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos2fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos2fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos2i(int x, int y)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos2iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos2iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos2s(short x, short y)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos2sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos2sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos3d(double x, double y, double z)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos3dv(DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos3dv(double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos3f(float x, float y, float z)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos3fv(FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos3fv(float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos3i(int x, int y, int z)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos3iv(IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos3iv(int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos3s(short x, short y, short z)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos3sv(ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glWindowPos3sv(short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glWriteMaskEXT(int res, int in, int outX, int outY, int outZ, int outW)
    {
        methodCallCount++;
    }

    @Override
    public ByteBuffer glAllocateMemoryNV(int size, float readFrequency, float writeFrequency, float priority)
    {
        return null;
    }

    @Override
    public void glFreeMemoryNV(ByteBuffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glAlphaFunc(int func, float ref)
    {
        methodCallCount++;
    }

    @Override
    public void glClientActiveTexture(int texture)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4ub(byte red, byte green, byte blue, byte alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glFogf(int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glFogfv(int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glFogfv(int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetLightfv(int light, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetLightfv(int light, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMaterialfv(int face, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetMaterialfv(int face, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexEnvfv(int tenv, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexEnvfv(int tenv, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexEnviv(int tenv, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexEnviv(int tenv, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexGenfv(int coord, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexGenfv(int coord, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexGeniv(int coord, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexGeniv(int coord, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glLightModelf(int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glLightModelfv(int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glLightModelfv(int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glLightf(int light, int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glBeginConditionalRender(int id, int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glBindFragDataLocation(int program, int color, String name)
    {
        methodCallCount++;
    }

    @Override
    public void glBufferAddressRangeNV(int pname, int index, long address, long length)
    {
        methodCallCount++;
    }

    @Override
    public void glClampColor(int target, int clamp)
    {
        methodCallCount++;
    }

    @Override
    public void glClearNamedBufferDataEXT(int buffer, int internalformat, int format, int type, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glClearNamedBufferSubDataEXT(int buffer, int internalformat, int format, int type, long offset, long size, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glColorFormatNV(int size, int type, int stride)
    {
        methodCallCount++;
    }

    @Override
    public void glColorMaski(int index, boolean r, boolean g, boolean b, boolean a)
    {
        methodCallCount++;
    }

    @Override
    public void glCompileShaderIncludeARB(int shader, int count, String[] path, IntBuffer length)
    {
        methodCallCount++;
    }

    @Override
    public void glCompileShaderIncludeARB(int shader, int count, String[] path, int[] length, int length_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTexImage1D(int target, int level, int internalformat, int width, int border, int imageSize, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTexImage1D(int target, int level, int internalformat, int width, int border, int imageSize, long data_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTexSubImage1D(int target, int level, int xoffset, int width, int format, int imageSize, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTexSubImage1D(int target, int level, int xoffset, int width, int format, int imageSize, long data_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyTexImage1D(int target, int level, int internalformat, int x, int y, int width, int border)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyTexSubImage1D(int target, int level, int xoffset, int x, int y, int width)
    {
        methodCallCount++;
    }

    @Override
    public long glCreateSyncFromCLeventARB(long context, long event, int flags)
    {
        return 0;
    }

    @Override
    public void glDebugMessageEnableAMD(int category, int severity, int count, IntBuffer ids, boolean enabled)
    {
        methodCallCount++;
    }

    @Override
    public void glDebugMessageEnableAMD(int category, int severity, int count, int[] ids, int ids_offset, boolean enabled)
    {
        methodCallCount++;
    }

    @Override
    public void glDebugMessageInsertAMD(int category, int severity, int id, int length, String buf)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteNamedStringARB(int namelen, String name)
    {
        methodCallCount++;
    }

    @Override
    public void glDisablei(int target, int index)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawBuffer(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glEdgeFlagFormatNV(int stride)
    {
        methodCallCount++;
    }

    @Override
    public void glEnablei(int target, int index)
    {
        methodCallCount++;
    }

    @Override
    public void glEndConditionalRender()
    {
        methodCallCount++;
    }

    @Override
    public void glFogCoordFormatNV(int type, int stride)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferTexture1D(int target, int attachment, int textarget, int texture, int level)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferTextureARB(int target, int attachment, int texture, int level)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferTextureFaceARB(int target, int attachment, int texture, int level, int face)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferTextureLayerARB(int target, int attachment, int texture, int level, int layer)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveUniformName(int program, int uniformIndex, int bufSize, IntBuffer length, ByteBuffer uniformName)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveUniformName(int program, int uniformIndex, int bufSize, int[] length, int length_offset, byte[] uniformName, int uniformName_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetBooleani_v(int target, int index, ByteBuffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetBooleani_v(int target, int index, byte[] data, int data_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetBufferParameterui64vNV(int target, int pname, LongBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetBufferParameterui64vNV(int target, int pname, long[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetBufferSubData(int target, long offset, long size, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glGetCompressedTexImage(int target, int level, Buffer img)
    {
        methodCallCount++;
    }

    @Override
    public void glGetCompressedTexImage(int target, int level, long img_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glGetDebugMessageLogAMD(int count, int bufsize, IntBuffer categories, IntBuffer severities, IntBuffer ids, IntBuffer lengths, ByteBuffer message)
    {
        return 0;
    }

    @Override
    public int glGetDebugMessageLogAMD(int count, int bufsize, int[] categories, int categories_offset, int[] severities, int severities_offset, int[] ids, int ids_offset, int[] lengths, int lengths_offset, byte[] message, int message_offset)
    {
        return 0;
    }

    @Override
    public void glGetDoublev(int pname, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetDoublev(int pname, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetIntegerui64i_vNV(int value, int index, LongBuffer result)
    {
        methodCallCount++;
    }

    @Override
    public void glGetIntegerui64i_vNV(int value, int index, long[] result, int result_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetIntegerui64vNV(int value, LongBuffer result)
    {
        methodCallCount++;
    }

    @Override
    public void glGetIntegerui64vNV(int value, long[] result, int result_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedBufferParameterui64vNV(int buffer, int pname, LongBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedBufferParameterui64vNV(int buffer, int pname, long[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedFramebufferParameterivEXT(int framebuffer, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedFramebufferParameterivEXT(int framebuffer, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedStringARB(int namelen, String name, int bufSize, IntBuffer stringlen, ByteBuffer string)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedStringARB(int namelen, String name, int bufSize, int[] stringlen, int stringlen_offset, byte[] string, int string_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedStringivARB(int namelen, String name, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetNamedStringivARB(int namelen, String name, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetQueryObjectiv(int id, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetQueryObjectiv(int id, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexImage(int target, int level, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexImage(int target, int level, int format, int type, long pixels_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexLevelParameterfv(int target, int level, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexLevelParameterfv(int target, int level, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexLevelParameteriv(int target, int level, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexLevelParameteriv(int target, int level, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexParameterIiv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexParameterIiv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexParameterIuiv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexParameterIuiv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetUniformui64vNV(int program, int location, LongBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetUniformui64vNV(int program, int location, long[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribdv(int index, int pname, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribdv(int index, int pname, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnCompressedTexImage(int target, int lod, int bufSize, Buffer img)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnTexImage(int target, int level, int format, int type, int bufSize, Buffer img)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnUniformdv(int program, int location, int bufSize, DoubleBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnUniformdv(int program, int location, int bufSize, double[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnUniformuiv(int program, int location, int bufSize, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnUniformuiv(int program, int location, int bufSize, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public long glImportSyncEXT(int external_sync_type, long external_sync, int flags)
    {
        return 0;
    }

    @Override
    public void glIndexFormatNV(int type, int stride)
    {
        methodCallCount++;
    }

    @Override
    public boolean glIsBufferResidentNV(int target)
    {
        return false;
    }

    @Override
    public boolean glIsEnabledi(int target, int index)
    {
        return false;
    }

    @Override
    public boolean glIsNamedBufferResidentNV(int buffer)
    {
        return false;
    }

    @Override
    public boolean glIsNamedStringARB(int namelen, String name)
    {
        return false;
    }

    @Override
    public void glLogicOp(int opcode)
    {
        methodCallCount++;
    }

    @Override
    public void glMakeBufferNonResidentNV(int target)
    {
        methodCallCount++;
    }

    @Override
    public void glMakeBufferResidentNV(int target, int access)
    {
        methodCallCount++;
    }

    @Override
    public void glMakeNamedBufferNonResidentNV(int buffer)
    {
        methodCallCount++;
    }

    @Override
    public void glMakeNamedBufferResidentNV(int buffer, int access)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiDrawArrays(int mode, IntBuffer first, IntBuffer count, int drawcount)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiDrawArrays(int mode, int[] first, int first_offset, int[] count, int count_offset, int drawcount)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiDrawArraysIndirectAMD(int mode, Buffer indirect, int primcount, int stride)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiDrawElements(int mode, IntBuffer count, int type, PointerBuffer indices, int drawcount)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiDrawElementsIndirectAMD(int mode, int type, Buffer indirect, int primcount, int stride)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedFramebufferParameteriEXT(int framebuffer, int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glNamedStringARB(int type, int namelen, String name, int stringlen, String string)
    {
        methodCallCount++;
    }

    @Override
    public void glNormalFormatNV(int type, int stride)
    {
        methodCallCount++;
    }

    @Override
    public void glPixelStoref(int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glMultiTexCoord4f(int target, float s, float t, float r, float q)
    {
        methodCallCount++;
    }

    @Override
    public void glNormal3f(float nx, float ny, float nz)
    {
        methodCallCount++;
    }

    @Override
    public void glPointParameterf(int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glPointParameterfv(int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glPointParameterfv(int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPointParameteri(int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glPointParameteriv(int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glPointParameteriv(int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPointSize(float size)
    {
        methodCallCount++;
    }

    @Override
    public void glPolygonMode(int face, int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glPrimitiveRestartIndex(int index)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramParameteriARB(int program, int pname, int value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformui64NV(int program, int location, long value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformui64vNV(int program, int location, int count, LongBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glProgramUniformui64vNV(int program, int location, int count, long[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glSecondaryColorFormatNV(int size, int type, int stride)
    {
        methodCallCount++;
    }

    @Override
    public void glSetMultisamplefvAMD(int pname, int index, FloatBuffer val)
    {
        methodCallCount++;
    }

    @Override
    public void glSetMultisamplefvAMD(int pname, int index, float[] val, int val_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilOpValueAMD(int face, int value)
    {
        methodCallCount++;
    }

    @Override
    public void glTessellationFactorAMD(float factor)
    {
        methodCallCount++;
    }

    @Override
    public void glTessellationModeAMD(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glTexBuffer(int target, int internalformat, int buffer)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoordFormatNV(int size, int type, int stride)
    {
        methodCallCount++;
    }

    @Override
    public void glTexImage1D(int target, int level, int internalFormat, int width, int border, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glTexImage1D(int target, int level, int internalFormat, int width, int border, int format, int type, long pixels_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexImage2DMultisampleCoverageNV(int target, int coverageSamples, int colorSamples, int internalFormat, int width, int height, boolean fixedSampleLocations)
    {
        methodCallCount++;
    }

    @Override
    public void glTexImage3DMultisampleCoverageNV(int target, int coverageSamples, int colorSamples, int internalFormat, int width, int height, int depth, boolean fixedSampleLocations)
    {
        methodCallCount++;
    }

    @Override
    public void glTexParameterIiv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glTexParameterIiv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexParameterIuiv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glTexParameterIuiv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, long pixels_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureBufferRangeEXT(int texture, int target, int internalformat, int buffer, long offset, long size)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureImage2DMultisampleCoverageNV(int texture, int target, int coverageSamples, int colorSamples, int internalFormat, int width, int height, boolean fixedSampleLocations)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureImage2DMultisampleNV(int texture, int target, int samples, int internalFormat, int width, int height, boolean fixedSampleLocations)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureImage3DMultisampleCoverageNV(int texture, int target, int coverageSamples, int colorSamples, int internalFormat, int width, int height, int depth, boolean fixedSampleLocations)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureImage3DMultisampleNV(int texture, int target, int samples, int internalFormat, int width, int height, int depth, boolean fixedSampleLocations)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformui64NV(int location, long value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformui64vNV(int location, int count, LongBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformui64vNV(int location, int count, long[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayBindVertexBufferEXT(int vaobj, int bindingindex, int buffer, long offset, int stride)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayVertexAttribBindingEXT(int vaobj, int attribindex, int bindingindex)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayVertexAttribFormatEXT(int vaobj, int attribindex, int size, int type, boolean normalized, int relativeoffset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayVertexAttribIFormatEXT(int vaobj, int attribindex, int size, int type, int relativeoffset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayVertexAttribLFormatEXT(int vaobj, int attribindex, int size, int type, int relativeoffset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexArrayVertexBindingDivisorEXT(int vaobj, int bindingindex, int divisor)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1d(int index, double x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1dv(int index, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1dv(int index, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1s(int index, short x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1sv(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1sv(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2d(int index, double x, double y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2dv(int index, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2dv(int index, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2s(int index, short x, short y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2sv(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2sv(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3d(int index, double x, double y, double z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3dv(int index, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3dv(int index, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3s(int index, short x, short y, short z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3sv(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3sv(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4Nbv(int index, ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4Nbv(int index, byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4Niv(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4Niv(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4Nsv(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4Nsv(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4Nub(int index, byte x, byte y, byte z, byte w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4Nubv(int index, ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4Nubv(int index, byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4Nuiv(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4Nuiv(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4Nusv(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4Nusv(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4bv(int index, ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4bv(int index, byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4d(int index, double x, double y, double z, double w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4dv(int index, DoubleBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4dv(int index, double[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4iv(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4iv(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4s(int index, short x, short y, short z, short w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4sv(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4sv(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4ubv(int index, ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4ubv(int index, byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4uiv(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4uiv(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4usv(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4usv(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribFormatNV(int index, int size, int type, boolean normalized, int stride)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI1i(int index, int x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI1iv(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI1iv(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI1ui(int index, int x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI1uiv(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI1uiv(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI2i(int index, int x, int y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI2iv(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI2iv(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI2ui(int index, int x, int y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI2uiv(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI2uiv(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI3i(int index, int x, int y, int z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI3iv(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI3iv(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI3ui(int index, int x, int y, int z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI3uiv(int index, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI3uiv(int index, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4bv(int index, ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4bv(int index, byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4sv(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4sv(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4ubv(int index, ByteBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4ubv(int index, byte[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4usv(int index, ShortBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribI4usv(int index, short[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribIFormatNV(int index, int size, int type, int stride)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexFormatNV(int size, int type, int stride)
    {
        methodCallCount++;
    }

    @Override
    public void glTexEnvf(int target, int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glTexEnvfv(int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glTexEnvfv(int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexEnvi(int target, int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glTexEnviv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glTexEnviv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexGenf(int coord, int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glTexGenfv(int coord, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glTexGenfv(int coord, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexGeni(int coord, int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glTexGeniv(int coord, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glTexGeniv(int coord, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glOrtho(double left, double right, double bottom, double top, double near_val, double far_val)
    {
        methodCallCount++;
    }

    @Override
    public void glFrustum(double left, double right, double bottom, double top, double zNear, double zFar)
    {
        methodCallCount++;
    }

    @Override
    public void glAttachShader(int program, int shader)
    {
        methodCallCount++;
    }

    @Override
    public void glBeginQuery(int target, int id)
    {
        methodCallCount++;
    }

    @Override
    public void glBindAttribLocation(int program, int index, String name)
    {
        methodCallCount++;
    }

    @Override
    public void glBlendColor(float red, float green, float blue, float alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glCompileShader(int shader)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int imageSize, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int imageSize, long data_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, long data_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public int glCreateProgram()
    {
        return 0;
    }

    @Override
    public int glCreateShader(int type)
    {
        return 0;
    }

    @Override
    public void glDebugMessageControl(int source, int type, int severity, int count, IntBuffer ids, boolean enabled)
    {
        methodCallCount++;
    }

    @Override
    public void glDebugMessageControl(int source, int type, int severity, int count, int[] ids, int ids_offset, boolean enabled)
    {
        methodCallCount++;
    }

    @Override
    public void glDebugMessageInsert(int source, int type, int id, int severity, int length, String buf)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteProgram(int program)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteQueries(int n, IntBuffer ids)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteQueries(int n, int[] ids, int ids_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteShader(int shader)
    {
        methodCallCount++;
    }

    @Override
    public void glDetachShader(int program, int shader)
    {
        methodCallCount++;
    }

    @Override
    public void glDisableVertexAttribArray(int index)
    {
        methodCallCount++;
    }

    @Override
    public void glEnableVertexAttribArray(int index)
    {
        methodCallCount++;
    }

    @Override
    public void glEndQuery(int target)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferTexture3D(int target, int attachment, int textarget, int texture, int level, int zoffset)
    {
        methodCallCount++;
    }

    @Override
    public void glGenQueries(int n, IntBuffer ids)
    {
        methodCallCount++;
    }

    @Override
    public void glGenQueries(int n, int[] ids, int ids_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveAttrib(int program, int index, int bufsize, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveAttrib(int program, int index, int bufsize, int[] length, int length_offset, int[] size, int size_offset, int[] type, int type_offset, byte[] name, int name_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveUniform(int program, int index, int bufsize, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name)
    {
        methodCallCount++;
    }

    @Override
    public void glGetActiveUniform(int program, int index, int bufsize, int[] length, int length_offset, int[] size, int size_offset, int[] type, int type_offset, byte[] name, int name_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetAttachedShaders(int program, int maxcount, IntBuffer count, IntBuffer shaders)
    {
        methodCallCount++;
    }

    @Override
    public void glGetAttachedShaders(int program, int maxcount, int[] count, int count_offset, int[] shaders, int shaders_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glGetAttribLocation(int program, String name)
    {
        return 0;
    }

    @Override
    public int glGetDebugMessageLog(int count, int bufsize, IntBuffer sources, IntBuffer types, IntBuffer ids, IntBuffer severities, IntBuffer lengths, ByteBuffer messageLog)
    {
        return 0;
    }

    @Override
    public int glGetDebugMessageLog(int count, int bufsize, int[] sources, int sources_offset, int[] types, int types_offset, int[] ids, int ids_offset, int[] severities, int severities_offset, int[] lengths, int lengths_offset, byte[] messageLog, int messageLog_offset)
    {
        return 0;
    }

    @Override
    public void glGetObjectLabel(int identifier, int name, int bufSize, IntBuffer length, ByteBuffer label)
    {
        methodCallCount++;
    }

    @Override
    public void glGetObjectLabel(int identifier, int name, int bufSize, int[] length, int length_offset, byte[] label, int label_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetObjectPtrLabel(Buffer ptr, int bufSize, IntBuffer length, ByteBuffer label)
    {
        methodCallCount++;
    }

    @Override
    public void glGetObjectPtrLabel(Buffer ptr, int bufSize, int[] length, int length_offset, byte[] label, int label_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramBinary(int program, int bufSize, IntBuffer length, IntBuffer binaryFormat, Buffer binary)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramBinary(int program, int bufSize, int[] length, int length_offset, int[] binaryFormat, int binaryFormat_offset, Buffer binary)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramInfoLog(int program, int bufsize, IntBuffer length, ByteBuffer infolog)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramInfoLog(int program, int bufsize, int[] length, int length_offset, byte[] infolog, int infolog_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramiv(int program, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetProgramiv(int program, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetQueryObjectuiv(int id, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetQueryObjectuiv(int id, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetQueryiv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetQueryiv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetShaderInfoLog(int shader, int bufsize, IntBuffer length, ByteBuffer infolog)
    {
        methodCallCount++;
    }

    @Override
    public void glGetShaderInfoLog(int shader, int bufsize, int[] length, int length_offset, byte[] infolog, int infolog_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetShaderSource(int shader, int bufsize, IntBuffer length, ByteBuffer source)
    {
        methodCallCount++;
    }

    @Override
    public void glGetShaderSource(int shader, int bufsize, int[] length, int length_offset, byte[] source, int source_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetShaderiv(int shader, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetShaderiv(int shader, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glGetUniformLocation(int program, String name)
    {
        return 0;
    }

    @Override
    public void glGetUniformfv(int program, int location, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetUniformfv(int program, int location, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetUniformiv(int program, int location, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetUniformiv(int program, int location, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribfv(int index, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribfv(int index, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribiv(int index, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetVertexAttribiv(int index, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public boolean glIsProgram(int program)
    {
        return false;
    }

    @Override
    public boolean glIsQuery(int id)
    {
        return false;
    }

    @Override
    public boolean glIsShader(int shader)
    {
        return false;
    }

    @Override
    public void glLinkProgram(int program)
    {
        methodCallCount++;
    }

    @Override
    public void glObjectLabel(int identifier, int name, int length, ByteBuffer label)
    {
        methodCallCount++;
    }

    @Override
    public void glObjectLabel(int identifier, int name, int length, byte[] label, int label_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glObjectPtrLabel(Buffer ptr, int length, ByteBuffer label)
    {
        methodCallCount++;
    }

    @Override
    public void glObjectPtrLabel(Buffer ptr, int length, byte[] label, int label_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glPopDebugGroup()
    {
        methodCallCount++;
    }

    @Override
    public void glProgramBinary(int program, int binaryFormat, Buffer binary, int length)
    {
        methodCallCount++;
    }

    @Override
    public void glPushDebugGroup(int source, int id, int length, ByteBuffer message)
    {
        methodCallCount++;
    }

    @Override
    public void glPushDebugGroup(int source, int id, int length, byte[] message, int message_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glShaderSource(int shader, int count, String[] string, IntBuffer length)
    {
        methodCallCount++;
    }

    @Override
    public void glShaderSource(int shader, int count, String[] string, int[] length, int length_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilFuncSeparate(int face, int func, int ref, int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilMaskSeparate(int face, int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilOpSeparate(int face, int fail, int zfail, int zpass)
    {
        methodCallCount++;
    }

    @Override
    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, long pixels_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, long pixels_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1f(int location, float x)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1fv(int location, int count, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1fv(int location, int count, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1i(int location, int x)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1iv(int location, int count, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform1iv(int location, int count, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2f(int location, float x, float y)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2fv(int location, int count, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2fv(int location, int count, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2i(int location, int x, int y)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2iv(int location, int count, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform2iv(int location, int count, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3f(int location, float x, float y, float z)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3fv(int location, int count, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3fv(int location, int count, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3i(int location, int x, int y, int z)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3iv(int location, int count, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform3iv(int location, int count, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4f(int location, float x, float y, float z, float w)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4fv(int location, int count, FloatBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4fv(int location, int count, float[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4i(int location, int x, int y, int z, int w)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4iv(int location, int count, IntBuffer v)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform4iv(int location, int count, int[] v, int v_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        methodCallCount++;
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glUseProgram(int program)
    {
        methodCallCount++;
    }

    @Override
    public void glValidateProgram(int program)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1f(int indx, float x)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1fv(int indx, FloatBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib1fv(int indx, float[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2f(int indx, float x, float y)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2fv(int indx, FloatBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib2fv(int indx, float[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3f(int indx, float x, float y, float z)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3fv(int indx, FloatBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib3fv(int indx, float[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4f(int indx, float x, float y, float z, float w)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4fv(int indx, FloatBuffer values)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttrib4fv(int indx, float[] values, int values_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, long ptr_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glReleaseShaderCompiler()
    {
        methodCallCount++;
    }

    @Override
    public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length)
    {
        methodCallCount++;
    }

    @Override
    public void glShaderBinary(int n, int[] shaders, int shaders_offset, int binaryformat, Buffer binary, int length)
    {
        methodCallCount++;
    }

    @Override
    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision)
    {
        methodCallCount++;
    }

    @Override
    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, int[] range, int range_offset, int[] precision, int precision_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexAttribPointer(GLArrayData array)
    {
        methodCallCount++;
    }

    @Override
    public void glUniform(GLUniformData data)
    {
        methodCallCount++;
    }

    @Override
    public void glActiveTexture(int texture)
    {
        methodCallCount++;
    }

    @Override
    public void glBindBuffer(int target, int buffer)
    {
        methodCallCount++;
    }

    @Override
    public void glBindFramebuffer(int target, int framebuffer)
    {
        methodCallCount++;
    }

    @Override
    public void glBindRenderbuffer(int target, int renderbuffer)
    {
        methodCallCount++;
    }

    @Override
    public void glBindTexture(int target, int texture)
    {
        methodCallCount++;
    }

    @Override
    public void glBlendEquation(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha)
    {
        methodCallCount++;
    }

    @Override
    public void glBlendFunc(int sfactor, int dfactor)
    {
        methodCallCount++;
    }

    @Override
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha)
    {
        methodCallCount++;
    }

    @Override
    public void glBufferData(int target, long size, Buffer data, int usage)
    {
        methodCallCount++;
    }

    @Override
    public void glBufferSubData(int target, long offset, long size, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public int glCheckFramebufferStatus(int target)
    {
        return 0;
    }

    @Override
    public void glClear(int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glClearDepthf(float depth)
    {
        methodCallCount++;
    }

    @Override
    public void glClearStencil(int s)
    {
        methodCallCount++;
    }

    @Override
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, long data_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, long data_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border)
    {
        methodCallCount++;
    }

    @Override
    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glCullFace(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteBuffers(int n, IntBuffer buffers)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteBuffers(int n, int[] buffers, int buffers_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteFramebuffers(int n, IntBuffer framebuffers)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteFramebuffers(int n, int[] framebuffers, int framebuffers_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteRenderbuffers(int n, int[] renderbuffers, int renderbuffers_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteTextures(int n, IntBuffer textures)
    {
        methodCallCount++;
    }

    @Override
    public void glDeleteTextures(int n, int[] textures, int textures_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glDepthFunc(int func)
    {
        methodCallCount++;
    }

    @Override
    public void glDepthMask(boolean flag)
    {
        methodCallCount++;
    }

    @Override
    public void glDepthRangef(float zNear, float zFar)
    {
        methodCallCount++;
    }

    @Override
    public void glDisable(int cap)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawArrays(int mode, int first, int count)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices)
    {
        methodCallCount++;
    }

    @Override
    public void glDrawElements(int mode, int count, int type, long indices_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glEnable(int cap)
    {
        methodCallCount++;
    }

    @Override
    public void glFinish()
    {
        methodCallCount++;
    }

    @Override
    public void glFlush()
    {
        methodCallCount++;
    }

    @Override
    public void glFlushMappedBufferRange(int target, long offset, long length)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer)
    {
        methodCallCount++;
    }

    @Override
    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level)
    {
        methodCallCount++;
    }

    @Override
    public void glFrontFace(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glGenBuffers(int n, IntBuffer buffers)
    {
        methodCallCount++;
    }

    @Override
    public void glGenBuffers(int n, int[] buffers, int buffers_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGenFramebuffers(int n, IntBuffer framebuffers)
    {
        methodCallCount++;
    }

    @Override
    public void glGenFramebuffers(int n, int[] framebuffers, int framebuffers_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGenRenderbuffers(int n, IntBuffer renderbuffers)
    {
        methodCallCount++;
    }

    @Override
    public void glGenRenderbuffers(int n, int[] renderbuffers, int renderbuffers_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGenTextures(int n, IntBuffer textures)
    {
        methodCallCount++;
    }

    @Override
    public void glGenTextures(int n, int[] textures, int textures_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGenerateMipmap(int target)
    {
        methodCallCount++;
    }

    @Override
    public void glGetBooleanv(int pname, ByteBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetBooleanv(int pname, byte[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetBufferParameteriv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetBufferParameteriv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glGetError()
    {
        return 0;
    }

    @Override
    public void glGetFloatv(int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetFloatv(int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public int glGetGraphicsResetStatus()
    {
        return 0;
    }

    @Override
    public void glGetIntegerv(int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetIntegerv(int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMatrixMode(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glPushMatrix()
    {
        methodCallCount++;
    }

    @Override
    public void glPopMatrix()
    {
        methodCallCount++;
    }

    @Override
    public void glLoadIdentity()
    {
        methodCallCount++;
    }

    @Override
    public void glLoadMatrixf(FloatBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glLoadMatrixf(float[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMultMatrixf(FloatBuffer m)
    {
        methodCallCount++;
    }

    @Override
    public void glMultMatrixf(float[] m, int m_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTranslatef(float x, float y, float z)
    {
        methodCallCount++;
    }

    @Override
    public void glRotatef(float angle, float x, float y, float z)
    {
        methodCallCount++;
    }

    @Override
    public void glScalef(float x, float y, float z)
    {
        methodCallCount++;
    }

    @Override
    public void glOrthof(float left, float right, float bottom, float top, float zNear, float zFar)
    {
        methodCallCount++;
    }

    @Override
    public void glFrustumf(float left, float right, float bottom, float top, float zNear, float zFar)
    {
        methodCallCount++;
    }

    @Override
    public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetRenderbufferParameteriv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public String glGetString(int name)
    {
        return null;
    }

    @Override
    public void glGetTexParameterfv(int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexParameterfv(int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexParameteriv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetTexParameteriv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnUniformfv(int program, int location, int bufSize, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnUniformfv(int program, int location, int bufSize, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnUniformiv(int program, int location, int bufSize, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glGetnUniformiv(int program, int location, int bufSize, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glHint(int target, int mode)
    {
        methodCallCount++;
    }

    @Override
    public boolean glIsBuffer(int buffer)
    {
        return false;
    }

    @Override
    public boolean glIsEnabled(int cap)
    {
        return false;
    }

    @Override
    public boolean glIsFramebuffer(int framebuffer)
    {
        return false;
    }

    @Override
    public boolean glIsRenderbuffer(int renderbuffer)
    {
        return false;
    }

    @Override
    public boolean glIsTexture(int texture)
    {
        return false;
    }

    @Override
    public void glLineWidth(float width)
    {
        methodCallCount++;
    }

    @Override
    public ByteBuffer glMapBuffer(int target, int access)
    {
        return null;
    }

    @Override
    public ByteBuffer glMapBufferRange(int target, long offset, long length, int access)
    {
        return null;
    }

    @Override
    public void glPixelStorei(int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glPolygonOffset(float factor, float units)
    {
        methodCallCount++;
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, long pixels_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glReadnPixels(int x, int y, int width, int height, int format, int type, int bufSize, Buffer data)
    {
        methodCallCount++;
    }

    @Override
    public void glRenderbufferStorage(int target, int internalformat, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glSampleCoverage(float value, boolean invert)
    {
        methodCallCount++;
    }

    @Override
    public void glScissor(int x, int y, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilFunc(int func, int ref, int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilMask(int mask)
    {
        methodCallCount++;
    }

    @Override
    public void glStencilOp(int fail, int zfail, int zpass)
    {
        methodCallCount++;
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, long pixels_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexParameterf(int target, int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glTexParameterfv(int target, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glTexParameterfv(int target, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexParameteri(int target, int pname, int param)
    {
        methodCallCount++;
    }

    @Override
    public void glTexParameteriv(int target, int pname, IntBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glTexParameteriv(int target, int pname, int[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexStorage1D(int target, int levels, int internalformat, int width)
    {
        methodCallCount++;
    }

    @Override
    public void glTexStorage2D(int target, int levels, int internalformat, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth)
    {
        methodCallCount++;
    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels)
    {
        methodCallCount++;
    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, long pixels_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureStorage1D(int texture, int target, int levels, int internalformat, int width)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureStorage2D(int texture, int target, int levels, int internalformat, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public void glTextureStorage3D(int texture, int target, int levels, int internalformat, int width, int height, int depth)
    {
        methodCallCount++;
    }

    @Override
    public boolean glUnmapBuffer(int target)
    {
        return false;
    }

    @Override
    public void glViewport(int x, int y, int width, int height)
    {
        methodCallCount++;
    }

    @Override
    public boolean isGL()
    {
        return false;
    }

    @Override
    public boolean isGL4bc()
    {
        return false;
    }

    @Override
    public boolean isGL4()
    {
        return false;
    }

    @Override
    public boolean isGL3bc()
    {
        return false;
    }

    @Override
    public boolean isGL3()
    {
        return false;
    }

    @Override
    public boolean isGL2()
    {
        return false;
    }

    @Override
    public boolean isGLES1()
    {
        return false;
    }

    @Override
    public boolean isGLES2()
    {
        return false;
    }

    @Override
    public boolean isGLES3()
    {
        return false;
    }

    @Override
    public boolean isGLES()
    {
        return false;
    }

    @Override
    public boolean isGL2ES1()
    {
        return false;
    }

    @Override
    public boolean isGL2ES2()
    {
        return false;
    }

    @Override
    public boolean isGL3ES3()
    {
        return false;
    }

    @Override
    public boolean isGL4ES3()
    {
        return false;
    }

    @Override
    public boolean isGL2GL3()
    {
        return false;
    }

    @Override
    public boolean isGL4core()
    {
        return false;
    }

    @Override
    public boolean isGL3core()
    {
        return false;
    }

    @Override
    public boolean isGLcore()
    {
        return false;
    }

    @Override
    public boolean isGLES2Compatible()
    {
        return false;
    }

    @Override
    public boolean isGLES3Compatible()
    {
        return false;
    }

    @Override
    public boolean hasGLSL()
    {
        return false;
    }

    @Override
    public GL getDownstreamGL() throws GLException
    {
        return null;
    }

    @Override
    public GL getRootGL() throws GLException
    {
        return null;
    }

    @Override
    public GL getGL() throws GLException
    {
        return null;
    }

    @Override
    public GL4bc getGL4bc() throws GLException
    {
        return null;
    }

    @Override
    public GL4 getGL4() throws GLException
    {
        return null;
    }

    @Override
    public GL3bc getGL3bc() throws GLException
    {
        return null;
    }

    @Override
    public GL3 getGL3() throws GLException
    {
        return null;
    }

    @Override
    public GL2 getGL2() throws GLException
    {
        return null;
    }

    @Override
    public GLES1 getGLES1() throws GLException
    {
        return null;
    }

    @Override
    public GLES2 getGLES2() throws GLException
    {
        return null;
    }

    @Override
    public GLES3 getGLES3() throws GLException
    {
        return null;
    }

    @Override
    public GL2ES1 getGL2ES1() throws GLException
    {
        return null;
    }

    @Override
    public GL2ES2 getGL2ES2() throws GLException
    {
        return null;
    }

    @Override
    public GL3ES3 getGL3ES3() throws GLException
    {
        return null;
    }

    @Override
    public GL4ES3 getGL4ES3() throws GLException
    {
        return null;
    }

    @Override
    public GL2GL3 getGL2GL3() throws GLException
    {
        return null;
    }

    @Override
    public GLProfile getGLProfile()
    {
        return null;
    }

    @Override
    public GLContext getContext()
    {
        return null;
    }

    @Override
    public boolean isFunctionAvailable(String glFunctionName)
    {
        return false;
    }

    @Override
    public boolean isExtensionAvailable(String glExtensionName)
    {
        return false;
    }

    @Override
    public boolean hasBasicFBOSupport()
    {
        return false;
    }

    @Override
    public boolean hasFullFBOSupport()
    {
        return false;
    }

    @Override
    public int getMaxRenderbufferSamples()
    {
        return 0;
    }

    @Override
    public boolean isNPOTTextureAvailable()
    {
        return false;
    }

    @Override
    public boolean isTextureFormatBGRA8888Available()
    {
        return false;
    }

    @Override
    public void setSwapInterval(int interval)
    {
        methodCallCount++;
    }

    @Override
    public int getSwapInterval()
    {
        return 0;
    }

    @Override
    public Object getPlatformGLExtensions()
    {
        return null;
    }

    @Override
    public Object getExtension(String extensionName)
    {
        return null;
    }

    @Override
    public void glClearDepth(double depth)
    {
        methodCallCount++;
    }

    @Override
    public void glDepthRange(double zNear, double zFar)
    {
        methodCallCount++;
    }

    @Override
    public int glGetBoundBuffer(int target)
    {
        return 0;
    }

    @Override
    public long glGetBufferSize(int buffer)
    {
        return 0;
    }

    @Override
    public boolean glIsVBOArrayEnabled()
    {
        return false;
    }

    @Override
    public boolean glIsVBOElementArrayEnabled()
    {
        return false;
    }

    @Override
    public int getBoundFramebuffer(int target)
    {
        return 0;
    }

    @Override
    public int getDefaultDrawFramebuffer()
    {
        return 0;
    }

    @Override
    public int getDefaultReadFramebuffer()
    {
        return 0;
    }

    @Override
    public int getDefaultReadBuffer()
    {
        return 0;
    }

    @Override
    public void glLightfv(int light, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glLightfv(int light, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glMaterialf(int face, int pname, float param)
    {
        methodCallCount++;
    }

    @Override
    public void glMaterialfv(int face, int pname, FloatBuffer params)
    {
        methodCallCount++;
    }

    @Override
    public void glMaterialfv(int face, int pname, float[] params, int params_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glShadeModel(int mode)
    {
        methodCallCount++;
    }

    @Override
    public void glEnableClientState(int arrayName)
    {
        methodCallCount++;
    }

    @Override
    public void glDisableClientState(int arrayName)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexPointer(GLArrayData array)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexPointer(int size, int type, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glVertexPointer(int size, int type, int stride, long pointer_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColorPointer(GLArrayData array)
    {
        methodCallCount++;
    }

    @Override
    public void glColorPointer(int size, int type, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glColorPointer(int size, int type, int stride, long pointer_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glColor4f(float red, float green, float blue, float alpha)
    {
        methodCallCount++;
    }

    @Override
    public void glNormalPointer(GLArrayData array)
    {
        methodCallCount++;
    }

    @Override
    public void glNormalPointer(int type, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glNormalPointer(int type, int stride, long pointer_buffer_offset)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoordPointer(GLArrayData array)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoordPointer(int size, int type, int stride, Buffer pointer)
    {
        methodCallCount++;
    }

    @Override
    public void glTexCoordPointer(int size, int type, int stride, long pointer_buffer_offset)
    {
        methodCallCount++;
    }

    public int getCallCount()
    {
        return methodCallCount;
    }

    public void resetCallCount()
    {
        methodCallCount = 0;
    }
}
