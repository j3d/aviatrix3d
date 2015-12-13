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

package org.j3d.aviatrix3d.test;

import java.nio.*;

import com.jogamp.opengl.*;

import com.jogamp.common.nio.PointerBuffer;

/**
 * Pretend Mock of the GL2 interface. This exists because CGLIB cannot
 * generate a proper mock for the GL/GL2/GL3... interfaces due to too
 * many methods, and the combined set of static constructs result
 * in having more than 65k bytes in a method.
 * <p>
 *     All vendor-specific extension APIs throw
 * {@link UnsupportedOperationException} to track and discourage use of vendor
 * extensions in a generic API like this.
 * </p>
 *
 * @author justin
 */
public class MockGL2 extends MockGL implements GL2
{
    public MockGL2(GLContext ctx)
    {
        super(ctx);
    }

    @Override
    public void glAccum(int op, float value)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { op, value });
    }

    @Override
    public void glActiveStencilFaceEXT(int face)
    {
        CallDetails details = getMethodDetails("glActiveStencilFaceEXT");
        details.foundArguments.add(new Object[] { face });
    }

    @Override
    public void glApplyTextureEXT(int mode)
    {
        CallDetails details = getMethodDetails("glApplyTextureEXT");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public boolean glAreTexturesResident(int n, IntBuffer textures, ByteBuffer residences)
    {
        CallDetails details = getMethodDetails("glAreTexturesResident");
        details.foundArguments.add(new Object[] { n, textures, residences });

        return false;
    }

    @Override
    public boolean glAreTexturesResident(int n, int[] textures, int textures_offset, byte[] residences, int residences_offset)
    {
        CallDetails details = getMethodDetails("glAreTexturesResident");
        details.foundArguments.add(new Object[] { n, textures, textures_offset, residences, residences_offset });

        return false;
    }

    @Override
    public void glArrayElement(int i)
    {
        CallDetails details = getMethodDetails("glArrayElement");
        details.foundArguments.add(new Object[] { i });

    }

    @Override
    public void glAttachObjectARB(long containerObj, long obj)
    {
        CallDetails details = getMethodDetails("glAttachObjectARB");
        details.foundArguments.add(new Object[] { containerObj, obj });
    }

    @Override
    public void glBegin(int mode)
    {
        CallDetails details = getMethodDetails("glBegin");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public void glBeginConditionalRenderNVX(int id)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBeginOcclusionQueryNV(int id)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBeginPerfMonitorAMD(int monitor)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBeginPerfQueryINTEL(int queryHandle)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBeginTransformFeedback(int primitiveMode)
    {
        CallDetails details = getMethodDetails("glBeginTransformFeedback");
        details.foundArguments.add(new Object[] { primitiveMode });
    }

    @Override
    public void glBeginVertexShaderEXT()
    {
        CallDetails details = getMethodDetails("glBeginVertexShaderEXT");
        details.foundArguments.add(new Object[] { });
    }

    @Override
    public void glBeginVideoCaptureNV(int video_capture_slot)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBindBufferBase(int target, int index, int buffer)
    {
        CallDetails details = getMethodDetails("glBindBufferBase");
        details.foundArguments.add(new Object[] { target, index, buffer });
    }

    @Override
    public void glBindBufferRange(int target, int index, int buffer, long offset, long size)
    {
        CallDetails details = getMethodDetails("glBindBufferRange");
        details.foundArguments.add(new Object[] { target, index, buffer, offset, size });
    }

    @Override
    public void glBindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format)
    {
        CallDetails details = getMethodDetails("glBindImageTexture");
        details.foundArguments.add(new Object[] { unit, texture, level, layered, layer, access, format });
    }

    @Override
    public void glBindTransformFeedback(int target, int id)
    {
        CallDetails details = getMethodDetails("glBindTransformFeedback");
        details.foundArguments.add(new Object[] { target, id });
    }

    @Override
    public int glBindLightParameterEXT(int light, int value)
    {
        CallDetails details = getMethodDetails("glBindLightParameterEXT");
        details.foundArguments.add(new Object[] { light, value });
        details.returnValue = ++indexCounter;

        return details.returnValue;
    }

    @Override
    public int glBindMaterialParameterEXT(int face, int value)
    {
        CallDetails details = getMethodDetails("glBindMaterialParameterEXT");
        details.foundArguments.add(new Object[] { face, value });
        details.returnValue = ++indexCounter;

        return details.returnValue;
    }

    @Override
    public void glBindMultiTextureEXT(int texunit, int target, int texture)
    {
        CallDetails details = getMethodDetails("glBindMultiTextureEXT");
        details.foundArguments.add(new Object[] { texunit, target, texture });
    }

    @Override
    public int glBindParameterEXT(int value)
    {
        CallDetails details = getMethodDetails("glBindParameterEXT");
        details.foundArguments.add(new Object[] { value });
        details.returnValue = ++indexCounter;

        return details.returnValue;
    }

    @Override
    public void glBindProgramARB(int target, int program)
    {
        CallDetails details = getMethodDetails("glBindProgramARB");
        details.foundArguments.add(new Object[] { target, program });
    }

    @Override
    public int glBindTexGenParameterEXT(int unit, int coord, int value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int glBindTextureUnitParameterEXT(int unit, int value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBindTransformFeedbackNV(int target, int id)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBindVertexArray(int array)
    {
        CallDetails details = getMethodDetails("glBindVertexArray");
        details.foundArguments.add(new Object[] { array });
    }

    @Override
    public void glBindVertexShaderEXT(int id)
    {
        CallDetails details = getMethodDetails("glBindVertexShaderEXT");
        details.foundArguments.add(new Object[] { id });
    }

    @Override
    public void glBindVideoCaptureStreamBufferNV(int video_capture_slot, int stream, int frame_region, long offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBindVideoCaptureStreamTextureNV(int video_capture_slot, int stream, int frame_region, int target, int texture)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBitmap(int width, int height, float xorig, float yorig, float xmove, float ymove, ByteBuffer bitmap)
    {
        CallDetails details = getMethodDetails("glBitmap");
        details.foundArguments.add(new Object[] { width, height, xorig, yorig, xmove, ymove, bitmap });
    }

    @Override
    public void glBitmap(int width, int height, float xorig, float yorig, float xmove, float ymove, byte[] bitmap, int bitmap_offset)
    {
        CallDetails details = getMethodDetails("glBitmap");
        details.foundArguments.add(new Object[] { width, height, xorig, yorig, xmove, ymove, bitmap, bitmap_offset });
    }

    @Override
    public void glBitmap(int width, int height, float xorig, float yorig, float xmove, float ymove, long bitmap_buffer_offset)
    {
        CallDetails details = getMethodDetails("glBitmap");
        details.foundArguments.add(new Object[] { width, height, xorig, yorig, xmove, ymove, bitmap_buffer_offset });
    }

    @Override
    public void glBlendEquationIndexedAMD(int buf, int mode)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBlendEquationSeparateIndexedAMD(int buf, int modeRGB, int modeAlpha)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBlendFuncIndexedAMD(int buf, int src, int dst)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBlendFuncSeparateINGR(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBlendFuncSeparateIndexedAMD(int buf, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter)
    {
        CallDetails details = getMethodDetails("glBlitFramebuffer");
        details.foundArguments.add(new Object[] { srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter });
    }

    @Override
    public void glBufferParameteri(int target, int pname, int param)
    {
        CallDetails details = getMethodDetails("glBufferParameteri");
        details.foundArguments.add(new Object[] { target, pname, param });
    }

    @Override
    public void glCallList(int list)
    {
        CallDetails details = getMethodDetails("glCallList");
        details.foundArguments.add(new Object[] { list });

    }

    @Override
    public void glCallLists(int n, int type, Buffer lists)
    {
        CallDetails details = getMethodDetails("glCallLists");
        details.foundArguments.add(new Object[] { n, type, lists });
    }

    @Override
    public int glCheckNamedFramebufferStatusEXT(int framebuffer, int target)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glClearAccum(float red, float green, float blue, float alpha)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil)
    {
        CallDetails details = getMethodDetails("glClearBufferfi");
        details.foundArguments.add(new Object[] { buffer, drawbuffer, depth, stencil });
    }

    @Override
    public void glClearBufferfv(int buffer, int drawbuffer, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glClearBufferfv");
        details.foundArguments.add(new Object[] { buffer, drawbuffer, value });
    }

    @Override
    public void glClearBufferfv(int buffer, int drawbuffer, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glClearBufferfv");
        details.foundArguments.add(new Object[] { buffer, drawbuffer, value, value_offset });
    }

    @Override
    public void glClearBufferiv(int buffer, int drawbuffer, IntBuffer value)
    {
        CallDetails details = getMethodDetails("glClearBufferiv");
        details.foundArguments.add(new Object[] { buffer, drawbuffer, value });
    }

    @Override
    public void glClearBufferiv(int buffer, int drawbuffer, int[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glClearBufferiv");
        details.foundArguments.add(new Object[] { buffer, drawbuffer, value, value_offset });
    }

    @Override
    public void glClearBufferuiv(int buffer, int drawbuffer, IntBuffer value)
    {
        CallDetails details = getMethodDetails("glClearBufferiuv");
        details.foundArguments.add(new Object[] { buffer, drawbuffer, value });
    }

    @Override
    public void glClearBufferuiv(int buffer, int drawbuffer, int[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glClearBufferiuv");
        details.foundArguments.add(new Object[] { buffer, drawbuffer, value, value_offset });
    }

    @Override
    public void glClearColorIi(int red, int green, int blue, int alpha)
    {
        CallDetails details = getMethodDetails("glClearColorIi");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });

    }
    @Override
    public void glClearColorIui(int red, int green, int blue, int alpha)
    {
        CallDetails details = getMethodDetails("glClearColorIui");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glClearIndex(float c)
    {
        CallDetails details = getMethodDetails("glClearIndex");
        details.foundArguments.add(new Object[] { c });
    }

    @Override
    public void glClearNamedBufferData(int buffer, int internalformat, int format, int type, Buffer data)
    {
        CallDetails details = getMethodDetails("glClearNamedBufferData");
        details.foundArguments.add(new Object[] { buffer, internalformat, format, type, data });
    }

    @Override
    public void glClearNamedBufferSubData(int buffer, int internalformat, long offset, long size, int format, int type, Buffer data)
    {
        CallDetails details = getMethodDetails("glClearNamedBufferSubData");
        details.foundArguments.add(new Object[] { buffer, internalformat, offset, size, format, type, data, type, data });
    }

    @Override
    public void glClientAttribDefaultEXT(int mask)
    {
        CallDetails details = getMethodDetails("glClientAttribDefaultEXT");
        details.foundArguments.add(new Object[] { mask });
    }

    @Override
    public void glClipPlane(int plane, DoubleBuffer equation)
    {
        CallDetails details = getMethodDetails("glClipPlane");
        details.foundArguments.add(new Object[] { plane, equation });
    }

    @Override
    public void glClipPlane(int plane, double[] equation, int equation_offset)
    {
        CallDetails details = getMethodDetails("glClipPlane");
        details.foundArguments.add(new Object[] { plane, equation, equation_offset });
    }

    @Override
    public void glClipPlanef(int plane, FloatBuffer equation)
    {
        CallDetails details = getMethodDetails("glClipPlanef");
        details.foundArguments.add(new Object[] { plane, equation });
    }

    @Override
    public void glClipPlanef(int plane, float[] equation, int equation_offset)
    {
        CallDetails details = getMethodDetails("glClipPlanef");
        details.foundArguments.add(new Object[] { plane, equation, equation_offset });
    }

    @Override
    public void glColor3b(byte red, byte green, byte blue)
    {
        CallDetails details = getMethodDetails("glColor3b");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glColor3bv(ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glColor3bv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor3bv(byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor3bv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor3d(double red, double green, double blue)
    {
        CallDetails details = getMethodDetails("glColor3d");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glColor3dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glColor3d");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor3dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor3d");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor3f(float red, float green, float blue)
    {
        CallDetails details = getMethodDetails("glColor3f");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glColor3fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glColor3fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor3fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor3fv");
        details.foundArguments.add(new Object[] { v, v_offset});
    }

    @Override
    public void glColor3h(short red, short green, short blue)
    {
        CallDetails details = getMethodDetails("glColor3h");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glColor3hv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glColor3hv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor3hv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor3hv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor3i(int red, int green, int blue)
    {
        CallDetails details = getMethodDetails("glColor3i");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glColor3iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glColor3iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor3iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor3iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor3s(short red, short green, short blue)
    {
        CallDetails details = getMethodDetails("glColor3s");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glColor3sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glColor3sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor3sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor3sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor3ub(byte red, byte green, byte blue)
    {
        CallDetails details = getMethodDetails("glColor3ub");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glColor3ubv(ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glColor3ubv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor3ubv(byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor3ubv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor3ui(int red, int green, int blue)
    {
        CallDetails details = getMethodDetails("glColor3ui");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glColor3uiv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glColor3uiv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor3uiv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor3uiv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor3us(short red, short green, short blue)
    {
        CallDetails details = getMethodDetails("glColor3us");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glColor3usv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glColor3usv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor3usv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor3usv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor4b(byte red, byte green, byte blue, byte alpha)
    {
        CallDetails details = getMethodDetails("glColor4b");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glColor4bv(ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glColor4bv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor4bv(byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor4bv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor4d(double red, double green, double blue, double alpha)
    {
        CallDetails details = getMethodDetails("glColor4d");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glColor4dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glColor4dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor4dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor4dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor4fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glColor4fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor4fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor4fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor4h(short red, short green, short blue, short alpha)
    {
        CallDetails details = getMethodDetails("glColor4h");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glColor4hv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glColor4hv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor4hv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor4hv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor4i(int red, int green, int blue, int alpha)
    {
        CallDetails details = getMethodDetails("glColor4i");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glColor4iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glColor4iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor4iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor4iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor4s(short red, short green, short blue, short alpha)
    {
        CallDetails details = getMethodDetails("glColor4s");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glColor4sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glColor4sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor4sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor4sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor4ubv(ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glColor4ubv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor4ubv(byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor4ubv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor4ui(int red, int green, int blue, int alpha)
    {
        CallDetails details = getMethodDetails("glColor4ui");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glColor4uiv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glColor4uiv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor4uiv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor4uiv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColor4us(short red, short green, short blue, short alpha)
    {
        CallDetails details = getMethodDetails("glColor4us");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glColor4usv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glColor4usv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glColor4usv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glColor4usv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glColorMaskIndexed(int index, boolean r, boolean g, boolean b, boolean a)
    {
        CallDetails details = getMethodDetails("glColorMaskIndexed");
        details.foundArguments.add(new Object[] { index, r, g, b, a });
    }

    @Override
    public void glColorMaterial(int face, int mode)
    {
        CallDetails details = getMethodDetails("glColorMaterial");
        details.foundArguments.add(new Object[] { face, mode });
    }

    @Override
    public void glColorSubTable(int target, int start, int count, int format, int type, Buffer data)
    {
        CallDetails details = getMethodDetails("glColorSubTable");
        details.foundArguments.add(new Object[] { target, start, count, format, type, data });
    }

    @Override
    public void glColorSubTable(int target, int start, int count, int format, int type, long data_buffer_offset)
    {
        CallDetails details = getMethodDetails("glColorSubTable");
        details.foundArguments.add(new Object[] { target, start, count, format, type, data_buffer_offset });
    }

    @Override
    public void glColorTable(int target, int internalformat, int width, int format, int type, Buffer table)
    {
        CallDetails details = getMethodDetails("glColorTable");
        details.foundArguments.add(new Object[] { target, internalformat, width, format, type, table });
    }

    @Override
    public void glColorTable(int target, int internalformat, int width, int format, int type, long table_buffer_offset)
    {
        CallDetails details = getMethodDetails("glColorTable");
        details.foundArguments.add(new Object[] { target, internalformat, width, format, type, table_buffer_offset });
    }

    @Override
    public void glColorTableParameterfv(int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glColorTableParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glColorTableParameterfv(int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glColorTableParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glColorTableParameteriv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glColorTableParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glColorTableParameteriv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glColorTableParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glCompileShaderARB(long shaderObj)
    {
        CallDetails details = getMethodDetails("glCompileShaderARB");
        details.foundArguments.add(new Object[] { shaderObj });
    }

    @Override
    public void glCompressedMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width, int border, int imageSize, Buffer bits)
    {
        CallDetails details = getMethodDetails("glCompressedMultiTexImage1DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, internalformat, width, border, imageSize, bits });
    }

    @Override
    public void glCompressedMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer bits)
    {
        CallDetails details = getMethodDetails("glCompressedMultiTexImage2DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, internalformat, width, height, border, imageSize, bits });
    }

    @Override
    public void glCompressedMultiTexImage3DEXT(int texunit, int target, int level, int internalformat, int width, int height, int depth, int border, int imageSize, Buffer bits)
    {
        CallDetails details = getMethodDetails("glCompressedMultiTexImage3DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, internalformat, width, height, depth, border, imageSize, bits });
    }

    @Override
    public void glCompressedMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int width, int format, int imageSize, Buffer bits)
    {
        CallDetails details = getMethodDetails("glCompressedMultiTexSubImage1DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, xoffset, width, format, imageSize, bits });
    }

    @Override
    public void glCompressedMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer bits)
    {
        CallDetails details = getMethodDetails("glCompressedMultiTexSubImage2DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, xoffset, yoffset, width, height, format, imageSize, bits });
    }

    @Override
    public void glCompressedMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, Buffer bits)
    {
        CallDetails details = getMethodDetails("glCompressedMultiTexSubImage3DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, xoffset, yoffset, zoffset, width, depth, height, format, imageSize, bits });
    }

    @Override
    public void glCompressedTextureImage1DEXT(int texture, int target, int level, int internalformat, int width, int border, int imageSize, Buffer bits)
    {
        CallDetails details = getMethodDetails("glCompressedTextureImage1DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, internalformat, width, border, imageSize, bits });
    }

    @Override
    public void glCompressedTextureImage2DEXT(int texture, int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer bits)
    {
        CallDetails details = getMethodDetails("glCompressedTextureImage2DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, internalformat, width, height, border, imageSize, bits });
    }

    @Override
    public void glCompressedTextureImage3DEXT(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int imageSize, Buffer bits)
    {
        CallDetails details = getMethodDetails("glCompressedTextureImage3DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, internalformat, width, height, depth, border, imageSize, bits });
    }

    @Override
    public void glCompressedTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format, int imageSize, Buffer bits)
    {
        CallDetails details = getMethodDetails("glCompressedTextureSubImage1DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, xoffset, width, format, imageSize, bits });
    }

    @Override
    public void glCompressedTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer bits)
    {
        CallDetails details = getMethodDetails("glCompressedTextureSubImage2DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, xoffset, yoffset, width, height, format, imageSize, bits });
    }

    @Override
    public void glCompressedTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, Buffer bits)
    {
        CallDetails details = getMethodDetails("glCompressedTextureSubImage3DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, xoffset, yoffset, zoffset, width, height, depth, format, imageSize, bits });
    }

    @Override
    public void glConvolutionFilter1D(int target, int internalformat, int width, int format, int type, Buffer image)
    {
        CallDetails details = getMethodDetails("glConvolutionFilter1D");
        details.foundArguments.add(new Object[] { target, internalformat, width, format, type, image });
    }

    @Override
    public void glConvolutionFilter1D(int target, int internalformat, int width, int format, int type, long image_buffer_offset)
    {
        CallDetails details = getMethodDetails("glConvolutionFilter1D");
        details.foundArguments.add(new Object[] { target, internalformat, width, format, type, image_buffer_offset });
    }

    @Override
    public void glConvolutionFilter2D(int target, int internalformat, int width, int height, int format, int type, Buffer image)
    {
        CallDetails details = getMethodDetails("glConvolutionFilter2D");
        details.foundArguments.add(new Object[] { target, internalformat, width, height, format, type, image });
    }

    @Override
    public void glConvolutionFilter2D(int target, int internalformat, int width, int height, int format, int type, long image_buffer_offset)
    {
        CallDetails details = getMethodDetails("glConvolutionFilter2D");
        details.foundArguments.add(new Object[] { target, internalformat, width, height, format, type, image_buffer_offset });
    }

    @Override
    public void glConvolutionParameterf(int target, int pname, float params)
    {
        CallDetails details = getMethodDetails("glConvolutionParameterf");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glConvolutionParameterfv(int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glConvolutionParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glConvolutionParameterfv(int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glConvolutionParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glConvolutionParameteri(int target, int pname, int params)
    {
        CallDetails details = getMethodDetails("glConvolutionParameteri");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glConvolutionParameteriv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glConvolutionParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glConvolutionParameteriv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glConvolutionParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glCopyBufferSubData(int readTarget, int writeTarget, long readOffset, long writeOffset, long size)
    {
        CallDetails details = getMethodDetails("glCopyBufferSubData");
        details.foundArguments.add(new Object[] { readTarget, writeTarget, readOffset, writeOffset, size });
    }

    @Override
    public void glDeleteTransformFeedbacks(int n, IntBuffer ids)
    {
        CallDetails details = getMethodDetails("glDeleteTransformFeedbacks");
        details.foundArguments.add(new Object[] { n, ids });
    }

    @Override
    public void glDeleteTransformFeedbacks(int n, int[] ids, int ids_offset)
    {
        CallDetails details = getMethodDetails("glDeleteTransformFeedbacks");
        details.foundArguments.add(new Object[] { n, ids, ids_offset });
    }

    @Override
    public void glCopyColorSubTable(int target, int start, int x, int y, int width)
    {
        CallDetails details = getMethodDetails("glCopyColorSubTable");
        details.foundArguments.add(new Object[] { target, start, x, y, width });
    }

    @Override
    public void glCopyColorTable(int target, int internalformat, int x, int y, int width)
    {
        CallDetails details = getMethodDetails("glCopyColorTable");
        details.foundArguments.add(new Object[] { target, internalformat, x, y, width });
    }

    @Override
    public void glCopyConvolutionFilter1D(int target, int internalformat, int x, int y, int width)
    {
        CallDetails details = getMethodDetails("glCopyConvolutionFilter1D");
        details.foundArguments.add(new Object[] { target, internalformat, x, y, width });
    }

    @Override
    public void glCopyConvolutionFilter2D(int target, int internalformat, int x, int y, int width, int height)
    {
        CallDetails details = getMethodDetails("glCopyConvolutionFilter2D");
        details.foundArguments.add(new Object[] { target, internalformat, x, y, width, height});
    }

    @Override
    public void glCopyImageSubDataNV(int srcName, int srcTarget, int srcLevel, int srcX, int srcY, int srcZ, int dstName, int dstTarget, int dstLevel, int dstX, int dstY, int dstZ, int width, int height, int depth)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glCopyMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int x, int y, int width, int border)
    {
        CallDetails details = getMethodDetails("glCopyMultiTexImage1DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, internalformat, x, y, width, border });
    }

    @Override
    public void glCopyMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int x, int y, int width, int height, int border)
    {
        CallDetails details = getMethodDetails("glCopyMultiTexImage2DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, internalformat, x, y, width, height, border });
    }

    @Override
    public void glCopyMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int x, int y, int width)
    {
        CallDetails details = getMethodDetails("glCopyMultiTexSubImage1DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, xoffset, x, y, width });
    }

    @Override
    public void glCopyMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset, int x, int y, int width, int height)
    {
        CallDetails details = getMethodDetails("glCopyMultiTexSubImage2DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, xoffset, yoffset, x, y, width, height });
    }

    @Override
    public void glCopyMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height)
    {
        CallDetails details = getMethodDetails("glCopyMultiTexSubImage3DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, xoffset, yoffset, zoffset, x, y, width, height });
    }

    @Override
    public void glCopyPixels(int x, int y, int width, int height, int type)
    {
        CallDetails details = getMethodDetails("glCopyPixels");
        details.foundArguments.add(new Object[] { x, y, width, height, type });
    }

    @Override
    public void glCopyTextureImage1DEXT(int texture, int target, int level, int internalformat, int x, int y, int width, int border)
    {
        CallDetails details = getMethodDetails("glCopyTextureImage1DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, internalformat, x, y, width, border });
    }

    @Override
    public void glCopyTextureImage2DEXT(int texture, int target, int level, int internalformat, int x, int y, int width, int height, int border)
    {
        CallDetails details = getMethodDetails("glCopyTextureImage2DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, internalformat, x, y, width, height, border });
    }

    @Override
    public void glCopyTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int x, int y, int width)
    {
        CallDetails details = getMethodDetails("glCopyTextureSubImage1DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, xoffset, x, y, width });
    }

    @Override
    public void glCopyTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int x, int y, int width, int height)
    {
        CallDetails details = getMethodDetails("glCopyTextureSubImage2DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, xoffset, yoffset, x, y, width, height });
    }

    @Override
    public void glCopyTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height)
    {
        CallDetails details = getMethodDetails("glCopyTextureSubImage3DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, xoffset, yoffset, zoffset, x, y, width, height });
    }

    @Override
    public void glCoverageModulationNV(int components)
    {
        CallDetails details = getMethodDetails("glCoverageModulationNV");
        details.foundArguments.add(new Object[] { components });
    }

    @Override
    public void glCoverageModulationTableNV(int n, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glCoverageModulationTableNV");
        details.foundArguments.add(new Object[] { n, v });
    }

    @Override
    public void glCoverageModulationTableNV(int n, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glCoverageModulationTableNV");
        details.foundArguments.add(new Object[] { n, v, v_offset });
    }

    @Override
    public void glCreatePerfQueryINTEL(int queryId, IntBuffer queryHandle)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glCreatePerfQueryINTEL(int queryId, int[] queryHandle, int queryHandle_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long glCreateProgramObjectARB()
    {
        return 0;
    }

    @Override
    public long glCreateShaderObjectARB(int shaderType)
    {
        return 0;
    }

    @Override
    public void glCullParameterdvEXT(int pname, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glCullParameterdvEXT");
        details.foundArguments.add(new Object[] { pname, params });
    }

    @Override
    public void glCullParameterdvEXT(int pname, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glCullParameterdvEXT");
        details.foundArguments.add(new Object[] { pname, params, params_offset });
    }

    @Override
    public void glCullParameterfvEXT(int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glCullParameterfvEXT");
        details.foundArguments.add(new Object[] { pname, params });
    }

    @Override
    public void glCullParameterfvEXT(int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glCullParameterfvEXT");
        details.foundArguments.add(new Object[] { pname, params, params_offset });
    }

    @Override
    public void glCurrentPaletteMatrixARB(int index)
    {
        CallDetails details = getMethodDetails("glCurrentPaletteMatrixARB");
        details.foundArguments.add(new Object[]{index});
    }

    @Override
    public void glDeleteLists(int list, int range)
    {
        CallDetails details = getMethodDetails("glDeleteLists");
        details.foundArguments.add(new Object[] { list, range });
    }

    @Override
    public void glDeleteNamesAMD(int identifier, int num, IntBuffer names)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDeleteNamesAMD(int identifier, int num, int[] names, int names_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDeleteObjectARB(long obj)
    {
        CallDetails details = getMethodDetails("glDeleteObjectARB");
        details.foundArguments.add(new Object[] { obj });
    }

    @Override
    public void glDeleteOcclusionQueriesNV(int n, IntBuffer ids)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDeleteOcclusionQueriesNV(int n, int[] ids, int ids_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDeletePerfMonitorsAMD(int n, IntBuffer monitors)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDeletePerfMonitorsAMD(int n, int[] monitors, int monitors_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDeletePerfQueryINTEL(int queryHandle)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDeleteProgramsARB(int n, IntBuffer programs)
    {
        CallDetails details = getMethodDetails("glDeleteProgramsARB");
        details.foundArguments.add(new Object[] { n, programs });
    }

    @Override
    public void glDeleteProgramsARB(int n, int[] programs, int programs_offset)
    {
        CallDetails details = getMethodDetails("glDeleteProgramsARB");
        details.foundArguments.add(new Object[] { n, programs, programs_offset });
    }

    @Override
    public void glDeleteTransformFeedbacksNV(int n, IntBuffer ids)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDeleteTransformFeedbacksNV(int n, int[] ids, int ids_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDeleteVertexArrays(int n, IntBuffer arrays)
    {
        CallDetails details = getMethodDetails("glDeleteVertexArrays");
        details.foundArguments.add(new Object[] { n, arrays });
    }

    @Override
    public void glDeleteVertexArrays(int n, int[] arrays, int arrays_offset)
    {
        CallDetails details = getMethodDetails("glDeleteVertexArrays");
        details.foundArguments.add(new Object[] { n, arrays, arrays_offset });
    }

    @Override
    public void glDeleteVertexShaderEXT(int id)
    {
        CallDetails details = getMethodDetails("glDeleteVertexShaderEXT");
        details.foundArguments.add(new Object[] { id });
    }

    @Override
    public void glDepthBoundsEXT(double zmin, double zmax)
    {
        CallDetails details = getMethodDetails("glDepthBoundsEXT");
        details.foundArguments.add(new Object[] { zmin, zmax });
    }

    @Override
    public void glDetachObjectARB(long containerObj, long attachedObj)
    {
        CallDetails details = getMethodDetails("glDetachObjectARB");
        details.foundArguments.add(new Object[] { containerObj, attachedObj });
    }

    @Override
    public void glDisableClientStateIndexedEXT(int array, int index)
    {
        CallDetails details = getMethodDetails("glDisableClientStateIndexedEXT");
        details.foundArguments.add(new Object[] { array, index });
    }

    @Override
    public void glDisableClientStateiEXT(int array, int index)
    {
        CallDetails details = getMethodDetails("glDisableClientStateiEXT");
        details.foundArguments.add(new Object[] { array, index });
    }

    @Override
    public void glDisableIndexed(int target, int index)
    {
        CallDetails details = getMethodDetails("glDisableIndexed");
        details.foundArguments.add(new Object[] { target, index });
    }

    @Override
    public void glDisableVariantClientStateEXT(int id)
    {
        CallDetails details = getMethodDetails("glDisableVariantClientStateEXT");
        details.foundArguments.add(new Object[] { id });
    }

    @Override
    public void glDisableVertexArrayAttribEXT(int vaobj, int index)
    {
        CallDetails details = getMethodDetails("glDisableVertexArrayAttribEXT");
        details.foundArguments.add(new Object[] { vaobj, index });
    }

    @Override
    public void glDisableVertexArrayEXT(int vaobj, int array)
    {
        CallDetails details = getMethodDetails("glDisableVertexArrayEXT");
        details.foundArguments.add(new Object[] { vaobj, array });
    }

    @Override
    public void glDisableVertexAttribAPPLE(int index, int pname)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDisableVertexAttribArrayARB(int index)
    {
        CallDetails details = getMethodDetails("glDisableVertexAttribArrayARB");
        details.foundArguments.add(new Object[] { index });
    }

    @Override
    public void glDrawArraysInstanced(int mode, int first, int count, int instancecount)
    {
        CallDetails details = getMethodDetails("glDrawArraysInstanced");
        details.foundArguments.add(new Object[] { mode, first, count, instancecount });
    }

    @Override
    public void glDrawBuffers(int n, IntBuffer bufs)
    {
        CallDetails details = getMethodDetails("glDrawBuffers");
        details.foundArguments.add(new Object[] { n, bufs });
    }

    @Override
    public void glDrawBuffers(int n, int[] bufs, int bufs_offset)
    {
        CallDetails details = getMethodDetails("glDrawBuffers");
        details.foundArguments.add(new Object[] { n, bufs, bufs_offset });
    }

    @Override
    public void glDrawElementsInstancedBaseInstance(int mode, int count, int type, long indices_buffer_offset, int instancecount, int baseinstance)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDrawElementsInstancedBaseVertexBaseInstance(int mode, int count, int type, long indices_buffer_offset, int instancecount, int basevertex, int baseinstance)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDrawBuffersATI(int n, IntBuffer bufs)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDrawBuffersATI(int n, int[] bufs, int bufs_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDrawElementsInstanced(int mode, int count, int type, Buffer indices, int instancecount)
    {
        CallDetails details = getMethodDetails("glDrawElementsInstanced");
        details.foundArguments.add(new Object[] { mode, count, type, indices, instancecount });
    }

    @Override
    public void glDrawElementsInstanced(int mode, int count, int type, long indices_buffer_offset, int instancecount)
    {
        CallDetails details = getMethodDetails("glDrawElementsInstanced");
        details.foundArguments.add(new Object[] { mode, count, type, indices_buffer_offset, instancecount });
    }

    @Override
    public void glDrawPixels(int width, int height, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glDrawPixels");
        details.foundArguments.add(new Object[] { width, height, format, type, pixels });
    }

    @Override
    public void glDrawPixels(int width, int height, int format, int type, long pixels_buffer_offset)
    {
        CallDetails details = getMethodDetails("glDrawPixels");
        details.foundArguments.add(new Object[] { width, height, format, type, pixels_buffer_offset });
    }

    @Override
    public void glDrawRangeElements(int mode, int start, int end, int count, int type, Buffer indices)
    {
        CallDetails details = getMethodDetails("glDrawRangeElements");
        details.foundArguments.add(new Object[] { mode, start, end, count, type, indices });
    }

    @Override
    public void glDrawRangeElements(int mode, int start, int end, int count, int type, long indices_buffer_offset)
    {
        CallDetails details = getMethodDetails("glDrawRangeElements");
        details.foundArguments.add(new Object[] { mode, start, end, count, type, indices_buffer_offset });
    }

    @Override
    public void glDrawTextureNV(int texture, int sampler, float x0, float y0, float x1, float y1, float z, float s0, float t0, float s1, float t1)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDrawTransformFeedbackNV(int mode, int id)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glEdgeFlag(boolean flag)
    {
        CallDetails details = getMethodDetails("glEdgeFlag");
        details.foundArguments.add(new Object[] { flag });
    }

    @Override
    public void glEdgeFlagPointer(int stride, Buffer ptr)
    {
        CallDetails details = getMethodDetails("glEdgeFlagPointer");
        details.foundArguments.add(new Object[] { stride, ptr });
    }

    @Override
    public void glEdgeFlagPointer(int stride, long ptr_buffer_offset)
    {
        CallDetails details = getMethodDetails("glEdgeFlagPointer");
        details.foundArguments.add(new Object[] { stride, ptr_buffer_offset});
    }

    @Override
    public void glEdgeFlagv(ByteBuffer flag)
    {
        CallDetails details = getMethodDetails("glEdgeFlagv");
        details.foundArguments.add(new Object[] { flag });
    }

    @Override
    public void glEdgeFlagv(byte[] flag, int flag_offset)
    {
        CallDetails details = getMethodDetails("glEdgeFlagv");
        details.foundArguments.add(new Object[] { flag, flag_offset });
    }

    @Override
    public void glEnableClientStateIndexedEXT(int array, int index)
    {
        CallDetails details = getMethodDetails("glEnableClientStateIndexedEXT");
        details.foundArguments.add(new Object[] { array, index });
    }

    @Override
    public void glEnableClientStateiEXT(int array, int index)
    {
        CallDetails details = getMethodDetails("glEnableClientStateiEXT");
        details.foundArguments.add(new Object[] { array, index });
    }

    @Override
    public void glEnableIndexed(int target, int index)
    {
        CallDetails details = getMethodDetails("glEnableIndexed");
        details.foundArguments.add(new Object[] { target, index });
    }

    @Override
    public void glEnableVariantClientStateEXT(int id)
    {
        CallDetails details = getMethodDetails("glEnableVariantClientStateEXT");
        details.foundArguments.add(new Object[] { id });
    }

    @Override
    public void glEnableVertexArrayAttribEXT(int vaobj, int index)
    {
        CallDetails details = getMethodDetails("glEnableVertexArrayAttribEXT");
        details.foundArguments.add(new Object[] { vaobj, index });
    }

    @Override
    public void glEnableVertexArrayEXT(int vaobj, int array)
    {
        CallDetails details = getMethodDetails("glEnableVertexArrayEXT");
        details.foundArguments.add(new Object[] { vaobj, array });
    }

    @Override
    public void glEnableVertexAttribAPPLE(int index, int pname)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glEnableVertexAttribArrayARB(int index)
    {
        CallDetails details = getMethodDetails("glEnableVertexAttribArrayARB");
        details.foundArguments.add(new Object[] { index });
    }

    @Override
    public void glEnd()
    {
        CallDetails details = getMethodDetails("glEnd");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glEndConditionalRenderNVX()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glEndList()
    {
        CallDetails details = getMethodDetails("glEndList");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glEndOcclusionQueryNV()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glEndPerfMonitorAMD(int monitor)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glEndPerfQueryINTEL(int queryHandle)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glEndTransformFeedback()
    {
        CallDetails details = getMethodDetails("glEndTransformFeedback");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glFramebufferParameteri(int target, int pname, int param)
    {
        CallDetails details = getMethodDetails("glFramebufferParameteri");
        details.foundArguments.add(new Object[] { target, pname, param });
    }

    @Override
    public void glEndVertexShaderEXT()
    {
        CallDetails details = getMethodDetails("glEndVertexShaderEXT");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glEndVideoCaptureNV(int video_capture_slot)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glEvalCoord1d(double u)
    {
        CallDetails details = getMethodDetails("glEvalCoord1d");
        details.foundArguments.add(new Object[] { u });
    }

    @Override
    public void glEvalCoord1dv(DoubleBuffer u)
    {
        CallDetails details = getMethodDetails("glEvalCoord1dv");
        details.foundArguments.add(new Object[] { u });
    }

    @Override
    public void glEvalCoord1dv(double[] u, int u_offset)
    {
        CallDetails details = getMethodDetails("glEvalCoord1dv");
        details.foundArguments.add(new Object[] { u, u_offset });
    }

    @Override
    public void glEvalCoord1f(float u)
    {
        CallDetails details = getMethodDetails("glEvalCoord1f");
        details.foundArguments.add(new Object[] { u });
    }

    @Override
    public void glEvalCoord1fv(FloatBuffer u)
    {
        CallDetails details = getMethodDetails("glEvalCoord1fv");
        details.foundArguments.add(new Object[] { u });
    }

    @Override
    public void glEvalCoord1fv(float[] u, int u_offset)
    {
        CallDetails details = getMethodDetails("glEvalCoord1fv");
        details.foundArguments.add(new Object[] { u, u_offset });
    }

    @Override
    public void glEvalCoord2d(double u, double v)
    {
        CallDetails details = getMethodDetails("glEvalCoord2d");
        details.foundArguments.add(new Object[] { u, v });
    }

    @Override
    public void glEvalCoord2dv(DoubleBuffer u)
    {
        CallDetails details = getMethodDetails("glEvalCoord2dv");
        details.foundArguments.add(new Object[] { u });
    }

    @Override
    public void glEvalCoord2dv(double[] u, int u_offset)
    {
        CallDetails details = getMethodDetails("glEvalCoord2dv");
        details.foundArguments.add(new Object[] { u, u_offset });
    }

    @Override
    public void glEvalCoord2f(float u, float v)
    {
        CallDetails details = getMethodDetails("glEvalCoord2f");
        details.foundArguments.add(new Object[] { u, v });
    }

    @Override
    public void glEvalCoord2fv(FloatBuffer u)
    {
        CallDetails details = getMethodDetails("glEvalCoord2fv");
        details.foundArguments.add(new Object[] { u });
    }

    @Override
    public void glEvalCoord2fv(float[] u, int u_offset)
    {
        CallDetails details = getMethodDetails("glEvalCoord2fv");
        details.foundArguments.add(new Object[] { u, u_offset });
    }

    @Override
    public void glEvalMapsNV(int target, int mode)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glEvalMesh1(int mode, int i1, int i2)
    {
        CallDetails details = getMethodDetails("glEvalMesh1");
        details.foundArguments.add(new Object[] { mode, i1, i2 });
    }

    @Override
    public void glEvalMesh2(int mode, int i1, int i2, int j1, int j2)
    {
        CallDetails details = getMethodDetails("glEvalMesh2");
        details.foundArguments.add(new Object[] { mode, i1, i2, j1, j2 });
    }

    @Override
    public void glEvalPoint1(int i)
    {
        CallDetails details = getMethodDetails("glEvalPoint1");
        details.foundArguments.add(new Object[] { i });
    }

    @Override
    public void glEvalPoint2(int i, int j)
    {
        CallDetails details = getMethodDetails("glEvalPoint2");
        details.foundArguments.add(new Object[] { i, j });
    }

    @Override
    public void glExtractComponentEXT(int res, int src, int num)
    {
        CallDetails details = getMethodDetails("glExtractComponentEXT");
        details.foundArguments.add(new Object[] { res, src, num });
    }

    @Override
    public void glFeedbackBuffer(int size, int type, FloatBuffer buffer)
    {
        CallDetails details = getMethodDetails("glFeedbackBuffer");
        details.foundArguments.add(new Object[] { size, type, buffer });
    }

    @Override
    public void glFinishTextureSUNX()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glFlushMappedNamedBufferRangeEXT(int buffer, long offset, long length)
    {
        CallDetails details = getMethodDetails("glFlushMappedNamedBufferRangeEXT");
        details.foundArguments.add(new Object[] { buffer, offset, length });
    }

    @Override
    public void glFlushPixelDataRangeNV(int target)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glFlushVertexArrayRangeAPPLE(int length, Buffer pointer)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glFogCoordPointer(int type, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glFogCoordPointer");
        details.foundArguments.add(new Object[] { type, stride, pointer });
    }

    @Override
    public void glFogCoordPointer(int type, int stride, long pointer_buffer_offset)
    {
        CallDetails details = getMethodDetails("glFogCoordPointer");
        details.foundArguments.add(new Object[] { type, stride, pointer_buffer_offset });
    }

    @Override
    public void glFogCoordd(double coord)
    {
        CallDetails details = getMethodDetails("glFogCoordd");
        details.foundArguments.add(new Object[] { coord });
    }

    @Override
    public void glFogCoorddv(DoubleBuffer coord)
    {
        CallDetails details = getMethodDetails("glFogCoorddv");
        details.foundArguments.add(new Object[] { coord });
    }

    @Override
    public void glFogCoorddv(double[] coord, int coord_offset)
    {
        CallDetails details = getMethodDetails("glFogCoorddv");
        details.foundArguments.add(new Object[] { coord, coord_offset });
    }

    @Override
    public void glFogCoordf(float coord)
    {
        CallDetails details = getMethodDetails("glFogCoordf");
        details.foundArguments.add(new Object[] { coord });
    }

    @Override
    public void glFogCoordfv(FloatBuffer coord)
    {
        CallDetails details = getMethodDetails("glFogCoordfv");
        details.foundArguments.add(new Object[] { coord });
    }

    @Override
    public void glFogCoordfv(float[] coord, int coord_offset)
    {
        CallDetails details = getMethodDetails("glFogCoordfv");
        details.foundArguments.add(new Object[] { coord, coord_offset });
    }

    @Override
    public void glFogCoordh(short fog)
    {
        CallDetails details = getMethodDetails("glFogCoordh");
        details.foundArguments.add(new Object[] { fog });
    }

    @Override
    public void glFogCoordhv(ShortBuffer fog)
    {
        CallDetails details = getMethodDetails("glFogCoordhv");
        details.foundArguments.add(new Object[] { fog });
    }

    @Override
    public void glFogCoordhv(short[] fog, int fog_offset)
    {
        CallDetails details = getMethodDetails("glFogCoordhv");
        details.foundArguments.add(new Object[] { fog, fog_offset });
    }

    @Override
    public void glFogi(int pname, int param)
    {
        CallDetails details = getMethodDetails("glFogi");
        details.foundArguments.add(new Object[] { pname, param });
    }

    @Override
    public void glFogiv(int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glFogiv");
        details.foundArguments.add(new Object[] { pname, params });
    }

    @Override
    public void glFogiv(int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glFogiv");
        details.foundArguments.add(new Object[] { pname, params, params_offset });
    }

    @Override
    public void glFragmentCoverageColorNV(int color)
    {
        CallDetails details = getMethodDetails("glFragmentCoverageColorNV");
        details.foundArguments.add(new Object[] { color });
    }

    @Override
    public void glFrameTerminatorGREMEDY()
    {
        CallDetails details = getMethodDetails("glFrameTerminatorGREMEDY");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glFramebufferDrawBufferEXT(int framebuffer, int mode)
    {
        CallDetails details = getMethodDetails("glFramebufferDrawBufferEXT");
        details.foundArguments.add(new Object[] { framebuffer, mode });
    }

    @Override
    public void glFramebufferDrawBuffersEXT(int framebuffer, int n, IntBuffer bufs)
    {
        CallDetails details = getMethodDetails("glFramebufferDrawBuffersEXT");
        details.foundArguments.add(new Object[] { framebuffer, n, bufs });
    }

    @Override
    public void glFramebufferDrawBuffersEXT(int framebuffer, int n, int[] bufs, int bufs_offset)
    {
        CallDetails details = getMethodDetails("glFramebufferDrawBuffersEXT");
        details.foundArguments.add(new Object[] { framebuffer, n, bufs, bufs_offset });
    }

    @Override
    public void glFramebufferReadBufferEXT(int framebuffer, int mode)
    {
        CallDetails details = getMethodDetails("glFramebufferReadBufferEXT");
        details.foundArguments.add(new Object[] { framebuffer, mode });
    }

    @Override
    public void glFramebufferSampleLocationsfvNV(int target, int start, int count, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glFramebufferSampleLocationsfvNV");
        details.foundArguments.add(new Object[] { target, start, count, v });
    }

    @Override
    public void glFramebufferSampleLocationsfvNV(int target, int start, int count, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glFramebufferSampleLocationsfvNV");
        details.foundArguments.add(new Object[] { target, start, count, v, v_offset });
    }

    @Override
    public void glFramebufferTextureEXT(int target, int attachment, int texture, int level)
    {
        CallDetails details = getMethodDetails("glFramebufferTextureEXT");
        details.foundArguments.add(new Object[] { target, attachment, texture, level});
    }

    @Override
    public void glFramebufferTextureFaceEXT(int target, int attachment, int texture, int level, int face)
    {
        CallDetails details = getMethodDetails("glFramebufferTextureFaceEXT");
        details.foundArguments.add(new Object[] { target, attachment, texture, level, face });
    }

    @Override
    public void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer)
    {
        CallDetails details = getMethodDetails("glFramebufferTextureLayer");
        details.foundArguments.add(new Object[] { target, attachment, texture, level, layer });
    }

    @Override
    public void glGenTransformFeedbacks(int n, IntBuffer ids)
    {
        CallDetails details = getMethodDetails("glGenTransformFeedbacks");
        details.foundArguments.add(new Object[] { n, ids });
    }

    @Override
    public void glGenTransformFeedbacks(int n, int[] ids, int ids_offset)
    {
        CallDetails details = getMethodDetails("glGenTransformFeedbacks");
        details.foundArguments.add(new Object[] { n, ids, ids_offset });
    }

    @Override
    public int glGenLists(int range)
    {
        return 0;
    }

    @Override
    public void glGenNamesAMD(int identifier, int num, IntBuffer names)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGenNamesAMD(int identifier, int num, int[] names, int names_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGenOcclusionQueriesNV(int n, IntBuffer ids)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGenOcclusionQueriesNV(int n, int[] ids, int ids_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGenPerfMonitorsAMD(int n, IntBuffer monitors)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGenPerfMonitorsAMD(int n, int[] monitors, int monitors_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGenProgramsARB(int n, IntBuffer programs)
    {
        CallDetails details = getMethodDetails("glGenProgramsARB");
        details.foundArguments.add(new Object[] { n, programs });
    }

    @Override
    public void glGenProgramsARB(int n, int[] programs, int programs_offset)
    {
        CallDetails details = getMethodDetails("glGenProgramsARB");
        details.foundArguments.add(new Object[] { n, programs, programs_offset });
    }

    @Override
    public int glGenSymbolsEXT(int datatype, int storagetype, int range, int components)
    {
        return 0;
    }

    @Override
    public void glGenTransformFeedbacksNV(int n, IntBuffer ids)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGenTransformFeedbacksNV(int n, int[] ids, int ids_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGenVertexArrays(int n, IntBuffer arrays)
    {
        CallDetails details = getMethodDetails("glGenVertexArrays");
        details.foundArguments.add(new Object[] { n, arrays });
    }

    @Override
    public void glGenVertexArrays(int n, int[] arrays, int arrays_offset)
    {
        CallDetails details = getMethodDetails("glGenVertexArrays");
        details.foundArguments.add(new Object[] { n, arrays, arrays_offset });
    }

    @Override
    public int glGenVertexShadersEXT(int range)
    {
        return 0;
    }

    @Override
    public void glGenerateMultiTexMipmapEXT(int texunit, int target)
    {
        CallDetails details = getMethodDetails("glGenerateMultiTexMipmapEXT");
        details.foundArguments.add(new Object[] { texunit, target });
    }

    @Override
    public void glGenerateTextureMipmapEXT(int texture, int target)
    {
        CallDetails details = getMethodDetails("glGenerateTextureMipmapEXT");
        details.foundArguments.add(new Object[] { texture, target });
    }

    @Override
    public void glGetActiveUniformARB(long programObj, int index, int maxLength, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name)
    {
        CallDetails details = getMethodDetails("glGetActiveUniformARB");
        details.foundArguments.add(new Object[] { programObj, index, maxLength, length, size, type, name });
    }

    @Override
    public void glGetActiveUniformARB(long programObj, int index, int maxLength, int[] length, int length_offset, int[] size, int size_offset, int[] type, int type_offset, byte[] name, int name_offset)
    {
        CallDetails details = getMethodDetails("glGetActiveUniformARB");
        details.foundArguments.add(new Object[] { programObj, index, maxLength, length, length_offset, size, size_offset, type, type_offset, name, name_offset });
    }

    @Override
    public void glGetAttachedObjectsARB(long containerObj, int maxCount, IntBuffer count, LongBuffer obj)
    {
        CallDetails details = getMethodDetails("glGetAttachedObjectsARB");
        details.foundArguments.add(new Object[] { containerObj, maxCount, count, obj });
    }

    @Override
    public void glGetAttachedObjectsARB(long containerObj, int maxCount, int[] count, int count_offset, long[] obj, int obj_offset)
    {
        CallDetails details = getMethodDetails("glGetAttachedObjectsARB");
        details.foundArguments.add(new Object[] { containerObj, maxCount, count, count_offset, obj, obj_offset });
    }

    @Override
    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize, IntBuffer length, ByteBuffer uniformBlockName)
    {
        CallDetails details = getMethodDetails("glGetActiveUniformBlockName");
        details.foundArguments.add(new Object[] { program, uniformBlockIndex, bufSize, length, uniformBlockName });
    }

    @Override
    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize, int[] length, int length_offset, byte[] uniformBlockName, int uniformBlockName_offset)
    {
        CallDetails details = getMethodDetails("glGetActiveUniformBlockName");
        details.foundArguments.add(new Object[] { program, uniformBlockIndex, bufSize, length, length_offset, uniformBlockName, uniformBlockName_offset });
    }

    @Override
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetActiveUniformBlockiv");
        details.foundArguments.add(new Object[] { program, uniformBlockIndex, pname, params });
    }

    @Override
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetActiveUniformBlockiv");
        details.foundArguments.add(new Object[] { program, uniformBlockIndex, pname, params, params_offset });
    }

    @Override
    public void glGetActiveUniformsiv(int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetActiveUniformsiv");
        details.foundArguments.add(new Object[] { program, uniformCount, uniformIndices, pname, params });
    }

    @Override
    public void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int uniformIndices_offset, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetActiveUniformsiv");
        details.foundArguments.add(new Object[] { program, uniformCount, uniformIndices, pname, params, params_offset });
    }

    @Override
    public void glGetBooleanIndexedv(int target, int index, ByteBuffer data)
    {
        CallDetails details = getMethodDetails("glGetBooleanIndexedv");
        details.foundArguments.add(new Object[] { target, index, data });
    }

    @Override
    public void glGetBooleanIndexedv(int target, int index, byte[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetBooleanIndexedv");
        details.foundArguments.add(new Object[] { target, index, data, data_offset });
    }

    @Override
    public void glGetClipPlane(int plane, DoubleBuffer equation)
    {
        CallDetails details = getMethodDetails("glGetClipPlane");
        details.foundArguments.add(new Object[] { plane, equation });
    }

    @Override
    public void glGetClipPlane(int plane, double[] equation, int equation_offset)
    {
        CallDetails details = getMethodDetails("glGetClipPlane");
        details.foundArguments.add(new Object[] { plane, equation, equation_offset });
    }

    @Override
    public void glGetClipPlanef(int plane, FloatBuffer equation)
    {
        CallDetails details = getMethodDetails("glGetClipPlanef");
        details.foundArguments.add(new Object[] { plane, equation });
    }

    @Override
    public void glGetClipPlanef(int plane, float[] equation, int equation_offset)
    {
        CallDetails details = getMethodDetails("glGetClipPlanef");
        details.foundArguments.add(new Object[] { plane, equation, equation_offset });
    }

    @Override
    public void glGetColorTable(int target, int format, int type, Buffer table)
    {
        CallDetails details = getMethodDetails("glGetColorTable");
        details.foundArguments.add(new Object[] { target, format, type, table });
    }

    @Override
    public void glGetColorTable(int target, int format, int type, long table_buffer_offset)
    {
        CallDetails details = getMethodDetails("glGetColorTable");
        details.foundArguments.add(new Object[] { target, format, type, table_buffer_offset });
    }

    @Override
    public void glGetColorTableParameterfv(int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetColorTableParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetColorTableParameterfv(int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetColorTableParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetColorTableParameteriv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetColorTableParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetColorTableParameteriv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetColorTableParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetCompressedMultiTexImageEXT(int texunit, int target, int lod, Buffer img)
    {
        CallDetails details = getMethodDetails("glGetCompressedMultiTexImageEXT");
        details.foundArguments.add(new Object[] { texunit, target, lod, img });
    }

    @Override
    public void glGetCompressedTextureImageEXT(int texture, int target, int lod, Buffer img)
    {
        CallDetails details = getMethodDetails("glGetCompressedTextureImageEXT");
        details.foundArguments.add(new Object[] { texture, target, lod, img });
    }

    @Override
    public void glGetConvolutionFilter(int target, int format, int type, Buffer image)
    {
        CallDetails details = getMethodDetails("glGetConvolutionFilter");
        details.foundArguments.add(new Object[] { target, format, type, image });
    }

    @Override
    public void glGetConvolutionFilter(int target, int format, int type, long image_buffer_offset)
    {
        CallDetails details = getMethodDetails("glGetConvolutionFilter");
        details.foundArguments.add(new Object[] { target, format, type, image_buffer_offset });
    }

    @Override
    public void glGetConvolutionParameterfv(int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetConvolutionParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetConvolutionParameterfv(int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetConvolutionParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetConvolutionParameteriv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetConvolutionParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetConvolutionParameteriv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetConvolutionParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetCoverageModulationTableNV(int bufsize, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glGetCoverageModulationTableNV");
        details.foundArguments.add(new Object[] { bufsize, v });
    }

    @Override
    public void glGetCoverageModulationTableNV(int bufsize, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glGetCoverageModulationTableNV");
        details.foundArguments.add(new Object[] { bufsize, v, v_offset });
    }

    @Override
    public void glGetDoubleIndexedvEXT(int target, int index, DoubleBuffer data)
    {
        CallDetails details = getMethodDetails("glGetDoubleIndexedvEXT");
        details.foundArguments.add(new Object[] { target, index, data });
    }

    @Override
    public void glGetDoubleIndexedvEXT(int target, int index, double[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetDoubleIndexedvEXT");
        details.foundArguments.add(new Object[] { target, index, data, data_offset });
    }

    @Override
    public void glGetDoublei_vEXT(int pname, int index, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glGetDoublei_vEXT");
        details.foundArguments.add(new Object[] { pname, index, params });
    }

    @Override
    public void glGetDoublei_vEXT(int pname, int index, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetDoublei_vEXT");
        details.foundArguments.add(new Object[] { pname, index, params, params_offset });
    }

    @Override
    public void glGetFirstPerfQueryIdINTEL(IntBuffer queryId)
    {

    }

    @Override
    public void glGetFirstPerfQueryIdINTEL(int[] queryId, int queryId_offset)
    {

    }

    @Override
    public void glGetFloatIndexedvEXT(int target, int index, FloatBuffer data)
    {
        CallDetails details = getMethodDetails("glGetFloatIndexedvEXT");
        details.foundArguments.add(new Object[] { target, index, data });
    }

    @Override
    public void glGetFloatIndexedvEXT(int target, int index, float[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetFloatIndexedvEXT");
        details.foundArguments.add(new Object[] { target, index, data, data_offset });
    }

    @Override
    public void glGetFloati_vEXT(int pname, int index, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetFloati_vEXT");
        details.foundArguments.add(new Object[] { pname, index, params });
    }

    @Override
    public void glGetFloati_vEXT(int pname, int index, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetFloati_vEXT");
        details.foundArguments.add(new Object[] { pname, index, params, params_offset });
    }

    @Override
    public int glGetFragDataLocation(int program, String name)
    {
        return 0;
    }

    @Override
    public void glGetFramebufferParameteriv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetFramebufferParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetFramebufferParameteriv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetFramebufferParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetFramebufferParameterivEXT(int framebuffer, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetFramebufferParameterivEXT");
        details.foundArguments.add(new Object[] { framebuffer, pname, params });
    }

    @Override
    public void glGetFramebufferParameterivEXT(int framebuffer, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetFramebufferParameterivEXT");
        details.foundArguments.add(new Object[] { framebuffer, pname, params, params_offset });
    }

    @Override
    public long glGetHandleARB(int pname)
    {
        return 0;
    }

    @Override
    public void glGetHistogram(int target, boolean reset, int format, int type, Buffer values)
    {
        CallDetails details = getMethodDetails("glGetHistogram");
        details.foundArguments.add(new Object[] { target, reset, format, type, values });
    }

    @Override
    public void glGetHistogram(int target, boolean reset, int format, int type, long values_buffer_offset)
    {
        CallDetails details = getMethodDetails("glGetHistogram");
        details.foundArguments.add(new Object[] { target, reset, format, type, values_buffer_offset });
    }

    @Override
    public void glGetHistogramParameterfv(int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetHistogramParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetHistogramParameterfv(int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetHistogramParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetHistogramParameteriv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetHistogramParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetHistogramParameteriv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetHistogramParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetInfoLogARB(long obj, int maxLength, IntBuffer length, ByteBuffer infoLog)
    {
        CallDetails details = getMethodDetails("glGetInfoLogARB");
        details.foundArguments.add(new Object[] { obj, maxLength, length, infoLog });
    }

    @Override
    public void glGetInfoLogARB(long obj, int maxLength, int[] length, int length_offset, byte[] infoLog, int infoLog_offset)
    {
        CallDetails details = getMethodDetails("glGetInfoLogARB");
        details.foundArguments.add(new Object[] { obj, maxLength, length, length_offset, infoLog, infoLog_offset });
    }

    @Override
    public void glGetIntegerIndexedv(int target, int index, IntBuffer data)
    {
        CallDetails details = getMethodDetails("glGetIntegerIndexedv");
        details.foundArguments.add(new Object[] { target, index, data });
    }

    @Override
    public void glGetIntegerIndexedv(int target, int index, int[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetIntegerIndexedv");
        details.foundArguments.add(new Object[] { target, index, data, data_offset });
    }

    @Override
    public void glGetIntegeri_v(int target, int index, IntBuffer data)
    {
        CallDetails details = getMethodDetails("glGetIntegeri_v");
        details.foundArguments.add(new Object[] { target, index, data });
    }

    @Override
    public void glGetIntegeri_v(int target, int index, int[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetIntegeri_v");
        details.foundArguments.add(new Object[] { target, index, data, data_offset });
    }

    @Override
    public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetInternalformativ");
        details.foundArguments.add(new Object[] { target, internalformat, pname, bufSize, params });
    }

    @Override
    public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetInternalformativ");
        details.foundArguments.add(new Object[] { target, internalformat, pname, bufSize, params, params_offset });
    }

    @Override
    public void glGetInvariantBooleanvEXT(int id, int value, ByteBuffer data)
    {
        CallDetails details = getMethodDetails("glGetInvariantBooleanvEXT");
        details.foundArguments.add(new Object[] { id, value, data });
    }

    @Override
    public void glGetInvariantBooleanvEXT(int id, int value, byte[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetInvariantBooleanvEXT");
        details.foundArguments.add(new Object[] { id, value, data, data_offset });
    }

    @Override
    public void glGetInvariantFloatvEXT(int id, int value, FloatBuffer data)
    {
        CallDetails details = getMethodDetails("glGetInvariantFloatvEXT");
        details.foundArguments.add(new Object[] { id, value, data });
    }

    @Override
    public void glGetInvariantFloatvEXT(int id, int value, float[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetInvariantFloatvEXT");
        details.foundArguments.add(new Object[] { id, value, data, data_offset });
    }

    @Override
    public void glGetInvariantIntegervEXT(int id, int value, IntBuffer data)
    {
        CallDetails details = getMethodDetails("glGetInvariantIntegervEXT");
        details.foundArguments.add(new Object[] { id, value, data });
    }

    @Override
    public void glGetInvariantIntegervEXT(int id, int value, int[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetInvariantIntegervEXT");
        details.foundArguments.add(new Object[] { id, value, data, data_offset });
    }

    @Override
    public void glGetLightiv(int light, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetLightiv");
        details.foundArguments.add(new Object[] { light, pname, params });
    }

    @Override
    public void glGetLightiv(int light, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetLightiv");
        details.foundArguments.add(new Object[] { light, pname, params, params_offset });
    }

    @Override
    public void glGetLocalConstantBooleanvEXT(int id, int value, ByteBuffer data)
    {
        CallDetails details = getMethodDetails("glGetLocalConstantBooleanvEXT");
        details.foundArguments.add(new Object[] { id, value, data });
    }

    @Override
    public void glGetLocalConstantBooleanvEXT(int id, int value, byte[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetLocalConstantBooleanvEXT");
        details.foundArguments.add(new Object[] { id, value, data, data_offset });
    }

    @Override
    public void glGetLocalConstantFloatvEXT(int id, int value, FloatBuffer data)
    {
        CallDetails details = getMethodDetails("glGetLocalConstantFloatvEXT");
        details.foundArguments.add(new Object[] { id, value, data });
    }

    @Override
    public void glGetLocalConstantFloatvEXT(int id, int value, float[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetLocalConstantFloatvEXT");
        details.foundArguments.add(new Object[] { id, value, data, data_offset });
    }

    @Override
    public void glGetLocalConstantIntegervEXT(int id, int value, IntBuffer data)
    {
        CallDetails details = getMethodDetails("glGetLocalConstantIntegervEXT");
        details.foundArguments.add(new Object[] { id, value, data });
    }

    @Override
    public void glGetLocalConstantIntegervEXT(int id, int value, int[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetLocalConstantIntegervEXT");
        details.foundArguments.add(new Object[] { id, value, data, data_offset });
    }

    @Override
    public void glGetMapAttribParameterfvNV(int target, int index, int pname, FloatBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMapAttribParameterfvNV(int target, int index, int pname, float[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMapAttribParameterivNV(int target, int index, int pname, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMapAttribParameterivNV(int target, int index, int pname, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMapControlPointsNV(int target, int index, int type, int ustride, int vstride, boolean packed, Buffer points)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMapParameterfvNV(int target, int pname, FloatBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMapParameterfvNV(int target, int pname, float[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMapParameterivNV(int target, int pname, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMapParameterivNV(int target, int pname, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMapdv(int target, int query, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glGetMapdv");
        details.foundArguments.add(new Object[] { target, query, v });
    }

    @Override
    public void glGetMapdv(int target, int query, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glGetMapdv");
        details.foundArguments.add(new Object[] { target, query, v, v_offset });
    }

    @Override
    public void glGetMapfv(int target, int query, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glGetMapfv");
        details.foundArguments.add(new Object[] { target, query, v });
    }

    @Override
    public void glGetMapfv(int target, int query, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glGetMapfv");
        details.foundArguments.add(new Object[] { target, query, v, v_offset });
    }

    @Override
    public void glGetMapiv(int target, int query, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glGetMapiv");
        details.foundArguments.add(new Object[] { target, query, v });
    }

    @Override
    public void glGetMapiv(int target, int query, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glGetMapiv");
        details.foundArguments.add(new Object[] { target, query, v, v_offset });
    }

    @Override
    public void glGetMaterialiv(int face, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMaterialiv");
        details.foundArguments.add(new Object[] { face, pname, params });
    }

    @Override
    public void glGetMaterialiv(int face, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMaterialiv");
        details.foundArguments.add(new Object[] { face, pname, params, params_offset });
    }

    @Override
    public void glGetMinmax(int target, boolean reset, int format, int type, Buffer values)
    {
        CallDetails details = getMethodDetails("glGetMinmax");
        details.foundArguments.add(new Object[] { target, reset, format, type, values  });
    }

    @Override
    public void glGetMinmax(int target, boolean reset, int format, int type, long values_buffer_offset)
    {
        CallDetails details = getMethodDetails("glGetMinmax");
        details.foundArguments.add(new Object[] { target, reset, format, type, values_buffer_offset  });
    }

    @Override
    public void glGetMinmaxParameterfv(int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMinmaxParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetMinmaxParameterfv(int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMinmaxParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetMinmaxParameteriv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMinmaxParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetMinmaxParameteriv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMinmaxParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetMultiTexEnvfvEXT(int texunit, int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMultiTexEnvfvEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params });
    }

    @Override
    public void glGetMultiTexEnvfvEXT(int texunit, int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMultiTexEnvfvEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params, params_offset });
    }

    @Override
    public void glGetMultiTexEnvivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMultiTexEnvivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params });
    }

    @Override
    public void glGetMultiTexEnvivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMultiTexEnvivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params, params_offset });
    }

    @Override
    public void glGetMultiTexGendvEXT(int texunit, int coord, int pname, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMultiTexGendvEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, params });
    }

    @Override
    public void glGetMultiTexGendvEXT(int texunit, int coord, int pname, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMultiTexGendvEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, params, params_offset });
    }

    @Override
    public void glGetMultiTexGenfvEXT(int texunit, int coord, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMultiTexGenfvEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, params });
    }

    @Override
    public void glGetMultiTexGenfvEXT(int texunit, int coord, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMultiTexGenfvEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, params, params_offset });
    }

    @Override
    public void glGetMultiTexGenivEXT(int texunit, int coord, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMultiTexGenivEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, params });
    }

    @Override
    public void glGetMultiTexGenivEXT(int texunit, int coord, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMultiTexGenivEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, params, params_offset });
    }

    @Override
    public void glGetMultiTexImageEXT(int texunit, int target, int level, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glGetMultiTexImageEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, format, type, pixels });
    }

    @Override
    public void glGetMultiTexLevelParameterfvEXT(int texunit, int target, int level, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMultiTexLevelParameterfvEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, pname, params });
    }

    @Override
    public void glGetMultiTexLevelParameterfvEXT(int texunit, int target, int level, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMultiTexLevelParameterfvEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, pname, params, params_offset });
    }

    @Override
    public void glGetMultiTexLevelParameterivEXT(int texunit, int target, int level, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMultiTexLevelParameterivEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, pname, params });
    }

    @Override
    public void glGetMultiTexLevelParameterivEXT(int texunit, int target, int level, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMultiTexLevelParameterivEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, pname, params, params_offset });
    }

    @Override
    public void glGetMultiTexParameterIivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMultiTexParameterIivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params });
    }

    @Override
    public void glGetMultiTexParameterIivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMultiTexParameterIivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params, params_offset });
    }

    @Override
    public void glGetMultiTexParameterIuivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMultiTexParameterIuivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params });
    }

    @Override
    public void glGetMultiTexParameterIuivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMultiTexParameterIuivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params, params_offset });
    }

    @Override
    public void glGetMultiTexParameterfvEXT(int texunit, int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMultiTexParameterfvEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params });
    }

    @Override
    public void glGetMultiTexParameterfvEXT(int texunit, int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMultiTexParameterfvEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params, params_offset });
    }

    @Override
    public void glGetMultiTexParameterivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMultiTexParameterivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params });
    }

    @Override
    public void glGetMultiTexParameterivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMultiTexParameterivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params, params_offset });
    }

    @Override
    public void glGetMultisamplefvNV(int pname, int index, FloatBuffer val)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetMultisamplefvNV(int pname, int index, float[] val, int val_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetNamedBufferParameterivEXT(int buffer, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetNamedBufferParameterivEXT");
        details.foundArguments.add(new Object[] { buffer, pname, params });
    }

    @Override
    public void glGetNamedBufferParameterivEXT(int buffer, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetNamedBufferParameterivEXT");
        details.foundArguments.add(new Object[] { buffer, pname, params, params_offset });
    }

    @Override
    public void glGetNamedBufferSubDataEXT(int buffer, long offset, long size, Buffer data)
    {
        CallDetails details = getMethodDetails("glGetNamedBufferSubDataEXT");
        details.foundArguments.add(new Object[] { buffer, offset, size, data });
    }

    @Override
    public void glGetNamedFramebufferAttachmentParameterivEXT(int framebuffer, int attachment, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetNamedFramebufferAttachmentParameterivEXT");
        details.foundArguments.add(new Object[] { framebuffer, attachment, pname, params });
    }

    @Override
    public void glGetNamedFramebufferAttachmentParameterivEXT(int framebuffer, int attachment, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetNamedFramebufferAttachmentParameterivEXT");
        details.foundArguments.add(new Object[] { framebuffer, attachment, pname, params, params_offset });
    }

    @Override
    public void glGetNamedFramebufferParameteriv(int framebuffer, int pname, IntBuffer param)
    {
        CallDetails details = getMethodDetails("glGetNamedFramebufferAttachmentParameteriv");
        details.foundArguments.add(new Object[] { framebuffer, pname, param });
    }

    @Override
    public void glGetNamedFramebufferParameteriv(int framebuffer, int pname, int[] param, int param_offset)
    {
        CallDetails details = getMethodDetails("glGetNamedFramebufferAttachmentParameteriv");
        details.foundArguments.add(new Object[] { framebuffer, pname, param, param_offset });
    }

    @Override
    public void glGetNamedProgramLocalParameterIivEXT(int program, int target, int index, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetNamedProgramLocalParameterIivEXT");
        details.foundArguments.add(new Object[] { program, target, index, params });
    }

    @Override
    public void glGetNamedProgramLocalParameterIivEXT(int program, int target, int index, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetNamedProgramLocalParameterIivEXT");
        details.foundArguments.add(new Object[] { program, target, index, params, params_offset });
    }

    @Override
    public void glGetNamedProgramLocalParameterIuivEXT(int program, int target, int index, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetNamedProgramLocalParameterIuivEXT");
        details.foundArguments.add(new Object[] { program, target, index, params });
    }

    @Override
    public void glGetNamedProgramLocalParameterIuivEXT(int program, int target, int index, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetNamedProgramLocalParameterIuivEXT");
        details.foundArguments.add(new Object[] { program, target, index, params, params_offset });
    }

    @Override
    public void glGetNamedProgramLocalParameterdvEXT(int program, int target, int index, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glGetNamedProgramLocalParameterdvEXT");
        details.foundArguments.add(new Object[] { program, target, index, params });
    }

    @Override
    public void glGetNamedProgramLocalParameterdvEXT(int program, int target, int index, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetNamedProgramLocalParameterdvEXT");
        details.foundArguments.add(new Object[] { program, target, index, params, params_offset });
    }

    @Override
    public void glGetNamedProgramLocalParameterfvEXT(int program, int target, int index, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { program, target, index, params });
    }

    @Override
    public void glGetNamedProgramLocalParameterfvEXT(int program, int target, int index, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetNamedProgramLocalParameterfvEXT");
        details.foundArguments.add(new Object[] { program, target, index, params, params_offset });
    }

    @Override
    public void glGetNamedProgramStringEXT(int program, int target, int pname, Buffer string)
    {
        CallDetails details = getMethodDetails("glGetNamedProgramStringEXT");
        details.foundArguments.add(new Object[] { program, target, pname, string });
    }

    @Override
    public void glGetNamedProgramivEXT(int program, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetNamedProgramivEXT");
        details.foundArguments.add(new Object[] { program, target, pname, params });
    }

    @Override
    public void glGetNamedProgramivEXT(int program, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetNamedProgramivEXT");
        details.foundArguments.add(new Object[] { program, target, pname, params, params_offset });
    }

    @Override
    public void glGetNamedRenderbufferParameterivEXT(int renderbuffer, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetNamedRenderbufferParameterivEXT");
        details.foundArguments.add(new Object[] { renderbuffer, pname, params });
    }

    @Override
    public void glGetNamedRenderbufferParameterivEXT(int renderbuffer, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetNamedRenderbufferParameterivEXT");
        details.foundArguments.add(new Object[] { renderbuffer, pname, params, params_offset });
    }

    @Override
    public void glGetNextPerfQueryIdINTEL(int queryId, IntBuffer nextQueryId)
    {

    }

    @Override
    public void glGetNextPerfQueryIdINTEL(int queryId, int[] nextQueryId, int nextQueryId_offset)
    {

    }

    @Override
    public void glGetObjectParameterfvARB(long obj, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetObjectParameterfvARB");
        details.foundArguments.add(new Object[] { obj, pname, params });
    }

    @Override
    public void glGetObjectParameterfvARB(long obj, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetObjectParameterfvARB");
        details.foundArguments.add(new Object[] { obj, pname, params, params_offset });
    }

    @Override
    public void glGetObjectParameterivAPPLE(int objectType, int name, int pname, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetObjectParameterivAPPLE(int objectType, int name, int pname, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetObjectParameterivARB(long obj, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetObjectParameterivARB");
        details.foundArguments.add(new Object[] { obj, pname, params });
    }

    @Override
    public void glGetObjectParameterivARB(long obj, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetObjectParameterivARB");
        details.foundArguments.add(new Object[] { obj, pname, params, params_offset });
    }

    @Override
    public void glGetOcclusionQueryivNV(int id, int pname, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetOcclusionQueryivNV(int id, int pname, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetOcclusionQueryuivNV(int id, int pname, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetOcclusionQueryuivNV(int id, int pname, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfCounterInfoINTEL(int queryId, int counterId, int counterNameLength, ByteBuffer counterName, int counterDescLength, ByteBuffer counterDesc, IntBuffer counterOffset, IntBuffer counterDataSize, IntBuffer counterTypeEnum, IntBuffer counterDataTypeEnum, LongBuffer rawCounterMaxValue)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfCounterInfoINTEL(int queryId, int counterId, int counterNameLength, byte[] counterName, int counterName_offset, int counterDescLength, byte[] counterDesc, int counterDesc_offset, int[] counterOffset, int counterOffset_offset, int[] counterDataSize, int counterDataSize_offset, int[] counterTypeEnum, int counterTypeEnum_offset, int[] counterDataTypeEnum, int counterDataTypeEnum_offset, long[] rawCounterMaxValue, int rawCounterMaxValue_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfMonitorCounterDataAMD(int monitor, int pname, int dataSize, IntBuffer data, IntBuffer bytesWritten)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfMonitorCounterDataAMD(int monitor, int pname, int dataSize, int[] data, int data_offset, int[] bytesWritten, int bytesWritten_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfMonitorCounterInfoAMD(int group, int counter, int pname, Buffer data)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfMonitorCounterStringAMD(int group, int counter, int bufSize, IntBuffer length, ByteBuffer counterString)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfMonitorCounterStringAMD(int group, int counter, int bufSize, int[] length, int length_offset, byte[] counterString, int counterString_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfMonitorCountersAMD(int group, IntBuffer numCounters, IntBuffer maxActiveCounters, int counterSize, IntBuffer counters)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfMonitorCountersAMD(int group, int[] numCounters, int numCounters_offset, int[] maxActiveCounters, int maxActiveCounters_offset, int counterSize, int[] counters, int counters_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfMonitorGroupStringAMD(int group, int bufSize, IntBuffer length, ByteBuffer groupString)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfMonitorGroupStringAMD(int group, int bufSize, int[] length, int length_offset, byte[] groupString, int groupString_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfMonitorGroupsAMD(IntBuffer numGroups, int groupsSize, IntBuffer groups)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfMonitorGroupsAMD(int[] numGroups, int numGroups_offset, int groupsSize, int[] groups, int groups_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfQueryDataINTEL(int queryHandle, int flags, int dataSize, Buffer data, IntBuffer bytesWritten)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfQueryDataINTEL(int queryHandle, int flags, int dataSize, Buffer data, int[] bytesWritten, int bytesWritten_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfQueryIdByNameINTEL(ByteBuffer queryName, IntBuffer queryId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfQueryIdByNameINTEL(byte[] queryName, int queryName_offset, int[] queryId, int queryId_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfQueryInfoINTEL(int queryId, int queryNameLength, ByteBuffer queryName, IntBuffer dataSize, IntBuffer noCounters, IntBuffer noInstances, IntBuffer capsMask)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPerfQueryInfoINTEL(int queryId, int queryNameLength, byte[] queryName, int queryName_offset, int[] dataSize, int dataSize_offset, int[] noCounters, int noCounters_offset, int[] noInstances, int noInstances_offset, int[] capsMask, int capsMask_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetPixelMapfv(int map, FloatBuffer values)
    {
        CallDetails details = getMethodDetails("glGetPixelMapfv");
        details.foundArguments.add(new Object[] { map, values });
    }

    @Override
    public void glGetPixelMapfv(int map, float[] values, int values_offset)
    {
        CallDetails details = getMethodDetails("glGetPixelMapfv");
        details.foundArguments.add(new Object[] { map, values, values_offset });
    }

    @Override
    public void glGetPixelMapfv(int map, long values_buffer_offset)
    {
        CallDetails details = getMethodDetails("glGetPixelMapfv");
        details.foundArguments.add(new Object[] { map, values_buffer_offset });
    }

    @Override
    public void glGetPixelMapuiv(int map, IntBuffer values)
    {
        CallDetails details = getMethodDetails("glGetPixelMapuiv");
        details.foundArguments.add(new Object[] { map, values });
    }

    @Override
    public void glGetPixelMapuiv(int map, int[] values, int values_offset)
    {
        CallDetails details = getMethodDetails("glGetPixelMapuiv");
        details.foundArguments.add(new Object[] { map, values, values_offset });
    }

    @Override
    public void glGetPixelMapuiv(int map, long values_buffer_offset)
    {
        CallDetails details = getMethodDetails("glGetPixelMapuiv");
        details.foundArguments.add(new Object[] { map, values_buffer_offset });
    }

    @Override
    public void glGetPixelMapusv(int map, ShortBuffer values)
    {
        CallDetails details = getMethodDetails("glGetPixelMapusv");
        details.foundArguments.add(new Object[] { map, values });
    }

    @Override
    public void glGetPixelMapusv(int map, short[] values, int values_offset)
    {
        CallDetails details = getMethodDetails("glGetPixelMapusv");
        details.foundArguments.add(new Object[] { map, values, values_offset });
    }

    @Override
    public void glGetPixelMapusv(int map, long values_buffer_offset)
    {
        CallDetails details = getMethodDetails("glGetPixelMapusv");
        details.foundArguments.add(new Object[] { map, values_buffer_offset });
    }

    @Override
    public void glGetPixelTransformParameterfvEXT(int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetPixelTransformParameterfvEXT");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetPixelTransformParameterfvEXT(int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetPixelTransformParameterfvEXT");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetPixelTransformParameterivEXT(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetPixelTransformParameterivEXT");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetPixelTransformParameterivEXT(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetPixelTransformParameterivEXT");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetPointeri_vEXT(int pname, int index, PointerBuffer params)
    {
        CallDetails details = getMethodDetails("glGetPointeri_vEXT");
        details.foundArguments.add(new Object[] { pname, index, params });
    }

    @Override
    public void glGetPolygonStipple(ByteBuffer mask)
    {
        CallDetails details = getMethodDetails("glGetPolygonStipple");
        details.foundArguments.add(new Object[] { mask });
    }

    @Override
    public void glGetPolygonStipple(byte[] mask, int mask_offset)
    {
        CallDetails details = getMethodDetails("glGetPolygonStipple");
        details.foundArguments.add(new Object[] { mask, mask_offset });
    }

    @Override
    public void glGetPolygonStipple(long mask_buffer_offset)
    {
        CallDetails details = getMethodDetails("glGetPolygonStipple");
        details.foundArguments.add(new Object[] { mask_buffer_offset });
    }

    @Override
    public void glGetProgramEnvParameterIivNV(int target, int index, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetProgramEnvParameterIivNV(int target, int index, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetProgramEnvParameterIuivNV(int target, int index, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetProgramEnvParameterIuivNV(int target, int index, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetProgramEnvParameterdvARB(int target, int index, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glGetProgramEnvParameterdvARB");
        details.foundArguments.add(new Object[] { target, index, params });
    }

    @Override
    public void glGetProgramEnvParameterdvARB(int target, int index, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetProgramEnvParameterdvARB");
        details.foundArguments.add(new Object[] { target, index, params, params_offset });
    }

    @Override
    public void glGetProgramEnvParameterfvARB(int target, int index, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetProgramEnvParameterfvARB");
        details.foundArguments.add(new Object[] { target, index, params });
    }

    @Override
    public void glGetProgramEnvParameterfvARB(int target, int index, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetProgramEnvParameterfvARB");
        details.foundArguments.add(new Object[] { target, index, params, params_offset });
    }

    @Override
    public void glGetProgramLocalParameterIivNV(int target, int index, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetProgramLocalParameterIivNV(int target, int index, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetProgramLocalParameterIuivNV(int target, int index, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetProgramLocalParameterIuivNV(int target, int index, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetProgramLocalParameterdvARB(int target, int index, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glGetProgramLocalParameterdvARB");
        details.foundArguments.add(new Object[] { target, index, params });
    }

    @Override
    public void glGetProgramLocalParameterdvARB(int target, int index, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetProgramLocalParameterdvARB");
        details.foundArguments.add(new Object[] { target, index, params, params_offset });
    }

    @Override
    public void glGetProgramLocalParameterfvARB(int target, int index, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetProgramLocalParameterdvARB");
        details.foundArguments.add(new Object[] { target, index, params });
    }

    @Override
    public void glGetProgramLocalParameterfvARB(int target, int index, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetProgramLocalParameterdvARB");
        details.foundArguments.add(new Object[] { target, index, params, params_offset });
    }

    @Override
    public void glGetProgramStringARB(int target, int pname, Buffer string)
    {
        CallDetails details = getMethodDetails("glGetProgramStringARB");
        details.foundArguments.add(new Object[] { target, pname, string });
    }

    @Override
    public void glGetProgramSubroutineParameteruivNV(int target, int index, IntBuffer param)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetProgramSubroutineParameteruivNV(int target, int index, int[] param, int param_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetProgramivARB(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetProgramivARB");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetProgramivARB(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetProgramivARB");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetQueryObjecti64vEXT(int id, int pname, LongBuffer params)
    {
        CallDetails details = getMethodDetails("glGetQueryObjecti64vEXT");
        details.foundArguments.add(new Object[] { id, pname, params });
    }

    @Override
    public void glGetQueryObjecti64vEXT(int id, int pname, long[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetQueryObjecti64vEXT");
        details.foundArguments.add(new Object[] { id, pname, params, params_offset });
    }

    @Override
    public void glGetQueryObjectui64vEXT(int id, int pname, LongBuffer params)
    {
        CallDetails details = getMethodDetails("glGetQueryObjectui64vEXT");
        details.foundArguments.add(new Object[] { id, pname, params });
    }

    @Override
    public void glGetQueryObjectui64vEXT(int id, int pname, long[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetQueryObjectui64vEXT");
        details.foundArguments.add(new Object[] { id, pname, params, params_offset });
    }

    @Override
    public void glGetSeparableFilter(int target, int format, int type, Buffer row, Buffer column, Buffer span)
    {
        CallDetails details = getMethodDetails("glGetSeparableFilter");
        details.foundArguments.add(new Object[] { target, format, type, row, column, span });
    }

    @Override
    public void glGetSeparableFilter(int target, int format, int type, long row_buffer_offset, long column_buffer_offset, long span_buffer_offset)
    {
        CallDetails details = getMethodDetails("glGetSeparableFilter");
        details.foundArguments.add(new Object[] { target, format, type, row_buffer_offset, column_buffer_offset, span_buffer_offset });
    }

    @Override
    public void glGetShaderSourceARB(long obj, int maxLength, IntBuffer length, ByteBuffer source)
    {
        CallDetails details = getMethodDetails("glGetShaderSourceARB");
        details.foundArguments.add(new Object[] { obj, maxLength, length, source });
    }

    @Override
    public void glGetShaderSourceARB(long obj, int maxLength, int[] length, int length_offset, byte[] source, int source_offset)
    {
        CallDetails details = getMethodDetails("glGetShaderSourceARB");
        details.foundArguments.add(new Object[] { obj, maxLength, length, length_offset, source, source_offset });
    }

    @Override
    public String glGetStringi(int name, int index)
    {
        return null;
    }

    @Override
    public void glGetTexGendv(int coord, int pname, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTexGendv");
        details.foundArguments.add(new Object[] { coord, pname, params });
    }

    @Override
    public void glGetTexGendv(int coord, int pname, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTexGendv");
        details.foundArguments.add(new Object[] { coord, pname, params, params_offset });
    }

    @Override
    public void glGetTextureImageEXT(int texture, int target, int level, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glGetTextureImageEXT");
        details.foundArguments.add(new Object[] { texture, target, level, format, type, pixels });
    }

    @Override
    public void glGetTextureLevelParameterfvEXT(int texture, int target, int level, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTextureLevelParameterfvEXT");
        details.foundArguments.add(new Object[] { texture, target, level, pname, params });
    }

    @Override
    public void glGetTextureLevelParameterfvEXT(int texture, int target, int level, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTextureLevelParameterfvEXT");
        details.foundArguments.add(new Object[] { texture, target, level, pname, params, params_offset });
    }

    @Override
    public void glGetTextureLevelParameterivEXT(int texture, int target, int level, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTextureLevelParameterivEXT");
        details.foundArguments.add(new Object[] { texture, target, level, pname, params });
    }

    @Override
    public void glGetTextureLevelParameterivEXT(int texture, int target, int level, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTextureLevelParameterivEXT");
        details.foundArguments.add(new Object[] { texture, target, level, pname, params, params_offset });
    }

    @Override
    public void glGetTextureParameterIivEXT(int texture, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTextureLevelParameterivEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params });
    }

    @Override
    public void glGetTextureParameterIivEXT(int texture, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { texture, target, pname, params, params_offset });
    }

    @Override
    public void glGetTextureParameterIuivEXT(int texture, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTextureParameterIuivEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params });
    }

    @Override
    public void glGetTextureParameterIuivEXT(int texture, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTextureParameterIuivEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params, params_offset });
    }

    @Override
    public void glGetTextureParameterfvEXT(int texture, int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTextureParameterfvEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params });
    }

    @Override
    public void glGetTextureParameterfvEXT(int texture, int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTextureParameterfvEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params, params_offset });
    }

    @Override
    public void glGetTextureParameterivEXT(int texture, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTextureParameterivEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params });
    }

    @Override
    public void glGetTextureParameterivEXT(int texture, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTextureParameterivEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params, params_offset });
    }

    @Override
    public void glGetTransformFeedbackVarying(int program, int index, int bufSize, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name)
    {
        CallDetails details = getMethodDetails("glGetTransformFeedbackVarying");
        details.foundArguments.add(new Object[] { program, index, bufSize, length, size, type, name });
    }

    @Override
    public void glGetTransformFeedbackVarying(int program, int index, int bufSize, int[] length, int length_offset, int[] size, int size_offset, int[] type, int type_offset, byte[] name, int name_offset)
    {
        CallDetails details = getMethodDetails("glGetTransformFeedbackVarying");
        details.foundArguments.add(new Object[] { program, index, bufSize, length, length_offset, size, size_offset, type, type_offset, name, name_offset });
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
    public int glGetUniformLocationARB(long programObj, String name)
    {
        return 0;
    }

    @Override
    public void glGetUniformIndices(int program, int uniformCount, String[] uniformNames, IntBuffer uniformIndices)
    {
        CallDetails details = getMethodDetails("glGetUniformIndices");
        details.foundArguments.add(new Object[] { program, uniformCount, uniformNames, uniformIndices });
    }

    @Override
    public void glGetUniformIndices(int program, int uniformCount, String[] uniformNames, int[] uniformIndices, int uniformIndices_offset)
    {
        CallDetails details = getMethodDetails("glGetUniformIndices");
        details.foundArguments.add(new Object[] { program, uniformCount, uniformNames, uniformIndices, uniformIndices_offset });
    }

    @Override
    public long glGetUniformOffsetEXT(int program, int location)
    {
        return 0;
    }

    @Override
    public void glGetUniformi64vNV(int program, int location, LongBuffer params)
    {
        CallDetails details = getMethodDetails("glGetUniformi64vNV");
        details.foundArguments.add(new Object[] { program, location, params });
    }

    @Override
    public void glGetUniformi64vNV(int program, int location, long[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetUniformi64vNV");
        details.foundArguments.add(new Object[] { program, location, params, params_offset });
    }

    @Override
    public void glGetUniformfvARB(long programObj, int location, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetUniformfvARB");
        details.foundArguments.add(new Object[] { programObj, location, params });
    }

    @Override
    public void glGetUniformfvARB(long programObj, int location, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetUniformfvARB");
        details.foundArguments.add(new Object[] { programObj, location, params, params_offset });
    }

    @Override
    public void glGetUniformivARB(long programObj, int location, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetUniformivARB");
        details.foundArguments.add(new Object[] { programObj, location, params });
    }

    @Override
    public void glGetUniformivARB(long programObj, int location, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetUniformivARB");
        details.foundArguments.add(new Object[] { programObj, location, params, params_offset });
    }

    @Override
    public void glGetUniformuiv(int program, int location, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetUniformuiv");
        details.foundArguments.add(new Object[] { program, location, params });
    }

    @Override
    public void glGetUniformuiv(int program, int location, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetUniformuiv");
        details.foundArguments.add(new Object[] { program, location, params });
    }

    @Override
    public void glGetVariantBooleanvEXT(int id, int value, ByteBuffer data)
    {
        CallDetails details = getMethodDetails("glGetVariantBooleanvEXT");
        details.foundArguments.add(new Object[] { id, value, data });
    }

    @Override
    public void glGetVariantBooleanvEXT(int id, int value, byte[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetVariantBooleanvEXT");
        details.foundArguments.add(new Object[] { id, value, data, data_offset });
    }

    @Override
    public void glGetVariantFloatvEXT(int id, int value, FloatBuffer data)
    {
        CallDetails details = getMethodDetails("glGetVariantFloatvEXT");
        details.foundArguments.add(new Object[] { id, value, data });
    }

    @Override
    public void glGetVariantFloatvEXT(int id, int value, float[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetVariantFloatvEXT");
        details.foundArguments.add(new Object[] { id, value, data, data_offset });
    }

    @Override
    public void glGetVariantIntegervEXT(int id, int value, IntBuffer data)
    {
        CallDetails details = getMethodDetails("glGetVariantIntegervEXT");
        details.foundArguments.add(new Object[] { id, value, data });
    }

    @Override
    public void glGetVariantIntegervEXT(int id, int value, int[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { id, value, data, data_offset });
    }

    @Override
    public void glGetVertexArrayIntegeri_vEXT(int vaobj, int index, int pname, IntBuffer param)
    {
        CallDetails details = getMethodDetails("glGetVertexArrayIntegeri_vEXT");
        details.foundArguments.add(new Object[] { vaobj, index, pname, param });
    }

    @Override
    public void glGetVertexArrayIntegeri_vEXT(int vaobj, int index, int pname, int[] param, int param_offset)
    {
        CallDetails details = getMethodDetails("glGetVertexArrayIntegeri_vEXT");
        details.foundArguments.add(new Object[] { vaobj, index, pname, param, param_offset });
    }

    @Override
    public void glGetVertexArrayIntegervEXT(int vaobj, int pname, IntBuffer param)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { vaobj, pname, param });
    }

    @Override
    public void glGetVertexArrayIntegervEXT(int vaobj, int pname, int[] param, int param_offset)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { vaobj, pname, param, param_offset });
    }

    @Override
    public void glGetVertexArrayPointeri_vEXT(int vaobj, int index, int pname, PointerBuffer param)
    {
        CallDetails details = getMethodDetails("glGetVertexArrayPointeri_vEXT");
        details.foundArguments.add(new Object[] { vaobj, index, pname, param });
    }

    @Override
    public void glGetVertexArrayPointervEXT(int vaobj, int pname, PointerBuffer param)
    {
        CallDetails details = getMethodDetails("glGetVertexArrayPointervEXT");
        details.foundArguments.add(new Object[] { vaobj, pname, param });
    }

    @Override
    public void glGetVertexAttribIiv(int index, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribIiv");
        details.foundArguments.add(new Object[] { index, pname, params });
    }

    @Override
    public void glGetVertexAttribIiv(int index, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribIiv");
        details.foundArguments.add(new Object[] { index, pname, params, params_offset });
    }

    @Override
    public void glGetVertexAttribIivEXT(int index, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribIivEXT");
        details.foundArguments.add(new Object[] { index, pname, params });
    }

    @Override
    public void glGetVertexAttribIivEXT(int index, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribIivEXT");
        details.foundArguments.add(new Object[] { index, pname, params, params_offset });
    }

    @Override
    public void glGetVertexAttribIuiv(int index, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribIuiv");
        details.foundArguments.add(new Object[] { index, pname, params });
    }

    @Override
    public void glGetVertexAttribIuiv(int index, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribIuiv");
        details.foundArguments.add(new Object[] { index, pname, params, params_offset });
    }

    @Override
    public void glInvalidateFramebuffer(int target, int numAttachments, IntBuffer attachments)
    {

    }

    @Override
    public void glInvalidateFramebuffer(int target, int numAttachments, int[] attachments, int attachments_offset)
    {

    }

    @Override
    public void glInvalidateSubFramebuffer(int target, int numAttachments, IntBuffer attachments, int x, int y, int width, int height)
    {

    }

    @Override
    public void glInvalidateSubFramebuffer(int target, int numAttachments, int[] attachments, int attachments_offset, int x, int y, int width, int height)
    {

    }

    @Override
    public boolean glIsTransformFeedback(int id)
    {
        return false;
    }

    @Override
    public void glGetVertexAttribIuivEXT(int index, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribIuivEXT");
        details.foundArguments.add(new Object[] { index, pname, params });
    }

    @Override
    public void glGetVertexAttribIuivEXT(int index, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribIuivEXT");
        details.foundArguments.add(new Object[] { index, pname, params, params_offset });
    }

    @Override
    public void glGetVertexAttribLi64vNV(int index, int pname, LongBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVertexAttribLi64vNV(int index, int pname, long[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVertexAttribLui64vNV(int index, int pname, LongBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVertexAttribLui64vNV(int index, int pname, long[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVertexAttribdvARB(int index, int pname, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribdvARB");
        details.foundArguments.add(new Object[] { index, pname, params });
    }

    @Override
    public void glGetVertexAttribdvARB(int index, int pname, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribdvARB");
        details.foundArguments.add(new Object[] { index, pname, params, params_offset });
    }

    @Override
    public void glGetVertexAttribfvARB(int index, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { index, pname, params });
    }

    @Override
    public void glGetVertexAttribfvARB(int index, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { index, pname, params, params_offset });
    }

    @Override
    public void glGetVertexAttribivARB(int index, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { index, pname, params });
    }

    @Override
    public void glGetVertexAttribivARB(int index, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { index, pname, params, params_offset });
    }

    @Override
    public void glBlendBarrier()
    {

    }

    @Override
    public void glGetVideoCaptureStreamdvNV(int video_capture_slot, int stream, int pname, DoubleBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVideoCaptureStreamdvNV(int video_capture_slot, int stream, int pname, double[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVideoCaptureStreamfvNV(int video_capture_slot, int stream, int pname, FloatBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVideoCaptureStreamfvNV(int video_capture_slot, int stream, int pname, float[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVideoCaptureStreamivNV(int video_capture_slot, int stream, int pname, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVideoCaptureStreamivNV(int video_capture_slot, int stream, int pname, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVideoCaptureivNV(int video_capture_slot, int pname, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVideoCaptureivNV(int video_capture_slot, int pname, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetnColorTable(int target, int format, int type, int bufSize, Buffer table)
    {
        CallDetails details = getMethodDetails("glGetnColorTable");
        details.foundArguments.add(new Object[] { target, format, type, bufSize, table });
    }

    @Override
    public void glGetnConvolutionFilter(int target, int format, int type, int bufSize, Buffer image)
    {
        CallDetails details = getMethodDetails("glGetnConvolutionFilter");
        details.foundArguments.add(new Object[] { target, format, type, bufSize, image });
    }

    @Override
    public void glGetnHistogram(int target, boolean reset, int format, int type, int bufSize, Buffer values)
    {
        CallDetails details = getMethodDetails("glGetnHistogram");
        details.foundArguments.add(new Object[] { target, reset, format, type, bufSize, values });
    }

    @Override
    public void glGetnMapdv(int target, int query, int bufSize, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glGetnMapdv");
        details.foundArguments.add(new Object[] { target, query, bufSize, v });
    }

    @Override
    public void glGetnMapdv(int target, int query, int bufSize, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glGetnMapdv");
        details.foundArguments.add(new Object[] { target, query, bufSize, v, v_offset });
    }

    @Override
    public void glGetnMapfv(int target, int query, int bufSize, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glGetnMapfv");
        details.foundArguments.add(new Object[] { target, query, bufSize, v });
    }

    @Override
    public void glGetnMapfv(int target, int query, int bufSize, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glGetnMapfv");
        details.foundArguments.add(new Object[] { target, query, bufSize, v, v_offset });
    }

    @Override
    public void glGetnMapiv(int target, int query, int bufSize, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glGetnMapiv");
        details.foundArguments.add(new Object[] { target, query, bufSize, v });
    }

    @Override
    public void glGetnMapiv(int target, int query, int bufSize, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glGetnMapiv");
        details.foundArguments.add(new Object[] { target, query, bufSize, v, v_offset });
    }

    @Override
    public void glGetnMinmax(int target, boolean reset, int format, int type, int bufSize, Buffer values)
    {
        CallDetails details = getMethodDetails("glGetnMinmax");
        details.foundArguments.add(new Object[] { target, reset, format, type, bufSize, values });
    }

    @Override
    public void glGetnPixelMapfv(int map, int bufSize, FloatBuffer values)
    {
        CallDetails details = getMethodDetails("glGetnPixelMapfv");
        details.foundArguments.add(new Object[] { map, bufSize, values });
    }

    @Override
    public void glGetnPixelMapfv(int map, int bufSize, float[] values, int values_offset)
    {
        CallDetails details = getMethodDetails("glGetnPixelMapfv");
        details.foundArguments.add(new Object[] { map, bufSize, values, values_offset });
    }

    @Override
    public void glGetnPixelMapuiv(int map, int bufSize, IntBuffer values)
    {
        CallDetails details = getMethodDetails("glGetnPixelMapuiv");
        details.foundArguments.add(new Object[] { map, bufSize, values });
    }

    @Override
    public void glGetnPixelMapuiv(int map, int bufSize, int[] values, int values_offset)
    {
        CallDetails details = getMethodDetails("glGetnPixelMapuiv");
        details.foundArguments.add(new Object[] { map, bufSize, values, values_offset });
    }

    @Override
    public void glGetnPixelMapusv(int map, int bufSize, ShortBuffer values)
    {
        CallDetails details = getMethodDetails("glGetnPixelMapusv");
        details.foundArguments.add(new Object[] { map, bufSize, values });
    }

    @Override
    public void glGetnPixelMapusv(int map, int bufSize, short[] values, int values_offset)
    {
        CallDetails details = getMethodDetails("glGetnPixelMapusv");
        details.foundArguments.add(new Object[] { map, bufSize, values, values_offset });
    }

    @Override
    public void glGetnPolygonStipple(int bufSize, ByteBuffer pattern)
    {
        CallDetails details = getMethodDetails("glGetnPolygonStipple");
        details.foundArguments.add(new Object[] { bufSize, pattern });
    }

    @Override
    public void glGetnPolygonStipple(int bufSize, byte[] pattern, int pattern_offset)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { bufSize, pattern, pattern_offset });
    }

    @Override
    public void glGetnSeparableFilter(int target, int format, int type, int rowBufSize, Buffer row, int columnBufSize, Buffer column, Buffer span)
    {
        CallDetails details = getMethodDetails("glGetnSeparableFilter");
        details.foundArguments.add(new Object[] { target, format, type, rowBufSize, row, columnBufSize, column, span });
    }

    @Override
    public void glHintPGI(int target, int mode)
    {
        CallDetails details = getMethodDetails("glHintPGI");
        details.foundArguments.add(new Object[] { target, mode });
    }

    @Override
    public void glHistogram(int target, int width, int internalformat, boolean sink)
    {
        CallDetails details = getMethodDetails("glHistogram");
        details.foundArguments.add(new Object[] { target, width, internalformat, sink });
    }

    @Override
    public void glIndexFuncEXT(int func, float ref)
    {
        CallDetails details = getMethodDetails("glIndexFuncEXT");
        details.foundArguments.add(new Object[] { func, ref });
    }

    @Override
    public void glIndexMask(int mask)
    {
        CallDetails details = getMethodDetails("glIndexMask");
        details.foundArguments.add(new Object[] { mask });
    }

    @Override
    public void glIndexMaterialEXT(int face, int mode)
    {
        CallDetails details = getMethodDetails("glIndexMaterialEXT");
        details.foundArguments.add(new Object[] { face, mode });
    }

    @Override
    public void glIndexPointer(int type, int stride, Buffer ptr)
    {
        CallDetails details = getMethodDetails("glIndexPointer");
        details.foundArguments.add(new Object[] { type, stride, ptr });
    }

    @Override
    public void glIndexd(double c)
    {
        CallDetails details = getMethodDetails("glIndexd");
        details.foundArguments.add(new Object[] { c });
    }

    @Override
    public void glIndexdv(DoubleBuffer c)
    {
        CallDetails details = getMethodDetails("glIndexdv");
        details.foundArguments.add(new Object[] { c });
    }

    @Override
    public void glIndexdv(double[] c, int c_offset)
    {
        CallDetails details = getMethodDetails("glIndexdv");
        details.foundArguments.add(new Object[] { c, c_offset });
    }

    @Override
    public void glIndexf(float c)
    {
        CallDetails details = getMethodDetails("glIndexf");
        details.foundArguments.add(new Object[] { c });
    }

    @Override
    public void glIndexfv(FloatBuffer c)
    {
        CallDetails details = getMethodDetails("glIndexfv");
        details.foundArguments.add(new Object[] { c });
    }

    @Override
    public void glIndexfv(float[] c, int c_offset)
    {
        CallDetails details = getMethodDetails("glIndexfv");
        details.foundArguments.add(new Object[] { c, c_offset });
    }

    @Override
    public void glIndexi(int c)
    {
        CallDetails details = getMethodDetails("glIndexi");
        details.foundArguments.add(new Object[] { c });
    }

    @Override
    public void glIndexiv(IntBuffer c)
    {
        CallDetails details = getMethodDetails("glIndexiv");
        details.foundArguments.add(new Object[] { c });
    }

    @Override
    public void glIndexiv(int[] c, int c_offset)
    {
        CallDetails details = getMethodDetails("glIndexiv");
        details.foundArguments.add(new Object[] { c, c_offset });
    }

    @Override
    public void glIndexs(short c)
    {
        CallDetails details = getMethodDetails("glIndexs");
        details.foundArguments.add(new Object[] { c });
    }

    @Override
    public void glIndexsv(ShortBuffer c)
    {
        CallDetails details = getMethodDetails("glIndexsv");
        details.foundArguments.add(new Object[] { c });
    }

    @Override
    public void glIndexsv(short[] c, int c_offset)
    {
        CallDetails details = getMethodDetails("glIndexsv");
        details.foundArguments.add(new Object[] { c, c_offset });
    }

    @Override
    public void glIndexub(byte c)
    {
        CallDetails details = getMethodDetails("glIndexub");
        details.foundArguments.add(new Object[] { c });
    }

    @Override
    public void glIndexubv(ByteBuffer c)
    {
        CallDetails details = getMethodDetails("glIndexubv");
        details.foundArguments.add(new Object[] { c });
    }

    @Override
    public void glIndexubv(byte[] c, int c_offset)
    {
        CallDetails details = getMethodDetails("glIndexubv");
        details.foundArguments.add(new Object[] { c, c_offset });
    }

    @Override
    public void glInitNames()
    {
        CallDetails details = getMethodDetails("glInitNames");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glInsertComponentEXT(int res, int src, int num)
    {
        CallDetails details = getMethodDetails("glInsertComponentEXT");
        details.foundArguments.add(new Object[] { res, src, num });
    }

    @Override
    public void glInterleavedArrays(int format, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glInterleavedArrays");
        details.foundArguments.add(new Object[] { format, stride, pointer });
    }

    @Override
    public void glInterleavedArrays(int format, int stride, long pointer_buffer_offset)
    {
        CallDetails details = getMethodDetails("glInterleavedArrays");
        details.foundArguments.add(new Object[] { format, stride, pointer_buffer_offset });
    }

    @Override
    public boolean glIsEnabledIndexed(int target, int index)
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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean glIsOcclusionQueryNV(int id)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean glIsProgramARB(int program)
    {
        return false;
    }

    @Override
    public void glUniform1i64ARB(int location, long x)
    {

    }

    @Override
    public void glUniform2i64ARB(int location, long x, long y)
    {

    }

    @Override
    public void glUniform3i64ARB(int location, long x, long y, long z)
    {

    }

    @Override
    public void glUniform4i64ARB(int location, long x, long y, long z, long w)
    {

    }

    @Override
    public void glUniform1i64vARB(int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glUniform1i64vARB(int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glUniform2i64vARB(int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glUniform2i64vARB(int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glUniform3i64vARB(int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glUniform3i64vARB(int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glUniform4i64vARB(int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glUniform4i64vARB(int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glUniform1ui64ARB(int location, long x)
    {

    }

    @Override
    public void glUniform2ui64ARB(int location, long x, long y)
    {

    }

    @Override
    public void glUniform3ui64ARB(int location, long x, long y, long z)
    {

    }

    @Override
    public void glUniform4ui64ARB(int location, long x, long y, long z, long w)
    {

    }

    @Override
    public void glUniform1ui64vARB(int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glUniform1ui64vARB(int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glUniform2ui64vARB(int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glUniform2ui64vARB(int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glUniform3ui64vARB(int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glUniform3ui64vARB(int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glUniform4ui64vARB(int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glUniform4ui64vARB(int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glGetUniformi64vARB(int program, int location, LongBuffer params)
    {

    }

    @Override
    public void glGetUniformi64vARB(int program, int location, long[] params, int params_offset)
    {

    }

    @Override
    public void glGetUniformui64vARB(int program, int location, LongBuffer params)
    {

    }

    @Override
    public void glGetUniformui64vARB(int program, int location, long[] params, int params_offset)
    {

    }

    @Override
    public void glGetnUniformi64vARB(int program, int location, int bufSize, LongBuffer params)
    {

    }

    @Override
    public void glGetnUniformi64vARB(int program, int location, int bufSize, long[] params, int params_offset)
    {

    }

    @Override
    public void glGetnUniformui64vARB(int program, int location, int bufSize, LongBuffer params)
    {

    }

    @Override
    public void glGetnUniformui64vARB(int program, int location, int bufSize, long[] params, int params_offset)
    {

    }

    @Override
    public void glProgramUniform1i64ARB(int program, int location, long x)
    {

    }

    @Override
    public void glProgramUniform2i64ARB(int program, int location, long x, long y)
    {

    }

    @Override
    public void glProgramUniform3i64ARB(int program, int location, long x, long y, long z)
    {

    }

    @Override
    public void glProgramUniform4i64ARB(int program, int location, long x, long y, long z, long w)
    {

    }

    @Override
    public void glProgramUniform1i64vARB(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform1i64vARB(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform2i64vARB(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform2i64vARB(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform3i64vARB(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform3i64vARB(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform4i64vARB(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform4i64vARB(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform1ui64ARB(int program, int location, long x)
    {

    }

    @Override
    public void glProgramUniform2ui64ARB(int program, int location, long x, long y)
    {

    }

    @Override
    public void glProgramUniform3ui64ARB(int program, int location, long x, long y, long z)
    {

    }

    @Override
    public void glProgramUniform4ui64ARB(int program, int location, long x, long y, long z, long w)
    {

    }

    @Override
    public void glProgramUniform1ui64vARB(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform1ui64vARB(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform2ui64vARB(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform2ui64vARB(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform3ui64vARB(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform3ui64vARB(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform4ui64vARB(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform4ui64vARB(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public boolean glIsTransformFeedbackNV(int id)
    {
        throw new UnsupportedOperationException();
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
    public void glMemoryBarrier(int barriers)
    {

    }

    @Override
    public void glPauseTransformFeedback()
    {

    }

    @Override
    public boolean glIsVertexAttribEnabledAPPLE(int index, int pname)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glLightModeli(int pname, int param)
    {
        CallDetails details = getMethodDetails("glLightModeli");
        details.foundArguments.add(new Object[] { pname, param });
    }

    @Override
    public void glLightModeliv(int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glLightModeliv");
        details.foundArguments.add(new Object[] { pname, params });
    }

    @Override
    public void glLightModeliv(int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glLightModeliv");
        details.foundArguments.add(new Object[] { pname, params, params_offset });
    }

    @Override
    public void glLighti(int light, int pname, int param)
    {
        CallDetails details = getMethodDetails("glLighti");
        details.foundArguments.add(new Object[] { light, pname, param });
    }

    @Override
    public void glLightiv(int light, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glLightiv");
        details.foundArguments.add(new Object[] { light, pname, params });
    }

    @Override
    public void glLightiv(int light, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glLightiv");
        details.foundArguments.add(new Object[] { light, pname, params, params_offset });
    }

    @Override
    public void glLineStipple(int factor, short pattern)
    {
        CallDetails details = getMethodDetails("glLineStipple");
        details.foundArguments.add(new Object[] { factor, pattern });
    }

    @Override
    public void glLinkProgramARB(long programObj)
    {
        CallDetails details = getMethodDetails("glLinkProgramARB");
        details.foundArguments.add(new Object[] { programObj });
    }

    @Override
    public void glListBase(int base)
    {
        CallDetails details = getMethodDetails("glListBase");
        details.foundArguments.add(new Object[] { base });
    }

    @Override
    public void glLoadMatrixd(DoubleBuffer m)
    {
        CallDetails details = getMethodDetails("glLoadMatrixd");
        details.foundArguments.add(new Object[] { m });
    }

    @Override
    public void glLoadMatrixd(double[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glLoadMatrixd");
        details.foundArguments.add(new Object[] { m, m_offset });
    }

    @Override
    public void glLoadName(int name)
    {
        CallDetails details = getMethodDetails("glLoadName");
        details.foundArguments.add(new Object[] { name });
    }

    @Override
    public void glLoadTransposeMatrixd(DoubleBuffer m)
    {
        CallDetails details = getMethodDetails("glLoadTransposeMatrixd");
        details.foundArguments.add(new Object[] { m });
    }

    @Override
    public void glLoadTransposeMatrixd(double[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glLoadTransposeMatrixd");
        details.foundArguments.add(new Object[] { m, m_offset });
    }

    @Override
    public void glLoadTransposeMatrixf(FloatBuffer m)
    {
        CallDetails details = getMethodDetails("glLoadTransposeMatrixf");
        details.foundArguments.add(new Object[] { m });
    }

    @Override
    public void glLoadTransposeMatrixf(float[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { m, m_offset });
    }

    @Override
    public void glLockArraysEXT(int first, int count)
    {
        CallDetails details = getMethodDetails("glLockArraysEXT");
        details.foundArguments.add(new Object[] { first, count });
    }

    @Override
    public void glMap1d(int target, double u1, double u2, int stride, int order, DoubleBuffer points)
    {
        CallDetails details = getMethodDetails("glMap1d");
        details.foundArguments.add(new Object[] { target, u1, u2, stride, order, points });
    }

    @Override
    public void glMap1d(int target, double u1, double u2, int stride, int order, double[] points, int points_offset)
    {
        CallDetails details = getMethodDetails("glMap1d");
        details.foundArguments.add(new Object[] { target, u1, u2, stride, order, points, points_offset });
    }

    @Override
    public void glMap1f(int target, float u1, float u2, int stride, int order, FloatBuffer points)
    {
        CallDetails details = getMethodDetails("glMap1f");
        details.foundArguments.add(new Object[] { target, u1, u2, stride, order, points });
    }

    @Override
    public void glMap1f(int target, float u1, float u2, int stride, int order, float[] points, int points_offset)
    {
        CallDetails details = getMethodDetails("glMap1d");
        details.foundArguments.add(new Object[] { target, u1, u2, stride, order, points, points_offset });
    }

    @Override
    public void glMap2d(int target, double u1, double u2, int ustride, int uorder, double v1, double v2, int vstride, int vorder, DoubleBuffer points)
    {
        CallDetails details = getMethodDetails("glMap2d");
        details.foundArguments.add(new Object[] { target, u1, u2, ustride, uorder, v1, v2, vstride, vorder, points });
    }

    @Override
    public void glMap2d(int target, double u1, double u2, int ustride, int uorder, double v1, double v2, int vstride, int vorder, double[] points, int points_offset)
    {
        CallDetails details = getMethodDetails("glMap2d");
        details.foundArguments.add(new Object[] { target, u1, u2, ustride, uorder, v1, v2, vstride, vorder, points, points_offset });
    }

    @Override
    public void glMap2f(int target, float u1, float u2, int ustride, int uorder, float v1, float v2, int vstride, int vorder, FloatBuffer points)
    {
        CallDetails details = getMethodDetails("glMap2f");
        details.foundArguments.add(new Object[] { target, u1, u2, ustride, uorder, v1, v2, vstride, vorder, points });
    }

    @Override
    public void glMap2f(int target, float u1, float u2, int ustride, int uorder, float v1, float v2, int vstride, int vorder, float[] points, int points_offset)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { target, u1, u2, ustride, uorder, v1, v2, vstride, vorder, points, points_offset });
    }

    @Override
    public void glMapControlPointsNV(int target, int index, int type, int ustride, int vstride, int uorder, int vorder, boolean packed, Buffer points)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMapGrid1d(int un, double u1, double u2)
    {
        CallDetails details = getMethodDetails("glMapGrid1d");
        details.foundArguments.add(new Object[] { un, u1, u2 });
    }

    @Override
    public void glMapGrid1f(int un, float u1, float u2)
    {
        CallDetails details = getMethodDetails("glMapGrid1f");
        details.foundArguments.add(new Object[] { un, u1, u2 });
    }

    @Override
    public void glMapGrid2d(int un, double u1, double u2, int vn, double v1, double v2)
    {
        CallDetails details = getMethodDetails("glMapGrid2d");
        details.foundArguments.add(new Object[] { un, u1, u2, vn, v1, v2 });
    }

    @Override
    public void glMapGrid2f(int un, float u1, float u2, int vn, float v1, float v2)
    {
        CallDetails details = getMethodDetails("glMapGrid2f");
        details.foundArguments.add(new Object[] { un, u1, u2, vn, v1, v2 });
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMapParameterfvNV(int target, int pname, float[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMapParameterivNV(int target, int pname, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMapParameterivNV(int target, int pname, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteBuffer glMapTexture2DINTEL(int texture, int level, int access, IntBuffer stride, IntBuffer layout)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteBuffer glMapTexture2DINTEL(int texture, int level, int access, int[] stride, int stride_offset, int[] layout, int layout_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMapVertexAttrib1dAPPLE(int index, int size, double u1, double u2, int stride, int order, DoubleBuffer points)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMapVertexAttrib1dAPPLE(int index, int size, double u1, double u2, int stride, int order, double[] points, int points_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMapVertexAttrib1fAPPLE(int index, int size, float u1, float u2, int stride, int order, FloatBuffer points)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMapVertexAttrib1fAPPLE(int index, int size, float u1, float u2, int stride, int order, float[] points, int points_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMapVertexAttrib2dAPPLE(int index, int size, double u1, double u2, int ustride, int uorder, double v1, double v2, int vstride, int vorder, DoubleBuffer points)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMapVertexAttrib2dAPPLE(int index, int size, double u1, double u2, int ustride, int uorder, double v1, double v2, int vstride, int vorder, double[] points, int points_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMapVertexAttrib2fAPPLE(int index, int size, float u1, float u2, int ustride, int uorder, float v1, float v2, int vstride, int vorder, FloatBuffer points)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMapVertexAttrib2fAPPLE(int index, int size, float u1, float u2, int ustride, int uorder, float v1, float v2, int vstride, int vorder, float[] points, int points_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMateriali(int face, int pname, int param)
    {
        CallDetails details = getMethodDetails("glMateriali");
        details.foundArguments.add(new Object[] { face, pname, param });
    }

    @Override
    public void glMaterialiv(int face, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glMaterialiv");
        details.foundArguments.add(new Object[] { face, pname, params });
    }

    @Override
    public void glMaterialiv(int face, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glMaterialiv");
        details.foundArguments.add(new Object[] { face, pname, params, params_offset });
    }

    @Override
    public void glMatrixFrustumEXT(int mode, double left, double right, double bottom, double top, double zNear, double zFar)
    {
        CallDetails details = getMethodDetails("glMatrixFrustumEXT");
        details.foundArguments.add(new Object[] { mode, left, right, bottom, top, zNear, zFar });
    }

    @Override
    public void glMatrixIndexPointerARB(int size, int type, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glMatrixIndexPointerARB");
        details.foundArguments.add(new Object[] { size, type, stride, pointer });
    }

    @Override
    public void glMatrixIndexPointerARB(int size, int type, int stride, long pointer_buffer_offset)
    {
        CallDetails details = getMethodDetails("glMatrixIndexPointerARB");
        details.foundArguments.add(new Object[] { size, type, stride, pointer_buffer_offset });
    }

    @Override
    public void glMaxShaderCompilerThreadsARB(int count)
    {

    }

    @Override
    public void glFramebufferSampleLocationsfvARB(int target, int start, int count, FloatBuffer v)
    {

    }

    @Override
    public void glFramebufferSampleLocationsfvARB(int target, int start, int count, float[] v, int v_offset)
    {

    }

    @Override
    public void glNamedFramebufferSampleLocationsfvARB(int framebuffer, int start, int count, FloatBuffer v)
    {

    }

    @Override
    public void glNamedFramebufferSampleLocationsfvARB(int framebuffer, int start, int count, float[] v, int v_offset)
    {

    }

    @Override
    public void glEvaluateDepthValuesARB()
    {

    }

    @Override
    public void glMatrixIndexubvARB(int size, ByteBuffer indices)
    {
        CallDetails details = getMethodDetails("glMatrixIndexubvARB");
        details.foundArguments.add(new Object[] { size, indices });
    }

    @Override
    public void glMatrixIndexubvARB(int size, byte[] indices, int indices_offset)
    {
        CallDetails details = getMethodDetails("glMatrixIndexubvARB");
        details.foundArguments.add(new Object[] { size, indices, indices_offset });
    }

    @Override
    public void glMatrixIndexuivARB(int size, IntBuffer indices)
    {
        CallDetails details = getMethodDetails("glMatrixIndexuivARB");
        details.foundArguments.add(new Object[] { size, indices });
    }

    @Override
    public void glMatrixIndexuivARB(int size, int[] indices, int indices_offset)
    {
        CallDetails details = getMethodDetails("glMatrixIndexuivARB");
        details.foundArguments.add(new Object[] { size, indices, indices_offset });
    }

    @Override
    public void glMatrixIndexusvARB(int size, ShortBuffer indices)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { size, indices });
    }

    @Override
    public void glMatrixIndexusvARB(int size, short[] indices, int indices_offset)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { size, indices, indices_offset });
    }

    @Override
    public void glMatrixLoadIdentityEXT(int mode)
    {
        CallDetails details = getMethodDetails("glMatrixLoadIdentityEXT");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public void glMatrixLoadTransposedEXT(int mode, DoubleBuffer m)
    {
        CallDetails details = getMethodDetails("glMatrixLoadTransposedEXT");
        details.foundArguments.add(new Object[] { mode, m });
    }

    @Override
    public void glMatrixLoadTransposedEXT(int mode, double[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glMatrixLoadTransposedEXT");
        details.foundArguments.add(new Object[] { mode, m, m_offset });
    }

    @Override
    public void glMatrixLoadTransposefEXT(int mode, FloatBuffer m)
    {
        CallDetails details = getMethodDetails("glMatrixLoadTransposefEXT");
        details.foundArguments.add(new Object[] { mode, m });
    }

    @Override
    public void glMatrixLoadTransposefEXT(int mode, float[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glMatrixLoadTransposefEXT");
        details.foundArguments.add(new Object[] { mode, m, m_offset });
    }

    @Override
    public void glMatrixLoaddEXT(int mode, DoubleBuffer m)
    {
        CallDetails details = getMethodDetails("glMatrixLoaddEXT");
        details.foundArguments.add(new Object[] { mode, m });
    }

    @Override
    public void glMatrixLoaddEXT(int mode, double[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glMatrixLoaddEXT");
        details.foundArguments.add(new Object[] { mode, m, m_offset });
    }

    @Override
    public void glMatrixLoadfEXT(int mode, FloatBuffer m)
    {
        CallDetails details = getMethodDetails("glMatrixLoadfEXT");
        details.foundArguments.add(new Object[] { mode, m });
    }

    @Override
    public void glMatrixLoadfEXT(int mode, float[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glMatrixLoadfEXT");
        details.foundArguments.add(new Object[] { mode, m, m_offset });
    }

    @Override
    public void glMatrixMultTransposedEXT(int mode, DoubleBuffer m)
    {
        CallDetails details = getMethodDetails("glMatrixMultTransposedEXT");
        details.foundArguments.add(new Object[] { mode, m });
    }

    @Override
    public void glMatrixMultTransposedEXT(int mode, double[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glMatrixMultTransposedEXT");
        details.foundArguments.add(new Object[] { mode, m, m_offset });
    }

    @Override
    public void glMatrixMultTransposefEXT(int mode, FloatBuffer m)
    {
        CallDetails details = getMethodDetails("glMatrixMultTransposefEXT");
        details.foundArguments.add(new Object[] { mode, m });
    }

    @Override
    public void glMatrixMultTransposefEXT(int mode, float[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glMatrixMultTransposefEXT");
        details.foundArguments.add(new Object[] { mode, m, m_offset });
    }

    @Override
    public void glMatrixMultdEXT(int mode, DoubleBuffer m)
    {
        CallDetails details = getMethodDetails("glMatrixMultdEXT");
        details.foundArguments.add(new Object[] { mode, m });
    }

    @Override
    public void glMatrixMultdEXT(int mode, double[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glMatrixMultdEXT");
        details.foundArguments.add(new Object[] { mode, m, m_offset });
    }

    @Override
    public void glMatrixMultfEXT(int mode, FloatBuffer m)
    {
        CallDetails details = getMethodDetails("glMatrixMultfEXT");
        details.foundArguments.add(new Object[] { mode, m });
    }

    @Override
    public void glMatrixMultfEXT(int mode, float[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glMatrixMultfEXT");
        details.foundArguments.add(new Object[] { mode, m, m_offset });
    }

    @Override
    public void glMatrixOrthoEXT(int mode, double left, double right, double bottom, double top, double zNear, double zFar)
    {
        CallDetails details = getMethodDetails("glMatrixOrthoEXT");
        details.foundArguments.add(new Object[] { mode, left, right, bottom, top, zNear, zFar });
    }

    @Override
    public void glMatrixPopEXT(int mode)
    {
        CallDetails details = getMethodDetails("glMatrixPopEXT");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public void glMatrixPushEXT(int mode)
    {
        CallDetails details = getMethodDetails("glMatrixPushEXT");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public void glMatrixRotatedEXT(int mode, double angle, double x, double y, double z)
    {
        CallDetails details = getMethodDetails("glMatrixRotatedEXT");
        details.foundArguments.add(new Object[] { mode, angle, x, y, z });
    }

    @Override
    public void glMatrixRotatefEXT(int mode, float angle, float x, float y, float z)
    {
        CallDetails details = getMethodDetails("glMatrixRotatefEXT");
        details.foundArguments.add(new Object[] { mode, angle, x, y, z });
    }

    @Override
    public void glMatrixScaledEXT(int mode, double x, double y, double z)
    {
        CallDetails details = getMethodDetails("glMatrixScaledEXT");
        details.foundArguments.add(new Object[] { mode, x, y, z });
    }

    @Override
    public void glMatrixScalefEXT(int mode, float x, float y, float z)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { mode, x, y, z });
    }

    @Override
    public void glMatrixTranslatedEXT(int mode, double x, double y, double z)
    {
        CallDetails details = getMethodDetails("glMatrixTranslatedEXT");
        details.foundArguments.add(new Object[] { mode, x, y, z });
    }

    @Override
    public void glMatrixTranslatefEXT(int mode, float x, float y, float z)
    {
        CallDetails details = getMethodDetails("glMatrixTranslatefEXT");
        details.foundArguments.add(new Object[] { mode, x, y, z });
    }

    @Override
    public void glMinmax(int target, int internalformat, boolean sink)
    {
        CallDetails details = getMethodDetails("glMinmax");
        details.foundArguments.add(new Object[] { target, internalformat, sink });
    }

    @Override
    public void glMultMatrixd(DoubleBuffer m)
    {
        CallDetails details = getMethodDetails("glMultMatrixd");
        details.foundArguments.add(new Object[] { m });
    }

    @Override
    public void glMultMatrixd(double[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glMultMatrixd");
        details.foundArguments.add(new Object[] { m, m_offset });
    }

    @Override
    public void glMultTransposeMatrixd(DoubleBuffer m)
    {
        CallDetails details = getMethodDetails("glMultTransposeMatrixd");
        details.foundArguments.add(new Object[] { m });
    }

    @Override
    public void glMultTransposeMatrixd(double[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glMultTransposeMatrixd");
        details.foundArguments.add(new Object[] { m, m_offset });
    }

    @Override
    public void glMultTransposeMatrixf(FloatBuffer m)
    {
        CallDetails details = getMethodDetails("glMultTransposeMatrixf");
        details.foundArguments.add(new Object[] { m });
    }

    @Override
    public void glMultTransposeMatrixf(float[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glMultTransposeMatrixf");
        details.foundArguments.add(new Object[] { m, m_offset });
    }

    @Override
    public void glMultiDrawArraysIndirectBindlessCountNV(int mode, Buffer indirect, int drawCount, int maxDrawCount, int stride, int vertexBufferCount)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMultiDrawArraysIndirectBindlessNV(int mode, Buffer indirect, int drawCount, int stride, int vertexBufferCount)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMultiDrawElementsIndirectBindlessCountNV(int mode, int type, Buffer indirect, int drawCount, int maxDrawCount, int stride, int vertexBufferCount)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glCreateStatesNV(int n, IntBuffer states)
    {

    }

    @Override
    public void glCreateStatesNV(int n, int[] states, int states_offset)
    {

    }

    @Override
    public void glDeleteStatesNV(int n, IntBuffer states)
    {

    }

    @Override
    public void glDeleteStatesNV(int n, int[] states, int states_offset)
    {

    }

    @Override
    public boolean glIsStateNV(int state)
    {
        return false;
    }

    @Override
    public void glStateCaptureNV(int state, int mode)
    {

    }

    @Override
    public int glGetCommandHeaderNV(int tokenID, int size)
    {
        return 0;
    }

    @Override
    public short glGetStageIndexNV(int shadertype)
    {
        return 0;
    }

    @Override
    public void glDrawCommandsNV(int primitiveMode, int buffer, PointerBuffer indirects, IntBuffer sizes, int count)
    {

    }

    @Override
    public void glDrawCommandsNV(int primitiveMode, int buffer, PointerBuffer indirects, int[] sizes, int sizes_offset, int count)
    {

    }

    @Override
    public void glDrawCommandsAddressNV(int primitiveMode, LongBuffer indirects, IntBuffer sizes, int count)
    {

    }

    @Override
    public void glDrawCommandsAddressNV(int primitiveMode, long[] indirects, int indirects_offset, int[] sizes, int sizes_offset, int count)
    {

    }

    @Override
    public void glDrawCommandsStatesNV(int buffer, PointerBuffer indirects, IntBuffer sizes, IntBuffer states, IntBuffer fbos, int count)
    {

    }

    @Override
    public void glDrawCommandsStatesNV(int buffer, PointerBuffer indirects, int[] sizes, int sizes_offset, int[] states, int states_offset, int[] fbos, int fbos_offset, int count)
    {

    }

    @Override
    public void glDrawCommandsStatesAddressNV(LongBuffer indirects, IntBuffer sizes, IntBuffer states, IntBuffer fbos, int count)
    {

    }

    @Override
    public void glDrawCommandsStatesAddressNV(long[] indirects, int indirects_offset, int[] sizes, int sizes_offset, int[] states, int states_offset, int[] fbos, int fbos_offset, int count)
    {

    }

    @Override
    public void glCreateCommandListsNV(int n, IntBuffer lists)
    {

    }

    @Override
    public void glCreateCommandListsNV(int n, int[] lists, int lists_offset)
    {

    }

    @Override
    public void glDeleteCommandListsNV(int n, IntBuffer lists)
    {

    }

    @Override
    public void glDeleteCommandListsNV(int n, int[] lists, int lists_offset)
    {

    }

    @Override
    public boolean glIsCommandListNV(int list)
    {
        return false;
    }

    @Override
    public void glListDrawCommandsStatesClientNV(int list, int segment, PointerBuffer indirects, IntBuffer sizes, IntBuffer states, IntBuffer fbos, int count)
    {

    }

    @Override
    public void glListDrawCommandsStatesClientNV(int list, int segment, PointerBuffer indirects, int[] sizes, int sizes_offset, int[] states, int states_offset, int[] fbos, int fbos_offset, int count)
    {

    }

    @Override
    public void glCommandListSegmentsNV(int list, int segments)
    {

    }

    @Override
    public void glCompileCommandListNV(int list)
    {

    }

    @Override
    public void glCallCommandListNV(int list)
    {

    }

    @Override
    public void glMultiDrawElementsIndirectBindlessNV(int mode, int type, Buffer indirect, int drawCount, int stride, int vertexBufferCount)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMultiTexBufferEXT(int texunit, int target, int internalformat, int buffer)
    {
        CallDetails details = getMethodDetails("glMultiTexBufferEXT");
        details.foundArguments.add(new Object[] { texunit, target, internalformat, buffer });
    }

    @Override
    public void glMultiTexCoord1bOES(int texture, byte s)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1bOES");
        details.foundArguments.add(new Object[] { texture, s });
    }

    @Override
    public void glMultiTexCoord1bvOES(int texture, ByteBuffer coords)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1bvOES");
        details.foundArguments.add(new Object[] { texture, coords });
    }

    @Override
    public void glMultiTexCoord1bvOES(int texture, byte[] coords, int coords_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1bvOES");
        details.foundArguments.add(new Object[] { texture, coords, coords_offset });
    }

    @Override
    public void glMultiTexCoord1d(int target, double s)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1d");
        details.foundArguments.add(new Object[] { target, s });
    }

    @Override
    public void glMultiTexCoord1dv(int target, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1dv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord1dv(int target, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1dv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord1f(int target, float s)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1f");
        details.foundArguments.add(new Object[] { target, s });
    }

    @Override
    public void glMultiTexCoord1fv(int target, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1fv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord1fv(int target, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1fv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord1h(int target, short s)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1h");
        details.foundArguments.add(new Object[] { target, s });
    }

    @Override
    public void glMultiTexCoord1hv(int target, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1hv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord1hv(int target, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("gvlMultiTexCoord1h");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord1i(int target, int s)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1i");
        details.foundArguments.add(new Object[] { target, s });
    }

    @Override
    public void glMultiTexCoord1iv(int target, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1iv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord1iv(int target, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1iv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord1s(int target, short s)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1s");
        details.foundArguments.add(new Object[] { target, s });
    }

    @Override
    public void glMultiTexCoord1sv(int target, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1sv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord1sv(int target, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord1sv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord2bOES(int texture, byte s, byte t)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2bOES");
        details.foundArguments.add(new Object[] { texture, s, t });
    }

    @Override
    public void glMultiTexCoord2bvOES(int texture, ByteBuffer coords)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2bvOES");
        details.foundArguments.add(new Object[] { texture, coords });
    }

    @Override
    public void glMultiTexCoord2bvOES(int texture, byte[] coords, int coords_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2bvOES");
        details.foundArguments.add(new Object[] { texture, coords, coords_offset });
    }

    @Override
    public void glMultiTexCoord2d(int target, double s, double t)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2d");
        details.foundArguments.add(new Object[] { target, s, t });
    }

    @Override
    public void glMultiTexCoord2dv(int target, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2dv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord2dv(int target, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2dv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord2f(int target, float s, float t)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2f");
        details.foundArguments.add(new Object[] { target, s, t });
    }

    @Override
    public void glMultiTexCoord2fv(int target, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2f");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord2fv(int target, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2fv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord2h(int target, short s, short t)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2h");
        details.foundArguments.add(new Object[] { target, s, t });
    }

    @Override
    public void glMultiTexCoord2hv(int target, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2hv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord2hv(int target, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2hv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord2i(int target, int s, int t)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2i");
        details.foundArguments.add(new Object[] { target, s, t });
    }

    @Override
    public void glMultiTexCoord2iv(int target, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2iv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord2iv(int target, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2iv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord2s(int target, short s, short t)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2s");
        details.foundArguments.add(new Object[] { target, s, t });
    }

    @Override
    public void glMultiTexCoord2sv(int target, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2sv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord2sv(int target, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord2sv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord3bOES(int texture, byte s, byte t, byte r)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { texture, s, t, r });
    }

    @Override
    public void glMultiTexCoord3bvOES(int texture, ByteBuffer coords)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3bvOES");
        details.foundArguments.add(new Object[] { texture, coords });
    }

    @Override
    public void glMultiTexCoord3bvOES(int texture, byte[] coords, int coords_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3bvOES");
        details.foundArguments.add(new Object[] { texture, coords, coords_offset });
    }

    @Override
    public void glMultiTexCoord3d(int target, double s, double t, double r)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3d");
        details.foundArguments.add(new Object[] { target, s, t, r });
    }

    @Override
    public void glMultiTexCoord3dv(int target, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3dv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord3dv(int target, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3dv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord3f(int target, float s, float t, float r)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3f");
        details.foundArguments.add(new Object[] { target, s, t, r });
    }

    @Override
    public void glMultiTexCoord3fv(int target, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3fv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord3fv(int target, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3fv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord3h(int target, short s, short t, short r)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3h");
        details.foundArguments.add(new Object[] { target, s, t, r });
    }

    @Override
    public void glMultiTexCoord3hv(int target, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3hv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord3hv(int target, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3hv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord3i(int target, int s, int t, int r)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3i");
        details.foundArguments.add(new Object[] { target, s, t, r });
    }

    @Override
    public void glMultiTexCoord3iv(int target, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3iv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord3iv(int target, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3iv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord3s(int target, short s, short t, short r)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3s");
        details.foundArguments.add(new Object[] { target, s, t, r });
    }

    @Override
    public void glMultiTexCoord3sv(int target, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3sv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord3sv(int target, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord3sv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord4bOES(int texture, byte s, byte t, byte r, byte q)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord4bOES");
        details.foundArguments.add(new Object[] { texture, s, t, r, q });
    }

    @Override
    public void glMultiTexCoord4bvOES(int texture, ByteBuffer coords)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord4bvOES");
        details.foundArguments.add(new Object[] { texture, coords });
    }

    @Override
    public void glMultiTexCoord4bvOES(int texture, byte[] coords, int coords_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord4bvOES");
        details.foundArguments.add(new Object[] { texture, coords, coords_offset});
    }

    @Override
    public void glMultiTexCoord4d(int target, double s, double t, double r, double q)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord4d");
        details.foundArguments.add(new Object[] { target, s, t, r, q });
    }

    @Override
    public void glMultiTexCoord4dv(int target, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord4dv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord4dv(int target, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord4dv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord4fv(int target, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord4fv(int target, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord4h(int target, short s, short t, short r, short q)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { target, s, t, r, q });
    }

    @Override
    public void glMultiTexCoord4hv(int target, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord4hv(int target, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord4i(int target, int s, int t, int r, int q)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord4i");
        details.foundArguments.add(new Object[] { target, s, t, r, q });
    }

    @Override
    public void glMultiTexCoord4iv(int target, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord4iv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord4iv(int target, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord4iv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoord4s(int target, short s, short t, short r, short q)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord4s");
        details.foundArguments.add(new Object[] { target, s, t, r, q });
    }

    @Override
    public void glMultiTexCoord4sv(int target, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord4sv");
        details.foundArguments.add(new Object[] { target, v });
    }

    @Override
    public void glMultiTexCoord4sv(int target, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord4sv");
        details.foundArguments.add(new Object[] { target, v, v_offset });
    }

    @Override
    public void glMultiTexCoordPointerEXT(int texunit, int size, int type, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glMultiTexCoordPointerEXT");
        details.foundArguments.add(new Object[] { texunit, size, type, stride, pointer });
    }

    @Override
    public void glMultiTexEnvfEXT(int texunit, int target, int pname, float param)
    {
        CallDetails details = getMethodDetails("glMultiTexEnvfEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, param });
    }

    @Override
    public void glMultiTexEnvfvEXT(int texunit, int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glMultiTexEnvfvEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params });
    }

    @Override
    public void glMultiTexEnvfvEXT(int texunit, int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexEnvfvEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params, params_offset });
    }

    @Override
    public void glMultiTexEnviEXT(int texunit, int target, int pname, int param)
    {
        CallDetails details = getMethodDetails("glMultiTexEnviEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, param });
    }

    @Override
    public void glMultiTexEnvivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glMultiTexEnvivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params });
    }

    @Override
    public void glMultiTexEnvivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexEnvivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params, params_offset });
    }

    @Override
    public void glMultiTexGendEXT(int texunit, int coord, int pname, double param)
    {
        CallDetails details = getMethodDetails("glMultiTexGendEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, param });
    }

    @Override
    public void glMultiTexGendvEXT(int texunit, int coord, int pname, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glMultiTexGendvEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, params });
    }

    @Override
    public void glMultiTexGendvEXT(int texunit, int coord, int pname, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexGendvEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, params, params_offset });
    }

    @Override
    public void glMultiTexGenfEXT(int texunit, int coord, int pname, float param)
    {
        CallDetails details = getMethodDetails("glMultiTexGenfEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, param });
    }

    @Override
    public void glMultiTexGenfvEXT(int texunit, int coord, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glMultiTexGenfvEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, params });
    }

    @Override
    public void glMultiTexGenfvEXT(int texunit, int coord, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexGenfvEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, params, params_offset });
    }

    @Override
    public void glMultiTexGeniEXT(int texunit, int coord, int pname, int param)
    {
        CallDetails details = getMethodDetails("glMultiTexGeniEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, param });
    }

    @Override
    public void glMultiTexGenivEXT(int texunit, int coord, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glMultiTexGenivEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, params });
    }

    @Override
    public void glMultiTexGenivEXT(int texunit, int coord, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexGenivEXT");
        details.foundArguments.add(new Object[] { texunit, coord, pname, params, params_offset });
    }

    @Override
    public void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width, int border, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glMultiTexImage1DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, internalformat, width, border, format, type, pixels });
    }

    @Override
    public void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glMultiTexImage2DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, internalformat, width, height, border, format, type, pixels });
    }

    @Override
    public void glMultiTexImage3DEXT(int texunit, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glMultiTexImage3DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, internalformat, width, height, depth, border, format, type, pixels });
    }

    @Override
    public void glMultiTexParameterIivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glMultiTexParameterIivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params });
    }

    @Override
    public void glMultiTexParameterIivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexParameterIivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params, params_offset });
    }

    @Override
    public void glMultiTexParameterIuivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glMultiTexParameterIuivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params });
    }

    @Override
    public void glMultiTexParameterIuivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexParameterIuivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params, params_offset });
    }

    @Override
    public void glMultiTexParameterfEXT(int texunit, int target, int pname, float param)
    {
        CallDetails details = getMethodDetails("glMultiTexParameterfEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, param });
    }

    @Override
    public void glMultiTexParameterfvEXT(int texunit, int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glMultiTexParameterfvEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params });
    }

    @Override
    public void glMultiTexParameterfvEXT(int texunit, int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexParameterfvEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params, params_offset });
    }

    @Override
    public void glMultiTexParameteriEXT(int texunit, int target, int pname, int param)
    {
        CallDetails details = getMethodDetails("glMultiTexParameteriEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, param });
    }

    @Override
    public void glMultiTexParameterivEXT(int texunit, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glMultiTexParameterivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params });
    }

    @Override
    public void glMultiTexParameterivEXT(int texunit, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glMultiTexParameterivEXT");
        details.foundArguments.add(new Object[] { texunit, target, pname, params, params_offset });
    }

    @Override
    public void glMultiTexRenderbufferEXT(int texunit, int target, int renderbuffer)
    {
        CallDetails details = getMethodDetails("glMultiTexRenderbufferEXT");
        details.foundArguments.add(new Object[] { texunit, target, renderbuffer });
    }

    @Override
    public void glMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int width, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glMultiTexSubImage1DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, xoffset, width, format, type, pixels });
    }

    @Override
    public void glMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glMultiTexSubImage2DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, xoffset, yoffset, width, height, format, type, pixels });
    }

    @Override
    public void glMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glMultiTexSubImage3DEXT");
        details.foundArguments.add(new Object[] { texunit, target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels });
    }

    @Override
    public void glNamedBufferDataEXT(int buffer, long size, Buffer data, int usage)
    {
        CallDetails details = getMethodDetails("glNamedBufferDataEXT");
        details.foundArguments.add(new Object[] { buffer, size, data, usage });
    }

    @Override
    public void glNamedBufferStorageEXT(int buffer, long size, Buffer data, int flags)
    {

    }

    @Override
    public void glNamedBufferSubDataEXT(int buffer, long offset, long size, Buffer data)
    {
        CallDetails details = getMethodDetails("glNamedBufferSubDataEXT");
        details.foundArguments.add(new Object[] { buffer, offset, size, data });
    }

    @Override
    public void glNamedCopyBufferSubDataEXT(int readBuffer, int writeBuffer, long readOffset, long writeOffset, long size)
    {
        CallDetails details = getMethodDetails("glNamedCopyBufferSubDataEXT");
        details.foundArguments.add(new Object[] { readBuffer, writeBuffer, readOffset, writeOffset, size });
    }

    @Override
    public void glNamedFramebufferParameteri(int framebuffer, int pname, int param)
    {
        CallDetails details = getMethodDetails("glNamedFramebufferParameteri");
        details.foundArguments.add(new Object[] { framebuffer, pname, param });
    }

    @Override
    public void glNamedFramebufferRenderbufferEXT(int framebuffer, int attachment, int renderbuffertarget, int renderbuffer)
    {
        CallDetails details = getMethodDetails("glNamedFramebufferRenderbufferEXT");
        details.foundArguments.add(new Object[] { framebuffer, attachment, renderbuffertarget, renderbuffer });
    }

    @Override
    public void glNamedFramebufferSampleLocationsfvNV(int framebuffer, int start, int count, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glNamedFramebufferSampleLocationsfvNV");
        details.foundArguments.add(new Object[] { framebuffer, start, count, v });
    }

    @Override
    public void glNamedFramebufferSampleLocationsfvNV(int framebuffer, int start, int count, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glNamedFramebufferSampleLocationsfvNV");
        details.foundArguments.add(new Object[] { framebuffer, start, count, v, v_offset });
    }

    @Override
    public void glNamedFramebufferTexture1DEXT(int framebuffer, int attachment, int textarget, int texture, int level)
    {
        CallDetails details = getMethodDetails("glNamedFramebufferTexture1DEXT");
        details.foundArguments.add(new Object[] { framebuffer, attachment, textarget, texture, level });
    }

    @Override
    public void glNamedFramebufferTexture2DEXT(int framebuffer, int attachment, int textarget, int texture, int level)
    {
        CallDetails details = getMethodDetails("glNamedFramebufferTexture2DEXT");
        details.foundArguments.add(new Object[] { framebuffer, attachment, textarget, texture, level });
    }

    @Override
    public void glNamedFramebufferTexture3DEXT(int framebuffer, int attachment, int textarget, int texture, int level, int zoffset)
    {
        CallDetails details = getMethodDetails("glNamedFramebufferTexture3DEXT");
        details.foundArguments.add(new Object[] { framebuffer, attachment, textarget, texture, level, zoffset });
    }

    @Override
    public void glNamedFramebufferTextureEXT(int framebuffer, int attachment, int texture, int level)
    {
        CallDetails details = getMethodDetails("glNamedFramebufferTextureEXT");
        details.foundArguments.add(new Object[] { framebuffer, attachment, texture, level });
    }

    @Override
    public void glNamedFramebufferTextureFaceEXT(int framebuffer, int attachment, int texture, int level, int face)
    {
        CallDetails details = getMethodDetails("glNamedFramebufferTextureFaceEXT");
        details.foundArguments.add(new Object[] { framebuffer, attachment, texture, level, face });
    }

    @Override
    public void glNamedFramebufferTextureLayerEXT(int framebuffer, int attachment, int texture, int level, int layer)
    {
        CallDetails details = getMethodDetails("glNamedFramebufferTextureLayerEXT");
        details.foundArguments.add(new Object[] { framebuffer, attachment, texture, level, layer });
    }

    @Override
    public void glNamedProgramLocalParameter4dEXT(int program, int target, int index, double x, double y, double z, double w)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameter4dEXT");
        details.foundArguments.add(new Object[] { program, target, index, x, y, z, w });
    }

    @Override
    public void glNamedProgramLocalParameter4dvEXT(int program, int target, int index, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameter4dvEXT");
        details.foundArguments.add(new Object[] { program, target, index, params });
    }

    @Override
    public void glNamedProgramLocalParameter4dvEXT(int program, int target, int index, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameter4dvEXT");
        details.foundArguments.add(new Object[] { program, target, index, params, params_offset });
    }

    @Override
    public void glNamedProgramLocalParameter4fEXT(int program, int target, int index, float x, float y, float z, float w)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameter4fEXT");
        details.foundArguments.add(new Object[] { program, target, index, x, y, z, w });
    }

    @Override
    public void glNamedProgramLocalParameter4fvEXT(int program, int target, int index, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameter4fvEXT");
        details.foundArguments.add(new Object[] { program, target, index, params });
    }

    @Override
    public void glNamedProgramLocalParameter4fvEXT(int program, int target, int index, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameter4fvEXT");
        details.foundArguments.add(new Object[] { program, target, index, params, params_offset });
    }

    @Override
    public void glNamedProgramLocalParameterI4iEXT(int program, int target, int index, int x, int y, int z, int w)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameterI4iEXT");
        details.foundArguments.add(new Object[] { program, target, index, x, y, z, w });
    }

    @Override
    public void glNamedProgramLocalParameterI4ivEXT(int program, int target, int index, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameterI4ivEXT");
        details.foundArguments.add(new Object[] { program, target, index, params });
    }

    @Override
    public void glNamedProgramLocalParameterI4ivEXT(int program, int target, int index, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameterI4ivEXT");
        details.foundArguments.add(new Object[] { program, target, index, params, params_offset });
    }

    @Override
    public void glNamedProgramLocalParameterI4uiEXT(int program, int target, int index, int x, int y, int z, int w)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameterI4uiEXT");
        details.foundArguments.add(new Object[] { program, target, index, x, y, z, w });
    }

    @Override
    public void glNamedProgramLocalParameterI4uivEXT(int program, int target, int index, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameterI4uivEXT");
        details.foundArguments.add(new Object[] { program, target, index, params });
    }

    @Override
    public void glNamedProgramLocalParameterI4uivEXT(int program, int target, int index, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameterI4uivEXT");
        details.foundArguments.add(new Object[] { program, target, index, params, params_offset });
    }

    @Override
    public void glNamedProgramLocalParameters4fvEXT(int program, int target, int index, int count, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameters4fvEXT");
        details.foundArguments.add(new Object[] { program, target, index, params });
    }

    @Override
    public void glNamedProgramLocalParameters4fvEXT(int program, int target, int index, int count, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParameters4fvEXT");
        details.foundArguments.add(new Object[] { program, target, index, params, params_offset });
    }

    @Override
    public void glNamedProgramLocalParametersI4ivEXT(int program, int target, int index, int count, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParametersI4ivEXT");
        details.foundArguments.add(new Object[] { program, target, index, params });
    }

    @Override
    public void glNamedProgramLocalParametersI4ivEXT(int program, int target, int index, int count, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParametersI4ivEXT");
        details.foundArguments.add(new Object[] { program, target, index, params, params_offset });
    }

    @Override
    public void glNamedProgramLocalParametersI4uivEXT(int program, int target, int index, int count, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParametersI4uivEXT");
        details.foundArguments.add(new Object[] { program, target, index, params });
    }

    @Override
    public void glNamedProgramLocalParametersI4uivEXT(int program, int target, int index, int count, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glNamedProgramLocalParametersI4uivEXT");
        details.foundArguments.add(new Object[] { program, target, index, params, params_offset });
    }

    @Override
    public void glNamedProgramStringEXT(int program, int target, int format, int len, Buffer string)
    {
        CallDetails details = getMethodDetails("glNamedProgramStringEXT");
        details.foundArguments.add(new Object[] { program, target, format, len, string });
    }

    @Override
    public void glNamedRenderbufferStorageEXT(int renderbuffer, int internalformat, int width, int height)
    {
        CallDetails details = getMethodDetails("glNamedRenderbufferStorageEXT");
        details.foundArguments.add(new Object[] { renderbuffer, internalformat, width, height });
    }

    @Override
    public void glNamedRenderbufferStorageMultisampleCoverageEXT(int renderbuffer, int coverageSamples, int colorSamples, int internalformat, int width, int height)
    {
        CallDetails details = getMethodDetails("glNamedRenderbufferStorageMultisampleCoverageEXT");
        details.foundArguments.add(new Object[] { renderbuffer, coverageSamples, colorSamples, internalformat, width, height });
    }

    @Override
    public void glNamedRenderbufferStorageMultisampleEXT(int renderbuffer, int samples, int internalformat, int width, int height)
    {
        CallDetails details = getMethodDetails("glNamedRenderbufferStorageMultisampleEXT");
        details.foundArguments.add(new Object[] { renderbuffer, samples, internalformat, width, height });
    }

    @Override
    public void glNewList(int list, int mode)
    {
        CallDetails details = getMethodDetails("glNewList");
        details.foundArguments.add(new Object[] { list, mode });
    }

    @Override
    public void glNormal3b(byte nx, byte ny, byte nz)
    {
        CallDetails details = getMethodDetails("glNormal3b");
        details.foundArguments.add(new Object[] { nx, ny, nz });
    }

    @Override
    public void glNormal3bv(ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glNormal3bv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glNormal3bv(byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glNormal3bv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glNormal3d(double nx, double ny, double nz)
    {
        CallDetails details = getMethodDetails("glNormal3d");
        details.foundArguments.add(new Object[] { nx, ny, nz });
    }

    @Override
    public void glNormal3dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glNormal3dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glNormal3dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glNormal3dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glNormal3fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glNormal3fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glNormal3fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glNormal3fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glNormal3h(short nx, short ny, short nz)
    {
        CallDetails details = getMethodDetails("glNormal3h");
        details.foundArguments.add(new Object[] { nx, ny, nz });
    }

    @Override
    public void glNormal3hv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glNormal3hv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glNormal3hv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glNormal3hv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glNormal3i(int nx, int ny, int nz)
    {
        CallDetails details = getMethodDetails("glNormal3i");
        details.foundArguments.add(new Object[] { nx, ny, nz });
    }

    @Override
    public void glNormal3iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glNormal3iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glNormal3iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glNormal3iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glNormal3s(short nx, short ny, short nz)
    {
        CallDetails details = getMethodDetails("glNormal3s");
        details.foundArguments.add(new Object[] { nx, ny, nz });
    }

    @Override
    public void glNormal3sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glNormal3sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glNormal3sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glNormal3sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public int glObjectPurgeableAPPLE(int objectType, int name, int option)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int glObjectUnpurgeableAPPLE(int objectType, int name, int option)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPNTrianglesfATI(int pname, float param)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPNTrianglesiATI(int pname, int param)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPassThrough(float token)
    {
        CallDetails details = getMethodDetails("glPassThrough");
        details.foundArguments.add(new Object[] { token });
    }

    @Override
    public void glPauseTransformFeedbackNV()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPixelDataRangeNV(int target, int length, Buffer pointer)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPixelMapfv(int map, int mapsize, FloatBuffer values)
    {
        CallDetails details = getMethodDetails("glPixelMapfv");
        details.foundArguments.add(new Object[] { map, mapsize, values });
    }

    @Override
    public void glPixelMapfv(int map, int mapsize, float[] values, int values_offset)
    {
        CallDetails details = getMethodDetails("glPixelMapfv");
        details.foundArguments.add(new Object[] { map, mapsize, values, values_offset });
    }

    @Override
    public void glPixelMapfv(int map, int mapsize, long values_buffer_offset)
    {
        CallDetails details = getMethodDetails("glPixelMapfv");
        details.foundArguments.add(new Object[] { map, mapsize, values_buffer_offset });
    }

    @Override
    public void glPixelMapuiv(int map, int mapsize, IntBuffer values)
    {
        CallDetails details = getMethodDetails("glPixelMapuiv");
        details.foundArguments.add(new Object[] { map, mapsize, values });
    }

    @Override
    public void glPixelMapuiv(int map, int mapsize, int[] values, int values_offset)
    {
        CallDetails details = getMethodDetails("glPixelMapuiv");
        details.foundArguments.add(new Object[] { map, mapsize, values, values_offset });
    }

    @Override
    public void glPixelMapuiv(int map, int mapsize, long values_buffer_offset)
    {
        CallDetails details = getMethodDetails("glPixelMapuiv");
        details.foundArguments.add(new Object[] { map, mapsize, values_buffer_offset });
    }

    @Override
    public void glPixelMapusv(int map, int mapsize, ShortBuffer values)
    {
        CallDetails details = getMethodDetails("glPixelMapusv");
        details.foundArguments.add(new Object[] { map, mapsize, values });
    }

    @Override
    public void glPixelMapusv(int map, int mapsize, short[] values, int values_offset)
    {
        CallDetails details = getMethodDetails("glPixelMapusv");
        details.foundArguments.add(new Object[] { map, mapsize, values, values_offset });
    }

    @Override
    public void glPixelMapusv(int map, int mapsize, long values_buffer_offset)
    {
        CallDetails details = getMethodDetails("glPixelMapusv");
        details.foundArguments.add(new Object[] { map, mapsize, values_buffer_offset });
    }

    @Override
    public void glPixelTransferf(int pname, float param)
    {
        CallDetails details = getMethodDetails("glPixelTransferf");
        details.foundArguments.add(new Object[] { pname, param });
    }

    @Override
    public void glPixelTransferi(int pname, int param)
    {
        CallDetails details = getMethodDetails("glPixelTransferi");
        details.foundArguments.add(new Object[] { pname, param });
    }

    @Override
    public void glPixelTransformParameterfEXT(int target, int pname, float param)
    {
        CallDetails details = getMethodDetails("glPixelTransformParameterfEXT");
        details.foundArguments.add(new Object[] { target, pname, param });
    }

    @Override
    public void glPixelTransformParameterfvEXT(int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glPixelTransformParameterfvEXT");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glPixelTransformParameterfvEXT(int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glPixelTransformParameterfvEXT");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glPixelTransformParameteriEXT(int target, int pname, int param)
    {
        CallDetails details = getMethodDetails("glPixelTransformParameteriEXT");
        details.foundArguments.add(new Object[] { target, pname, param });
    }

    @Override
    public void glPixelTransformParameterivEXT(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glPixelTransformParameterivEXT");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glPixelTransformParameterivEXT(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glPixelTransformParameterivEXT");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glPixelZoom(float xfactor, float yfactor)
    {
        CallDetails details = getMethodDetails("glPixelZoom");
        details.foundArguments.add(new Object[] { xfactor, yfactor });
    }

    @Override
    public void glPolygonOffsetClampEXT(float factor, float units, float clamp)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPolygonStipple(ByteBuffer mask)
    {
        CallDetails details = getMethodDetails("glPolygonStipple");
        details.foundArguments.add(new Object[] { mask });
    }

    @Override
    public void glPolygonStipple(byte[] mask, int mask_offset)
    {
        CallDetails details = getMethodDetails("glPolygonStipple");
        details.foundArguments.add(new Object[] { mask, mask_offset });
    }

    @Override
    public void glPolygonStipple(long mask_buffer_offset)
    {
        CallDetails details = getMethodDetails("glPolygonStipple");
        details.foundArguments.add(new Object[] { mask_buffer_offset });
    }

    @Override
    public void glPopAttrib()
    {
        CallDetails details = getMethodDetails("glPopAttrib");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glPopClientAttrib()
    {
        CallDetails details = getMethodDetails("glPopClientAttrib");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glPopName()
    {
        CallDetails details = getMethodDetails("glPopName");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glPrimitiveRestartIndexNV(int index)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPrimitiveRestartNV()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPrioritizeTextures(int n, IntBuffer textures, FloatBuffer priorities)
    {
        CallDetails details = getMethodDetails("glPrioritizeTextures");
        details.foundArguments.add(new Object[] { n, textures, priorities });
    }

    @Override
    public void glPrioritizeTextures(int n, int[] textures, int textures_offset, float[] priorities, int priorities_offset)
    {
        CallDetails details = getMethodDetails("glPrioritizeTextures");
        details.foundArguments.add(new Object[] { n, textures, textures_offset, priorities, priorities_offset });
    }

    @Override
    public void glProgramBufferParametersIivNV(int target, int bindingIndex, int wordIndex, int count, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramBufferParametersIivNV(int target, int bindingIndex, int wordIndex, int count, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramBufferParametersIuivNV(int target, int bindingIndex, int wordIndex, int count, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramBufferParametersIuivNV(int target, int bindingIndex, int wordIndex, int count, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramBufferParametersfvNV(int target, int bindingIndex, int wordIndex, int count, FloatBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramBufferParametersfvNV(int target, int bindingIndex, int wordIndex, int count, float[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramEnvParameter4dARB(int target, int index, double x, double y, double z, double w)
    {
        CallDetails details = getMethodDetails("glProgramEnvParameter4dARB");
        details.foundArguments.add(new Object[] { target, index, x, y, z, w });
    }

    @Override
    public void glProgramEnvParameter4dvARB(int target, int index, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glProgramEnvParameter4dvARB");
        details.foundArguments.add(new Object[] { target, index, params });
    }

    @Override
    public void glProgramEnvParameter4dvARB(int target, int index, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glProgramEnvParameter4dvARB");
        details.foundArguments.add(new Object[] { target, index, params, params_offset });
    }

    @Override
    public void glProgramEnvParameter4fARB(int target, int index, float x, float y, float z, float w)
    {
        CallDetails details = getMethodDetails("glProgramEnvParameter4fARB");
        details.foundArguments.add(new Object[] { target, index, x, y, z, w });
    }

    @Override
    public void glProgramEnvParameter4fvARB(int target, int index, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glProgramEnvParameter4fvARB");
        details.foundArguments.add(new Object[] { target, index, params });
    }

    @Override
    public void glProgramEnvParameter4fvARB(int target, int index, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glProgramEnvParameter4fvARB");
        details.foundArguments.add(new Object[] { target, index, params, params_offset });
    }

    @Override
    public void glProgramEnvParameterI4iNV(int target, int index, int x, int y, int z, int w)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramEnvParameterI4ivNV(int target, int index, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramEnvParameterI4ivNV(int target, int index, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramEnvParameterI4uiNV(int target, int index, int x, int y, int z, int w)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramEnvParameterI4uivNV(int target, int index, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramEnvParameterI4uivNV(int target, int index, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramEnvParameters4fvEXT(int target, int index, int count, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glProgramEnvParameters4fvEXT");
        details.foundArguments.add(new Object[] { target, index, count, params });
    }

    @Override
    public void glProgramEnvParameters4fvEXT(int target, int index, int count, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glProgramEnvParameters4fvEXT");
        details.foundArguments.add(new Object[] { target, index, count, params, params_offset });
    }

    @Override
    public void glProgramEnvParametersI4ivNV(int target, int index, int count, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramEnvParametersI4ivNV(int target, int index, int count, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramEnvParametersI4uivNV(int target, int index, int count, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramEnvParametersI4uivNV(int target, int index, int count, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramLocalParameter4dARB(int target, int index, double x, double y, double z, double w)
    {
        CallDetails details = getMethodDetails("glProgramLocalParameter4dARB");
        details.foundArguments.add(new Object[] { target, index, x, y, z, w });
    }

    @Override
    public void glProgramLocalParameter4dvARB(int target, int index, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glProgramLocalParameter4dvARB");
        details.foundArguments.add(new Object[] { target, index, params });
    }

    @Override
    public void glProgramLocalParameter4dvARB(int target, int index, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glProgramLocalParameter4dvARB");
        details.foundArguments.add(new Object[] { target, index, params, params_offset });
    }

    @Override
    public void glProgramLocalParameter4fARB(int target, int index, float x, float y, float z, float w)
    {
        CallDetails details = getMethodDetails("glProgramLocalParameter4fARB");
        details.foundArguments.add(new Object[] { target, index, x, y, z, w });
    }

    @Override
    public void glProgramLocalParameter4fvARB(int target, int index, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glProgramLocalParameter4fvARB");
        details.foundArguments.add(new Object[] { target, index, params });
    }

    @Override
    public void glProgramLocalParameter4fvARB(int target, int index, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glProgramLocalParameter4fvARB");
        details.foundArguments.add(new Object[] { target, index, params, params_offset });
    }

    @Override
    public void glProgramLocalParameterI4iNV(int target, int index, int x, int y, int z, int w)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramLocalParameterI4ivNV(int target, int index, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramLocalParameterI4ivNV(int target, int index, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramLocalParameterI4uiNV(int target, int index, int x, int y, int z, int w)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramLocalParameterI4uivNV(int target, int index, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramLocalParameterI4uivNV(int target, int index, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramLocalParameters4fvEXT(int target, int index, int count, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glProgramLocalParameters4fvEXT");
        details.foundArguments.add(new Object[] { target, index, count, params });
    }

    @Override
    public void glProgramLocalParameters4fvEXT(int target, int index, int count, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glProgramLocalParameters4fvEXT");
        details.foundArguments.add(new Object[] { target, index, count, params, params_offset });
    }

    @Override
    public void glProgramLocalParametersI4ivNV(int target, int index, int count, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramLocalParametersI4ivNV(int target, int index, int count, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramLocalParametersI4uivNV(int target, int index, int count, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramLocalParametersI4uivNV(int target, int index, int count, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramStringARB(int target, int format, int len, String string)
    {
        CallDetails details = getMethodDetails("glProgramStringARB");
        details.foundArguments.add(new Object[] { target, format, len, string });
    }

    @Override
    public void glProgramSubroutineParametersuivNV(int target, int count, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramSubroutineParametersuivNV(int target, int count, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramUniform1dEXT(int program, int location, double x)
    {

    }

    @Override
    public void glProgramUniform1dvEXT(int program, int location, int count, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniform1dvEXT(int program, int location, int count, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform1i64NV(int program, int location, long x)
    {

    }

    @Override
    public void glProgramUniform1i64vNV(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform1i64vNV(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform1ui64NV(int program, int location, long x)
    {

    }

    @Override
    public void glProgramUniform1ui64vNV(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform1ui64vNV(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform2dEXT(int program, int location, double x, double y)
    {

    }

    @Override
    public void glProgramUniform2dvEXT(int program, int location, int count, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniform2dvEXT(int program, int location, int count, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform2i64NV(int program, int location, long x, long y)
    {

    }

    @Override
    public void glProgramUniform2i64vNV(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform2i64vNV(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform2ui64NV(int program, int location, long x, long y)
    {

    }

    @Override
    public void glProgramUniform2ui64vNV(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform2ui64vNV(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform3dEXT(int program, int location, double x, double y, double z)
    {

    }

    @Override
    public void glProgramUniform3dvEXT(int program, int location, int count, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniform3dvEXT(int program, int location, int count, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform3i64NV(int program, int location, long x, long y, long z)
    {

    }

    @Override
    public void glProgramUniform3i64vNV(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform3i64vNV(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform3ui64NV(int program, int location, long x, long y, long z)
    {

    }

    @Override
    public void glProgramUniform3ui64vNV(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform3ui64vNV(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform4dEXT(int program, int location, double x, double y, double z, double w)
    {

    }

    @Override
    public void glProgramUniform4dvEXT(int program, int location, int count, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniform4dvEXT(int program, int location, int count, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform4i64NV(int program, int location, long x, long y, long z, long w)
    {

    }

    @Override
    public void glProgramUniform4i64vNV(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform4i64vNV(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform4ui64NV(int program, int location, long x, long y, long z, long w)
    {

    }

    @Override
    public void glProgramUniform4ui64vNV(int program, int location, int count, LongBuffer value)
    {

    }

    @Override
    public void glProgramUniform4ui64vNV(int program, int location, int count, long[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix2dvEXT(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix2dvEXT(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix2x3dvEXT(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix2x3dvEXT(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix2x4dvEXT(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix2x4dvEXT(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix3dvEXT(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix3dvEXT(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix3x2dvEXT(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix3x2dvEXT(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix3x4dvEXT(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix3x4dvEXT(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix4dvEXT(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix4dvEXT(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix4x2dvEXT(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix4x2dvEXT(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix4x3dvEXT(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix4x3dvEXT(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramVertexLimitNV(int target, int limit)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProvokingVertexEXT(int mode)
    {
        CallDetails details = getMethodDetails("glProvokingVertexEXT");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public void glPushAttrib(int mask)
    {
        CallDetails details = getMethodDetails("glPushAttrib");
        details.foundArguments.add(new Object[] { mask });
    }

    @Override
    public void glPushClientAttrib(int mask)
    {
        CallDetails details = getMethodDetails("glPushClientAttrib");
        details.foundArguments.add(new Object[] { mask });
    }

    @Override
    public void glPushClientAttribDefaultEXT(int mask)
    {
        CallDetails details = getMethodDetails("glPushClientAttribDefaultEXT");
        details.foundArguments.add(new Object[] { mask });
    }

    @Override
    public void glPushName(int name)
    {
        CallDetails details = getMethodDetails("glPushName");
        details.foundArguments.add(new Object[] { name });
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
    public void glQueryObjectParameteruiAMD(int target, int id, int pname, int param)
    {

    }

    @Override
    public void glRasterPos2d(double x, double y)
    {
        CallDetails details = getMethodDetails("glRasterPos2d");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glRasterPos2dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glRasterPos2dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glRasterPos2dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glRasterPos2dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glRasterPos2f(float x, float y)
    {
        CallDetails details = getMethodDetails("glRasterPos2f");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glRasterPos2fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glRasterPos2fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glRasterPos2fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glRasterPos2fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glRasterPos2i(int x, int y)
    {
        CallDetails details = getMethodDetails("glRasterPos2i");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glRasterPos2iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glRasterPos2iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glRasterPos2iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glRasterPos2iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glRasterPos2s(short x, short y)
    {
        CallDetails details = getMethodDetails("glRasterPos2s");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glRasterPos2sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glRasterPos2sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glRasterPos2sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glRasterPos2sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glRasterPos3d(double x, double y, double z)
    {
        CallDetails details = getMethodDetails("glRasterPos3d");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glRasterPos3dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glRasterPos3dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glRasterPos3dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glRasterPos3dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glRasterPos3f(float x, float y, float z)
    {
        CallDetails details = getMethodDetails("glRasterPos3f");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glRasterPos3fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glRasterPos3fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glRasterPos3fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glRasterPos3fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glRasterPos3i(int x, int y, int z)
    {
        CallDetails details = getMethodDetails("glRasterPos3i");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glRasterPos3iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glRasterPos3iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glRasterPos3iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glRasterPos3iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glRasterPos3s(short x, short y, short z)
    {
        CallDetails details = getMethodDetails("glRasterPos3s");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glRasterPos3sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glRasterPos3sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glRasterPos3sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glRasterPos3sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glRasterPos4d(double x, double y, double z, double w)
    {
        CallDetails details = getMethodDetails("glRasterPos4d");
        details.foundArguments.add(new Object[] { x, y, z, w });
    }

    @Override
    public void glRasterPos4dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glRasterPos4dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glRasterPos4dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glRasterPos4dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glRasterPos4f(float x, float y, float z, float w)
    {
        CallDetails details = getMethodDetails("glRasterPos4f");
        details.foundArguments.add(new Object[] { x, y, z, w });
    }

    @Override
    public void glRasterPos4fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glRasterPos4fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glRasterPos4fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glRasterPos4fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glRasterPos4i(int x, int y, int z, int w)
    {
        CallDetails details = getMethodDetails("glRasterPos4i");
        details.foundArguments.add(new Object[] { x, y, z, w });
    }

    @Override
    public void glRasterPos4iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glRasterPos4iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glRasterPos4iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glRasterPos4iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glRasterPos4s(short x, short y, short z, short w)
    {
        CallDetails details = getMethodDetails("glRasterPos4s");
        details.foundArguments.add(new Object[] { x, y, z, w });
    }

    @Override
    public void glRasterPos4sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glRasterPos4sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glRasterPos4sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glRasterPos4sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glRasterSamplesEXT(int samples, boolean fixedsamplelocations)
    {

    }

    @Override
    public void glReadBuffer(int mode)
    {
        CallDetails details = getMethodDetails("glReadBuffer");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public void glResumeTransformFeedback()
    {

    }

    @Override
    public void glTexStorage2DMultisample(int target, int samples, int internalformat, int width, int height, boolean fixedsamplelocations)
    {

    }

    @Override
    public void glRectd(double x1, double y1, double x2, double y2)
    {
        CallDetails details = getMethodDetails("glRectd");
        details.foundArguments.add(new Object[] { x1, y1, x2, y2 });
    }

    @Override
    public void glRectdv(DoubleBuffer v1, DoubleBuffer v2)
    {
        CallDetails details = getMethodDetails("glRectdv");
        details.foundArguments.add(new Object[] { v1, v2 });
    }

    @Override
    public void glRectdv(double[] v1, int v1_offset, double[] v2, int v2_offset)
    {
        CallDetails details = getMethodDetails("glRectdv");
        details.foundArguments.add(new Object[] { v1, v1_offset, v2, v2_offset });
    }

    @Override
    public void glRectf(float x1, float y1, float x2, float y2)
    {
        CallDetails details = getMethodDetails("glRectf");
        details.foundArguments.add(new Object[] { x1, y1, x2, y2 });
    }

    @Override
    public void glRectfv(FloatBuffer v1, FloatBuffer v2)
    {
        CallDetails details = getMethodDetails("glRectfv");
        details.foundArguments.add(new Object[] { v1, v2 });
    }

    @Override
    public void glRectfv(float[] v1, int v1_offset, float[] v2, int v2_offset)
    {
        CallDetails details = getMethodDetails("glRectfv");
        details.foundArguments.add(new Object[] { v1, v1_offset, v2, v2_offset });
    }

    @Override
    public void glRecti(int x1, int y1, int x2, int y2)
    {
        CallDetails details = getMethodDetails("glRecti");
        details.foundArguments.add(new Object[] { x1, y1, x2, y2 });
    }

    @Override
    public void glRectiv(IntBuffer v1, IntBuffer v2)
    {
        CallDetails details = getMethodDetails("glRectiv");
        details.foundArguments.add(new Object[] { v1, v2 });
    }

    @Override
    public void glRectiv(int[] v1, int v1_offset, int[] v2, int v2_offset)
    {
        CallDetails details = getMethodDetails("glRectiv");
        details.foundArguments.add(new Object[] { v1, v1_offset, v2, v2_offset });
    }

    @Override
    public void glRects(short x1, short y1, short x2, short y2)
    {
        CallDetails details = getMethodDetails("glRects");
        details.foundArguments.add(new Object[] { x1, y1, x2, y2 });
    }

    @Override
    public void glRectsv(ShortBuffer v1, ShortBuffer v2)
    {
        CallDetails details = getMethodDetails("glRectsv");
        details.foundArguments.add(new Object[] { v1, v2 });
    }

    @Override
    public void glRectsv(short[] v1, int v1_offset, short[] v2, int v2_offset)
    {
        CallDetails details = getMethodDetails("glRectsv");
        details.foundArguments.add(new Object[] { v1, v1_offset, v2, v2_offset });
    }

    @Override
    public int glRenderMode(int mode)
    {
        return 0;
    }

    @Override
    public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height)
    {
        CallDetails details = getMethodDetails("glRenderbufferStorageMultisample");
        details.foundArguments.add(new Object[] { target, samples, internalformat, width, height });
    }

    @Override
    public void glRenderbufferStorageMultisampleCoverageNV(int target, int coverageSamples, int colorSamples, int internalformat, int width, int height)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glResetHistogram(int target)
    {
        CallDetails details = getMethodDetails("glResetHistogram");
        details.foundArguments.add(new Object[] { target });
    }

    @Override
    public void glResetMinmax(int target)
    {
        CallDetails details = getMethodDetails("glResetMinmax");
        details.foundArguments.add(new Object[] { target });
    }

    @Override
    public void glResolveDepthValuesNV()
    {

    }

    @Override
    public void glResumeTransformFeedbackNV()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glRotated(double angle, double x, double y, double z)
    {
        CallDetails details = getMethodDetails("glRotated");
        details.foundArguments.add(new Object[] { angle, x, y, z });
    }

    @Override
    public void glSampleMaskIndexedNV(int index, int mask)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glScaled(double x, double y, double z)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glSecondaryColor3b(byte red, byte green, byte blue)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3b");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glSecondaryColor3bv(ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3bv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glSecondaryColor3bv(byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3bv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glSecondaryColor3d(double red, double green, double blue)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3d");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glSecondaryColor3dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glSecondaryColor3dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glSecondaryColor3f(float red, float green, float blue)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3f");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glSecondaryColor3fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glSecondaryColor3fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glSecondaryColor3h(short red, short green, short blue)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3h");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glSecondaryColor3hv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3hv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glSecondaryColor3hv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3hv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glSecondaryColor3i(int red, int green, int blue)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3i");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glSecondaryColor3iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glSecondaryColor3iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glSecondaryColor3s(short red, short green, short blue)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3s");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glSecondaryColor3sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glSecondaryColor3sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glSecondaryColor3ub(byte red, byte green, byte blue)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3ub");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glSecondaryColor3ubv(ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3ubv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glSecondaryColor3ubv(byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3ubv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glSecondaryColor3ui(int red, int green, int blue)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3ui");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glSecondaryColor3uiv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3uiv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glSecondaryColor3uiv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3uiv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glSecondaryColor3us(short red, short green, short blue)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3us");
        details.foundArguments.add(new Object[] { red, green, blue });
    }

    @Override
    public void glSecondaryColor3usv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3usv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glSecondaryColor3usv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glSecondaryColor3usv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glSecondaryColorPointer(int size, int type, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glSecondaryColorPointer");
        details.foundArguments.add(new Object[] { size, type, stride, pointer });
    }

    @Override
    public void glSecondaryColorPointer(int size, int type, int stride, long pointer_buffer_offset)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { size, type, stride, pointer_buffer_offset });
    }

    @Override
    public void glSelectBuffer(int size, IntBuffer buffer)
    {
        CallDetails details = getMethodDetails("glSelectBuffer");
        details.foundArguments.add(new Object[] { size, buffer });
    }

    @Override
    public void glSelectPerfMonitorCountersAMD(int monitor, boolean enable, int group, int numCounters, IntBuffer counterList)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glSelectPerfMonitorCountersAMD(int monitor, boolean enable, int group, int numCounters, int[] counterList, int counterList_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type, Buffer row, Buffer column)
    {
        CallDetails details = getMethodDetails("glSeparableFilter2D");
        details.foundArguments.add(new Object[] { target, internalformat, width, height, format, type, row, column });
    }

    @Override
    public void glSeparableFilter2D(int target, int internalformat, int width, int height, int format, int type, long row_buffer_offset, long column_buffer_offset)
    {
        CallDetails details = getMethodDetails("glSeparableFilter2D");
        details.foundArguments.add(new Object[] { target, internalformat, width, height, format, type, row_buffer_offset, column_buffer_offset });
    }

    @Override
    public void glSetInvariantEXT(int id, int type, Buffer addr)
    {
        CallDetails details = getMethodDetails("glSetInvariantEXT");
        details.foundArguments.add(new Object[] { id, type, addr });
    }

    @Override
    public void glSetLocalConstantEXT(int id, int type, Buffer addr)
    {
        CallDetails details = getMethodDetails("glSetLocalConstantEXT");
        details.foundArguments.add(new Object[] { id, type, addr });
    }

    @Override
    public void glShaderOp1EXT(int op, int res, int arg1)
    {
        CallDetails details = getMethodDetails("glShaderOp1EXT");
        details.foundArguments.add(new Object[] { op, res, arg1 });
    }

    @Override
    public void glShaderOp2EXT(int op, int res, int arg1, int arg2)
    {
        CallDetails details = getMethodDetails("glShaderOp2EXT");
        details.foundArguments.add(new Object[] { op, res, arg1, arg2 });
    }

    @Override
    public void glShaderOp3EXT(int op, int res, int arg1, int arg2, int arg3)
    {
        CallDetails details = getMethodDetails("glShaderOp3EXT");
        details.foundArguments.add(new Object[] { op, res, arg1, arg2, arg3 });
    }

    @Override
    public void glShaderSourceARB(long shaderObj, int count, String[] string, IntBuffer length)
    {
        CallDetails details = getMethodDetails("glShaderSourceARB");
        details.foundArguments.add(new Object[] { shaderObj, count, string, length });
    }

    @Override
    public void glShaderSourceARB(long shaderObj, int count, String[] string, int[] length, int length_offset)
    {
        CallDetails details = getMethodDetails("glShaderSourceARB");
        details.foundArguments.add(new Object[] { shaderObj, count, string, length, length_offset });
    }

    @Override
    public void glStencilClearTagEXT(int stencilTagBits, int stencilClearTag)
    {
        CallDetails details = getMethodDetails("glStencilClearTagEXT");
        details.foundArguments.add(new Object[] { stencilTagBits, stencilClearTag });
    }

    @Override
    public void glStringMarkerGREMEDY(int len, Buffer string)
    {
        CallDetails details = getMethodDetails("glStringMarkerGREMEDY");
        details.foundArguments.add(new Object[] { len, string });
    }

    @Override
    public void glSubpixelPrecisionBiasNV(int xbits, int ybits)
    {

    }

    @Override
    public void glConservativeRasterParameterfNV(int pname, float value)
    {

    }

    @Override
    public void glSwizzleEXT(int res, int in, int outX, int outY, int outZ, int outW)
    {
        CallDetails details = getMethodDetails("glSwizzleEXT");
        details.foundArguments.add(new Object[] { res, in, outX, outY, outZ, outW });
    }

    @Override
    public void glSyncTextureINTEL(int texture)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexCoord1bOES(byte s)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexCoord1bvOES(ByteBuffer coords)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexCoord1bvOES(byte[] coords, int coords_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexCoord1d(double s)
    {
        CallDetails details = getMethodDetails("glTexCoord1d");
        details.foundArguments.add(new Object[] { s });
    }

    @Override
    public void glTexCoord1dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord1dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord1dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord1dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord1f(float s)
    {
        CallDetails details = getMethodDetails("glTexCoord1f");
        details.foundArguments.add(new Object[] { s });
    }

    @Override
    public void glTexCoord1fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord1fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord1fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord1fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord1h(short s)
    {
        CallDetails details = getMethodDetails("glTexCoord1h");
        details.foundArguments.add(new Object[] { s });
    }

    @Override
    public void glTexCoord1hv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord1hv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord1hv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord1hv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord1i(int s)
    {
        CallDetails details = getMethodDetails("glTexCoord1i");
        details.foundArguments.add(new Object[] { s });
    }

    @Override
    public void glTexCoord1iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord1iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord1iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord1iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord1s(short s)
    {
        CallDetails details = getMethodDetails("glTexCoord1s");
        details.foundArguments.add(new Object[] { s });
    }

    @Override
    public void glTexCoord1sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord1sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord1sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord1sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord2bOES(byte s, byte t)
    {
        CallDetails details = getMethodDetails("glTexCoord2bOES");
        details.foundArguments.add(new Object[] { s, t });
    }

    @Override
    public void glTexCoord2bvOES(ByteBuffer coords)
    {
        CallDetails details = getMethodDetails("glTexCoord2bvOES");
        details.foundArguments.add(new Object[] { coords });
    }

    @Override
    public void glTexCoord2bvOES(byte[] coords, int coords_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord2bvOES");
        details.foundArguments.add(new Object[] { coords, coords_offset });
    }

    @Override
    public void glTexCoord2d(double s, double t)
    {
        CallDetails details = getMethodDetails("glTexCoord2d");
        details.foundArguments.add(new Object[] { s, t });
    }

    @Override
    public void glTexCoord2dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord2dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord2dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord2dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord2f(float s, float t)
    {
        CallDetails details = getMethodDetails("glTexCoord2f");
        details.foundArguments.add(new Object[] { s, t });
    }

    @Override
    public void glTexCoord2fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord2fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord2fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord2fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord2h(short s, short t)
    {
        CallDetails details = getMethodDetails("glTexCoord2h");
        details.foundArguments.add(new Object[] { s, t });
    }

    @Override
    public void glTexCoord2hv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord2hv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord2hv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord2hv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord2i(int s, int t)
    {
        CallDetails details = getMethodDetails("glTexCoord2i");
        details.foundArguments.add(new Object[] { s, t });
    }

    @Override
    public void glTexCoord2iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord2iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord2iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord2iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord2s(short s, short t)
    {
        CallDetails details = getMethodDetails("glTexCoord2s");
        details.foundArguments.add(new Object[] { s, t });
    }

    @Override
    public void glTexCoord2sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord2sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord2sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord2sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord3bOES(byte s, byte t, byte r)
    {
        CallDetails details = getMethodDetails("glTexCoord3bOES");
        details.foundArguments.add(new Object[] { s, t, r });
    }

    @Override
    public void glTexCoord3bvOES(ByteBuffer coords)
    {
        CallDetails details = getMethodDetails("glTexCoord3bOES");
        details.foundArguments.add(new Object[] { coords });
    }

    @Override
    public void glTexCoord3bvOES(byte[] coords, int coords_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord3bOES");
        details.foundArguments.add(new Object[] { coords, coords_offset });
    }

    @Override
    public void glTexCoord3d(double s, double t, double r)
    {
        CallDetails details = getMethodDetails("glTexCoord3d");
        details.foundArguments.add(new Object[] { s, t, r });
    }

    @Override
    public void glTexCoord3dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord3dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord3dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord3dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord3f(float s, float t, float r)
    {
        CallDetails details = getMethodDetails("glTexCoord3f");
        details.foundArguments.add(new Object[] { s, t, r });
    }

    @Override
    public void glTexCoord3fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord3fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord3fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord3fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord3h(short s, short t, short r)
    {
        CallDetails details = getMethodDetails("glTexCoord3h");
        details.foundArguments.add(new Object[] { s, t, r });
    }

    @Override
    public void glTexCoord3hv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord3hv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord3hv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord3hv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord3i(int s, int t, int r)
    {
        CallDetails details = getMethodDetails("glTexCoord3i");
        details.foundArguments.add(new Object[] { s, t, r });
    }

    @Override
    public void glTexCoord3iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord3iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord3iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord3iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord3s(short s, short t, short r)
    {
        CallDetails details = getMethodDetails("glTexCoord3s");
        details.foundArguments.add(new Object[] { s, t, r });
    }

    @Override
    public void glTexCoord3sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord3sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord3sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord3sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord4bOES(byte s, byte t, byte r, byte q)
    {
        CallDetails details = getMethodDetails("glTexCoord4bOES");
        details.foundArguments.add(new Object[] { s, t, r, q });
    }

    @Override
    public void glTexCoord4bvOES(ByteBuffer coords)
    {
        CallDetails details = getMethodDetails("glTexCoord4bvOES");
        details.foundArguments.add(new Object[] { coords });
    }

    @Override
    public void glTexCoord4bvOES(byte[] coords, int coords_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord4bvOES");
        details.foundArguments.add(new Object[] { coords, coords_offset });
    }

    @Override
    public void glTexCoord4d(double s, double t, double r, double q)
    {
        CallDetails details = getMethodDetails("glTexCoord4d");
        details.foundArguments.add(new Object[] { s, t, r, q });
    }

    @Override
    public void glTexCoord4dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord4dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord4dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord4dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord4f(float s, float t, float r, float q)
    {
        CallDetails details = getMethodDetails("glTexCoord4f");
        details.foundArguments.add(new Object[] { s, t, r, q });
    }

    @Override
    public void glTexCoord4fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord4fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord4fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord4fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord4h(short s, short t, short r, short q)
    {
        CallDetails details = getMethodDetails("glTexCoord4h");
        details.foundArguments.add(new Object[] { s, t, r, q });
    }

    @Override
    public void glTexCoord4hv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord4hv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord4hv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord4hv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord4i(int s, int t, int r, int q)
    {
        CallDetails details = getMethodDetails("glTexCoord4i");
        details.foundArguments.add(new Object[] { s, t, r, q });
    }

    @Override
    public void glTexCoord4iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord4iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord4iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord4iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexCoord4s(short s, short t, short r, short q)
    {
        CallDetails details = getMethodDetails("glTexCoord4s");
        details.foundArguments.add(new Object[] { s, t, r, q });
    }

    @Override
    public void glTexCoord4sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glTexCoord4sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glTexCoord4sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glTexCoord4sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glTexGend(int coord, int pname, double param)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { coord, pname, param });
    }

    @Override
    public void glTexGendv(int coord, int pname, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { coord, pname, params });
    }

    @Override
    public void glTexGendv(int coord, int pname, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glAccum");
        details.foundArguments.add(new Object[] { coord, pname, params, params_offset });
    }

    @Override
    public void glTexRenderbufferNV(int target, int renderbuffer)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexStorageSparseAMD(int target, int internalFormat, int width, int height, int depth, int layers, int flags)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTextureBarrierNV()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTextureBufferEXT(int texture, int target, int internalformat, int buffer)
    {
        CallDetails details = getMethodDetails("glTextureBufferEXT");
        details.foundArguments.add(new Object[] { texture, target, internalformat, buffer });
    }

    @Override
    public void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width, int border, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glTextureImage1DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, internalformat, width, border, format, type, pixels });
    }

    @Override
    public void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width, int border, int format, int type, long pixels_buffer_offset)
    {

    }

    @Override
    public void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glTextureImage2DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, internalformat, width, height, border, format, type, pixels });
    }

    @Override
    public void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width, int height, int border, int format, int type, long pixels_buffer_offset)
    {

    }

    @Override
    public void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glTextureImage2DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, internalformat, width, height, depth, border, format, type, pixels });
    }

    @Override
    public void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, long pixels_buffer_offset)
    {

    }

    @Override
    public void glTextureLightEXT(int pname)
    {
        CallDetails details = getMethodDetails("glTextureLightEXT");
        details.foundArguments.add(new Object[] { pname });
    }

    @Override
    public void glTextureMaterialEXT(int face, int mode)
    {
        CallDetails details = getMethodDetails("glTextureMaterialEXT");
        details.foundArguments.add(new Object[] { face, mode });
    }

    @Override
    public void glTextureNormalEXT(int mode)
    {
        CallDetails details = getMethodDetails("glTextureNormalEXT");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public void glTexturePageCommitmentEXT(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, boolean resident)
    {
        CallDetails details = getMethodDetails("glTextureParameterIivEXT");
        details.foundArguments.add(new Object[] { texture, level, xoffset, yoffset, zoffset, width, height, depth, resident });
    }

    @Override
    public void glTextureParameterIivEXT(int texture, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glTextureParameterIivEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params });
    }

    @Override
    public void glTextureParameterIivEXT(int texture, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glTextureParameterIivEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params, params_offset });
    }

    @Override
    public void glTextureParameterIuivEXT(int texture, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glTextureParameterIuivEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params });
    }

    @Override
    public void glTextureParameterIuivEXT(int texture, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glTextureParameterIuivEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params, params_offset });
    }

    @Override
    public void glTextureParameterfEXT(int texture, int target, int pname, float param)
    {
        CallDetails details = getMethodDetails("glTextureParameterfEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, param });
    }

    @Override
    public void glTextureParameterfvEXT(int texture, int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glTextureParameterfvEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params });
    }

    @Override
    public void glTextureParameterfvEXT(int texture, int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glTextureParameterfvEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params, params_offset });
    }

    @Override
    public void glTextureParameteriEXT(int texture, int target, int pname, int param)
    {
        CallDetails details = getMethodDetails("glTextureParameteriEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, param });
    }

    @Override
    public void glTextureParameterivEXT(int texture, int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glTextureParameterivEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params });
    }

    @Override
    public void glTextureParameterivEXT(int texture, int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glTextureParameterivEXT");
        details.foundArguments.add(new Object[] { texture, target, pname, params, params_offset });
    }

    @Override
    public void glTextureRangeAPPLE(int target, int length, Buffer pointer)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTextureRenderbufferEXT(int texture, int target, int renderbuffer)
    {
        CallDetails details = getMethodDetails("glTextureRenderbufferEXT");
        details.foundArguments.add(new Object[] { texture, target, renderbuffer });
    }

    @Override
    public void glTextureStorage2DMultisampleEXT(int texture, int target, int samples, int internalformat, int width, int height, boolean fixedsamplelocations)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTextureStorage3DMultisampleEXT(int texture, int target, int samples, int internalformat, int width, int height, int depth, boolean fixedsamplelocations)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTextureStorageSparseAMD(int texture, int target, int internalFormat, int width, int height, int depth, int layers, int flags)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glTextureSubImage1DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, xoffset, width, format, type, pixels });
    }

    @Override
    public void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format, int type, long pixels_buffer_offset)
    {

    }

    @Override
    public void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glTextureSubImage2DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, xoffset, yoffset, width, height, format, type, pixels });
    }

    @Override
    public void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, long pixels_buffer_offset)
    {

    }

    @Override
    public void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glTextureSubImage3DEXT");
        details.foundArguments.add(new Object[] { texture, target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels });
    }

    @Override
    public void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, long pixels_buffer_offset)
    {

    }

    @Override
    public void glTransformFeedbackVaryings(int program, int count, String[] varyings, int bufferMode)
    {
        CallDetails details = getMethodDetails("glTransformFeedbackVaryings");
        details.foundArguments.add(new Object[] { program, count, varyings, bufferMode });
    }

    @Override
    public void glTranslated(double x, double y, double z)
    {
        CallDetails details = getMethodDetails("glTranslated");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glUniform1fARB(int location, float v0)
    {
        CallDetails details = getMethodDetails("glUniform1fARB");
        details.foundArguments.add(new Object[] { location, v0 });
    }

    @Override
    public void glUniform1fvARB(int location, int count, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniform1fvARB");
        details.foundArguments.add(new Object[] { location, count, value });
    }

    @Override
    public void glUniform1fvARB(int location, int count, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniform1fvARB");
        details.foundArguments.add(new Object[] { location, count, value, value_offset });
    }

    @Override
    public void glUniform1i64NV(int location, long x)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform1i64vNV(int location, int count, LongBuffer value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform1i64vNV(int location, int count, long[] value, int value_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform1iARB(int location, int v0)
    {
        CallDetails details = getMethodDetails("glUniform1iARB");
        details.foundArguments.add(new Object[] { location, v0 });
    }

    @Override
    public void glUniform1ivARB(int location, int count, IntBuffer value)
    {
        CallDetails details = getMethodDetails("glUniform1ivARB");
        details.foundArguments.add(new Object[] { location, count, value });
    }

    @Override
    public void glUniform1ivARB(int location, int count, int[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniform1ivARB");
        details.foundArguments.add(new Object[] { location, count, value, value_offset });
    }

    @Override
    public void glUniform1ui64NV(int location, long x)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform1ui64vNV(int location, int count, LongBuffer value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform1ui64vNV(int location, int count, long[] value, int value_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform1ui(int location, int v0)
    {
        CallDetails details = getMethodDetails("glUniform1ui");
        details.foundArguments.add(new Object[] { location, v0 });
    }

    @Override
    public void glUniform1uiv(int location, int count, IntBuffer value)
    {
        CallDetails details = getMethodDetails("glUniform1uiv");
        details.foundArguments.add(new Object[] { location, count, value });
    }

    @Override
    public void glUniform1uiv(int location, int count, int[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniform1uiv");
        details.foundArguments.add(new Object[] { location, count, value, value_offset });
    }

    @Override
    public void glUniform2fARB(int location, float v0, float v1)
    {
        CallDetails details = getMethodDetails("glUniform2fARB");
        details.foundArguments.add(new Object[] { location, v0, v1 });
    }

    @Override
    public void glUniform2fvARB(int location, int count, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniform2fvARB");
        details.foundArguments.add(new Object[] { location, count, value });
    }

    @Override
    public void glUniform2fvARB(int location, int count, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniform2fvARB");
        details.foundArguments.add(new Object[] { location, count, value, value_offset });
    }

    @Override
    public void glUniform2i64NV(int location, long x, long y)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform2i64vNV(int location, int count, LongBuffer value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform2i64vNV(int location, int count, long[] value, int value_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform2iARB(int location, int v0, int v1)
    {
        CallDetails details = getMethodDetails("glUniform2iARB");
        details.foundArguments.add(new Object[] { location, v0, v1 });
    }

    @Override
    public void glUniform2ivARB(int location, int count, IntBuffer value)
    {
        CallDetails details = getMethodDetails("glUniform2ivARB");
        details.foundArguments.add(new Object[] { location, count, value });
    }

    @Override
    public void glUniform2ivARB(int location, int count, int[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniform2ivARB");
        details.foundArguments.add(new Object[] { location, count, value, value_offset });
    }

    @Override
    public void glUniform2ui64NV(int location, long x, long y)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform2ui64vNV(int location, int count, LongBuffer value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform2ui64vNV(int location, int count, long[] value, int value_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform2ui(int location, int v0, int v1)
    {
        CallDetails details = getMethodDetails("glUniform2ui");
        details.foundArguments.add(new Object[] { location, v0, v1 });
    }

    @Override
    public void glUniform2uiv(int location, int count, IntBuffer value)
    {
        CallDetails details = getMethodDetails("glUniform2uiv");
        details.foundArguments.add(new Object[] { location, count, value });
    }

    @Override
    public void glUniform2uiv(int location, int count, int[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniform2uiv");
        details.foundArguments.add(new Object[] { location, count, value, value_offset });
    }

    @Override
    public void glUniform3fARB(int location, float v0, float v1, float v2)
    {
        CallDetails details = getMethodDetails("glUniform3fARB");
        details.foundArguments.add(new Object[] { location, v0, v1, v2 });
    }

    @Override
    public void glUniform3fvARB(int location, int count, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniform3fvARB");
        details.foundArguments.add(new Object[] { location, count, value });
    }

    @Override
    public void glUniform3fvARB(int location, int count, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniform3fvARB");
        details.foundArguments.add(new Object[] { location, count, value, value_offset });
    }

    @Override
    public void glUniform3i64NV(int location, long x, long y, long z)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform3i64vNV(int location, int count, LongBuffer value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform3i64vNV(int location, int count, long[] value, int value_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform3iARB(int location, int v0, int v1, int v2)
    {
        CallDetails details = getMethodDetails("glUniform3iARB");
        details.foundArguments.add(new Object[] { location, v0, v1, v2 });
    }

    @Override
    public void glUniform3ivARB(int location, int count, IntBuffer value)
    {
        CallDetails details = getMethodDetails("glUniform3ivARB");
        details.foundArguments.add(new Object[] { location, count, value });
    }

    @Override
    public void glUniform3ivARB(int location, int count, int[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniform3ivARB");
        details.foundArguments.add(new Object[] { location, count, value, value_offset });
    }

    @Override
    public void glUniform3ui64NV(int location, long x, long y, long z)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform3ui64vNV(int location, int count, LongBuffer value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform3ui64vNV(int location, int count, long[] value, int value_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform3ui(int location, int v0, int v1, int v2)
    {
        CallDetails details = getMethodDetails("glUniform3ui");
        details.foundArguments.add(new Object[] { location, v0, v1, v2 });
    }

    @Override
    public void glUniform3uiv(int location, int count, IntBuffer value)
    {
        CallDetails details = getMethodDetails("glUniform3uiv");
        details.foundArguments.add(new Object[] { location, count, value });
    }

    @Override
    public void glUniform3uiv(int location, int count, int[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniform3uiv");
        details.foundArguments.add(new Object[] { location, count, value, value_offset });
    }

    @Override
    public void glUniform4fARB(int location, float v0, float v1, float v2, float v3)
    {
        CallDetails details = getMethodDetails("glUniform4fARB");
        details.foundArguments.add(new Object[] { location, v0, v1, v2, v3 });
    }

    @Override
    public void glUniform4fvARB(int location, int count, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniform4fvARB");
        details.foundArguments.add(new Object[] { location, count, value });
    }

    @Override
    public void glUniform4fvARB(int location, int count, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniform4fvARB");
        details.foundArguments.add(new Object[] { location, count, value, value_offset });
    }

    @Override
    public void glUniform4i64NV(int location, long x, long y, long z, long w)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform4i64vNV(int location, int count, LongBuffer value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform4i64vNV(int location, int count, long[] value, int value_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform4iARB(int location, int v0, int v1, int v2, int v3)
    {
        CallDetails details = getMethodDetails("glUniform4iARB");
        details.foundArguments.add(new Object[] { location, v0, v1, v2, v3 });
    }

    @Override
    public void glUniform4ivARB(int location, int count, IntBuffer value)
    {
        CallDetails details = getMethodDetails("glUniform4ivARB");
        details.foundArguments.add(new Object[] { location, count, value });
    }

    @Override
    public void glUniform4ivARB(int location, int count, int[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniform4ivARB");
        details.foundArguments.add(new Object[] { location, count, value, value_offset });
    }

    @Override
    public void glUniform4ui64NV(int location, long x, long y, long z, long w)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform4ui64vNV(int location, int count, LongBuffer value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform4ui64vNV(int location, int count, long[] value, int value_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniform4ui(int location, int v0, int v1, int v2, int v3)
    {
        CallDetails details = getMethodDetails("glUniform4ui");
        details.foundArguments.add(new Object[] { location, v0, v1, v2, v3 });
    }

    @Override
    public void glUniform4uiv(int location, int count, IntBuffer value)
    {
        CallDetails details = getMethodDetails("glUniform4uiv");
        details.foundArguments.add(new Object[] { location, count, value });
    }

    @Override
    public void glUniform4uiv(int location, int count, int[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniform4uiv");
        details.foundArguments.add(new Object[] { location, count, value, value_offset });
    }

    @Override
    public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding)
    {
        CallDetails details = getMethodDetails("glUniformBlockBinding");
        details.foundArguments.add(new Object[] { program, uniformBlockIndex, uniformBlockBinding });
    }

    @Override
    public void glUniformBufferEXT(int program, int location, int buffer)
    {
        CallDetails details = getMethodDetails("glUniformBufferEXT");
        details.foundArguments.add(new Object[] { program, location, buffer });
    }

    @Override
    public void glUniformMatrix2fvARB(int location, int count, boolean transpose, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniformMatrix2fvARB");
        details.foundArguments.add(new Object[] { location, count, transpose, value });
    }

    @Override
    public void glUniformMatrix2fvARB(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniformMatrix2fvARB");
        details.foundArguments.add(new Object[] { location, count, transpose, value, value_offset });
    }

    @Override
    public void glUniformMatrix2x3fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniformMatrix2x3fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value });
    }

    @Override
    public void glUniformMatrix2x3fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniformMatrix2x3fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value, value_offset });
    }

    @Override
    public void glUniformMatrix2x4fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniformMatrix2x4fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value });
    }

    @Override
    public void glUniformMatrix2x4fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniformMatrix2x4fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value, value_offset });
    }

    @Override
    public void glUniformMatrix3fvARB(int location, int count, boolean transpose, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniformMatrix3fvARB");
        details.foundArguments.add(new Object[] { location, count, transpose, value });
    }

    @Override
    public void glUniformMatrix3fvARB(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniformMatrix3fvARB");
        details.foundArguments.add(new Object[] { location, count, transpose, value, value_offset });
    }

    @Override
    public void glUniformMatrix3x2fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniformMatrix3x2fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value });
    }

    @Override
    public void glUniformMatrix3x2fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniformMatrix3x2fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value, value_offset });
    }

    @Override
    public void glUniformMatrix3x4fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniformMatrix3x4fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value });
    }

    @Override
    public void glUniformMatrix3x4fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniformMatrix3x4fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value, value_offset });
    }

    @Override
    public void glUniformMatrix4fvARB(int location, int count, boolean transpose, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniformMatrix4fvARB");
        details.foundArguments.add(new Object[] { location, count, transpose, value });
    }

    @Override
    public void glUniformMatrix4fvARB(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniformMatrix4fvARB");
        details.foundArguments.add(new Object[] { location, count, transpose, value, value_offset });
    }

    @Override
    public void glUniformMatrix4x2fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniformMatrix4x2fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value });
    }

    @Override
    public void glUniformMatrix4x2fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniformMatrix4x2fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value, value_offset });
    }

    @Override
    public void glUniformMatrix4x3fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniformMatrix4x3fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value });
    }

    @Override
    public void glUniformMatrix4x3fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniformMatrix4x3fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value, value_offset });
    }

    @Override
    public void glVertexAttribDivisor(int index, int divisor)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUnlockArraysEXT()
    {
        CallDetails details = getMethodDetails("glUnlockArraysEXT");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public boolean glUnmapNamedBufferEXT(int buffer)
    {
        return false;
    }

    @Override
    public void glUnmapTexture2DINTEL(int texture, int level)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUseProgramObjectARB(long programObj)
    {
        CallDetails details = getMethodDetails("glUseProgramObjectARB");
        details.foundArguments.add(new Object[] { programObj });
    }

    @Override
    public void glVDPAUFiniNV()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVDPAUGetSurfaceivNV(long surface, int pname, int bufSize, IntBuffer length, IntBuffer values)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVDPAUGetSurfaceivNV(long surface, int pname, int bufSize, int[] length, int length_offset, int[] values, int values_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVDPAUInitNV(Buffer vdpDevice, Buffer getProcAddress)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean glVDPAUIsSurfaceNV(long surface)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVDPAUMapSurfacesNV(int numSurfaces, PointerBuffer surfaces)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long glVDPAURegisterOutputSurfaceNV(Buffer vdpSurface, int target, int numTextureNames, IntBuffer textureNames)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long glVDPAURegisterOutputSurfaceNV(Buffer vdpSurface, int target, int numTextureNames, int[] textureNames, int textureNames_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long glVDPAURegisterVideoSurfaceNV(Buffer vdpSurface, int target, int numTextureNames, IntBuffer textureNames)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long glVDPAURegisterVideoSurfaceNV(Buffer vdpSurface, int target, int numTextureNames, int[] textureNames, int textureNames_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVDPAUSurfaceAccessNV(long surface, int access)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVDPAUUnmapSurfacesNV(int numSurface, PointerBuffer surfaces)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVDPAUUnregisterSurfaceNV(long surface)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glValidateProgramARB(long programObj)
    {
        CallDetails details = getMethodDetails("glValidateProgramARB");
        details.foundArguments.add(new Object[] { programObj });
    }

    @Override
    public void glVariantPointerEXT(int id, int type, int stride, Buffer addr)
    {
        CallDetails details = getMethodDetails("glVariantPointerEXT");
        details.foundArguments.add(new Object[] { id, type, stride, addr });
    }

    @Override
    public void glVariantPointerEXT(int id, int type, int stride, long addr_buffer_offset)
    {
        CallDetails details = getMethodDetails("glVariantPointerEXT");
        details.foundArguments.add(new Object[] { id, type, stride, addr_buffer_offset });
    }

    @Override
    public void glVariantbvEXT(int id, ByteBuffer addr)
    {
        CallDetails details = getMethodDetails("glVariantbvEXT");
        details.foundArguments.add(new Object[] { id, addr });
    }

    @Override
    public void glVariantbvEXT(int id, byte[] addr, int addr_offset)
    {
        CallDetails details = getMethodDetails("glVariantbvEXT");
        details.foundArguments.add(new Object[] { id, addr, addr_offset });
    }

    @Override
    public void glVariantdvEXT(int id, DoubleBuffer addr)
    {
        CallDetails details = getMethodDetails("glVariantdvEXT");
        details.foundArguments.add(new Object[] { id, addr });
    }

    @Override
    public void glVariantdvEXT(int id, double[] addr, int addr_offset)
    {
        CallDetails details = getMethodDetails("glVariantdvEXT");
        details.foundArguments.add(new Object[] { id, addr, addr_offset });
    }

    @Override
    public void glVariantfvEXT(int id, FloatBuffer addr)
    {
        CallDetails details = getMethodDetails("glVariantfvEXT");
        details.foundArguments.add(new Object[] { id, addr });
    }

    @Override
    public void glVariantfvEXT(int id, float[] addr, int addr_offset)
    {
        CallDetails details = getMethodDetails("glVariantfvEXT");
        details.foundArguments.add(new Object[] { id, addr, addr_offset });
    }

    @Override
    public void glVariantivEXT(int id, IntBuffer addr)
    {
        CallDetails details = getMethodDetails("glVariantivEXT");
        details.foundArguments.add(new Object[] { id, addr });
    }

    @Override
    public void glVariantivEXT(int id, int[] addr, int addr_offset)
    {
        CallDetails details = getMethodDetails("glVariantivEXT");
        details.foundArguments.add(new Object[] { id, addr, addr_offset });
    }

    @Override
    public void glVariantsvEXT(int id, ShortBuffer addr)
    {
        CallDetails details = getMethodDetails("glVariantsvEXT");
        details.foundArguments.add(new Object[] { id, addr });
    }

    @Override
    public void glVariantsvEXT(int id, short[] addr, int addr_offset)
    {
        CallDetails details = getMethodDetails("glVariantsvEXT");
        details.foundArguments.add(new Object[] { id, addr, addr_offset });
    }

    @Override
    public void glVariantubvEXT(int id, ByteBuffer addr)
    {
        CallDetails details = getMethodDetails("glVariantubvEXT");
        details.foundArguments.add(new Object[] { id, addr });
    }

    @Override
    public void glVariantubvEXT(int id, byte[] addr, int addr_offset)
    {
        CallDetails details = getMethodDetails("glVariantubvEXT");
        details.foundArguments.add(new Object[] { id, addr, addr_offset });
    }

    @Override
    public void glVariantuivEXT(int id, IntBuffer addr)
    {
        CallDetails details = getMethodDetails("glVariantuivEXT");
        details.foundArguments.add(new Object[] { id, addr });
    }

    @Override
    public void glVariantuivEXT(int id, int[] addr, int addr_offset)
    {
        CallDetails details = getMethodDetails("glVariantuivEXT");
        details.foundArguments.add(new Object[] { id, addr, addr_offset });
    }

    @Override
    public void glVariantusvEXT(int id, ShortBuffer addr)
    {
        CallDetails details = getMethodDetails("glVariantusvEXT");
        details.foundArguments.add(new Object[] { id, addr });
    }

    @Override
    public void glVariantusvEXT(int id, short[] addr, int addr_offset)
    {
        CallDetails details = getMethodDetails("glVariantusvEXT");
        details.foundArguments.add(new Object[] { id, addr, addr_offset });
    }

    @Override
    public void glVertex2bOES(byte x, byte y)
    {
        CallDetails details = getMethodDetails("glVertex2bOES");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glVertex2bvOES(ByteBuffer coords)
    {
        CallDetails details = getMethodDetails("glVertex2bvOES");
        details.foundArguments.add(new Object[] { coords });
    }

    @Override
    public void glVertex2bvOES(byte[] coords, int coords_offset)
    {
        CallDetails details = getMethodDetails("glVertex2bvOES");
        details.foundArguments.add(new Object[] { coords, coords_offset });
    }

    @Override
    public void glVertex2d(double x, double y)
    {
        CallDetails details = getMethodDetails("glVertex2d");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glVertex2dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex2dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex2dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex2dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex2f(float x, float y)
    {
        CallDetails details = getMethodDetails("glVertex2f");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glVertex2fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex2fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex2fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex2fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex2h(short x, short y)
    {
        CallDetails details = getMethodDetails("glVertex2h");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glVertex2hv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex2hv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex2hv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex2hv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex2i(int x, int y)
    {
        CallDetails details = getMethodDetails("glVertex2i");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glVertex2iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex2iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex2iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex2iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex2s(short x, short y)
    {
        CallDetails details = getMethodDetails("glVertex2s");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glVertex2sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex2sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex2sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex2sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex3bOES(byte x, byte y, byte z)
    {
        CallDetails details = getMethodDetails("glVertex3bOES");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glVertex3bvOES(ByteBuffer coords)
    {
        CallDetails details = getMethodDetails("glVertex3bvOES");
        details.foundArguments.add(new Object[] { coords });
    }

    @Override
    public void glVertex3bvOES(byte[] coords, int coords_offset)
    {
        CallDetails details = getMethodDetails("glVertex3bvOES");
        details.foundArguments.add(new Object[] { coords, coords_offset });
    }

    @Override
    public void glVertex3d(double x, double y, double z)
    {
        CallDetails details = getMethodDetails("glVertex3d");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glVertex3dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex3dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex3dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex3dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex3f(float x, float y, float z)
    {
        CallDetails details = getMethodDetails("glVertex3f");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glVertex3fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex3fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex3fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex3fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex3h(short x, short y, short z)
    {
        CallDetails details = getMethodDetails("glVertex3h");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glVertex3hv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex3hv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex3hv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex3hv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex3i(int x, int y, int z)
    {
        CallDetails details = getMethodDetails("glVertex3i");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glVertex3iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex3iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex3iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex3iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex3s(short x, short y, short z)
    {
        CallDetails details = getMethodDetails("glVertex3s");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glVertex3sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex3sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex3sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex3sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex4bOES(byte x, byte y, byte z, byte w)
    {
        CallDetails details = getMethodDetails("glVertex4bOES");
        details.foundArguments.add(new Object[] { x, y, z, w });
    }

    @Override
    public void glVertex4bvOES(ByteBuffer coords)
    {
        CallDetails details = getMethodDetails("glVertex4bvOES");
        details.foundArguments.add(new Object[] { coords });
    }

    @Override
    public void glVertex4bvOES(byte[] coords, int coords_offset)
    {
        CallDetails details = getMethodDetails("glVertex4bvOES");
        details.foundArguments.add(new Object[] { coords, coords_offset });
    }

    @Override
    public void glVertex4d(double x, double y, double z, double w)
    {
        CallDetails details = getMethodDetails("glVertex4d");
        details.foundArguments.add(new Object[] { x, y, z, w });
    }

    @Override
    public void glVertex4dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex4dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex4dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex4dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex4f(float x, float y, float z, float w)
    {
        CallDetails details = getMethodDetails("glVertex4f");
        details.foundArguments.add(new Object[] { x, y, z, w });
    }

    @Override
    public void glVertex4fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex4fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex4fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex4fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex4h(short x, short y, short z, short w)
    {
        CallDetails details = getMethodDetails("glVertex4h");
        details.foundArguments.add(new Object[] { x, y, z, w });
    }

    @Override
    public void glVertex4hv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex4hv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex4hv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex4hv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex4i(int x, int y, int z, int w)
    {
        CallDetails details = getMethodDetails("glVertex4i");
        details.foundArguments.add(new Object[] { x, y, z, w });
    }

    @Override
    public void glVertex4iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex4iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex4iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex4iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertex4s(short x, short y, short z, short w)
    {
        CallDetails details = getMethodDetails("glVertex4s");
        details.foundArguments.add(new Object[] { x, y, z, w });
    }

    @Override
    public void glVertex4sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertex4sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glVertex4sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertex4sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glVertexArrayColorOffsetEXT(int vaobj, int buffer, int size, int type, int stride, long offset)
    {
        CallDetails details = getMethodDetails("glVertexArrayColorOffsetEXT");
        details.foundArguments.add(new Object[] { vaobj, buffer, size, type, stride, offset });
    }

    @Override
    public void glVertexArrayEdgeFlagOffsetEXT(int vaobj, int buffer, int stride, long offset)
    {
        CallDetails details = getMethodDetails("glVertexArrayEdgeFlagOffsetEXT");
        details.foundArguments.add(new Object[] { vaobj, buffer, stride, offset });
    }

    @Override
    public void glVertexArrayFogCoordOffsetEXT(int vaobj, int buffer, int type, int stride, long offset)
    {
        CallDetails details = getMethodDetails("glVertexArrayFogCoordOffsetEXT");
        details.foundArguments.add(new Object[] { vaobj, buffer, type, stride, offset });
    }

    @Override
    public void glVertexArrayIndexOffsetEXT(int vaobj, int buffer, int type, int stride, long offset)
    {
        CallDetails details = getMethodDetails("glVertexArrayIndexOffsetEXT");
        details.foundArguments.add(new Object[] { vaobj, buffer, type, stride, offset });
    }

    @Override
    public void glVertexArrayMultiTexCoordOffsetEXT(int vaobj, int buffer, int texunit, int size, int type, int stride, long offset)
    {
        CallDetails details = getMethodDetails("glVertexArrayMultiTexCoordOffsetEXT");
        details.foundArguments.add(new Object[] { vaobj, buffer, texunit, size, type, stride, offset });
    }

    @Override
    public void glVertexArrayNormalOffsetEXT(int vaobj, int buffer, int type, int stride, long offset)
    {
        CallDetails details = getMethodDetails("glVertexArrayNormalOffsetEXT");
        details.foundArguments.add(new Object[] { vaobj, buffer, type, stride, offset });
    }

    @Override
    public void glVertexArrayParameteriAPPLE(int pname, int param)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexArrayRangeAPPLE(int length, Buffer pointer)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexArraySecondaryColorOffsetEXT(int vaobj, int buffer, int size, int type, int stride, long offset)
    {
        CallDetails details = getMethodDetails("glVertexArraySecondaryColorOffsetEXT");
        details.foundArguments.add(new Object[] { vaobj, buffer, size, type, stride, offset });
    }

    @Override
    public void glVertexArrayTexCoordOffsetEXT(int vaobj, int buffer, int size, int type, int stride, long offset)
    {
        CallDetails details = getMethodDetails("glVertexArrayTexCoordOffsetEXT");
        details.foundArguments.add(new Object[] { vaobj, buffer, size, type, stride, offset });
    }

    @Override
    public void glVertexArrayVertexAttribIOffsetEXT(int vaobj, int buffer, int index, int size, int type, int stride, long offset)
    {
        CallDetails details = getMethodDetails("glVertexArrayVertexAttribIOffsetEXT");
        details.foundArguments.add(new Object[] { vaobj, buffer, index, size, type, stride, offset });
    }

    @Override
    public void glVertexArrayVertexAttribOffsetEXT(int vaobj, int buffer, int index, int size, int type, boolean normalized, int stride, long offset)
    {
        CallDetails details = getMethodDetails("glVertexArrayVertexAttribOffsetEXT");
        details.foundArguments.add(new Object[] { vaobj, buffer, index, size, type, normalized, stride, offset });
    }

    @Override
    public void glVertexArrayVertexOffsetEXT(int vaobj, int buffer, int size, int type, int stride, long offset)
    {
        CallDetails details = getMethodDetails("glVertexArrayVertexOffsetEXT");
        details.foundArguments.add(new Object[] { vaobj, buffer, size, type, stride, offset });
    }

    @Override
    public void glVertexAttrib1dARB(int index, double x)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1dARB");
        details.foundArguments.add(new Object[] { index, x });
    }

    @Override
    public void glVertexAttrib1dvARB(int index, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1dvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib1dvARB(int index, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1dvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib1fARB(int index, float x)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1fARB");
        details.foundArguments.add(new Object[] { index, x });
    }

    @Override
    public void glVertexAttrib1fvARB(int index, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1fvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib1fvARB(int index, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1fvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib1h(int index, short x)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1h");
        details.foundArguments.add(new Object[] { index, x });
    }

    @Override
    public void glVertexAttrib1hv(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1hv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib1hv(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1hv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib1sARB(int index, short x)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1sARB");
        details.foundArguments.add(new Object[] { index, x });
    }

    @Override
    public void glVertexAttrib1svARB(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1svARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib1svARB(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1svARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib2dARB(int index, double x, double y)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2dARB");
        details.foundArguments.add(new Object[] { index, x, y });
    }

    @Override
    public void glVertexAttrib2dvARB(int index, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2dvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib2dvARB(int index, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2dvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib2fARB(int index, float x, float y)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2fARB");
        details.foundArguments.add(new Object[] { index, x, y });
    }

    @Override
    public void glVertexAttrib2fvARB(int index, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2fvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib2fvARB(int index, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2fvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib2h(int index, short x, short y)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2h");
        details.foundArguments.add(new Object[] { index, x, y });
    }

    @Override
    public void glVertexAttrib2hv(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2hv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib2hv(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2hv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib2sARB(int index, short x, short y)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2sARB");
        details.foundArguments.add(new Object[] { index, x, y });
    }

    @Override
    public void glVertexAttrib2svARB(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2svARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib2svARB(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2svARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib3dARB(int index, double x, double y, double z)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3dARB");
        details.foundArguments.add(new Object[] { index, x, y, z });
    }

    @Override
    public void glVertexAttrib3dvARB(int index, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3dvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib3dvARB(int index, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3dvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib3fARB(int index, float x, float y, float z)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3fARB");
        details.foundArguments.add(new Object[] { index, x, y, z });
    }

    @Override
    public void glVertexAttrib3fvARB(int index, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3fvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib3fvARB(int index, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3fvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib3h(int index, short x, short y, short z)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3h");
        details.foundArguments.add(new Object[] { index, x, y, z });
    }

    @Override
    public void glVertexAttrib3hv(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3hv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib3hv(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3hv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib3sARB(int index, short x, short y, short z)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3sARB");
        details.foundArguments.add(new Object[] { index, x, y, z });
    }

    @Override
    public void glVertexAttrib3svARB(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3svARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib3svARB(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3svARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4NbvARB(int index, ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4NbvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4NbvARB(int index, byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4NbvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4NivARB(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4NivARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4NivARB(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4NivARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4NsvARB(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4NsvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4NsvARB(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4NsvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4NubARB(int index, byte x, byte y, byte z, byte w)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4NubARB");
        details.foundArguments.add(new Object[] { index, x, y, z, w });
    }

    @Override
    public void glVertexAttrib4NubvARB(int index, ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4NubARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4NubvARB(int index, byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4NubvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4NuivARB(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4NuivARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4NuivARB(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4NuivARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4NusvARB(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4NusvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4NusvARB(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4NusvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4bvARB(int index, ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4bvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4bvARB(int index, byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4bvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4dARB(int index, double x, double y, double z, double w)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4dARB");
        details.foundArguments.add(new Object[] { index, x, y, z, w });
    }

    @Override
    public void glVertexAttrib4dvARB(int index, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4dvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4dvARB(int index, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4dvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4fARB(int index, float x, float y, float z, float w)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4fARB");
        details.foundArguments.add(new Object[] { index, x, y, z, w });
    }

    @Override
    public void glVertexAttrib4fvARB(int index, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4fvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4fvARB(int index, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4fvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4h(int index, short x, short y, short z, short w)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4h");
        details.foundArguments.add(new Object[] { index, x, y, z, w });
    }

    @Override
    public void glVertexAttrib4hv(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4hv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4hv(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4hv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4ivARB(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4ivARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4ivARB(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4ivARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4sARB(int index, short x, short y, short z, short w)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4sARB");
        details.foundArguments.add(new Object[] { index, x, y, z, w });
    }

    @Override
    public void glVertexAttrib4svARB(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4svARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4svARB(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4svARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4ubvARB(int index, ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4ubvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4ubvARB(int index, byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4ubvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4uivARB(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4uivARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4uivARB(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4uivARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4usvARB(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4usvARB");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4usvARB(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4usvARB");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI1iEXT(int index, int x)
    {
        CallDetails details = getMethodDetails("glVertexAttribI1iEXT");
        details.foundArguments.add(new Object[] { index, x });
    }

    @Override
    public void glVertexAttribI1ivEXT(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI1ivEXT");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI1ivEXT(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI1ivEXT");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI1uiEXT(int index, int x)
    {
        CallDetails details = getMethodDetails("glVertexAttribI1uiEXT");
        details.foundArguments.add(new Object[] { index, x });
    }

    @Override
    public void glVertexAttribI1uivEXT(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI1uivEXT");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI1uivEXT(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI1uivEXT");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI2iEXT(int index, int x, int y)
    {
        CallDetails details = getMethodDetails("glVertexAttribI2iEXT");
        details.foundArguments.add(new Object[] { index, x, y });
    }

    @Override
    public void glVertexAttribI2ivEXT(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI2ivEXT");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI2ivEXT(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI2ivEXT");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI2uiEXT(int index, int x, int y)
    {
        CallDetails details = getMethodDetails("glVertexAttribI2uiEXT");
        details.foundArguments.add(new Object[] { index, x, y });
    }

    @Override
    public void glVertexAttribI2uivEXT(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI2uivEXT");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI2uivEXT(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI2uivEXT");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI3iEXT(int index, int x, int y, int z)
    {
        CallDetails details = getMethodDetails("glVertexAttribI3iEXT");
        details.foundArguments.add(new Object[] { index, x, y, z });
    }

    @Override
    public void glVertexAttribI3ivEXT(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI3ivEXT");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI3ivEXT(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI3ivEXT");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI3uiEXT(int index, int x, int y, int z)
    {
        CallDetails details = getMethodDetails("glVertexAttribI3uiEXT");
        details.foundArguments.add(new Object[] { index, x, y, z });
    }

    @Override
    public void glVertexAttribI3uivEXT(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI3uivEXT");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI3uivEXT(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI3uivEXT");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI4bvEXT(int index, ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4bvEXT");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI4bvEXT(int index, byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4bvEXT");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI4i(int index, int x, int y, int z, int w)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4i");
        details.foundArguments.add(new Object[] { index, x, y, z, w });
    }

    @Override
    public void glVertexAttribI4iEXT(int index, int x, int y, int z, int w)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4iEXT");
        details.foundArguments.add(new Object[] { index, x, y, z, w });
    }

    @Override
    public void glVertexAttribI4iv(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4iv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI4iv(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4iv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI4ivEXT(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4ivEXT");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI4ivEXT(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4ivEXT");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI4svEXT(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4svEXT");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI4svEXT(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4svEXT");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI4ubvEXT(int index, ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4ubvEXT");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI4ubvEXT(int index, byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4ubvEXT");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI4ui(int index, int x, int y, int z, int w)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4ui");
        details.foundArguments.add(new Object[] { index, x, y, z, w });
    }

    @Override
    public void glVertexAttribI4uiEXT(int index, int x, int y, int z, int w)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4uiEXT");
        details.foundArguments.add(new Object[] { index, x, y, z, w });
    }

    @Override
    public void glVertexAttribI4uiv(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4uiv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI4uiv(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4uiv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI4uivEXT(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4uivEXT");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI4uivEXT(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4uivEXT");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI4usvEXT(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4usvEXT");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI4usvEXT(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4usvEXT");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribIPointer(int index, int size, int type, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glVertexAttribIPointer");
        details.foundArguments.add(new Object[] { index, size, type, stride, pointer });
    }

    @Override
    public void glVertexAttribIPointer(int index, int size, int type, int stride, long pointer_buffer_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribIPointer");
        details.foundArguments.add(new Object[] { index, size, type, stride, pointer_buffer_offset });
    }

    @Override
    public boolean isPBOPackBound()
    {
        return false;
    }

    @Override
    public boolean isPBOUnpackBound()
    {
        return false;
    }

    @Override
    public void glVertexAttribIPointerEXT(int index, int size, int type, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glVertexAttribIPointerEXT");
        details.foundArguments.add(new Object[] { index, size, type, stride, pointer });
    }

    @Override
    public void glVertexAttribL1i64NV(int index, long x)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL1i64vNV(int index, LongBuffer v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL1i64vNV(int index, long[] v, int v_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL1ui64NV(int index, long x)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL1ui64vNV(int index, LongBuffer v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL1ui64vNV(int index, long[] v, int v_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL2i64NV(int index, long x, long y)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL2i64vNV(int index, LongBuffer v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL2i64vNV(int index, long[] v, int v_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL2ui64NV(int index, long x, long y)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL2ui64vNV(int index, LongBuffer v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL2ui64vNV(int index, long[] v, int v_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL3i64NV(int index, long x, long y, long z)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL3i64vNV(int index, LongBuffer v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL3i64vNV(int index, long[] v, int v_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL3ui64NV(int index, long x, long y, long z)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL3ui64vNV(int index, LongBuffer v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL3ui64vNV(int index, long[] v, int v_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL4i64NV(int index, long x, long y, long z, long w)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL4i64vNV(int index, LongBuffer v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL4i64vNV(int index, long[] v, int v_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL4ui64NV(int index, long x, long y, long z, long w)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL4ui64vNV(int index, LongBuffer v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL4ui64vNV(int index, long[] v, int v_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribLFormatNV(int index, int size, int type, int stride)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribParameteriAMD(int index, int pname, int param)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribPointerARB(int index, int size, int type, boolean normalized, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glVertexAttribPointerARB");
        details.foundArguments.add(new Object[] { index, size, type, normalized, stride, pointer });
    }

    @Override
    public void glVertexAttribPointerARB(int index, int size, int type, boolean normalized, int stride, long pointer_buffer_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribPointerARB");
        details.foundArguments.add(new Object[] { index, size, type, normalized, stride, pointer_buffer_offset });
    }

    @Override
    public void glVertexAttribs1hv(int index, int n, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribs1hv");
        details.foundArguments.add(new Object[] { index, n, v });
    }

    @Override
    public void glVertexAttribs1hv(int index, int n, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribs1hv");
        details.foundArguments.add(new Object[] { index, n, v, v_offset });
    }

    @Override
    public void glVertexAttribs2hv(int index, int n, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribs2hv");
        details.foundArguments.add(new Object[] { index, n, v });
    }

    @Override
    public void glVertexAttribs2hv(int index, int n, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribs2hv");
        details.foundArguments.add(new Object[] { index, n, v, v_offset });
    }

    @Override
    public void glVertexAttribs3hv(int index, int n, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribs3hv");
        details.foundArguments.add(new Object[] { index, n, v });
    }

    @Override
    public void glVertexAttribs3hv(int index, int n, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribs3hv");
        details.foundArguments.add(new Object[] { index, n, v, v_offset });
    }

    @Override
    public void glVertexAttribs4hv(int index, int n, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribs4hv");
        details.foundArguments.add(new Object[] { index, n, v });
    }

    @Override
    public void glVertexAttribs4hv(int index, int n, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribs4hv");
        details.foundArguments.add(new Object[] { index, n, v, v_offset });
    }

    @Override
    public void glVertexBlendARB(int count)
    {
        CallDetails details = getMethodDetails("glVertexBlendARB");
        details.foundArguments.add(new Object[] { count });
    }

    @Override
    public void glVertexWeightPointerEXT(int size, int type, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glVertexWeightPointerEXT");
        details.foundArguments.add(new Object[] { size, type, stride, pointer });
    }

    @Override
    public void glVertexWeightPointerEXT(int size, int type, int stride, long pointer_buffer_offset)
    {
        CallDetails details = getMethodDetails("glVertexWeightPointerEXT");
        details.foundArguments.add(new Object[] { size, type, stride, pointer_buffer_offset });
    }

    @Override
    public void glVertexWeightfEXT(float weight)
    {
        CallDetails details = getMethodDetails("glVertexWeightfEXT");
        details.foundArguments.add(new Object[] { weight });
    }

    @Override
    public void glVertexWeightfvEXT(FloatBuffer weight)
    {
        CallDetails details = getMethodDetails("glVertexWeightfvEXT");
        details.foundArguments.add(new Object[] { weight });
    }

    @Override
    public void glVertexWeightfvEXT(float[] weight, int weight_offset)
    {
        CallDetails details = getMethodDetails("glVertexWeightfvEXT");
        details.foundArguments.add(new Object[] { weight, weight_offset });
    }

    @Override
    public void glVertexWeighth(short weight)
    {
        CallDetails details = getMethodDetails("glVertexWeighth");
        details.foundArguments.add(new Object[] { weight });
    }

    @Override
    public void glVertexWeighthv(ShortBuffer weight)
    {
        CallDetails details = getMethodDetails("glVertexWeighthv");
        details.foundArguments.add(new Object[] { weight });
    }

    @Override
    public void glVertexWeighthv(short[] weight, int weight_offset)
    {
        CallDetails details = getMethodDetails("glVertexWeighthv");
        details.foundArguments.add(new Object[] { weight, weight_offset });
    }

    @Override
    public int glVideoCaptureNV(int video_capture_slot, IntBuffer sequence_num, LongBuffer capture_time)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int glVideoCaptureNV(int video_capture_slot, int[] sequence_num, int sequence_num_offset, long[] capture_time, int capture_time_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVideoCaptureStreamParameterdvNV(int video_capture_slot, int stream, int pname, DoubleBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVideoCaptureStreamParameterdvNV(int video_capture_slot, int stream, int pname, double[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glFramebufferTextureMultiviewOVR(int target, int attachment, int texture, int level, int baseViewIndex, int numViews)
    {

    }

    @Override
    public void glVideoCaptureStreamParameterfvNV(int video_capture_slot, int stream, int pname, FloatBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVideoCaptureStreamParameterfvNV(int video_capture_slot, int stream, int pname, float[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVideoCaptureStreamParameterivNV(int video_capture_slot, int stream, int pname, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVideoCaptureStreamParameterivNV(int video_capture_slot, int stream, int pname, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glWeightPointerARB(int size, int type, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glWeightPointerARB");
        details.foundArguments.add(new Object[] { size, type, stride, pointer });
    }

    @Override
    public void glWeightPointerARB(int size, int type, int stride, long pointer_buffer_offset)
    {
        CallDetails details = getMethodDetails("glWeightPointerARB");
        details.foundArguments.add(new Object[] { size, type, stride, pointer_buffer_offset });
    }

    @Override
    public void glWeightbvARB(int size, ByteBuffer weights)
    {
        CallDetails details = getMethodDetails("glWeightbvARB");
        details.foundArguments.add(new Object[] { size, weights });
    }

    @Override
    public void glWeightbvARB(int size, byte[] weights, int weights_offset)
    {
        CallDetails details = getMethodDetails("glWeightbvARB");
        details.foundArguments.add(new Object[] { size, weights, weights_offset });
    }

    @Override
    public void glWeightdvARB(int size, DoubleBuffer weights)
    {
        CallDetails details = getMethodDetails("glWeightdvARB");
        details.foundArguments.add(new Object[] { size, weights });
    }

    @Override
    public void glWeightdvARB(int size, double[] weights, int weights_offset)
    {
        CallDetails details = getMethodDetails("glWeightdvARB");
        details.foundArguments.add(new Object[] { size, weights, weights_offset });
    }

    @Override
    public void glWeightfvARB(int size, FloatBuffer weights)
    {
        CallDetails details = getMethodDetails("glWeightfvARB");
        details.foundArguments.add(new Object[] { size, weights });
    }

    @Override
    public void glWeightfvARB(int size, float[] weights, int weights_offset)
    {
        CallDetails details = getMethodDetails("glWeightfvARB");
        details.foundArguments.add(new Object[] { size, weights, weights_offset });
    }

    @Override
    public void glWeightivARB(int size, IntBuffer weights)
    {
        CallDetails details = getMethodDetails("glWeightivARB");
        details.foundArguments.add(new Object[] { size, weights });
    }

    @Override
    public void glWeightivARB(int size, int[] weights, int weights_offset)
    {
        CallDetails details = getMethodDetails("glWeightivARB");
        details.foundArguments.add(new Object[] { size, weights, weights_offset });
    }

    @Override
    public void glWeightsvARB(int size, ShortBuffer weights)
    {
        CallDetails details = getMethodDetails("glWeightsvARB");
        details.foundArguments.add(new Object[] { size, weights });
    }

    @Override
    public void glWeightsvARB(int size, short[] weights, int weights_offset)
    {
        CallDetails details = getMethodDetails("glWeightsvARB");
        details.foundArguments.add(new Object[] { size, weights, weights_offset });
    }

    @Override
    public void glWeightubvARB(int size, ByteBuffer weights)
    {
        CallDetails details = getMethodDetails("glWeightubvARB");
        details.foundArguments.add(new Object[] { size, weights });
    }

    @Override
    public void glWeightubvARB(int size, byte[] weights, int weights_offset)
    {
        CallDetails details = getMethodDetails("glWeightubvARB");
        details.foundArguments.add(new Object[] { size, weights, weights_offset });
    }

    @Override
    public void glWeightuivARB(int size, IntBuffer weights)
    {
        CallDetails details = getMethodDetails("glWeightuivARB");
        details.foundArguments.add(new Object[] { size, weights });
    }

    @Override
    public void glWeightuivARB(int size, int[] weights, int weights_offset)
    {
        CallDetails details = getMethodDetails("glWeightuivARB");
        details.foundArguments.add(new Object[] { size, weights, weights_offset });
    }

    @Override
    public void glWeightusvARB(int size, ShortBuffer weights)
    {
        CallDetails details = getMethodDetails("glWeightusvARB");
        details.foundArguments.add(new Object[] { size, weights });
    }

    @Override
    public void glWeightusvARB(int size, short[] weights, int weights_offset)
    {
        CallDetails details = getMethodDetails("glWeightusvARB");
        details.foundArguments.add(new Object[] { size, weights, weights_offset });
    }

    @Override
    public void glWindowPos2d(double x, double y)
    {
        CallDetails details = getMethodDetails("glWindowPos2d");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glWindowPos2dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glWindowPos2dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glWindowPos2dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glWindowPos2dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glWindowPos2f(float x, float y)
    {
        CallDetails details = getMethodDetails("glWindowPos2f");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glWindowPos2fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glWindowPos2fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glWindowPos2fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glWindowPos2fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glWindowPos2i(int x, int y)
    {
        CallDetails details = getMethodDetails("glWindowPos2i");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glWindowPos2iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glWindowPos2iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glWindowPos2iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glWindowPos2iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glWindowPos2s(short x, short y)
    {
        CallDetails details = getMethodDetails("glWindowPos2s");
        details.foundArguments.add(new Object[] { x, y });
    }

    @Override
    public void glWindowPos2sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glWindowPos2sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glWindowPos2sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glWindowPos2sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glWindowPos3d(double x, double y, double z)
    {
        CallDetails details = getMethodDetails("glWindowPos3d");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glWindowPos3dv(DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glWindowPos3dv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glWindowPos3dv(double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glWindowPos3dv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glWindowPos3f(float x, float y, float z)
    {
        CallDetails details = getMethodDetails("glWindowPos3f");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glWindowPos3fv(FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glWindowPos3fv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glWindowPos3fv(float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glWindowPos3fv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glWindowPos3i(int x, int y, int z)
    {
        CallDetails details = getMethodDetails("glWindowPos3i");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glWindowPos3iv(IntBuffer v)
    {
        CallDetails details = getMethodDetails("glWindowPos3iv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glWindowPos3iv(int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glWindowPos3iv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glWindowPos3s(short x, short y, short z)
    {
        CallDetails details = getMethodDetails("glWindowPos3s");
        details.foundArguments.add(new Object[] { x, y, z });
    }

    @Override
    public void glWindowPos3sv(ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glWindowPos3sv");
        details.foundArguments.add(new Object[] { v });
    }

    @Override
    public void glWindowPos3sv(short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glWindowPos3sv");
        details.foundArguments.add(new Object[] { v, v_offset });
    }

    @Override
    public void glWriteMaskEXT(int res, int in, int outX, int outY, int outZ, int outW)
    {
        CallDetails details = getMethodDetails("glWriteMaskEXT");
        details.foundArguments.add(new Object[] { res, in, outX, outY, outZ, outW });
    }

    @Override
    public GLBufferStorage mapNamedBufferEXT(int bufferName, int access) throws GLException
    {
        return null;
    }

    @Override
    public GLBufferStorage mapNamedBufferRangeEXT(int bufferName, long offset, long length, int access) throws
                                                                                                        GLException
    {
        return null;
    }

    @Override
    public void glAlphaFunc(int func, float ref)
    {
        CallDetails details = getMethodDetails("glAlphaFunc");
        details.foundArguments.add(new Object[] { func, ref });
    }

    @Override
    public void glClientActiveTexture(int texture)
    {
        CallDetails details = getMethodDetails("glClientActiveTexture");
        details.foundArguments.add(new Object[] { texture });
    }

    @Override
    public void glColor4ub(byte red, byte green, byte blue, byte alpha)
    {
        CallDetails details = getMethodDetails("glColor4ub");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glFogf(int pname, float param)
    {
        CallDetails details = getMethodDetails("glFogf");
        details.foundArguments.add(new Object[] { pname, param });
    }

    @Override
    public void glFogfv(int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glFogfv");
        details.foundArguments.add(new Object[] { pname, params });
    }

    @Override
    public void glFogfv(int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glFogfv");
        details.foundArguments.add(new Object[] { pname, params, params_offset });
    }

    @Override
    public void glGetLightfv(int light, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetLightfv");
        details.foundArguments.add(new Object[] { light, pname, params });
    }

    @Override
    public void glGetLightfv(int light, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetLightfv");
        details.foundArguments.add(new Object[] { light, pname, params, params_offset });
    }

    @Override
    public void glGetMaterialfv(int face, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetMaterialfv");
        details.foundArguments.add(new Object[] { face, pname, params });
    }

    @Override
    public void glGetMaterialfv(int face, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetMaterialfv");
        details.foundArguments.add(new Object[] { face, pname, params, params_offset });
    }

    @Override
    public void glGetTexEnvfv(int tenv, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTexEnvfv");
        details.foundArguments.add(new Object[] { tenv, pname, params });
    }

    @Override
    public void glGetTexEnvfv(int tenv, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTexEnvfv");
        details.foundArguments.add(new Object[] { tenv, pname, params, params_offset });
    }

    @Override
    public void glGetTexEnviv(int tenv, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTexEnviv");
        details.foundArguments.add(new Object[] { tenv, pname, params });
    }

    @Override
    public void glGetTexEnviv(int tenv, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTexEnviv");
        details.foundArguments.add(new Object[] { tenv, pname, params, params_offset });
    }

    @Override
    public void glGetTexGenfv(int coord, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTexGenfv");
        details.foundArguments.add(new Object[] { coord, pname, params });
    }

    @Override
    public void glGetTexGenfv(int coord, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTexGenfv");
        details.foundArguments.add(new Object[] { coord, pname, params, params_offset });
    }

    @Override
    public void glGetTexGeniv(int coord, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTexGeniv");
        details.foundArguments.add(new Object[] { coord, pname, params });
    }

    @Override
    public void glGetTexGeniv(int coord, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTexGeniv");
        details.foundArguments.add(new Object[] { coord, pname, params, params_offset });
    }

    @Override
    public void glLightModelf(int pname, float param)
    {
        CallDetails details = getMethodDetails("glLightModelf");
        details.foundArguments.add(new Object[] { pname, param });
    }

    @Override
    public void glLightModelfv(int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glLightModelfv");
        details.foundArguments.add(new Object[] { pname, params });
    }

    @Override
    public void glLightModelfv(int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glLightModelfv");
        details.foundArguments.add(new Object[] { pname, params, params_offset });
    }

    @Override
    public void glLightf(int light, int pname, float param)
    {
        CallDetails details = getMethodDetails("glLightf");
        details.foundArguments.add(new Object[] { pname, param });
    }

    @Override
    public void glBeginConditionalRender(int id, int mode)
    {
        CallDetails details = getMethodDetails("glBeginConditionalRender");
        details.foundArguments.add(new Object[] { id, mode });
    }

    @Override
    public void glBeginQueryIndexed(int target, int index, int id)
    {

    }

    @Override
    public void glBindFragDataLocation(int program, int color, String name)
    {
        CallDetails details = getMethodDetails("glBindFragDataLocation");
        details.foundArguments.add(new Object[] { program, color, name });
    }

    @Override
    public void glBlendEquationSeparatei(int buf, int modeRGB, int modeAlpha)
    {

    }

    @Override
    public void glBlendEquationi(int buf, int mode)
    {

    }

    @Override
    public void glBlendFuncSeparatei(int buf, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha)
    {

    }

    @Override
    public void glBlendFunci(int buf, int src, int dst)
    {

    }

    @Override
    public void glBufferAddressRangeNV(int pname, int index, long address, long length)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glBufferPageCommitmentARB(int target, long offset, long size, boolean commit)
    {

    }

    @Override
    public void glClampColor(int target, int clamp)
    {
        CallDetails details = getMethodDetails("glClampColor");
        details.foundArguments.add(new Object[] { target, clamp });
    }

    @Override
    public void glClearBufferData(int target, int internalformat, int format, int type, Buffer data)
    {
        CallDetails details = getMethodDetails("glClearBufferData");
        details.foundArguments.add(new Object[] { target, internalformat, format, type, data });
    }

    @Override
    public void glClearBufferSubData(int target, int internalformat, long offset, long size, int format, int type, Buffer data)
    {
        CallDetails details = getMethodDetails("glClearBufferSubData");
        details.foundArguments.add(new Object[] { target, internalformat, offset, size, format, type, data });
    }

    @Override
    public void glColorFormatNV(int size, int type, int stride)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glColorMaski(int index, boolean r, boolean g, boolean b, boolean a)
    {
        CallDetails details = getMethodDetails("glColorMaski");
        details.foundArguments.add(new Object[] { index, r, g, b, a });
    }

    @Override
    public void glCompressedTexImage1D(int target, int level, int internalformat, int width, int border, int imageSize, Buffer data)
    {
        CallDetails details = getMethodDetails("glCompressedTexImage1D");
        details.foundArguments.add(new Object[] { target, level, internalformat, width, border, imageSize, data });
    }

    @Override
    public void glCompressedTexImage1D(int target, int level, int internalformat, int width, int border, int imageSize, long data_buffer_offset)
    {
        CallDetails details = getMethodDetails("glCompressedTexImage1D");
        details.foundArguments.add(new Object[] { target, level, internalformat, width, border, imageSize, data_buffer_offset });
    }

    @Override
    public void glCompressedTexSubImage1D(int target, int level, int xoffset, int width, int format, int imageSize, Buffer data)
    {
        CallDetails details = getMethodDetails("glCompressedTexSubImage1D");
        details.foundArguments.add(new Object[] { target, level, xoffset, width, format, imageSize, data });
    }

    @Override
    public void glCompressedTexSubImage1D(int target, int level, int xoffset, int width, int format, int imageSize, long data_buffer_offset)
    {
        CallDetails details = getMethodDetails("glCompressedTexSubImage1D");
        details.foundArguments.add(new Object[] { target, level, xoffset, width, format, imageSize, data_buffer_offset });
    }

    @Override
    public void glCopyTexImage1D(int target, int level, int internalformat, int x, int y, int width, int border)
    {
        CallDetails details = getMethodDetails("glCopyTexImage1D");
        details.foundArguments.add(new Object[] { target, level, internalformat, x, y, width, border });
    }

    @Override
    public void glCopyTexSubImage1D(int target, int level, int xoffset, int x, int y, int width)
    {
        CallDetails details = getMethodDetails("glCopyTexSubImage1D");
        details.foundArguments.add(new Object[] { target, level, xoffset, x, y, width });
    }

    @Override
    public void glDebugMessageEnableAMD(int category, int severity, int count, IntBuffer ids, boolean enabled)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDebugMessageEnableAMD(int category, int severity, int count, int[] ids, int ids_offset, boolean enabled)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDebugMessageInsertAMD(int category, int severity, int id, int length, String buf)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDisablei(int target, int index)
    {
        CallDetails details = getMethodDetails("glDisablei");
        details.foundArguments.add(new Object[] { target, index });
    }

    @Override
    public void glDrawBuffer(int mode)
    {
        CallDetails details = getMethodDetails("glDrawBuffer");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public void glDrawTransformFeedback(int mode, int id)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glDrawTransformFeedbackStream(int mode, int id, int stream)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glEdgeFlagFormatNV(int stride)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glEnablei(int target, int index)
    {
        CallDetails details = getMethodDetails("glEnablei");
        details.foundArguments.add(new Object[] { target, index });
    }

    @Override
    public void glEndQueryIndexed(int target, int index)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glEndConditionalRender()
    {
        CallDetails details = getMethodDetails("glEndConditionalRender");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glFogCoordFormatNV(int type, int stride)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glFramebufferTexture1D(int target, int attachment, int textarget, int texture, int level)
    {
        CallDetails details = getMethodDetails("glFramebufferTexture1D");
        details.foundArguments.add(new Object[] { target, attachment, textarget, texture, level });
    }

    @Override
    public void glGetActiveAtomicCounterBufferiv(int program, int bufferIndex, int pname, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetActiveAtomicCounterBufferiv(int program, int bufferIndex, int pname, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetActiveUniformName(int program, int uniformIndex, int bufSize, IntBuffer length, ByteBuffer uniformName)
    {
        CallDetails details = getMethodDetails("glGetActiveUniformName");
        details.foundArguments.add(new Object[] { program, uniformIndex, bufSize, length, uniformName });
    }

    @Override
    public void glGetActiveUniformName(int program, int uniformIndex, int bufSize, int[] length, int length_offset, byte[] uniformName, int uniformName_offset)
    {
        CallDetails details = getMethodDetails("glGetActiveUniformName");
        details.foundArguments.add(new Object[] { program, uniformIndex, bufSize, length, length_offset, uniformName, uniformName_offset });
    }

    @Override
    public void glGetBooleani_v(int target, int index, ByteBuffer data)
    {
        CallDetails details = getMethodDetails("glGetBooleani_v");
        details.foundArguments.add(new Object[] { target, index, data });
    }

    @Override
    public void glGetBooleani_v(int target, int index, byte[] data, int data_offset)
    {
        CallDetails details = getMethodDetails("glGetBooleani_v");
        details.foundArguments.add(new Object[] { target, index, data, data_offset });
    }

    @Override
    public void glGetBufferParameterui64vNV(int target, int pname, LongBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetBufferParameterui64vNV(int target, int pname, long[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetBufferSubData(int target, long offset, long size, Buffer data)
    {
        CallDetails details = getMethodDetails("glGetBufferSubData");
        details.foundArguments.add(new Object[] { target, offset, size, data });
    }

    @Override
    public void glGetCompressedTexImage(int target, int level, Buffer img)
    {
        CallDetails details = getMethodDetails("glGetCompressedTexImage");
        details.foundArguments.add(new Object[] { target, level, img });
    }

    @Override
    public void glGetCompressedTexImage(int target, int level, long img_buffer_offset)
    {
        CallDetails details = getMethodDetails("glGetCompressedTexImage");
        details.foundArguments.add(new Object[] { target, level, img_buffer_offset });
    }

    @Override
    public int glGetDebugMessageLogAMD(int count, int bufsize, IntBuffer categories, IntBuffer severities, IntBuffer ids, IntBuffer lengths, ByteBuffer message)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int glGetDebugMessageLogAMD(int count, int bufsize, int[] categories, int categories_offset, int[] severities, int severities_offset, int[] ids, int ids_offset, int[] lengths, int lengths_offset, byte[] message, int message_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetDoublev(int pname, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glGetDoublev");
        details.foundArguments.add(new Object[] { pname, params });
    }

    @Override
    public void glGetDoublev(int pname, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetDoublev");
        details.foundArguments.add(new Object[] { pname, params, params_offset });
    }

    @Override
    public void glGetIntegerui64i_vNV(int value, int index, LongBuffer result)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetIntegerui64i_vNV(int value, int index, long[] result, int result_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetIntegerui64vNV(int value, LongBuffer result)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetIntegerui64vNV(int value, long[] result, int result_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetInternalformati64v(int target, int internalformat, int pname, int bufSize, LongBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetInternalformati64v(int target, int internalformat, int pname, int bufSize, long[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetNamedBufferParameterui64vNV(int buffer, int pname, LongBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetNamedBufferParameterui64vNV(int buffer, int pname, long[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetQueryIndexediv(int target, int index, int pname, IntBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetQueryIndexediv(int target, int index, int pname, int[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetQueryObjectiv(int id, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetQueryObjectiv");
        details.foundArguments.add(new Object[] { id, pname, params });
    }

    @Override
    public void glGetQueryObjectiv(int id, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetQueryObjectiv");
        details.foundArguments.add(new Object[] { id, pname, params, params_offset });
    }

    @Override
    public void glGetQueryObjectui64v(int id, int pname, LongBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetQueryObjectui64v(int id, int pname, long[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetTexImage(int target, int level, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glGetTexImage");
        details.foundArguments.add(new Object[] { target, level, format, type, pixels });
    }

    @Override
    public void glGetTexImage(int target, int level, int format, int type, long pixels_buffer_offset)
    {
        CallDetails details = getMethodDetails("glGetTexImage");
        details.foundArguments.add(new Object[] { target, level, format, type, pixels_buffer_offset });
    }

    @Override
    public void glGetTexLevelParameterfv(int target, int level, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTexLevelParameterfv");
        details.foundArguments.add(new Object[] { target, level, pname, params });
    }

    @Override
    public void glGetTexLevelParameterfv(int target, int level, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTexLevelParameterfv");
        details.foundArguments.add(new Object[] { target, level, pname, params, params_offset });
    }

    @Override
    public void glGetTexLevelParameteriv(int target, int level, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTexLevelParameteriv");
        details.foundArguments.add(new Object[] { target, level, pname, params });
    }

    @Override
    public void glGetTexLevelParameteriv(int target, int level, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTexLevelParameteriv");
        details.foundArguments.add(new Object[] { target, level, pname, params, params_offset });
    }

    @Override
    public void glGetTexParameterIiv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTexParameterIiv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetTexParameterIiv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTexParameterIiv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetTexParameterIuiv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTexParameterIuiv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetTexParameterIuiv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTexParameterIuiv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetUniformui64vNV(int program, int location, LongBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetUniformui64vNV(int program, int location, long[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVertexAttribLdv(int index, int pname, DoubleBuffer params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVertexAttribLdv(int index, int pname, double[] params, int params_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glGetVertexAttribdv(int index, int pname, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribdv");
        details.foundArguments.add(new Object[] { index, pname, params });
    }

    @Override
    public void glGetVertexAttribdv(int index, int pname, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribdv");
        details.foundArguments.add(new Object[] { index, pname, params, params_offset });
    }

    @Override
    public void glGetnCompressedTexImage(int target, int lod, int bufSize, Buffer img)
    {
        CallDetails details = getMethodDetails("glGetnCompressedTexImage");
        details.foundArguments.add(new Object[] { target, lod, bufSize, img });
    }

    @Override
    public void glGetnTexImage(int target, int level, int format, int type, int bufSize, Buffer img)
    {
        CallDetails details = getMethodDetails("glGetnTexImage");
        details.foundArguments.add(new Object[] { target, level, format, type, bufSize, img });
    }

    @Override
    public void glGetnUniformdv(int program, int location, int bufSize, DoubleBuffer params)
    {
        CallDetails details = getMethodDetails("glGetnUniformdv");
        details.foundArguments.add(new Object[] { program, location, bufSize, params });
    }

    @Override
    public void glGetnUniformdv(int program, int location, int bufSize, double[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetnUniformdv");
        details.foundArguments.add(new Object[] { program, location, bufSize, params, params_offset });
    }

    @Override
    public void glGetnUniformuiv(int program, int location, int bufSize, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetnUniformuiv");
        details.foundArguments.add(new Object[] { program, location, bufSize, params });
    }

    @Override
    public void glGetnUniformuiv(int program, int location, int bufSize, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetnUniformuiv");
        details.foundArguments.add(new Object[] { program, location, bufSize, params, params_offset });
    }

    @Override
    public void glPrimitiveBoundingBox(float minX, float minY, float minZ, float minW, float maxX, float maxY, float maxZ, float maxW)
    {

    }

    @Override
    public long glImportSyncEXT(int external_sync_type, long external_sync, int flags)
    {
        return 0;
    }

    @Override
    public void glIndexFormatNV(int type, int stride)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glInvalidateBufferData(int buffer)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glInvalidateBufferSubData(int buffer, long offset, long length)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glInvalidateTexImage(int texture, int level)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glInvalidateTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean glIsBufferResidentNV(int target)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean glIsEnabledi(int target, int index)
    {
        return false;
    }

    @Override
    public boolean glIsNamedBufferResidentNV(int buffer)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glLogicOp(int opcode)
    {
        CallDetails details = getMethodDetails("glLogicOp");
        details.foundArguments.add(new Object[] { opcode });
    }

    @Override
    public void glMakeBufferNonResidentNV(int target)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMakeBufferResidentNV(int target, int access)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMakeNamedBufferNonResidentNV(int buffer)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMakeNamedBufferResidentNV(int buffer, int access)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMinSampleShading(float value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMultiDrawArrays(int mode, IntBuffer first, IntBuffer count, int drawcount)
    {
        CallDetails details = getMethodDetails("glMultiDrawArrays");
        details.foundArguments.add(new Object[] { mode, first, count, drawcount });
    }

    @Override
    public void glMultiDrawArrays(int mode, int[] first, int first_offset, int[] count, int count_offset, int drawcount)
    {
        CallDetails details = getMethodDetails("glMultiDrawArrays");
        details.foundArguments.add(new Object[] { mode, first, first_offset, count, count_offset, drawcount });
    }

    @Override
    public void glMultiDrawArraysIndirectAMD(int mode, Buffer indirect, int primcount, int stride)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glMultiDrawElements(int mode, IntBuffer count, int type, PointerBuffer indices, int drawcount)
    {
        CallDetails details = getMethodDetails("glMultiDrawElements");
        details.foundArguments.add(new Object[] { mode, count, type, indices, drawcount });
    }

    @Override
    public void glMultiDrawElementsIndirectAMD(int mode, int type, Buffer indirect, int primcount, int stride)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glNamedBufferPageCommitmentARB(int buffer, long offset, long size, boolean commit)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glNamedBufferPageCommitmentEXT(int buffer, long offset, long size, boolean commit)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glNormalFormatNV(int type, int stride)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glPixelStoref(int pname, float param)
    {
        CallDetails details = getMethodDetails("glPixelStoref");
        details.foundArguments.add(new Object[] { pname, param });
    }

    @Override
    public void glMultiTexCoord4f(int target, float s, float t, float r, float q)
    {
        CallDetails details = getMethodDetails("glMultiTexCoord4f");
        details.foundArguments.add(new Object[] { target, s, t, r, q });
    }

    @Override
    public void glNormal3f(float nx, float ny, float nz)
    {
        CallDetails details = getMethodDetails("glNormal3f");
        details.foundArguments.add(new Object[] { nx, ny, nz });
    }

    @Override
    public void glPointParameterf(int pname, float param)
    {
        CallDetails details = getMethodDetails("glPointParameterf");
        details.foundArguments.add(new Object[] { pname, param });
    }

    @Override
    public void glPointParameterfv(int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glPointParameterfv");
        details.foundArguments.add(new Object[] { pname, params });
    }

    @Override
    public void glPointParameterfv(int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glPointParameterfv");
        details.foundArguments.add(new Object[] { pname, params, params_offset });
    }

    @Override
    public void glPointParameteri(int pname, int param)
    {
        CallDetails details = getMethodDetails("glPointParameteri");
        details.foundArguments.add(new Object[] { pname, param });
    }

    @Override
    public void glPointParameteriv(int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glPointParameteriv");
        details.foundArguments.add(new Object[] { pname, params });
    }

    @Override
    public void glPointParameteriv(int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glPointParameteriv");
        details.foundArguments.add(new Object[] { pname, params, params_offset });
    }

    @Override
    public void glPointSize(float size)
    {
        CallDetails details = getMethodDetails("glPointSize");
        details.foundArguments.add(new Object[] { size });
    }

    @Override
    public void glPolygonMode(int face, int mode)
    {
        CallDetails details = getMethodDetails("glPolygonMode");
        details.foundArguments.add(new Object[] { face, mode });
    }

    @Override
    public void glPrimitiveRestartIndex(int index)
    {
        CallDetails details = getMethodDetails("glPrimitiveRestartIndex");
        details.foundArguments.add(new Object[] { index });
    }

    @Override
    public void glProgramUniform1d(int program, int location, double v0)
    {

    }

    @Override
    public void glProgramUniform1dv(int program, int location, int count, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniform1dv(int program, int location, int count, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform2d(int program, int location, double v0, double v1)
    {

    }

    @Override
    public void glProgramUniform2dv(int program, int location, int count, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniform2dv(int program, int location, int count, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform3d(int program, int location, double v0, double v1, double v2)
    {

    }

    @Override
    public void glProgramUniform3dv(int program, int location, int count, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniform3dv(int program, int location, int count, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform4d(int program, int location, double v0, double v1, double v2, double v3)
    {

    }

    @Override
    public void glProgramUniform4dv(int program, int location, int count, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniform4dv(int program, int location, int count, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix2dv(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix2dv(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix2x3dv(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix2x3dv(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix2x4dv(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix2x4dv(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix3dv(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix3dv(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix3x2dv(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix3x2dv(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix3x4dv(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix3x4dv(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix4dv(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix4dv(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix4x2dv(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix4x2dv(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix4x3dv(int program, int location, int count, boolean transpose, DoubleBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix4x3dv(int program, int location, int count, boolean transpose, double[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformui64NV(int program, int location, long value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramUniformui64vNV(int program, int location, int count, LongBuffer value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProgramUniformui64vNV(int program, int location, int count, long[] value, int value_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glProvokingVertex(int mode)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glSecondaryColorFormatNV(int size, int type, int stride)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glSetMultisamplefvAMD(int pname, int index, FloatBuffer val)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glSetMultisamplefvAMD(int pname, int index, float[] val, int val_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glStencilOpValueAMD(int face, int value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTessellationFactorAMD(float factor)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTessellationModeAMD(int mode)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexBuffer(int target, int internalformat, int buffer)
    {
        CallDetails details = getMethodDetails("glTexBuffer");
        details.foundArguments.add(new Object[] { target, internalformat, buffer });
    }

    @Override
    public void glTexCoordFormatNV(int size, int type, int stride)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexImage1D(int target, int level, int internalFormat, int width, int border, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glTexImage1D");
        details.foundArguments.add(new Object[] { target, level, internalFormat, width, border, format, type, pixels });
    }

    @Override
    public void glTexImage1D(int target, int level, int internalFormat, int width, int border, int format, int type, long pixels_buffer_offset)
    {
        CallDetails details = getMethodDetails("glTexImage1D");
        details.foundArguments.add(new Object[] { target, level, internalFormat, width, border, format, type, pixels_buffer_offset });
    }

    @Override
    public void glTexImage2DMultisampleCoverageNV(int target, int coverageSamples, int colorSamples, int internalFormat, int width, int height, boolean fixedSampleLocations)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexImage3DMultisampleCoverageNV(int target, int coverageSamples, int colorSamples, int internalFormat, int width, int height, int depth, boolean fixedSampleLocations)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexPageCommitmentARB(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, boolean resident)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexStorage3DMultisample(int target, int samples, int internalformat, int width, int height, int depth, boolean fixedsamplelocations)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexParameterIiv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glTexParameterIiv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glTexParameterIiv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glTexParameterIiv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glTexParameterIuiv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glTexParameterIuiv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glTexParameterIuiv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glTexParameterIuiv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glTexSubImage1D");
        details.foundArguments.add(new Object[] { target, level, xoffset, width, format, type, pixels });
    }

    @Override
    public void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, long pixels_buffer_offset)
    {
        CallDetails details = getMethodDetails("glTexSubImage1D");
        details.foundArguments.add(new Object[] { target, level, xoffset, width, format, type, pixels_buffer_offset });
    }

    @Override
    public void glTextureBufferRangeEXT(int texture, int target, int internalformat, int buffer, long offset, long size)
    {
        CallDetails details = getMethodDetails("glTextureBufferRangeEXT");
        details.foundArguments.add(new Object[] { texture, target, internalformat, buffer, offset, size });
    }

    @Override
    public void glTextureImage2DMultisampleCoverageNV(int texture, int target, int coverageSamples, int colorSamples, int internalFormat, int width, int height, boolean fixedSampleLocations)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTextureImage2DMultisampleNV(int texture, int target, int samples, int internalFormat, int width, int height, boolean fixedSampleLocations)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTextureImage3DMultisampleCoverageNV(int texture, int target, int coverageSamples, int colorSamples, int internalFormat, int width, int height, int depth, boolean fixedSampleLocations)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTextureImage3DMultisampleNV(int texture, int target, int samples, int internalFormat, int width, int height, int depth, boolean fixedSampleLocations)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniformui64NV(int location, long value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniformui64vNV(int location, int count, LongBuffer value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glUniformui64vNV(int location, int count, long[] value, int value_offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexArrayBindVertexBufferEXT(int vaobj, int bindingindex, int buffer, long offset, int stride)
    {
        CallDetails details = getMethodDetails("glVertexArrayBindVertexBufferEXT");
        details.foundArguments.add(new Object[] { vaobj, bindingindex, buffer, offset, stride });
    }

    @Override
    public void glVertexArrayVertexAttribBindingEXT(int vaobj, int attribindex, int bindingindex)
    {
        CallDetails details = getMethodDetails("glVertexArrayVertexAttribBindingEXT");
        details.foundArguments.add(new Object[] { vaobj, attribindex, bindingindex });
    }

    @Override
    public void glVertexArrayVertexAttribDivisorEXT(int vaobj, int index, int divisor)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexArrayVertexAttribFormatEXT(int vaobj, int attribindex, int size, int type, boolean normalized, int relativeoffset)
    {
        CallDetails details = getMethodDetails("glVertexArrayVertexAttribFormatEXT");
        details.foundArguments.add(new Object[] { vaobj, attribindex, size, type, normalized, relativeoffset });
    }

    @Override
    public void glVertexArrayVertexAttribIFormatEXT(int vaobj, int attribindex, int size, int type, int relativeoffset)
    {
        CallDetails details = getMethodDetails("glVertexArrayVertexAttribIFormatEXT");
        details.foundArguments.add(new Object[] { vaobj, attribindex, size, type, relativeoffset });
    }

    @Override
    public void glVertexArrayVertexAttribLFormatEXT(int vaobj, int attribindex, int size, int type, int relativeoffset)
    {
        CallDetails details = getMethodDetails("glVertexArrayVertexAttribLFormatEXT");
        details.foundArguments.add(new Object[] { vaobj, attribindex, size, type, relativeoffset });
    }

    @Override
    public void glVertexArrayVertexAttribLOffsetEXT(int vaobj, int buffer, int index, int size, int type, int stride, long offset)
    {
        CallDetails details = getMethodDetails("glVertexArrayVertexAttribLOffsetEXT");
        details.foundArguments.add(new Object[] { vaobj, buffer, index, size, type, stride, offset });
    }

    @Override
    public void glVertexArrayVertexBindingDivisorEXT(int vaobj, int bindingindex, int divisor)
    {
        CallDetails details = getMethodDetails("glVertexArrayVertexBindingDivisorEXT");
        details.foundArguments.add(new Object[] { vaobj, bindingindex, divisor });
    }

    @Override
    public void glVertexAttrib1d(int index, double x)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1d");
        details.foundArguments.add(new Object[] { index, x });
    }

    @Override
    public void glVertexAttrib1dv(int index, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1dv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib1dv(int index, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1dv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib1s(int index, short x)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1s");
        details.foundArguments.add(new Object[] { index, x });
    }

    @Override
    public void glVertexAttrib1sv(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1sv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib1sv(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1sv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib2d(int index, double x, double y)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2d");
        details.foundArguments.add(new Object[] { index, x, y });
    }

    @Override
    public void glVertexAttrib2dv(int index, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2dv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib2dv(int index, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2dv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib2s(int index, short x, short y)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2s");
        details.foundArguments.add(new Object[] { index, x, y });
    }

    @Override
    public void glVertexAttrib2sv(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2sv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib2sv(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2sv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib3d(int index, double x, double y, double z)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3d");
        details.foundArguments.add(new Object[] { index, x, y, z });
    }

    @Override
    public void glVertexAttrib3dv(int index, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3dv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib3dv(int index, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3dv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib3s(int index, short x, short y, short z)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3s");
        details.foundArguments.add(new Object[] { index, x, y, z });
    }

    @Override
    public void glVertexAttrib3sv(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3sv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib3sv(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3sv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4Nbv(int index, ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4Nbv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4Nbv(int index, byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4Nbv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4Niv(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4Niv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4Niv(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4Niv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4Nsv(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4Nsv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4Nsv(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4Nsv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4Nub(int index, byte x, byte y, byte z, byte w)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4Nub");
        details.foundArguments.add(new Object[] { index, x, y, z, w });
    }

    @Override
    public void glVertexAttrib4Nubv(int index, ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4Nubv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4Nubv(int index, byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4Nubv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4Nuiv(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4Nuiv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4Nuiv(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4Nuiv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4Nusv(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4Nusv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4Nusv(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4Nusv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4bv(int index, ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4bv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4bv(int index, byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4bv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4d(int index, double x, double y, double z, double w)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4d");
        details.foundArguments.add(new Object[] { index, x, y, z, w });
    }

    @Override
    public void glVertexAttrib4dv(int index, DoubleBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4dv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4dv(int index, double[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4dv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4iv(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4iv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4iv(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4iv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4s(int index, short x, short y, short z, short w)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4s");
        details.foundArguments.add(new Object[] { index, x, y, z, w });
    }

    @Override
    public void glVertexAttrib4sv(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4sv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4sv(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4sv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4ubv(int index, ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4ubv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4ubv(int index, byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4ubv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4uiv(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4uiv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4uiv(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4uiv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttrib4usv(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4usv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttrib4usv(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4usv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribFormatNV(int index, int size, int type, boolean normalized, int stride)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribI1i(int index, int x)
    {
        CallDetails details = getMethodDetails("glVertexAttribI1i");
        details.foundArguments.add(new Object[] { index, x });
    }

    @Override
    public void glVertexAttribI1iv(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI1iv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI1iv(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI1iv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI1ui(int index, int x)
    {
        CallDetails details = getMethodDetails("glVertexAttribI1ui");
        details.foundArguments.add(new Object[] { index, x });
    }

    @Override
    public void glVertexAttribI1uiv(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI1uiv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI1uiv(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI1uiv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI2i(int index, int x, int y)
    {
        CallDetails details = getMethodDetails("glVertexAttribI2i");
        details.foundArguments.add(new Object[] { index, x, y });
    }

    @Override
    public void glVertexAttribI2iv(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI2iv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI2iv(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI2iv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI2ui(int index, int x, int y)
    {
        CallDetails details = getMethodDetails("glVertexAttribI2ui");
        details.foundArguments.add(new Object[] { index, x, y });
    }

    @Override
    public void glVertexAttribI2uiv(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI2uiv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI2uiv(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI2uiv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI3i(int index, int x, int y, int z)
    {
        CallDetails details = getMethodDetails("glVertexAttribI3i");
        details.foundArguments.add(new Object[] { index, x, y, z });
    }

    @Override
    public void glVertexAttribI3iv(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI3iv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI3iv(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI3iv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI3ui(int index, int x, int y, int z)
    {
        CallDetails details = getMethodDetails("glVertexAttribI3ui");
        details.foundArguments.add(new Object[] { index, x, y, z });
    }

    @Override
    public void glVertexAttribI3uiv(int index, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI3uiv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI3uiv(int index, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI3uiv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI4bv(int index, ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4bv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI4bv(int index, byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4bv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI4sv(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4sv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI4sv(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4sv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI4ubv(int index, ByteBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4ubv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI4ubv(int index, byte[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4ubv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribI4usv(int index, ShortBuffer v)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4usv");
        details.foundArguments.add(new Object[] { index, v });
    }

    @Override
    public void glVertexAttribI4usv(int index, short[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribI4usv");
        details.foundArguments.add(new Object[] { index, v, v_offset });
    }

    @Override
    public void glVertexAttribIFormatNV(int index, int size, int type, int stride)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glVertexAttribL1d(int index, double x)
    {

    }

    @Override
    public void glVertexAttribL1dv(int index, DoubleBuffer v)
    {

    }

    @Override
    public void glVertexAttribL1dv(int index, double[] v, int v_offset)
    {

    }

    @Override
    public void glVertexAttribL2d(int index, double x, double y)
    {

    }

    @Override
    public void glVertexAttribL2dv(int index, DoubleBuffer v)
    {

    }

    @Override
    public void glVertexAttribL2dv(int index, double[] v, int v_offset)
    {

    }

    @Override
    public void glVertexAttribL3d(int index, double x, double y, double z)
    {

    }

    @Override
    public void glVertexAttribL3dv(int index, DoubleBuffer v)
    {

    }

    @Override
    public void glVertexAttribL3dv(int index, double[] v, int v_offset)
    {

    }

    @Override
    public void glVertexAttribL4d(int index, double x, double y, double z, double w)
    {

    }

    @Override
    public void glVertexAttribL4dv(int index, DoubleBuffer v)
    {

    }

    @Override
    public void glVertexAttribL4dv(int index, double[] v, int v_offset)
    {

    }

    @Override
    public void glVertexAttribLPointer(int index, int size, int type, int stride, long pointer_buffer_offset)
    {

    }

    @Override
    public void glVertexFormatNV(int size, int type, int stride)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void glTexEnvf(int target, int pname, float param)
    {
        CallDetails details = getMethodDetails("glTexEnvf");
        details.foundArguments.add(new Object[] { target, pname, param });
    }

    @Override
    public void glTexEnvfv(int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glTexEnvfv");
        details.foundArguments.add(new Object[] { target, pname });
    }

    @Override
    public void glTexEnvfv(int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glTexEnvfv");
        details.foundArguments.add(new Object[] { target, pname, params_offset });
    }

    @Override
    public void glTexEnvi(int target, int pname, int param)
    {
        CallDetails details = getMethodDetails("glTexEnvi");
        details.foundArguments.add(new Object[] { target, pname, param });
    }

    @Override
    public void glTexEnviv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glTexEnviv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glTexEnviv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glTexEnviv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glTexGenf(int coord, int pname, float param)
    {
        CallDetails details = getMethodDetails("glTexGenfv");
        details.foundArguments.add(new Object[] { coord, pname, param });
    }

    @Override
    public void glTexGenfv(int coord, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glTexGenfv");
        details.foundArguments.add(new Object[] { coord, pname, params });
    }

    @Override
    public void glTexGenfv(int coord, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glTexGenfv");
        details.foundArguments.add(new Object[] { coord, pname, params, params_offset });
    }

    @Override
    public void glTexGeni(int coord, int pname, int param)
    {
        CallDetails details = getMethodDetails("glTexGeni");
        details.foundArguments.add(new Object[] { coord, pname, param });
    }

    @Override
    public void glTexGeniv(int coord, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glTexGeniv");
        details.foundArguments.add(new Object[] { coord, pname, params });
    }

    @Override
    public void glTexGeniv(int coord, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glTexGeniv");
        details.foundArguments.add(new Object[] { coord, pname, params, params_offset });
    }

    @Override
    public void glOrtho(double left, double right, double bottom, double top, double near_val, double far_val)
    {
        CallDetails details = getMethodDetails("glOrtho");
        details.foundArguments.add(new Object[] { left, right, bottom, top, near_val, far_val });
    }

    @Override
    public void glFrustum(double left, double right, double bottom, double top, double zNear, double zFar)
    {
        CallDetails details = getMethodDetails("glFrustum");
        details.foundArguments.add(new Object[] { left, right, bottom, top, zNear, zFar });
    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices)
    {

    }

    @Override
    public void glActiveShaderProgram(int pipeline, int program)
    {

    }

    @Override
    public void glAttachShader(int program, int shader)
    {
        CallDetails details = getMethodDetails("glAttachShader");
        details.foundArguments.add(new Object[] { program, shader });
    }

    @Override
    public void glBeginQuery(int target, int id)
    {
        CallDetails details = getMethodDetails("glBeginQuery");
        details.foundArguments.add(new Object[] { target, id });
    }

    @Override
    public void glBindAttribLocation(int program, int index, String name)
    {
        CallDetails details = getMethodDetails("glBindAttribLocation");
        details.foundArguments.add(new Object[] { program, index, name });
    }

    @Override
    public void glBindProgramPipeline(int pipeline)
    {

    }

    @Override
    public void glBlendColor(float red, float green, float blue, float alpha)
    {
        CallDetails details = getMethodDetails("glBlendColor");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glCompileShader(int shader)
    {
        CallDetails details = getMethodDetails("glCompileShader");
        details.foundArguments.add(new Object[] { shader });
    }

    @Override
    public void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int imageSize, Buffer data)
    {
        CallDetails details = getMethodDetails("glCompressedTexImage3D");
        details.foundArguments.add(new Object[] { target, level, internalformat, width, height, depth, border, imageSize, data });
    }

    @Override
    public void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int imageSize, long data_buffer_offset)
    {
        CallDetails details = getMethodDetails("glCompressedTexImage3D");
        details.foundArguments.add(new Object[] { target, level, internalformat, width, height, depth, border, imageSize, data_buffer_offset });
    }

    @Override
    public void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, Buffer data)
    {
        CallDetails details = getMethodDetails("glCompressedTexSubImage3D");
        details.foundArguments.add(new Object[] { target, level, xoffset, yoffset, zoffset, width, height, depth, format, imageSize, data });
    }

    @Override
    public void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, long data_buffer_offset)
    {
        CallDetails details = getMethodDetails("glCompressedTexSubImage3D");
        details.foundArguments.add(new Object[] { target, level, xoffset, yoffset, zoffset, width, height, depth, format, imageSize, data_buffer_offset });
    }

    @Override
    public void glCopyImageSubData(int srcName, int srcTarget, int srcLevel, int srcX, int srcY, int srcZ, int dstName, int dstTarget, int dstLevel, int dstX, int dstY, int dstZ, int srcWidth, int srcHeight, int srcDepth)
    {

    }

    @Override
    public void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height)
    {
        CallDetails details = getMethodDetails("glCopyTexSubImage3D");
        details.foundArguments.add(new Object[] { target, level, xoffset, yoffset, zoffset, x, y, width, height });
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
    public int glCreateShaderProgramv(int type, int count, String[] strings)
    {
        return 0;
    }

    @Override
    public void glDebugMessageControl(int source, int type, int severity, int count, IntBuffer ids, boolean enabled)
    {
        CallDetails details = getMethodDetails("glDebugMessageControl");
        details.foundArguments.add(new Object[] { source, type, severity, count, ids, enabled });
    }

    @Override
    public void glDebugMessageControl(int source, int type, int severity, int count, int[] ids, int ids_offset, boolean enabled)
    {
        CallDetails details = getMethodDetails("glDebugMessageControl");
        details.foundArguments.add(new Object[] { source, type, severity, count, ids, ids_offset, enabled });
    }

    @Override
    public void glDebugMessageInsert(int source, int type, int id, int severity, int length, String buf)
    {
        CallDetails details = getMethodDetails("glDebugMessageInsert");
        details.foundArguments.add(new Object[] { source, type, id, severity, length, buf });
    }

    @Override
    public void glDeleteProgram(int program)
    {
        CallDetails details = getMethodDetails("glDeleteProgram");
        details.foundArguments.add(new Object[] { program });
    }

    @Override
    public void glDeleteProgramPipelines(int n, IntBuffer pipelines)
    {

    }

    @Override
    public void glDeleteProgramPipelines(int n, int[] pipelines, int pipelines_offset)
    {

    }

    @Override
    public void glDeleteQueries(int n, IntBuffer ids)
    {
        CallDetails details = getMethodDetails("glDeleteQueries");
        details.foundArguments.add(new Object[] { n, ids });
    }

    @Override
    public void glDeleteQueries(int n, int[] ids, int ids_offset)
    {
        CallDetails details = getMethodDetails("glDeleteQueries");
        details.foundArguments.add(new Object[] { n, ids, ids_offset });
    }

    @Override
    public void glDeleteShader(int shader)
    {
        CallDetails details = getMethodDetails("glDeleteShader");
        details.foundArguments.add(new Object[] { shader });
    }

    @Override
    public void glDetachShader(int program, int shader)
    {
        CallDetails details = getMethodDetails("glDetachShader");
        details.foundArguments.add(new Object[] { program, shader });
    }

    @Override
    public void glDisableVertexAttribArray(int index)
    {
        CallDetails details = getMethodDetails("glDisableVertexAttribArray");
        details.foundArguments.add(new Object[] { index });
    }

    @Override
    public void glDrawArraysInstancedBaseInstance(int mode, int first, int count, int instancecount, int baseinstance)
    {

    }

    @Override
    public void glEnableVertexAttribArray(int index)
    {
        CallDetails details = getMethodDetails("glEnableVertexAttribArray");
        details.foundArguments.add(new Object[] { index });
    }

    @Override
    public void glEndQuery(int target)
    {
        CallDetails details = getMethodDetails("glEndQuery");
        details.foundArguments.add(new Object[] { target });
    }

    @Override
    public void glFramebufferTexture3D(int target, int attachment, int textarget, int texture, int level, int zoffset)
    {
        CallDetails details = getMethodDetails("glFramebufferTexture3D");
        details.foundArguments.add(new Object[] { target, attachment, textarget, texture, level, zoffset });
    }

    @Override
    public void glGenProgramPipelines(int n, IntBuffer pipelines)
    {

    }

    @Override
    public void glGenProgramPipelines(int n, int[] pipelines, int pipelines_offset)
    {

    }

    @Override
    public void glGenQueries(int n, IntBuffer ids)
    {
        CallDetails details = getMethodDetails("glGenQueries");
        details.foundArguments.add(new Object[] { n, ids });
    }

    @Override
    public void glGenQueries(int n, int[] ids, int ids_offset)
    {
        CallDetails details = getMethodDetails("glGenQueries");
        details.foundArguments.add(new Object[] { n, ids, ids_offset });
    }

    @Override
    public void glGetActiveAttrib(int program, int index, int bufsize, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name)
    {
        CallDetails details = getMethodDetails("glGetActiveAttrib");
        details.foundArguments.add(new Object[] { program, index,  bufsize, length, size, type, name });
    }

    @Override
    public void glGetActiveAttrib(int program, int index, int bufsize, int[] length, int length_offset, int[] size, int size_offset, int[] type, int type_offset, byte[] name, int name_offset)
    {
        CallDetails details = getMethodDetails("glGetActiveAttrib");
        details.foundArguments.add(new Object[] { program, index,  bufsize, length, length_offset, size, size_offset, type, type_offset, name, name_offset});
    }

    @Override
    public void glGetActiveUniform(int program, int index, int bufsize, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name)
    {
        CallDetails details = getMethodDetails("glGetActiveUniform");
        details.foundArguments.add(new Object[] { program, index,  bufsize, length, size, type, name });
    }

    @Override
    public void glGetActiveUniform(int program, int index, int bufsize, int[] length, int length_offset, int[] size, int size_offset, int[] type, int type_offset, byte[] name, int name_offset)
    {
        CallDetails details = getMethodDetails("glGetActiveUniform");
        details.foundArguments.add(new Object[] { program, index,  bufsize, length, length_offset, size, size_offset, type, type_offset, name, name_offset});
    }

    @Override
    public void glGetAttachedShaders(int program, int maxcount, IntBuffer count, IntBuffer shaders)
    {
        CallDetails details = getMethodDetails("glGetAttachedShaders");
        details.foundArguments.add(new Object[] { program, maxcount, count, shaders });
    }

    @Override
    public void glGetAttachedShaders(int program, int maxcount, int[] count, int count_offset, int[] shaders, int shaders_offset)
    {
        CallDetails details = getMethodDetails("glGetAttachedShaders");
        details.foundArguments.add(new Object[] { program, maxcount, count, count_offset, shaders, shaders_offset });
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
    public void glGetMultisamplefv(int pname, int index, FloatBuffer val)
    {

    }

    @Override
    public void glGetMultisamplefv(int pname, int index, float[] val, int val_offset)
    {

    }

    @Override
    public void glGetObjectLabel(int identifier, int name, int bufSize, IntBuffer length, ByteBuffer label)
    {
        CallDetails details = getMethodDetails("glGetObjectLabel");
        details.foundArguments.add(new Object[] { identifier, name, bufSize, length, label });
    }

    @Override
    public void glGetObjectLabel(int identifier, int name, int bufSize, int[] length, int length_offset, byte[] label, int label_offset)
    {
        CallDetails details = getMethodDetails("glGetObjectLabel");
        details.foundArguments.add(new Object[] { identifier, name, bufSize, length, length_offset, label, label_offset });
    }

    @Override
    public void glGetObjectPtrLabel(Buffer ptr, int bufSize, IntBuffer length, ByteBuffer label)
    {
        CallDetails details = getMethodDetails("glGetObjectPtrLabel");
        details.foundArguments.add(new Object[] { ptr, bufSize, length, label });
    }

    @Override
    public void glGetObjectPtrLabel(Buffer ptr, int bufSize, int[] length, int length_offset, byte[] label, int label_offset)
    {
        CallDetails details = getMethodDetails("glGetObjectPtrLabel");
        details.foundArguments.add(new Object[] { ptr, bufSize, length, length_offset, label, label_offset });
    }

    @Override
    public void glGetProgramBinary(int program, int bufSize, IntBuffer length, IntBuffer binaryFormat, Buffer binary)
    {
        CallDetails details = getMethodDetails("glGetProgramBinary");
        details.foundArguments.add(new Object[] { program, bufSize, length, binaryFormat });
    }

    @Override
    public void glGetProgramBinary(int program, int bufSize, int[] length, int length_offset, int[] binaryFormat, int binaryFormat_offset, Buffer binary)
    {
        CallDetails details = getMethodDetails("glGetProgramBinary");
        details.foundArguments.add(new Object[] { program, bufSize, length, length_offset, binaryFormat, binaryFormat_offset });
    }

    @Override
    public void glGetProgramInfoLog(int program, int bufsize, IntBuffer length, ByteBuffer infolog)
    {
        CallDetails details = getMethodDetails("glGetProgramInfoLog");
        details.foundArguments.add(new Object[] { program, bufsize, length, infolog });
    }

    @Override
    public void glGetProgramInfoLog(int program, int bufsize, int[] length, int length_offset, byte[] infolog, int infolog_offset)
    {
        CallDetails details = getMethodDetails("glGetProgramInfoLog");
        details.foundArguments.add(new Object[] { program, bufsize, length, length_offset, infolog, infolog_offset });
    }

    @Override
    public void glGetProgramPipelineInfoLog(int pipeline, int bufSize, IntBuffer length, ByteBuffer infoLog)
    {

    }

    @Override
    public void glGetProgramPipelineInfoLog(int pipeline, int bufSize, int[] length, int length_offset, byte[] infoLog, int infoLog_offset)
    {

    }

    @Override
    public void glGetProgramPipelineiv(int pipeline, int pname, IntBuffer params)
    {

    }

    @Override
    public void glGetProgramPipelineiv(int pipeline, int pname, int[] params, int params_offset)
    {

    }

    @Override
    public void glGetProgramiv(int program, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetProgramiv");
        details.foundArguments.add(new Object[] { program, pname, params, params });
    }

    @Override
    public void glGetProgramiv(int program, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetProgramiv");
        details.foundArguments.add(new Object[] { program, pname, params, params_offset });
    }

    @Override
    public void glGetQueryObjecti64v(int id, int pname, LongBuffer params)
    {

    }

    @Override
    public void glGetQueryObjecti64v(int id, int pname, long[] params, int params_offset)
    {

    }

    @Override
    public void glGetQueryObjectuiv(int id, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetQueryObjectuiv");
        details.foundArguments.add(new Object[] { id, pname, params, params });
    }

    @Override
    public void glGetQueryObjectuiv(int id, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetQueryObjectuiv");
        details.foundArguments.add(new Object[] { id, pname, params, params_offset });
    }

    @Override
    public void glGetQueryiv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetQueryiv");
        details.foundArguments.add(new Object[] { target, pname, params, params });
    }

    @Override
    public void glGetQueryiv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetQueryiv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetSamplerParameterIiv(int sampler, int pname, IntBuffer params)
    {

    }

    @Override
    public void glGetSamplerParameterIiv(int sampler, int pname, int[] params, int params_offset)
    {

    }

    @Override
    public void glGetSamplerParameterIuiv(int sampler, int pname, IntBuffer params)
    {

    }

    @Override
    public void glGetSamplerParameterIuiv(int sampler, int pname, int[] params, int params_offset)
    {

    }

    @Override
    public void glGetShaderInfoLog(int shader, int bufsize, IntBuffer length, ByteBuffer infolog)
    {
        CallDetails details = getMethodDetails("glGetShaderInfoLog");
        details.foundArguments.add(new Object[] { shader, bufsize, length, infolog });
    }

    @Override
    public void glGetShaderInfoLog(int shader, int bufsize, int[] length, int length_offset, byte[] infolog, int infolog_offset)
    {
        CallDetails details = getMethodDetails("glGetShaderInfoLog");
        details.foundArguments.add(new Object[] { shader, bufsize, length, length_offset, infolog, infolog_offset });
    }

    @Override
    public void glGetShaderSource(int shader, int bufsize, IntBuffer length, ByteBuffer source)
    {
        CallDetails details = getMethodDetails("glGetShaderSource");
        details.foundArguments.add(new Object[] { shader, bufsize, length, source });
    }

    @Override
    public void glGetShaderSource(int shader, int bufsize, int[] length, int length_offset, byte[] source, int source_offset)
    {
        CallDetails details = getMethodDetails("glGetShaderSource");
        details.foundArguments.add(new Object[] { shader, bufsize, length, length_offset, source, source_offset });
    }

    @Override
    public void glGetShaderiv(int shader, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetShaderiv");
        details.foundArguments.add(new Object[] { shader, pname, params, params });
    }

    @Override
    public void glGetShaderiv(int shader, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetShaderiv");
        details.foundArguments.add(new Object[] { shader, pname, params, params_offset });
    }

    @Override
    public int glGetUniformLocation(int program, String name)
    {
        return 0;
    }

    @Override
    public void glGetUniformfv(int program, int location, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetUniformfv");
        details.foundArguments.add(new Object[] { program, location, params, params });
    }

    @Override
    public void glGetUniformfv(int program, int location, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetUniformfv");
        details.foundArguments.add(new Object[] { program, location, params, params_offset });
    }

    @Override
    public void glGetUniformiv(int program, int location, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetUniformiv");
        details.foundArguments.add(new Object[] { program, location, params, params });
    }

    @Override
    public void glGetUniformiv(int program, int location, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetUniformiv");
        details.foundArguments.add(new Object[] { program, location, params, params_offset });
    }

    @Override
    public void glGetVertexAttribfv(int index, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribfv");
        details.foundArguments.add(new Object[] { index, pname, params, params });
    }

    @Override
    public void glGetVertexAttribfv(int index, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribfv");
        details.foundArguments.add(new Object[] { index, pname, params, params_offset });
    }

    @Override
    public void glGetVertexAttribiv(int index, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribiv");
        details.foundArguments.add(new Object[] { index, pname, params });
    }

    @Override
    public void glGetVertexAttribiv(int index, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetVertexAttribiv");
        details.foundArguments.add(new Object[] { index, pname, params, params_offset });
    }

    @Override
    public boolean glIsProgram(int program)
    {
        return false;
    }

    @Override
    public boolean glIsProgramPipeline(int pipeline)
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
        CallDetails details = getMethodDetails("glLinkProgram");
        details.foundArguments.add(new Object[] { program });
    }

    @Override
    public void glObjectLabel(int identifier, int name, int length, ByteBuffer label)
    {
        CallDetails details = getMethodDetails("glObjectLabel");
        details.foundArguments.add(new Object[] { identifier, name, length, label });
    }

    @Override
    public void glObjectLabel(int identifier, int name, int length, byte[] label, int label_offset)
    {
        CallDetails details = getMethodDetails("glObjectLabel");
        details.foundArguments.add(new Object[] { identifier, name, length, label, label_offset });
    }

    @Override
    public void glObjectPtrLabel(Buffer ptr, int length, ByteBuffer label)
    {
        CallDetails details = getMethodDetails("glObjectPtrLabel");
        details.foundArguments.add(new Object[] { ptr, length, label });
    }

    @Override
    public void glObjectPtrLabel(Buffer ptr, int length, byte[] label, int label_offset)
    {
        CallDetails details = getMethodDetails("glObjectPtrLabel");
        details.foundArguments.add(new Object[] { ptr, length, label, label_offset });
    }

    @Override
    public void glPopDebugGroup()
    {
        CallDetails details = getMethodDetails("glPopDebugGroup");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glProgramBinary(int program, int binaryFormat, Buffer binary, int length)
    {
        CallDetails details = getMethodDetails("glProgramBinary");
        details.foundArguments.add(new Object[] { program, binaryFormat, binary, length });
    }

    @Override
    public void glProgramParameteri(int program, int pname, int value)
    {

    }

    @Override
    public void glProgramUniform1f(int program, int location, float v0)
    {

    }

    @Override
    public void glProgramUniform1fv(int program, int location, int count, FloatBuffer value)
    {

    }

    @Override
    public void glProgramUniform1fv(int program, int location, int count, float[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform1i(int program, int location, int v0)
    {

    }

    @Override
    public void glProgramUniform1iv(int program, int location, int count, IntBuffer value)
    {

    }

    @Override
    public void glProgramUniform1iv(int program, int location, int count, int[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform1ui(int program, int location, int v0)
    {

    }

    @Override
    public void glProgramUniform1uiv(int program, int location, int count, IntBuffer value)
    {

    }

    @Override
    public void glProgramUniform1uiv(int program, int location, int count, int[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform2f(int program, int location, float v0, float v1)
    {

    }

    @Override
    public void glProgramUniform2fv(int program, int location, int count, FloatBuffer value)
    {

    }

    @Override
    public void glProgramUniform2fv(int program, int location, int count, float[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform2i(int program, int location, int v0, int v1)
    {

    }

    @Override
    public void glProgramUniform2iv(int program, int location, int count, IntBuffer value)
    {

    }

    @Override
    public void glProgramUniform2iv(int program, int location, int count, int[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform2ui(int program, int location, int v0, int v1)
    {

    }

    @Override
    public void glProgramUniform2uiv(int program, int location, int count, IntBuffer value)
    {

    }

    @Override
    public void glProgramUniform2uiv(int program, int location, int count, int[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform3f(int program, int location, float v0, float v1, float v2)
    {

    }

    @Override
    public void glProgramUniform3fv(int program, int location, int count, FloatBuffer value)
    {

    }

    @Override
    public void glProgramUniform3fv(int program, int location, int count, float[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform3i(int program, int location, int v0, int v1, int v2)
    {

    }

    @Override
    public void glProgramUniform3iv(int program, int location, int count, IntBuffer value)
    {

    }

    @Override
    public void glProgramUniform3iv(int program, int location, int count, int[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform3ui(int program, int location, int v0, int v1, int v2)
    {

    }

    @Override
    public void glProgramUniform3uiv(int program, int location, int count, IntBuffer value)
    {

    }

    @Override
    public void glProgramUniform3uiv(int program, int location, int count, int[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform4f(int program, int location, float v0, float v1, float v2, float v3)
    {

    }

    @Override
    public void glProgramUniform4fv(int program, int location, int count, FloatBuffer value)
    {

    }

    @Override
    public void glProgramUniform4fv(int program, int location, int count, float[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform4i(int program, int location, int v0, int v1, int v2, int v3)
    {

    }

    @Override
    public void glProgramUniform4iv(int program, int location, int count, IntBuffer value)
    {

    }

    @Override
    public void glProgramUniform4iv(int program, int location, int count, int[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniform4ui(int program, int location, int v0, int v1, int v2, int v3)
    {

    }

    @Override
    public void glProgramUniform4uiv(int program, int location, int count, IntBuffer value)
    {

    }

    @Override
    public void glProgramUniform4uiv(int program, int location, int count, int[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix2fv(int program, int location, int count, boolean transpose, FloatBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix2fv(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix2x3fv(int program, int location, int count, boolean transpose, FloatBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix2x3fv(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix2x4fv(int program, int location, int count, boolean transpose, FloatBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix2x4fv(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix3fv(int program, int location, int count, boolean transpose, FloatBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix3fv(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix3x2fv(int program, int location, int count, boolean transpose, FloatBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix3x2fv(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix3x4fv(int program, int location, int count, boolean transpose, FloatBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix3x4fv(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix4fv(int program, int location, int count, boolean transpose, FloatBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix4fv(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix4x2fv(int program, int location, int count, boolean transpose, FloatBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix4x2fv(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {

    }

    @Override
    public void glProgramUniformMatrix4x3fv(int program, int location, int count, boolean transpose, FloatBuffer value)
    {

    }

    @Override
    public void glProgramUniformMatrix4x3fv(int program, int location, int count, boolean transpose, float[] value, int value_offset)
    {

    }

    @Override
    public void glApplyFramebufferAttachmentCMAAINTEL()
    {

    }

    @Override
    public void glPushDebugGroup(int source, int id, int length, ByteBuffer message)
    {
        CallDetails details = getMethodDetails("glPushDebugGroup");
        details.foundArguments.add(new Object[] { source, id, length, message });
    }

    @Override
    public void glPushDebugGroup(int source, int id, int length, byte[] message, int message_offset)
    {
        CallDetails details = getMethodDetails("glPushDebugGroup");
        details.foundArguments.add(new Object[] { source, id, length, message, message_offset });
    }

    @Override
    public void glQueryCounter(int id, int target)
    {

    }

    @Override
    public void glSampleMaski(int index, int mask)
    {

    }

    @Override
    public void glSamplerParameterIiv(int sampler, int pname, IntBuffer param)
    {

    }

    @Override
    public void glSamplerParameterIiv(int sampler, int pname, int[] param, int param_offset)
    {

    }

    @Override
    public void glSamplerParameterIuiv(int sampler, int pname, IntBuffer param)
    {

    }

    @Override
    public void glSamplerParameterIuiv(int sampler, int pname, int[] param, int param_offset)
    {

    }

    @Override
    public void glShaderSource(int shader, int count, String[] string, IntBuffer length)
    {
        CallDetails details = getMethodDetails("glShaderSource");
        details.foundArguments.add(new Object[] { shader, count, string, length, length });
    }

    @Override
    public void glShaderSource(int shader, int count, String[] string, int[] length, int length_offset)
    {
        CallDetails details = getMethodDetails("glShaderSource");
        details.foundArguments.add(new Object[] { shader, count, string, length, length_offset });
    }

    @Override
    public void glStencilFuncSeparate(int face, int func, int ref, int mask)
    {
        CallDetails details = getMethodDetails("glStencilFuncSeparate");
        details.foundArguments.add(new Object[] { face, func, ref, mask });
    }

    @Override
    public void glStencilMaskSeparate(int face, int mask)
    {
        CallDetails details = getMethodDetails("glStencilMaskSeparate");
        details.foundArguments.add(new Object[] { face, mask });
    }

    @Override
    public void glStencilOpSeparate(int face, int fail, int zfail, int zpass)
    {
        CallDetails details = getMethodDetails("glStencilOpSeparate");
        details.foundArguments.add(new Object[] { face, fail, zfail, zpass });
    }

    @Override
    public void glTexImage2DMultisample(int target, int samples, int internalformat, int width, int height, boolean fixedsamplelocations)
    {

    }

    @Override
    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glTexImage3D");
        details.foundArguments.add(new Object[] { target, level, internalformat, width, height, depth, border, format, type,  pixels });
    }

    @Override
    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, long pixels_buffer_offset)
    {
        CallDetails details = getMethodDetails("glTexImage3D");
        details.foundArguments.add(new Object[] { target, level, internalformat, width, height, depth, border, format, type,  pixels_buffer_offset });
    }

    @Override
    public void glTexImage3DMultisample(int target, int samples, int internalformat, int width, int height, int depth, boolean fixedsamplelocations)
    {

    }

    @Override
    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glTexSubImage3D");
        details.foundArguments.add(new Object[] { target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels });
    }

    @Override
    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, long pixels_buffer_offset)
    {
        CallDetails details = getMethodDetails("glTexSubImage3D");
        details.foundArguments.add(new Object[] { target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels_buffer_offset });
    }

    @Override
    public void glUniform1f(int location, float x)
    {
        CallDetails details = getMethodDetails("glUniform1f");
        details.foundArguments.add(new Object[] { location, x });
    }

    @Override
    public void glUniform1fv(int location, int count, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glUniform1fv");
        details.foundArguments.add(new Object[] { location, count, v });
    }

    @Override
    public void glUniform1fv(int location, int count, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glUniform1fv");
        details.foundArguments.add(new Object[] { location, count, v, v_offset });
    }

    @Override
    public void glUniform1i(int location, int x)
    {
        CallDetails details = getMethodDetails("glUniform1i");
        details.foundArguments.add(new Object[] { location, x });
    }

    @Override
    public void glUniform1iv(int location, int count, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glUniform1iv");
        details.foundArguments.add(new Object[] { location, count, v });
    }

    @Override
    public void glUniform1iv(int location, int count, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glUniform1iv");
        details.foundArguments.add(new Object[] { location, count, v, v_offset });
    }

    @Override
    public void glUniform2f(int location, float x, float y)
    {
        CallDetails details = getMethodDetails("glUniform2fv");
        details.foundArguments.add(new Object[] { location, x, y });
    }

    @Override
    public void glUniform2fv(int location, int count, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glUniform2fv");
        details.foundArguments.add(new Object[] { location, count, v });
    }

    @Override
    public void glUniform2fv(int location, int count, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glUniform2fv");
        details.foundArguments.add(new Object[] { location, count, v, v_offset });
    }

    @Override
    public void glUniform2i(int location, int x, int y)
    {
        CallDetails details = getMethodDetails("glUniform2i");
        details.foundArguments.add(new Object[] { location, x, y });
    }

    @Override
    public void glUniform2iv(int location, int count, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glUniform2iv");
        details.foundArguments.add(new Object[] { location, count, v });
    }

    @Override
    public void glUniform2iv(int location, int count, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glUniform2iv");
        details.foundArguments.add(new Object[] { location, count, v, v_offset });
    }

    @Override
    public void glUniform3f(int location, float x, float y, float z)
    {
        CallDetails details = getMethodDetails("glUniform3f");
        details.foundArguments.add(new Object[] { location, x, y, z });
    }

    @Override
    public void glUniform3fv(int location, int count, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glUniform3fv");
        details.foundArguments.add(new Object[] { location, count, v });
    }

    @Override
    public void glUniform3fv(int location, int count, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glUniform3fv");
        details.foundArguments.add(new Object[] { location, count, v, v_offset });
    }

    @Override
    public void glUniform3i(int location, int x, int y, int z)
    {
        CallDetails details = getMethodDetails("glUniform3i");
        details.foundArguments.add(new Object[] { location, x, y, z });
    }

    @Override
    public void glUniform3iv(int location, int count, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glUniform3iv");
        details.foundArguments.add(new Object[] { location, count, v });
    }

    @Override
    public void glUniform3iv(int location, int count, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glUniform3iv");
        details.foundArguments.add(new Object[] { location, count, v, v_offset });
    }

    @Override
    public void glUniform4f(int location, float x, float y, float z, float w)
    {
        CallDetails details = getMethodDetails("glUniform4f");
        details.foundArguments.add(new Object[] { location, x, y, z, w });
    }

    @Override
    public void glUniform4fv(int location, int count, FloatBuffer v)
    {
        CallDetails details = getMethodDetails("glUniform4fv");
        details.foundArguments.add(new Object[] { location, count, v });
    }

    @Override
    public void glUniform4fv(int location, int count, float[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glUniform4fv");
        details.foundArguments.add(new Object[] { location, count, v, v_offset });
    }

    @Override
    public void glUniform4i(int location, int x, int y, int z, int w)
    {
        CallDetails details = getMethodDetails("glUniform4i");
        details.foundArguments.add(new Object[] { location, x, y, z, w });
    }

    @Override
    public void glUniform4iv(int location, int count, IntBuffer v)
    {
        CallDetails details = getMethodDetails("glUniform4iv");
        details.foundArguments.add(new Object[] { location, count, v });
    }

    @Override
    public void glUniform4iv(int location, int count, int[] v, int v_offset)
    {
        CallDetails details = getMethodDetails("glUniform4iv");
        details.foundArguments.add(new Object[] { location, count, v, v_offset });
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniformMatrix2fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value });
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniformMatrix2fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value, value_offset });
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniformMatrix3fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value });
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniformMatrix3fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value, value_offset });
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value)
    {
        CallDetails details = getMethodDetails("glUniformMatrix4fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value });
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int value_offset)
    {
        CallDetails details = getMethodDetails("glUniformMatrix4fv");
        details.foundArguments.add(new Object[] { location, count, transpose, value, value_offset });
    }

    @Override
    public void glUseProgram(int program)
    {
        CallDetails details = getMethodDetails("glUseProgram");
        details.foundArguments.add(new Object[] { program });
    }

    @Override
    public void glUseProgramStages(int pipeline, int stages, int program)
    {

    }

    @Override
    public void glValidateProgram(int program)
    {
        CallDetails details = getMethodDetails("glValidateProgram");
        details.foundArguments.add(new Object[] { program });
    }

    @Override
    public void glValidateProgramPipeline(int pipeline)
    {

    }

    @Override
    public void glVertexAttrib1f(int indx, float x)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1f");
        details.foundArguments.add(new Object[] { indx, x });
    }

    @Override
    public void glVertexAttrib1fv(int indx, FloatBuffer values)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1fv");
        details.foundArguments.add(new Object[] { indx, values });
    }

    @Override
    public void glVertexAttrib1fv(int indx, float[] values, int values_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib1fv");
        details.foundArguments.add(new Object[] { indx, values, values_offset });
    }

    @Override
    public void glVertexAttrib2f(int indx, float x, float y)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2f");
        details.foundArguments.add(new Object[] { indx, x, y });
    }

    @Override
    public void glVertexAttrib2fv(int indx, FloatBuffer values)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2fv");
        details.foundArguments.add(new Object[] { indx, values });
    }

    @Override
    public void glVertexAttrib2fv(int indx, float[] values, int values_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib2fv");
        details.foundArguments.add(new Object[] { indx, values, values_offset });
    }

    @Override
    public void glVertexAttrib3f(int indx, float x, float y, float z)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3f");
        details.foundArguments.add(new Object[] { indx, x, y, z });
    }

    @Override
    public void glVertexAttrib3fv(int indx, FloatBuffer values)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3fv");
        details.foundArguments.add(new Object[] { indx, values });
    }

    @Override
    public void glVertexAttrib3fv(int indx, float[] values, int values_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib3fv");
        details.foundArguments.add(new Object[] { indx, values, values_offset });
    }

    @Override
    public void glVertexAttrib4f(int indx, float x, float y, float z, float w)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4f");
        details.foundArguments.add(new Object[] { indx, x, y, z });
    }

    @Override
    public void glVertexAttrib4fv(int indx, FloatBuffer values)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4fv");
        details.foundArguments.add(new Object[] { indx, values });
    }

    @Override
    public void glVertexAttrib4fv(int indx, float[] values, int values_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttrib4fv");
        details.foundArguments.add(new Object[] { indx, values, values_offset });
    }

    @Override
    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr)
    {
        CallDetails details = getMethodDetails("glVertexAttribPointer");
        details.foundArguments.add(new Object[] { indx, size, type, normalized, stride, ptr });
    }

    @Override
    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, long ptr_buffer_offset)
    {
        CallDetails details = getMethodDetails("glVertexAttribPointer");
        details.foundArguments.add(new Object[] { indx, size, type, normalized, stride, ptr_buffer_offset });
    }

    @Override
    public void glReleaseShaderCompiler()
    {
        CallDetails details = getMethodDetails("glReleaseShaderCompiler");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length)
    {
        CallDetails details = getMethodDetails("glShaderBinary");
        details.foundArguments.add(new Object[] { n, shaders, binaryformat, binary, length });
    }

    @Override
    public void glShaderBinary(int n, int[] shaders, int shaders_offset, int binaryformat, Buffer binary, int length)
    {
        CallDetails details = getMethodDetails("glShaderBinary");
        details.foundArguments.add(new Object[] { n, shaders, shaders_offset, binaryformat, binary, length });
    }

    @Override
    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision)
    {
        CallDetails details = getMethodDetails("glGetShaderPrecisionFormat");
        details.foundArguments.add(new Object[] { shadertype, precisiontype, range, precision });
    }

    @Override
    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, int[] range, int range_offset, int[] precision, int precision_offset)
    {
        CallDetails details = getMethodDetails("glGetShaderPrecisionFormat");
        details.foundArguments.add(new Object[] { shadertype, precisiontype, range, range_offset, precision, precision_offset });
    }

    @Override
    public void glVertexAttribPointer(GLArrayData array)
    {
        CallDetails details = getMethodDetails("glVertexAttribPointer");
        details.foundArguments.add(new Object[] { array });
    }

    @Override
    public void glUniform(GLUniformData data)
    {
        CallDetails details = getMethodDetails("glUniform");
        details.foundArguments.add(new Object[] { data });
    }

    @Override
    public void glMatrixMode(int mode)
    {
        CallDetails details = getMethodDetails("glMatrixMode");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public void glPushMatrix()
    {
        CallDetails details = getMethodDetails("glPushMatrix");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glPopMatrix()
    {
        CallDetails details = getMethodDetails("glPopMatrix");
        details.foundArguments.add(new Object[0] );
    }

    @Override
    public void glLoadIdentity()
    {
        CallDetails details = getMethodDetails("glLoadIdentity");
        details.foundArguments.add(new Object[0] );
    }

    @Override
    public void glLoadMatrixf(FloatBuffer m)
    {
        CallDetails details = getMethodDetails("glLoadMatrixf");
        details.foundArguments.add(new Object[] { m });
    }

    @Override
    public void glLoadMatrixf(float[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glLoadMatrixf");
        details.foundArguments.add(new Object[] { m, m_offset });

    }

    @Override
    public void glMultMatrixf(FloatBuffer m)
    {
        CallDetails details = getMethodDetails("glMultMatrixf");
        details.foundArguments.add(new Object[] { m });

    }

    @Override
    public void glMultMatrixf(float[] m, int m_offset)
    {
        CallDetails details = getMethodDetails("glMultMatrixf");
        details.foundArguments.add(new Object[] { m, m_offset });

    }

    @Override
    public void glTranslatef(float x, float y, float z)
    {
        CallDetails details = getMethodDetails("glTranslatef");
        details.foundArguments.add(new Object[] { x, y, z });

    }

    @Override
    public void glRotatef(float angle, float x, float y, float z)
    {
        CallDetails details = getMethodDetails("glRotatef");
        details.foundArguments.add(new Object[] { angle, x, y, z });

    }

    @Override
    public void glScalef(float x, float y, float z)
    {
        CallDetails details = getMethodDetails("glScalef");
        details.foundArguments.add(new Object[] { x, y, z });

    }

    @Override
    public void glOrthof(float left, float right, float bottom, float top, float zNear, float zFar)
    {
        CallDetails details = getMethodDetails("glOrthof");
        details.foundArguments.add(new Object[] { left, right, bottom, top, zNear, zFar });
    }

    @Override
    public void glFrustumf(float left, float right, float bottom, float top, float zNear, float zFar)
    {
        CallDetails details = getMethodDetails("glFrustumf");
        details.foundArguments.add(new Object[] { left, right, bottom, top, zNear, zFar });
    }

    @Override
    public boolean isGL2()
    {
        return true;
    }

    @Override
    public boolean isGLES1()
    {
        return true;
    }

    @Override
    public boolean isGLES2()
    {
        return true;
    }

    @Override
    public boolean isGLES3()
    {
        return true;
    }

    @Override
    public boolean isGLES()
    {
        return true;
    }

    @Override
    public boolean isGL2ES1()
    {
        return true;
    }

    @Override
    public boolean isGL2ES2()
    {
        return true;
    }

    @Override
    public boolean isGL2GL3()
    {
        return true;
    }

    @Override
    public boolean isGLcore()
    {
        return true;
    }

    @Override
    public boolean hasGLSL()
    {
        return true;
    }

    @Override
    public GL getDownstreamGL() throws GLException
    {
        return this;
    }

    @Override
    public GL getRootGL() throws GLException
    {
        return this;
    }

    @Override
    public GL getGL() throws GLException
    {
        return this;
    }

    @Override
    public void glLightfv(int light, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glLightfv");
        details.foundArguments.add(new Object[] { light, pname, params });
    }

    @Override
    public void glLightfv(int light, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glLightfv");
        details.foundArguments.add(new Object[] { light, pname, params, params_offset });
    }

    @Override
    public void glMaterialf(int face, int pname, float param)
    {
        CallDetails details = getMethodDetails("glMaterialf");
        details.foundArguments.add(new Object[] { face, pname, param });
    }

    @Override
    public void glMaterialfv(int face, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glMaterialfv");
        details.foundArguments.add(new Object[] { face, pname, params });
    }

    @Override
    public void glMaterialfv(int face, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glMaterialfv");
        details.foundArguments.add(new Object[] { face, pname, params, params_offset });
    }

    @Override
    public void glShadeModel(int mode)
    {
        CallDetails details = getMethodDetails("glShadeModel");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public void glEnableClientState(int arrayName)
    {
        CallDetails details = getMethodDetails("glEnableClientState");
        details.foundArguments.add(new Object[] { arrayName });
    }

    @Override
    public void glDisableClientState(int arrayName)
    {
        CallDetails details = getMethodDetails("glDisableClientState");
        details.foundArguments.add(new Object[] { arrayName });
    }

    @Override
    public void glVertexPointer(GLArrayData array)
    {
        CallDetails details = getMethodDetails("glVertexPointer");
        details.foundArguments.add(new Object[] { array });
    }

    @Override
    public void glVertexPointer(int size, int type, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glVertexPointer");
        details.foundArguments.add(new Object[] { size, type, stride, pointer });
    }

    @Override
    public void glVertexPointer(int size, int type, int stride, long pointer_buffer_offset)
    {
        CallDetails details = getMethodDetails("glVertexPointer");
        details.foundArguments.add(new Object[] { size, type, stride, pointer_buffer_offset });
    }

    @Override
    public void glColorPointer(GLArrayData array)
    {
        CallDetails details = getMethodDetails("glColorPointer");
        details.foundArguments.add(new Object[] { array });
    }

    @Override
    public void glColorPointer(int size, int type, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glColorPointer");
        details.foundArguments.add(new Object[] { size, type, stride, pointer });
    }

    @Override
    public void glColorPointer(int size, int type, int stride, long pointer_buffer_offset)
    {
        CallDetails details = getMethodDetails("glColorPointer");
        details.foundArguments.add(new Object[] { size, type, stride, pointer_buffer_offset });
    }

    @Override
    public void glColor4f(float red, float green, float blue, float alpha)
    {
        CallDetails details = getMethodDetails("glColor4f");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glNormalPointer(GLArrayData array)
    {
        CallDetails details = getMethodDetails("array");
        details.foundArguments.add(new Object[] { array });
    }

    @Override
    public void glNormalPointer(int type, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glNormalPointer");
        details.foundArguments.add(new Object[] { type, stride, pointer });
    }

    @Override
    public void glNormalPointer(int type, int stride, long pointer_buffer_offset)
    {
        CallDetails details = getMethodDetails("glNormalPointer");
        details.foundArguments.add(new Object[] { type, stride, pointer_buffer_offset });
    }

    @Override
    public void glTexCoordPointer(GLArrayData array)
    {
        CallDetails details = getMethodDetails("glTexCoordPointer");
        details.foundArguments.add(new Object[] { array });
    }

    @Override
    public void glTexCoordPointer(int size, int type, int stride, Buffer pointer)
    {
        CallDetails details = getMethodDetails("glTexCoordPointer");
        details.foundArguments.add(new Object[] { size, type, stride, pointer });
    }

    @Override
    public void glTexCoordPointer(int size, int type, int stride, long pointer_buffer_offset)
    {
        CallDetails details = getMethodDetails("glTexCoordPointer");
        details.foundArguments.add(new Object[] { size, type, stride, pointer_buffer_offset });

    }
}
