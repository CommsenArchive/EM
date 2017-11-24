package com.commsen.em.contract.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Filter;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.filters.Filters;
import org.osgi.resource.Namespace;
import org.osgi.resource.Requirement;

import aQute.bnd.header.Attrs;
import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Domain;
import aQute.bnd.osgi.resource.FilterParser;
import aQute.bnd.osgi.resource.FilterParser.And;
import aQute.bnd.osgi.resource.FilterParser.Expression;
import aQute.bnd.osgi.resource.FilterParser.ExpressionVisitor;
import aQute.bnd.osgi.resource.FilterParser.Not;
import aQute.bnd.osgi.resource.FilterParser.Op;
import aQute.bnd.osgi.resource.FilterParser.Or;
import aQute.bnd.osgi.resource.FilterParser.SimpleExpression;

class NitriteContractStorage implements ContractStorage {

	Set<String> supportedNamespaces = new HashSet<>();
	String storage = System.getProperty("user.home") + "/.em/contracts.db";
	Nitrite db;

	public NitriteContractStorage() throws IOException {
		Files.createDirectories(Paths.get(storage).getParent());
		supportedNamespaces.add("em.contract");
		db = Nitrite.builder().compressed().filePath(storage).openOrCreate("user", "password");
	}

	public NitriteContractStorage(String storage) throws IOException {
		Files.createDirectories(Paths.get(storage).getParent());
		supportedNamespaces.add("em.contract");
		this.storage = storage;
		db = Nitrite.builder().compressed().filePath(storage).openOrCreate("user", "password");
	}

	@Override
	public boolean saveContractor(File contractor, String coordinates) throws IOException {

		Domain domain = Domain.domain(contractor);
		Parameters parameters = domain.getProvideCapability();

		NitriteCollection collection = db.getCollection("contracts");
		collection.remove(Filters.eq("contractor", coordinates));
		List<Document> docs = new LinkedList<>();
		for (Entry<String, Attrs> entry : parameters.entrySet()) {
			String namespace = entry.getKey();
			if (supportedNamespaces.contains(namespace)) {
				Document doc = Document.createDocument("ns", entry.getKey()).put("contractor", coordinates);
				for (Entry<String, String> field : entry.getValue().entrySet()) {
					if (field.getKey().equals(namespace)) {
						doc.put(field.getKey().replace('.', '|'), field.getValue());
					}
				}
				docs.add(doc);
			}
		}
		if (docs.isEmpty()) {
			return false;
		} 
		
		collection.insert(docs.toArray(new Document[docs.size()]));
		return true;
	}

	@Override
	public Set<String> getAllContracts() {
		NitriteCollection collection = db.getCollection("contracts");
		Cursor cursor = collection.find();
		Set<String> result = new HashSet<>();
		for (Document document : cursor) {
			result.add(document.toString());
		}
		return result;
	}

	@Override
	public Set<String> getContractors(Requirement requirement) {
		FilterParser fp = new FilterParser();
		String filter = requirement.getDirectives().get(Namespace.REQUIREMENT_FILTER_DIRECTIVE);
		if (filter != null) {
			Expression expression = fp.parse(filter);
			NitriteVistor nitriteVistor = new NitriteVistor(requirement.getNamespace());
			Filter f = expression.visit(nitriteVistor);

			if (f == null) {
				return Collections.emptySet();
			}

			boolean knownRequirement = false;
			for (String string : supportedNamespaces) {
				if (filter.contains(string)) {
					knownRequirement = true;
					break;
				}
			}

			if (!knownRequirement) {
				return Collections.emptySet();
			}

			NitriteCollection collection = db.getCollection("contracts");
			Cursor cursor = collection.find(f);
			Set<String> result = new HashSet<>();
			for (Document document : cursor) {
				result.add(document.get("contractor", String.class));
			}
			return result;
		}
		return Collections.emptySet();
	}


	@Override
	public void close() {
		db.close();
	}

	class NitriteVistor extends ExpressionVisitor<Filter> {

		String namespace;

		public NitriteVistor(String namespace) {
			super(null);
			this.namespace = namespace;
		}

		@Override
		public Filter visit(SimpleExpression expr) {
			if (expr.getKey().equals(namespace) && expr.getOp() == Op.EQUAL) {
				return Filters.eq(expr.getKey().replace('.', '|'), expr.getValue());
			}
			return null;
		}

		@Override
		public Filter visit(And expr) {
			List<Filter> andFilters = new LinkedList<>();
			for (Expression expression : expr.getExpressions()) {
				Filter f = expression.visit(this);
				if (f != null) {
					andFilters.add(f);
				}
			}
			if (!andFilters.isEmpty()) {
				return Filters.and(andFilters.toArray(new Filter[andFilters.size()]));
			}
			return null;
		}

		@Override
		public Filter visit(Or expr) {
			List<Filter> orFilters = new LinkedList<>();
			for (Expression expression : expr.getExpressions()) {
				Filter f = expression.visit(this);
				if (f != null) {
					orFilters.add(f);
				}
			}
			if (!orFilters.isEmpty()) {
				return Filters.or(orFilters.toArray(new Filter[orFilters.size()]));
			}
			return null;
		}

		@Override
		public Filter visit(Not expr) {
			Filter f = expr.visit(this);
			if (f != null) {
				return Filters.not(f);
			}
			return null;
		}

	}
}
