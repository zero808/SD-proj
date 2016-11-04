package pt.upa.broker.ws.it.replication;

import static org.junit.Assert.*;

import org.junit.runners.MethodSorters;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

/**
 * Test suite
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class updateReplicaIT extends ReplicationAbstractIT {

	private String validOrigin = "Beja";
	private String validDestination = "Lisboa";
	private int validPrice = 15;

	// tests

	@Test
	@Ignore
	public void transportUpdateTest()
			throws UnknownTransportFault_Exception, InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		
		String id = mainServer.getFrontEnd().requestTransport(validOrigin, validDestination, validPrice);
		
		TransportView tVMain, tVReplica;
		tVMain = mainServer.getFrontEnd().viewTransport(id);
		tVReplica= replicaServer.getFrontEnd().viewTransport(id);
		
		assertEquals(tVMain.getOrigin(), tVReplica.getOrigin());
		assertEquals(tVMain.getDestination(), tVReplica.getDestination());
		assertEquals(tVMain.getPrice(), tVReplica.getPrice());
		assertEquals(tVMain.getState(), tVReplica.getState());
		assertEquals(tVMain.getTransporterCompany(), tVReplica.getTransporterCompany());
		
		int mainSize = mainServer.getFrontEnd().listTransports().size();
		int replicaSize = replicaServer.getFrontEnd().listTransports().size();
		
		assertEquals(mainSize, replicaSize);
	}
	
	@Test
	@Ignore
	public void clearSyncTest() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		mainServer.getFrontEnd().requestTransport(validOrigin, validDestination, validPrice);
		
		assertTrue(mainServer.getFrontEnd().listTransports().size() > 0);
		
		mainServer.getFrontEnd().clearTransports();
		
		int mainSize = mainServer.getFrontEnd().listTransports().size();
		int replicaSize = replicaServer.getFrontEnd().listTransports().size();
		
		assertTrue(mainSize == 0);
		assertTrue(replicaSize == 0);
		assertEquals(mainSize, replicaSize);
		
	}

	@Test
	@Ignore
	public void replicaTakeoverTest() throws InterruptedException {
		mainServer.getFrontEnd().kill();
		Thread.sleep(13000);
		
		mainServer.getFrontEnd().ping("hello");
		
	}
	
	@Test
	public void testInOrder() throws Exception {
		transportUpdateTest();
		clearSyncTest();
		replicaTakeoverTest();
	}
}
