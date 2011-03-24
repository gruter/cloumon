# Set Neptune-specific environment variables here.
# JAVA_HOME, CLOUMON_HOME

#JAVA HOME dir
export JAVA_HOME=/usr/java/jdk1.6.0_06

#CLOUMON home dir
export CLOUMON_HOME=/home/cloumon/cloumon-1.0.0

#CLOUMON conf dis
export CLOUMON_CONF_DIR="${CLOUMON_HOME}/conf"

# Extra Java CLASSPATH elements.  Optional.
#export CLOUMON_CLASSPATH=

# The directory where pid files are stored. /tmp by default.
export CLOUMON_PID_DIR=~/.cloumon_pids

# A string representing this instance of neptune. $USER by default.
# export CLOUMON_IDENT_STRING=$USER