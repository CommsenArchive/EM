package em.contractors.karaf.scheduler;

import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.Requires;
import com.commsen.em.annotations.RequiresLogging;

@Provides( //
		value = "scheduler", //
		options = { //
				"provider=karaf" //
		} //
)
@Requires(raw = "osgi.identity;filter:='(osgi.identity=org.apache.karaf.scheduler.core)'")
@RequiresLogging
public class KarafSchedulerContracts {}
