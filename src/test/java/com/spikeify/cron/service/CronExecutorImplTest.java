package com.spikeify.cron.service;

import com.spikeify.Spikeify;
import com.spikeify.cron.TestHelper;
import com.spikeify.cron.data.CronExecutorResult;
import com.spikeify.cron.data.ScheduleUpdater;
import com.spikeify.cron.entities.CronJob;
import com.spikeify.cron.entities.enums.CronJobResult;
import com.spikeify.cron.entities.enums.RunEvery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CronExecutorImplTest {

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
		service = new CronServiceImpl(manager, executor);
	}

	@After
	public void tearDown() {

		sfy.truncateNamespace(sfy.getNamespace());
	}

	@Test
	public void testRun() throws Exception {

		CronJob job = manager.create("job");
		// 1.
		CronExecutorResult result = executor.run(job, null);
		assertNotNull(result);
		Assert.assertEquals(CronJobResult.fail, result.getJobResult());
		assertEquals("Unable to run: " + job.getId() + " [job] - no schedule defined (job will not run)!", result.getMessage());

		// 2.
		job = manager.update(job, new ScheduleUpdater("http://localhost:8080/", 1, RunEvery.minute));

		result = executor.run(job, null);
		assertNotNull(result);
		Assert.assertEquals(CronJobResult.fail, result.getJobResult());
		assertEquals("Connection refused", result.getMessage());
	}
}