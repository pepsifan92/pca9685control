# pcf9685control
Controlling the chip pcf9685 (16ch-PWM-Controller) via I2C from Raspberry Pi from openHAB

The chip PCF8591 is an PWM-driver.<br>
**Addressrange:** 64-128 (0x40-0x80)<br>
**Pinrange:** 0-15<br>

## Config in *.item file
In the **items-file** of openHAB the following **configuration** is needed:<br>
`Switch|Dimmer Name-of-Item { pcf9685control="I2CAddressInDecimal;PinNumber" }`

**Example:**<br>
`Switch led { pcf9685control="64;0" }` <br>
This would make the Pin 0 from the PCF8591 chip with the address 0x40 (64 in decimal) switchable.

`Dimmer led { pcf9685control="64;1" }` <br>
This would make the Pin 1 from the PCF8591 chip with the address 0x40 (64 in decimal) dimable. Values can be sent by a slider or from mapping with values 0-100.

## Config in *.sitemap file
**Example** to control from the website:<br>
`Slider item=led sendFrequency=30 switchSupport`<br> 
`Switch item=led`<br>
`Switch item=led mappings=[0="Off",50="mid",100="Full"]`
