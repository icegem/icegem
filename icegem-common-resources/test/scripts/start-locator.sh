#!/bin/bash
$GEMFIRE/bin/gemfire start-locator -port=${locator.port} -Dgemfire.mcast-port=0 -dir=locator -properties=locator.properties