#*********************************************************************
#
#  (C) 2001 - 2006 Web3D Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.j3d.renderer.aviatrix3d.device.input.mouse
# directory
#
# Author: Alan Hudson
# Version: $Revision: 1.1 $
#
#*********************************************************************

PACKAGE = org.j3d.renderer.aviatrix3d.device.input.mouse

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = MouseTracker.java \
    MouseDevice.java \
    MouseManager.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = vecmath.jar j3d-org-inputdevice_1.1.0.jar j3d-org-util_1.1.0.jar

include ${PROJECT_ROOT}/make/Makefile.jav
