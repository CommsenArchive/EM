package com.commesn.em.demo.scheduler;

import org.osgi.service.component.annotations.Component;

import com.commsen.em.annotations.RequiresScheduler;

@Component( //
		immediate = true, //
		property = { //
				"scheduler.expression=0/5 * * * * ?", // run every 5 seconds
				"scheduler.concurrent:Boolean=false" } //
) //
@RequiresScheduler
public class Reminder implements Runnable {

	@Override
	public void run() {
		System.out.println("I am an annoying reminder poping up every 5 seconds!");
	}

}
