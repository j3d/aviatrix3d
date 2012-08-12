#*********************************************************************
#
#  (C) 2003 Yumetech, Inc
#    http://www.j3d.org/
#
# Package makefile for the org.j3d.aviatrix3d.output.graphics directory
#
# Author: Justin Couch
# Version: $Revision: 3.13 $
#
#*********************************************************************

PACKAGE = org.j3d.aviatrix3d.output.graphics

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  EnableState.java \
  BaseBufferDescriptor.java \
  PbufferDescriptor.java \
  FBORenderTargetDescriptor.java \
  FBODescriptor.java \
  MainCanvasDescriptor.java \
  RenderingProcessor.java \
  StereoRenderingProcessor.java \
  AV3DSurfaceInfo.java \
  BaseRenderingProcessor.java \
  BaseStereoProcessor.java \
  StandardRenderingProcessor.java \
  DebugRenderingProcessor.java \
  QuadBufferStereoProcessor.java \
  SingleEyeStereoProcessor.java \
  DebugSingleEyeStereoProcessor.java \
  SurfaceMonitor.java \
  AWTSurfaceMonitor.java \
  BaseSurface.java \
  AWTResizeHandler.java \
  BaseAWTSurface.java \
  SimpleAWTSurface.java \
  DebugAWTSurface.java \
  StereoAWTSurface.java \
  PbufferUnavailableException.java \
  PbufferSurface.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-util_1.1.0.jar j3d-org-elumens_1.1.0.jar j3d-org-opengl-swt.jar \
  vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
