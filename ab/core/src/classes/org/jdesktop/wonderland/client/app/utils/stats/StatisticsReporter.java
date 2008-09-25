/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.client.app.utils.stats;

import org.jdesktop.wonderland.common.StableAPI;

/**
 * Periodically reports statistics. This class reports the values 
 * over the last measurement period, max, and cumulative values.
 *
 * @author deronj
 */ 

@StableAPI
public class StatisticsReporter implements Runnable {

    // By default report statistics every 30 sec
    private static final int REPORT_PERIOD_MS = 30000; 

    protected int reportPeriodMs = REPORT_PERIOD_MS;

    protected StatisticsSet period;
    protected StatisticsSet max;
    protected StatisticsSet cumulative;

    protected double startTimeSecs;
    protected double probeIntervalSecs;
    protected double totalSecs;

    private boolean stop;

    private Thread thread;

    private String name;

    /**
     * statSetClass specifies the subclass of StatisticSet that this reporter
     * should report for.
     */
    public StatisticsReporter (StatisticsSet period, StatisticsSet max, 
			       StatisticsSet cumulative) {
	this.period = period;
	this.max = max;
	this.cumulative = cumulative;
	name = period.getName();
    }

    public StatisticsReporter (int reportPeriodSecs, StatisticsSet period, 
			       StatisticsSet max, StatisticsSet cumulative) {
	this(period, max, cumulative);
	reportPeriodMs = reportPeriodSecs * 1000;
    }

    public void start () {
	thread = new Thread(this, name + " Statistics Reporter");
	System.err.println("Starting " + name + " Statistics Reporter");
	thread.start();
    }

    public void stop () {
	stop = true;
	thread = null;
    }

    private double currentTimeMillis () {
	long currentTimeNanos = System.nanoTime();
	return (double)currentTimeNanos / 1000000.0;
    }

    private void startTimer() {
	startTimeSecs = currentTimeMillis() / 1000.0;
    }

    private void stopTimer() {
	double stopTimeSecs = currentTimeMillis() / 1000.0;
	probeIntervalSecs = stopTimeSecs - startTimeSecs;
	totalSecs += probeIntervalSecs;
    }

    public void run () {
	while (!stop) {

	    startTimer();
	    try { Thread.sleep(reportPeriodMs); } catch (InterruptedException ex) {}
	    stopTimer();

	    period.probe();
	    period.max(max);
	    period.accumulate(cumulative);

	    if (period.hasTriggered() ||
		max.hasTriggered() ||
		cumulative.hasTriggered()) {
		printStats();
	    }

	    period.reset();
	}	    

	System.err.println();
	System.err.println("Stopped " + name + " Statistics Reporter");
    }

    private void printStats () {
	System.err.println("--------------------------------------------------------");
	System.err.println();
	System.err.println(name + " statistics for last period (" + 
			   probeIntervalSecs + " secs)");
	period.printStatsAndRates(probeIntervalSecs);

	System.err.println();
	System.err.println(name + " statistics maximums");
	max.printStats();

	System.err.println();
	System.err.println(name + " statistics cumulative");
	cumulative.printStatsAndRates(totalSecs);
    }
}

