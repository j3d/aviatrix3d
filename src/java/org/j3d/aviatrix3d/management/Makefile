#*********************************************************************
#
#  (C) 2003 Yumetech, Inc
#    http://www.j3d.org/
#
# Package makefile for the org.j3d.aviatrix3d.management directory
#
# Author: Justin Couch
# Version: $Revision: 1.3 $
#
#*********************************************************************

PACKAGE = org.j3d.aviatrix3d.management

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  DisplayCollection.java \
  RenderManager.java \
  LayerContainer.java \
  DefaultBatchPickingHandler.java \
  DefaultPickingHandler.java \
  ThreadPrivilegedAction.java \
  ShutdownThread.java \
  SingleThreadRenderManager.java \
  PipelineStateObserver.java \
  DisplayCollectionThread.java \
  GraphicsPipelineThread.java \
  AudioPipelineThread.java \
  MultiThreadRenderManager.java \
  SingleDisplayCollection.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-util_1.1.0.jar vecmath.jar 

include ${PROJECT_ROOT}/make/Makefile.jav
