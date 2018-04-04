Infinilinc JavaScript API
=========================

The Infinilinc Android app exposes a simple asynchronous programming interface
via JavaScript. All methods are available through a global object called `nfc`.
For instance, a method `foobar()` would be invoked as `nfc.foobar()` in a
calling program.

This document lists the methods accessible through the interface and provides
descriptions with use cases.

An example application using the Infinilinc API is available in
/debug-web/public/debug.html. The example is currently deployed to the project
website at [https://infinilinc.firebaseapp.com/debug.html](https://infinilinc.firebaseapp.com/debug.html).
Full documentation for the example is available in /debug-web/.

Methods
-------

### `boolean exists()`

A trivial method used to verify the presence of the `nfc` object. Programs
should check for `nfc` using this method prior to using any other methods.

Always returns true.

### `void enable()`

Enables the NFC interface in the mode configured by `setMode()`. If `setMode()`
has not been invoked, card mode is started by default. This method should be
called when the program is ready to connect to other Infinilinc devices.

### `void disable()`

Disables the NFC interface and prevents new connections from being made.

### `void send(String str)`

Sends a string `str` to the NFC interface to be sent to the connected Infinilinc
device. In card mode, `str` is buffered and sent to the connected reader upon
request. In reader mode, `str` is sent to the connected Infinilinc card via a
send command sequence.

`str` can be of any length; fragmentation is handled automatically.

The send operation is conducted asynchronously, allowing `send()` to return
immediately. When the send is finished, `onSendComplete()` is called.

### `void receive()`

Receives a string from the connected device. In reader mode, `receive()` will
start a command exchange with the connected card to read the data buffered
to the card. In card mode, `receive()` will place the NFC interface in a
pending state until data has been received from the connected reader.

The receive operation is conducted asynchronously, allowing `receive()` to
return immediately. When the receive is finished, `onReceiveComplete()` is
called with the received data as its parameter.

### `void disconnect()`

Disconnects from a connected tag if the device is in reader mode. When in card
mode, this method does nothing.

### `void setMode(int mode)`

Configures the NFC interface to operate in either reader mode or card mode. In
reader mode, the device waits for a card to connect and controls the command
sequence for data transactions. In card mode, the device waits for commands from
a reader and responds to received commands.

`mode` should be `0` to place the device in card mode or `1` to place the device
in reader mode.

The default mode of the device is card mode (`0`).

### `int mode()`

Returns the current NFC mode as configured by `setMode()`. If `setMode()` has
not been called, the device defaults to card mode (`0`).

Returns `0` if in card mode or `1` if in reader mode.

### `boolean enabled()`

Gives the enabled status of the NFC interface.

Returns `true` if the NFC interface is enabled, `false` otherwise.

Callback methods
----------------

These methods are not defined by the Android app, but instead are function
references which should be overridden by the program to respond to and sequence
events. Code would look something like the following:

	nfc.onConnect = function() {
		alert('Connected to another Infinilinc device');
		doSomething();
	}

### `void onConnect()`

Invoked when the device is enabled and another Infinilinc device is connected.

### `void onDisconnect()`

Invoked when the connection with the previous Infinilinc device is lost.

### `void onSendComplete()`

Invoked when a send operation started using `send()` has completed.

### `void onReceive(String str)`

Invoked when a receive operation started using `receive()` has completed. A
single parameter, `str`, is provided containing the received data.
