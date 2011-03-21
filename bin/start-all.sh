#!/usr/bin/env bash

# Start cloumon daemons.  Run this on master node.

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/cloumon-config.sh

"$bin"/cloumon-daemon.sh start manager
sleep 10
"$bin/cloumon-daemons.sh" collector cd "$CLOUMON_HOME" \; "$bin/cloumon-daemon.sh" start collector
sleep 10
"$bin/cloumon-daemons.sh" agent cd "$CLOUMON_HOME" \; "$bin/cloumon-daemon.sh" start agent
