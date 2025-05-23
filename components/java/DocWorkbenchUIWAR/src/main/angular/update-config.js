/** =============================================================================================================== *
 * Copyright 2025 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

const fs = require('fs');
const path = require('path');

const configFileSubPath = process.argv[2];
if (!configFileSubPath) {
  console.error('Please provide a configFileSubPath as an argument.');
  process.exit(1);
}

const tenantId = process.argv[3];
if (!tenantId) {
  console.error('Please provide a tenantId as an argument.');
  process.exit(1);
}

const alphaNumericHyphenRegex = /^[a-zA-Z0-9-]+$/;
if (!alphaNumericHyphenRegex.test(tenantId)) {
  console.error('Invalid tenantId. Please provide an alphanumeric string with only hyphens allowed.');
  process.exit(1);
}

const configFilePath = path.join(__dirname, 'src', 'assets', configFileSubPath);

fs.readFile(configFilePath, 'utf8', (err, data) => {
  if (err) {
    console.error('Error reading config file:', err);
    process.exit(1);
  }

  const config = JSON.parse(data);
  config.config.tenantId = tenantId;

  fs.writeFile(configFilePath, JSON.stringify(config,
    null,
    2), 'utf8', (err) => {
      if (err) {
        console.error('Error writing config file:', err);
        process.exit(1);
      }

      console.log('Config file updated successfully.');
    });
});