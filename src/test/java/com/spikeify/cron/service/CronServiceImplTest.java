package com.spikeify.cron.service;

import com.spikeify.cron.TestHelper;
import com.spikeify.cron.data.CronExecutorResult;
import com.spikeify.cron.data.ScheduleUpdater;
import com.spikeify.cron.data.json.CronJobJSON;
import com.spikeify.cron.entities.CronJob;
import com.spikeify.cron.entities.DummyCronJob;
import com.spikeify.cron.entities.enums.CronJobResult;
import com.spikeify.cron.entities.enums.RunEvery;
import com.spikeify.cron.exceptions.CronJobException;
import com.spikeify.Spikeify;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CronServiceImplTest {

	private static final long ONE_WEEK = 7L * 24L * 60L * 60L * 1000L;

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

	@After
	public void tearDown() {

	//	sfy.truncateNamespace(sfy.getNamespace());
	}

	@Test
	public void testCreateUpdate() throws Exception {

		CronJob job = service.create("Bla");
		assertNotNull(job);
		assertNotNull(job.getId());
		assertEquals("Bla", job.getName());
		assertFalse(job.run());

		CronJob compare = service.update(job, new ScheduleUpdater("http://localhost/", 1, RunEvery.week));
		assertEquals(RunEvery.week, compare.getIntervalUnit());
		assertEquals(1, compare.getInterval());
	}

	@Test
	public void testList() throws Exception {

		List<CronJob> list = service.list();
		assertEquals(0, list.size());

		service.create("Bla");
		list = service.list();
		assertEquals(1, list.size());
		assertEquals("Bla", list.get(0).getName());
	}

	@Test
	public void testRun() throws Exception {

		List<CronJob> list = service.list();
		assertEquals(0, list.size());

		// no job should run ...
		int count = service.run();
		assertEquals(0, count);

		CronJob job = service.create("Bla");
		count = service.run();
		assertEquals(0, count);

		service.update(job, new ScheduleUpdater("http://localhost/", 1, RunEvery.week));
		count = service.run();
		assertEquals(1, count);

		// make sure job is not run again
		count = service.run();
		assertEquals(0, count);

		// get job next run
		CronJob found = service.find(job.getId());
		assertTrue(found.getNextRun() > System.currentTimeMillis() + ONE_WEEK - 10000L);
		assertTrue(found.getNextRun() < System.currentTimeMillis() + ONE_WEEK + 10000L);
	}

	@Test
	public void testRunSingleJob() throws Exception {

		CronJob job = service.create("Bla");
		CronExecutorResult result = service.run(job);
		Assert.assertEquals(CronJobResult.fail, result.getJobResult());
		assertEquals("No URL given, can't run!", result.getMessage());
	}

	@Test
	public void testDeleteJob() throws CronJobException {

		CronJob job = service.create("Bla");
		service.delete(job);

		assertNull(service.find(job.getId()));

		service.delete(job);
	}

	@Test
	public void testExportImportJobs() throws CronJobException {

		CronJob one = service.create("one");
		one = service.update(one, new ScheduleUpdater("http://localhost/", 5, RunEvery.minute, 10, 30, 11, 45, 0));

		CronJob two = service.create("two");
		two = service.update(two, new ScheduleUpdater("http://localhost/", 10, 30, 0));

		CronJob three = service.create("three");
		three = service.update(three, new ScheduleUpdater("http://localhost/", 1, RunEvery.hour));

		List<CronJobJSON> list = service.exportJobs(0);

		service.delete(one);
		service.delete(three);

		assertEquals(1, service.list().size());

		service.importJobs(list, 0);
		List<CronJob> compare = service.list();
		assertEquals(3, compare.size());

		int count = 0;
		for (CronJob job : compare) {

			if (job.equals(one) ||
				job.equals(two) ||
				job.equals(three)) {
				count++;
			}
		}

		assertEquals(3, count);
	}

	@Test
	public void importFromResourceTest() throws CronJobException {

		service.importJobs("/dummy.json", false, 0);

		List<CronJob> list = service.list();
		assertEquals(3, list.size());

		String rootUrl = null;
		int count = 0;

		for (CronJob job : list) {
			switch (job.getName()) {

				case "one":
					count++;

					assertFalse(job.isDisabled());
					assertEquals("http://localhost/", job.getTarget(rootUrl));
					assertEquals(5, job.getInterval());
					assertEquals(RunEvery.minute, job.getIntervalUnit());
					assertEquals(10, job.getRunFromHour().intValue());
					assertEquals(30, job.getRunFromMinute().intValue());
					assertEquals(11, job.getRunToHour().intValue());
					assertEquals(45, job.getRunToMinute().intValue());
					break;

				case "two":
					count++;
					assertTrue(job.isDisabled());
					assertEquals("http://localhost/", job.getTarget(rootUrl));
					assertEquals(1, job.getInterval());
					assertEquals(RunEvery.day, job.getIntervalUnit());
					assertEquals(10, job.getRunFromHour().intValue());
					assertEquals(30, job.getRunFromMinute().intValue());
					assertNull(job.getRunToHour());
					assertNull(job.getRunToMinute());
					break;

				case "three":
					count++;
					assertFalse(job.isDisabled());
					assertEquals("http://localhost/", job.getTarget(rootUrl));
					assertEquals(1, job.getInterval());
					assertEquals(RunEvery.hour, job.getIntervalUnit());
					assertNull(job.getRunFromHour());
					assertNull(job.getRunFromMinute());
					assertNull(job.getRunToHour());
					assertNull(job.getRunToMinute());
					break;
			}
		}

		assertEquals(3, count);

		// change job ...
		CronJob job = manager.findByName("one");

		CronJob changed = service.update(job, new ScheduleUpdater("http://localhost", 5, RunEvery.minute));

		// re-import from same file
		service.importJobs("/dummy.json", true, 0);

		CronJob compare = manager.findByName("one");
		assertFalse(job.isDisabled());
		assertEquals("http://localhost/", job.getTarget(rootUrl));
		assertEquals(changed.getInterval(), compare.getInterval());
		assertEquals(changed.getIntervalUnit(), compare.getIntervalUnit());
		assertNull(compare.getRunToHour());
		assertNull(compare.getRunToMinute());
		assertNull(compare.getRunFromHour());
		assertNull(compare.getRunFromMinute());
	}

	@Test
	public void multipleThreadsRunningSameCronJobs() throws InterruptedException, CronJobException {


		int WORKERS = 3;
		Thread[] threads = new Thread[WORKERS];

		List<CronJobJSON> jobs = new ArrayList<>();
		jobs.add(new CronJobJSON(new DummyCronJob("one")));
		jobs.add(new CronJobJSON(new DummyCronJob("two")));
		jobs.add(new CronJobJSON(new DummyCronJob("three")));
		service.importJobs(jobs, 0);

		int[] count = new int[WORKERS];

		for (int i = 0; i < WORKERS; i++) {

			final int index = i;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {

					CronManager manager = new CronManagerImpl(sfy);
					CronExecutor executor = new SlowCronExecutor();

					CronServiceImpl service = new CronServiceImpl(manager, executor, null);
					count[index] = service.run();
				}
			};

			threads[i] = new Thread(runnable);
		}

		for (int i = 0; i < WORKERS; i++) {
			threads[i].start();
		}
		for (int i = 0; i < WORKERS; i++) {
			threads[i].join();
		}

		int total = 0;
		for (int i = 0; i < WORKERS; i++) {
			total = total + count[i];
		}

		assertEquals(3, total);
	}
}