package mal.core.util;

public class BagConfigException extends RuntimeException{

	private StringBuilder message = new StringBuilder();
	
	public BagConfigException(String msg)
	{
		message.append("Bag Config Initilization Failed Catastrophically: Fix the error and try again.\n");
		message.append(msg + "\n");
		throw this;
	}
	
	@Override
	public final String getMessage()
	{
		return message.toString();
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/