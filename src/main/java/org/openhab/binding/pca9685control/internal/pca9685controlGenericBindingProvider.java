/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pca9685control.internal;

import java.util.TreeMap;

import org.openhab.binding.pca9685control.pca9685controlBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author MichaelP
 * @since 1.0
 */
public class pca9685controlGenericBindingProvider extends AbstractGenericBindingProvider implements pca9685controlBindingProvider {

	private static final Logger logger = 
			LoggerFactory.getLogger(pca9685controlGenericBindingProvider.class);
	
	private TreeMap<Integer, PCA9685PwmControl> PCA9685Map = new TreeMap<>();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBindingType() {
		return "pca9685control";
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		if (!(item instanceof SwitchItem || item instanceof DimmerItem)) {
			throw new BindingConfigParseException("item '" + item.getName()
					+ "' is of type '" + item.getClass().getSimpleName()
					+ "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {		
		super.processBindingConfiguration(context, item, bindingConfig);
			
		String[] properties = bindingConfig.split(";");		
		pca9685controlConfig config = new pca9685controlConfig();
		try{
			config.address = Integer.parseInt(properties[0]);
			config.pinNumber = Integer.parseInt(properties[1]);			
			checkOfValidValues(config, item.getName());
		}catch(Exception e){
			e.printStackTrace();
		}
		logger.debug("pca9685controlGenericBindingProvider: processBindingConfiguration({},{}) is called!", config.address, config.pinNumber);
		//parse bindingconfig here ...
		
		addBindingConfig(item, config);
		handleBoards(config);
	}
	
	/* ================================= SELF WRITTEN METHODS - BEGIN ===============================*/
	
	private void checkOfValidValues(pca9685controlConfig config, String itemName){
		if(config.address < 64 && config.address > 128){
			throw new IllegalArgumentException("The given address '" + config.address + "'of the item '" + itemName + "' is invalid! PCA9685 must be between 64 and 128 (0x40 and 0x80)");
		}
		
		if(config.pinNumber < 0 && config.pinNumber > 15){
			throw new IllegalArgumentException("The pinNumber of the item '" + itemName + "'is invalid! Must be between 0-15.");
		}		
	}
		
	private void handleBoards(pca9685controlConfig config){
		try {
			if(!PCA9685Map.containsKey(config.address)){
				try{
					PCA9685Map.put(config.address, new PCA9685PwmControl(config.address));	
					logger.debug("handleBoards: added board with address: {} !", config.address);
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
			removeUnusedBoardsFromMap(config);
		} catch (Exception e) {
			logger.debug("Exception in handleBoards... however, it works.");
		}
	}
	
	private void removeUnusedBoardsFromMap(pca9685controlConfig config){
		//Check all Boards in map of usage. If not used, remove from map.
		keyLoop:
		for(Integer mapKey : PCA9685Map.keySet()){
			for(BindingConfig bindingConfig : bindingConfigs.values()){
				pca9685controlConfig conf = (pca9685controlConfig) bindingConfig;
				if(mapKey == conf.address){
					continue keyLoop;
				}				
			}
			PCA9685Map.remove(mapKey);
			logger.debug("handleBoards: removed board with address: {} !", mapKey);
		}
	}

	@Override
	public int getAddress(String itemName) {
		pca9685controlConfig config = (pca9685controlConfig) bindingConfigs.get(itemName);
		
		if (config == null) {
			throw new IllegalArgumentException("The item name '" + itemName + "'is invalid or the item isn't configured");
		}
		
		return config.address;
	}

	@Override
	public int getPinNumber(String itemName) {
		pca9685controlConfig config = (pca9685controlConfig) bindingConfigs.get(itemName);
		
		if (config == null) {
			throw new IllegalArgumentException("The item name '" + itemName + "'is invalid or the item isn't configured");
		}
		
		return config.pinNumber;
	}

	@Override
	public int getPwmValue(String itemName) {
		pca9685controlConfig config = (pca9685controlConfig) bindingConfigs.get(itemName);
		
		if (config == null) {
			throw new IllegalArgumentException("The item name '" + itemName + "'is invalid or the item isn't configured");
		}
		
		return config.pwmValue;
	}
	
	@Override
	public void setPwmValue(String itemName, int value) {
		pca9685controlConfig config = (pca9685controlConfig) bindingConfigs.get(itemName);
		
		if (config == null) {
			throw new IllegalArgumentException("The item name '" + itemName + "'is invalid or the item isn't configured");
		}
		
		config.pwmValue = value;
	}
	
	@Override
	public boolean isItemConfigured(String itemName) {
		if (bindingConfigs.containsKey(itemName)) {
			return true;
		}
		return false;
	}
	
	
	public class pca9685controlConfig implements BindingConfig{
		int address;
		int pinNumber;
		int pwmValue;
	}


	@Override
	public TreeMap<Integer, PCA9685PwmControl> getPCA9685Map() {		
		return PCA9685Map;
	}

	
	
	/* ================================= SELF WRITTEN METHODS - END ===============================*/
	
}
