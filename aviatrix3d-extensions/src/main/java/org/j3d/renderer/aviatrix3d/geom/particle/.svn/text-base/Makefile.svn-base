#*********************************************************************
#
#  (C) 2001 J3d.org
#    http://code.j3d.org/
# 
# Package makefile for the org.j3d.renderer.aviatrix3d.geom.particle directory
#
# Author: Justin Couch
# Version: $Revision: 2.1 $
#
#*********************************************************************

PACKAGE = org.j3d.renderer.aviatrix3d.geom.particle

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = AVParticle.java \
  AVParticleSystem.java \
  PointParticle.java \
  PointParticleSystem.java \
  TriangleFanParticle.java \
  TriangleFanParticleSystem.java \
  QuadParticle.java \
  QuadParticleSystem.java
    

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-geom-particle_1.1.0.jar j3d-org-util_1.1.0.jar \
  vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
