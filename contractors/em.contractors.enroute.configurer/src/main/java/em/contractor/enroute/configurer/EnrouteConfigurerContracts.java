package em.contractor.enroute.configurer;

import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.Requires;

@Provides(value="configurator")
@Requires(raw="osgi.identity;filter:='(&(osgi.identity=osgi.enroute.configurer.simple.provider)(version>=2.0.0))'")
public class EnrouteConfigurerContracts {}
