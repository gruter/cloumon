#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/cloumon-config.sh

"$bin"/cloumon-daemon.sh stop manager
"$bin/cloumon-daemons.sh" agent cd "$CLOUMON_HOME" \; "$bin/cloumon-daemon.sh" stop agent
"$bin/cloumon-daemons.sh" collector cd "$CLOUMON_HOME" \; "$bin/cloumon-daemon.sh" stop collector
