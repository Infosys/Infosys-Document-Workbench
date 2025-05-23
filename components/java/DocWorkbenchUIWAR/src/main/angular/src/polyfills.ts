/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

 /***************************************************************************************************
 * Load `$localize` onto the global scope - used if i18n tags appear in Angular templates.
 */
import '@angular/localize/init';

// Have added this manually as got global variable undefined error while build.
// This is used in annotator libs
(window as any).global = window;


/**
 * Required to support Web Animations `@angular/platform-browser/animations`.
 * Needed for: All but Chrome, Firefox and Opera. http://caniuse.com/#feat=web-animation
 **/
// import 'web-animations-js';  // Run `npm install --save web-animations-js`.

/**
 * By default, zone.js will patch all possible macroTask and DomEvents
 * user can disable parts of macroTask/DomEvents patch by setting following flags
 */

 // (window as any).__Zone_disable_requestAnimationFrame = true; // disable patch requestAnimationFrame
 // (window as any).__Zone_disable_on_property = true; // disable patch onProperty such as onclick
 // (window as any).__zone_symbol__BLACK_LISTED_EVENTS = ['scroll', 'mousemove']; // disable patch specified eventNames

 /*
 * in IE/Edge developer tools, the addEventListener will also be wrapped by zone.js
 * with the following flag, it will bypass `zone.js` patch for IE/Edge
 */
// (window as any).__Zone_enable_cross_context_check = true;

/***************************************************************************************************
 * Zone JS is required by default for Angular itself.
 */
import 'zone.js';  // Included with Angular CLI.
import 'web-animations-js/web-animations.min.js'; //NiaSln - Added for animations

/***************************************************************************************************
 * APPLICATION IMPORTS
 */
// TODO:Error while updating angular version 8.2 to 9.1 ,so commenting
//  import 'core-js/modules/es7.string.match-all';
// import 'core-js/modules/es7.array.includes';
// import 'core-js/modules/es7.string.pad-start';
// import 'core-js/modules/es7.object.values';
