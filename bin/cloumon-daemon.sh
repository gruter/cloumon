#!/usr/bin/env bash
# 
# Runs a Neptune command as a daemon.
#
# Environment Variables
#

usage="Usage: cloumon-daemon.sh (start|stop) <command>"

# if no args specified, show usage
if [ $# -le 1 ]; then
  echo $usage
  exit 1
fi

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/cloumon-config.sh

if [ -f "${CLOUMON_CONF_DIR}/cloumon-env.sh" ]; then
  . "${CLOUMON_CONF_DIR}/cloumon-env.sh"
fi

startStop=$1
shift
command=$1
shift

cloumon_rotate_log ()
{
    log=$1;
    num=5;
    if [ -n "$2" ]; then
	num=$2
    fi
    if [ -f "$log" ]; then # rotate logs
	while [ $num -gt 1 ]; do
	    prev=`expr $num - 1`
	    [ -f "$log.$prev" ] && mv "$log.$prev" "$log.$num"
	    num=$prev
	done
	mv "$log" "$log.$num";
    fi
}

if [ "$CLOUMON_LOG_DIR" = "" ]; then
  export CLOUMON_LOG_DIR="$CLOUMON_HOME/logs"
fi
mkdir -p "$CLOUMON_LOG_DIR"

if [ "$CLOUMON_PID_DIR" = "" ]; then
  CLOUMON_PID_DIR=/tmp
fi

if [ "$CLOUMON_IDENT_STRING" = "" ]; then
  export CLOUMON_IDENT_STRING="$USER"
fi

# some variables
export CLOUMON_LOGFILE=cloumon-$CLOUMON_IDENT_STRING-$command-`hostname`.log
export CLOUMON_ROOT_LOGGER="INFO,DRFA"
export CLOUMON_ROOT_LOGGER_APPENDER="DRFA"
log=$CLOUMON_LOG_DIR/cloumon-$CLOUMON_IDENT_STRING-$command-`hostname`.out
pid=$CLOUMON_PID_DIR/cloumon-$CLOUMON_IDENT_STRING-$command.pid

if [ ! -e $CLOUMON_PID_DIR ]; then
    mkdir $CLOUMON_PID_DIR
fi
    
# Set default scheduling priority
if [ "$CLOUMON_NICENESS" = "" ]; then
    export CLOUMON_NICENESS=0
fi

case $startStop in

  (start)

    if [ -f $pid ]; then
      if kill -0 `cat $pid` > /dev/null 2>&1; then
        echo $command running as process `cat $pid`.  Stop it first.
        exit 1
      fi
    fi

    cloumon_rotate_log $log
    echo starting $command, logging to $log
    nohup nice -n $CLOUMON_NICENESS "$CLOUMON_HOME"/bin/cloumon $command "$@" > "$log" 2>&1 < /dev/null &
    echo $! > $pid
    sleep 1; head "$log"
    ;;
          
  (stop)

    if [ -f $pid ]; then
      if kill -9 `cat $pid` > /dev/null 2>&1; then
        echo stopping $command
        kill `cat $pid`
      else
        echo no $command to stop
      fi
    else
      echo no $command to stop
    fi
    ;;

  (*)
    echo $usage
    exit 1
    ;;

esac
