package com.commsen.em.contract.storage;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.osgi.resource.Requirement;

public interface ContractStorage {

	void saveContractor (File contractor, String coordinates) throws IOException;

	Set<String> getContractors (Requirement requirement);

	Set<String> getAllContracts ();

}
