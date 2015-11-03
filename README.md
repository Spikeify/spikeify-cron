# spikeify-cron
A Cron built on top of Spikeify

Cron supports
 - storing cron jobs into database to be executed
 - importing cron jobs from resource file (only when changed)
 - set schedule of cron job to executed (interval and unit, for instance: every 5 minutes)
 - set time frame within a day (run from 5:00 until 13:00)
 - cron job execution is a GET HTTP call to some URL (with basic auth if desired)
 - check and execution must triggered manually (via machine cron, thread loop ...)

Read more about it in the [Basic Usage](https://github.com/Spikeify/spikeify-cron/wiki/Basic-Usage).