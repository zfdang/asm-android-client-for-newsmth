package com.athena.asm.tool.infosense;

import java.util.ArrayList;
import java.util.List;

/**
 * EmailAddrSensro + PhoneNumSensor
 * @author aleck
 *
 */
public class MultiSensor extends Sensor {
	private Sensor email;
	private Sensor phone;

	public MultiSensor() {
		super(Type.MULTIPLE);
		email = new EmailAddrSensor();
		phone = new PhoneNumSensor();
	}

	@Override
	public List<Info> scan(CharSequence text) {
		List<Info> ret = new ArrayList<Info>();
		ret.addAll(email.scan(text));
		ret.addAll(phone.scan(text));
		return ret;
	}

}
