#
# Top Level makefile for the Aviatrix3D project
#
# This makefile is designed to build the entire library from scratch. It is
# not desigend as a hacking system. It is recommended that you use the normal
# javac/CLASSPATH setup for that.
#
# The following commands are offered:
#
# - class:  Compile just the classes. Don't make JAR file
# - jar:      Make the java JAR file
# - javadoc:  Generate the javadoc information
# - shaders:  Copy the shader definitions into the classes area
# - all:      Build everything (including docs)
# - clean:    Blow everything away
#

ifndef PROJECT_ROOT
export PROJECT_ROOT=$(PWD)
endif

include $(PROJECT_ROOT)/make/Makefile.inc

# Default instruction is to print out the help list
help:
	$(PRINT) 
	$(PRINT) "                   The Aviatrix3D Project"
	$(PRINT) 
	$(PRINT) "More information on this project can be found at http://aviatrix3d.j3d.org"
	$(PRINT) 
	$(PRINT) "The following options are offered and will build the codebase withouth SWT:"
	$(PRINT) 
	$(PRINT) "class:       Compile just the classes. Don't make JAR files."
	$(PRINT) "jar:         Make the java JAR file"
	$(PRINT) "javadoc:     Generate the javadoc information"
	$(PRINT)
	$(PRINT) "The following options will build the codebase with SWT support"
	$(PRINT)
	$(PRINT) "swt-class:       Compile just the classes. Don't make JAR files."
	$(PRINT) "swt-jar:         Make the java JAR file"
	$(PRINT) "swt-javadoc:     Generate the javadoc information"
	$(PRINT)
	$(PRINT) "The following options are generic across the codebase"
	$(PRINT)
	$(PRINT) "config:      Make the configuration files"
	$(PRINT) "shaders:     Copy the shader source files into the classes area"
	$(PRINT) "shaderjar:   Build the shader files into a separate JAR"
	$(PRINT) "all:         Build everything (including docs)"
	$(PRINT) "clean:       Blow all the library classes away"
	$(PRINT) 

all: class config shaders jar javadoc

class:
	make -f $(JAVA_DIR)/Makefile buildall

jar: class config shaderjar
	make -f $(JAVA_DIR)/Makefile jar

config:
	make -f $(CONFIG_DIR)/Makefile buildall

shaders:
	make -f $(SHADER_DIR)/Makefile buildall

shaderjar: shaders
	make -f $(SHADER_DIR)/Makefile jar

javadoc:
	make -f $(JAVA_DIR)/Makefile javadoc

swt-class:
	make -f $(JAVA_DIR)/Makefile-swt buildall

swt-jar: swt-class config shaderjar
	make -f $(JAVA_DIR)/Makefile-swt jar

swt-javadoc:
	make -f $(JAVA_DIR)/Makefile-swt javadoc


clean:
	make -f $(JAVA_DIR)/Makefile clean
	make -f $(CONFIG_DIR)/Makefile clean
	make -f $(SHADER_DIR)/Makefile clean
