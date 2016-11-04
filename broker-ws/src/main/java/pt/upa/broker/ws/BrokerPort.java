package pt.upa.broker.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.jws.WebService;
import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.TransporterClient;

@WebService(endpointInterface = "pt.upa.broker.ws.BrokerPortType", wsdlLocation = "broker.1_2.wsdl", name = "BrokerWebService", portName = "BrokerPort", targetNamespace = "http://ws.broker.upa.pt/", serviceName = "BrokerService")
public class BrokerPort implements BrokerPortType {

	private ConcurrentHashMap<TransportView, JobView> requestedJobs = new ConcurrentHashMap<TransportView, JobView>();
	private ConcurrentHashMap<String, TransporterClient> transporterCompanies = new ConcurrentHashMap<String, TransporterClient>();
	private SynchronizeState ss;
	private int transporterId = 0;

	private final String[] regiaoNorte = { "Porto", "Braga", "Viana do Castelo", "Vila Real", "Bragança" };
	private final String[] regiaoCentro = { "Lisboa", "Leiria", "Santarem", "Castelo Branco", "Coimbra", "Aveiro",
			"Viseu", "Guarda" };
	private final String[] regiaoSul = { "Setubal", "Évora", "Portalegre", "Beja", "Faro" };

	public void setSs(SynchronizeState ss) {
		this.ss = ss;
	}

	public String ping(String name) {
		String teste = "Broker responding to ping, " + name;
		System.out.println(teste);
		return teste;
	}

	public String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {

		System.out.println(
				"Received a transport request. From " + origin + " to " + destination + " up to a price of " + price);

		// Check arguments
		if (!isKnown(origin) || !isKnown(destination))
			doUnknownLocationFaultException("Origin or destination unknown", (!isKnown(origin)) ? origin : destination);
		if (price < 0)
			doInvalidPriceFaultException("Negative price", price);

		// Create the request
		TransportView tV = new TransportView();
		tV.setOrigin(origin);
		tV.setDestination(destination);
		tV.setPrice(price);
		tV.setTransporterCompany("");
		tV.setState(TransportStateView.REQUESTED);
		tV.setId(Integer.toString(transporterId++));

		// Get each company's offer
		JobView job = requestBudgets(tV);

		// Requested -> Budgeted
		if (job == null) {
			tV.setState(TransportStateView.FAILED);
			doUnavailableTransportFaultException("No jobs available for these locations.", origin, destination);
		}

		tV.setState(TransportStateView.BUDGETED);

		// Check if offer is good enough
		try {
			if (job.getJobPrice() > tV.getPrice()) {
				job = transporterCompanies.get(job.getCompanyName()).decideJob(job.getJobIdentifier(), false);
				tV.setState(TransportStateView.FAILED);
				doUnavailableTransportPriceFault("Offers are too expensive.", job.getJobPrice());
			} else {
				job = transporterCompanies.get(job.getCompanyName()).decideJob(job.getJobIdentifier(), true);
				tV.setPrice(job.getJobPrice());
			}

		} catch (BadJobFault_Exception e) {
			System.out.println("Job " + e.getFaultInfo().getId() + " not found.");
			tV.setState(TransportStateView.FAILED);
			return null;
		}

		// Budgeted -> Accepted/Rejected
		if (job.getJobState() == JobStateView.ACCEPTED) {
			tV.setTransporterCompany(job.getCompanyName());
			tV.setState(TransportStateView.BOOKED);
		} else
			tV.setState(TransportStateView.FAILED);

		requestedJobs.put(tV, job);
		if (ss != null)
			sync(tV, job.getCompanyName(), job.getJobIdentifier(), job.getJobState().toString(), job.getJobPrice(),
					job.getJobOrigin(), job.getJobDestination());

		return tV.getId();
	}

	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {

		if (id == null)
			doUnknownTransportFaultException("Id is null", id);

		System.out.println("Serving a viewTransport request with id: " + id);

		TransportView tV = null;
		for (TransportView t : requestedJobs.keySet()) {
			if (id.equals(t.getId())) {
				tV = t;
			}
		}
		if (tV == null)
			doUnknownTransportFaultException("Request not found", id);

		// update tV State with current associated jV State
		if (tV.getState() != TransportStateView.FAILED) {
			updateTransportStatus(tV);
		}

		return tV;
	}

	public List<TransportView> listTransports() {
		System.out.println("Serving a listTransports request");

		// Refresh each transport's status
		for (TransportView tv : requestedJobs.keySet()) {
			try {
				updateTransportStatus(tv);
			} catch (UnknownTransportFault_Exception e) {
				e.printStackTrace();
			}
		}

		// Sort the list
		ArrayList<TransportView> list = new ArrayList<TransportView>(requestedJobs.keySet());
		list.sort((tV1, tV2) -> tV1.getId().compareTo(tV2.getId()));
		return list;

	}

	public void clearTransports() {
		System.out.println("Serving a clearTransports request");
		for (TransporterClient tc : transporterCompanies.values()) {
			tc.clearJobs();
		}
		requestedJobs.clear();
		if (ss != null)
			ss.syncClear();
	}

	public void syncClear() {
		requestedJobs.clear();
	}

	private JobView requestBudgets(TransportView tV) {

		List<JobView> budgets = new ArrayList<JobView>();

		for (TransporterClient company : transporterCompanies.values())
			try {
				budgets.add(company.requestJob(tV.getOrigin(), tV.getDestination(), tV.getPrice()));
			} catch (BadLocationFault_Exception | BadPriceFault_Exception e) {
				// Ignore this because there is no job
			}

		// Get the job with the lowest price, reject all others
		JobView lowestbid = null;
		for (JobView job : budgets) {
			if (job == null)
				continue;
			if (lowestbid == null) {
				lowestbid = job;
			} else {
				try {
					if (job.getJobPrice() < lowestbid.getJobPrice()) {
						// Reject current lowest, replace it with 'job'
						transporterCompanies.get(lowestbid.getCompanyName()).decideJob(lowestbid.getJobIdentifier(),
								false);
						lowestbid = job;
					} else {
						transporterCompanies.get(job.getCompanyName()).decideJob(job.getJobIdentifier(), false);
					}
				} catch (BadJobFault_Exception e) {
				}
			}
		}
		return lowestbid;
	}

	private void updateTransportStatus(TransportView tV) throws UnknownTransportFault_Exception {
		JobView jV = transporterCompanies.get(tV.getTransporterCompany())
				.jobStatus(requestedJobs.get(tV).getJobIdentifier());

		if (jV == null)
			doUnknownTransportFaultException("Job not found in company", tV.getId());

		tV.setState(jobStateToTransportState(jV.getJobState()));
	}

	private TransportStateView jobStateToTransportState(JobStateView jobState) {
		if (jobState == JobStateView.ACCEPTED)
			return TransportStateView.BOOKED;
		if (jobState == JobStateView.HEADING)
			return TransportStateView.HEADING;
		if (jobState == JobStateView.ONGOING)
			return TransportStateView.ONGOING;
		if (jobState == JobStateView.COMPLETED)
			return TransportStateView.COMPLETED;
		if (jobState == JobStateView.REJECTED)
			return TransportStateView.FAILED;
		return null;
	}

	public boolean isKnown(String location) {
		for (String city : regiaoNorte) {
			if (city.equals(location))
				return true;
		}
		for (String city : regiaoCentro) {
			if (city.equals(location))
				return true;
		}
		for (String city : regiaoSul) {
			if (city.equals(location))
				return true;
		}
		return false;
	}

	@Override
	public void update(TransportView tv, String companyName, String jvId, String jobState, int jobPrice,
			String jobOrigin, String jobDestination) {

		JobView jV = new JobView();
		jV.setCompanyName(companyName);
		jV.setJobIdentifier(jvId);
		jV.setJobDestination(jobDestination);
		jV.setJobOrigin(jobOrigin);
		jV.setJobPrice(jobPrice);

		JobStateView state = null;
		switch (jobState) {
		case ("ACCEPTED"):
			state = JobStateView.ACCEPTED;
			break;
		case ("REJECTED"):
			state = JobStateView.REJECTED;
			break;
		case ("HEADING"):
			state = JobStateView.HEADING;
			break;
		case ("ONGOING"):
			state = JobStateView.ONGOING;
			break;
		case ("COMPLETED"):
			state = JobStateView.COMPLETED;
			break;
		}

		jV.setJobState(state);
		transporterId++;
		requestedJobs.put(tv, jV);

	}

	public void sync(TransportView tv, String companyName, String jvId, String jobState, int jobPrice, String jobOrigin,
			String jobDestination) {
		ss.set(tv, companyName, jvId, jobState, jobPrice, jobOrigin, jobDestination);
		ss.run();
	}

	public ConcurrentHashMap<TransportView, JobView> getRequestedJobs() {
		return requestedJobs;
	}

	public void addTransporterCompanies(String name, TransporterClient company) {
		this.transporterCompanies.put(name, company);
	}

	private void doUnknownLocationFaultException(String message, String location)
			throws UnknownLocationFault_Exception {
		UnknownLocationFault faultInfo = new UnknownLocationFault();
		faultInfo.setLocation(location);
		throw new UnknownLocationFault_Exception(message, faultInfo);
	}

	private void doInvalidPriceFaultException(String message, int price) throws InvalidPriceFault_Exception {
		InvalidPriceFault faultInfo = new InvalidPriceFault();
		faultInfo.setPrice(price);
		throw new InvalidPriceFault_Exception(message, faultInfo);

	}

	private void doUnavailableTransportFaultException(String message, String origin, String destination)
			throws UnavailableTransportFault_Exception {
		UnavailableTransportFault faultinfo = new UnavailableTransportFault();
		faultinfo.setOrigin(origin);
		faultinfo.setDestination(destination);
		throw new UnavailableTransportFault_Exception(message, faultinfo);
	}

	private void doUnavailableTransportPriceFault(String message, int jobPrice)
			throws UnavailableTransportPriceFault_Exception {
		UnavailableTransportPriceFault faultInfo = new UnavailableTransportPriceFault();
		faultInfo.setBestPriceFound(jobPrice);
		throw new UnavailableTransportPriceFault_Exception(message, faultInfo);
	}

	private void doUnknownTransportFaultException(String message, String id) throws UnknownTransportFault_Exception {
		UnknownTransportFault faultInfo = new UnknownTransportFault();
		faultInfo.setId(id);
		throw new UnknownTransportFault_Exception(message, faultInfo);
	}

	public Collection<TransporterClient> getTransporterCompanies() {
		return transporterCompanies.values();
	}

	public void kill(){
		System.out.println("Received kill command. Goodbye.");
		System.exit(1);
	}
	
}
