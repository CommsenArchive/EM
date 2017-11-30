package com.commsen.em.contract.storage;

import static com.commsen.em.maven.util.Constants.VAL_EM_HOME;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.codehaus.plexus.component.annotations.Component;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import static org.dizitart.no2.filters.Filters.*;

import com.commsen.em.storage.PathsStorage;

@Component(role = PathsStorage.class)
class NitritePathStorage implements PathsStorage {

	static String storage = VAL_EM_HOME + "/paths.db";

	private ThreadLocal<Nitrite> db = new ThreadLocal<>();
	
	@Override
	public void savePaths(Path m2path, Path projectPath, Path emPath) {
		
		String m2P = m2path.toString(), pP = projectPath.toString(), emP = emPath.toString();
		
		NitriteCollection collection = getDb().getCollection("paths");
		collection.remove(or(eq("m2path", m2P), eq("projectPath", pP), eq("emPath", emP)));
		Document doc = Document.createDocument("m2path", m2P).put("projectPath", pP).put("emPath", emP);
		collection.insert(doc);
	}

	@Override
	public Path getEmPath(Path path) {
		String p = path.toString();
		NitriteCollection collection = getDb().getCollection("paths");
		Document result = collection.find(or(eq("m2path", p), eq("projectPath", p))).firstOrDefault();
		return result == null ? path : Paths.get(result.get("emPath", String.class));
	}

	@Override
	public Path getM2Path(Path path) {
		String p = path.toString();
		NitriteCollection collection = getDb().getCollection("paths");
		Document result = collection.find(or(eq("emPath", p), eq("projectPath", p))).firstOrDefault();
		return result == null ? path : Paths.get(result.get("m2path", String.class));
	}

	@Override
	public Path getProjectPath(Path path) {
		String p = path.toString();
		NitriteCollection collection = getDb().getCollection("paths");
		Document result = collection.find(or(eq("emPath", p), eq("m2path", p))).firstOrDefault();
		return result == null ? path : Paths.get(result.get("projectPath", String.class));
	}

	@Override
	public void close() throws IOException {
		getDb().close();
	}

	private Nitrite getDb() {
		Nitrite nitriteDB = db.get();
		if (nitriteDB == null || nitriteDB.isClosed()) {
			nitriteDB = Nitrite.builder().compressed().filePath(storage).openOrCreate("user", "password");
			db.set(nitriteDB);
		}
		return nitriteDB;
	}
	
}
