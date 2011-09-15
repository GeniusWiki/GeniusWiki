/**
Source from http://code.google.com/p/glazkov-attic/ License under MIT license
http://www.opensource.org/licenses/mit-license.php
Some modifications are made by Edgenius

This function will detect if system is online. If system is online, it only run once and stop. 
Otherwise, it keep going to ping server until system goes online.  
**/
// provides connection monitoring
    // controller
    function Monitor(serverUrl) {
 
    	
        var me = this;
    
        //  triggered when connection changes
        //  sends as parameter:
        //      online : Boolean, true if connection became available,
        //          false if connection is broken
        this.onconnectionchange = nil;
    
        // starts the monitoring
        this.start = function() {
            try {
                wp = google.gears.factory.create('beta.workerpool', '1.0');
            }
            catch(e) {
                return false;
            }
            wp.onmessage = function(a, b, message) {
                if (message.sender == id) {
                    // only two messages: 
                    // first [f]ailure to connect 
                    // or back [o]nline
                    var text = message.text;
                    if (text == 'f') {
                        me.onconnectionchange(false);
                    }
                    else if (text == 'o') {
                        me.onconnectionchange(true);
                    }
                }
            }
            id = wp.createWorker(String(worker) + ';worker()');
            // send a message to the worker, identifying owner's id
            wp.sendMessage(serverUrl, id);
            return true;
        }
        
        function worker() {
            
            
            var wp = google.gears.workerPool;
            var url;
            var parentId;
            
            var first = true;
            var online;
            
            var timer = google.gears.factory.create('beta.timer', '1.0');
            
            wp.onmessage = function(a, b, message) {
                url = message.text;
                parentId = message.sender;
                poll();
            }
            
            function poll() {
               	var POLLING_INTERVAL = 5000;
                var request = google.gears.factory.create('beta.httprequest', '1.0');
                request.open('HEAD', url + "?" + String(Math.floor(Math.random()*10000)));
                request.onreadystatechange = function() {
                    if (request.readyState == 4) {
                        try {
                            if (request.status == 200) {
                                if (!online) {
                                    online = true;
                                    wp.sendMessage('o', parentId);
                                }
                            }
                        }
                        catch(e) {
                            if (online || first) {
                                online = false;
                                first = false;
                                wp.sendMessage('f', parentId);
                            }
                        }
                        if(!online){
                        	wp.sendMessage('d', parentId);
                        	timer.setTimeout(poll, POLLING_INTERVAL);
                        }
                    }
                }
                try {
                    request.send();
                }
                catch(e) {
                    if (online) {
                        online = false;
                        wp.sendMessage('f', parentId);
                        timer.setTimeout(poll, POLLING_INTERVAL);
                    }
                }
            }

        }
        
        function nil() {}
    }
