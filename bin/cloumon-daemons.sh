#!/usr/bin/env bash
#
# Run a shell command on all slave hosts.
#

usage="Usage: cloumon-daemons.sh command..."

# if no args specified, show usage
if [ $# -le 0 ]; then
  echo $usage
  exit 1
fi

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/cloumon-config.sh

if [ -f "${CLOUMON_CONF_DIR}/cloumon-env.sh" ]; then
  . "${CLOUMON_CONF_DIR}/cloumon-env.sh"
fi

COMMAND=$1
shift

# figure out which class to run
if [ "$COMMAND" = "collector" ] ; then
  export HOSTLIST="${CLOUMON_CONF_DIR}/collectors"
elif [ "$COMMAND" = "agent" ] ; then
  export HOSTLIST="${CLOUMON_CONF_DIR}/agents"
else
  echo "check parameter [collector or agent]"
  exit 1
fi

for slave in `cat "$HOSTLIST"`; do
 ssh $CLOUMON_SSH_OPTS $slave $"${@// /\\ }" \
   2>&1 | sed "s/^/$slave: /" &
 if [ "$CLOUMON_SLAVE_SLEEP" != "" ]; then
   sleep $CLOUMON_SLAVE_SLEEP
 fi
done

wait
