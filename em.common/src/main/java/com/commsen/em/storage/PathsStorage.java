package com.commsen.em.storage;

import java.io.Closeable;
import java.nio.file.Path;

public interface PathsStorage extends Closeable {

	void savePaths(Path m2path, Path projectPath, Path emPath);
	
	Path getM2Path (Path path);

	Path getProjectPath (Path path);

	Path getEmPath (Path path);

}
