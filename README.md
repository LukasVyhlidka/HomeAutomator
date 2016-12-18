# HomeAutomator

[eQ-3 MAX!](http://www.eq-3.com/products/max.html) is a heating home automation solution. It is great because you can set a temperature for every room in your home separatelly.

The only problem I have found is that it only controlls the valves on the radiators. For me, I am living in a house with own Boiler. To eQ-3 max to work properly I would have to make my Boiler to heat all the time, even in case all the Valves are turned off. That is because eQ-3 MAX does not support the Boiler switching based on a need of single thermostats/valves.

This project is going to add this support into the eQ-3 MAX! system. It is going to provide you a Remote Relay that switches the boiler on in case any of your room needs the heat and otherwise.

It is going to consist of following:

  * Java Based Server
    * Can run e.g. on a raspberry pi
    * Obtains the information from the eQ-3 MAX! Cube
    * Provides a REST api to the Boiler Relay module
  * Arduino based relay module to switch the Boiler
    * Checks the REST server from time to time
    * Sets the relay accordingly.
    
The status of the project is only a Draft. It is because I am waiting for my eQ-3 Cube and microchips (arduino, Wemos D1) to arrive (hope on Christmas). Then I am going to play and share the results with you.

So far, there is only the skeleton of the Server.
