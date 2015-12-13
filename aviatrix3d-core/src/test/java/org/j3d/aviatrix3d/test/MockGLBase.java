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

import java.util.*;

import com.jogamp.opengl.*;

import org.hamcrest.Matcher;

import static org.testng.Assert.*;

/**
 * Base mock for all the OpenGL calls for testing purposes.
 * <p>
 *     Manages it's own call tracking manually as we cannot
 * use the standard reflection proxy or CGLIB proxying due
 * to too many calls and static initialisers.
 * </p>
 *
 * @author justin
 */
public class MockGLBase implements GLBase
{
    protected static class CallDetails
    {
        int callCount;
        int returnValue;

        List<Object[]> foundArguments = new LinkedList<>();
    }


    /** Any method that needs to return an index for binding something increments this */
    protected int indexCounter;

    private int methodCallCount;

    private Map<String, CallDetails> methodCallDetails;

    private GLProfile configuredProfile;

    private GLContext mockContext;

    protected MockGLBase(GLContext ctx)
    {
        methodCallCount = 0;
        indexCounter = 0;
        methodCallDetails = new HashMap<>();
        mockContext = ctx;
    }

    @Override
    public boolean isGL()
    {
        return true;
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
    public boolean isGL2ES3()
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
    public boolean isGLES31Compatible()
    {
        return false;
    }

    @Override
    public boolean isGLES32Compatible()
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
    public GL2ES3 getGL2ES3() throws GLException
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
        return configuredProfile;
    }

    @Override
    public GLContext getContext()
    {
        return mockContext;
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
        CallDetails details = getMethodDetails("setSwapInterval");
        details.foundArguments.add(new Object[] { interval });
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
        CallDetails details = getMethodDetails("glClearDepth");
        details.foundArguments.add(new Object[] { depth });
    }

    @Override
    public void glDepthRange(double zNear, double zFar)
    {
        CallDetails details = getMethodDetails("glDepthRange");
        details.foundArguments.add(new Object[] { zNear, zFar });
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

    @Override
    public boolean isVBOArrayBound()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isVBOElementArrayBound()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBoundFramebuffer(int target)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDefaultDrawFramebuffer()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDefaultReadFramebuffer()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDefaultReadBuffer()
    {
        throw new UnsupportedOperationException();
    }

    public int getCallCount()
    {
        return methodCallCount;
    }

    public void resetCallCount()
    {
        methodCallCount = 0;
    }

    public void verifyCall(String methodName, Object... wantedArgs)
    {
        CallDetails details = methodCallDetails.get(methodName);

        assertNotNull(details, "Method " + methodName + " was not called");

        details.callCount--;
        Object[] foundArgs = details.foundArguments.remove(0);

        if(wantedArgs == null) {
            if(foundArgs.length != 0) {
                fail("Found argument length of " + foundArgs.length + " does not match empty wanted arg list");
            }
        } else {
            assertEquals(wantedArgs.length, foundArgs.length, "Wanted and found argument length mismatch");
        }


        for(int i = 0; i < wantedArgs.length; i++) {
            if(wantedArgs[i] instanceof Matcher) {
                assertTrue(((Matcher)wantedArgs[i]).matches(foundArgs[i]),
                           "Argument " + i + " does not match. Wanted " +
                           wantedArgs[i] +
                           " but found " +
                           (foundArgs[i] != null ? foundArgs[i].getClass().getSimpleName() : "null"));
            } else
            {
                assertEquals(foundArgs[i], wantedArgs[i], "Mismatch in argument " + i);
            }
        }
    }

    /**
     * Fetches the internal call detail holder and increments the method
     * access counter. Only used internally to the methods.
     *
     * @param methodName
     * @return
     */
    protected CallDetails getMethodDetails(String methodName)
    {
        methodCallCount++;

        CallDetails details = methodCallDetails.get(methodName);
        if(details == null)
        {
            details = new CallDetails();
            methodCallDetails.put(methodName, details);
        }

        details.callCount++;

        return details;
    }

}

