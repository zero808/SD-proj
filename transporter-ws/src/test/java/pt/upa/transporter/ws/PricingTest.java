package pt.upa.transporter.ws;

import static org.junit.Assert.*;

import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PricingTest {

	// static members
	private static TransporterPort port;
	private static String companyName = "UpaTransporter1";
	private static String companyName2 = "UpaTransporter2";
	private static String northLocation = "Porto";
	private static String centralLocation = "Lisboa";
	private static String southLocation = "Beja";
	private int smallPrice = 6;
	private int bigPrice = 150;
	private int validPrice = 50;
	private int invalidPrice = -50;
	private int testIterations = 300;
	private static PrintStream originalStream;

	@BeforeClass
	public static void oneTimeSetUp() {
		originalStream = System.out;
		// Silence Output
		System.setOut(new PrintStream(new OutputStream() {
			public void write(int b) {
				// NO-OP
			}
		}));
	}

	@AfterClass
	public static void oneTimeTearDown() {
		port = null;
		System.setOut(originalStream);
	}

	@Before
	public void setUp() {
		port = new TransporterPort();
		port.setCompanyName(companyName);
		port.setType(companyName);
	}

	@After
	public void tearDown() {
		port = null;
	}

	// tests

	@Test(expected = BadPriceFault_Exception.class)
	public void invalidPriceTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		port.requestJob(centralLocation, southLocation, invalidPrice);
	}

	@Test
	public void lessThanTenTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		int jobPrice;
		for (int i = 0; i < testIterations; i++) {
			jobPrice = port.requestJob(centralLocation, southLocation,
					smallPrice).getJobPrice();
			assertTrue("jobPrice is not lower than 10", jobPrice < smallPrice);
		}
	}

	@Test
	public void maximumPriceTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		for (int i = 0; i < testIterations; i++) {
			JobView job = port.requestJob(centralLocation, southLocation,
					bigPrice + i);
			assertNull("The response is not null",job);
		}
	}

	@Test
	public void NorthCompanyEvenPrice() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		port = new TransporterPort();
		port.setCompanyName(companyName2);
		port.setType(companyName2);
		int returnedPrice;

		for (int i = 0; i < testIterations; i++) {
			returnedPrice = port.requestJob(centralLocation, northLocation,
					validPrice).getJobPrice();
			assertTrue("jobPrice is not lower than proposed price",returnedPrice < validPrice);
		}
	}

	@Test
	public void NorthCompanyOddPrice() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		port = new TransporterPort();
		port.setCompanyName(companyName2);
		port.setType(companyName2);
		int returnedPrice;

		for (int i = 0; i < testIterations; i++) {
			returnedPrice = port.requestJob(centralLocation, northLocation,
					validPrice + 1).getJobPrice();
			assertTrue("jobPrice is not higher than proposed price", returnedPrice > validPrice);
		}
	}

	@Test
	public void SouthCompanyEvenPrice() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		int returnedPrice;

		for (int i = 0; i < testIterations; i++) {
			returnedPrice = port.requestJob(centralLocation, southLocation,
					validPrice).getJobPrice();
			assertTrue("jobPrice is not higher than proposed price",returnedPrice > validPrice);
		}
	}

	@Test
	public void SouthCompanyOddPrice() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		int returnedPrice;

		for (int i = 0; i < testIterations; i++) {
			returnedPrice = port.requestJob(centralLocation, southLocation,
					validPrice + 1).getJobPrice();
			assertTrue("jobPrice is not lower than proposed price", returnedPrice < validPrice);
		}
	}

	@Test
	public void ZeroPrice() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		int returnedPrice;

		for (int i = 0; i < testIterations; i++) {
			returnedPrice = port.requestJob(centralLocation, southLocation, 0)
					.getJobPrice();
			assertTrue("jobPrice is not 0",returnedPrice == 0);
		}
	}

	@Test
	public void correctPricingTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {

		JobView job, job2;
		TransporterPort port2 = new TransporterPort();
		port2.setCompanyName(companyName2);
		port2.setType(companyName2);

		for (int i = 11; i <= 100; i++) {
			if (i % 2 == 0) { // even price
				job = port.requestJob(centralLocation, southLocation, i);
				job2 = port2.requestJob(centralLocation, northLocation, i);
				assertTrue("jobPrice is not higher than proposed price", job.getJobPrice() > i);
				assertTrue("jobPrice is not lower than proposed price", job2.getJobPrice() < i);

			} else if (i % 2 == 1) { // odd price
				job = port.requestJob(centralLocation, southLocation, i);
				job2 = port2.requestJob(centralLocation, northLocation, i);
				assertTrue("jobPrice is not lower than proposed price", job.getJobPrice() < i);
				assertTrue("jobPrice is not higher than proposed price", job2.getJobPrice() > i);
			}
		}
		for (int j = 101; j < testIterations; j++) {
			job = port.requestJob(centralLocation, southLocation, j);
			job2 = port2.requestJob(centralLocation, northLocation, j);
			assertNull("The response not is null", job);
			assertNull("The response not is null", job2);
		}
	}

}
