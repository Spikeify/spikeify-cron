package com.spikeify.cron.entities;

import com.spikeify.Spikeify;
import com.spikeify.cron.TestHelper;
import com.spikeify.cron.data.LockCronUpdater;
import com.spikeify.cron.entities.enums.CronJobResult;
import com.spikeify.cron.entities.enums.RunEvery;
import com.spikeify.cron.exceptions.CronJobException;
import com.spikeify.cron.service.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class CronJobTest {

	Spikeify sfy;
	CronManager manager;
	CronExecutor executor;
	CronService service;

	@Before
	public void setUp() {

		sfy = TestHelper.getSpikeify();
		sfy.truncateNamespace(sfy.getNamespace());

		manager = new CronManagerImpl(sfy);
		executor = new CronExecutorImpl();
		service = new CronServiceImpl(manager, executor, null);
	}

	@Test
	public void createJobTest() {

		CronJob job = new CronJob("bla");
		assertEquals("bla", job.getName());

		assertNull(job.getFirstRun());
		assertFalse(job.isDisabled());

		assertNull(job.getRunFromHour());
		assertNull(job.getRunFromMinute());
		assertNull(job.getRunToHour());
		assertNull(job.getRunToMinute());

		assertNull(job.getIntervalUnit());
		assertEquals(0, job.getInterval());

		assertEquals(1, job.getNextRun());
		assertNull(job.getLastResult());
		assertNull(job.getLastRun());

		assertEquals(job.getId() + " [bla] - no schedule defined (job will not run)!", job.toString());

		// 2.
		job.setFirstRun(2075785196000L); // 20 years from today ... or less ;)
		assertFalse(job.isDisabled());
		assertEquals(job.getId() + " [bla] first run: 2035-10-12 06:59 - no schedule defined (job will not run)!", job.toString());

		// 3.
		job.setRunInterval(1, RunEvery.minute);
		assertFalse(job.isDisabled());
		assertEquals(job.getId() + " [bla] first run: 2035-10-12 06:59, runs every minute, missing target (job will not run), next run: 2035-10-12 06:59", job.toString());

		// 4.
		job.setRunInterval(3, RunEvery.hour);
		job.runFromTo(2, 10, 5, 25);
		assertEquals(job.getId() + " [bla] first run: 2035-10-12 06:59, runs every 3rd hour, from: 02:10, until: 05:25, missing target (job will not run), next run: 2035-10-13 02:10", job.toString());

		// 5.
		job.setTarget("http://some.url/target");
		assertEquals(job.getId() + " [bla] first run: 2035-10-12 06:59, runs every 3rd hour, from: 02:10, until: 05:25, target: http://some.url/target, next run: 2035-10-13 02:10", job.toString());


		// 6.
		job.disable();
		assertEquals(job.getId() + " [bla] first run: 2035-10-12 06:59, runs every 3rd hour, from: 02:10, until: 05:25, target: http://some.url/target, next run: disabled", job.toString());

		// 7.
		job.enable();
		job.runExactlyAt(10, 25);
		assertEquals(job.getId() + " [bla] first run: 2035-10-12 06:59, runs every day at: 10:25, target: http://some.url/target, next run: 2035-10-12 10:25", job.toString());
	}

	@Test
	public void calculateNextRunTest() {

		CronJob job = new CronJob("bla");
		job.calculateNextRun();

		// 1. take current time ... as nothing else is available
		assertTrue(System.currentTimeMillis() >= job.getNextRun());
		assertFalse(job.run());

		// a
		job.setTarget("/target");
		assertEquals("http://some/target", job.getTarget("http://some"));


		// 2.
		job.setTarget("http://some/target");
		assertTrue(System.currentTimeMillis() >= job.getNextRun());
		assertFalse(job.run());

		// 3.
		job.setRunInterval(1, RunEvery.minute);
		assertTrue(System.currentTimeMillis() >= job.getNextRun());
		assertTrue(job.run());

		long lastRun = System.currentTimeMillis();
		job.setLastRun(lastRun, CronJobResult.ok, null);
		job.calculateNextRun();

		assertEquals(lastRun + 60L * 1000L, job.getNextRun());
		assertFalse(job.run());

		// 4. set from to limit
		// simulate time
		CronJob spiedJob = Mockito.spy(job);
		Mockito.when(spiedJob.getTime()).thenReturn(1444468800000L); // 10 Oct 2015 09:20:00

		spiedJob.setLastRun(1444468800000L, CronJobResult.ok, null); // 10 Oct 2015 09:20:00
		spiedJob.runFromTo(10, 30, 11, 20);

		spiedJob.calculateNextRun();
		assertEquals(1444473000000L, spiedJob.getNextRun()); // expected 10 Oct 2015 10:30
		assertEquals(spiedJob.getId() + " [bla] runs every minute, from: 10:30, until: 11:20, target: http://some/target, next run: 2015-10-10 10:30", spiedJob.toString());

		// time is: 10 Oct 2015 10:30:00
		Mockito.when(spiedJob.getTime()).thenReturn(1444473030000L); // 10 Oct 2015 10:30:30
		spiedJob.calculateNextRun();
		assertEquals(1444473030000L, spiedJob.getNextRun()); // expected 10 Oct 2015 10:30:30
		assertEquals(spiedJob.getId() + " [bla] runs every minute, from: 10:30, until: 11:20, target: http://some/target, next run: 2015-10-10 10:30", spiedJob.toString());

		// set last run to: 10 Oct 2015 10:30:00
		spiedJob.setLastRun(1444473000000L, CronJobResult.ok, null);
		spiedJob.calculateNextRun();
		assertEquals(1444473060000L, spiedJob.getNextRun()); // expected 10 Oct 2015 10:31:00
		assertEquals(spiedJob.getId() + " [bla] runs every minute, from: 10:30, until: 11:20, target: http://some/target, next run: 2015-10-10 10:31", spiedJob.toString());

		// set last run to: 10 Oct 2015 11:19:20
		Mockito.when(spiedJob.getTime()).thenReturn(1444475960000L);
		spiedJob.setLastRun(1444475960000L, CronJobResult.ok, null);
		spiedJob.calculateNextRun();
		assertEquals(1444559420000L, spiedJob.getNextRun()); // expected 11 Oct 2015 10:30:00
		assertEquals(spiedJob.getId() + " [bla] runs every minute, from: 10:30, until: 11:20, target: http://some/target, next run: 2015-10-11 10:30", spiedJob.toString());

		// run from is greater than run to ...
		spiedJob.runFromTo(11, 20, 10, 30);

		// set time: 10 Oct 2015 11:20:20
		Mockito.when(spiedJob.getTime()).thenReturn(1444476020000L);
		spiedJob.setLastRun(1444476020000L, CronJobResult.ok, null);

		spiedJob.calculateNextRun();
		assertEquals(1444476080000L, spiedJob.getNextRun()); // expected 10 Oct 2015 11:21:00
		assertEquals(spiedJob.getId() + " [bla] runs every minute, from: 11:20, until: 10:30, target: http://some/target, next run: 2015-10-10 11:21", spiedJob.toString());

		// set time: 10 Oct 2015 23:59:59
		Mockito.when(spiedJob.getTime()).thenReturn(1444521599000L);
		spiedJob.setLastRun(1444521599000L, CronJobResult.ok, null);

		spiedJob.calculateNextRun();
		assertEquals(1444521659000L, spiedJob.getNextRun()); // expected 11 Oct 2015 00:00:59
		assertEquals(spiedJob.getId() + " [bla] runs every minute, from: 11:20, until: 10:30, target: http://some/target, next run: 2015-10-11 00:00", spiedJob.toString());

		// set time to 10 Oct 2015 10:31:00
		Mockito.when(spiedJob.getTime()).thenReturn(1444473060000L);
		spiedJob.setLastRun(1444473060000L, CronJobResult.ok, null);

		spiedJob.calculateNextRun();
		assertEquals(1444476000000L, spiedJob.getNextRun()); // expected 10 Oct 2015 11:20:00
		assertEquals(spiedJob.getId() + " [bla] runs every minute, from: 11:20, until: 10:30, target: http://some/target, next run: 2015-10-10 11:20", spiedJob.toString());

		// set desired first run
		// set time: 10 Oct 2015 23:59:59
		Mockito.when(spiedJob.getTime()).thenReturn(1444521599000L);
		spiedJob.setFirstRun(1444559420000L); // 11 Oct 2015 10:30:00

		spiedJob.calculateNextRun();
		assertEquals(1444562420000L, spiedJob.getNextRun()); // expected 11 Oct 2015 11:20:00
		assertEquals(spiedJob.getId() + " [bla] first run: 2015-10-11 10:30, runs every minute, from: 11:20, until: 10:30, target: http://some/target, next run: 2015-10-11 11:20", spiedJob.toString());
	}

	@Test
	public void getDescriptionTest() {
		CronJob job = new CronJob("bla");
		assertEquals("bla", job.getName());

		// 2.
		job.setFirstRun(2075785196000L); // 20 years from today ... or less ;)
		assertFalse(job.isDisabled());
		assertEquals("first run: 2035-10-12 06:59 - no schedule defined (job will not run)!", job.getDescription(true, 0));
		assertEquals("first run: 2035-10-11 19:59 - no schedule defined (job will not run)!", job.getDescription(true, -11));
		assertEquals("first run: 2035-10-12 18:59 - no schedule defined (job will not run)!", job.getDescription(true, 12));

		// 3.
		job.setRunInterval(1, RunEvery.minute);
		assertFalse(job.isDisabled());
		assertEquals("first run: 2035-10-12 06:59, runs every minute, missing target (job will not run), next run: 2035-10-12 06:59", job.getDescription(true, 0));
		assertEquals("first run: 2035-10-11 19:59, runs every minute, missing target (job will not run), next run: 2035-10-11 19:59", job.getDescription(true, -11));
		assertEquals("first run: 2035-10-12 18:59, runs every minute, missing target (job will not run), next run: 2035-10-12 18:59", job.getDescription(true, 12));

		// 4.
		job.setRunInterval(3, RunEvery.hour);
		job.runFromTo(2, 10, 5, 25);
		assertEquals("first run: 2035-10-12 06:59, runs every 3rd hour, from: 02:10, until: 05:25, missing target (job will not run), next run: 2035-10-13 02:10", job.getDescription(true, 0));
		assertEquals("first run: 2035-10-11 19:59, runs every 3rd hour, from: 15:10, until: 18:25, missing target (job will not run), next run: 2035-10-12 15:10", job.getDescription(true, -11));
		assertEquals("first run: 2035-10-12 18:59, runs every 3rd hour, from: 14:10, until: 17:25, missing target (job will not run), next run: 2035-10-13 14:10", job.getDescription(true, 12));

		// 5.
		job.setTarget("http://some.url/target");
		assertEquals("first run: 2035-10-12 06:59, runs every 3rd hour, from: 02:10, until: 05:25, target: http://some.url/target, next run: 2035-10-13 02:10", job.getDescription(true, 0));

		assertEquals("first run: 2035-10-11 19:59, runs every 3rd hour, from: 15:10, until: 18:25, target: http://some.url/target, next run: 2035-10-12 15:10", job.getDescription(true, -11));
		assertEquals("first run: 2035-10-12 18:59, runs every 3rd hour, from: 14:10, until: 17:25, target: http://some.url/target, next run: 2035-10-13 14:10", job.getDescription(true, 12));
		assertEquals(2075854256000L, job.getNextRun()); //  Sat, 13 Oct 2035 02:10:56 GMT
		assertEquals(2075897456000L, job.getNextRun(12)); //  Sat, 13 Oct 2035 14:10:56 GMT
		assertEquals(2075814656000L, job.getNextRun(-11)); //  Fri, 12 Oct 2035 15:10:56 GMT


		// 6.
		job.disable();
		assertEquals("first run: 2035-10-12 06:59, runs every 3rd hour, from: 02:10, until: 05:25, target: http://some.url/target, next run: disabled", job.getDescription(true, 0));
		assertEquals("first run: 2035-10-11 19:59, runs every 3rd hour, from: 15:10, until: 18:25, target: http://some.url/target, next run: disabled", job.getDescription(true, -11));
		assertEquals("first run: 2035-10-12 18:59, runs every 3rd hour, from: 14:10, until: 17:25, target: http://some.url/target, next run: disabled", job.getDescription(true, 12));

		// 7.
		job.enable();
		job.runExactlyAt(10, 25);
		assertEquals("first run: 2035-10-12 06:59, runs every day at: 10:25, target: http://some.url/target, next run: 2035-10-12 10:25", job.getDescription(true, 0));
		assertEquals("first run: 2035-10-11 19:59, runs every day at: 23:25, target: http://some.url/target, next run: 2035-10-11 23:25", job.getDescription(true, -11));
		assertEquals("first run: 2035-10-12 18:59, runs every day at: 22:25, target: http://some.url/target, next run: 2035-10-12 22:25", job.getDescription(true, 12));
	}

	@Test
	public void testLockedCronJob() {

		// job was locked but never executed ... test if he is still locked after 1 minute
		/*"id": "vGyezkyQC7",
			"name": "repositoryScan",
			"firstRun": null,
			"lastRun": null,
			"lastResult": "ok",
			"nextRun": 1459859251979,
			"interval": 5,
			"intervalUnit": "minute",
			"runFromHour": null,
			"runFromMinute": null,
			"runToHour": null,
			"runToMinute": null,
			"started": true,
			"disabled": false,
			"startedTime": 55001224521869,
			"lastResultMessage": null*/

		CronJob job = new CronJob("Bla");
		job.lastRun = null;
		job.nextRun = 1459859251979L;
		job.startTime = 55001224521869L;
		job.interval = 5;
		job.intervalUnit = RunEvery.minute;
		job.target = "/test";

		assertFalse(job.isLocked());
		assertTrue(job.canRun());
		assertTrue(job.run());

		// job is locked start time is set and next run is now
		job.nextRun = System.currentTimeMillis();
		job.setStarted(System.nanoTime());
		assertTrue(job.isLocked());
		assertTrue(job.canRun()); // it could run if were not locked
		assertFalse(job.run());

		// at least one minute must past from next run to unlock job with start time set
		job.nextRun = System.currentTimeMillis() - 60L * 1000;
		job.setStarted(System.nanoTime());
		assertFalse(job.isLocked());        // it is unlocked and it can run
		assertTrue(job.canRun());
		assertTrue(job.run());
	}


	@Test
	public void startDeadlockedJob() throws CronJobException {
		// job was locked but never executed ... test if he is still locked after 1 minute
		CronJob job = new CronJob("Bla");
		job.id = "balabala";
		job.lastRun = null;
		job.nextRun = 1459859251979L;
		job.startTime = 55001224521869L;
		job.interval = 5;
		job.intervalUnit = RunEvery.minute;
		job.target = "/test";

		sfy.update(job).now();

		int count = service.run();
		assertEquals(1, count);  // job was started
	}
}