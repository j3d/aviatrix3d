#*********************************************************************
#
#                      (C) 2003 Yumetech, Inc
#  		       http://aviatrix3d.j3d.org
# 
#*********************************************************************

# Names of packages to be compiled. These are declared in the order that
# they are to be compiled in. We create one at a time
BUILD_ORDER = org.j3d.aviatrix3d \


# Names of packages that we want included in the javadoc
JAVADOC_PACKAGES = org.j3d.aviatrix3d \

# Names of JAR files we want to create
JARS = aviatrix3d-all.jar

# List of packages that have native code libraries in use and require
# JNI header generation
NATIVE_PACKAGES = 


# 3rd party libraries that need including.
JARS_3RDPARTY=

# Definitions of setup information if we are making a self-executable JAR
MAIN_CLASS     = 
MAIN_PACKAGE   = 
MAIN_JAR       = aviatrix3d-all.jar

#*********************************************************************
#
# Javadoc
#
#*********************************************************************

OVERVIEW    = overview.html
WINDOWTITLE = 'j3d.org Aviatrix3D Code Library'
DOCTITLE    = 'j3d.org Aviatrix3D Project API'
HEADER      = 'j3d.org Aviatrix3D'
BOTTOM      = '<center> \
               Latest Info from <a href="http://aviatrix3d.j3d.org"> \
               http://aviatrix3d.j3d.org/</a><br> \
               <font size=2>Copyright &copy; 2003 - \
               <a href="http://www.j3d.org">j3d.org</a></font> \
               </center>'

# A list of the local directories that contain other Javadoc that we want
# to link to. For example we link to the JDK, JAXP, SAI and JUnit Javadocs
# here. Note that these *must* be full URLs and not just local directory 
# references. If your directory path contains a space in it, you may be in
# trouble. We haven't tested it with that. Try using double quotes inside
# the single quotes to get the space treated in the file name
#LINK_URLS   = 'file:///c:/java/jaxp-1.1/docs/api/' \
#              'file:///c:/java/junit3.5/javadoc/' \
#			  'file:///c:/java/jdk1.3/docs/api/' \
#			  'file:///c:/java/j3d/1.2.1/html/'

include $(PROJECT_ROOT)/make/Makefile.jav 
