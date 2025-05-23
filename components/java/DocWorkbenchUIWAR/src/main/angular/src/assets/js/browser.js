/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


function openChrome() {
  var shell = new ActiveXObject("WScript.Shell");
  shell.run("Chrome " + window.location.href);
}

if ((/trident/.test(navigator.userAgent.toLowerCase()))) {
  var message = "<center style='background-color:grey;height:100vh;color:white;padding:15%;'>"
  message += "<h4>This site works best on Chrome browser. "
  message += "<a style='color:yellow' onclick='openChrome();' href=''>Click here</a> to open in Chrome.</h4>"
  message += "</center>";
  document.getElementsByTagName("body")[0].innerHTML = message;
}
