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

PACKAGE = org.j3d.renderer.aviatrix3d.swt.output

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  SWTSurfaceMonitor.java \
  SWTResizeHandler.java \
  BaseSWTSurface.java \
  SimpleSWTSurface.java \
  DebugSWTSurface.java \
  StereoSWTSurface.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-util_1.1.0.jar j3d-org-elumens_1.1.0.jar \
  j3d-org-opengl-swt.jar vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
