package com.commsen.em.storage;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.osgi.resource.Requirement;

public interface ContractStorage {

	boolean saveContractor (File contractor, String coordinates) throws IOException;

	Set<String> getContractors (Requirement requirement);

	Set<String> getAllContracts ();

}
