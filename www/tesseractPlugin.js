
	var tesseractPlugin = {
		createEvent: function(str, callback) {
			cordova.exec(callback, function(err) {
			callback('Nothing to echo.');
				},
				"TesseractPlugin",			//Service: Mapped to native java.class
				"addTesseractPluginEntry",	//Action: The action name to call into on the native side
				[str]							//An array of arguments 
				);						//This string should contain path to image
												  
		 }
	}
	module.exports = tesseractPlugin;

