/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pca9685control.internal;

import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.openhab.binding.pca9685control.pca9685controlBindingProvider;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.gpio.extension.pca.PCA9685Pin;
import com.pi4j.io.gpio.Pin;

	

/**
 * Implement this class if you are going create an actively polling service
 * like querying a Website/Device.
 * 
 * @author MichaelP
 * @since 1.0
 */
public class pca9685controlBinding extends AbstractActiveBinding<pca9685controlBindingProvider> {

	private static final Logger logger = 
		LoggerFactory.getLogger(pca9685controlBinding.class);	
		
	/**
	 * The BundleContext. This is only valid when the bundle is ACTIVE. It is set in the activate()
	 * method and must not be accessed anymore once the deactivate() method was called or before activate()
	 * was called.
	 */
	private BundleContext bundleContext;

	
	/** 
	 * the refresh interval which is used to poll values from the pca9685control
	 * server (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 5000;
	
	public pca9685controlBinding() {
		logger.debug("pca9685controlBinding binding started");
	}			
	
	/**
	 * Called by the SCR to activate the component with its configuration read from CAS
	 * 
	 * @param bundleContext BundleContext of the Bundle that defines this component
	 * @param configuration Configuration properties for this component obtained from the ConfigAdmin service
	 */
	public void activate(final BundleContext bundleContext, final Map<String, Object> configuration) {
		this.bundleContext = bundleContext;	
			
		// to override the default refresh interval one has to add a 
		// parameter to openhab.cfg like <bindingName>:refresh=<intervalInMs>
		String refreshIntervalString = (String) configuration.get("refresh");
		if (StringUtils.isNotBlank(refreshIntervalString)) {
			refreshInterval = Long.parseLong(refreshIntervalString);
		}
		
		setProperlyConfigured(true);
	}
	
	/**
	 * Called by the SCR when the configuration of a binding has been changed through the ConfigAdmin service.
	 * @param configuration Updated configuration properties
	 */
	public void modified(final Map<String, Object> configuration) {
		// update the internal configuration accordingly
	}
	
	/**
	 * Called by the SCR to deactivate the component when either the configuration is removed or
	 * mandatory references are no longer satisfied or the component has simply been stopped.
	 * @param reason Reason code for the deactivation:<br>
	 * <ul>
	 * <li> 0 – Unspecified
     * <li> 1 – The component was disabled
     * <li> 2 – A reference became unsatisfied
     * <li> 3 – A configuration was changed
     * <li> 4 – A configuration was deleted
     * <li> 5 – The component was disposed
     * <li> 6 – The bundle was stopped
     * </ul>
	 */
	public void deactivate(final int reason) {
		this.bundleContext = null;
		// deallocate resources here that are no longer needed and 
		// should be reset when activating this binding again
	}

	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected String getName() {
		return "pca9685control Refresh Service";
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void execute() {
		// the frequently executed code (polling) goes here ...		
//		logger.debug("execute() method is called! (pca9685control) ItemNames: {}, Addresses: {}", providers.iterator().next().getItemNames().toString(), providers.iterator().next().getPCA9685Map().keySet());
		eventPublisher.postCommand("pca9685controlBindingStatus", StringType.valueOf("Addresses given in item-config: " + providers.iterator().next().getPCA9685Map().keySet()));		
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		// the code being executed when a command was sent on the openHAB
		// event bus goes here. This method is only called if one of the
		// BindingProviders provide a binding for the given 'itemName'.
//		logger.debug("pca9685control: internalReceiveCommand({},{}) is called!", itemName, command);
		
		for (pca9685controlBindingProvider provider : providers) {
			int i2cAddress = provider.getAddress(itemName);		
			Pin pin = PCA9685Pin.ALL[provider.getPinNumber(itemName)];
			logger.debug("pca9685control: internalReceiveCommand: Address: {}, Pin: {}, PwmValue: {} is called!", i2cAddress, provider.getPinNumber(itemName), provider.getPwmValue(itemName));
			if(command == OnOffType.ON){
				provider.getPCA9685Map().get(i2cAddress).setOn(pin);
				provider.setPwmValue(itemName, 100);
			} else if(command == OnOffType.OFF) {
				provider.getPCA9685Map().get(i2cAddress).setOff(pin);
				provider.setPwmValue(itemName, 0);
			} else if(command == IncreaseDecreaseType.INCREASE){
				int pwmval = provider.getPwmValue(itemName);
				if(pwmval < 100){
					provider.setPwmValue(itemName, pwmval+1);
					provider.getPCA9685Map().get(i2cAddress).setPwm(pin, NaturalFading.STEPS_100[pwmval+1]);
				}
			} else if(command == IncreaseDecreaseType.DECREASE){
				int pwmval = provider.getPwmValue(itemName);
				if(pwmval > 0){
					provider.setPwmValue(itemName, pwmval-1);
					provider.getPCA9685Map().get(i2cAddress).setPwm(pin, NaturalFading.STEPS_100[pwmval-1]);
				}
			} else {				
				try{
					//Hopefully command is a integer value 0-100.
					Integer value = Integer.parseInt(command.toString());
					provider.getPCA9685Map().get(i2cAddress).setPwm(pin, NaturalFading.STEPS_100[value]);
					provider.setPwmValue(itemName, value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveUpdate(String itemName, State newState) {
		// the code being executed when a state was sent on the openHAB
		// event bus goes here. This method is only called if one of the 
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveUpdate({},{}) is called!", itemName, newState);
	}	
}
