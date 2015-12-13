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

/**
 * Pretend Mock of the GL2 interface. This exists because CGLIB cannot
 * generate a proper mock for the GL/GL2/GL3... interfaces due to too
 * many methods existing and then having more than 65k bytes in a method.
 *
 * @author justin
 */
public class MockGL extends MockGLBase implements GL
{
    public MockGL(GLContext ctx)
    {
        super(ctx);
    }

    @Override
    public void glActiveTexture(int texture)
    {
        CallDetails details = getMethodDetails("glActiveTexture");
        details.foundArguments.add(new Object[] { texture });
    }

    @Override
    public void glBindBuffer(int target, int buffer)
    {
        CallDetails details = getMethodDetails("glBindBuffer");
        details.foundArguments.add(new Object[] { target, buffer });
    }

    @Override
    public void glBindFramebuffer(int target, int framebuffer)
    {
        CallDetails details = getMethodDetails("glBindFramebuffer");
        details.foundArguments.add(new Object[] { target, framebuffer });
    }

    @Override
    public void glBindRenderbuffer(int target, int renderbuffer)
    {
        CallDetails details = getMethodDetails("glBindRenderbuffer");
        details.foundArguments.add(new Object[] { target, renderbuffer });
    }

    @Override
    public void glBindTexture(int target, int texture)
    {
        CallDetails details = getMethodDetails("glBindTexture");
        details.foundArguments.add(new Object[] { target, texture });
    }

    @Override
    public void glBlendEquation(int mode)
    {
        CallDetails details = getMethodDetails("glBlendEquation");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha)
    {
        CallDetails details = getMethodDetails("glBlendEquationSeparate");
        details.foundArguments.add(new Object[] { modeRGB, modeAlpha });
    }

    @Override
    public void glBlendFunc(int sfactor, int dfactor)
    {
        CallDetails details = getMethodDetails("glBlendFunc");
        details.foundArguments.add(new Object[] { sfactor, dfactor });
    }

    @Override
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha)
    {
        CallDetails details = getMethodDetails("glBlendFuncSeparate");
        details.foundArguments.add(new Object[] { srcRGB, dstRGB, srcAlpha, dstAlpha });
    }

    @Override
    public void glBufferData(int target, long size, Buffer data, int usage)
    {
        CallDetails details = getMethodDetails("glBufferData");
        details.foundArguments.add(new Object[] { target, size, data, usage });
    }

    @Override
    public void glBufferSubData(int target, long offset, long size, Buffer data)
    {
        CallDetails details = getMethodDetails("glBufferSubData");
        details.foundArguments.add(new Object[] { target, offset, size, data });
    }

    @Override
    public int glCheckFramebufferStatus(int target)
    {
        return 0;
    }

    @Override
    public void glClear(int mask)
    {
        CallDetails details = getMethodDetails("glClear");
        details.foundArguments.add(new Object[] { mask });
    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha)
    {
        CallDetails details = getMethodDetails("glClearColor");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glClearDepthf(float depth)
    {
        CallDetails details = getMethodDetails("glClearDepthf");
        details.foundArguments.add(new Object[] { depth });
    }

    @Override
    public void glClearStencil(int s)
    {
        CallDetails details = getMethodDetails("glClearStencil");
        details.foundArguments.add(new Object[] { s });
    }

    @Override
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha)
    {
        CallDetails details = getMethodDetails("glColorMask");
        details.foundArguments.add(new Object[] { red, green, blue, alpha });
    }

    @Override
    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data)
    {
        CallDetails details = getMethodDetails("glCompressedTexImage2D");
        details.foundArguments.add(new Object[] { target, level, internalformat, width, height, border, imageSize, data });
    }

    @Override
    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, long data_buffer_offset)
    {
        CallDetails details = getMethodDetails("glCompressedTexImage2D");
        details.foundArguments.add(new Object[] { target, level, internalformat, width, height, border, imageSize, data_buffer_offset });
    }

    @Override
    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data)
    {
        CallDetails details = getMethodDetails("glCompressedTexSubImage2D");
        details.foundArguments.add(new Object[] { target, level, xoffset, yoffset, width, height, format, imageSize, data });
    }

    @Override
    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, long data_buffer_offset)
    {
        CallDetails details = getMethodDetails("glCompressedTexSubImage2D");
        details.foundArguments.add(new Object[] { target, level, xoffset, yoffset, width, height, format, imageSize, data_buffer_offset });
    }

    @Override
    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border)
    {
        CallDetails details = getMethodDetails("glCopyTexImage2D");
        details.foundArguments.add(new Object[] { target, level, internalformat, x, y, width, height, border });
    }

    @Override
    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height)
    {
        CallDetails details = getMethodDetails("glCopyTexSubImage2D");
        details.foundArguments.add(new Object[] { target, level, xoffset, yoffset, x, y, width, height });
    }

    @Override
    public void glCullFace(int mode)
    {
        CallDetails details = getMethodDetails("glCullFace");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public void glDeleteBuffers(int n, IntBuffer buffers)
    {
        CallDetails details = getMethodDetails("glDeleteBuffers");
        details.foundArguments.add(new Object[] { n, buffers });
    }

    @Override
    public void glDeleteBuffers(int n, int[] buffers, int buffers_offset)
    {
        CallDetails details = getMethodDetails("glDeleteBuffers");
        details.foundArguments.add(new Object[] { n, buffers, buffers_offset });
    }

    @Override
    public void glDeleteFramebuffers(int n, IntBuffer framebuffers)
    {
        CallDetails details = getMethodDetails("glDeleteFramebuffers");
        details.foundArguments.add(new Object[] { n, framebuffers });
    }

    @Override
    public void glDeleteFramebuffers(int n, int[] framebuffers, int framebuffers_offset)
    {
        CallDetails details = getMethodDetails("glDeleteFramebuffers");
        details.foundArguments.add(new Object[] { n, framebuffers, framebuffers_offset });
    }

    @Override
    public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers)
    {
        CallDetails details = getMethodDetails("glDeleteRenderbuffers");
        details.foundArguments.add(new Object[] { n, renderbuffers });
    }

    @Override
    public void glDeleteRenderbuffers(int n, int[] renderbuffers, int renderbuffers_offset)
    {
        CallDetails details = getMethodDetails("glDeleteRenderbuffers");
        details.foundArguments.add(new Object[] { n, renderbuffers, renderbuffers_offset });
    }

    @Override
    public void glDeleteTextures(int n, IntBuffer textures)
    {
        CallDetails details = getMethodDetails("glDeleteTextures");
        details.foundArguments.add(new Object[] { n, textures });
    }

    @Override
    public void glDeleteTextures(int n, int[] textures, int textures_offset)
    {
        CallDetails details = getMethodDetails("glDeleteTextures");
        details.foundArguments.add(new Object[] { n, textures, textures_offset });
    }

    @Override
    public void glDepthFunc(int func)
    {
        CallDetails details = getMethodDetails("glDepthFunc");
        details.foundArguments.add(new Object[] { func });
    }

    @Override
    public void glDepthMask(boolean flag)
    {
        CallDetails details = getMethodDetails("glDepthMask");
        details.foundArguments.add(new Object[] { flag });
    }

    @Override
    public void glDepthRangef(float zNear, float zFar)
    {
        CallDetails details = getMethodDetails("glDepthRangef");
        details.foundArguments.add(new Object[] { zNear, zFar });
    }

    @Override
    public void glDisable(int cap)
    {
        CallDetails details = getMethodDetails("glDisable");
        details.foundArguments.add(new Object[] { cap });
    }

    @Override
    public void glDrawArrays(int mode, int first, int count)
    {
        CallDetails details = getMethodDetails("glDrawArrays");
        details.foundArguments.add(new Object[] { mode, first, count });
    }

    @Override
    public void glDrawElements(int mode, int count, int type, long indices_buffer_offset)
    {
        CallDetails details = getMethodDetails("glDrawElements");
        details.foundArguments.add(new Object[] { mode, count, type, indices_buffer_offset });
    }

    @Override
    public void glEnable(int cap)
    {
        CallDetails details = getMethodDetails("glEnable");
        details.foundArguments.add(new Object[] { cap });
    }

    @Override
    public void glFinish()
    {
        CallDetails details = getMethodDetails("glFinish");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glFlush()
    {
        CallDetails details = getMethodDetails("glFlush");
        details.foundArguments.add(new Object[0]);
    }

    @Override
    public void glFlushMappedBufferRange(int target, long offset, long length)
    {
        CallDetails details = getMethodDetails("glFlushMappedBufferRange");
        details.foundArguments.add(new Object[] { target, offset, length });
    }

    @Override
    public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer)
    {
        CallDetails details = getMethodDetails("glFramebufferRenderbuffer");
        details.foundArguments.add(new Object[] { target, attachment, renderbuffertarget, renderbuffer });
    }

    @Override
    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level)
    {
        CallDetails details = getMethodDetails("glFramebufferTexture2D");
        details.foundArguments.add(new Object[] { target, attachment, textarget, texture, level });
    }

    @Override
    public void glFrontFace(int mode)
    {
        CallDetails details = getMethodDetails("glFrontFace");
        details.foundArguments.add(new Object[] { mode });
    }

    @Override
    public void glGenBuffers(int n, IntBuffer buffers)
    {
        CallDetails details = getMethodDetails("glGenBuffers");
        details.foundArguments.add(new Object[] { n, buffers });
    }

    @Override
    public void glGenBuffers(int n, int[] buffers, int buffers_offset)
    {
        CallDetails details = getMethodDetails("glGenBuffers");
        details.foundArguments.add(new Object[] { n, buffers, buffers_offset });
    }

    @Override
    public void glGenFramebuffers(int n, IntBuffer framebuffers)
    {
        CallDetails details = getMethodDetails("glGenFramebuffers");
        details.foundArguments.add(new Object[] { n, framebuffers });
    }

    @Override
    public void glGenFramebuffers(int n, int[] framebuffers, int framebuffers_offset)
    {
        CallDetails details = getMethodDetails("glGenFramebuffers");
        details.foundArguments.add(new Object[] { n, framebuffers, framebuffers_offset });
    }

    @Override
    public void glGenRenderbuffers(int n, IntBuffer renderbuffers)
    {
        CallDetails details = getMethodDetails("glGenRenderbuffers");
        details.foundArguments.add(new Object[] { n, renderbuffers });
    }

    @Override
    public void glGenRenderbuffers(int n, int[] renderbuffers, int renderbuffers_offset)
    {
        CallDetails details = getMethodDetails("glGenRenderbuffers");
        details.foundArguments.add(new Object[] { n, renderbuffers, renderbuffers_offset });
    }

    @Override
    public void glGenTextures(int n, IntBuffer textures)
    {
        CallDetails details = getMethodDetails("glGenTextures");
        details.foundArguments.add(new Object[] { n, textures });
    }

    @Override
    public void glGenTextures(int n, int[] textures, int textures_offset)
    {
        CallDetails details = getMethodDetails("glGenTextures");
        details.foundArguments.add(new Object[] { n, textures, textures_offset });
    }

    @Override
    public void glGenerateMipmap(int target)
    {
        CallDetails details = getMethodDetails("glGenerateMipmap");
        details.foundArguments.add(new Object[] { target });
    }

    @Override
    public void glGetBooleanv(int pname, ByteBuffer params)
    {
        CallDetails details = getMethodDetails("glGetBooleanv");
        details.foundArguments.add(new Object[] { pname, params });
    }

    @Override
    public void glGetBooleanv(int pname, byte[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetBooleanv");
        details.foundArguments.add(new Object[] { pname, params, params_offset });
    }

    @Override
    public void glGetBufferParameteriv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetBufferParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetBufferParameteriv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetBufferParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public int glGetError()
    {
        return 0;
    }

    @Override
    public void glGetFloatv(int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetFloatv");
        details.foundArguments.add(new Object[] { pname, params });
    }

    @Override
    public void glGetFloatv(int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetFloatv");
        details.foundArguments.add(new Object[] { pname, params, params_offset });
    }

    @Override
    public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetFramebufferAttachmentParameteriv");
        details.foundArguments.add(new Object[] { target, attachment, pname, params });
    }

    @Override
    public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetFramebufferAttachmentParameteriv");
        details.foundArguments.add(new Object[] { target, attachment, pname, params, params_offset });
    }

    @Override
    public int glGetGraphicsResetStatus()
    {
        return 0;
    }

    @Override
    public void glGetIntegerv(int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetIntegerv");
        details.foundArguments.add(new Object[] { pname, params });
    }

    @Override
    public void glGetIntegerv(int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetIntegerv");
        details.foundArguments.add(new Object[] { pname, params, params_offset });
    }

    @Override
    public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetRenderbufferParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetRenderbufferParameteriv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetRenderbufferParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public String glGetString(int name)
    {
        return null;
    }

    @Override
    public void glGetTexParameterfv(int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTexParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetTexParameterfv(int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTexParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetTexParameteriv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetTexParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glGetTexParameteriv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetTexParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glGetnUniformfv(int program, int location, int bufSize, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glGetnUniformfv");
        details.foundArguments.add(new Object[] { program, location, bufSize, params });
    }

    @Override
    public void glGetnUniformfv(int program, int location, int bufSize, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetnUniformfv");
        details.foundArguments.add(new Object[] { program, location, bufSize, params, params_offset });
    }

    @Override
    public void glGetnUniformiv(int program, int location, int bufSize, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glGetnUniformiv");
        details.foundArguments.add(new Object[] { program, location, bufSize, params });
    }

    @Override
    public void glGetnUniformiv(int program, int location, int bufSize, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glGetnUniformiv");
        details.foundArguments.add(new Object[] { program, location, bufSize, params, params_offset });
    }

    @Override
    public void glHint(int target, int mode)
    {
        CallDetails details = getMethodDetails("glHint");
        details.foundArguments.add(new Object[] { target, mode });
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
        CallDetails details = getMethodDetails("glLineWidth");
        details.foundArguments.add(new Object[] { width });
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
        CallDetails details = getMethodDetails("glPixelStorei");
        details.foundArguments.add(new Object[] { pname, param });
    }

    @Override
    public void glPolygonOffset(float factor, float units)
    {
        CallDetails details = getMethodDetails("glPolygonOffset");
        details.foundArguments.add(new Object[] { factor, units });
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glReadPixels");
        details.foundArguments.add(new Object[] { x, y, width, height, format, type, pixels });
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, long pixels_buffer_offset)
    {
        CallDetails details = getMethodDetails("glReadPixels");
        details.foundArguments.add(new Object[] { x, y, width, height, format, type, pixels_buffer_offset });
    }

    @Override
    public void glReadnPixels(int x, int y, int width, int height, int format, int type, int bufSize, Buffer data)
    {
        CallDetails details = getMethodDetails("glReadnPixels");
        details.foundArguments.add(new Object[] { x, y, width, height, format, type, bufSize, data });
    }

    @Override
    public void glRenderbufferStorage(int target, int internalformat, int width, int height)
    {
        CallDetails details = getMethodDetails("glRenderbufferStorage");
        details.foundArguments.add(new Object[] { target, internalformat, width, height });
    }

    @Override
    public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height)
    {
        CallDetails details = getMethodDetails("glRenderbufferStorageMultisample");
        details.foundArguments.add(new Object[] { target, internalformat, width, height });
    }

    @Override
    public void glSampleCoverage(float value, boolean invert)
    {
        CallDetails details = getMethodDetails("glSampleCoverage");
        details.foundArguments.add(new Object[] { value, invert });
    }

    @Override
    public void glScissor(int x, int y, int width, int height)
    {
        CallDetails details = getMethodDetails("glScissor");
        details.foundArguments.add(new Object[] { x, y, width, height });
    }

    @Override
    public void glStencilFunc(int func, int ref, int mask)
    {
        CallDetails details = getMethodDetails("glStencilFunc");
        details.foundArguments.add(new Object[] { func, ref, mask });
    }

    @Override
    public void glStencilMask(int mask)
    {
        CallDetails details = getMethodDetails("glStencilMask");
        details.foundArguments.add(new Object[] { mask });
    }

    @Override
    public void glStencilOp(int fail, int zfail, int zpass)
    {
        CallDetails details = getMethodDetails("glStencilOp");
        details.foundArguments.add(new Object[] { fail, zfail, zpass });
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glTexImage2D");
        details.foundArguments.add(new Object[] { target, level, internalformat, width, height, border, format, type, pixels });
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, long pixels_buffer_offset)
    {
        CallDetails details = getMethodDetails("glTexImage2D");
        details.foundArguments.add(new Object[] { target, level, internalformat, width, height, border, format, type, pixels_buffer_offset });
    }

    @Override
    public void glTexParameterf(int target, int pname, float param)
    {
        CallDetails details = getMethodDetails("glTexParameterf");
        details.foundArguments.add(new Object[] { target, pname, param });
    }

    @Override
    public void glTexParameterfv(int target, int pname, FloatBuffer params)
    {
        CallDetails details = getMethodDetails("glTexParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glTexParameterfv(int target, int pname, float[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glTexParameterfv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glTexParameteri(int target, int pname, int param)
    {
        CallDetails details = getMethodDetails("glTexParameteri");
        details.foundArguments.add(new Object[] { target, pname, param });
    }

    @Override
    public void glTexParameteriv(int target, int pname, IntBuffer params)
    {
        CallDetails details = getMethodDetails("glTexParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params });
    }

    @Override
    public void glTexParameteriv(int target, int pname, int[] params, int params_offset)
    {
        CallDetails details = getMethodDetails("glTexParameteriv");
        details.foundArguments.add(new Object[] { target, pname, params, params_offset });
    }

    @Override
    public void glTexStorage1D(int target, int levels, int internalformat, int width)
    {
        CallDetails details = getMethodDetails("glTexStorage1D");
        details.foundArguments.add(new Object[] { target, levels, internalformat, width });
    }

    @Override
    public void glTexStorage2D(int target, int levels, int internalformat, int width, int height)
    {
        CallDetails details = getMethodDetails("glTexStorage2D");
        details.foundArguments.add(new Object[] { target, levels, internalformat, width, height });
    }

    @Override
    public void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth)
    {
        CallDetails details = getMethodDetails("glTexStorage3D");
        details.foundArguments.add(new Object[] { target, levels, internalformat, width, height, depth });
    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels)
    {
        CallDetails details = getMethodDetails("glTexSubImage2D");
        details.foundArguments.add(new Object[] { target, level, xoffset, yoffset, width, height, format, type, pixels });
    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, long pixels_buffer_offset)
    {
        CallDetails details = getMethodDetails("glTexSubImage2D");
        details.foundArguments.add(new Object[] { target, level, xoffset, yoffset, width, height, format, type, pixels_buffer_offset });
    }

    @Override
    public void glTextureStorage1DEXT(int texture, int target, int levels, int internalformat, int width)
    {
        CallDetails details = getMethodDetails("glTextureStorage1DEXT");
        details.foundArguments.add(new Object[] { texture, target, levels, internalformat, width });
    }

    @Override
    public void glTextureStorage2DEXT(int texture, int target, int levels, int internalformat, int width, int height)
    {
        CallDetails details = getMethodDetails("glTextureStorage2DEXT");
        details.foundArguments.add(new Object[] { texture, target, levels, internalformat, width, height });
    }

    @Override
    public void glTextureStorage3DEXT(int texture, int target, int levels, int internalformat, int width, int height, int depth)
    {
        CallDetails details = getMethodDetails("glTextureStorage1DEXT");
        details.foundArguments.add(new Object[] { texture, target, levels, internalformat, width, height, depth });
    }

    @Override
    public boolean glUnmapBuffer(int target)
    {
        return false;
    }

    @Override
    public void glViewport(int x, int y, int width, int height)
    {
        CallDetails details = getMethodDetails("glViewport");
        details.foundArguments.add(new Object[] { x, y, width, height });
    }

    @Override
    public boolean isGL2ES3()
    {
        return false;
    }

    @Override
    public GL2ES3 getGL2ES3() throws GLException
    {
        return null;
    }

    @Override
    public int getBoundBuffer(int target)
    {
        return 0;
    }

    @Override
    public GLBufferStorage getBufferStorage(int bufferName)
    {
        return null;
    }

    @Override
    public GLBufferStorage mapBuffer(int target, int access) throws GLException
    {
        return null;
    }

    @Override
    public GLBufferStorage mapBufferRange(int target, long offset, long length, int access) throws GLException
    {
        return null;
    }
}
