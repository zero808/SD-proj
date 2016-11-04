package pt.upa.transporter.ws;

import java.util.Random;

public class TravelSimulator implements Runnable {

	private JobView job;
	
	public TravelSimulator(JobView job){
		this.job = job;
	}
	
	public void run() {
		Random rn = new Random();
		int interval;
		
		try {
			interval = rn.nextInt(4000) + 1000;
			Thread.sleep(interval);		
			job.setJobState(JobStateView.HEADING);
			System.out.println("Job with id '" + job.getJobIdentifier() + "' is now heading to destination.");
			
			interval = rn.nextInt(4000) + 1000;
			Thread.sleep(interval);
			job.setJobState(JobStateView.ONGOING);
			
			interval = rn.nextInt(4000) + 1000;
			Thread.sleep(interval);
			job.setJobState(JobStateView.COMPLETED);
			System.out.println("Job with id '" + job.getJobIdentifier() + "' as arrived. Job completed.");
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}