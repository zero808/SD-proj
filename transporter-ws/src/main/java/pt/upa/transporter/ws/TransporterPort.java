package pt.upa.transporter.ws;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

import javax.jws.HandlerChain;
import javax.jws.WebService;

@WebService(endpointInterface = "pt.upa.transporter.ws.TransporterPortType", wsdlLocation = "transporter.1_0.wsdl", name = "TransporterWebService", portName = "TransporterPort", targetNamespace = "http://ws.transporter.upa.pt/", serviceName = "TransporterService")
@HandlerChain(file = "handler-chain.xml")
public class TransporterPort implements TransporterPortType {

	private final String[] regiaoNorte = { "Porto", "Braga", "Viana do Castelo", "Vila Real", "Bragança" };
	private final String[] regiaoCentro = { "Lisboa", "Leiria", "Santarem", "Castelo Branco", "Coimbra", "Aveiro",
			"Viseu", "Guarda" };
	private final String[] regiaoSul = { "Setubal", "Évora", "Portalegre", "Beja", "Faro" };

	private String companyName;
	private CompanyType type;
	private int jobId = 0;
	private ConcurrentHashMap<String, JobView> jobs = new ConcurrentHashMap<String, JobView>();

	public String ping(String name) {
		String teste = getCompanyName() + " responding to ping from " + name;
		System.out.println(teste);
		return teste;
	}

	public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {

		System.out.println("Received a job request.");

		// Check for malformed request
		if (origin == null) {
			BadLocationFault locationinfo = new BadLocationFault();
			locationinfo.setLocation(origin);
			throw new BadLocationFault_Exception("Invalid origin", locationinfo);
		} else if (destination == null) {
			BadLocationFault locationinfo = new BadLocationFault();
			locationinfo.setLocation(destination);
			throw new BadLocationFault_Exception("Invalid destination", locationinfo);
		} else if (price < 0) {
			BadPriceFault faultInfo = new BadPriceFault();
			faultInfo.setPrice(price);
			throw new BadPriceFault_Exception("invalid price", faultInfo);
		}

		// Check if the Cities are in company's servable region
		if (!isServable(origin) || !isServable(destination) || (price > 100)) {
			return null;
		}

		System.out.println("From " + origin + " to " + destination + " for the price of " + price);

		JobView job = new JobView();
		job.setCompanyName(getCompanyName());
		job.setJobIdentifier(Integer.toString(jobId++));
		job.setJobOrigin(origin);
		job.setJobDestination(destination);
		job.setJobState(JobStateView.PROPOSED);
		job.setJobPrice(generatePrice(price));

		jobs.put(job.getJobIdentifier(), job);

		return job;
	}

	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {

		if (id == null) {
			BadJobFault faultInfo = new BadJobFault();
			faultInfo.setId(id);
			throw new BadJobFault_Exception("Id is null.", faultInfo);
		}

		JobView jv = jobs.get(id);

		if (jv == null) {
			BadJobFault faultInfo = new BadJobFault();
			faultInfo.setId(id);
			throw new BadJobFault_Exception("There isn't a Job with this id", faultInfo);
		}

		if (jv.getJobState() != JobStateView.PROPOSED) {
			BadJobFault faultInfo = new BadJobFault();
			faultInfo.setId(id);
			throw new BadJobFault_Exception("This job has already been decided.", faultInfo);
		}
		
		if (accept) {
			System.out.println("job " + id + " was accepted.");
			jv.setJobState(JobStateView.ACCEPTED);
			Runnable task = new TravelSimulator(jv);
			Thread worker = new Thread(task);
			worker.start();
		} else {
			System.out.println("job " + id + " was rejected.");
			jv.setJobState(JobStateView.REJECTED);
		}

		return jv;
	}

	/**
	 * Makes a copy of a JobView
	 * 
	 * @return the copy
	 */
	private JobView copyJobView(JobView orig) {
		JobView job = new JobView();
		job.setCompanyName(orig.getCompanyName());
		job.setJobIdentifier(orig.getJobIdentifier());
		job.setJobOrigin(orig.getJobOrigin());
		job.setJobDestination(orig.getJobDestination());
		job.setJobState(orig.getJobState());
		job.setJobPrice(orig.getJobPrice());
		return job;
	}

	public JobView jobStatus(String id) {
		System.out.println("Responding to a status check for job with id '" + id + "'.");

		if (id == null) {
			return null;
		}

		JobView temp = jobs.get(id), job;
		if (temp != null) {
			job = copyJobView(temp);
			return job;
		}
		return null;
	}

	public ArrayList<JobView> listJobs() {
		System.out.println("List jobs request.");
		final ArrayList<JobView> copy = new ArrayList<JobView>(jobs.values());
		
		copy.sort(new Comparator<JobView>() {
			public int compare(JobView job1, JobView job2) {
		        return job1.getJobIdentifier().compareTo(job2.getJobIdentifier());
		    }
		});
		
		return copy;
	}

	public void clearJobs() {
		System.out.println("All jobs cleared.");
		jobs.clear();
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public CompanyType getType() {
		return type;
	}

	public void setType(String name) {
		int number = Integer.parseInt(name.replaceAll("[\\D]", "")) % 2;
		CompanyType temp = (number == 1) ? CompanyType.SUL : CompanyType.NORTE;
		this.type = temp;
	}

	public String findRegion(String location) throws BadLocationFault_Exception {

		for (String city : regiaoNorte) {
			if (city.equals(location))
				return "NORTE";
		}
		for (String city : regiaoCentro) {
			if (city.equals(location))
				return "CENTRO";
		}
		for (String city : regiaoSul) {
			if (city.equals(location))
				return "SUL";
		}

		BadLocationFault locationinfo = new BadLocationFault();
		locationinfo.setLocation(location);
		throw new BadLocationFault_Exception("Unknown location", locationinfo);
	}

	boolean isServable(String location) throws BadLocationFault_Exception {

		String region = findRegion(location);

		// Todas as empresas podem servir cidades da regiao centro
		if (region.equals("CENTRO")) {
			return true;
		}
		if (region.equals(this.getType().toString()))
			return true;

		return false;
	}

	public int generatePrice(int referencePrice) {

		if (referencePrice == 0) {
			return 0;
		}

		if (referencePrice <= 10 && referencePrice >= 1) {
			return 1 + (int) Math.random()*(referencePrice - 2);
		}

		int price = (int) (Math.random()*(referencePrice - 2) + 1);
		
		if (this.getType() == CompanyType.NORTE) {
			if (referencePrice % 2 == 0) {
				return price;
			} else {
				return price + referencePrice + 1;
			}
		} else {
			if (referencePrice % 2 == 1) {
				return price;
			} else {
				return price + referencePrice + 1;
			}
		}
	}
}
