package com.commsen.em.contract.storage;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.osgi.resource.Requirement;

public interface ContractStorage extends Closeable {

	boolean saveContractor (File contractor, String coordinates) throws IOException;

	Set<String> getContractors (Requirement requirement);

	Set<String> getAllContracts ();

	static ContractStorage instance() throws IOException {
		return new NitriteContractStorage();
	}
}
