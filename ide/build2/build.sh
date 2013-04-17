#!/bin/bash

#configuration
product_name=CompassIDE
build_dir=$(pwd)/target/${product_name}
product=plugins/platform/cml.product
eclipse_providing_pde=/home/rwl/phd/tools/eclipse/classic_372
pde_version=3.7.0.v20111116-2009
equinox_version=1.2.0.v20110502
copy_plugins_cmd="ant -f copyProjects.xml copyStuff -DbuildDirectory=${build_dir}"
target_platform=$(pwd)/target_platform
baseos=win32
basews=win32
basearch=x86_64


#variables
plugins=${eclipse_providing_pde}/plugins
launcher=${plugins}/org.eclipse.equinox.launcher_${equinox_version}.jar
buildfile=${plugins}/org.eclipse.pde.build_${pde_version}/scripts/productBuild/productBuild.xml

#functions
function fn_error {
    echo "[Build Script Error]: ${1}";
    exit -1;
}

function print_configuration {
    echo "----------------------------------------"
    echo " Eclipse Product Build                  "
    echo "----------------------------------------"
    echo "Build Directory: ${build_dir}"
    echo "Product Definition: ${product}"
    echo "Eclipse Providing PDE: ${eclipse_providing_pde}"
    echo "Target Platform: ${target_platform} "
    echo "----------------------------------------"
}


# check configuration
if [ ! -d ${eclipse_providing_pde} ]; then 
    fn_error "Cannot find eclipse installation with the PDE tools. Please update the eclipse_providing_pde variable in the build2/build.sh script." ;
fi 

if [ ! -f ${launcher} ]; then
    fn_error "Cannot find the PDE jar in the eclipse plugin directory. Make sure the equinox version and pde version variables are set correct in the build2/build.sh script" ;
fi

### Execute ###
print_configuration 
echo "---------- BUILD DIR ----------"
rm -rf ${build_dir} || fn_error "Unable to delete ${build_dir}" ;
mkdir -p ${build_dir}/plugins || fn_error "Unable to mkdir -p ${build_dir}/plugins";
echo "Created ${build_dir}/plugins"

echo "---------- COPY ----------"
${copy_plugins_cmd} || fn_error "Unable to copy plugins" ;

echo "---------- PDE BUILD ----------"
rm -rf build.log || fn_error "Cannot delete build.log";
touch build.log || fn_error "Cannot create build.log" ;
java -jar ${launcher} \
-application org.eclipse.ant.core.antRunner \
-buildfile ${buildfile} \
-DbuildDirectory=${build_dir} \
-Dproduct=${build_dir}/${product} \
-Dbuilder=$(pwd)/build_config \
-DbaseLocation=${target_platform} \
-Dbase=$(pwd) \
-Dbaseos=${baseos} \
-Dbasews=${basews} \
-Dbasearch=${basearch} \
-DpluginPath=${target_platform} > build.log &
tail --pid $! -f build.log
echo "---------- DONE--------------------"
