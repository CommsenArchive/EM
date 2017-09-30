package em.contractors.karaf.shell;

import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.Requires;
import com.commsen.em.annotations.RequiresLogging;

@Provides( //
		value = "local.console", //
		options = { //
				"provider=karaf" //
		} //
)
@Requires(raw = "osgi.identity;filter:='(osgi.identity=org.apache.karaf.shell.core)'")
@Requires(raw = "osgi.identity;filter:='(osgi.identity=org.apache.karaf.shell.commands)'")
@Requires(raw = "osgi.identity;filter:='(osgi.identity=org.apache.karaf.bundle.core)'")
@RequiresLogging
public class KarafShellContracts {}
