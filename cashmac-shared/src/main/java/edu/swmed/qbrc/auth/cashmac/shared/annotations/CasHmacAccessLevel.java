package edu.swmed.qbrc.auth.cashmac.shared.annotations;

import edu.swmed.qbrc.auth.cashmac.shared.constants.CasHmacAccessLevels;

public @interface CasHmacAccessLevel {
	String[] value() default { CasHmacAccessLevels.NONE }; 
}
