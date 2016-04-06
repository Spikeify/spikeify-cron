package com.spikeify.cron.service;

import com.spikeify.Spikeify;
import com.spikeify.cron.TestHelper;
import com.spikeify.cron.data.CronJsonUpdater;
import com.spikeify.cron.data.LastRunUpdater;
import com.spikeify.cron.data.LockCronUpdater;
import com.spikeify.cron.data.ScheduleUpdater;
import com.spikeify.cron.data.json.CronJobJSON;
import com.spikeify.cron.entities.CronJob;
import com.spikeify.cron.entities.enums.CronJobResult;
import com.spikeify.cron.entities.enums.RunEvery;
import com.spikeify.cron.exceptions.CronJobException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class CronManagerImplTest {

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
		service = new CronServiceImpl(manager, executor, new DefaultCronSettings("http://some/"));
	}

	@After
	public void tearDown() {

		sfy.truncateNamespace(sfy.getNamespace());
	}

	@Test
	public void testCreate() throws Exception {

		// simple create test
		CronJob job = manager.create("  new cron job  ");
		assertNotNull(job);
		assertNotNull(job.getId());
		assertEquals("new cron job", job.getName());

		// 2
		CronJob compare = manager.findByName("new cron job");
		assertEquals(job.getId(), compare.getId());

		// 3
		compare = manager.get(job.getId());
		assertEquals(job.getId(), compare.getId());
	}

	@Test(expected = CronJobException.class)
	public void testGet() throws CronJobException {

		try {
			manager.get("bla");
		}
		catch (CronJobException e) {
			assertEquals("Job with id: 'bla', not found!", e.getMessage());
			assertEquals(404, e.getErrorCode());
			throw e;
		}
	}

	@Test
	public void testFind() {

		assertNull(manager.find("bla"));
	}

	@Test
	public void testCreateWithSameName() throws CronJobException {
		// duplicate same id test
		// 1st one is a success
		manager.create("new job");

		// 2nd one should fail
		try {
			manager.create("new job");
			assertTrue("Should not come this far!", false);
		}
		catch (CronJobException e) {
			assertEquals(409, e.getErrorCode());
			assertEquals("Job named: 'new job', already exists!", e.getMessage());
		}
	}

	@Test
	public void testUpdate() throws Exception {

		CronJob job = manager.create("  new cron job  ");
		assertNotNull(job);
		assertNotNull(job.getId());
		assertEquals("new cron job", job.getName());
		assertEquals(1, job.getNextRun());

		// 1.
		String rootUrl = "http://some/";

		CronJob updated = manager.update(job, new ScheduleUpdater("url", 10, RunEvery.week));
		assertEquals(10, updated.getInterval());
		assertEquals(RunEvery.week, updated.getIntervalUnit());
		assertEquals("http://some/url", updated.getTarget(rootUrl));
		assertNull(updated.getRunFromHour());
		assertNull(updated.getRunFromMinute());
		assertNull(updated.getRunToHour());
		assertNull(updated.getRunToMinute());
//		assertTrue(updated.getNextRun() <= System.currentTimeMillis());

		// 2.
		updated = manager.update(job, new ScheduleUpdater("other/url", 2, RunEvery.day, 10, 30, 19, 45, 0));
		assertEquals(2, updated.getInterval());
		assertEquals(RunEvery.day, updated.getIntervalUnit());
		assertEquals("http://some/other/url", updated.getTarget(rootUrl));
		assertEquals(10, updated.getRunFromHour().longValue());
		assertEquals(30, updated.getRunFromMinute().longValue());
		assertEquals(19, updated.getRunToHour().longValue());
		assertEquals(45, updated.getRunToMinute().longValue());
//		assertTrue(updated.getNextRun() <= System.currentTimeMillis());

		// 3.
		updated = manager.update(job, new ScheduleUpdater("other/url", 2, RunEvery.day, 10, 30, 19, 45, 2));
		assertEquals(2, updated.getInterval());
		assertEquals(RunEvery.day, updated.getIntervalUnit());
		assertEquals("http://some/other/url", updated.getTarget(rootUrl));
		assertEquals(8, updated.getRunFromHour().longValue());
		assertEquals(30, updated.getRunFromMinute().longValue());
		assertEquals(17, updated.getRunToHour().longValue());
		assertEquals(45, updated.getRunToMinute().longValue());

		// 4.
		updated = manager.update(job, new ScheduleUpdater("once/url", 11, 44, 0));
		assertEquals(1, updated.getInterval());
		assertEquals(RunEvery.day, updated.getIntervalUnit());
		assertEquals("http://some/once/url", updated.getTarget(rootUrl));
		assertEquals(11, updated.getRunFromHour().longValue());
		assertEquals(44, updated.getRunFromMinute().longValue());
		assertNull(updated.getRunToHour());
		assertNull(updated.getRunToMinute());
//		assertTrue(updated.getNextRun() <= System.currentTimeMillis());

		// 5. update with timezone
		updated = manager.update(job, new ScheduleUpdater("once/url", 11, 44, 2));
		assertEquals(1, updated.getInterval());
		assertEquals(RunEvery.day, updated.getIntervalUnit());
		assertEquals("http://some/once/url", updated.getTarget(rootUrl));
		assertEquals(9, updated.getRunFromHour().longValue());
		assertEquals(44, updated.getRunFromMinute().longValue());
		assertNull(updated.getRunToHour());
		assertNull(updated.getRunToMinute());

		// 6.
		updated = manager.update(job, new ScheduleUpdater("once/url", 11, 44, -2));
		assertEquals(1, updated.getInterval());
		assertEquals(RunEvery.day, updated.getIntervalUnit());
		assertEquals("http://some/once/url", updated.getTarget(rootUrl));
		assertEquals(13, updated.getRunFromHour().longValue());
		assertEquals(44, updated.getRunFromMinute().longValue());
		assertNull(updated.getRunToHour());
		assertNull(updated.getRunToMinute());

		// 7.
		updated = manager.update(job, new ScheduleUpdater("once/url", 11, 44, -11));
		assertEquals(1, updated.getInterval());
		assertEquals(RunEvery.day, updated.getIntervalUnit());
		assertEquals("http://some/once/url", updated.getTarget(rootUrl));
		assertEquals(22, updated.getRunFromHour().longValue());
		assertEquals(44, updated.getRunFromMinute().longValue());
		assertNull(updated.getRunToHour());
		assertNull(updated.getRunToMinute());

		// 8.
		updated = manager.update(job, new ScheduleUpdater("once/url", 0, 44, -11));
		assertEquals(1, updated.getInterval());
		assertEquals(RunEvery.day, updated.getIntervalUnit());
		assertEquals("http://some/once/url", updated.getTarget(rootUrl));
		assertEquals(11, updated.getRunFromHour().longValue());
		assertEquals(44, updated.getRunFromMinute().longValue());
		assertNull(updated.getRunToHour());
		assertNull(updated.getRunToMinute());
	}

	@Test
	public void testUpdateWithJSON() throws CronJobException {

		CronJobJSON one = new CronJobJSON();
		one.target = "http://localhost";
		one.name = "one";
		one.disabled = false;
		one.interval = 5;
		one.intervalUnits = RunEvery.day;
		one.startHour = 10;
		one.startMinute = 20;
		one.endHour = 20;
		one.endMinute = 30;

		CronJob job = manager.create("one");
		CronJob compare = manager.update(job, new CronJsonUpdater(one, 0));

		assertEquals("one", compare.getName());
		assertEquals(5, compare.getInterval());
		assertEquals(RunEvery.day, compare.getIntervalUnit());
		assertEquals(10, compare.getRunFromHour().intValue());
		assertEquals(20, compare.getRunFromMinute().intValue());
		assertEquals(20, compare.getRunToHour().intValue());
		assertEquals(30, compare.getRunToMinute().intValue());

		//
		one.startHour = null;
		compare = manager.update(job, new CronJsonUpdater(one, 0));

		assertEquals("one", compare.getName());
		assertEquals(5, compare.getInterval());
		assertEquals(RunEvery.day, compare.getIntervalUnit());
		assertNull(compare.getRunFromHour());
		assertNull(compare.getRunFromMinute());
		assertNull(compare.getRunToHour());
		assertNull(compare.getRunToMinute());
	}

	@Test
	public void testList() throws Exception {

		for (int i = 0; i < 10; i++) {
			manager.create("job" + i);
		}

		List<CronJob> list = manager.list();
		assertEquals(10, list.size());

		Set<String> ids = new HashSet<>();
		for (int i = 0; i < 10; i++) {
			ids.add(list.get(i).getId());
		}

		assertEquals(10, ids.size());
	}

	@Test
	public void testListByTime() throws Exception {

		for (int i = 0; i < 10; i++) {
			manager.create("job" + i);
		}

		// empty jobs none should run
		List<CronJob> list = manager.list(System.currentTimeMillis());
		assertEquals(0, list.size());

		// 2. change a job to be able to run
		CronJob job = manager.create("runnable");
		CronJob updated = manager.update(job, new ScheduleUpdater("http://some/url", 1, RunEvery.minute));

		assertTrue(updated.getNextRun() <= System.currentTimeMillis());

		list = manager.list(System.currentTimeMillis());
		assertEquals(1, list.size());
		CronJob compare = list.get(0);

		assertEquals(job.getId(), compare.getId());
	}

	@Test
	public void runAndSetNextRunTime() throws CronJobException {

		CronJob job = manager.create("runnable");
		manager.update(job, new ScheduleUpdater("http://some/url", 1, RunEvery.minute));

		// set last run time
		manager.update(job, new LastRunUpdater(System.currentTimeMillis(), CronJobResult.ok, null));

		List<CronJob> list = manager.list();
		assertEquals(1, list.size());

		CronJob compare = list.get(0);
		assertTrue(compare.getNextRun() > System.currentTimeMillis() + (50L * 1000L)); // 50s plus last run
		assertTrue(compare.getNextRun() < System.currentTimeMillis() + (70L * 1000L)); // 70s plus last run

		list = manager.list(System.currentTimeMillis());
		assertEquals(0, list.size()); // no jobs should be returned
	}
}