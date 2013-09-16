#*********************************************************************
#
#  (C) 2003 Yumetech, Inc
#    http://www.j3d.org/
#
# Package makefile for the org.j3d.aviatrix3d.pipeline.graphics directory
#
# Author: Justin Couch
# Version: $Revision: 3.11 $
#
#*********************************************************************

PACKAGE = org.j3d.aviatrix3d.pipeline.graphics

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  VisualDetails.java \
  GraphicsRequestData.java \
  GraphicsCullOutputDetails.java \
  GraphicsEnvironmentData.java \
  GraphicsProfilingData.java \
  GraphicsDetails.java \
  GraphicsInstructions.java \
  BufferDetails.java \
  MultipassDetails.java \
  SceneRenderBucket.java \
  MultipassRenderBucket.java \
  ViewportLayerCollection.java \
  ViewportCollection.java \
  CulledGeometryReceiver.java \
  GraphicsCullStage.java \
  BaseCullStage.java \
  SortedGeometryReceiver.java \
  StateSortComparator.java \
  GraphicsSortStage.java \
  BaseSortStage.java \
  BaseStateSortStage.java \
  GraphicsResizeListener.java \
  GraphicsListenerMulticaster.java \
  SurfaceInfo.java \
  SurfaceInfoListener.java \
  SurfaceInfoListenerMulticaster.java \
  GraphicsOutputDevice.java \
  GraphicsRenderPipeline.java \
  OverrideShapeProxyRenderable.java \
  OverrideAppearanceProxyRenderable.java \
  NullCullStage.java \
  NullSortStage.java \
  SimpleFrustumCullStage.java \
  GenericCullStage.java \
  FrustumCullStage.java \
  SimpleTransparencySortStage.java \
  StateSortStage.java \
  TransparencyDepthSortStage.java \
  StateAndTransparencyDepthSortStage.java \
  CullToSingleSortListener.java \
  SortToSingleDrawListener.java \
  DefaultGraphicsPipeline.java \
  ViewportResizeManager.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-util_1.1.0.jar vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
