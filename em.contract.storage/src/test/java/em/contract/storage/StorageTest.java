package em.contract.storage;

import java.io.IOException;

import org.junit.Test;
import org.osgi.resource.Requirement;

import com.commsen.em.contract.storage.ContractStorage;
import com.commsen.em.contract.storage.NitriteContractStorage;

import aQute.bnd.osgi.resource.FilterParser;
import aQute.bnd.osgi.resource.FilterParser.Expression;

public class StorageTest {

	
	ContractStorage contractStorage;

	public StorageTest() throws IOException {
		contractStorage = new NitriteContractStorage();
	}
	
	@Test
	public void crud() throws IOException {

//		contractStorage.saveContractor(new Contract("a", "b"), "fake.contractor");
//
//		Set<String> contractors = contractStorage.getContractors(new Contract("a", "b"));
//
//		Assert.assertNotNull(contractors);
//
//		boolean found;
//
//		Assert.assertTrue(contractors.stream().filter(c -> "fake.contractor".equals(c)).count() > 0);

	}

	@Test
	public void fromFilter() throws IOException {

//		FilterParser fp = new FilterParser();
//		
//		Requirement 
//		Expression expression = fp.parse("(|(&(a=1)(b=2))(&(!(c=3))(version>=5)))");
//		contractStorage.getContractors(expression);
	}
}
