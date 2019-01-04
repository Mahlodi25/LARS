package bsi.lars.backend;

import java.util.EventObject;

/**
 * Event for calculation changes
 * 
 *
 */
public class UpdateCalculationEvent extends EventObject {

	private static final long serialVersionUID = 2201178591449133260L;

	public UpdateCalculationEvent(Object source) {
		super(source);
	}
	
}
